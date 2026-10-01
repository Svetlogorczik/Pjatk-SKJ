import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class DNS_Answer {
    short id;
    boolean[] flags = new boolean[32];
    short questions;
    short answer_RRs;
    short authority_RRs;
    short additional_RRs;
    String qName;
    short qType;
    short qClass;
    String aName;
    short aType;
    short aClass;
    int time_to_live;
    short data_length;
    int adress;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("---- DNS Answer ----\n");

        // Header
        sb.append("ID: ").append(id & 0xFFFF).append("\n");

        sb.append("Flags: ");
        for (boolean f : flags) sb.append(f ? "1" : "0");
        sb.append("\n");

        sb.append("Questions: ").append(questions & 0xFFFF).append("\n");
        sb.append("Answer RRs: ").append(answer_RRs & 0xFFFF).append("\n");
        sb.append("Authority RRs: ").append(authority_RRs & 0xFFFF).append("\n");
        sb.append("Additional RRs: ").append(additional_RRs & 0xFFFF).append("\n");

        // Question section
        sb.append("\n-- Question --\n");
        sb.append("QName: ").append(qName).append("\n");
        sb.append("QType: ").append(qType & 0xFFFF).append("\n");
        sb.append("QClass: ").append(qClass & 0xFFFF).append("\n");

        // Answer section
        sb.append("\n-- Answer --\n");
        sb.append("AName: ").append(aName).append("\n");
        sb.append("AType: ").append(aType & 0xFFFF).append("\n");
        sb.append("AClass: ").append(aClass & 0xFFFF).append("\n");
        sb.append("TTL: ").append(time_to_live).append("\n");
        sb.append("Data Length: ").append(data_length & 0xFFFF).append("\n");

        // Convert IPv4 (int) to dotted format
        String ip = String.format("%d.%d.%d.%d",
                (adress >> 24) & 0xFF,
                (adress >> 16) & 0xFF,
                (adress >> 8) & 0xFF,
                adress & 0xFF);

        sb.append("Address: ").append(ip).append("\n");

        return sb.toString();
    }

    public DNS_Answer(byte[] data) {

        int index = 0;

        // -------- DNS HEADER (12 BYTES) --------
        id = (short) ((data[index++] << 8) | (data[index++] & 0xFF));

        // FLAGS (16 bits)
        short flagsShort = (short) ((data[index++] << 8) | (data[index++] & 0xFF));
        for (int i = 0; i < 16; i++) {
            flags[15 - i] = ((flagsShort >> i) & 1) == 1;
        }

        questions      = (short) ((data[index++] << 8) | (data[index++] & 0xFF));
        answer_RRs     = (short) ((data[index++] << 8) | (data[index++] & 0xFF));
        authority_RRs  = (short) ((data[index++] << 8) | (data[index++] & 0xFF));
        additional_RRs = (short) ((data[index++] << 8) | (data[index++] & 0xFF));

        // -------- QNAME --------
        StringBuilder qn = new StringBuilder();
        while (data[index] != 0) {
            int len = data[index++] & 0xFF;     // label length

            for (int i = 0; i < len; i++) {
                qn.append((char) data[index++]);
            }
            qn.append(".");
        }
        index++; // skip the zero byte
        if (qn.length() > 0) qn.setLength(qn.length() - 1);

        qName = qn.toString();

        // -------- QTYPE & QCLASS --------
        qType  = (short) ((data[index++] << 8) | (data[index++] & 0xFF));
        qClass = (short) ((data[index++] << 8) | (data[index++] & 0xFF));


        // ------ ANSWER SECTION (A record only) ------
        // NAME (skip: often pointer 0xC0 xx)
        if ((data[index] & 0xC0) == 0xC0) {
            index += 2; // pointer is 2 bytes
        } else {
            while (data[index] != 0) {
                index += (data[index] & 0xFF) + 1;
            }
            index++;
        }

        aType      = (short) ((data[index++] << 8) | (data[index++] & 0xFF));
        aClass     = (short) ((data[index++] << 8) | (data[index++] & 0xFF));
        time_to_live = ((data[index++] & 0xFF) << 24) |
                ((data[index++] & 0xFF) << 16) |
                ((data[index++] & 0xFF) << 8) |
                (data[index++] & 0xFF);

        data_length = (short) ((data[index++] << 8) | (data[index++] & 0xFF));

        // IPv4 address (4 bytes)
        if (aType == 1 && data_length == 4) {
            adress = ((data[index++] & 0xFF) << 24) |
                    ((data[index++] & 0xFF) << 16) |
                    ((data[index++] & 0xFF) << 8) |
                    (data[index++] & 0xFF);
        }
    }
}
