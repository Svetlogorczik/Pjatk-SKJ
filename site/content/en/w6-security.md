---
title: Network security — who the "bad guys" are and what they can do
short: Security
description: Malware (viruses, worms, spyware, botnets), DoS/DDoS attacks, packet sniffing and IP spoofing.
kind: mixed
source: Lecture 1 — Chapter 1, slides 1-57…1-62 (Kurose & Ross)
---

## In short

- The Internet was **not originally designed with security in mind** — it assumed a group of mutually trusting users. Today designers are "playing catch-up", and security concerns **all layers**.
- **Malware**: a **virus** (needs you to run something), a **worm** (runs by itself), **spyware** (spies on you); infected computers form a **botnet**.
- **DoS / DDoS**: flooding a server or a link with bogus traffic so that real users cannot use it.
- **Sniffing**: on broadcast media (WiFi, shared Ethernet) other people's packets can be captured — Wireshark is exactly such a sniffer.
- **IP spoofing**: sending a packet with a **false source address**.

## What is network security about?

According to the lecture, the field studies three things:

1. how **bad guys** can attack computer networks,
2. how we can **defend** networks against attacks,
3. how to **design** architectures that are immune to attacks.

The problem: the Internet was **not originally designed with (much) security in mind**. The original vision was *"a group of mutually trusting users attached to a transparent network"*. That's why protocol designers are **playing catch-up**, and security considerations appear **in all layers** (layers — see [W7](w7-layers.html)).

## Malware

Malware can get into a host as:

| Type | How it spreads | Key difference |
|---|---|---|
| **Virus** | self-replicating infection by **receiving and executing** an object, e.g. an e-mail attachment | needs **your action** (opening the file) |
| **Worm** | self-replicating infection by **passively receiving** an object that **gets itself executed** | spreads **without your participation** |

Additionally:

- **Spyware** can record keystrokes and visited web sites, and upload this info to a collection site.
- An infected host can be enrolled in a **botnet** — an army of compromised computers used for **spam** or **DDoS** attacks.

## Denial of service: DoS and DDoS

**Denial of Service (DoS)** — attackers make resources (a server, bandwidth) **unavailable to legitimate traffic** by **overwhelming** them with bogus traffic.

The attack scheme from the lecture (**distributed** DoS, i.e. DDoS):

:::steps
1. **Select a target** (e.g. a shop's server).
2. **Break into hosts** around the network (build a botnet).
3. **Send packets** to the target from all compromised hosts at once.
:::

:::own The link to W5 and to your project
- DDoS deliberately pushes the traffic intensity to $La/R > 1$ (see [W5](w5-performance.html)) — queues grow forever and normal packets are lost.
- You can accidentally "mini-DoS" yourself: if your server or proxy serves clients **one by one** and waits without a limit for a single slow client, everyone else is stuck. Fixes: **timeouts** (`setSoTimeout`) and **threads** per client.
:::

## Packet interception: sniffing

**Packet sniffing**:

- works on **broadcast media** — shared Ethernet, wireless networks — where the signal reaches many devices at once,
- a network interface in **"promiscuous" mode** reads and records **all** packets passing by, not only those addressed to it — **including passwords!**

On the slide: C captures a packet `src:B dest:A payload` that is not meant for it at all.

**Wireshark**, used in our labs, is exactly such a (free) **packet sniffer**.

:::own See it with your own eyes — HTTP Basic Auth
In [C2](c2-http.html) you will see the header `Authorization: Basic c3R1ZGVudDpzaWVjMTIz` in Wireshark. That is **not encryption**, only Base64 encoding — anyone who captures the packet can read the login and password:

@code(c2/Base64Peek.java)

That's why passwords are only sent over **HTTPS** (TLS encrypts the data). More about cryptography is in chapter 8 of the textbook.
:::

:::warning Ethics and law [[own]]
Sniff **only your own traffic** or a network you are allowed to monitor (e.g. the lab). Intercepting other people's communication without permission is a crime in Poland (among others, art. 267 of the Criminal Code) and in most countries.
:::

## Fake identity: IP spoofing

**IP spoofing** — sending a packet with a **false source address**. On the slide host C sends a packet with `src:B`, pretending to be B.

Why? E.g. to hide the real source of a DDoS attack or to frame somebody else. The lecture announces: *lots more on security throughout the course, especially in chapter 8.*

:::own Fun fact: why is spoofing easier with UDP than with TCP?
TCP requires a **three-way handshake** (see [C7](c7-tcp-wireshark.html)): the server's reply goes back to the **real** address B, so attacker C never sees it and cannot complete the connection. UDP has no "handshake" at all, so a forged datagram is simply accepted.
:::

## Check yourself [[own]]

:::check What is the difference between a virus and a worm?
A virus needs a user action (running an infected object, e.g. an attachment). A worm spreads by itself — it's enough that a host passively receives an object that executes itself.
:::

:::check What is a botnet and what is it used for?
A network of compromised (infected) computers controlled by the attacker. It is used for spam and DDoS attacks.
:::

:::check Why is sniffing especially easy on WiFi?
Because radio is a broadcast medium — the signal reaches all devices in range, and a card in promiscuous mode records all packets.
:::

:::check Does Base64 encoding protect a password?
No. Base64 is just a different notation that anyone can reverse. Protection comes from encryption (e.g. HTTPS/TLS).
:::

:::cheatsheet
### Attacks — table to remember
| Attack | What it does |
|---|---|
| Virus | infection after **executing** an object (attachment) |
| Worm | infection that **executes itself** |
| Spyware | records keystrokes, sites, uploads data |
| Botnet | army of infected hosts (spam, DDoS) |
| DoS/DDoS | flooding a resource with bogus traffic |
| Sniffing | capturing packets on a broadcast medium |
| IP spoofing | false source address |

### Facts from the lecture
- The Internet was designed for "mutually trusting users" → security added later, **in all layers**.
- DDoS: 1) select target, 2) break into hosts (botnet), 3) send packets to target.
- Wireshark = free packet sniffer.

### Practice (author's notes)
- Base64 ≠ encryption → passwords only over HTTPS.
- Timeouts and threads protect your server from being "frozen" by one client.
- Sniff only your own traffic.
:::
