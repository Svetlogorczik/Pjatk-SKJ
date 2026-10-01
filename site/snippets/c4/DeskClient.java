import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for QuoteDesk: reads the greeting, logs in, downloads quotes between BEGIN/END,
 * saves them to quotes.txt, then uses DUMP (read until the server closes).
 *
 * Run:  java DeskClient localhost 5050 alice
 */
public class DeskClient {

    private final BufferedReader in;
    private final PrintWriter out;

    DeskClient(Socket socket) throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5050;
        String nick = args.length > 2 ? args[2] : "student";

        // Part 1: conversation with a clear end marker (END)
        try (Socket socket = new Socket(host, port)) {
            DeskClient c = new DeskClient(socket);
            c.expect("HELLO");                       // 1. the server talks first - read it!
            c.send("NAME " + nick);
            c.expect("OK");
            c.send("QUOTES 3");
            c.expect("BEGIN");

            List<String> quotes = new ArrayList<>();
            String line;
            while (!(line = c.read()).equals("END")) {  // 2. read until the marker line
                quotes.add(line);
            }
            save("quotes.txt", quotes);              // 3. only the lines BETWEEN the markers
            c.send("BYE");
            c.read();
        }

        // Part 2: the server sends everything and closes -> read until readLine() returns null
        try (Socket socket = new Socket(host, port)) {
            DeskClient c = new DeskClient(socket);
            c.expect("HELLO");
            c.send("NAME " + nick);
            c.expect("OK");
            c.send("DUMP");
            List<String> all = new ArrayList<>();
            String line;
            while ((line = c.in.readLine()) != null) {
                log("<- " + line);
                all.add(line);
            }
            // first line = "ALL n", last line = "CLOSING" -> skip both
            save("all-quotes.txt", all.subList(1, all.size() - 1));
        }
    }

    private void send(String line) {
        out.print(line + "\r\n");
        out.flush();                                 // without flush the server may wait forever
        log("-> " + line);
    }

    private String read() throws IOException {
        String line = in.readLine();
        if (line == null) {
            throw new IOException("server closed the connection");
        }
        log("<- " + line);
        return line;
    }

    private String expect(String prefix) throws IOException {
        String line = read();
        if (!line.startsWith(prefix)) {
            throw new IOException("expected " + prefix + " but got: " + line);
        }
        return line;
    }

    private static void save(String file, List<String> lines) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) {
                w.write(l);
                w.newLine();
            }
        }
        log("saved " + lines.size() + " lines to " + file);
    }

    private static void log(String text) {
        System.out.println("[client] " + text);
    }
}
