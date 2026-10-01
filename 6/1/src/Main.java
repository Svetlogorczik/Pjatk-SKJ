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

        //TUTAJ WPISZ KOD ANALIZUJĄCY ZAPYTANIE I ODPOWIEDŹ DNS
        int i = 0;
        System.out.printf("ID: 0x%02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Flags: 0x%02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Questions: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Answer RRs: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Authority RRs: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Additional RRs: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.print("Name: ");
        byte letter = packetToReceive.getData()[i++];
        while (letter != 0) {
            if(letter == 3);
            else if(letter == 2) System.out.print(".");
            else
                System.out.print((char) letter);
            letter = packetToReceive.getData()[i++];
        }
        System.out.println();
        System.out.printf("Type: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Class: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.println("Answers");
        System.out.printf("Name: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Type: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Class: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Time to live: %02X%02X%02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++], packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Data length: %02X%02X \n", packetToReceive.getData()[i++], packetToReceive.getData()[i++]);
        System.out.printf("Adress: %d.%d.%d.%d \n", packetToReceive.getData()[i++]&0xFF, packetToReceive.getData()[i++]&0xFF, packetToReceive.getData()[i++]&0xFF, packetToReceive.getData()[i++]&0xFF);

        log("Client closing");
        socket.close();
        log("Client closed");
    }

    public static void log(String message) {
        System.out.println("[C]: " + message);
        System.out.flush();
    }

}