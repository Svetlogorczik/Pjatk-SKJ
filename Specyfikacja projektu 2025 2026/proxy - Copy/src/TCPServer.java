import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class TCPServer
{
    static int idCounter = 0;

    ServerSocket serverSocket = null;
    Socket clientSocket = null;

    int portNumber = 0;

    TCPServer(int port)
    {
        portNumber = port;
    }
    void start(HashMap<String,String> nameMap, List<String> serverList)
    {
        try {
            System.out.println("Creating the main server socket at port " + portNumber);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Socket created");
        }
        catch (IOException e) {
            System.err.println("Counldn't create a server socket: " + e);
            System.exit(1);
        }
        while(true) try {
            System.out.println("Waiting for a client");
            clientSocket = serverSocket.accept();
            System.out.println("A client connected from " + clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort());

            Scanner in = new Scanner(clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String command = "";
            String parameter = "";
            String name = "";

            String input = "";
            String output = "";

            command=in.next();
            input = command;

            switch(command) {
                case "GET":
                    parameter=in.next();
                    input += " " + parameter;
                    switch(parameter) {
                        case "NAMES":
                            output = "OK " + nameMap.size() + " " + String.join(" ", nameMap.keySet());
                            break;
                        case "VALUE":
                            name=in.next();
                            input += " " + name;
                            if(nameMap.containsKey(name)) {
                                TCPClient proxyClient = new TCPClient(nameMap.get(name));
                                output = proxyClient.send(input);
                            }
                            else
                                output = "NA";
                            break;
                        default:
                            output = "NA";
                    }
                    break;
                case "SET":
                    name=in.next();
                    String value = in.next();
                    input += " " + name +  " " + value;
                    if(nameMap.containsKey(name)) {
                        TCPClient proxyClient = new TCPClient(nameMap.get(name));
                        output = proxyClient.send(input);
                    }
                    else
                        output = "NA";
                    break;
                case "QUIT":
                    for(String addressPort : serverList)
                    {
                        TCPClient proxyClient = new TCPClient(addressPort);
                        proxyClient.send(command);
                    }
                    System.out.println("Terminating");
                    System.exit(0);
                default:
                    output = "NA";
            }
            System.out.println("Parsed command: " + input);
            System.out.println("Response: " + output);

            out.println(output);

            in.close();
            out.close();
        }
        catch (IOException e) {
            System.err.println("Error at work: " + e);
        }
    }
}
