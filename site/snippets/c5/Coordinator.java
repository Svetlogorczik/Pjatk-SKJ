import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Two channels between two programs:
 *   TCP (reliable, ordered)  : Coordinator -> Worker  "TASK r a b",  later "ACK r OK|BAD", at the end "BYE"
 *   UDP (fast, no guarantee) : Worker -> Coordinator  "RESULT r sum"
 *
 * Run:  java Coordinator 6100 6101      (TCP port, UDP port)   then start Worker
 */
public class Coordinator {

    public static void main(String[] args) throws IOException {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 6100;
        int udpPort = args.length > 1 ? Integer.parseInt(args[1]) : 6101;
        int rounds = 5;
        Random random = new Random();

        try (ServerSocket server = new ServerSocket(tcpPort);
             DatagramSocket udp = new DatagramSocket(udpPort)) {
            udp.setSoTimeout(2000);
            log("TCP on " + tcpPort + ", UDP on " + udpPort + " - waiting for a worker");

            try (Socket worker = server.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(worker.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(worker.getOutputStream(), StandardCharsets.UTF_8), true)) {
                log("worker connected from " + worker.getRemoteSocketAddress());

                for (int r = 1; r <= rounds; r++) {
                    int a = random.nextInt(100);
                    int b = random.nextInt(100);
                    out.println("TASK " + r + " " + a + " " + b);                 // over TCP
                    log("TCP -> TASK " + r + " " + a + " " + b);

                    String verdict;
                    try {
                        DatagramPacket p = new DatagramPacket(new byte[512], 512);
                        udp.receive(p);                                            // over UDP
                        String msg = new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8).trim();
                        log("UDP <- " + msg + "  (from port " + p.getPort() + ")");
                        String[] f = msg.split(" ");                               // RESULT r sum
                        boolean ok = f.length == 3 && f[0].equals("RESULT")
                                && Integer.parseInt(f[1]) == r && Integer.parseInt(f[2]) == a + b;
                        verdict = ok ? "OK" : "BAD";
                    } catch (SocketTimeoutException e) {
                        verdict = "BAD";
                        log("no UDP answer - datagram lost?");
                    }
                    out.println("ACK " + r + " " + verdict);                      // over TCP again
                    log("TCP -> ACK " + r + " " + verdict);
                }
                out.println("BYE");
                in.readLine();                                                     // wait until the worker closes
            }
        }
        log("done");
    }

    private static void log(String text) {
        System.out.println("[coordinator] " + text);
    }
}
