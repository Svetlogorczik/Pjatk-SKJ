import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Client {
    DatagramSocket datagramSocket =  null;
    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;

    String protocol;

    InetAddress address = null;
    int portNumber = 0;



    Client(String addressPortProtocol)
    {
        try {
            String[] arr =  addressPortProtocol.split(":");
            address = InetAddress.getByName(arr[0]);
            portNumber = Integer.parseInt(arr[1]);
            protocol = arr[2];
        }
        catch (UnknownHostException e) {
            System.err.println("Unknown host: " + address);
        }
        catch (Exception e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    String send(String command) {
        try {
            String response = "";
            if(protocol.equals("TCP")) {
                System.out.println("Creating a TCP client socket");
                socket = new Socket(address, portNumber);
                System.out.println("Socket created");
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("Sending " + command + " as a request");
                out.println(command);
                if(! command.equals("QUIT")) {

                    System.out.println("Waiting for a response");
                    response = in.readLine();
                }
                out.close();
                in.close();
                socket.close();
            }
            else if(protocol.equals("UDP")) {
                System.out.println("Creating a UDP client socket");
                datagramSocket = new DatagramSocket();
                System.out.println("Socket created");
                byte[] buffer = (command+" ").getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, portNumber);
                System.out.println("Sending " + command + " as a request");
                datagramSocket.send(packet);

                if(! command.equals("QUIT")) {
                    buffer = new byte[256];
                    packet = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.setSoTimeout(100);
                    System.out.println("Waiting for a response");
                    datagramSocket.receive(packet);

                    response = new String(packet.getData(), 0, packet.getLength());
                }
                datagramSocket.close();
            }
            System.out.println(response);
            return response;

        }
        catch (SocketTimeoutException e) {
            System.out.println("Timed out waiting for response");
        }
        catch (ConnectException e) {
            System.out.println("No connection");
        }
        catch (IOException e) {
            System.err.println("Error at work: " + e);
            System.exit(1);
        }
        return null;
    }
}
