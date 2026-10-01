---
title: Internet history — from ARPAnet to smartphones
short: Internet history
description: The key dates and ideas: packet switching, ARPAnet, Cerf and Kahn's principles, TCP/IP, DNS, the Web, commercialization and the Internet "everywhere" — plus the summary of chapter 1.
kind: mixed
source: Lecture 1 — Chapter 1, slides 1-72…1-78 (Kurose & Ross)
---

## In short

- **1961–1972**: the idea of **packet switching** and the **ARPAnet** are born.
- **1972–1980**: connecting many networks (*internetworking*) — **Cerf and Kahn's principles**, which define today's Internet.
- **1980–1990**: new protocols — **TCP/IP, SMTP, DNS, FTP**, TCP congestion control.
- **1990–2000**: the **Web**, commercialization, new applications.
- **2005–today**: smartphones, fast wireless access, social networks, the cloud, the giants' own networks.

## 1961–1972: early packet-switching principles

:::timeline
- **1961** — Kleinrock: **queueing theory** shows the effectiveness of packet switching.
- **1964** — Baran: packet switching in **military networks**.
- **1967** — ARPAnet conceived by ARPA (*Advanced Research Projects Agency*).
- **1969** — the first ARPAnet node operational.
- **1972** — ARPAnet public demo; **NCP** (*Network Control Protocol*) — the first host-host protocol; the first **e-mail** program; ARPAnet has **15 nodes**.
:::

## 1972–1980: internetworking, new and proprietary networks

:::timeline
- **1970** — **ALOHAnet**: a satellite network in Hawaii.
- **1974** — **Cerf and Kahn**: an architecture for interconnecting networks.
- **1976** — **Ethernet** at Xerox PARC.
- **late 70s** — proprietary architectures: DECnet, SNA, XNA; switching **fixed-length** packets (an ATM precursor).
- **1979** — ARPAnet has **200 nodes**.
:::

**Cerf and Kahn's internetworking principles** — they define today's Internet architecture:

| Principle | What it means (simply) [[own]] |
|---|---|
| **minimalism, autonomy** | **no internal changes** are required to interconnect networks |
| **best-effort** service model | the network "tries hard" but **guarantees nothing** (a packet may be lost) |
| **stateless** routing | routers don't remember connections — each packet is handled separately |
| **decentralized** control | there is no single "boss" of the Internet |

:::own Where do you see these principles in class?
"Best effort" is the reason why **UDP can lose datagrams** and **TCP** must detect losses and retransmit by itself ([C5](c5-tcp-udp.html), [C7](c7-tcp-wireshark.html)). "Decentralized control" also shows up in the project — the proxy network has no central server, each node only knows its neighbours.
:::

## 1980–1990: new protocols, a proliferation of networks

:::timeline
- **1982** — the **SMTP** e-mail protocol defined.
- **1983** — deployment of **TCP/IP**; **DNS** defined for name-to-IP-address translation.
- **1985** — the **FTP** protocol defined.
- **1988** — **TCP congestion control**.
- **1980s** — new national networks: CSnet, BITnet, NSFnet, Minitel; **100,000 hosts** connected to a confederation of networks.
:::

## 1990–2000s: commercialization, the Web, new applications

:::timeline
- **early 1990s** — ARPAnet decommissioned.
- **1991** — NSF lifts restrictions on **commercial** use of NSFnet (NSFnet decommissioned in 1995).
- **early 1990s** — the **Web**: hypertext (Bush 1945, Nelson 1960s), **HTML and HTTP** — Berners-Lee.
- **1994** — the **Mosaic** browser, later **Netscape**.
- **late 1990s** — commercialization of the Web.
- **late 1990s – 2000s** — more "killer apps": instant messaging, **P2P** file sharing; network **security** comes to the forefront; est. **50 million hosts**, **100 million+ users**; backbone links running at **Gbps**.
:::

## 2005–present: the Internet is "everywhere"

- ~**18 billion devices** attached to the Internet (2017),
- the rise of **smartphones** (iPhone: 2007),
- aggressive deployment of **broadband** access,
- increasing ubiquity of high-speed wireless access: **4G/5G, WiFi**,
- **online social networks** — Facebook: ~2.5 billion users,
- service providers (Google, Facebook, Microsoft) create **their own networks**, bypassing the commercial Internet to be "close" to the end user (see [W4](w4-structure.html)),
- enterprises run their services **in the cloud** (Amazon Web Services, Microsoft Azure).

## Chapter 1 summary (from the final slide)

We have covered a "ton" of material:

- Internet overview, what's a protocol ([W1](w1-internet.html)),
- network edge, access network, core ([W2](w2-edge.html), [W3](w3-core.html)),
- packet switching vs circuit switching, Internet structure ([W3](w3-core.html), [W4](w4-structure.html)),
- performance: loss, delay, throughput ([W5](w5-performance.html)),
- layering, service models ([W7](w7-layers.html)),
- security ([W6](w6-security.html)),
- history (this page).

You now have **context, an overview, vocabulary and a "feel" for networking**. More depth, detail and fun follow — in the labs.

:::own Dates that connect to the labs
| Year | Event | Where you meet it in class |
|---|---|---|
| 1976 | Ethernet | the link layer in Wireshark ([W7](w7-layers.html)) |
| 1983 | TCP/IP, DNS | TCP sockets ([C3](c3-tcp-server.html)), a hand-made DNS query ([C6](c6-dns.html)) |
| 1988 | TCP congestion control | slow start in Wireshark ([C7](c7-tcp-wireshark.html)) |
| ~1990 | HTTP, HTML | HTTP analysis and your own server ([C2](c2-http.html), [C3](c3-tcp-server.html)) |
:::

## Check yourself [[own]]

:::check Who proposed the internetworking architecture the Internet is based on, and when?
Cerf and Kahn, 1974.
:::

:::check In which year was TCP/IP deployed and DNS defined?
In 1983.
:::

:::check What does "best effort" mean?
The network tries to deliver a packet but guarantees nothing — a packet can be lost, delayed or arrive out of order.
:::

:::check Who created HTML and HTTP?
Tim Berners-Lee (early 1990s).
:::

:::cheatsheet
### Timeline (exam minimum)
- **1961** Kleinrock — queueing theory, packets are effective.
- **1964** Baran — packets in military nets.
- **1967** ARPA conceives ARPAnet; **1969** first node.
- **1970** ALOHAnet (Hawaii, satellite).
- **1972** ARPAnet demo, NCP, first e-mail, 15 nodes.
- **1974** Cerf and Kahn — internetworking.
- **1976** Ethernet (Xerox PARC).
- **1979** ARPAnet: 200 nodes.
- **1982** SMTP; **1983** TCP/IP + DNS; **1985** FTP; **1988** TCP congestion control.
- **1991** commercial use of NSFnet; **1995** NSFnet decommissioned.
- **~1990** Web: HTML, HTTP (Berners-Lee); **1994** Mosaic → Netscape.
- **2007** iPhone; **2017** ~18 billion devices.

### Cerf and Kahn's principles
1. Minimalism, autonomy (no internal changes).
2. Best effort.
3. Stateless routing.
4. Decentralized control.
:::
