---
title: Start here — how to use this site and set up your computer
short: Start & setup
description: How the site is organised and how to learn from it most effectively, plus installing the JDK and Wireshark, running programs from a terminal, several windows at once and Java 8 compatibility.
kind: own
source: original guide
---

## How to read this site

@legend

- **Lectures (W1–W8)** — all lecture material, explained in plain language, with examples and formulas.
- **Labs (C1–C7)** — class tasks **rewritten** so that the logic is the same but the wording and code are different (you don't copy previous years' work).
- **Practical test** and **Project** — what you need to know and how to prepare.
- The **📝 Cheat sheet** button in every topic opens a short step-by-step summary. All of them together (for printing) are on the [All cheat sheets](cheatsheets.html) page.
- The **✓ Mark as done** button stores your progress in your browser (the bar in the menu).
- Every code block has **Copy**, and files have **Download**.
- Top right: **language** (PL / EN / RU) and **theme** (light / dark).

## Suggested learning order

:::steps
1. Read [Course rules & dates](course.html) — so you know what's at stake.
2. Set up your computer (below).
3. Lectures [W1](w1-internet.html) → [W8](w8-history.html) — after each one click **Cheat sheet** and answer "Check yourself".
4. Labs [C1](c1-relay.html) → [C7](c7-tcp-wireshark.html) — **run** every program, don't just read.
5. [Practical test](exam.html) — training with the DrillServer.
6. [Project](project.html) — in stages, from 150 points up.
:::

## Installing Java (JDK)

You need the **JDK** (not just the JRE) — it contains the `javac` compiler.

:::steps
1. Install a JDK (e.g. Eclipse Temurin / OpenJDK). Version 8, 11, 17 or 21 — any will do, but write code compatible with **Java 8** (below).
2. Check in a terminal: `java -version` and `javac -version`.
3. If `javac` "is not recognized" — add the JDK's `bin` folder to `PATH` (Windows: *Environment Variables*).
:::

### Compiling and running from a terminal

```bash
cd folder-with-files
javac MiniWebServer.java          # creates MiniWebServer.class
java MiniWebServer 8080           # you run the CLASS (without .class), arguments separated by spaces
javac *.java                      # all files at once (that's how the instructor compiles the project!)
javac --release 8 *.java          # check that the code is Java 8 compatible
```

### Several programs at once

Network programs are almost always run **in several terminals** (server + client, several nodes).

| System | How to open another window |
|---|---|
| Windows | a new `cmd`/PowerShell window or in a `.bat` script: `start "name" cmd /k java Program` |
| Linux / macOS | a new terminal tab or in a script: `java Program &` (in the background) |
| IntelliJ | several run configurations; **Run → Edit Configurations → Program arguments** for arguments; the *Allow multiple instances* option |

## Java 8 compatibility

The project must be in **Java 8**. These constructs are **newer** — don't use them:

| Don't use | Since | Instead (Java 8) |
|---|---|---|
| `var x = ...` | 10 | an explicit type: `String x = ...` |
| `List.of(...)`, `Map.of(...)` | 9 | `Arrays.asList(...)`, `new HashMap<>()` + `put` |
| `"ab".repeat(3)` | 11 | a loop / `StringBuilder` |
| `str.isBlank()`, `strip()` | 11 | `trim().isEmpty()`, `trim()` |
| `list.getFirst()`, `getLast()` | 21 | `list.get(0)`, `list.get(list.size() - 1)` |
| text blocks `"""..."""` | 15 | normal strings with `\n` |
| `record`, arrow `switch` | 14–16 | a class, a classic `switch` with `break` |

`javac --release 8` reports such errors immediately, even if you have a newer JDK.

## Wireshark

:::steps
1. Download Wireshark from the project's website and install it. On **Windows** tick the **Npcap** installation (with **loopback** traffic support, so you can see `localhost`).
2. On **Linux** add your user to the `wireshark` group (or run it with `sudo`).
3. After starting, choose an interface: **Wi-Fi / Ethernet** for the Internet, **Loopback / lo** for programs on `localhost`.
4. Start (the blue fin) → do something on the network → Stop (the red square) → type a filter, e.g. `http`.
:::

## Useful console tools

| Tool | For | Example |
|---|---|---|
| `ping` | does a host answer, RTT | `ping pjwstk.edu.pl` |
| `tracert` / `traceroute` | the route through routers | `tracert pjwstk.edu.pl` |
| `nslookup` | DNS queries | `nslookup -type=MX pjwstk.edu.pl` |
| `curl -v` | an HTTP request with headers | `curl -v http://localhost:8080/` |
| `nc` / `telnet` | a "manual" TCP conversation | `nc -C localhost 5050` |
| `netstat` / `ss` / `lsof` | who uses a port | `netstat -ano \| findstr 8080` |
| `ipconfig` / `ip a` | my IP address | `ipconfig /all` |

:::tip Firewall
The first time you start a server, Windows may ask whether Java may access the network — allow it for **private networks**. If you test only on `localhost`, the firewall usually doesn't interfere.
:::

:::cheatsheet
### The site
- 📝 Cheat sheet = topic summary · ✓ = progress · Copy / Download on code · PL/EN/RU · light/dark.
- ✍️ = site author's addition · 🔁 = rewritten task · 📘 = from the lecture.

### Java
- `javac File.java` → `java File arg1 arg2`
- `javac *.java` · `javac --release 8 *.java`
- avoid: `var`, `List.of`, `repeat`, `isBlank`, `getFirst`, text blocks, records.

### Several windows
- Windows `.bat`: `start "name" cmd /k java Program`
- Linux/mac: `java Program &`

### Wireshark
- Windows: Npcap + loopback · Loopback interface for `localhost` · filters: `http`, `dns`, `tcp.port == 8080`.

### Console
ping · tracert/traceroute · nslookup · curl -v · nc -C / telnet · netstat/ss/lsof · ipconfig / ip a.
:::
