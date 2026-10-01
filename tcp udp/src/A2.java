import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class A2 {
    public static void main(String[] args) throws IOException, InterruptedException {

        int port = 40000;
        log("Starting");
        ServerSocket ss = new ServerSocket(port);
        log("Server socked allowed");

        log("Client starting");
        DatagramSocket socketUDP = new DatagramSocket();
        log("Client socketUDP: " + socketUDP.getLocalPort());

        String nazwaA1 = "localhost";

        log("Server address resolving");
        InetAddress serverAddress = InetAddress.getByName(nazwaA1);
        log("Server address resolved " + serverAddress.toString());

        log("Listening");
        Socket socketTCP = ss.accept();
        for(int i = 2; i <= 22; i+=2) {

            log("Client connected");

            log("Streams collecting");
            InputStream in = socketTCP.getInputStream();
            OutputStream out = socketTCP.getOutputStream();
            InputStreamReader isr = new InputStreamReader(in);
            OutputStreamWriter osw = new OutputStreamWriter(out);
            BufferedReader br = new BufferedReader(isr);
            BufferedWriter bw = new BufferedWriter(osw);
            log("Streams collected");

            log("Request reading");
            String line = br.readLine();
            log("Request: " + line);

            String response = line.toUpperCase();
            log("Response sending: " + response);
            bw.write(response);
            bw.newLine();
            bw.flush();
            log("Response sent: " + response);


            String textToSend = "Hello World " + i;
            byte[] bufToSend = textToSend.getBytes(StandardCharsets.UTF_8);
            log("Client datagram (packet) creating");
            DatagramPacket packet = new DatagramPacket(bufToSend, bufToSend.length, serverAddress, port);
            log("Client datagram (packet) created");

            log("Client datagram (packet) sending");
            socketUDP.send(packet);
            log("Client datagram (packet) sent");

            log("Client datagram (packet) creating for data from server");
            DatagramPacket packetToReceive = new DatagramPacket(new byte[1460], 1460);
            log("Client datagram (packet) created for data from server");

            log("Client datagram (packet) receiving");
            socketUDP.receive(packetToReceive);
            log("Client datagram (packet) sent");

            log("Response analysing");
            String source = packetToReceive.getAddress().toString() + ":" + packetToReceive.getPort();
            log("Server name " + source);
            String data = new String(packetToReceive.getData(), 0, packetToReceive.getLength(), StandardCharsets.UTF_8);
            log("Data received " + data);

        }
        log("Client socketTCP closing");
        socketTCP.close();

        log("Client socketTCP closed");
        log("Server socketTCP closing");
        ss.close();
        log("Server socketTCP closed");
        log("Ending");

        log("Client closing");
        socketUDP.close();
        log("Client closed");
    }

    public static void log(String message) {
        System.out.println("[S]: " + message);
        System.out.flush();
    }
}