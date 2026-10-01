import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UDPClient {
    public static void main(String[] args) throws IOException {
        log("Client starting");

        DatagramSocket socket = new DatagramSocket();
        log("Client socket: " + socket.getLocalPort());

        String nazwaA1 = "localhost";
        int port = 40000;
        log("Server address resolving");
        InetAddress serverAddress = InetAddress.getByName(nazwaA1);
        log("Server address resolved " + serverAddress.toString());

        String textToSend = "Hello World!";
        byte[] bufToSend = textToSend.getBytes(StandardCharsets.UTF_8);
        log("Client datagram (packet) creating");
        DatagramPacket packet = new DatagramPacket(bufToSend, bufToSend.length, serverAddress, port);
        log("Client datagram (packet) created");

        log("Client datagram (packet) sending");
        socket.send(packet);
        log("Client datagram (packet) sent");

        log("Client datagram (packet) creating for data from server");
        DatagramPacket packetToReceive = new DatagramPacket(new byte[1460], 1460);
        log("Client datagram (packet) created for data from server");

        log("Client datagram (packet) receiving");
        socket.receive(packetToReceive);
        log("Client datagram (packet) sent");

        log("Response analysing");
        String source = packetToReceive.getAddress().toString() + ":" + packetToReceive.getPort();
        log("Server name " + source);
        String data = new String(packetToReceive.getData(), 0, packetToReceive.getLength(), StandardCharsets.UTF_8);
        log("Data received " + data);

        log("Client closing");
        socket.close();
        log("Client closed");
    }

    public static void log(String message) {
        System.out.println("[C]: " + message);
        System.out.flush();
    }
}