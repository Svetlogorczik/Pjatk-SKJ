---
title: Network performance — delay, loss and throughput
short: Delay & throughput
description: The four components of packet delay, the caravan analogy, traffic intensity La/R, traceroute, packet loss and the bottleneck link — with formulas and step-by-step calculators.
kind: mixed
source: Lecture 1 — Chapter 1, slides 1-44…1-56 (Kurose & Ross)
---

## In short

- Packet delay at one node is the sum of **four** components:
  $d_{nodal} = d_{proc} + d_{queue} + d_{trans} + d_{prop}$.
- $d_{trans} = L/R$ depends on **packet size and link rate**; $d_{prop} = d/s$ depends on **link length** — two completely different things!
- **Traffic intensity** $La/R$: near 0 — small queues; near 1 — queues grow very fast; above 1 — "infinite" delay.
- **traceroute** measures the delay to consecutive routers on the path.
- End-to-end **throughput** = the rate of the **slowest** link on the path — the **bottleneck**.

## Where do delay and loss come from?

Packets **queue** in router buffers. This happens when the **arrival rate** to a link temporarily exceeds the **output link capacity**. Then:

- one packet is being **transmitted** (transmission delay),
- others **wait in the buffer** (queueing delay),
- if there are **no free buffers**, an arriving packet is **dropped** (loss).

## Four sources of delay

$$
d_{nodal} = d_{proc} + d_{queue} + d_{trans} + d_{prop}
$$

@svg(delay, caption=A packet from A to B passes a router: processing → queue → transmission (pushing the bits out) → propagation (the signal travelling).)

| Component | What it is | Depends on | Typically |
|---|---|---|---|
| $d_{proc}$ — **nodal processing** | the router checks bit errors and determines the output link | router speed | < 1 ms |
| $d_{queue}$ — **queueing** | time waiting at the output link for transmission | the router's **congestion level** | from 0 to very large |
| $d_{trans}$ — **transmission** | time to push all the packet's bits into the link | $L$ — packet length [bit], $R$ — link rate [bit/s] | $d_{trans} = L/R$ |
| $d_{prop}$ — **propagation** | time for **one bit** to travel along the link | $d$ — link length [m], $s$ — propagation speed ≈ $2 \cdot 10^8$ m/s | $d_{prop} = d/s$ |

:::warning $d_{trans}$ and $d_{prop}$ are very different (a sentence from the lecture)
- $d_{trans}$ **does not depend on distance** — only on how many bits must be pushed out and how fast the link accepts them.
- $d_{prop}$ **does not depend on packet size** — only on how far away the other end of the link is.
:::

:::own How to remember it?
Imagine pouring water into a pipe: **transmission** is the time to pour the whole bucket (depends on bucket size and funnel width), **propagation** is the time for the first drop to reach the end of the pipe (depends on pipe length).
:::

## The caravan analogy (from the lecture)

A caravan of **10 cars** (a 10-bit "packet") drives through **toll booths** ("routers") that are **100 km** apart.

- Cars "propagate" at **100 km/h**.
- A toll booth needs **12 s** to service one car (the "bit transmission time").
- Car ≈ bit, caravan ≈ packet.

**Question:** how long until the whole caravan is lined up before the 2nd toll booth?

:::steps
1. Time to "push" the whole caravan through the booth (transmission): $12\ \text{s} \cdot 10 = 120\ \text{s} = 2$ min.
2. Time for the last car to drive to the 2nd booth (propagation): $100\ \text{km} / (100\ \text{km/h}) = 1$ h $= 60$ min.
3. Total: $2 + 60 = 62$ minutes.
:::

**The second version from the lecture:** cars now drive **1000 km/h** and a booth needs **1 minute** per car. Will cars arrive at the 2nd booth before all cars are serviced at the 1st?

:::steps
1. The first car leaves the booth after 1 min.
2. It drives 100 km at 1000 km/h: $100/1000$ h $= 6$ min.
3. It reaches the 2nd booth after $1 + 6 = 7$ min.
4. By then 7 cars have passed the 1st booth — **3 are still waiting there**.
:::

**Yes!** The first bits of a packet can reach the next router while the last bits are still in the previous one (when $d_{trans} > d_{prop}$).

## Queueing delay and traffic intensity

Notation from the lecture:

- $R$ — link bandwidth [bit/s],
- $L$ — packet length [bit],
- $a$ — average packet arrival rate [packets/s].

**Traffic intensity**:

$$
I = \frac{L \cdot a}{R}
$$

| Value of $La/R$ | What happens to the queue |
|---|---|
| $\approx 0$ | average queueing delay is **small** |
| $\to 1$ | average queueing delay is **large** (grows very fast) |
| $> 1$ | more "work" arrives than can be serviced — average delay is **infinite** |

:::example Compute the traffic intensity [[own]]
Link $R = 10$ Mb/s, 1500 B packets, on average 500 packets per second.

1. $L = 1500 \cdot 8 = 12\,000$ bit.
2. $L \cdot a = 12\,000 \cdot 500 = 6\,000\,000$ bit/s.
3. $I = 6 \cdot 10^6 / 10^7 = 0.6$ — queues are noticeable but stable.
4. At 800 packets/s: $I = 0.96$ — huge delays. At 900/s: $I = 1.08 > 1$ — the queue grows forever, packets will be lost.
:::

:::tip Lesson for the project [[own]]
Never design a system "at the limit" ($I \approx 1$). A tiny increase in traffic then causes a huge increase in delay — this also applies to your proxy if it serves clients **one by one** in a single thread.
:::

## Real delays: traceroute

**traceroute** measures the delay from the source to **every router** on the path to the destination. How it works (from the lecture):

:::steps
1. For every $i$ it sends **3 packets** that will reach router $i$ on the path (field **TTL = i**).
2. Router $i$ returns packets to the sender.
3. The sender measures the time between transmission and reply — the RTT to router $i$.
:::

A fragment of the lecture's example (path `gaia.cs.umass.edu` → `www.eurecom.fr`):

```text
 1  cs-gw (128.119.240.254)  1 ms  1 ms  2 ms
 ...
 7  nycm-wash.abilene.ucaid.edu (198.32.8.46)  22 ms  22 ms  22 ms
 8  62.40.103.253 (62.40.103.253)  104 ms  109 ms  106 ms   <- trans-oceanic link
 ...
17  * * *                                                    <- no response
19  fantasia.eurecom.fr (193.55.113.142)  132 ms  128 ms  136 ms
```

- Each line is one router with **3 delay measurements**.
- The jump from 22 ms to 104 ms between hop 7 and 8 is a **trans-oceanic link** (long $d_{prop}$).
- `* * *` — no response (probe lost or the router doesn't reply).

:::own Why do delays sometimes "decrease" further along the path? (a lecture question)
Because every measurement is a **separate packet sent at a different moment**. Router queues change from millisecond to millisecond, so a packet to a farther router may simply have hit empty queues. Also, routers often answer such probes "with low priority".

Try it yourself (console):

```bash
# Windows
tracert pjwstk.edu.pl
# Linux / macOS
traceroute pjwstk.edu.pl
# just the RTT to a host
ping pjwstk.edu.pl
```
:::

## Packet loss

- The queue (buffer) in front of a link has **finite capacity**.
- A packet arriving to a **full queue** is **dropped** (lost).
- A lost packet may be retransmitted by the **previous node**, by the **source end system** — or **not at all**.

:::own Who retransmits? It depends on the protocol
- **TCP** notices the missing acknowledgement and **retransmits the segment** — that's why with Java's `Socket` you don't worry about lost data.
- **UDP does nothing** — that's why with `DatagramSocket` you must call `setSoTimeout(...)` yourself and decide what to do when no answer comes. See [C5](c5-tcp-udp.html).
:::

## Throughput

**Throughput** is the rate (bits per unit of time) at which bits **arrive** from sender to receiver:

- **instantaneous** — at a given moment,
- **average** — over a longer period.

The lecture compares links to **pipes** carrying a "fluid" of bits. A server sends a file of $F$ bits to a client; the server's link is $R_s$, the client's link is $R_c$.

| Situation | Average end-to-end throughput |
|---|---|
| $R_s < R_c$ | $R_s$ |
| $R_s > R_c$ | $R_c$ |

**Bottleneck link** — the link on the end-to-end path that **constrains** the end-to-end throughput. In general:

$$
\text{throughput} = \min(R_1, R_2, \dots, R_N)
$$

**The lecture's network scenario:** 10 connections (server–client) **fairly share** a backbone link $R$:

$$
\text{per-connection throughput} = \min\left(R_c,\ R_s,\ \frac{R}{10}\right)
$$

In practice the bottleneck is usually $R_c$ or $R_s$ (the access links), not the backbone.

:::example How long does a download take? [[own]]
A 4 MB file, server $R_s = 2$ Mb/s, client $R_c = 10$ Mb/s, backbone idle.

1. $F = 4 \cdot 8 = 32$ Mb.
2. Bottleneck: $\min(2, 10) = 2$ Mb/s.
3. Time: $32 / 2 = 16$ s.
:::

:::calc tput
:::

## End-to-end delay calculator [[own]]

Below you can compute the delay of a packet over $N$ identical links (store-and-forward): $d_{end\text{-}end} = N\,(d_{proc} + d_{queue} + d_{trans} + d_{prop})$. This "whole path" formula is from the textbook, not from the slides.

:::calc delay
:::

## Check yourself [[own]]

:::check A 1000 B packet, a 1 Mb/s link 2000 km long, s = 2·10⁸ m/s. What are d_trans and d_prop?
$d_{trans} = 8000 / 10^6 = 8$ ms. $d_{prop} = 2 \cdot 10^6 / (2 \cdot 10^8) = 0.01$ s $= 10$ ms.
:::

:::check Traffic intensity is 1.2. What happens?
More data arrives than the link can send. The queue grows forever, and once the buffer is full, packets are lost.
:::

:::check Client 50 Mb/s, server 100 Mb/s, a shared 400 Mb/s link used by 10 connections. What is the throughput of one connection?
$\min(50, 100, 400/10) = \min(50, 100, 40) = 40$ Mb/s — the shared link is the bottleneck.
:::

:::check Why does traceroute send packets with TTL = 1, 2, 3…?
So that the packet "dies" exactly at router $i$ — that router sends a reply back, which tells us its address and the RTT to it.
:::

:::cheatsheet
### Formulas
- $d_{nodal} = d_{proc} + d_{queue} + d_{trans} + d_{prop}$
- $d_{trans} = L / R$ (L in bits!)
- $d_{prop} = d / s$, $s \approx 2 \cdot 10^8$ m/s
- Traffic intensity: $I = La / R$ (≈0 small, →1 large, >1 infinite)
- Throughput $= \min(R_s, R_c, R/n)$; download time $= F / \text{throughput}$
- Path of $N$ links: $N \cdot d_{nodal}$ (textbook)

### "Compute the delay" step by step
1. Write down the data and **convert units**: B → bit (×8), Mb/s → bit/s (×10⁶), km → m (×1000).
2. $d_{trans} = L/R$.
3. $d_{prop} = d/s$.
4. Add $d_{proc}$ and $d_{queue}$ (if given; usually 0).
5. Multiply by the number of links (store-and-forward).
6. Result in ms (×1000).

### Caravan
- 10 cars × 12 s = 120 s + 100 km / 100 km/h = 60 min → **62 min**.
- 1000 km/h, 1 min/car → first car at booth 2 after **7 min**, 3 cars still waiting.

### traceroute
- TTL = i → router i answers; 3 measurements per router; `* * *` = no answer.
- Windows: `tracert`, Linux/macOS: `traceroute`.
:::
