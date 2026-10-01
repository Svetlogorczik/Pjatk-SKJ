---
title: TCP under the microscope — sequence numbers, RTT, window and slow start in Wireshark
short: TCP in Wireshark
description: We analyse a big file transfer over TCP — the 3-way handshake, seq/ack numbers, EstimatedRTT, the receiver window, retransmissions, throughput and congestion control — with step-by-step calculations.
kind: lab
source: logic of the "Wireshark Lab: TCP" (Kurose & Ross) used in class — questions and examples rewritten
---

## In short

- A TCP connection starts in **3 steps**: `SYN` → `SYN+ACK` → `ACK`.
- TCP numbers **bytes**, not packets: `seq` = number of the first byte in the segment, `ack` = number of the **next expected** byte.
- **SampleRTT** = time from sending a segment until its ACK arrives. **EstimatedRTT** is a smoothed average: $(1-\alpha)\cdot\text{EstimatedRTT} + \alpha\cdot\text{SampleRTT}$, $\alpha = 0.125$.
- The **receiver window** (*rwnd*) tells the sender how many more bytes fit in the receiver's buffer.
- **Throughput** = transferred bytes / time. **Slow start**: the window grows ×2 every RTT ("fleets" of packets).

:::origin
In class you do the Wireshark TCP lab from the Kurose & Ross textbook: you upload a big text file (about 150 KB) with HTTP POST to the lab server, capture the traffic and answer questions about addresses and ports, SYN/SYN-ACK sequence numbers, the segment with the POST, RTT and EstimatedRTT, segment lengths, the receiver window, retransmissions, acknowledgements, throughput and the congestion-control phases (the Stevens graph). Below you get **our own questions and numeric examples**, and every question has a hint where to look.
:::

## Preparation (from the lab instructions)

:::steps
1. Download the text file from the lab (link in the instructions) and save it on disk.
2. Open the lab page with the file upload form, choose the file, **don't send it yet**.
3. Start capturing in Wireshark.
4. Click the upload button — the file goes to the server with **POST**.
5. Stop capturing and type the filter **`tcp`**.
:::

:::tip No network?
Just like for HTTP — the lab has a ready-made capture file (*tcp-wireshark-trace1-1* in the `wireshark-traces-9e.zip` archive).
:::

## The TCP header in a nutshell [[own]]

| Field | What it's for |
|---|---|
| source / destination port | which processes are talking (e.g. 54321 → 80) |
| **sequence number** | the number of the first data byte in this segment |
| **acknowledgment number** | "I received everything up to this number — send from here" |
| flags | **SYN** (setup), **ACK** (ack field valid), **FIN** (close), **RST** (abort), PSH |
| **window** | free space in the receiver's buffer (possibly with a multiplier) |
| options | among others **MSS**, **window scale**, **SACK permitted**, timestamps |

@svg(handshake, caption=The 3-way handshake, two data segments (1460 B each) and one cumulative acknowledgement.)

## seq and ack numbers step by step [[own]]

Example: the client picks the initial number **2000**, the server **9000**; data segments carry **1448 B** each.

| # | Who → who | Flags | seq | ack | data | Explanation |
|---|---|---|---|---|---|---|
| 1 | C → S | SYN | 2000 | — | 0 | SYN "uses up" 1 number |
| 2 | S → C | SYN, ACK | 9000 | **2001** | 0 | ack = client's seq + 1 |
| 3 | C → S | ACK | 2001 | **9001** | 0 | ack = server's seq + 1 |
| 4 | C → S | ACK, PSH | 2001 | 9001 | 1448 | first data (e.g. start of the POST) |
| 5 | C → S | ACK | **3449** | 9001 | 1448 | 2001 + 1448 = 3449 |
| 6 | S → C | ACK | 9001 | **4897** | 0 | one ACK acknowledges **both** segments |

:::note Rules to remember
- next `seq` = previous `seq` + number of data bytes,
- `ack` = the number of the **next** byte the receiver expects,
- an ACK is **cumulative** — it acknowledges everything "up to here",
- Wireshark shows **relative** numbers by default (SYN has seq = 0). You'll see the raw ones as *Sequence Number (raw)* or after disabling *Relative sequence numbers* (Edit → Preferences → Protocols → TCP).
:::

## RTT and EstimatedRTT

**SampleRTT** of a segment = (arrival time of the ACK that acknowledges it) − (send time of the segment). A single sample "jumps around", so TCP computes a weighted average:

$$
\text{EstimatedRTT} = (1-\alpha)\cdot\text{EstimatedRTT} + \alpha\cdot\text{SampleRTT}, \qquad \alpha = 0.125
$$

In the lab you assume the **first** EstimatedRTT = the first measured RTT.

:::example EstimatedRTT step by step [[own]]
SampleRTT values (ms): 28, 32, 25, 40, 30.

1. $\text{Est}_1 = 28$
2. $\text{Est}_2 = 0.875 \cdot 28 + 0.125 \cdot 32 = 24.5 + 4 = 28.5$
3. $\text{Est}_3 = 0.875 \cdot 28.5 + 0.125 \cdot 25 = 24.9375 + 3.125 = 28.0625$
4. $\text{Est}_4 = 0.875 \cdot 28.0625 + 0.125 \cdot 40 \approx 29.555$
5. $\text{Est}_5 = 0.875 \cdot 29.555 + 0.125 \cdot 30 \approx 29.610$

One big sample (40 ms) raises the average only a little — that's the point.
:::

:::own Timeout (from the textbook, not from the lab)
TCP also tracks the variability $\text{DevRTT} = (1-\beta)\cdot\text{DevRTT} + \beta\cdot|\text{SampleRTT} - \text{EstimatedRTT}|$, $\beta = 0.25$, and sets
$\text{TimeoutInterval} = \text{EstimatedRTT} + 4\cdot\text{DevRTT}$. The calculator below computes everything at once (start: DevRTT = half of the first sample, as in RFC 6298).
:::

:::calc rtt
:::

The RTT graph in Wireshark: select a client-to-server segment → **Statistics → TCP Stream Graphs → Round Trip Time**.

## The receiver window and scaling

The **window** field in a segment from the receiver tells the sender how many bytes it may still send without waiting for an ACK (flow control). If **window scale** was negotiated in the SYN, the real value is:

$$
\text{window [B]} = \text{Window Size Value} \cdot \text{multiplier}
$$

Example: `Window Size Value: 501`, multiplier 128 → $501 \cdot 128 = 64\,128$ B. Wireshark shows it as *Calculated window size*. If the window never drops to (almost) zero, the receiver is **not throttling** the sender.

## Retransmissions and acknowledgements

- A **retransmission** = a segment with **the same** `seq` sent again. Wireshark marks it `[TCP Retransmission]`; filter: `tcp.analysis.retransmission`.
- **Delayed ACK**: the receiver often acknowledges **every other** segment (ack then grows by ~2 × MSS). Subtract consecutive `ack` values to see how many bytes one ACK acknowledges.

## Connection throughput

$$
\text{throughput} = \frac{\text{bytes transferred}}{\text{time}}
$$

:::example Computing the throughput [[own]]
The first data segment was sent at $t = 0.0245$ s, the last ACK arrived at $t = 0.2009$ s, $153\,000$ B were transferred.

1. Time: $0.2009 - 0.0245 = 0.1764$ s.
2. Throughput: $153\,000 / 0.1764 \approx 867\,347$ B/s $\approx 0.87$ MB/s.
3. In bits: $\cdot 8 \approx 6.94$ Mb/s.

The easiest way to get the number of bytes: `ack` of the last acknowledgement − `seq` of the first data segment.
:::

## Congestion control: slow start and congestion avoidance

- **Slow start**: at the beginning the congestion window (*cwnd*) **doubles every RTT** — the sender sends a "fleet" of 2, 4, 8… segments and waits for ACKs.
- **Congestion avoidance**: after passing a threshold the window grows **linearly** — by 1 MSS per RTT.
- On the **Statistics → TCP Stream Graphs → Time-Sequence (Stevens)** graph each dot is a sent segment; dots stacked vertically are a fleet sent back to back.
- The gap between fleets ≈ **RTT** (the sender waits for acknowledgements).

:::own How to recognise the phase from the graph?
Count the segments in consecutive fleets. 2 → 4 → 8 → 16 is slow start. The same number or +1 per RTT is congestion avoidance. If the fleets **stay the same size**, the limit may be the receiver window or the application — in a small transfer (150 KB) TCP often **doesn't even leave** slow start.
:::

## Questions for a capture

::::task Questions T1–T10
1. What IP address and port does the client have, and the server?
2. What **raw** sequence number does the SYN segment have? How do you know it's a SYN? Does the client announce **SACK** support?
3. What sequence number does the SYN-ACK have, and what is its ack value? Where did the server get it from?
4. Which segment contains the start of the POST, and how many data bytes does it carry? Did the whole file fit into one segment?
5. Measure SampleRTT for the first two data segments and compute EstimatedRTT after the second.
6. What is the length (headers + data) of the first four data segments?
7. What is the smallest window the server advertises at the beginning? Did the window throttle the sender?
8. Are there retransmissions? How did you check?
9. How many bytes does one ACK typically acknowledge? Do you see an ACK for every other segment?
10. What was the connection's throughput? How did you compute it?

:::hint Where to look
1. The IP + TCP layer of any segment from the client (the HTTP server has port **80**).
2. The *Sequence Number (raw)* field; flag **SYN = 1**; look for **SACK permitted** in the TCP options.
3. ack in SYN-ACK = seq from SYN + 1.
4. Search for the text "POST" in the bytes pane; data length = *TCP Segment Len*; the file is > 150 KB, so it won't fit (MSS ≈ 1460 B).
5. The Time column: (time of the ACK whose `ack` ≥ end of the segment) − (segment send time); then the formula with α = 0.125 — or the calculator above.
6. *Frame length* or *TCP Segment Len* + headers; in the lab's capture segments are < 1480 B, because IP is limited to 1500 B and TCP/IP headers take at least 40 B.
7. *Window Size Value* × multiplier (or *Calculated window size*) in segments from the server.
8. The filter `tcp.analysis.retransmission`, or look for a repeated `seq`.
9. Differences between consecutive `ack` values from the server.
10. Bytes / time — see the example above.
:::
::::

## Calculation exercises

::::task 1. EstimatedRTT
SampleRTT: 40, 36, 52, 44 ms, $\alpha = 0.125$, start = the first sample. Compute EstimatedRTT after each sample.

:::answer
40 → $0.875 \cdot 40 + 0.125 \cdot 36 = 39.5$ → $0.875 \cdot 39.5 + 0.125 \cdot 52 = 41.0625$ → $0.875 \cdot 41.0625 + 0.125 \cdot 44 \approx 41.43$ ms. Check it with the calculator.
:::
::::

::::task 2. seq/ack
The client's ISN = 500. It sends a SYN, then 3 segments of 1000 B. Give the raw `seq` of each data segment and the `ack` with which the server acknowledges all three.

:::answer
SYN: seq = 500. Data: 501, 1501, 2501. Cumulative ACK: **3501**.
:::
::::

::::task 3. Which phase?
Fleets contain 2, 4, 8, 16, 17, 18 segments. Where does slow start end?

:::answer
Up to 16 the window doubles (slow start), then it grows by 1 per RTT — congestion avoidance (the threshold was reached at about 16 segments).
:::
::::

:::cheatsheet
### Handshake
SYN (seq = x) → SYN+ACK (seq = y, ack = x+1) → ACK (ack = y+1).

### seq / ack
- next seq = seq + data length;
- ack = next expected byte; ACK is cumulative;
- SYN and FIN each use 1 number;
- Wireshark: relative numbers (raw in a separate field).

### Formulas
- $\text{Est} = 0.875\cdot\text{Est} + 0.125\cdot\text{Sample}$ (start: Est = 1st sample)
- $\text{Dev} = 0.75\cdot\text{Dev} + 0.25\cdot|\text{Sample} - \text{Est}|$; $\text{Timeout} = \text{Est} + 4\cdot\text{Dev}$
- window = Window Size Value × multiplier
- throughput = bytes / time (×8 → bit/s)

### Wireshark
- filter `tcp`, `tcp.flags.syn == 1`, `tcp.analysis.retransmission`, `tcp.stream eq 0`;
- Statistics → TCP Stream Graphs → Round Trip Time / Time-Sequence (Stevens);
- raw seq: Preferences → Protocols → TCP → untick *Relative sequence numbers*.

### Slow start vs congestion avoidance
- ×2 per RTT → slow start; +1 MSS per RTT → congestion avoidance;
- gap between fleets ≈ RTT.
:::
