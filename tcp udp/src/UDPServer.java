import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class UDPServer {
    public static void main(String[] args) throws IOException {
        log("Client starting");

        DatagramSocket socket = new DatagramSocket(40000);
        log("Client socket: " + socket.getLocalPort());

        log("Client datagram (packet) creating for data from server");
        DatagramPacket packetToReceive = new DatagramPacket(new byte[1460], 1460);
        log("Client datagram (packet) created for data from server");

        log("Client datagram (packet) receiving");
        socket.receive(packetToReceive);
        log("Client datagram (packet) sent");

        log("Response analysing");
        String source =  packetToReceive.getAddress().toString() + ":" + packetToReceive.getPort();
        log("Server name " + source);
        String data = new String(packetToReceive.getData(), 0, packetToReceive.getLength(), StandardCharsets.UTF_8);
        log("Data received " + data);

        data = data.toUpperCase();
        byte[]bytes = data.getBytes(StandardCharsets.UTF_8);

        log("Client datagram (packet) creating");
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, packetToReceive.getAddress(), packetToReceive.getPort());
        log("Client datagram (packet) created");

        log("Client datagram (packet) sending");
        socket.send(packet);
        log("Client datagram (packet) sent");


        log("Client closing");
        socket.close();
        log("Client closed");
    }

    public static void log(String message) {
        System.out.println("[C]: " + message);
        System.out.flush();
    }
}