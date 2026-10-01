import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {
    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;

    InetAddress address = null;
    int portNumber = 0;

    TCPClient(String address,int port)
    {
        try {
            this.address = InetAddress.getByName(address);
        }
        catch (UnknownHostException e) {
            System.err.println("Unknown host: " + address);
        }
        portNumber = port;
    }
    TCPClient(String addressPort)
    {
        try {
            address = InetAddress.getByName(addressPort.split(":")[0]);
            portNumber = Integer.parseInt(addressPort.split(":")[1]);
        }
        catch (UnknownHostException e) {
            System.err.println("Unknown host: " + address);
        }
    }

    String send(String command) {
        try {
            System.out.println("Creating a client socket");
            socket = new Socket(address, portNumber);
            System.out.println("Socket created");
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Sending " + command + " as a request");
            out.println(command);

            String response = "";

            System.out.println("Waiting for a response");
            response = in.readLine();
            System.out.println(response);

            out.close();
            in.close();
            socket.close();
            return response;
        } catch (IOException e) {
            System.err.println("Error at work: " + e);
            System.exit(1);
        }
        return null;
    }
}
