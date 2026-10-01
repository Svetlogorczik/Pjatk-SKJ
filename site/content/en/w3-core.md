---
title: Network core — packet switching and circuit switching
short: Network core
description: What happens to a packet inside the network — store-and-forward, queues and loss, forwarding vs routing — and why the Internet chose packets instead of "reserving a line" like the telephone.
kind: mixed
source: Lecture 1 — Chapter 1, slides 1-25…1-33 (Kurose & Ross)
---

## In short

- The **network core** is a mesh of interconnected **routers**.
- **Packet switching**: a host breaks data into packets, and routers forward them from one to the next at **full link capacity**.
- **Store-and-forward**: a router must receive the **entire packet** before it starts sending it on.
- When packets arrive faster than the router can send them, a **queue** forms; when the buffer is full, packets are **lost**.
- **Forwarding** = the local "which output?" decision, **routing** = global path computation.
- **Circuit switching** (telephone) reserves resources for the whole call. Packets let us serve **more users**, but without guarantees.

## The network core: a mesh of routers

The network core is a **mesh** of interconnected routers. It works by **packet switching**:

- hosts break application-layer messages into **packets**,
- routers **forward** packets from one router to the next, across links on the path from source to destination,
- each packet is transmitted at **full link capacity**.

## Store-and-forward

The rule from the lecture: **the entire packet must arrive at the router before the router can transmit it** on the next link.

Pushing an $L$-bit packet into an $R$ bit/s link takes $L/R$ seconds. On the path **source → router → destination** (two links of rate $R$, ignoring propagation delay):

$$
d_{end\text{-}end} = 2\,\frac{L}{R}
$$

:::example Example from the lecture (one hop)
$L = 10$ kbit, $R = 100$ Mb/s. Transmission delay on one link:

$$
\frac{L}{R} = \frac{10\,000\ \text{bit}}{100\,000\,000\ \text{bit/s}} = 0.0001\ \text{s} = 0.1\ \text{ms}
$$
:::

:::own Generalisation: N links and P packets
This is from the textbook (not from the slides), but it is very handy in exercises:

- **one packet**, $N$ links (i.e. $N-1$ routers on the way): $d = N \cdot \dfrac{L}{R}$,
- **$P$ packets** sent back to back over $N$ links: $d = (N + P - 1)\cdot \dfrac{L}{R}$.

Why? The first packet needs $N$ "ticks" of $L/R$ to reach the destination. Every next packet arrives **one tick later**, because they travel in single file.

| Time | Packet 1 | Packet 2 | Packet 3 |
|---|---|---|---|
| $1\cdot L/R$ | at the router | — | — |
| $2\cdot L/R$ | **at destination** | at the router | — |
| $3\cdot L/R$ | | **at destination** | at the router |
| $4\cdot L/R$ | | | **at destination** |

For $N = 2$, $P = 3$: $(2 + 3 - 1) \cdot L/R = 4\,L/R$ — matches the table.
:::

## Queueing and packet loss

The lecture's example: hosts A and B send to a router over 100 Mb/s links, but the router's link towards E is only **1.5 Mb/s**.

If for some period the **arrival rate** (bit/s) to a link exceeds the link's **transmission rate**:

- packets **queue**, waiting to be transmitted on the output link,
- packets can be **dropped** (lost) if the router's memory (buffer) fills up.

:::own Analogy: a supermarket checkout
A cashier serves 1 customer per minute. If for 10 minutes 3 customers arrive every minute, a queue forms. And when the queue doesn't fit in the shop, new customers leave — that is "packet loss".
:::

How long waiting in the queue takes is covered in [W5](w5-performance.html).

## Two key network-core functions: forwarding and routing

| | **Forwarding** | **Routing** |
|---|---|---|
| Scope | **local** action in one router | **global** action for the whole network |
| What it does | moves an arriving packet from the input link to the right **output link** | determines **source–destination paths** |
| How | looks into the **local forwarding table** | **routing algorithms** fill those tables |

The forwarding table from the slide (header value → output link):

| Header value | Output link |
|---|---|
| 0100 | 3 |
| 0101 | 2 |
| 0111 | 2 |
| 1001 | 1 |

The router reads the **destination address** in the arriving packet's header, finds it in the table and sends the packet out of the matching link.

:::own Analogy and the link to the project
- **Routing** = planning a route in your satnav before you leave. **Forwarding** = turning at each crossroads according to that route.
- In the **project** your proxy also has a "routing table": **key name → where to look for it** (a server or another proxy). Handling `GET VALUE x` is exactly forwarding — you look into the table and pass the request on. See [Project](project.html).
:::

## The alternative: circuit switching

In **circuit switching** end-to-end resources are **reserved** for the whole "call" between source and destination:

- on the slide every link has 4 circuits; the call gets e.g. the 2nd circuit on the top link and the 1st on the right link,
- resources are **dedicated**, no sharing → **guaranteed**, circuit-like performance,
- if the call sends nothing, its circuit segment is **idle** (nobody else can use it),
- this is how **traditional telephone networks** worked.

### FDM and TDM — how to split a link into circuits

- **FDM** (*Frequency Division Multiplexing*): the frequency spectrum is divided into **narrow bands**; each call gets **its own band** and can transmit at the max rate of that narrow band — but **all the time**.
- **TDM** (*Time Division Multiplexing*): time is divided into **slots**; each call gets **periodic slots** and transmits at the max rate of the **wider** band — but **only during its slots**.

:::own Exercise type: "how long does sending a file over a circuit take?"
A classic textbook problem: a 640,000-bit file, a TDM link of 1.536 Mb/s with **24** slots, circuit setup takes 500 ms.

1. Rate of one circuit: $1.536\ \text{Mb/s} / 24 = 64\ \text{kb/s}$.
2. Transmission time: $640\,000 / 64\,000 = 10$ s.
3. Plus circuit setup: $10 + 0.5 = 10.5$ s.
:::

## Packets vs circuits — how many users fit?

The lecture's example:

- a **1 Gb/s** link,
- each user sends **100 Mb/s** when **active**,
- a user is active only **10%** of the time.

**Circuit switching:** everyone needs a reserved 100 Mb/s, so $1\ \text{Gb/s} / 100\ \text{Mb/s} = 10$ users fit.

**Packet switching:** with **35** users the probability that **more than 10** are active at the same time (i.e. the link "clogs") is **less than 0.0004**.

### Where does 0.0004 come from? [[own]]

The lecture asks this question. Answer: it is the **binomial distribution**. Each of the $N = 35$ users is active independently with probability $p = 0.1$. The number of active users $X$ follows $B(35,\ 0.1)$:

$$
P(X = k) = \binom{35}{k}\, 0.1^{k}\, 0.9^{35-k}
$$

Trouble starts when **more than 10** are active:

$$
P(X > 10) = 1 - \sum_{k=0}^{10} \binom{35}{k}\, 0.1^{k}\, 0.9^{35-k} \approx 0.000424
$$

:::steps
1. Compute how many users the link carries at once: $k = \lfloor C / r \rfloor = 10$.
2. Write down the distribution: $N = 35$ trials, success = "active" with $p = 0.1$.
3. Compute $P(X \le 10)$ as a sum of 11 terms (k = 0…10) — in practice with a calculator or a program.
4. Subtract from 1: $P(X > 10) \approx 0.000424$, i.e. about 0.0004 — the slide simply rounds it.
:::

What if there are more than 35 users (the lecture's next question)? The overload probability grows quickly: 40 → about 0.0015; 50 → about 0.009; 70 → about 0.087; 100 → about 0.42. Try it yourself:

:::calc binom
:::

### Is packet switching always the winner?

From the lecture:

- **Pros** of packet switching: great for **bursty** data — sometimes lots of data, sometimes nothing; **resource sharing**; simpler, **no call setup**.
- **Cons**: **excessive congestion** is possible — delay and loss due to buffer overflow. Protocols are needed for **reliable data transfer** and **congestion control** (that's what TCP does).
- How to get circuit-like behaviour? **Bandwidth guarantees** — traditionally used for audio/video applications.

:::own Human analogies (a question from the lecture)
- **Reserved resources** (circuits): a reserved table in a restaurant — it waits for you even if you are late, but nobody else can sit there.
- **On-demand allocation** (packets): a queue at a fast-food bar — you reserve nothing, it's usually fast, but at rush hour you wait.
:::

## Check yourself [[own]]

:::check How long does it take to send 1 packet of 8000 bits over 3 links of 2 Mb/s (store-and-forward, no propagation)?
$N \cdot L/R = 3 \cdot 8000 / 2\,000\,000 = 0.012$ s $= 12$ ms.
:::

:::check And how long for 4 such packets over the same path?
$(N + P - 1)\cdot L/R = (3 + 4 - 1)\cdot 4$ ms $= 24$ ms.
:::

:::check What is the difference between forwarding and routing?
Forwarding is the router's **local** decision: which output link to use (according to the table). Routing is the **global** computation of paths that fills those tables.
:::

:::check Why can circuit switching waste resources?
Because a reserved circuit stays **idle** when the call sends nothing — and nobody else can use it meanwhile.
:::

:::cheatsheet
### Formulas
- One link: $d_{trans} = L/R$.
- Store-and-forward, 1 packet, $N$ links: $N \cdot L/R$.
- $P$ packets, $N$ links: $(N + P - 1)\cdot L/R$.
- TDM: circuit rate $= R / \text{number of slots}$.
- Circuits: number of users $= \lfloor C / r \rfloor$.
- Packets: $P(X > k) = 1 - \sum_{i=0}^{k}\binom{N}{i}p^i(1-p)^{N-i}$.

### Facts
- Router: the whole packet must arrive before it goes on.
- Arrivals > output rate → queue; full buffer → loss.
- Forwarding = local (table), routing = global (algorithms).
- Circuits: reservation, guarantee, waste; FDM (bands), TDM (slots).
- Lecture example: 1 Gb/s, 100 Mb/s, 10% → circuits 10 users, packets 35 users with P ≈ 0.0004.

### "File over a TDM circuit" step by step
1. Circuit rate = R / number of slots.
2. Time = file size [bit] / circuit rate.
3. Add the circuit setup time.
:::
