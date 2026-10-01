//KLIENT:
//1) zamiast kodu 400 od serwera chcę dostać 200 OK
//2) chcę zobaczyć caałą odpowiedź

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Klient_task {
    public static void main(String[] args) throws IOException, InterruptedException {
        String address = "172.21.40.125";
        int port = 20000;

        log("Starting");
        Socket socket = new Socket(address, port);
        //======================================================================


//        List<String> task = sendCommand(socket,"s32804 s32804");
//        writeFile(task);

        //task 1
        List<String> task = sendCommand(socket, "s32804 UCHlAzuDCE");
        for(int i = 0; i < 5; i++) {
            task = sendCommand(socket, task.getFirst().replaceAll(" ", ""));
            task.removeFirst();
        }



        //task 2
        for(int i = 0; i < 5; i++) {
            task = sendCommand(socket, String.join("",task));
            task.removeFirst();
        }


        //task 3
        for(int i = 0; i < 5; i++) {
            String pairs = "";
            for(int j = 0; j < task.size()/2; j++)
            {
                pairs = task.get(j*2) + task.get(j*2+1) + "\r\n";
            }
            task = sendCommand(socket, pairs);
            task.removeFirst();
        }


//        //task 4
//        for(int i = 0; i < 4; i++) {
//            int k = Integer.parseInt(task.getFirst());
//            String pairs = "";
//            for(int j = 1; j <= k; j++)
//            {
//                pairs = task.get(j) + task.get(j+k) + "\r\n";
//            }
//            task = sendCommand(socket, pairs);
//            task.removeFirst();
//        }


        //======================================================================
        log("Closing");
        socket.close();
        log("Closed");
        log("Ending");
    }


    public static void log(String message) {
        System.out.println("[C]: " + message);
        System.out.flush();
    }
    public static List<String> sendCommand(Socket socket, String command) throws IOException, InterruptedException {
        List<String> text = new ArrayList<String>();

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
        bw.write(command + "\r\n");
        bw.flush();
        log("Request sent: " + command);

        do {
            text.add(br.readLine());
        } while(in.available() > 0);


        log("Response text: ");
        for(String s : text)
            System.out.println(s);
        return text;
    }

    public static void writeFile(List<String> text) throws IOException {
        FileWriter fw = new FileWriter("output.txt");

        for(String s : text) {
            fw.write(s + "\r\n");
            System.out.println(s);
        }
        fw.close();
    }

}