import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * SKELETON for DrillServer - fill in the TODO parts yourself.
 * Run:  java DrillClient localhost 7777 3
 */
public class DrillClient {

    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 7777;
        int repeat = args.length > 2 ? Integer.parseInt(args[2]) : 3;

        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        send("LOGIN student");
        String token = read().split(" ")[1];     // "TOKEN Ab3dE9" -> "Ab3dE9"
        send("START " + token);

        for (int r = 0; r < repeat; r++) {
            // T1: one line "a b c" -> send a+b+c
            String line = read();
            // TODO split the line, add the numbers (use long), send the result
            read(); // verdict
        }

        for (int r = 0; r < repeat; r++) {
            // T2: read 4 lines -> send them in reverse order joined with ","
            // TODO
            read(); // verdict
        }

        for (int r = 0; r < repeat; r++) {
            // T3: read 6 lines -> send 3 lines with sums of pairs (BigInteger)
            // TODO
            read(); // verdict
        }

        for (int r = 0; r < repeat; r++) {
            // T4: first K, then K numbers, then for every incoming b_i send max(a_i, b_i) at once
            // TODO
            read(); // verdict
        }

        for (int r = 0; r < repeat; r++) {
            // T5: like T4 but K is unknown.
            // Hint: socket.setSoTimeout(...) + catch (SocketTimeoutException e) ends the first burst.
            // TODO
            read(); // verdict
        }

        System.out.println(read());              // FINISHED x/y
        socket.close();
    }

    private static void send(String line) {
        out.print(line + "\r\n");
        out.flush();
        System.out.println("-> " + line);
    }

    private static String read() throws IOException {
        String line = in.readLine();
        if (line == null) {
            throw new IOException("server closed the connection");
        }
        System.out.println("<- " + line);
        return line;
    }
}
