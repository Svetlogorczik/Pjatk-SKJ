import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Training server for the practical test (rewritten tasks, same skills).
 * Protocol: every message is ONE line ending with CRLF.
 *
 *   C: LOGIN <nick>          S: TOKEN <6 characters>
 *   C: START <token>         S: (data of task 1, round 1) ...
 *   After every answer the server sends a verdict line: OK  or  WRONG expected=<...>
 *   At the very end:         S: FINISHED <points>/<max>   (connection closed)
 *
 *   T1  1 line "a b c"                         -> 1 line: a+b+c
 *   T2  4 lines, one int each                  -> 1 line: the numbers in REVERSE order joined with ","
 *   T3  6 lines, one big number each           -> 3 lines: n1+n2, n3+n4, n5+n6   (BigInteger!)
 *   T4  line K, then K numbers a1..aK, then b1 -> answer max(a1,b1) at once, then b2 arrives, ...
 *   T5  like T4 but K is NOT sent              -> you have to detect the end of the first burst
 *
 * Every task is repeated R times (default 3).
 * Run:  java DrillServer 7777 3
 */
public class DrillServer {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 7777;
        int repeat = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[drill] listening on port " + port + ", every task x" + repeat);
            while (true) {
                Socket client = server.accept();
                new Thread(new Session(client, repeat)).start();
            }
        }
    }

    static final class Session implements Runnable {
        private final Socket socket;
        private final int repeat;
        private final Random rnd = new Random();
        private BufferedReader in;
        private PrintWriter out;
        private int points;
        private int max;
        private String who;

        Session(Socket socket, int repeat) {
            this.socket = socket;
            this.repeat = repeat;
        }

        @Override
        public void run() {
            who = socket.getRemoteSocketAddress().toString();
            try {
                socket.setSoTimeout(20000);   // a client that is silent for 20 s gets disconnected
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                if (!login()) {
                    return;
                }
                for (int r = 0; r < repeat; r++) task1();
                for (int r = 0; r < repeat; r++) task2();
                for (int r = 0; r < repeat; r++) task3();
                for (int r = 0; r < repeat; r++) task4(true);
                for (int r = 0; r < repeat; r++) task4(false);
                send("FINISHED " + points + "/" + max);
                log("finished with " + points + "/" + max);
            } catch (SocketTimeoutException e) {
                send("TIMEOUT");
                log("timeout - client was too slow or waited for something that will never come");
            } catch (IOException | RuntimeException e) {
                log("connection ended: " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // nothing more to do
                }
            }
        }

        private boolean login() throws IOException {
            String first = read();
            if (!first.matches("LOGIN \\S+")) {
                send("ERR expected: LOGIN <nick>");
                return false;
            }
            who = first.substring(6) + " " + who;
            String token = randomToken();
            send("TOKEN " + token);
            if (!read().equals("START " + token)) {
                send("ERR bad token");
                return false;
            }
            return true;
        }

        private void task1() throws IOException {
            long a = rnd.nextInt(2001) - 1000, b = rnd.nextInt(2001) - 1000, c = rnd.nextInt(2001) - 1000;
            send(a + " " + b + " " + c);
            verdict(1, String.valueOf(a + b + c), read());
        }

        private void task2() throws IOException {
            int[] n = new int[4];
            for (int i = 0; i < 4; i++) {
                n[i] = rnd.nextInt(100000);
                send(String.valueOf(n[i]));
            }
            verdict(2, n[3] + "," + n[2] + "," + n[1] + "," + n[0], read());
        }

        private void task3() throws IOException {
            BigInteger[] n = new BigInteger[6];
            for (int i = 0; i < 6; i++) {
                n[i] = bigNumber();
                send(n[i].toString());
            }
            StringBuilder expected = new StringBuilder();
            StringBuilder got = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                expected.append(i > 0 ? " | " : "").append(n[2 * i].add(n[2 * i + 1]));
                got.append(i > 0 ? " | " : "").append(read().trim());
            }
            verdict(3, expected.toString(), got.toString());
        }

        /** T4 (withK = true) and T5 (withK = false). */
        private void task4(boolean withK) throws IOException {
            int k = 2 + rnd.nextInt(4);
            List<BigInteger> a = new ArrayList<>();
            if (withK) {
                send(String.valueOf(k));
            }
            for (int i = 0; i < k; i++) {
                a.add(bigNumber());
                send(a.get(i).toString());
            }
            StringBuilder expected = new StringBuilder();
            StringBuilder got = new StringBuilder();
            for (int i = 0; i < k; i++) {
                BigInteger b = bigNumber();
                send(b.toString());                     // next number only after the previous answer
                expected.append(i > 0 ? " | " : "").append(a.get(i).max(b));
                got.append(i > 0 ? " | " : "").append(read().trim());
            }
            verdict(withK ? 4 : 5, expected.toString(), got.toString());
        }

        private void verdict(int task, String expected, String got) {
            max++;
            if (expected.equals(got)) {
                points++;
                send("OK");
            } else {
                send("WRONG expected=" + expected);
            }
            log("T" + task + (expected.equals(got) ? " OK" : " WRONG  expected=" + expected + "  got=" + got));
        }

        private BigInteger bigNumber() {
            StringBuilder sb = new StringBuilder().append(1 + rnd.nextInt(9));
            int digits = 14 + rnd.nextInt(16);
            for (int i = 0; i < digits; i++) {
                sb.append(rnd.nextInt(10));
            }
            return new BigInteger(sb.toString());
        }

        private String randomToken() {
            String abc = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(abc.charAt(rnd.nextInt(abc.length())));
            }
            return sb.toString();
        }

        private String read() throws IOException {
            String line = in.readLine();
            if (line == null) {
                throw new IOException("client closed the connection");
            }
            return line;
        }

        private void send(String line) {
            if (out != null) {
                out.print(line + "\r\n");
                out.flush();
            }
        }

        private void log(String text) {
            System.out.println("[drill] " + who + ": " + text);
        }
    }
}
