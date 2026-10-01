---
title: Network edge — hosts, access networks and physical media
short: Network edge
description: How your phone or laptop "gets into" the Internet, how fast different links are and how long it takes to push a packet into a link (L/R).
kind: mixed
source: Lecture 1 — Chapter 1, slides 1-10…1-24 (Kurose & Ross)
---

## In short

- The **network edge** consists of hosts: **clients** and **servers** (servers usually live in data centers).
- An **access network** connects a host to its first router (the "edge router"). Examples: cable, DSL, WiFi, 4G/5G, Ethernet at work.
- For an access network we ask two things: **what transmission rate** (bit/s), and is it **shared** or **dedicated**?
- A host splits a message into **packets** of $L$ bits and sends them at rate $R$. Pushing one packet into the link takes $L/R$.
- **Physical media**: guided (twisted pair, coax, fiber) and unguided (radio).

## The network edge: who lives here?

The edge of the network is made of **hosts** — devices that run applications. We split them into:

- **clients** — e.g. your browser, an app on your phone,
- **servers** — e.g. a web server, a mail server; today they are usually in **data centers**.

Between the hosts and the network core there are **access networks** and **physical media** (cables, radio). That is what this topic is about.

## Access networks — how do you get into the Internet?

The lecture's question: **how do we connect an end system to the edge router?** There are three groups of access networks:

1. **residential** — cable, DSL, fiber to the home,
2. **institutional** — school, company, university,
3. **mobile** — WiFi, 4G/5G.

What do we look at?

- The **transmission rate** of the access network (bit/s).
- Is access **shared** with other users, or **dedicated** to you only?

### Cable-based access (HFC)

Cable TV sends many things over **one cable** — each in a different frequency band. This is **FDM** (*Frequency Division Multiplexing*): video channels, data channels and a control channel sit next to each other on different frequencies.

:::own Analogy: FDM is like radio stations
Many radio stations broadcast at the same time and you pick one by turning the frequency knob. A cable works the same way: TV on some frequencies, Internet on others — and they don't disturb each other.
:::

Key facts from the lecture:

- **HFC** (*Hybrid Fiber Coax*) — a mixed network: fiber + coaxial cable connects homes to the ISP router.
- On the operator side (*cable headend*) there is a **CMTS** (*cable modem termination system*); at home there is a **cable modem** and a **splitter**.
- The link is **asymmetric**: towards the home (*downstream*) 40 Mb/s to 1.2 Gb/s, from the home (*upstream*) 30–100 Mb/s.
- Homes **share** the access network up to the *cable headend* — when everybody streams films in the evening, everyone gets a slice.

### DSL — Internet over the telephone line

- **DSL** (*Digital Subscriber Line*) uses the **existing telephone line** to the **central office**.
- In the central office there is a **DSLAM** (*DSL access multiplexer*): data goes to the Internet, voice goes to the telephone network.
- Voice and data use **different frequencies** (FDM again), so you can talk and browse at the same time.
- The line is **dedicated** (not shared with neighbours): 24–52 Mb/s downstream, 3.5–16 Mb/s upstream.

### Home network

A typical home has:

- a cable or DSL **modem** (connection to the operator),
- a **router** with a **firewall** and **NAT**,
- a **WiFi access point** (54 or 450 Mb/s),
- wired **Ethernet** (1 Gb/s).

Very often the modem, router and WiFi are **in one box** — the one you got from your operator.

### Wireless access networks

A shared wireless network connects the host to a router through a **base station**, a.k.a. an **access point**.

| | WLAN (WiFi) | Cellular (wide-area) network |
|---|---|---|
| Range | within or around a building (~100 ft ≈ 30 m) | tens of km |
| Provided by | you / a company / a university | a mobile operator |
| Standards and rates from the lecture | 802.11b/g/n: 11, 54, 450 Mb/s | 4G: tens of Mb/s (5G coming) |

### Enterprise networks (companies, universities)

Companies and universities have a **mix** of wired and wireless links, **switches** and **routers**:

- Ethernet: 100 Mb/s, 1 Gb/s, 10 Gb/s,
- WiFi: 11, 54, 450 Mb/s,
- one **institutional router** connects the whole network to the ISP; inside there are also institutional servers (mail, web).

## A host sends packets: the L/R formula

What does a host do when an application wants to send something?

:::steps
1. It takes the **application message** (e.g. a file).
2. It splits it into smaller chunks — **packets** — each $L$ bits long.
3. It transmits the packets into the access network at transmission rate $R$ (bit/s). $R$ is also called the **link capacity** or **link bandwidth**.
:::

The time needed to **push** a whole packet into the link is the **transmission delay**:

$$
d_{trans} = \frac{L\ \text{[bit]}}{R\ \text{[bit/s]}}
$$

:::example Step-by-step calculation [[own]]
A packet has 1500 bytes, the link is 100 Mb/s. How long does sending the packet take?

1. Convert bytes to bits: $L = 1500 \cdot 8 = 12\,000$ bit.
2. Convert Mb/s to bit/s: $R = 100 \cdot 10^6 = 10^8$ bit/s.
3. Divide: $d_{trans} = 12\,000 / 10^8 = 0.00012$ s $= 0.12$ ms $= 120\ \mu s$.
:::

:::danger The most common unit traps [[own]]
- **B** (byte) ≠ **b** (bit): $1\ \text{B} = 8\ \text{b}$. "MB" means megabytes, "Mb" means megabits.
- For link rates "kilo", "mega", "giga" are powers of **10**: $1\ \text{Mb/s} = 10^6$ bit/s (not $2^{20}$).
- A result in seconds is often easier to read in ms ($\times 1000$) or µs ($\times 10^6$).
:::

## Physical media

- A **bit** propagates between transmitter/receiver pairs.
- A **physical link** is what lies between the transmitter and the receiver.
- **Guided media**: the signal travels in a solid medium — copper, fiber, coax.
- **Unguided media**: the signal propagates freely — radio.

| Medium | How it works | Rates and properties from the lecture |
|---|---|---|
| **Twisted pair** | two insulated, twisted copper wires | Cat 5: 100 Mb/s and 1 Gb/s Ethernet; Cat 6: 10 Gb/s |
| **Coaxial cable** | two concentric copper conductors | bidirectional; broadband (many channels); hundreds of Mb/s per channel |
| **Fiber optic** | glass fiber carrying light pulses — each pulse is a bit | tens–hundreds of Gb/s; low error rate; repeaters far apart; immune to electromagnetic noise |
| **Radio** | signal in the electromagnetic spectrum, no physical "wire" | broadcast, *half-duplex*; problems: reflection, obstruction, interference |

Radio link types from the lecture:

- **terrestrial microwave** — channels up to 45 Mb/s,
- **WiFi (WLAN)** — up to hundreds of Mb/s,
- **cellular** (4G) — tens of Mb/s,
- **satellite** — up to 45 Mb/s per channel, **270 ms** end-to-end delay; **geosynchronous** vs **low-earth-orbit** (LEO) satellites.

:::own What does "half-duplex" mean?
**Half-duplex** — only one side transmits at a time (like a walkie-talkie: "over!"). **Full-duplex** — both sides transmit at the same time (like a phone call). That's why radio is harder than a cable: devices must "agree" who talks now.
:::

## Check yourself [[own]]

:::check Why can cable Internet slow down in the evening, while DSL usually doesn't?
Because in HFC the access network is **shared** by homes up to the *cable headend*. With DSL everyone has a **dedicated** line to the central office.
:::

:::check How long does it take to push a 1000 B packet into a 1 Gb/s link?
$L = 8000$ bit, $R = 10^9$ bit/s, so $d_{trans} = 8 \cdot 10^{-6}$ s $= 8\ \mu s$.
:::

:::check Which medium is immune to electromagnetic noise and has the lowest error rate?
Fiber optic cable.
:::

:::check What does it mean that a cable link is asymmetric?
The downstream rate (towards the home) is much higher than the upstream rate, e.g. 1.2 Gb/s vs 100 Mb/s.
:::

:::cheatsheet
### Access networks — numbers from the lecture
| Type | Rate | Shared? |
|---|---|---|
| Cable HFC | ↓ 40 Mb/s–1.2 Gb/s, ↑ 30–100 Mb/s | yes (to headend) |
| DSL | ↓ 24–52 Mb/s, ↑ 3.5–16 Mb/s | no (dedicated) |
| WiFi 802.11b/g/n | 11 / 54 / 450 Mb/s | yes, range ~30 m |
| 4G | tens of Mb/s | yes, range km |
| Ethernet | 100 Mb/s, 1 Gb/s, 10 Gb/s | — |
| Satellite | up to 45 Mb/s, 270 ms delay | — |

### Media
- Twisted pair Cat 5: 100 Mb/s–1 Gb/s; Cat 6: 10 Gb/s.
- Coax: many channels, hundreds of Mb/s per channel.
- Fiber: 10–100+ Gb/s, immune to noise.
- Radio: half-duplex, reflection, obstruction, interference.

### Transmission delay — step by step
1. $L$ in bytes → $\times 8$ → bits.
2. $R$ in Mb/s → $\times 10^6$ → bit/s.
3. $d_{trans} = L / R$ [s].
4. Result ×1000 → ms.

Example: 1500 B at 100 Mb/s → 12 000 / 10⁸ = 0.12 ms.
:::
