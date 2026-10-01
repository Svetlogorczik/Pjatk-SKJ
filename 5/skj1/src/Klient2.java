//KLIENT:
//1) zamiast kodu 400 od serwera chcę dostać 200 OK
//2) chcę zobaczyć caałą odpowiedź

import java.io.*;
import java.net.Socket;

public class Klient2 {
    public static void main(String[] args) throws IOException {
        String address = "57.128.198.21";
        int port = 58501;

        String command = "It's me";
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

        StringBuilder text = new StringBuilder();
        String hello = br.readLine();

        log("Response text: " + hello);

        log("Request sending: " + command);
        bw.write(command);
        bw.flush();
        log("Request sent: " + command);

        text.delete(0, text.length());
        String response = br.readLine();

        log("Response text: " + response);

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