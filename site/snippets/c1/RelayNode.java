import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * One node of a ring (A -> B -> C -> A).
 * The starter node creates a TOKEN, every node adds its name and passes it on.
 * Message format (one text line):  TOKEN <round> <hops> <path>   or   STOP
 *
 * Run:  java RelayNode relay-a.properties
 */
public class RelayNode {

    private final String name;
    private final int listenPort;
    private final String nextHost;
    private final int nextPort;
    private final boolean starter;
    private final int rounds;
    private final long delayMs;

    RelayNode(Properties p) {
        name = p.getProperty("name", "?").trim();
        listenPort = Integer.parseInt(p.getProperty("listenPort").trim());
        nextHost = p.getProperty("nextHost", "localhost").trim();
        nextPort = Integer.parseInt(p.getProperty("nextPort").trim());
        starter = Boolean.parseBoolean(p.getProperty("starter", "false").trim());
        rounds = Integer.parseInt(p.getProperty("rounds", "3").trim());
        delayMs = Long.parseLong(p.getProperty("delayMs", "300").trim());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage: java RelayNode <file.properties>");
            System.exit(1);
        }
        Properties config = new Properties();
        try (InputStream in = new FileInputStream(args[0])) {
            config.load(in);
        }
        new RelayNode(config).run();
    }

    void run() throws IOException, InterruptedException {
        // 1) Open our own "door" first, so the previous node can reach us.
        try (ServerSocket server = new ServerSocket(listenPort)) {
            log("listening on port " + listenPort + ", next node = " + nextHost + ":" + nextPort);

            // 2) Only the starter puts the first token into the ring.
            if (starter) {
                send("TOKEN 1 0 " + name);
            }

            // 3) Main loop: receive one line, react, pass it on.
            while (true) {
                String message = receive(server);
                if (message == null) {
                    continue;
                }
                if (message.equals("STOP")) {
                    log("STOP received - bye");
                    if (!starter) {
                        send("STOP");          // let the rest of the ring finish too
                    }
                    break;
                }

                String[] part = message.split(" "); // TOKEN round hops path
                int round = Integer.parseInt(part[1]);
                int hops = Integer.parseInt(part[2]) + 1;
                String path = part[3] + ">" + name;

                if (starter) {
                    log("round " + round + " finished: " + hops + " hops, path " + path);
                    if (round < rounds) {
                        send("TOKEN " + (round + 1) + " 0 " + name);
                    } else {
                        send("STOP");          // goes around the ring and comes back to us
                    }
                } else {
                    Thread.sleep(delayMs);     // so that you can watch it happen
                    send("TOKEN " + round + " " + hops + " " + path);
                }
            }
        }
    }

    /** Waits for ONE connection and reads ONE line from it. */
    private String receive(ServerSocket server) throws IOException {
        try (Socket socket = server.accept();
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line = in.readLine();
            log("<- " + line + "   (from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ")");
            return line;
        }
    }

    /** Opens a connection to the next node, sends one line and closes. Retries while the node is not up yet. */
    private void send(String line) throws IOException, InterruptedException {
        for (int attempt = 1; ; attempt++) {
            try (Socket socket = new Socket(nextHost, nextPort);
                 PrintWriter out = new PrintWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
                out.println(line);
                log("-> " + line);
                return;
            } catch (ConnectException e) {
                if (attempt == 20) {
                    throw e;
                }
                log("next node is not ready yet, retry " + attempt + "/20");
                Thread.sleep(500);
            }
        }
    }

    private void log(String text) {
        System.out.println("[" + name + "] " + text);
    }
}
