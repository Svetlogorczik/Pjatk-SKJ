import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Sends ONE correct HTTP/1.1 GET request and prints the WHOLE response.
 *
 * Run:  java HttpProbe localhost 8080 /index.html
 *       java HttpProbe localhost 8080 /index.html "Mon, 06 Oct 2025 09:00:00 GMT"
 *       java HttpProbe gaia.cs.umass.edu 80 /wireshark-labs/INTRO-wireshark-file1.html
 */
public class HttpProbe {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java HttpProbe <host> <port> <path> [If-Modified-Since date]");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String path = args[2];

        // Every line ends with CRLF and the headers end with an EMPTY line.
        StringBuilder request = new StringBuilder();
        request.append("GET ").append(path).append(" HTTP/1.1\r\n");
        request.append("Host: ").append(host).append("\r\n");    // required in HTTP/1.1
        request.append("User-Agent: HttpProbe/1.0\r\n");
        request.append("Connection: close\r\n");                 // server closes -> we know when to stop
        if (args.length > 3) {
            request.append("If-Modified-Since: ").append(args[3]).append("\r\n");
        }
        request.append("\r\n");

        try (Socket socket = new Socket(host, port)) {
            OutputStream out = socket.getOutputStream();
            out.write(request.toString().getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
            System.out.print(request.toString().replaceAll("(?m)^", "> "));

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String statusLine = in.readLine();
            if (statusLine == null) {
                System.out.println("(server closed the connection without an answer)");
                return;
            }
            System.out.println("< " + statusLine);
            String[] status = statusLine.split(" ", 3);         // HTTP/1.1 200 OK

            String line;
            while ((line = in.readLine()) != null) {             // until the server closes
                System.out.println("< " + line);
            }
            System.out.println("\nStatus code: " + status[1] + (status.length > 2 ? " (" + status[2] + ")" : ""));
        }
    }
}
