//SERWER:
//1) Zrobić osługę 3 plików - może być fake
//2) Jeżeli request ma datę ostatniej 
   //modyfikacji, odsyłamy 304
//3) Jeżeli request dotyczy czegoś co 
   //nie istnieje odsyłamy 404

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

public class Server {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        String response = "";
        String response1 = "HTTP/1.1 200 OK\r\n\r\n <html> Hello SKJ 2026 a</html>";
        String response2 = "HTTP/1.1 200 OK\r\n\r\n <html> Hello SKJ 2026 b</html>";
        String response3 = "HTTP/1.1 200 OK\r\n\r\n <html> Hello SKJ 2026 c</html>";
        LocalDateTime date1 = LocalDateTime.parse("2018-05-05T11:50:55");
        LocalDateTime date2 = LocalDateTime.parse("2018-05-05T11:50:55");
        LocalDateTime date3 = LocalDateTime.parse("2018-05-05T11:50:55");
        log("Starting");
        ServerSocket ss = new ServerSocket(port);
        while (true) {
            log("Listening");
            Socket socket = ss.accept();
            log("Client connected");
            log("Streams collecting");
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            InputStreamReader isr = new InputStreamReader(in);
            OutputStreamWriter osw = new OutputStreamWriter(out);
            BufferedReader br = new BufferedReader(isr);
            BufferedWriter bw = new BufferedWriter(osw);
            log("Streams collected");
            String line1 = br.readLine();
            StringBuilder line2 = new StringBuilder(br.readLine());
            line2.delete(0, 19);
            LocalDateTime date = LocalDateTime.parse(line2);
            log("Request line1: " + line1);
            log("Request line2: " + line2);
            if (line1.equals("GET /wireshark-labs/INTRO-wireshark-file1.html HTTP/1.1")) {
                if(date.equals(date1))
                    response = "HTTP/1.1 304 Not Modified";
                else
                response = response1;
            } else if (line1.equals("GET /wireshark-labs/INTRO-wireshark-file2.html HTTP/1.1")) {
                if(date.equals(date2))
                    response = "HTTP/1.1 304 Not Modified";
                else
                    response = response2;
            }
            else if (line1.equals("GET /wireshark-labs/INTRO-wireshark-file3.html HTTP/1.1"))
                if(date.equals(date3))
                    response = "HTTP/1.1 304 Not Modified";
                else
                    response = response3;
            else response = "Error 404";

            log("Response sending: " + response);
            bw.write(response);
            bw.flush();
            log("Response sent: " + response);
            socket.close();
        }

    }

    public static void log(String message) {
        System.out.println("[S]: " + message);
        System.out.flush();
    }
    public static void stop(ServerSocket ss) throws IOException {
        ss.close();
        log("Closed");
        log("Ending");
        }
}
