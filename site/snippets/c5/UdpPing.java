import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Sends a few datagrams to UdpMirror and measures the round-trip time (RTT) of each one.
 * A lost datagram is detected with a timeout - UDP itself will never tell you.
 *
 * Run:  java UdpPing localhost 6000 "Hello UDP"
 */
public class UdpPing {

    public static void main(String[] args) throws IOException {
        InetAddress server = InetAddress.getByName(args.length > 0 ? args[0] : "localhost");
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 6000;
        String text = args.length > 2 ? args[2] : "Hello UDP";

        try (DatagramSocket socket = new DatagramSocket()) {          // no port given -> any free port
            socket.setSoTimeout(1000);                                 // wait max 1 s for an answer
            System.out.println("[ping] my local UDP port: " + socket.getLocalPort());

            for (int i = 1; i <= 4; i++) {
                byte[] data = (text + " #" + i).getBytes(StandardCharsets.UTF_8);
                long start = System.nanoTime();
                socket.send(new DatagramPacket(data, data.length, server, port));

                DatagramPacket reply = new DatagramPacket(new byte[1500], 1500);
                try {
                    socket.receive(reply);
                    double rttMs = (System.nanoTime() - start) / 1_000_000.0;
                    String answer = new String(reply.getData(), 0, reply.getLength(), StandardCharsets.UTF_8);
                    System.out.printf("[ping] reply %d: \"%s\"  RTT = %.3f ms%n", i, answer, rttMs);
                } catch (SocketTimeoutException e) {
                    System.out.println("[ping] datagram " + i + " lost (no answer within 1 s)");
                }
            }
        }
    }
}
