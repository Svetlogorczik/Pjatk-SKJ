---
title: Talking to a text server — a line-by-line client
short: Text-protocol client
description: Who speaks first, when to stop reading and how to save the answer to a file. We practise on our own QuoteDesk server — the best training before the practical test.
kind: lab
source: logic of lab 5 (clients connecting to a server in class, reading answers, saving to a file) — rewritten task
---

## In short

- A text protocol = a conversation in **lines**: every message ends with `\r\n` (or `\n`).
- Before writing a client, find out: **who speaks first** (client or server) and **how you will recognise the end of an answer**.
- There are 4 ways to detect "end of answer": a known number of lines, a marker line (e.g. `END`), the server closing the connection (`readLine() == null`) or **silence** (timeout).
- An answer often has to be **saved to a file** — without the first and the last line (header and footer).

:::origin
In lab 5 you wrote a few clients that connected to the instructor's servers: one sent a line with an identifier and read everything until the connection closed, saving the middle of the answer to a file; another had to read the server's greeting first and only then send something. It was also training for the practical test. Here you get **your own practice server** (`QuoteDesk`), so you can train at home.
:::

## Four questions before writing a client

:::steps
1. **Who speaks first?** If the server sends a greeting and you immediately read "your" answer — everything shifts by one line. If both sides wait for the other — the program **hangs**.
2. **What is the format of each line?** Words, spaces, upper/lower case, line ending.
3. **How do I know the answer has ended?** (table below)
4. **What to do with an error?** E.g. the server answers `ERR ...` or closes the connection.
:::

### How to recognise the end of an answer?

| Method | In code | Example |
|---|---|---|
| a known **number** of lines | `for (int i = 0; i < n; i++) in.readLine();` | "the server will send 4 numbers" |
| the count given **in a header** | read `BEGIN 3`, then 3 lines | `BEGIN 3` + 3 quotes |
| an end **marker** line | `while (!(line = in.readLine()).equals("END"))` | `END` |
| the server **closes** the connection | `while ((line = in.readLine()) != null)` | `DUMP` in QuoteDesk |
| **silence** (nothing arrives) | `socket.setSoTimeout(500)` + `catch (SocketTimeoutException e)` | task 5 of the [practical test](exam.html) |

:::danger Don't use `in.available()` to detect the end of a message [[own]]
`available()` only tells you how many bytes **are already in the buffer** right now — not whether the server will send more. Network data arrives in pieces, so `available() == 0` can happen in the middle of an answer. The program works sometimes and fails sometimes — the worst kind of bug.
:::

## The QuoteDesk protocol

`S:` = server, `C:` = client. Every line ends with `\r\n`.

| Who | Message | Meaning |
|---|---|---|
| S | `HELLO QuoteDesk 1.0` | **greeting — the server speaks first** |
| C | `NAME <nick>` | introduce yourself (must come first) |
| S | `OK <nick>` | accepted |
| C | `QUOTES <n>` (n = 1…9) | ask for n quotes |
| S | `BEGIN <n>`, n lines, `END` | a multi-line answer with markers |
| C | `DUMP` | ask for everything |
| S | `ALL <k>`, k lines, `CLOSING` and **closes the connection** | an answer "until the end" |
| C | `BYE` | finish |
| S | `BYE <nick>` and closes | goodbye |
| S | `ERR <reason>` | error (e.g. no `NAME`, unknown command) |

## The practice server

Multi-threaded — every client gets its own thread, so several people can connect at once.

@code(c4/QuoteDesk.java)

## The client

@code(c4/DeskClient.java)

Things worth noticing in the client:

- `expect("HELLO")` — **we read the greeting first**, only then do we send `NAME`.
- The `send` / `read` / `expect` methods — a small "toolkit" that makes the main code read like a protocol description. Every sent and received line is logged — gold when debugging.
- Part 1 reads until the `END` marker, part 2 until the connection is closed (`null`).
- Only the lines **between** the markers go to the file: `all.subList(1, all.size() - 1)` skips the first (`ALL 9`) and the last (`CLOSING`).

## Running it

:::steps
1. `javac QuoteDesk.java DeskClient.java`
2. Terminal 1: `java QuoteDesk 5050`
3. Terminal 2: `java DeskClient localhost 5050 ola`
4. Check the files `quotes.txt` (3 lines) and `all-quotes.txt` (9 lines).
:::

A fragment of the client's output:

```text
[client] <- HELLO QuoteDesk 1.0
[client] -> NAME ola
[client] <- OK ola
[client] -> QUOTES 3
[client] <- BEGIN 3
[client] <- UDP: I told you once. Did you get it? Not my problem.
[client] <- DNS: it's always DNS.
[client] <- Port 80 is open, the door is ajar.
[client] <- END
[client] saved 3 lines to quotes.txt
```

## A "manual" conversation without writing code [[own]]

Before writing a client for an unknown server, **talk to it by hand** — you'll see at once who speaks first and what the answers look like:

```bash
# Linux / macOS (netcat); -C sends \r\n instead of \n
nc -C localhost 5050
# Windows: enable "Telnet Client" in Windows features, then:
telnet localhost 5050
```

Type commands and press Enter. The same works with the server during the practical test (if the instructor allows it).

## Tasks

::::task 1. Numbered output
Ask for 5 quotes and save them to a file as `1. text`, `2. text`…

:::answer
In the loop reading until `END` keep a counter: `quotes.add((quotes.size() + 1) + ". " + line);`, and change the command to `QUOTES 5`.
:::
::::

::::task 2. Handle an error
Send `QUOTES 0`. What does the server answer? Change the client so that after receiving `ERR` it prints a readable message and exits without an exception.

:::answer
The server answers `ERR unknown command: QUOTES 0` (only 1–9 are allowed). In the client: after `read()` check `if (line.startsWith("ERR")) { System.out.println("Server reported an error: " + line); return; }`.
:::
::::

::::task 3. A new protocol command
Add a `COUNT` command to the server, answered with `COUNT <number of quotes>`. Update the protocol table.

:::answer
In `serve`: `else if (cmd[0].equals("COUNT")) { reply(out, "COUNT " + QUOTES.length); }` — put it before the `ERR` branch. Remember the command only works after `NAME` (that's how the `nick == null` condition is built).
:::
::::

::::task 4. Robust against a "silent" server
What happens if the server never sends `END`? Protect the client from waiting forever.

:::answer
`readLine()` will wait forever. Set `socket.setSoTimeout(5000);` — then after 5 s with no data `readLine()` throws `SocketTimeoutException`, which you can catch and handle.
:::
::::

:::cheatsheet
### Before writing a client
1. Who speaks first? (check with `nc -C host port`)
2. Line format and line ending (`\r\n`).
3. How to recognise the end of an answer?
4. What does an error look like?

### 5 ways to detect the end of an answer
- known number of lines → `for`
- count in a header (`BEGIN n`) → read n
- marker (`END`) → `while (!line.equals("END"))`
- server closes → `while ((line = in.readLine()) != null)`
- silence → `setSoTimeout` + `SocketTimeoutException`

### Helper methods (pattern)
- `send(line)`: `out.print(line + "\r\n"); out.flush();` + log
- `read()`: `readLine()`, `null` → "server closed" exception; log
- `expect(prefix)`: `read()` + check the beginning

### Saving to a file
- `BufferedWriter w = new BufferedWriter(new FileWriter("out.txt"));`
- without the first and last line: `list.subList(1, list.size() - 1)`

### Don't
- Use `in.available()` as "end of message".
- Read the answer before reading the greeting.
- Forget `flush()`.
:::
