import java.io.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.io.IOException;



public class A1 {

    public static void main(String[] args) throws IOException, InterruptedException {
        String address = "localhost";
        int port = 40000;

        log("Starting");
        Socket socketTCP = new Socket(address, port);
        log("Connected");

        log("Streams collecting");
        InputStream in = socketTCP.getInputStream();
        OutputStream out = socketTCP.getOutputStream();
        InputStreamReader isr = new InputStreamReader(in);
        OutputStreamWriter osw = new OutputStreamWriter(out);
        BufferedReader br = new BufferedReader(isr);
        BufferedWriter bw = new BufferedWriter(osw);

        log("Client starting");

        DatagramSocket socketUDP = new DatagramSocket(40000);
        log("Client socketUDP: " + socketUDP.getLocalPort());


        log("Client datagram (packet) creating for data from server");
        DatagramPacket packetToReceive = new DatagramPacket(new byte[1460], 1460);
        log("Client datagram (packet) created for data from server");

        for(int i = 1; i <= 20; i+=2) {
            log("Streams collected");
            String request = "Hello world " + i;

            log("Request sending: " + request);
            bw.write(request);
            bw.newLine();
            bw.flush();
            log("Request sent: " + request);

            String line1 = br.readLine();
            log("Response line1: " + line1);

            log("Client datagram (packet) receiving");
            socketUDP.receive(packetToReceive);
            log("Client datagram (packet) sent");

            log("Response analysing");
            String source = packetToReceive.getAddress().toString() + ":" + packetToReceive.getPort();
            log("Server name " + source);
            String data = new String(packetToReceive.getData(), 0, packetToReceive.getLength(), StandardCharsets.UTF_8);
            log("Data received " + data);

            data = data.toUpperCase();
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

            log("Client datagram (packet) creating");
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, packetToReceive.getAddress(), packetToReceive.getPort());
            log("Client datagram (packet) created");

            log("Client datagram (packet) sending");
            socketUDP.send(packet);
            log("Client datagram (packet) sent");

        }
            log("Closing");
            socketTCP.close();
            log("Closed");
            log("Ending");

            log("Client closing");
            socketUDP.close();
            log("Client closed");
    }


    public static void log(String message) {

        System.out.println("[C]: " + message);

        System.out.flush();

    }

}
