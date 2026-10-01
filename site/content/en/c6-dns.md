---
title: DNS from the inside — a query built by hand, byte by byte
short: DNS by hand (UDP, bytes)
description: We build a DNS query from bytes in Java, send it over UDP to port 53 and decode the answer — header, flags, compressed names, TTL, addresses. Plus DNS analysis with nslookup and Wireshark.
kind: lab
source: logic of lab 6 (a DNS query as a byte array sent to 8.8.8.8 and analysis of the answer) and "Wireshark Lab: DNS" (Kurose & Ross) — rewritten tasks
---

## In short

- **DNS** translates names (e.g. `pjwstk.edu.pl`) into IP addresses. Queries usually go over **UDP to port 53**.
- A DNS message = a **12-byte header** + a **question** section + **answer**, **authority** and **additional** sections.
- A domain name is written as **labels**: `07 example 03 com 00`.
- In answers, names are often **compressed**: two bytes `C0 0C` = "the name at offset 12".
- In Java `byte` is signed (−128…127) — always do **`& 0xFF`**.

:::origin
In lab 6 the DNS query was written in the program as a ready-made byte array (for one specific domain), sent over UDP to a public DNS server on port 53, and the answer had to be taken apart into fields: ID, flags, counters, name, type, class, TTL and IP address. Plus the Wireshark DNS lab (nslookup, DNS cache, tracing queries). Our version **builds** the query itself for any domain and type, supports name compression and several record types.
:::

## What does DNS do? [[own]]

Computers know IP addresses, people prefer names. When you type `www.example.com`:

:::steps
1. Your computer checks **its own DNS cache** — maybe it already knows the answer.
2. If not, it asks the **local DNS server** (e.g. your provider's or `8.8.8.8`).
3. If needed, the local server asks **root** → **TLD** (`.com`) → the **authoritative** server for `example.com`, and remembers the result for **TTL** seconds.
4. Your computer gets the answer — from the client's point of view it's one question and one answer.
:::

Record types you will meet:

| Type | Code | Contains |
|---|---|---|
| **A** | 1 | an IPv4 address |
| **NS** | 2 | the name of an authoritative server for the domain |
| **CNAME** | 5 | "this is an alias, the real name is…" |
| **SOA** | 6 | zone information (often in a "no such name" answer) |
| **MX** | 15 | a mail server (with a preference) |
| **AAAA** | 28 | an IPv6 address |

## DNS message format

### Header — 12 bytes

| Bytes | Field | Meaning |
|---|---|---|
| 0–1 | **ID** | query number; the answer has **the same** ID |
| 2–3 | **Flags** | flags (below) |
| 4–5 | **QDCOUNT** | number of questions |
| 6–7 | **ANCOUNT** | number of answer records |
| 8–9 | **NSCOUNT** | number of authority records |
| 10–11 | **ARCOUNT** | number of additional records |

2-byte numbers are **big-endian** (high byte first): `00 02` = 2.

### Flags (16 bits)

| Bit(s) | Field | Meaning |
|---|---|---|
| 15 | **QR** | 0 = query, 1 = response |
| 14–11 | **OPCODE** | 0 = standard query |
| 10 | **AA** | authoritative answer |
| 9 | **TC** | truncated answer |
| 8 | **RD** | recursion desired |
| 7 | **RA** | recursion available |
| 6–4 | **Z** | reserved (0) |
| 3–0 | **RCODE** | 0 = OK, 3 = **NXDOMAIN** (no such name) |

:::example Decoding the flags step by step [[own]]
The query has flags `0x0100`, the answer `0x8180`.

1. `0x0100` = `0000 0001 0000 0000` → only bit 8 = 1 → **RD = 1** ("keep searching for me").
2. `0x8180` = `1000 0001 1000 0000` → bit 15 (**QR = 1**, a response), bit 8 (**RD = 1**, copied from the question), bit 7 (**RA = 1**, the server supports recursion), RCODE = 0 (OK).
3. In Java: `int qr = flags >> 15 & 1; int rcode = flags & 0xF;`
:::

### The question

- **QNAME** — the name as a sequence of labels: a length byte + letters, a `00` byte at the end.
- **QTYPE** — 2 bytes, e.g. `00 01` = A.
- **QCLASS** — 2 bytes, `00 01` = IN (Internet).

`www.example.com` → `03 77 77 77 07 65 78 61 6D 70 6C 65 03 63 6F 6D 00` (3 letters `www`, 7 letters `example`, 3 letters `com`, end).

### A resource record (answer)

| Field | Size | Notes |
|---|---|---|
| NAME | variable | often a **pointer** `C0 0C` |
| TYPE | 2 B | e.g. 1 = A |
| CLASS | 2 B | 1 = IN |
| TTL | **4 B** | how many seconds it may be cached |
| RDLENGTH | 2 B | data length |
| RDATA | RDLENGTH | e.g. 4 bytes of an IPv4 address |

:::note Name compression (pointer)
If a length byte starts with the bits `11` (i.e. `>= 0xC0`), it is **not** a label but a **pointer**: two bytes whose 14 bits are an offset in the message. `C0 0C` → offset `0x0C` = 12 — that's where the question's name starts (right after the 12-byte header). This way the name isn't repeated in every answer.

Compute the offset as: `((b1 & 0x3F) << 8) | (b2 & 0xFF)`.
:::

## Byte-by-byte example (real data from testing this page)

A query for `example.com`, type A (29 bytes):

```text
70 C5 01 00 00 01 00 00 00 00 00 00 07 65 78 61
6D 70 6C 65 03 63 6F 6D 00 00 01 00 01
```

| Bytes | Value | Meaning |
|---|---|---|
| `70 C5` | ID = 0x70C5 | random query number |
| `01 00` | flags 0x0100 | RD = 1 |
| `00 01` | QDCOUNT = 1 | one question |
| `00 00` ×3 | 0, 0, 0 | no answers or extra records |
| `07 65 78 61 6D 70 6C 65` | 7 + "example" | first label |
| `03 63 6F 6D` | 3 + "com" | second label |
| `00` | — | end of name |
| `00 01` | QTYPE = 1 | A |
| `00 01` | QCLASS = 1 | IN |

The answer from `8.8.8.8` (61 bytes):

```text
70 C5 81 80 00 01 00 02 00 00 00 00 07 65 78 61
6D 70 6C 65 03 63 6F 6D 00 00 01 00 01 C0 0C 00
01 00 01 00 00 01 2C 00 04 AC 42 93 F3 C0 0C 00
01 00 01 00 00 01 2C 00 04 68 14 17 9A
```

| Bytes | Meaning |
|---|---|
| `70 C5` | the same ID — it's the answer to **our** question |
| `81 80` | QR = 1, RD = 1, RA = 1, RCODE = 0 |
| `00 01 00 02 00 00 00 00` | 1 question, **2 answers** |
| `07 65 … 00 00 01 00 01` | the repeated question |
| `C0 0C` | NAME = pointer to offset 12 → `example.com` |
| `00 01 00 01` | TYPE = A, CLASS = IN |
| `00 00 01 2C` | TTL = 0x12C = **300 s** |
| `00 04` | RDLENGTH = 4 |
| `AC 42 93 F3` | 172.66.147.243 (0xAC = 172, 0x42 = 66, 0x93 = 147, 0xF3 = 243) |
| `C0 0C … 68 14 17 9A` | the second answer: 104.20.23.154 |

## The DnsLookup program

@code(c6/DnsLookup.java)

The key parts:

- `DataOutputStream.writeShort` writes 2 bytes in **big-endian** order — exactly what DNS wants.
- `u16` / `u32` read unsigned numbers: `(m[i] & 0xFF) << 8 | (m[i+1] & 0xFF)`.
- `readName` handles labels **and** pointers; after a pointer the name ends, so the position only moves by 2 bytes.
- A 512-byte buffer — the classic DNS-over-UDP limit (larger answers have TC = 1 set).

:::danger `byte` in Java is signed [[own]]
The byte `0xF3` in Java is **−13**, not 243. Without `& 0xFF` the IP address prints as `-84.66.-109.-13`. Always: `int value = b & 0xFF;`.
:::

### Running it

```bash
javac DnsLookup.java
java DnsLookup example.com
java DnsLookup pjwstk.edu.pl MX
java DnsLookup www.wikipedia.org A 1.1.1.1
```

Fragments of real results:

```text
Answer: pjwstk.edu.pl  type=MX  TTL=3600 s  data=pref 1 ASPMX.L.GOOGLE.COM
Answer: www.wikipedia.org  type=CNAME  TTL=20707 s  data=dyna.wikimedia.org
Answer: dyna.wikimedia.org  type=A  TTL=180 s  data=208.80.153.224
Flags: 0x8183  QR=1 OPCODE=0 AA=0 TC=0 RD=1 RA=1 RCODE=3
RCODE 3 = NXDOMAIN (no such name)
```

Notice the **CNAME**: `www.wikipedia.org` is an alias — the server immediately added the A record for the real name.

## DNS in nslookup and Wireshark

### nslookup — basics (from the lab)

```bash
nslookup www.example.com            # A record (default)
nslookup -type=NS example.com       # authoritative servers of the domain
nslookup -type=MX pjwstk.edu.pl     # mail servers
nslookup 8.8.8.8                    # reverse lookup: IP -> name
nslookup www.example.com 1.1.1.1    # ask a specific DNS server
```

- The first lines of the output show the **DNS server that answered** (name and IP).
- **"Non-authoritative answer"** = an answer from some server's **cache**, not from the authoritative server.

### The DNS cache on your computer (from the lab)

Your computer remembers recent answers. To see queries in Wireshark, clear the cache first:

| System | Command |
|---|---|
| Windows | `ipconfig /flushdns` |
| macOS | `sudo killall -HUP mDNSResponder` |
| Linux (systemd) | `sudo resolvectl flush-caches` |

### Questions for a capture

Clear the DNS and browser caches, start Wireshark with the filter `dns` (or `ip.addr == <your IP>`), open any page via `http://`, then run a few `nslookup` commands.

::::task Questions D1–D8
1. Does the DNS query go over UDP or TCP?
2. What is the **destination port** of the query and the **source port** of the answer?
3. To which IP address was the query sent? Is it your default local DNS server?
4. How many questions and answers are in the **query**? And in the **answer**?
5. What **type** of query does `nslookup www...` send? And `nslookup -type=NS ...`?
6. What do NS answers contain? Are there additional records, and what's in them?
7. The page references an image on **the same** server. Is there a new DNS query before the second GET? Why?
8. Match a query–answer pair by **ID**. Do they match?

:::answer Model answers (exact numbers depend on your capture)
1. UDP.
2. Query destination port **53**; answer source port **53** (the client's port is random).
3. The local DNS server's address — compare with `ipconfig /all` (Windows) or `resolvectl status` / `/etc/resolv.conf` (Linux).
4. Query: 1 question, 0 answers. Answer: 1 question (repeated) and ≥ 1 answer.
5. `nslookup www...` → type **A** (often also a separate **AAAA**). `-type=NS` → type **NS**.
6. Names of the authoritative servers; the *additional* section often contains their **IP addresses** (A/AAAA records) — "for free", although we didn't ask for them.
7. No — the address is already in the computer's **DNS cache**, so the second GET goes out immediately.
8. Yes — the answer always has the same ID as the query; that's how the client matches them.
:::
::::

## Tasks

::::task 1. A different DNS server
Ask `8.8.8.8`, `1.1.1.1` and your local server for the same name. Compare the TTLs. Why can they differ?

:::answer
The TTL in a cached answer is the **remaining** lifetime of the entry — every server stored the record at a different moment, so it "counts down" from a different point.
:::
::::

::::task 2. The TXT record
Add support for type **TXT** (code 16). RDATA is a string: a length byte + text (there can be several such strings).

:::answer
In `typeCode` add `case "TXT": return 16;`, in `typeName` `case 16: return "TXT";`, and in `rdata`: a loop `int p = start; while (p < start + len) { int l = m[p] & 0xFF; sb.append(new String(m, p + 1, l, StandardCharsets.US_ASCII)); p += 1 + l; }`.
:::
::::

::::task 3. Take apart bytes from Wireshark
In Wireshark right-click the DNS section of an answer → *Copy → …as Hex Stream*. Paste it into the program as a byte array and run it through `decode(...)`. Does the result match Wireshark's tree?
::::

:::cheatsheet
### Header (12 B)
ID(2) · FLAGS(2) · QDCOUNT(2) · ANCOUNT(2) · NSCOUNT(2) · ARCOUNT(2) — big-endian.

### Flags
QR(15) · OPCODE(14–11) · AA(10) · TC(9) · RD(8) · RA(7) · Z(6–4) · RCODE(3–0).
- query: `0x0100` (RD=1); typical answer: `0x8180`; NXDOMAIN: RCODE = 3.

### Building a query step by step
1. `writeShort(id)`, `writeShort(0x0100)`, `writeShort(1)`, 3× `writeShort(0)`.
2. For every label: `writeByte(len)` + the letters' bytes; at the end `writeByte(0)`.
3. `writeShort(QTYPE)` (A=1, NS=2, CNAME=5, MX=15, AAAA=28), `writeShort(1)` (IN).
4. Send over UDP to **port 53**, 512 B answer buffer, timeout.

### Parsing an answer step by step
1. Check the **ID**, then the flags and RCODE.
2. Read the counters.
3. Skip the question: name + 4 bytes.
4. For each record: NAME (watch for `C0 xx`), TYPE, CLASS, **TTL (4 B)**, RDLENGTH, RDATA.
5. A → 4 bytes `& 0xFF` joined with dots.

### Traps
- signed `byte` → `& 0xFF`.
- Pointer: `b >= 0xC0`, offset = `((b1 & 0x3F) << 8) | b2`; the name ends after a pointer.

### nslookup / Wireshark
- `nslookup name`, `-type=NS`, `-type=MX`, `nslookup IP` (reverse).
- "Non-authoritative" = from a cache.
- Filter `dns`, `udp.port == 53`; flush: `ipconfig /flushdns`.
:::
