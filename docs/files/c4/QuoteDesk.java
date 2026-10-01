import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Practice server with a line-based text protocol (one thread per client).
 *
 *   S: HELLO QuoteDesk 1.0             <- the SERVER speaks first
 *   C: NAME <nick>
 *   S: OK <nick>
 *   C: QUOTES <n>                      (n = 1..9)
 *   S: BEGIN <n>
 *   S: ...n lines...
 *   S: END
 *   C: DUMP                            <- server sends everything and CLOSES the connection
 *   C: BYE
 *   S: BYE <nick>                      (connection closed)
 *   anything else -> ERR <reason>
 *
 * Run:  java QuoteDesk 5050
 */
public class QuoteDesk {

    private static final String[] QUOTES = {
        "A packet a day keeps the timeout away.",
        "There is no place like 127.0.0.1.",
        "TCP: I'll tell you again until you get it.",
        "UDP: I told you once. Did you get it? Not my problem.",
        "DNS: it's always DNS.",
        "Port 80 is open, the door is ajar.",
        "flush() or it did not happen.",
        "readLine() waits for the end of the line - patiently.",
        "Every protocol is just a polite conversation.",
    };

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5050;
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[desk] listening on port " + port);
            while (true) {
                Socket client = server.accept();
                new Thread(() -> serve(client)).start();      // many clients at the same time
            }
        }
    }

    private static void serve(Socket socket) {
        String who = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            out.print("HELLO QuoteDesk 1.0\r\n");
            out.flush();
            String nick = null;
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("[desk] " + who + " says: " + line);
                String[] cmd = line.trim().split("\\s+");
                if (cmd[0].equals("NAME") && cmd.length == 2) {
                    nick = cmd[1];
                    reply(out, "OK " + nick);
                } else if (nick == null) {
                    reply(out, "ERR say NAME <nick> first");
                } else if (cmd[0].equals("QUOTES") && cmd.length == 2 && cmd[1].matches("[1-9]")) {
                    int n = Integer.parseInt(cmd[1]);
                    reply(out, "BEGIN " + n);
                    for (int i = 0; i < n; i++) {
                        reply(out, QUOTES[(i + nick.length()) % QUOTES.length]);
                    }
                    reply(out, "END");
                } else if (cmd[0].equals("DUMP")) {
                    reply(out, "ALL " + QUOTES.length);
                    for (String q : QUOTES) {
                        reply(out, q);
                    }
                    reply(out, "CLOSING");
                    return;                                    // try-with-resources closes the socket
                } else if (cmd[0].equals("BYE")) {
                    reply(out, "BYE " + nick);
                    return;
                } else {
                    reply(out, "ERR unknown command: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("[desk] " + who + " error: " + e.getMessage());
        } finally {
            System.out.println("[desk] " + who + " disconnected");
        }
    }

    private static void reply(PrintWriter out, String text) {
        out.print(text + "\r\n");                              // protocol line ending: CRLF
        out.flush();
    }
}
