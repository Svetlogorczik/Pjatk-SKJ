import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A tiny HTTP/1.1 server (one client at a time) that knows three "pages".
 *   200 OK            - page exists and is newer than If-Modified-Since (or no such header)
 *   304 Not Modified  - page exists and did not change since If-Modified-Since
 *   404 Not Found     - unknown path
 *   405 / 400         - wrong method / broken request line
 *
 * Run:  java MiniWebServer 8080      then open http://localhost:8080/index.html
 */
public class MiniWebServer {

    /** HTTP dates look like: Tue, 14 Oct 2025 08:15:00 GMT (always English, always GMT). */
    private static final DateTimeFormatter HTTP_DATE =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    private static final class Page {
        final String html;
        final ZonedDateTime lastModified;

        Page(String html, String lastModified) {
            this.html = html;
            this.lastModified = ZonedDateTime.parse(lastModified, HTTP_DATE);
        }
    }

    private final Map<String, Page> pages = new HashMap<>();

    MiniWebServer() {
        pages.put("/index.html", new Page("<h1>Start</h1><p>Hello from MiniWebServer!</p>",
                "Mon, 06 Oct 2025 09:00:00 GMT"));
        pages.put("/schedule.html", new Page("<h1>Schedule</h1><p>Labs every Tuesday.</p>",
                "Wed, 15 Oct 2025 12:30:00 GMT"));
        pages.put("/contact.html", new Page("<h1>Contact</h1><p>Room 404 (just kidding).</p>",
                "Fri, 21 Nov 2025 18:45:00 GMT"));
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new MiniWebServer().serve(port);
    }

    void serve(int port) throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            log("listening on http://localhost:" + port + "/index.html");
            while (true) {
                try (Socket client = server.accept()) {
                    handle(client);
                } catch (IOException e) {
                    log("client error: " + e.getMessage());
                }
            }
        }
    }

    private void handle(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.ISO_8859_1));
        OutputStream out = client.getOutputStream();

        // 1) Request line: METHOD SP PATH SP VERSION
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return;                                   // client connected and said nothing
        }
        log("request: " + requestLine);

        // 2) Header lines until the EMPTY line
        Map<String, String> headers = new LinkedHashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                headers.put(line.substring(0, colon).trim().toLowerCase(Locale.ROOT), line.substring(colon + 1).trim());
            }
        }
        log("headers: " + headers);

        String[] parts = requestLine.split(" ");
        if (parts.length != 3 || !parts[2].startsWith("HTTP/")) {
            send(out, 400, "Bad Request", null, "<h1>400 Bad Request</h1>");
            return;
        }
        if (!parts[0].equals("GET")) {
            send(out, 405, "Method Not Allowed", null, "<h1>405 Only GET is supported</h1>");
            return;
        }

        String path = parts[1].equals("/") ? "/index.html" : parts[1];
        Page page = pages.get(path);
        if (page == null) {
            send(out, 404, "Not Found", null, "<h1>404 Not Found</h1><p>" + path + "</p>");
            return;
        }

        // 3) Conditional GET
        String since = headers.get("if-modified-since");
        if (since != null) {
            try {
                ZonedDateTime clientCopy = ZonedDateTime.parse(since, HTTP_DATE);
                if (!page.lastModified.isAfter(clientCopy)) {
                    send(out, 304, "Not Modified", page, null);   // no body!
                    return;
                }
            } catch (DateTimeParseException e) {
                log("bad If-Modified-Since date, ignoring it: " + since);
            }
        }
        send(out, 200, "OK", page, page.html);
    }

    private void send(OutputStream out, int code, String reason, Page page, String body) throws IOException {
        byte[] bodyBytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        StringBuilder head = new StringBuilder();
        head.append("HTTP/1.1 ").append(code).append(' ').append(reason).append("\r\n");
        head.append("Date: ").append(HTTP_DATE.format(ZonedDateTime.now(ZoneOffset.UTC))).append("\r\n");
        head.append("Server: MiniWebServer/1.0\r\n");
        if (page != null) {
            head.append("Last-Modified: ").append(HTTP_DATE.format(page.lastModified)).append("\r\n");
        }
        if (body != null) {
            head.append("Content-Type: text/html; charset=utf-8\r\n");
            head.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        }
        head.append("Connection: close\r\n");
        head.append("\r\n");                                   // empty line = end of headers

        out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.write(bodyBytes);
        out.flush();
        log("response: " + code + " " + reason);
    }

    private static void log(String text) {
        System.out.println("[server] " + text);
    }
}
