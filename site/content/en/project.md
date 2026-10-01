---
title: The project — a proxy network connecting TCP and UDP servers
short: Project: proxy network
description: What the project requires (2025/26 version), how points are awarded, how to design the proxy step by step — protocol detection, key table, request forwarding, cycles, QUIT — and how to write the protocol documentation.
kind: own
source: Project specification 2025/2026 and the documentation template from the repository; solution plan and advice — original guide
---

## In short

- You get ready-made **key servers** (`TCPServer`, `UDPServer` — each guards one *key = value* pair) and **clients** (`TCPClient`, `UDPClient`). **You must not change them.**
- You write a **`Proxy`** program that joins many servers and other proxies into one network: a client asks **any** proxy and gets an answer as if it asked "the whole world" — **regardless of the protocol** (e.g. TCP client ↔ UDP server).
- You must **design and describe your own protocol** between proxy nodes.
- Points: from **150** (one protocol, no other proxies) to **500** (both protocols, any proxy network **with cycles**).
- Without the **PDF documentation** the project is not graded. Projects are checked by **anti-plagiarism software**.

:::warning This page does not contain a ready solution [[own]]
I deliberately don't publish a whole `Proxy.java`. Projects go through an anti-plagiarism system and plagiarism gives **negative points** — if many people copied the same code from a public page, everybody would lose. Instead you get: a precise explanation of the task, a step-by-step plan, small building-block fragments, the traps of the provided programs (verified on their real code) and test scripts.
:::

:::origin
The 2025/26 specification tells the story of the planet "Numberland", whose inhabitants and knowledge keepers speak two languages (TCP and UDP), and of a wise ruler who builds a network of **intermediaries** who know both languages and exchange knowledge. Below is the same content in plain words. **Check with your instructor whether the specification is the same in your edition** — it may have changed.
:::

## What is given (you don't change it)

### Running the provided programs

```bash
java TCPServer -port <number> -key <name> -value <integer>
java UDPServer -port <number> -key <name> -value <integer>
java TCPClient -address <address> -port <number> -command <command>
java UDPClient -address <address> -port <number> -command <command>
```

- Every server guards **one** key. In the whole network there is **at most one** server for a given key name.
- The TCP client talks only to a TCP server, the UDP client only to a UDP one. A client sends **one** command, reads **one** answer and exits.
- Servers don't keep connections — they handle requests **one by one**.

### The base client–server protocol

Every command and every answer is **one line** of text. In UDP the whole message fits into **one datagram**.

| Command | Answer | Notes |
|---|---|---|
| `GET NAMES` | `OK <count> <name1> <name2> …` | a base server always answers `OK 1 <its key>` |
| `GET VALUE <name>` | `OK <value>` or `NA` | `NA` = I don't have that key |
| `SET <name> <value>` | `OK` or `NA` | changes the key's value |
| `QUIT` | **no answer** | the server exits |
| anything else | `NA` | |

## What your proxy must do

```bash
java Proxy -port <port> -server <address> <port> [-server <address> <port> ...]
```

- `-port` — the port on which the proxy listens **over both TCP and UDP** (for clients and other proxies).
- `-server address port` — at least once, possibly many times: a node the proxy must connect to — **a key server or another proxy**. The same pair may be given to several proxies.
- A proxy may talk **directly only** to the nodes from its parameters; it reaches all others **through other proxies**.
- Wrong or missing parameters → an error message and **exit**.

After starting, the proxy:

:::steps
1. Opens a **TCP and a UDP** port for clients and other proxies.
2. Contacts the nodes from its parameters and finds out **which are servers and which are proxies**, and which **protocol** each server uses.
3. Collects information about **which keys exist in the network** and **how to reach them** (which server, or through which proxy).
4. Serves clients of both protocols:
   - `GET NAMES` → a list of **all** keys in the network (mind the **counter** in the second position!),
   - `GET VALUE` / `SET` → passes them into the network so they reach the right server, and returns the server's answer to the client; if the key doesn't exist in the network it may answer `NA` directly,
   - `QUIT` → passes it on into the network and **shuts down its servers**, then exits itself.
5. **The proxy does not store key values** — it only knows where they are.
:::

@svg(proxy-topology, caption=On the left a tree of proxies (no cycles). On the right a network with the cycle P1→P2→P3→P1 — required for 500 points.)

## Scoring (from the 2025/26 edition)

| Max points | What the proxy must handle |
|---|---|
| **150** | servers and clients (no other proxies), **one** protocol (TCP **or** UDP) |
| **300** | servers and clients (no other proxies), **both** protocols |
| **300** | servers + **other proxies**, the network is a **tree** (no cycles), **one** protocol |
| **400** | servers + other proxies, **tree**, **both** protocols |
| **500** | servers + other proxies, **any network (with cycles)**, **both** protocols |

:::danger An important rule from the documentation template
You declare the project "for" a specific number of points and it is tested **only** for that. If a project declared for 400 points doesn't meet **all** requirements for 400 — it gets **0**, not 300. It's better to declare less and be sure.
:::

## Formal requirements

From the specification and the documentation template (2025/26 edition):

| Requirement | Details |
|---|---|
| Language | **Java 8 (JDK 1.8)**, only standard network classes |
| Files | only **your** `.java` files, start scripts (optional) and the documentation **PDF** |
| Don't include | the provided servers and clients, an IntelliJ project |
| Packages | `.java` files **without** a `package` declaration |
| Compilation | automatically with `javac *.java`, run with a script/scripts |
| Documentation | a file **"Dokumentacja (index number).pdf"** — a **mandatory** condition for grading |
| Tests | everything on **localhost**, on one computer; don't hard-code paths, IPs, ports |
| Test script | the lab instructor decides the system (Windows/Linux) |
| Deadline | in GAKKO; in 2025/26: **19.12.2025, 23:55** (the group instructor could change it) — see [Course rules](course.html) |
| Grading | first correctness and compliance with the specification, then code quality and software engineering |
| Ambiguities | clarify them with the instructor — otherwise you risk failing |
| Plagiarism | plagiarised projects are not checked, and plagiarism gives **negative points** |

## Building the proxy step by step [[own]]

The safest way is to build the project **in stages** — each stage is a working program worth a certain number of points.

:::steps
1. **Parameters.** Read `-port` and the list of `-server address port`. Check the numbers and completeness; an error → a message and `System.exit(1)`.
2. **Listen on TCP and UDP** on the same port number (they are two different ports — see [C5](c5-tcp-udp.html)). Every request in its own thread.
3. **One command handler** — the same function gets a line of text and returns an answer line, no matter whether it came via TCP or UDP.
4. **Detect the servers** and build a **key table**: name → (address, port, protocol, node kind).
5. **GET NAMES / GET VALUE / SET / QUIT** for the servers from the parameters → that's already **150–300 points**.
6. **Talking to other proxies**: design your own commands (introduction, exchanging key lists, forwarding a request). A **tree**-shaped network → **300–400 points**.
7. **Cycles**: a request identifier + a "seen it" memory + a hop limit → **500 points**.
8. The protocol **documentation** (below) and **test scripts**.
:::

### Building block 1: listening on TCP and UDP at once

@code(project/frag-listeners.java, download=no, title=fragment — TCP + UDP listeners)

### Building block 2: ask a node without hanging

Every outgoing request to another node **must have a timeout** — otherwise one dead node freezes the whole proxy.

@code(project/frag-ask-tcp.java, download=no, title=fragment — TCP request with a timeout)

@code(project/frag-ask-udp.java, download=no, title=fragment — UDP request with a timeout)

### Building block 3: detect the protocol and node kind (pseudo-code)

@code(project/frag-detect.txt, download=no, lang=text, title=pseudo-code — detection)

### Building block 4: forwarding without loops (pseudo-code, 500 pts)

@code(project/frag-forward.txt, download=no, lang=text, title=pseudo-code — cycles)

## Traps of the provided programs (verified on their code) [[own]]

| Trap | Where it comes from | What to do |
|---|---|---|
| UDP: `GET NAMES` sent **without a trailing space** gives `NA` | `UDPServer` reads the **whole 256-byte buffer** with a Scanner, so the last word merges with zero bytes (`NAMES\0\0…`). That's why `UDPClient` appends a space. I checked it: without the space the answer is `NA`, with it `OK 1 b`. | when sending to a UDP server, add a space (or `\n`) at the end |
| UDP: the answer has **no line ending** | `UDPServer` sends `output.getBytes()` | read `new String(data, 0, getLength())` and `trim()` |
| UDP: the client's buffer is **256 B** | `UDPClient` receives into `new byte[256]` | a `GET NAMES` answer for a UDP client must not be longer (count the length!) |
| TCP: the client reads **one line** | `TCPClient` does a single `readLine()` | answer = one line ending with `\n` (`println`) |
| `QUIT` has no answer | both clients read nothing after `QUIT` | the proxy must **not** wait for an answer after `QUIT` |
| an incomplete command **kills the server** | the servers use `Scanner.next()`/`nextInt()` and catch only `IOException`; e.g. `SET a x` throws `InputMismatchException` | **validate commands** before forwarding; never send "half" commands |
| the TCP server handles clients **one by one** | a single `accept` loop | short connections, always close the socket after the answer |

## More advice [[own]]

- **The counter in `GET NAMES`**: `OK 3 a b c` — the number must equal the number of names, and every name must appear **once** (in a network with a cycle duplicates come easily — use a `Set`).
- **Threads and shared data**: the key table is read and modified by many threads → `ConcurrentHashMap`, `ConcurrentHashMap.newKeySet()`.
- **Deadlock in a cycle**: if P1 waits for P2's answer while P2 at the same time asks P1 — with handling in a **single** thread both sides wait forever. Handle every request in its own thread and use timeouts.
- **Start order**: a proxy may start before a server — retry detection every now and then (as in [C1](c1-relay.html)).
- **Logs**: print every sent and received message with the node name — with 5 windows open it's the only way to understand anything.
- **Don't invent an overly clever protocol**: a simple, well-described protocol beats a clever, unclear one — you also lose points for the protocol description.

## Test scripts

The scripts assume the folder contains the provided `TCPServer`, `UDPServer`, `TCPClient`, `UDPClient` and **your** `Proxy`. You can change the ports.

@code(project/test-tree.sh)

@code(project/test-tree.bat)

@code(project/test-cycle.sh)

A list of cases to check:

- [ ] `GET NAMES` from every proxy gives **all** keys, each once, with a correct counter,
- [ ] `GET VALUE` for a key on a TCP server and on a UDP server, asking via TCP and via UDP,
- [ ] `SET`, then `GET VALUE` from a **different** proxy shows the new value,
- [ ] an unknown key → `NA`, a wrong command → `NA`,
- [ ] in a network with a cycle nothing loops (no proxy burns the CPU, the logs stop),
- [ ] `QUIT` stops all proxies and servers,
- [ ] wrong `Proxy` parameters → an error and exit.

## Documentation — how to write it

The most important part of the documentation is the **protocol description**. The instructor's template says it plainly: the description must allow somebody to **write their own proxy in another language**, and the only thing in common will be the network conversation. The model is an **RFC** document. **Don't describe Java classes, methods or variables.**

@code(project/doc-outline.txt, download=no, lang=text, title=documentation outline)

:::own How to describe one message (a template — fill in with your commands)
| Description field | What to write |
|---|---|
| Name | e.g. "proxy introduction" |
| From → to, which transport | proxy → proxy, TCP |
| Exact format | words, field order, separator (space), line ending |
| Meaning of fields | what each field means, value range |
| Possible answers | answer format and meaning |
| Errors and timeouts | what the receiver does with a bad format, what the sender does without an answer |
| Example | one line sent and one line received |
:::

## Check yourself [[own]]

:::check Why can't the proxy simply remember key values?
Because the specification forbids it — values live only on the servers (and `SET` changes them in one place). A remembered copy would quickly become stale.
:::

:::check A UDP client asks a proxy about a key stored on a TCP server. What does the proxy do?
It receives the datagram, recognises the command, looks into its table, connects over **TCP** to the right server (or forwards to another proxy), gets an answer line and sends it back **in a UDP datagram** to the client's address and port.
:::

:::check How do you prevent a request from circulating forever in a network with a cycle?
Every request gets a unique identifier; a proxy remembers the identifiers of requests it has already handled and answers `NA` the second time (without forwarding). Plus a hop limit (TTL).
:::

:::cheatsheet
### Base commands
`GET NAMES` → `OK n k1 … kn` · `GET VALUE k` → `OK v`/`NA` · `SET k v` → `OK`/`NA` · `QUIT` → (nothing) · other → `NA`.

### Running
`java Proxy -port P -server host port [-server host port ...]` — a bad parameter → error + exit.

### Points
150 (1 protocol, no proxies) · 300 (2 protocols, no proxies) · 300 (1 protocol, tree) · 400 (2 protocols, tree) · 500 (2 protocols, cycles). You declare → **everything** must work, or 0.

### Architecture
1. parameters → 2. TCP+UDP listeners (threads) → 3. one command handler → 4. detection + key table → 5. forwarding with timeouts → 6. proxy–proxy protocol → 7. ID + seen + TTL (cycles) → 8. QUIT to everyone.

### Traps
- UDP to `UDPServer`: **trailing space** after the command.
- answers: one line; UDP ≤ 256 B.
- `QUIT`: don't wait for an answer.
- validate commands (incomplete ones kill the server).
- `GET NAMES`: unique names + correct counter.
- timeouts everywhere; `ConcurrentHashMap`.

### Submission
`javac *.java` without packages · only your .java + scripts + "Dokumentacja (index number).pdf" · localhost · no hard-coded IPs/ports · GAKKO before the deadline.
:::
