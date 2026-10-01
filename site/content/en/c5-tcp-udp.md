---
title: TCP and UDP together — a stream vs datagrams
short: TCP + UDP together
description: How UDP differs from TCP in practice, how to send and receive a datagram in Java, how to answer the sender, how to measure RTT and how two programs talk over two channels at the same time.
kind: lab
source: logic of the "tcp udp" lab (TCP client/server, UDP client/server, two programs exchanging messages over TCP and UDP in a loop) — rewritten task
---

## In short

- **TCP**: a connection, reliability, order — but it is a **byte stream** (you make message boundaries yourself, e.g. with line endings).
- **UDP**: no connection, no guarantees — but **one `send()` = one datagram = one `receive()`**.
- In UDP you reply to the sender at the **address and port stored in the received packet** (`getAddress()`, `getPort()`).
- From a received packet take only **`getLength()`** bytes — the rest of the buffer is garbage.
- UDP won't tell you a packet was lost — set **`setSoTimeout`** yourself.

:::origin
The "tcp udp" lab had: a TCP client and server (the server sent the text back in upper case), a UDP client and server (the same with datagrams) and a pair of programs that exchanged messages in a loop, once over TCP and once over UDP. Our version: a **UDP mirror** (reverses the text), a **UDP ping** measuring RTT, and a **Coordinator / Worker** pair where tasks go over TCP and results over UDP.
:::

## TCP or UDP? [[own]]

| Property | TCP | UDP |
|---|---|---|
| connection | yes (3-way handshake, [C7](c7-tcp-wireshark.html)) | no |
| delivery guarantee | yes (retransmissions) | **no** |
| order | preserved | may change |
| message boundaries | **none** — a byte stream | **yes** — each datagram separately |
| overhead | bigger (20 B header + acknowledgements) | small (8 B header) |
| Java classes | `ServerSocket`, `Socket` | `DatagramSocket`, `DatagramPacket` |
| typical use | HTTP, e-mail, files | DNS ([C6](c6-dns.html)), games, live video |

:::analogy TCP is a phone call, UDP is text messages [[own]]
With a phone call you connect first, then the conversation flows and you hear everything in order. Text messages are sent without connecting — each one is a separate "parcel", they can arrive in a different order or not at all.
:::

## UDP in Java

@svg(sockets-udp, caption=A UDP server binds to a port and waits in receive(). The client sends a datagram to host:port. The server answers to the sender's address and port taken from the received packet.)

:::steps
1. **Socket**: server `new DatagramSocket(port)` (fixed port), client `new DatagramSocket()` (any free port).
2. **Sending**: turn text into bytes (`getBytes(UTF_8)`), wrap it in `new DatagramPacket(bytes, length, address, port)`, call `send`.
3. **Receiving**: prepare an empty packet with a buffer (`new DatagramPacket(new byte[1500], 1500)`), call `receive` — the method **waits**.
4. **Reading**: `new String(p.getData(), 0, p.getLength(), UTF_8)` — **only `getLength()` bytes!**
5. **Replying**: to `p.getAddress()` and `p.getPort()` from the received packet.
:::

:::danger UDP traps [[own]]
- `new String(p.getData())` without a length — you get the text plus **garbage / zeros** from the rest of the buffer.
- The same `DatagramPacket` reused for `receive` has its **length shrunk** to the previous message — a longer message will be **truncated**. Create a new packet in the loop or call `p.setLength(buffer.length)`.
- A message longer than the buffer is **truncated without any error**.
- Without `setSoTimeout` the client waits for a lost answer **forever**.
- TCP and UDP have **separate** port numbers: different programs can use TCP 6000 and UDP 6000 at the same time (useful in the project — the proxy listens on both).
:::

### UDP mirror (server)

@code(c5/UdpMirror.java)

### UDP ping with RTT measurement (client)

@code(c5/UdpPing.java)

Output (a real printout from testing):

```text
[ping] my local UDP port: 37141
[ping] reply 1: "1# PDU olleH"  RTT = 5.645 ms
[ping] reply 2: "2# PDU olleH"  RTT = 0.555 ms
[ping] reply 3: "3# PDU olleH"  RTT = 0.601 ms
[ping] reply 4: "4# PDU olleH"  RTT = 0.217 ms
```

:::own Why is the first measurement the longest?
With the first packet Java "warms up" (class loading, JIT compilation) and the system prepares structures for the new socket. A good example that **one measurement is not enough** — for RTT you compute an average (see EstimatedRTT in [C7](c7-tcp-wireshark.html)).
:::

When you send a datagram to a port where nobody listens, on Linux you simply see `datagram lost (no answer within 1 s)` — UDP reports no error.

## Two channels at once: Coordinator and Worker

Two programs talk **at the same time** over TCP and UDP:

| Channel | Direction | Message |
|---|---|---|
| TCP | Coordinator → Worker | `TASK <round> <a> <b>` |
| UDP | Worker → Coordinator | `RESULT <round> <a+b>` |
| TCP | Coordinator → Worker | `ACK <round> OK` or `ACK <round> BAD` |
| TCP | Coordinator → Worker | `BYE` (after 5 rounds) |

Who listens where?

- Coordinator: **TCP 6100** (`ServerSocket`) and **UDP 6101** (`DatagramSocket(6101)`).
- Worker: connects over TCP to 6100; its UDP socket has **any** port — it sends to 6101.

@code(c5/Coordinator.java)

@code(c5/Worker.java)

Run: first `java Coordinator 6100 6101`, then `java Worker localhost 6100 6101`. A fragment of the Worker's output:

```text
[worker] connected over TCP, my UDP port is 49180
[worker] TCP <- TASK 1 3 1
[worker] UDP -> RESULT 1 4
[worker] TCP <- ACK 1 OK
[worker] TCP <- TASK 2 88 73
[worker] UDP -> RESULT 2 161
[worker] TCP <- ACK 2 OK
```

## Tasks

::::task 1. RTT statistics
Change `UdpPing` so that it sends 10 packets and prints the **minimum, maximum and average** RTT at the end.

:::answer
Keep `double min = Double.MAX_VALUE, max = 0, sum = 0; int ok = 0;` and update after every answer: `min = Math.min(min, rtt); max = Math.max(max, rtt); sum += rtt; ok++;`. At the end `sum / ok` (watch out for `ok == 0` when everything was lost).
:::
::::

::::task 2. Lost datagrams and retries
In `UdpMirror` randomly skip 30% of the answers (`if (Math.random() < 0.3) continue;`). Add **retries** (max 3 attempts) after a timeout in `UdpPing`.

:::answer
Wrap sending and receiving in a loop `for (int attempt = 1; attempt <= 3; attempt++) { send; try { receive; break; } catch (SocketTimeoutException e) { log("retry " + attempt); } }`. That's exactly what TCP does for you automatically.
:::
::::

::::task 3. A buffer that is too small
Send `UdpMirror` a text longer than 1500 bytes (e.g. 2000 `x` characters). What does the server get?

:::answer
The server gets only the first 1500 bytes — the rest of the datagram is lost **without an exception**. Conclusion: define a maximum message size in your protocol (in the project the provided programs use a **256-byte** buffer!).
:::
::::

::::task 4. Answer over UDP
Let the Coordinator send `ACK` not over TCP but **over UDP**, to the address and port the `RESULT` came from. What must the Worker change?

:::answer
Coordinator: `udp.send(new DatagramPacket(ack, ack.length, p.getAddress(), p.getPort()))`. The Worker, after sending `RESULT`, must call `udp.receive(...)` on **the same** UDP socket (its port is stored in the packet) and set a timeout. The TCP channel is then used only for tasks and `BYE`.
:::
::::

:::cheatsheet
### UDP — recipe
- Server: `DatagramSocket s = new DatagramSocket(port);`
- Client: `DatagramSocket s = new DatagramSocket(); s.setSoTimeout(1000);`
- Send: `byte[] b = text.getBytes(UTF_8); s.send(new DatagramPacket(b, b.length, InetAddress.getByName(host), port));`
- Receive: `DatagramPacket p = new DatagramPacket(new byte[1500], 1500); s.receive(p);`
- Text: `new String(p.getData(), 0, p.getLength(), UTF_8)`
- Reply: `new DatagramPacket(b, b.length, p.getAddress(), p.getPort())`

### TCP vs UDP
| | TCP | UDP |
|---|---|---|
| connection | yes | no |
| guarantees | yes | no |
| message boundaries | no (stream) | yes (datagram) |
| loss | retransmission | timeout in your code |

### Traps
- always `getLength()`;
- a new packet for every `receive` (or `setLength`);
- too small buffer = truncated message, no error;
- TCP port and UDP port with the same number are two different ports.

### RTT
`long t = System.nanoTime(); send...; receive...; (System.nanoTime() - t) / 1e6` = ms.
:::
