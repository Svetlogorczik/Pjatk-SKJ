import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Builds a DNS query BY HAND (byte by byte), sends it over UDP to port 53 and decodes the answer.
 *
 * Run:  java DnsLookup example.com
 *       java DnsLookup pjwstk.edu.pl MX
 *       java DnsLookup www.wikipedia.org A 1.1.1.1
 */
public class DnsLookup {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java DnsLookup <domain> [A|AAAA|NS|MX|CNAME] [dns-server]");
            System.exit(1);
        }
        String domain = args[0];
        int qtype = typeCode(args.length > 1 ? args[1] : "A");
        String server = args.length > 2 ? args[2] : "8.8.8.8";

        int id = new Random().nextInt(0x10000);
        byte[] query = buildQuery(id, domain, qtype);
        System.out.println("Query (" + query.length + " bytes):");
        System.out.println(hex(query, query.length));

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000);
            socket.send(new DatagramPacket(query, query.length, InetAddress.getByName(server), 53));
            DatagramPacket reply = new DatagramPacket(new byte[512], 512);   // classic DNS/UDP limit
            socket.receive(reply);
            System.out.println("Answer (" + reply.getLength() + " bytes) from " + server + ":");
            System.out.println(hex(reply.getData(), reply.getLength()));
            decode(reply.getData(), reply.getLength(), id);
        } catch (SocketTimeoutException e) {
            System.out.println("No answer within 3 s (UDP datagram lost or port 53 blocked).");
        }
    }

    /** Header (12 bytes) + Question (QNAME, QTYPE, QCLASS). */
    static byte[] buildQuery(int id, String domain, int qtype) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);   // writeShort = 2 bytes, big-endian
        out.writeShort(id);          // ID - we will match the answer by it
        out.writeShort(0x0100);      // flags: QR=0 (query), OPCODE=0, RD=1 (recursion desired)
        out.writeShort(1);           // QDCOUNT: 1 question
        out.writeShort(0);           // ANCOUNT
        out.writeShort(0);           // NSCOUNT
        out.writeShort(0);           // ARCOUNT
        for (String label : domain.split("\\.")) {          // www.example.com -> 3 www 7 example 3 com 0
            byte[] ascii = label.getBytes(StandardCharsets.US_ASCII);
            out.writeByte(ascii.length);
            out.write(ascii);
        }
        out.writeByte(0);            // end of the name
        out.writeShort(qtype);       // QTYPE
        out.writeShort(1);           // QCLASS = IN (Internet)
        return bytes.toByteArray();
    }

    static void decode(byte[] m, int length, int expectedId) {
        int id = u16(m, 0);
        int flags = u16(m, 2);
        System.out.printf("ID: 0x%04X %s%n", id, id == expectedId ? "(matches our query)" : "(DIFFERENT - not our answer!)");
        System.out.printf("Flags: 0x%04X  QR=%d OPCODE=%d AA=%d TC=%d RD=%d RA=%d RCODE=%d%n", flags,
                flags >> 15 & 1, flags >> 11 & 0xF, flags >> 10 & 1, flags >> 9 & 1, flags >> 8 & 1, flags >> 7 & 1, flags & 0xF);
        String[] rcodes = {"NOERROR", "FORMERR", "SERVFAIL", "NXDOMAIN (no such name)", "NOTIMP", "REFUSED"};
        int rcode = flags & 0xF;
        System.out.println("RCODE " + rcode + " = " + (rcode < rcodes.length ? rcodes[rcode] : "?"));
        int qd = u16(m, 4), an = u16(m, 6), ns = u16(m, 8), ar = u16(m, 10);
        System.out.println("Questions: " + qd + ", Answer RRs: " + an + ", Authority RRs: " + ns + ", Additional RRs: " + ar);

        int[] pos = {12};
        for (int i = 0; i < qd; i++) {
            String name = readName(m, pos);
            System.out.println("Question: " + name + "  type=" + typeName(u16(m, pos[0])) + " class=" + u16(m, pos[0] + 2));
            pos[0] += 4;
        }
        for (int i = 0; i < an + ns + ar && pos[0] < length; i++) {
            String section = i < an ? "Answer" : i < an + ns ? "Authority" : "Additional";
            String name = readName(m, pos);
            int type = u16(m, pos[0]);
            long ttl = u32(m, pos[0] + 4);
            int rdLength = u16(m, pos[0] + 8);
            int rdata = pos[0] + 10;
            System.out.println(section + ": " + name + "  type=" + typeName(type) + "  TTL=" + ttl + " s  data=" + rdata(m, rdata, rdLength, type));
            pos[0] = rdata + rdLength;
        }
    }

    /** Reads a domain name; supports compression pointers (two bytes 11xxxxxx xxxxxxxx). */
    static String readName(byte[] m, int[] pos) {
        StringBuilder name = new StringBuilder();
        int p = pos[0];
        boolean jumped = false;
        for (int guard = 0; guard < 128; guard++) {
            int len = m[p] & 0xFF;
            if (len == 0) {
                p++;
                break;
            }
            if ((len & 0xC0) == 0xC0) {                  // pointer to an earlier place in the message
                int target = ((len & 0x3F) << 8) | (m[p + 1] & 0xFF);
                if (!jumped) {
                    pos[0] = p + 2;                      // after a pointer the name always ends
                }
                jumped = true;
                p = target;
                continue;
            }
            if (name.length() > 0) {
                name.append('.');
            }
            name.append(new String(m, p + 1, len, StandardCharsets.US_ASCII));
            p += 1 + len;
        }
        if (!jumped) {
            pos[0] = p;
        }
        return name.length() == 0 ? "." : name.toString();
    }

    static String rdata(byte[] m, int start, int len, int type) {
        switch (type) {
            case 1:  // A
                return (m[start] & 0xFF) + "." + (m[start + 1] & 0xFF) + "." + (m[start + 2] & 0xFF) + "." + (m[start + 3] & 0xFF);
            case 28: { // AAAA
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 16; i += 2) {
                    sb.append(i == 0 ? "" : ":").append(Integer.toHexString(u16(m, start + i)));
                }
                return sb.toString();
            }
            case 2:  // NS
            case 5:  // CNAME
                return readName(m, new int[]{start});
            case 15: // MX: preference + name
                return "pref " + u16(m, start) + " " + readName(m, new int[]{start + 2});
            default:
                return hex(java.util.Arrays.copyOfRange(m, start, start + len), len).replace('\n', ' ');
        }
    }

    static int typeCode(String t) {
        switch (t.toUpperCase()) {
            case "A": return 1;
            case "NS": return 2;
            case "CNAME": return 5;
            case "MX": return 15;
            case "AAAA": return 28;
            default: throw new IllegalArgumentException("Unknown type " + t);
        }
    }

    static String typeName(int code) {
        switch (code) {
            case 1: return "A";
            case 2: return "NS";
            case 5: return "CNAME";
            case 6: return "SOA";
            case 15: return "MX";
            case 28: return "AAAA";
            default: return String.valueOf(code);
        }
    }

    static int u16(byte[] m, int i) {
        return ((m[i] & 0xFF) << 8) | (m[i + 1] & 0xFF);
    }

    static long u32(byte[] m, int i) {
        return ((long) u16(m, i) << 16) | u16(m, i + 2);
    }

    static String hex(byte[] data, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", data[i] & 0xFF)).append(i % 16 == 15 ? "\n" : " ");
        }
        return sb.toString().trim();
    }
}
