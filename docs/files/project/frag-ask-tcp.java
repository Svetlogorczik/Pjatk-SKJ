// Fragment (not a whole class): ask a TCP node one question, wait for ONE line, never hang forever.
static String askTcp(String host, int port, String command, int timeoutMs) {
    try (Socket s = new Socket()) {
        s.connect(new InetSocketAddress(host, port), timeoutMs);   // connect timeout
        s.setSoTimeout(timeoutMs);                                 // read timeout
        PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
        out.println(command);
        if (command.equals("QUIT")) {
            return null;                                           // QUIT gets no answer
        }
        return in.readLine();                                      // ONE line = the whole answer
    } catch (IOException e) {
        return null;                                               // refused / timeout / closed
    }
}
