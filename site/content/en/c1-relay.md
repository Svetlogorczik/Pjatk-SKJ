---
title: Your first distributed program — a relay of processes in a ring
short: Process relay (first run)
description: You run several processes that talk to each other over TCP. You learn ports, localhost, configuration files and the typical "Connection refused" and "Address already in use" errors.
kind: lab
source: logic of lab 1 (running a ready-made distributed program with .prop files) — rewritten task
---

## In short

- The first lab is about **running several processes at once** and seeing them talk to each other over the network — even on one computer.
- Every process has **its own port** (its own "door") and knows **which address and port** to send data to. We store that in a **configuration file**.
- First you have to **open the door** (`ServerSocket`); only then can someone knock (`Socket`).
- Our version: three nodes A → B → C → A form a **ring** and pass a "baton" (*token*) like in a relay race.

:::origin
In lab 1 you ran a ready-made program made of several windows, connected over TCP into a chain; each window read the addresses and ports of its neighbours from a `.prop` file. Below is **our own rewritten version** of the same idea — a console one, with our own code, where every step is visible.
:::

## Starter vocabulary

| Term | Meaning | Example |
|---|---|---|
| **process** | a running program | `java RelayNode relay-a.properties` |
| **IP address** | the computer's "house address" | `127.0.0.1`, `192.168.1.15` |
| **localhost** | "this very computer" — always `127.0.0.1` | `nextHost=localhost` |
| **port** | the number of a program's "door" on the computer (0–65535) | `7001` |
| **socket** | one end of a connection: IP address + port | `127.0.0.1:7001` |
| **server** | a process that **waits** for connections (`ServerSocket`) | a node listening on 7002 |
| **client** | a process that **connects** to a server (`Socket`) | node A connects to B |

:::own Important: one process can be a server and a client at the same time
In the ring **every** node is a server (it waits for the baton from its predecessor) **and** a client (it sends it to the next node). The project is the same: a proxy is a server for clients and a client for servers.
:::

## How our relay works

```text
        +-------------------+
        v                   |
   [A :7001] --> [B :7002] --> [C :7003]
   starter
```

:::steps
1. Every node opens its port (`ServerSocket`) and waits.
2. Node **A** (the starter) sends `TOKEN 1 0 A` to B (round 1, 0 hops, path "A").
3. Every node, after receiving the line: increments the hop counter, appends its name to the path and passes it on.
4. When the baton returns to A, A prints the round summary and starts the next round.
5. After the last round A sends `STOP`, which travels around the ring — every node shuts down.
:::

**The protocol** (format, order, actions — see [W1](w1-internet.html)):

| Message | Format | Receiver's action |
|---|---|---|
| baton | `TOKEN <round> <hops> <path>` | +1 hop, append own name, pass on |
| end | `STOP` | pass `STOP` on and exit (the starter just exits) |

Every TCP connection carries **exactly one line** and is then closed.

## Configuration files

Instead of writing ports in the code, every node reads them from a `.properties` file (format `key=value`, `#` is a comment):

@code(c1/relay-a.properties)

@code(c1/relay-b.properties)

@code(c1/relay-c.properties)

:::tip Why a file and not constants in the code?
You run the same program 3 times with different files — and get 3 different nodes. And in the project the instructor explicitly forbids **hard-coding addresses and ports** (documentation template, note 12). [[own]]
:::

## The node's code

@code(c1/RelayNode.java)

The most important places:

- `new ServerSocket(listenPort)` — we open the door. If the port is taken you get `BindException: Address already in use`.
- `server.accept()` — we **wait** (the program "stands still") until somebody connects.
- `in.readLine()` — we read a **whole line** ending with `\n`.
- `new Socket(nextHost, nextPort)` — we connect to the next node. If nobody listens there you get `ConnectException: Connection refused` — that's why we **retry** every 0.5 s.
- `PrintWriter(..., true)` — `true` means an automatic `flush()` after `println`. Without flush the data can get stuck in a buffer!

## Running it step by step

:::steps
1. Download `RelayNode.java` and the three `.properties` files into **one folder** (the "Download" buttons above).
2. Open a terminal in that folder and compile: `javac RelayNode.java`.
3. Open **three terminals** (or use the script below).
4. In the first: `java RelayNode relay-b.properties`, in the second: `java RelayNode relay-c.properties`.
5. Finally in the third: `java RelayNode relay-a.properties` — A starts the relay.
6. Watch the logs: `->` means a sent line, `<-` a received one.
:::

@code(c1/run-relay.bat)

@code(c1/run-relay.sh)

Expected output at node A (fragment):

```text
[A] listening on port 7001, next node = localhost:7002
[A] -> TOKEN 1 0 A
[A] <- TOKEN 1 2 A>B>C   (from 127.0.0.1:53318)
[A] round 1 finished: 3 hops, path A>B>C>A
...
[A] -> STOP
[A] <- STOP   (from 127.0.0.1:53340)
[A] STOP received - bye
```

:::own Notice the client's port
In the log `from 127.0.0.1:53318` port **53318** is none of our ports. It's an **ephemeral port** — the system picks it at random for every new connection on the client side. Only the **listening** side has a "fixed" port.
:::

## Typical errors and what they mean [[own]]

| Message | Cause | What to do |
|---|---|---|
| `java.net.ConnectException: Connection refused` | **nobody is listening** on that address:port (node not started or wrong port) | start the "servers" first; check the port in the file |
| `java.net.BindException: Address already in use` | the port is **taken** — e.g. a previous run is still alive | close the old process or change the port |
| `java.net.UnknownHostException` | the host name doesn't exist (typo) | fix `nextHost` |
| `NumberFormatException` | a wrong port number, space or letter in the file | fix the `.properties` file |
| a "Windows Firewall" window | Windows asks for permission for Java to use the network | allow it (private network) |
| the program "hangs" without an error | it waits in `accept()` or `readLine()` — the other side sent nothing | check the start order and `flush()` |

Useful commands — who uses a port?

```bash
# Windows
netstat -ano | findstr 7001
# Linux
ss -ltnp | grep 7001
# macOS
lsof -i :7001
```

## Tasks to do on your own

::::task 1. Add a fourth node D
Change **only the configuration files** (not the code!) so that the ring becomes A → B → C → D → A.

:::answer
Create `relay-d.properties` with `name=D`, `listenPort=7004`, `nextPort=7001`. In `relay-c.properties` set `nextPort=7004`. Start B, C, D and finally A. A's log will show `4 hops, path A>B>C>D>A`.
:::
::::

::::task 2. A ring across two computers
With a friend: nodes A and B on your computer, C on theirs.

:::hint
Check your IP address: `ipconfig` (Windows) or `ip a` (Linux) / `ifconfig` (macOS). In the file write the other computer's IP instead of `localhost`. Both computers must be in the same network and the firewall must allow ports 7001–7003.
:::
::::

::::task 3. Break it and observe
a) Remove the retry loop from `send` and start A first. b) Start `relay-b.properties` twice. Which exceptions do you see and why?

:::answer
a) `ConnectException: Connection refused` — B isn't listening yet. b) The second B gets `BindException: Address already in use` — port 7002 is already taken by the first process.
:::
::::

::::task 4. A baton with a payload
Let every node append a random number 1–9 to the message, and let the starter print their **sum** when the baton comes back. Design the message format so that it is unambiguous.

:::answer
E.g. `TOKEN <round> <hops> <path> <sum>`: every node does `sum += random`, and the starter prints the last field. Important: fields are separated by a **space**, and the path must not contain spaces (that's why `A>B>C`).
:::
::::

:::cheatsheet
### Concepts
- Port = a process's "door" (0–65535). Socket = IP + port.
- `localhost` = `127.0.0.1` = this computer.
- Server: `new ServerSocket(port)` → `accept()` (waits).
- Client: `new Socket(host, port)` → connects.
- The client's port is random (ephemeral).

### Start order
1. First the processes that **listen**.
2. Then the one that **starts** sending.
3. Or: retry the connection every few hundred ms.

### Configuration
- `Properties p = new Properties(); p.load(new FileInputStream(file));`
- `p.getProperty("listenPort")` → `Integer.parseInt(...)`.
- Never hard-code IPs and ports.

### Errors
- `Connection refused` → nobody listens.
- `Address already in use` → port taken.
- "Hangs" → waiting in `accept()`/`readLine()`; check `flush()`.
- Who uses a port: `netstat -ano | findstr PORT` / `ss -ltnp` / `lsof -i :PORT`.
:::
