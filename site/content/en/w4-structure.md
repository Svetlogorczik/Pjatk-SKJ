---
title: Internet structure — "a network of networks"
short: Internet structure
description: How millions of small Internet providers join into one network — ISPs, IXPs, peering, regional networks, tier-1 and content provider networks.
kind: mixed
source: Lecture 1 — Chapter 1, slides 1-34…1-43 (Kurose & Ross)
---

## In short

- Hosts connect to the Internet through **access ISPs**. Those must in turn be interconnected so that **any two hosts** can talk to each other.
- Connecting "everyone to everyone" doesn't scale — it needs $O(N^2)$ links.
- That's why a **hierarchy** emerged: access ISPs → **regional ISPs** → global **tier-1** ISPs; plus **IXPs** (exchange points) and **content provider** networks (Google, Microsoft, Akamai).
- The shape of this network was decided mostly by **economics** and **national policies**.

## The problem: millions of access ISPs

Hosts connect through **access Internet Service Providers**: residential, company, university, commercial. But access ISPs must also be **connected to each other** — otherwise a customer of one ISP could not send anything to a customer of another.

The resulting "network of networks" is very complex. The lecture builds it **step by step**, starting with the question: **how do we connect millions of access ISPs?**

### Idea 1: everyone to everyone — doesn't scale

Connecting each ISP directly to every other ISP needs $O(N^2)$ connections.

:::own How many links exactly?
For $N$ networks the number of pairs is:

$$
\binom{N}{2} = \frac{N(N-1)}{2}
$$

- 10 networks → 45 links,
- 1000 networks → 499,500 links,
- 1,000,000 networks → about $5 \cdot 10^{11}$ links. Completely unrealistic.
:::

### Idea 2: one global transit ISP

Every access ISP connects to **one global transit ISP**. Between the **customer** (access ISP) and the **provider** (global ISP) there is an **economic agreement** — the customer pays for transit.

### Idea 3: competition and exchange points (IXP)

If one global ISP is a viable business, there will be **competitors**: ISP A, ISP B, ISP C… Their customers want to talk to each other, so these global ISPs must interconnect too:

- **IXP** (*Internet exchange point*) — a place where many networks connect at once,
- **peering links** — a direct connection between two networks "as partners".

### Idea 4: regional networks

**Regional ISPs** may arise that collect traffic from local access ISPs and connect it to bigger ISPs.

### Idea 5: content provider networks

**Content providers** (e.g. Google, Microsoft, Akamai) build **their own networks** to bring services and content **close to end users**.

## The final picture

At the very top ("in the center") there is a **small number of well-connected large networks**:

- commercial **tier-1 ISPs** (e.g. Level 3, Sprint, AT&T, NTT) — national and international coverage,
- **content provider networks** (e.g. Google, Facebook) — a private network that connects their data centers to the Internet, often **bypassing** tier-1 and regional ISPs.

```text
          [Tier-1 ISP] ===== [Tier-1 ISP] ===== [Google]
              |    \            /     |            |
            [IXP]   \__[IXP]__/     [IXP]          |
              |                       |            |
       [Regional ISP]          [Regional ISP]      |
        /     |     \            /     \           |
  [access] [access] [access] [access] [access] ----+
     ISP      ISP      ISP      ISP      ISP
```

The lecture also shows the tier-1 network map of **Sprint (2019)**: **POPs** (*points of presence*) — places where the network has equipment and where customers connect — plus links to peering networks and to customer networks.

:::own Who pays whom? (simplified)
- **Customer → provider**: a smaller network pays a bigger one for **transit** (access to "the whole Internet").
- **Peering**: two networks of similar size exchange traffic between their customers — often **settlement-free**, because both benefit.
- **IXP**: a shared "hall" where many networks can connect at once instead of laying separate cables to each other.

Example IXPs: in Poland e.g. PLIX and EPIX; in Europe DE-CIX (Frankfurt) and AMS-IX (Amsterdam) are very large.
:::

:::own Why does Google have its own network?
So that a YouTube video does not travel across half the world. Own network + servers close to users = **lower delay**, less congestion and lower transit costs. You can see the path of packets through different networks with `traceroute` / `tracert` — see [W5](w5-performance.html).
:::

## Check yourself [[own]]

:::check Why don't we connect every ISP to every other ISP?
Because the number of links grows like $N(N-1)/2$, i.e. $O(N^2)$ — for millions of networks that's billions of links.
:::

:::check What is an IXP?
An Internet exchange point: a place where many networks (ISPs, content providers) connect and exchange traffic.
:::

:::check Give two tier-1 networks and two content provider networks from the lecture.
Tier-1: Level 3, Sprint, AT&T, NTT. Content providers: Google, Microsoft, Akamai, Facebook.
:::

:::check What is a POP?
A *point of presence* — a place in a provider's network (a group of routers) where customers can connect.
:::

:::cheatsheet
### Hierarchy (bottom-up)
1. **Access ISP** — home, company, university access.
2. **Regional ISP** — collects traffic from local access ISPs.
3. **Tier-1 ISP** — global (Level 3, Sprint, AT&T, NTT).
4. **Content provider networks** — Google, Microsoft, Akamai, Facebook: own networks, bypass tier-1.

### Keywords
- **Customer–provider**: economic agreement, the customer pays for transit.
- **IXP**: exchange point of many networks.
- **Peering**: direct connection of two networks.
- **POP**: point of presence of a provider network.
- The structure comes from **economics and policy**, not from one master plan.

### Counting "everyone-to-everyone" links
- $N(N-1)/2$, e.g. 1000 networks → 499,500 links → $O(N^2)$, doesn't scale.
:::
