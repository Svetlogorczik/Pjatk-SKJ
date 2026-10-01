import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Proxy {
    static int proxyPort = 0;
    static List<String> allServerList = new CopyOnWriteArrayList<>();
    static List<String> serverList = new CopyOnWriteArrayList<>();
    static Map<String,Integer> numMap = new ConcurrentHashMap<>();
    static Map<String,String> nameMap = new ConcurrentHashMap<>();
    static Set<String> clientProxySet = ConcurrentHashMap.newKeySet();

    public static void main(String args[]) {
        for(int i=0; i<args.length;)
        {
            switch (args[i])
            {
                case "-server":
                    allServerList.add(args[i + 1] + ":" + args[i + 2]);
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
        proxyUpdate();
        System.out.println(nameMap);

        Server proxyServerTCP = new Server(proxyPort,"TCP");
        Server proxyServerUDP = new Server(proxyPort,"UDP");

        new Thread(()->{proxyServerTCP.start(nameMap,serverList);}).start();
        new Thread(()->{proxyServerUDP.start(nameMap,serverList);}).start();

    }

    static void proxyUpdate(){

        new Thread(()->{
            boolean newName = false;
            while(!allServerList.isEmpty()) {
                for (String protocol : new String[]{"TCP", "UDP"}) {
                    for (String addressPort : allServerList) {
                        String addressPortProtocol = addressPort + ":" + protocol;
                        Client proxyClient = new Client(addressPortProtocol);
                        String response = proxyClient.send("GET NAMES P "+proxyPort);
                        if (response == null)
                            continue;
                        serverList.add(addressPortProtocol);
                        String[] temp = response.split(" ");
                        int num = Integer.parseInt(temp[1]);
                        for (int i = 2; i < temp.length; i++) {
                            if (!numMap.containsKey(temp[i]) || numMap.get(temp[i]) > num) {
                                numMap.put(temp[i], num);
                                nameMap.put(temp[i], addressPortProtocol);
                                newName = true;
                            }
                        }
                        allServerList.remove(addressPort);
                    }
                }
                if(newName) {
                    for (String addressPortProtocol : clientProxySet) {
                        Client proxyClient = new Client(addressPortProtocol);
                        proxyClient.send("UPDATE P " + proxyPort);
                    }
                    System.out.println("Sent UPDATE to all client Proxies");
                    newName = false;
                }
                try {
                    //System.out.println("Waiting 1 second...");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
}
