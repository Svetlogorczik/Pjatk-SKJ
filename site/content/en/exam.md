---
title: The practical test — training with the DrillServer
short: Practical test: training
description: What the test with the instructor's server looks like, which skills it checks and how to practise it at home on our training server (5 task types, each repeated several times).
kind: lab
source: logic of the test template in the repository (Test_template — task description and client) — tasks rewritten
---

## In short

- In the test you write a **TCP client** that connects to the **instructor's server**, logs in and solves tasks sent as **lines of text**.
- Every task must be done **several times in a row** (in the 2025/26 edition: **5 times**), and the server grades each answer at once.
- Task types: one line with numbers → one answer; several lines → one answer; many lines → several answers; a **stream** (you answer immediately to get the next number); a stream **without a given length**.
- The test has a **hard deadline** (the end time is stated in the description sent by the server).
- Below: our **training server** with the same skills but different operations — you can practise at home.

:::origin
The repository contains a test template from the previous edition: the client connects to a server in class, sends its identifier, receives a **password** and a **task description**, then logs in with the password and solves 5 tasks, each 5 times. In the original, the tasks were about **concatenating** numbers: from one line, from several lines, in pairs, "first half with second half" with a known and an unknown K. The description also defined a "line" as ASCII bytes terminated by LF, CR or CRLF. Our training has **the same task types but different operations** (sum, reversed order, `BigInteger` pair sums, pairwise maximum).
:::

## What the test checks (skills)

| Skill | Where you practised it |
|---|---|
| a TCP connection, `readLine`, `flush` | [C3](c3-tcp-server.html), [C4](c4-text-protocol.html) |
| reading a value from an answer (e.g. a password) and using it later | the "login" step below |
| parsing numbers from a line (`split`, `parseLong`) | T1 |
| reading a known number of lines | T2, T3 |
| numbers **bigger than `long`** (`BigInteger` or text operations) | T3–T5 |
| sending **several lines** of answer | T3 |
| answering **immediately** during a stream | T4 |
| detecting the end of a "burst" by **silence** (`setSoTimeout`) | T5 |

:::note Definition of a "line" (from the test description)
A line is a sequence of ASCII bytes terminated by **LF** (`\n`), **CR** (`\r`) or **CRLF** (`\r\n`). Good news: `BufferedReader.readLine()` recognises **all three** endings and returns the line without them. When sending, stick to one — `\r\n` is the safest.
:::

## The DrillServer training protocol

| Step | Who | Line |
|---|---|---|
| 1 | client | `LOGIN <nick>` |
| 2 | server | `TOKEN <6 characters>` |
| 3 | client | `START <the same token>` |
| 4 | server | data of task T1, round 1 … |
| after every answer | server | `OK` or `WRONG expected=<correct answer>` |
| at the end | server | `FINISHED <points>/<max>` and closes the connection |
| client silent > 20 s | server | `TIMEOUT` and closes |

### Tasks (each repeated R times, 3 by default)

| Task | Server sends | You send back | Example |
|---|---|---|---|
| **T1** | 1 line: `a b c` | 1 line: **a + b + c** | `5 -2 10` → `13` |
| **T2** | 4 lines, one number each | 1 line: the numbers **in reverse order**, comma-separated | `1`,`2`,`3`,`4` → `4,3,2,1` |
| **T3** | 6 lines with **big** numbers | 3 lines: pair sums (1+2, 3+4, 5+6) | `1…6` → `3`,`7`,`11` |
| **T4** | a line `K`, then K numbers $a_1…a_K$, then $b_1$ | after every $b_i$ **immediately** a line $\max(a_i, b_i)$; only then $b_{i+1}$ arrives | K=2: `2`,`10`,`20`,`15` → `15`, then `5` → `20` |
| **T5** | like T4, but **without the K line** | like T4 — you have to detect K yourself | — |

After T1, T2, T3 the server checks the answer immediately; in T4 and T5 — after the last pair.

## Downloads

@code(exam/DrillServer.java)

@code(exam/DrillClient.java, title=DrillClient.java — skeleton to complete)

## How to practise step by step

:::steps
1. Compile: `javac DrillServer.java DrillClient.java`.
2. Start the server: `java DrillServer 7777 3` (port 7777, every task 3 times).
3. Fill in the `TODO`s in `DrillClient` — **one task at a time**. Temporarily skip the others by sending anything.
4. Run the client: `java DrillClient localhost 7777 3` and watch the `->` / `<-` logs and the server console (it prints `OK` / `WRONG expected=…`).
5. When everything gives `OK`, change the number of repetitions to 5 and time yourself.
6. Only at the very end compare with the model solution (below).
:::

The server console for a correct solution (a real printout from testing):

```text
[drill] student /127.0.0.1:43174: T1 OK
[drill] student /127.0.0.1:43174: T2 OK
...
[drill] student /127.0.0.1:43174: T5 OK
[drill] student /127.0.0.1:43174: finished with 15/15
```

## Hints for each task

:::hint T1 — parsing one line
`for (String part : line.trim().split("\\s+")) sum += Long.parseLong(part);` — `split("\\s+")` handles multiple spaces, `trim()` handles the start/end. Use `long` — a sum of three `int`s can overflow `int`.
:::

:::hint T2 — a known number of lines
Read exactly 4 lines into a list, then build the answer backwards. Don't read "until the end" — the server won't close the connection, it's waiting for you.
:::

:::hint T3 — big numbers
The numbers have up to 30 digits — they don't fit into `long` (max ~9.2·10¹⁸). Use `new BigInteger(line.trim())` and `.add(...)`. Send **3 separate lines**.
:::

:::hint T4 — answer immediately
Read K, then K numbers into a list. Then in a loop `i = 0..K-1`: read $b_i$ and **immediately** send `a.get(i).max(b)` + `flush`. If you try to read all the $b$ first — the client hangs, because the server sends $b_{i+1}$ only after your answer.
:::

:::hint T5 — unknown K: detect the silence [[own]]
The server sends $a_1…a_K$ and $b_1$ **in one burst** and then **goes quiet**, because it waits for an answer. Set `socket.setSoTimeout(500)` and read in a loop until `SocketTimeoutException` is thrown — by then you've read $K + 1$ numbers, so $K = \text{count} - 1$, and the last one read is $b_1$. Afterwards go back to `setSoTimeout(0)`. That's my solution to the puzzle in the test text ("how can you guess how many numbers will be sent?").
:::

:::answer Model solution (look only after your own attempt)
@code(exam/DrillSolution.java)
:::

## How to approach the real test [[own]]

:::steps
1. **Prepare a template at home**: connection, `send` / `read` with logging, writing to a file, `BigInteger`, timeout handling. There's no time to write it from scratch during the test.
2. **First connection = save the task description to a file** (as in [C4](c4-text-protocol.html)) — you'll read it calmly, not in the console.
3. Read the test's **end time** and leave yourself a margin.
4. Do the tasks **in order** and check the server's answer after each one. A wrong answer in one task can shift the reading of the next lines.
5. Do every task **as many times as the text requires** (a loop!).
6. Write **Java 8**-compatible code — methods like `List.getFirst()` (Java 21) may not exist on the lab computer.
7. When something "hangs" — you are almost always waiting for a line the server won't send (wrong counter, unread verdict, missing `flush`).
:::

### Common mistakes

| Mistake | Symptom | Fix |
|---|---|---|
| unread `OK/WRONG` verdict | from the next task on everything is "shifted by a line" | after every answer read 1 verdict line |
| `in.available()` instead of counting lines | works sometimes | read a known number of lines or use a timeout |
| `int` / `long` for big numbers | `NumberFormatException` or wrong results | `BigInteger` |
| no `flush()` | the server waits, you wait → `TIMEOUT` | `flush()` after every answer |
| spaces at the end of a line | `NumberFormatException` | `trim()` |
| forgetting to reset `setSoTimeout(0)` | `SocketTimeoutException` in later tasks | reset it in `finally` |

:::cheatsheet
### Client template (core)
- `Socket s = new Socket(host, port);`
- `in = new BufferedReader(new InputStreamReader(s.getInputStream(), UTF_8));`
- `out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), UTF_8));`
- `send(x)`: `out.print(x + "\r\n"); out.flush();` + log
- `read()`: `readLine()`; `null` → exception; log

### Task patterns
- **1 line, many numbers** → `split("\\s+")` + `Long.parseLong`.
- **N lines** → `for (i < N) list.add(read())`.
- **big numbers** → `new BigInteger(s)`, `.add`, `.max`, `.compareTo`.
- **several answer lines** → a separate `send` for each.
- **stream** → read 1, answer 1, `flush`.
- **unknown length** → `setSoTimeout(500)` + `catch (SocketTimeoutException)` → reset `setSoTimeout(0)`.

### Order during the test
1. Save the description to a file. 2. Check the deadline. 3. Task by task, with a repetition loop. 4. Read the verdict after every answer.

### Training
`java DrillServer 7777 3` → `java DrillClient localhost 7777 3` → goal: `FINISHED 15/15`.
:::
