---
title: What is the Internet and what is a protocol?
short: Internet & protocols
description: Two views of the Internet — "what it is built from" and "what it gives to applications" — and the most important word of the whole course: protocol.
kind: mixed
source: Lecture 1 — Chapter 1, slides 1-1…1-9 (Kurose & Ross)
---

## In short

- The Internet is a **network of networks**: billions of devices connected by thousands of providers (ISPs).
- You can look at it in two ways: **what it is built from** (devices, links, routers) and **what service it gives to applications**.
- A **protocol** = an agreement that defines the **format** of messages, their **order** and the **actions** taken when sending/receiving them.
- Internet standards are **RFC** documents (Request for Comments), prepared by the **IETF**.

## Map of chapter 1

The lecture opens chapter 1 of the Kurose & Ross textbook. Its goal: get the "big picture" and the vocabulary — details come later. On this site every item of the lecture roadmap has its own topic:

| Lecture roadmap item | Topic on this site |
|---|---|
| What is the Internet? What is a protocol? | W1 (this page) |
| Network edge: hosts, access networks, physical media | [W2](w2-edge.html) |
| Network core: packet/circuit switching | [W3](w3-core.html), [W4](w4-structure.html) |
| Performance: loss, delay, throughput | [W5](w5-performance.html) |
| Security | [W6](w6-security.html) |
| Protocol layers, service models | [W7](w7-layers.html) |
| History | [W8](w8-history.html) |

## View no. 1: what the Internet is built from ("nuts and bolts")

Imagine taking the Internet apart. You would find three kinds of "building blocks":

| Building block | What it is | Examples |
|---|---|---|
| **Host** (*end system*) | a device at the **edge** of the network that runs network applications | laptop, phone, web server, game console, "smart" fridge |
| **Packet switch** | a device that **forwards** packets (chunks of data) | **router**, **switch** |
| **Communication link** | the "road" bits travel on | fiber, copper, radio, satellite |

Every link has a **transmission rate**, also called **bandwidth**, measured in **bits per second** (bit/s, Mb/s, Gb/s).

A **network** is a collection of devices, routers and links **managed by one organization** — e.g. a home network, a company network, a university network, a mobile operator network.

@svg(internet-overview, caption=Network edge (hosts in homes, companies, mobile networks and data centers) and network core (interconnected routers R).)

:::own Analogy: the Internet as mail and roads
- A **host** is a house — someone writes and reads letters there (applications).
- A **packet** is a letter in an envelope — a long text is split into many envelopes.
- A **router** is a crossroads with signposts — it looks at the address on the envelope and decides which road to send it down.
- A **link** is a road — one is a motorway (fiber), another a narrow path (old cable).
:::

The lecture also showed "fun" Internet-connected devices: a pacemaker with a monitor, Amazon Echo, an IP picture frame, a toaster with weather forecast, a fridge, a security camera, a bed with sensors, Fitbit, AR devices… Conclusion: **almost anything can be a host**, not only a computer.

### The Internet = "a network of networks"

The Internet is not one network. It is **many interconnected providers** (*ISP — Internet Service Provider*): local, regional, national and global. The details of this structure are in [W4](w4-structure.html).

**Protocols** are everywhere — they control sending and receiving of messages, e.g. HTTP (Web), video streaming, Skype, TCP, IP, WiFi, 4G, Ethernet.

**Internet standards**:

- **RFC** — *Request for Comments*, documents describing protocols,
- **IETF** — *Internet Engineering Task Force*, the organization that prepares them.

:::own Example RFCs that are useful in this course
- RFC 768 — UDP (short, worth skimming!),
- RFC 9293 — TCP (successor of RFC 793),
- RFC 1035 — DNS (the message format you will use in [C6](c6-dns.html)),
- RFC 9110 and 9112 — HTTP (semantics and HTTP/1.1, useful in [C2](c2-http.html) and [C3](c3-tcp-server.html)).

The project documentation template names RFC documents as the model for describing a protocol — so it is worth seeing what a real RFC looks like.
:::

## View no. 2: the Internet as a service

From a programmer's point of view, the Internet is an **infrastructure that provides services to applications**: Web, video, teleconferencing, e-mail, games, e-commerce, social media, home appliances…

The Internet also provides a **programming interface** for distributed applications — "hooks" that let a program send and receive data, i.e. "connect" to the transport service. The lecture compares it to the **postal service**: there are several service options (normal letter, registered letter…), and you choose the one you need.

:::own In Java these "hooks" are sockets
In the labs you will use exactly these "hooks":

- `Socket` and `ServerSocket` — the **TCP** service (reliable, like a registered letter with confirmation),
- `DatagramSocket` and `DatagramPacket` — the **UDP** service (fast, no guarantees, like a postcard).

```java
// A TCP connection to a web server on port 80 - one line "connects" the program to the Internet
Socket socket = new Socket("example.com", 80);
```
:::

## What is a protocol?

People first. When you ask someone in the street for the time, a "protocol" is at work too:

- you send **specific messages** ("Excuse me…", "What time is it?"),
- after receiving an answer you take **specific actions** (say thanks, walk away),
- if somebody ignores your "Excuse me…", you **don't** immediately ask for the time.

In networks, **devices** exchange messages instead of people — but the principle is the same. **All communication on the Internet is governed by protocols.**

:::note Definition from the lecture
A **protocol** defines the **format** and the **order** of messages sent and received among network entities, and the **actions** taken on message transmission and receipt.
:::

The lecture compares a human conversation with a computer conversation:

| People | Computers |
|---|---|
| "Hi" | TCP connection request |
| "Hi" | TCP connection response |
| "Got the time?" | `GET http://gaia.cs.umass.edu/kurose_ross` |
| "2:00" | `<file>` — the page content |

The slide also says something important: a protocol is a **distributed algorithm** that works through communication — every side performs its own part and reacts to the messages of the other side.

### Three ingredients of every protocol

:::steps
1. **Format** — what does a single message look like? (words, separators, line ending, field order, bytes)
2. **Order** — who speaks first? Which message is the answer to which?
3. **Actions** — what do we do after receiving a message? What if something unknown arrives, or nothing arrives at all?
:::

:::own Example: our own protocol from lab C4
This is a conversation between a client and the `QuoteDesk` server (see [C4](c4-text-protocol.html)). `S:` is the server, `C:` is the client:

```text
S: HELLO QuoteDesk 1.0      <- order: the server speaks first
C: NAME ola                 <- format: word + space + nick, line ends with \r\n
S: OK ola
C: QUOTES 2
S: BEGIN 2                  <- action: the server sends exactly 2 lines and an END marker
S: UDP: I told you once. Did you get it? Not my problem.
S: DNS: it's always DNS.
S: END
C: XYZ
S: ERR unknown command: XYZ <- action on error
```

All three ingredients are visible: the **format** of a line, the **order** (first `HELLO`, then `NAME`) and the **actions** (what the server does after `QUOTES`, and after an unknown command).
:::

## Why does it matter in labs and the project? [[own]]

- In the **practical test** you get a protocol described in words — you must follow its format (e.g. one line ending with `\r\n`) and order exactly.
- In the **project** you must **design and describe your own protocol** between proxy nodes. The documentation template says it clearly: the description must let someone **write their own implementation in another language** — so it must contain exactly the format, order and actions.
- When a program "hangs", it is almost always a protocol problem: one side waits for a message the other side will never send.

## Check yourself [[own]]

:::check What is the difference between a host and a router?
A host is an **end system** — applications run on it (e.g. a browser, a web server). A router is a **packet switch** inside the network — it does not run user applications, it forwards packets towards their destination.
:::

:::check Which three things does a protocol define?
The **format** of messages, their **order**, and the **actions** taken when a message is sent/received.
:::

:::check What is an RFC and who prepares it?
An RFC (*Request for Comments*) is a document with an Internet standard, e.g. a protocol description. It is prepared by the IETF (*Internet Engineering Task Force*).
:::

:::check Why is the Internet called "a network of networks"?
Because it consists of many independent networks (ISPs, company, university and operator networks) that are interconnected. There is no single "main" network that controls everything.
:::

:::cheatsheet
### Definitions (learn by heart)
- **Host / end system** — device at the network edge, runs applications.
- **Packet switch** — router or switch, forwards packets.
- **Link** — fiber, copper, radio, satellite; has a **bandwidth** in bit/s.
- **Network** — devices + routers + links managed by one organization.
- **Internet** — network of networks, interconnected ISPs.
- **Protocol** — **format + order + actions** for messages.
- **RFC** — standard document; **IETF** — who writes them.

### Two views of the Internet
1. "Nuts and bolts": hosts, routers/switches, links, networks.
2. "Service": infrastructure for applications + programming interface (in Java: sockets `Socket`, `DatagramSocket`).

### How to describe a protocol step by step (project, test)
1. List the participants (client, server, proxy).
2. For every message give the **exact format** (words, spaces, `\r\n`).
3. Describe the **order**: who starts, what answers what.
4. Describe **actions and errors**: unknown command, no answer.
5. Add an **example conversation** (like in an RFC).
:::
