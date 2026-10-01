import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Worker: gets tasks over TCP, sends results over UDP, reads the verdict over TCP.
 *
 * Run:  java Worker localhost 6100 6101
 */
public class Worker {

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int tcpPort = args.length > 1 ? Integer.parseInt(args[1]) : 6100;
        int udpPort = args.length > 2 ? Integer.parseInt(args[2]) : 6101;
        InetAddress coordinator = InetAddress.getByName(host);

        try (Socket tcp = new Socket(host, tcpPort);
             DatagramSocket udp = new DatagramSocket();
             BufferedReader in = new BufferedReader(new InputStreamReader(tcp.getInputStream(), StandardCharsets.UTF_8))) {
            log("connected over TCP, my UDP port is " + udp.getLocalPort());

            String line;
            while ((line = in.readLine()) != null) {
                log("TCP <- " + line);
                if (line.equals("BYE")) {
                    break;
                }
                if (line.startsWith("TASK ")) {
                    String[] f = line.split(" ");                     // TASK r a b
                    int round = Integer.parseInt(f[1]);
                    int sum = Integer.parseInt(f[2]) + Integer.parseInt(f[3]);
                    byte[] data = ("RESULT " + round + " " + sum).getBytes(StandardCharsets.UTF_8);
                    udp.send(new DatagramPacket(data, data.length, coordinator, udpPort));
                    log("UDP -> RESULT " + round + " " + sum);
                }
                // lines starting with ACK are only printed
            }
        }
        log("finished");
    }

    private static void log(String text) {
        System.out.println("[worker] " + text);
    }
}
