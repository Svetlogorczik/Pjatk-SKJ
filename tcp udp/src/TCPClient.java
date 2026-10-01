import java.io.*;
import java.net.Socket;

public class TCPClient {
    public static void main(String[] args) throws IOException {
        String address = "localhost";
        int port = 40000;

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

        String request = "Hello world!!!";

        log("Request sending: " + request);
        bw.write(request);
        bw.newLine();
        bw.flush();
        log("Request sent: " + request);

        String line1 = br.readLine();
        log("Response line1: " + line1);

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