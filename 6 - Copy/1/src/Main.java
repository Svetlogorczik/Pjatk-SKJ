
import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.net.*;


public class Main {

    public static void main(String[] args) throws IOException {

        byte[] doWyslania = {0x08, 0x54, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x77, 0x77, 0x77, 0x02, 0x77, 0x70, 0x02, 0x70, 0x6c, 0x00, 0x00, 0x01, 0x00, 0x01};
        String serverName = "8.8.8.8";
        int port = 53;
        log("Client starting");
        DatagramSocket socket = new DatagramSocket();
        log("Client socket: " + socket.getLocalPort());

        log("Server address resolving");
        InetAddress serverAddress = InetAddress.getByName(serverName);
        log("Server address resolved " + serverAddress.toString());

        log("Client datagram (packet) creating");
        DatagramPacket packet = new DatagramPacket(doWyslania, doWyslania.length, serverAddress, port);
        log("Client datagram (packet) created");
        log("Client datagram (packet) sending");
        socket.send(packet);
        log("Client datagram (packet) sent");

        log("Client datagram (packet) creating for data from server");
        DatagramPacket packetToReceive = new DatagramPacket(new byte[1500], 1500);
        log("Client datagram (packet) created for data from server");

        log("Client datagram (packet) receiving");
        socket.receive(packetToReceive);
        log("Client datagram (packet) sent");

        log("Response analysing");
        String source =  packetToReceive.getAddress().toString() + ":" + packetToReceive.getPort();
        log("Server name " + source);
        String data = new String(packetToReceive.getData(), packetToReceive.getLength());
        log("Data received " + data);

        DNS_Answer answer = new DNS_Answer(packetToReceive.getData());
        System.out.println(answer);


        //TUTAJ WPISZ KOD ANALIZUJĄCY ZAPYTANIE I ODPOWIEDŹ DNS
        log("Client closing");
        socket.close();
        log("Client closed");
    }

    public static void log(String message) {
        System.out.println("[C]: " + message);
        System.out.flush();
    }

}