import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Server
{

    ServerSocket serverSocket = null;
    DatagramSocket datagramSocket = null;
    Socket clientSocket = null;
    InetAddress clientAddress =  null;

    String protocol;

    int portNumber = 0;

    Server(int port,String protocol)
    {
        portNumber = port;
        this.protocol = protocol;
    }
    void start(Map<String,String> nameMap, List<String> serverList)
    {
            try {
                System.out.println("Creating the main " + protocol + " server socket at port " + portNumber);
                if(protocol.equals("TCP"))
                    serverSocket = new ServerSocket(portNumber);
                else if (protocol.equals("UDP"))
                    datagramSocket = new DatagramSocket(portNumber);
                System.out.println(protocol + " Socket created");
            }
            catch (IOException e) {
                System.err.println("Couldn't create a " + protocol + " server socket: " + e);
                System.exit(1);
            }
        while(true) try {
            System.out.println("Waiting for a client");
            PrintWriter out = null;

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            Scanner in  = null;

            String line = "";
            if(protocol.equals("TCP")) {
                clientSocket = serverSocket.accept();
                clientAddress = clientSocket.getInetAddress();
                System.out.println("A client connected from " + clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort());
                in = new Scanner(clientSocket.getInputStream());
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                line = in.nextLine();
            }
            else if (protocol.equals("UDP")){
                datagramSocket.receive(packet);
                clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                System.out.println("A request received from " + clientAddress.toString() + ":" + clientPort);

                in = new Scanner(new ByteArrayInputStream(packet.getData()));
                line = in.nextLine();
            }

            String input = "";
            String command = "";
            String parameter = "";
            String name = "";

            String output = "";

            String[] arr  = line.split(" ");
            command=arr[0];
            input=command;

            switch(command) {
                case "GET":
                    parameter=arr[1];
                    input += " " + parameter;
                    switch(parameter) {
                        case "NAMES":
                            output = "OK " + nameMap.size() + " " + String.join(" ", nameMap.keySet());
                            if(arr.length>3) {
                                Proxy.clientProxySet.add(String.valueOf(clientAddress).substring(1) + ":" + arr[3] + ":" + protocol);
                                input += " " + arr[2] + " " + arr[3];
                            }
                            break;
                        case "VALUE":
                            name=arr[2];
                            input += " " + name;
                            if(nameMap.containsKey(name)) {
                                Client proxyClient = new Client(nameMap.get(name));
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
                    name=arr[1];
                    String value=arr[2];
                    input+=" " + name + " " + value;
                    if(nameMap.containsKey(name)) {
                        Client proxyClient = new Client(nameMap.get(name));
                        output = proxyClient.send(input);
                    }
                    else
                        output = "NA";
                    break;
                case "UPDATE":
                    String clientPort = arr[2];
                    input += " " + clientPort;
                    Proxy.allServerList.add(String.valueOf(clientAddress).substring(1)+":"+clientPort);
                    Proxy.proxyUpdate();
                    output = "OK";
                    break;
                case "QUIT":
                    for(String addressPortProtocol : serverList)
                    {
                        Client proxyClient = new Client(addressPortProtocol);
                        proxyClient.send(command);
                    }
                    System.out.println("Terminating");
                    System.exit(0);
                default:
                    output = "NA";
            }
            System.out.println("Parsed command: " + input);
            System.out.println("Response: " + output);

            if(protocol.equals("TCP")) {
                out.println(output);
                out.close();
            }
            else if(protocol.equals("UDP")) {
                packet.setData(output.getBytes());
                datagramSocket.send(packet);
            }
            in.close();
        }
        catch (IOException e) {
            System.err.println("Error at work: " + e);
        }
    }
}
