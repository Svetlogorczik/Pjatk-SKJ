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
 * Example solution for DrillServer. Try the skeleton (DrillClient) first!
 * Run:  java DrillSolution localhost 7777 3
 */
public class DrillSolution {

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
        String token = read().split(" ")[1];
        send("START " + token);

        // T1: "a b c" -> a+b+c
        for (int r = 0; r < repeat; r++) {
            long sum = 0;
            for (String part : read().trim().split("\\s+")) {
                sum += Long.parseLong(part);
            }
            send(String.valueOf(sum));
            read();
        }

        // T2: 4 lines -> reversed, joined with ","
        for (int r = 0; r < repeat; r++) {
            List<String> numbers = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                numbers.add(read().trim());
            }
            StringBuilder answer = new StringBuilder();
            for (int i = numbers.size() - 1; i >= 0; i--) {
                answer.append(numbers.get(i)).append(i > 0 ? "," : "");
            }
            send(answer.toString());
            read();
        }

        // T3: 6 big numbers -> 3 sums of pairs
        for (int r = 0; r < repeat; r++) {
            BigInteger[] n = new BigInteger[6];
            for (int i = 0; i < 6; i++) {
                n[i] = new BigInteger(read().trim());
            }
            for (int i = 0; i < 3; i++) {
                send(n[2 * i].add(n[2 * i + 1]).toString());
            }
            read();
        }

        // T4: K is known
        for (int r = 0; r < repeat; r++) {
            int k = Integer.parseInt(read().trim());
            List<BigInteger> a = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                a.add(new BigInteger(read().trim()));
            }
            for (int i = 0; i < k; i++) {
                BigInteger b = new BigInteger(read().trim());
                send(a.get(i).max(b).toString());      // answer AT ONCE, otherwise no next number
            }
            read();
        }

        // T5: K is unknown -> the first burst ends when the server goes quiet
        for (int r = 0; r < repeat; r++) {
            List<BigInteger> burst = new ArrayList<>();
            socket.setSoTimeout(500);
            try {
                while (true) {
                    burst.add(new BigInteger(read().trim()));
                }
            } catch (SocketTimeoutException quiet) {
                // the server sent a1..aK and b1 and now waits for our first answer
            } finally {
                socket.setSoTimeout(0);                // back to "wait forever"
            }
            int k = burst.size() - 1;
            List<BigInteger> a = burst.subList(0, k);
            send(a.get(0).max(burst.get(k)).toString());
            for (int i = 1; i < k; i++) {
                BigInteger b = new BigInteger(read().trim());
                send(a.get(i).max(b).toString());
            }
            read();
        }

        System.out.println(read());                    // FINISHED x/y
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
