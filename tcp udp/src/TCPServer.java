import java.io.*;

import java.net.ServerSocket;

import java.net.Socket;


public class TCPServer {

    public static void main(String[] args) throws IOException {

        int port = 40000;

        log("Starting");

        ServerSocket ss = new ServerSocket(port);

        log("Server socked allowed");


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


        log("Request reading");

        String line = br.readLine();

        log("Request: " + line);


        String response = line.toUpperCase();

        log("Response sending: " + response);

        bw.write(response);

        bw.newLine();

        bw.flush();

        log("Response sent: " + response);



        log("Client socket closing");

        socket.close();

        log("Client socket closed");

        log("Server socket closing");

        ss.close();

        log("Server socket closed");

        log("Ending");

    }


    public static void log(String message) {

        System.out.println("[S]: " + message);

        System.out.flush();

    }

}