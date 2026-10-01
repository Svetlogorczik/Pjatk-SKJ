// Fragment (not a whole class): ask a UDP node one question, wait max timeoutMs for ONE datagram.
static String askUdp(String host, int port, String command, int timeoutMs) {
    try (DatagramSocket s = new DatagramSocket()) {
        s.setSoTimeout(timeoutMs);
        // The provided UDPServer parses the WHOLE 256-byte buffer with a Scanner,
        // so the last word must be followed by a separator - exactly like UDPClient does.
        byte[] data = (command + " ").getBytes(StandardCharsets.UTF_8);
        s.send(new DatagramPacket(data, data.length, InetAddress.getByName(host), port));
        if (command.equals("QUIT")) {
            return null;
        }
        DatagramPacket p = new DatagramPacket(new byte[256], 256);
        s.receive(p);
        return new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8).trim();
    } catch (IOException e) {
        return null;                                               // timeout = no UDP node there
    }
}
