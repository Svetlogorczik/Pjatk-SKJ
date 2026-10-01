// Fragment: one TCP listener and one UDP listener on THE SAME port number (they do not collide).
void startListeners(int port) throws IOException {
    ServerSocket tcp = new ServerSocket(port);
    DatagramSocket udp = new DatagramSocket(port);

    new Thread(() -> {                              // TCP: one thread per client
        while (true) {
            try {
                Socket client = tcp.accept();
                new Thread(() -> serveTcp(client)).start();
            } catch (IOException e) {
                log("TCP accept failed: " + e.getMessage());
            }
        }
    }).start();

    new Thread(() -> {                              // UDP: one datagram = one request
        while (true) {
            try {
                DatagramPacket p = new DatagramPacket(new byte[256], 256);
                udp.receive(p);
                new Thread(() -> serveUdp(udp, p)).start();   // reply to p.getAddress(), p.getPort()
            } catch (IOException e) {
                log("UDP receive failed: " + e.getMessage());
            }
        }
    }).start();
}
