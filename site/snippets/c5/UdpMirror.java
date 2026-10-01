import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

/**
 * UDP "mirror": every datagram is sent back REVERSED to the address and port it came from.
 * The text STOP ends the server.
 *
 * Run:  java UdpMirror 6000
 */
public class UdpMirror {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 6000;
        try (DatagramSocket socket = new DatagramSocket(port)) {       // bind to a fixed port
            System.out.println("[mirror] waiting on UDP port " + port);
            byte[] buffer = new byte[1500];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);                                // blocks until a datagram arrives

                // Only the first getLength() bytes are this message - the rest is old garbage!
                String text = new String(request.getData(), 0, request.getLength(), StandardCharsets.UTF_8);
                System.out.println("[mirror] from " + request.getAddress().getHostAddress() + ":" + request.getPort() + " -> " + text);
                if (text.trim().equals("STOP")) {
                    break;
                }

                byte[] answer = new StringBuilder(text).reverse().toString().getBytes(StandardCharsets.UTF_8);
                // Reply to the SENDER: its address and port are inside the received packet.
                socket.send(new DatagramPacket(answer, answer.length, request.getAddress(), request.getPort()));
            }
        }
        System.out.println("[mirror] stopped");
    }
}
