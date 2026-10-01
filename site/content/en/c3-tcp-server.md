---
title: TCP sockets in Java — your own mini HTTP server and client
short: TCP in Java: mini HTTP server
description: We write a client that sends a correct HTTP/1.1 request and reads the whole response, and a server with three pages that answers 200, 304 or 404. Step by step: streams, readLine, flush, the empty line.
kind: lab
source: logic of lab 3 (HTTP client + server handling 304 and 404) — rewritten task
---

## In short

- TCP server: `new ServerSocket(port)` → `accept()` → streams → read the request → send the response → `close()`.
- TCP client: `new Socket(host, port)` → send the request → **`flush()`** → read the response until the end.
- HTTP/1.1 requires: lines ending with **`\r\n`**, a **`Host`** header, and an **empty line** after the headers.
- Server logic: no such page → **404**; the page hasn't changed since `If-Modified-Since` → **304** (no body); otherwise → **200** with the body.

:::origin
In lab 3 there was a TCP client sending a GET request (goal: get `200 OK` instead of `400` and see the **whole** response), and a simple server handling 3 "fake" files: it returned `304` when the request had a last-modification date and `404` for missing files. Below is **our version**: correct HTTP/1.1 with the real HTTP date format, full header parsing and extra 400/405 codes.
:::

## What a conversation over TCP sockets looks like

@svg(sockets-tcp, caption=The server waits in accept(), the client connects with the Socket constructor. Then both sides write and read streams. ⏳ = the method blocks (waits).)

Java streams are "layered" — we wrap raw bytes in more convenient classes:

| Class | What it gives you | Note |
|---|---|---|
| `InputStream` / `OutputStream` | raw bytes from the socket | `socket.getInputStream()` |
| `InputStreamReader` | bytes → characters | give an encoding, e.g. `StandardCharsets.UTF_8` |
| `BufferedReader` | `readLine()` — reads a whole line | returns `null` when the other side closed the connection |
| `PrintWriter` / `BufferedWriter` | convenient text writing | data sits in a buffer **until `flush()`** |

:::danger Three mistakes that make a program "hang" [[own]]
1. **No `flush()`** — the data stayed in the buffer, the other side got nothing and waits.
2. **No line ending** — `readLine()` on the other side waits for a `\n` that never came (`print("abc")` instead of `println`/`"abc\r\n"`).
3. **Reading "until the end" when nobody closes** — the loop `while ((line = in.readLine()) != null)` ends only when the other side **closes the connection**. In HTTP you force that with the `Connection: close` header.
:::

## Why does the server answer 400 Bad Request?

A classic problem from class. Compare:

```text
WRONG:                                        RIGHT:
GET /index.html HTTP/1.1\r\n                  GET /index.html HTTP/1.1\r\n
If-Modified-Since: 2018-05-05T11:50:56\r\n    Host: localhost\r\n
                                              Connection: close\r\n
                                              If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT\r\n
                                              \r\n
```

| Problem in the "wrong" version | Effect |
|---|---|
| no `Host` header | real HTTP/1.1 servers answer **400** |
| no empty line at the end | the server waits for more headers — it **hangs** or answers after a timeout |
| a date in the wrong format | the server ignores the header or rejects the request |

An HTTP date **always** has the format `Mon, 06 Oct 2025 09:00:00 GMT` (English abbreviations, GMT zone). [[own]]

## The server: MiniWebServer

@code(c3/MiniWebServer.java)

What the server does, step by step:

:::steps
1. It opens a port (`ServerSocket`) and waits for a client in a loop (`accept()`).
2. It reads the **request line**, e.g. `GET /index.html HTTP/1.1`.
3. It reads the **headers** line by line until the **empty line**; stores them in a map (names in lowercase).
4. It checks the format: 3 parts and an `HTTP/...` version — otherwise **400**. A method other than GET — **405**.
5. It looks the page up in the map. Not there — **404**.
6. If there is `If-Modified-Since` and the page is **not newer** than that date — **304** without a body.
7. In all other cases — **200** with the body, `Content-Length` and `Last-Modified`.
8. It closes the connection (`try-with-resources`).
:::

:::note The 304 logic in one sentence
"Send the body only if the page on the server is **newer** than the client's copy": `if (!page.lastModified.isAfter(clientCopy)) → 304`.
:::

## The client: HttpProbe

@code(c3/HttpProbe.java)

- It builds the request with `\r\n` at the end of every line and an **empty line** at the end.
- It sends `Connection: close`, so the server closes the connection after the response and the `readLine() != null` loop ends — we get the **whole** response.
- The first response line is the status line: `split(" ", 3)` gives the version, the code and the phrase.

## Running it

:::steps
1. `javac MiniWebServer.java HttpProbe.java`
2. Terminal 1: `java MiniWebServer 8080`
3. Terminal 2: `java HttpProbe localhost 8080 /index.html` → **200**
4. `java HttpProbe localhost 8080 /index.html "Mon, 06 Oct 2025 09:00:00 GMT"` → **304**
5. `java HttpProbe localhost 8080 /missing.html` → **404**
6. Open `http://localhost:8080/schedule.html` in a browser and watch the traffic in Wireshark (Loopback, filter `tcp.port == 8080`).
:::

The output of step 4 (a real printout from testing this page):

```text
> GET /index.html HTTP/1.1
> Host: localhost
> User-Agent: HttpProbe/1.0
> Connection: close
> If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT
>
< HTTP/1.1 304 Not Modified
< Date: Thu, 01 Oct 2026 08:35:08 GMT
< Server: MiniWebServer/1.0
< Last-Modified: Mon, 06 Oct 2025 09:00:00 GMT
< Connection: close
<
Status code: 304 (Not Modified)
```

## Tasks

::::task 1. A fourth page
Add a page `/faq.html` modified on 1 December 2025, 10:00 GMT. Check 200 and 304.

:::answer
In the constructor: `pages.put("/faq.html", new Page("<h1>FAQ</h1>", "Mon, 01 Dec 2025 10:00:00 GMT"));` — the date must be in exactly the HTTP format (the weekday must match the date, otherwise parsing throws an exception).
:::
::::

::::task 2. The HEAD method
`HEAD` works like `GET`, but the server sends **only the headers** (with the same `Content-Length`), no body. Add it.

:::answer
Instead of rejecting everything except GET: `boolean head = parts[0].equals("HEAD"); if (!head && !parts[0].equals("GET")) → 405`. In `send` print `Content-Length` as before, but when `head == true` **don't call** `out.write(bodyBytes)`. Test: `curl -I http://localhost:8080/index.html`.
:::
::::

::::task 3. Many clients at once
Right now the server handles clients **one by one**. Make every client handled in a separate thread.

:::answer
```java
while (true) {
    Socket client = server.accept();
    new Thread(() -> {
        try (Socket c = client) {
            handle(c);
        } catch (IOException e) {
            log("client error: " + e.getMessage());
        }
    }).start();
}
```
The `pages` map is only read, so it is safe for many threads. If you ever modify it — use `ConcurrentHashMap`.
:::
::::

::::task 4. Require the Host header
According to HTTP/1.1, a request without a `Host` header should get **400**. Add this rule and test it with `nc`/`telnet`.

:::answer
After reading the headers: `if (parts[2].equals("HTTP/1.1") && !headers.containsKey("host")) { send(out, 400, "Bad Request", null, "<h1>Host header required</h1>"); return; }`. Test: `printf 'GET / HTTP/1.1\r\n\r\n' | nc localhost 8080`.
:::
::::

::::task 5. A real server
Use `HttpProbe` on a page from the HTTP lab (address in [C2](c2-http.html)), port 80. Then repeat with `If-Modified-Since` equal to the date from `Last-Modified`. What do you get?
::::

:::cheatsheet
### TCP server — skeleton
1. `ServerSocket ss = new ServerSocket(port);`
2. `Socket s = ss.accept();` (waits)
3. `BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), UTF_8));`
4. `PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), UTF_8));`
5. read → process → `out.print(...); out.flush();`
6. `s.close();` (and go back to `accept()`)

### TCP client — skeleton
1. `Socket s = new Socket(host, port);`
2. send lines + `flush()`
3. read `while ((line = in.readLine()) != null)` (if the server closes) or until an end marker
4. `s.close();`

### HTTP/1.1 request — template
```text
GET /path HTTP/1.1\r\n
Host: server-name\r\n
Connection: close\r\n
[If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT\r\n]
\r\n
```

### Server decision
| Condition | Response |
|---|---|
| bad request line | 400 |
| method ≠ GET | 405 |
| no page | 404 |
| page not newer than If-Modified-Since | 304 (no body) |
| otherwise | 200 + body |

### "Hangs"? Check
`flush()` · line ending `\r\n` · empty line after headers · `Connection: close`.
:::
