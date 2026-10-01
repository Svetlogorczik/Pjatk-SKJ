---
title: Protocol layers, encapsulation and the OSI model
short: Layers & encapsulation
description: Why networks are split into layers (the air-travel analogy), the five Internet layers, data unit names, header encapsulation and the seven layers of the ISO/OSI model.
kind: mixed
source: Lecture 1 — Chapter 1, slides 1-63…1-71 and 1-80 (Kurose & Ross)
---

## In short

- Networks are **complex**, so we split them into **layers**. Each layer provides a **service** to the layer above and **uses** the services of the layer below.
- The Internet stack has **5 layers**: application, transport, network, link, physical.
- Data units: **message** → **segment** → **datagram/packet** → **frame** → **bits**.
- **Encapsulation**: every layer adds **its own header** to the data from the layer above ($M$ → $H_tM$ → $H_nH_tM$ → $H_lH_nH_tM$).
- The **ISO/OSI** model has **7 layers** — additionally **presentation** and **session**, which the Internet doesn't have (if needed, the application does it).

## The problem: a network has tons of pieces

A network means hosts, routers, links of various media, applications, protocols, hardware, software… The lecture's question: **is there any hope of organizing this structure** — or at least our discussion of networks?

## The lecture's analogy: air travel

Air travel is a series of steps involving many services:

| Departure | Service (layer) | Arrival |
|---|---|---|
| ticket (purchase) | ticketing service | ticket (complain) |
| baggage (check) | baggage service | baggage (claim) |
| gates (load) | gate service | gates (unload) |
| runway takeoff | runway service | runway landing |
| airplane routing | **routing service** | airplane routing |

**Layers**: each layer implements a **service**

- via its **own internal-layer actions**,
- **relying on services** provided by the layer below.

Example: the baggage service doesn't know how to fly a plane — it just hands the suitcase "down" and trusts it will arrive.

## Why layering?

From the lecture:

- **Explicit structure** allows identifying the pieces of a complex system and their relationships — a **layered reference model** as a common language.
- **Modularization** eases maintenance and updating: a change in a layer's service **implementation** is **transparent** to the rest of the system. E.g. a change in the gate procedure doesn't affect the rest of the trip.

The lecture also asks: *"layering considered harmful?"*

:::own When do layers get in the way?
- **Duplicated work** — e.g. error checking in several layers at once.
- **Overhead** — every header is extra bytes (we calculate it below).
- Sometimes a layer **needs information from another layer** (e.g. a video app would like to know how loaded the network is), and strict layering makes that hard.
:::

## The Internet protocol stack (5 layers)

| No. | Layer | Job | Example protocols |
|---|---|---|---|
| 5 | **application** | supports network applications | IMAP, SMTP, HTTP |
| 4 | **transport** | **process-to-process** data transfer | TCP, UDP |
| 3 | **network** | **routing of datagrams** from source to destination | IP, routing protocols |
| 2 | **link** | data transfer between **neighbouring** network elements | Ethernet, 802.11 (WiFi), PPP |
| 1 | **physical** | bits "on the wire" | — |

### What is the data called in each layer?

| Layer | Data unit |
|---|---|
| application | **message** |
| transport | **segment** |
| network | **packet / datagram** |
| link | **frame** |
| physical | **bits** |

@svg(encapsulation, caption=Layers, data unit names and the headers added during encapsulation.)

## Encapsulation

When the source sends a message $M$, the data goes **down** the stack and every layer adds **its own header**:

:::steps
1. The application creates a **message** $M$.
2. The transport layer adds header $H_t$ → **segment** $H_t M$.
3. The network layer adds $H_n$ → **datagram** $H_n H_t M$.
4. The link layer adds $H_l$ → **frame** $H_l H_n H_t M$.
5. The physical layer sends **bits**.
:::

On the way:

- a **switch** looks only into the **link** layer (and physical),
- a **router** looks into the **network** layer (and below): it unpacks the frame, reads the IP header, packs it into a new frame for the next link.

At the destination the reverse happens (**decapsulation**): each layer removes **its own** header and passes the rest up, until the application gets just $M$.

:::own Analogy: a letter in envelopes
You write a letter ($M$), put it in an envelope with a room number (transport), that envelope into a bigger one with the building address (network), and that one into the courier's bag with a route number (link). Every intermediary opens only "its own" envelope.
:::

:::example What do headers cost? [[own]]
Typical sizes: TCP header 20 B, IP 20 B, Ethernet 18 B (14 B header + 4 B checksum). You send 100 B of application data.

1. All headers: $20 + 20 + 18 = 58$ B.
2. Whole frame: $100 + 58 = 158$ B.
3. "Efficiency": $100 / 158 \approx 63\%$ — more than a third is pure overhead!
4. For 1460 B of data: $1460 / 1518 \approx 96\%$. Conclusion: tiny packets waste the link.
:::

## The ISO/OSI reference model (7 layers)

| No. | OSI layer | In the Internet? |
|---|---|---|
| 7 | application | yes |
| 6 | **presentation** | **no** |
| 5 | **session** | **no** |
| 4 | transport | yes |
| 3 | network | yes |
| 2 | link | yes |
| 1 | physical | yes |

- **Presentation**: allows applications to **interpret the meaning** of data — encryption, compression, machine-specific conventions.
- **Session**: **synchronization**, checkpointing, **recovery** of data exchange.

The Internet stack is **"missing" these two layers**. If such services are needed, they must be implemented **in the application**. The lecture asks: *are they needed?*

:::own Example from our classes
Character encoding (`StandardCharsets.UTF_8` in Java) or Base64 in HTTP is exactly "presentation" — and **your application** does it. A mnemonic for the 7 OSI layers (bottom-up): **P**lease **D**o **N**ot **T**hrow **S**ausage **P**izza **A**way = Physical, Data link, Network, Transport, Session, Presentation, Application.
:::

## Wireshark and the layers

A slide from the lecture shows how **Wireshark** works:

- **packet capture** (pcap) receives a **copy of every Ethernet frame** sent and received by the computer,
- the **packet analyzer** unpacks it layer by layer: link (Ethernet) → network (IP) → transport (TCP/UDP) → application (e.g. a web browser, an e-mail client).

:::own Where do you see it in Wireshark?
Wireshark's middle pane shows a tree in exactly the order of encapsulation:

```text
Frame 42: 589 bytes on wire            <- physical layer (how many bytes went over)
Ethernet II, Src: ..., Dst: ...        <- link layer (Hl)
Internet Protocol Version 4, Src: ...  <- network layer (Hn)
Transmission Control Protocol, Src Port: 51234, Dst Port: 80  <- transport (Ht)
Hypertext Transfer Protocol            <- application (M)
```

Clicking a row highlights the matching bytes in the bottom pane.
:::

## Layers and Java in our classes [[own]]

| Layer | What we do in class | Who does it |
|---|---|---|
| application | HTTP ([C2](c2-http.html), [C3](c3-tcp-server.html)), DNS ([C6](c6-dns.html)), own text protocols ([C4](c4-text-protocol.html), test, project) | **you** — you write the messages |
| transport | TCP: `Socket`, `ServerSocket`; UDP: `DatagramSocket` | the operating system; you choose TCP or UDP |
| network | IP addresses: `InetAddress`, `127.0.0.1` | the operating system |
| link / physical | Ethernet, WiFi | network card and drivers |

## Check yourself [[own]]

:::check What is the data unit called in the transport, network and link layers?
Segment, datagram (packet), frame.
:::

:::check Which layer does a switch look into, and which does a router?
A switch — the link layer (2). A router — the network layer (3).
:::

:::check Which OSI layers are missing in the Internet stack and who takes over their jobs?
Presentation and session. Their jobs (e.g. encryption, compression, synchronization) are done by the application.
:::

:::check What does it mean that changing a layer's implementation is "transparent"?
The other layers won't notice — as long as the layer provides the same service, it can be rebuilt freely (e.g. replacing a cable with WiFi without changing the browser).
:::

:::cheatsheet
### 5 Internet layers (top-down)
| # | Layer | Unit | Protocols |
|---|---|---|---|
| 5 | application | message | HTTP, SMTP, IMAP, DNS |
| 4 | transport | segment | TCP, UDP |
| 3 | network | datagram/packet | IP, routing |
| 2 | link | frame | Ethernet, WiFi, PPP |
| 1 | physical | bits | — |

### Encapsulation
- $M$ → $H_tM$ → $H_nH_tM$ → $H_lH_nH_tM$ → bits.
- Switch: layers 1–2. Router: layers 1–3. Host: 1–5.

### OSI = 7 layers
- Internet + **presentation** (encryption, compression) + **session** (synchronization, checkpoints).
- Mnemonic: Please Do Not Throw Sausage Pizza Away.

### Header overhead step by step
1. Add up the headers (e.g. TCP 20 + IP 20 + Ethernet 18 = 58 B).
2. Frame = data + headers.
3. Efficiency = data / frame.
:::
