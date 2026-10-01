//KLIENT:
//1) zamiast kodu 400 od serwera chcę dostać 200 OK
//2) chcę zobaczyć caałą odpowiedź

import java.io.*;
import java.net.Socket;

public class Klient {
    public static void main(String[] args) throws IOException {
        String address = "localhost";
        int port = 8080;

        String command = "GET /wireshark-labs/INTRO-wireshark-file3.html HTTP/1.1\r\n"+
                        "If-Modified-Since: 2018-05-05T11:50:56\r\n";
        log("Starting");
        Socket socket = new Socket(address, port);
        log("Connected");
        log("Streams collecting");
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        InputStreamReader isr = new InputStreamReader(in);
        OutputStreamWriter osw = new OutputStreamWriter(out);
        BufferedReader br = new BufferedReader(isr);
        BufferedWriter bw = new BufferedWriter(osw);
        log("Streams collected");
        log("Request sending: " + command);
        bw.write(command);
        bw.flush();
        log("Request sent: " + command);
        StringBuilder text = new StringBuilder();
        String line = br.readLine();
        while(line != null)
        {
            text.append(line);
            text.append("\r\n");
            line = br.readLine();
        }
        log("Response text: " + text);

        log("Closing");
        socket.close();
        log("Closed");
        log("Ending");
    }


    public static void log(String message) {
        System.out.println("[C]: " + message);
        System.out.flush();
    }

}