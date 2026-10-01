import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proxy {
    public static void main(String args[]) {
        int proxyPort = 0;
        List<String> serverList = new ArrayList<>();
        HashMap<String,String> nameMap = new HashMap<>();
        for(int i=0; i<args.length;)
        {
            switch (args[i])
            {
                case "-server":
                    serverList.add(args[i + 1] + ":" + args[i + 2]);
                    i += 3;
                    break;
                case "-port":
                    proxyPort = Integer.parseInt(args[i + 1]);
                    i += 2;
                    break;
                default:
                    System.err.println("Unknown parameter: " + args[i]);
                    i++;
            }
        }

        for(String addressPort : serverList)
        {
            TCPClient proxyClient = new TCPClient(addressPort);
            String[] temp = proxyClient.send("GET NAMES").split(" ");
            for(int i=2; i<temp.length; i++)
                nameMap.put(temp[i],addressPort);
        }
        System.out.println(nameMap);

        TCPServer proxyServer = new TCPServer(proxyPort);
        proxyServer.start(nameMap,serverList);


    }
}
