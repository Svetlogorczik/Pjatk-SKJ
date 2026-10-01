---
title: Wireshark and HTTP — reading a browser's conversation with a server
short: Wireshark + HTTP
description: How to capture HTTP traffic in Wireshark and read the protocol version, headers, status codes, the conditional GET (304), splitting a response into TCP segments, embedded objects and Basic authentication.
kind: lab
source: logic of the "Wireshark Lab: HTTP" (Kurose & Ross) used in class — questions rewritten
---

## In short

- **Wireshark** copies every frame passing through the network card and shows it layer by layer.
- **HTTP** is a text protocol: a request (`GET /page HTTP/1.1` + headers + empty line) and a response (`HTTP/1.1 200 OK` + headers + empty line + body).
- **Conditional GET**: the browser sends `If-Modified-Since`, and the server answers `304 Not Modified` **without a body** if the file hasn't changed.
- A long HTTP response is split by TCP into **many segments**.
- `Authorization: Basic ...` is just **Base64** — the password can be read.

:::origin
In class you do the Wireshark HTTP lab from the Kurose & Ross textbook: you capture the browser's traffic to a few simple pages on the `gaia.cs.umass.edu` server and answer questions about headers, status codes, the conditional GET, long documents, images embedded in a page and a password-protected page. Below you get **our own set of questions** about the same things and instructions on **where to find the answers** — the actual values depend on your capture.
:::

## Before you start (from the lab instructions)

:::steps
1. **Turn off your VPN** — with a VPN the upper-layer data is encrypted and you will see nothing.
2. **Disable HTTP/3 and QUIC** in the browser — they encrypt traffic too (since 2025 most browsers enable them by default).
3. Type addresses starting with **`http://`**, not `https://`.
4. **Clear the browser cache** and history; turn off "privacy" settings that may change requests.
5. Type **`http`** (lowercase) in Wireshark's display filter — you will see only HTTP packets.
6. Ignore requests for **`favicon.ico`** — that's the browser asking for the page icon on its own.
:::

:::tip Can't capture live traffic?
The lab provides ready-made capture files (*trace files*, archive `wireshark-traces-9e.zip` on the authors' site). Open them with **File → Open** and answer the questions the same way.
:::

## Wireshark in 2 minutes [[own]]

The Wireshark window has three panes:

1. **Packet list** — one row per frame: number (assigned by Wireshark, it does not exist in the packet!), time, source, destination, protocol, info.
2. **Packet details** — a tree of layers: Frame → Ethernet → IP → TCP → HTTP (that's the encapsulation from [W7](w7-layers.html)).
3. **Bytes** — the raw content in hex and ASCII.

The most useful filters:

| Filter | What it shows |
|---|---|
| `http` | only HTTP |
| `http.request.method == "GET"` | only GET requests |
| `http.response.code == 304` | only 304 responses |
| `ip.addr == 128.119.245.12` | traffic to/from an address |
| `tcp.port == 8080` | traffic on port 8080 (e.g. your server from [C3](c3-tcp-server.html)) |
| `tcp` / `udp` / `dns` | a given protocol |

Right-click a packet → **Follow → TCP Stream** — you'll see the whole conversation as plain text (request in red, response in blue).

## Anatomy of an HTTP message [[own]]

An example (made up, but in exactly the format of a real one):

```http
GET /lab/hello.html HTTP/1.1
Host: www.example.org
User-Agent: Mozilla/5.0 (Windows NT 10.0)
Accept: text/html
Accept-Language: pl,en-US;q=0.7
If-Modified-Since: Tue, 14 Oct 2025 08:15:00 GMT
Connection: keep-alive

```

```http
HTTP/1.1 200 OK
Date: Wed, 15 Oct 2025 10:02:11 GMT
Server: Apache
Last-Modified: Wed, 15 Oct 2025 09:59:00 GMT
Content-Length: 128
Content-Type: text/html; charset=UTF-8

<html>...128 bytes of content...</html>
```

| Element | Where | Meaning |
|---|---|---|
| `GET /lab/hello.html HTTP/1.1` | **request line** | method, path, **client's HTTP version** |
| `Host` | header | server name (mandatory in HTTP/1.1) |
| `Accept-Language` | header | **languages** the browser accepts (`q` = preference) |
| `If-Modified-Since` | header | "send it only if it changed after this date" |
| empty line | after headers | **end of headers** — without it the server waits forever |
| `HTTP/1.1 200 OK` | **status line** | **server's HTTP version**, code, phrase |
| `Last-Modified` | header | when the file was modified on the server |
| `Content-Length` | header | how many **bytes of body** (without headers) |

The most important status codes:

| Code | Meaning |
|---|---|
| 200 OK | all good, body included |
| 301 Moved Permanently | the resource moved (header `Location`) |
| 304 Not Modified | unchanged — **use your cached copy**, no body |
| 400 Bad Request | the server didn't understand the request (e.g. bad format) |
| 401 Unauthorized | login required (header `WWW-Authenticate`) |
| 404 Not Found | no such resource |
| 405 Method Not Allowed | the method (e.g. POST) isn't allowed |

## Part A — a simple GET and response

Open a simple, short page (e.g. the first file of the lab) and find two packets: the **GET** and the response.

::::task Questions A1–A7
1. Which HTTP version does the browser declare, and which does the server?
2. What is the name of the header listing preferred languages, and which languages does it contain?
3. What is your computer's IP address, and the server's?
4. Which status code and phrase did the server return?
5. When was the file last modified on the server?
6. How many bytes of content did the server send?
7. Do the raw bytes show any header that is not in the packet list?

:::hint Where to look
1. The request line in the GET packet (last word) and the status line in the response (first word).
2. In the GET: `Accept-Language`.
3. The IP layer of the GET packet: **Source** = your computer, **Destination** = the server. Reversed in the response.
4. The response's status line, e.g. `200 OK`.
5. The `Last-Modified` header in the response. Note: the lab server sets the modification date to "now" **every minute** — that's why the instructions say to wait more than a minute before capturing.
6. The `Content-Length` header (the length of the body only). Don't confuse it with the TCP segment length (body + HTTP headers).
7. Compare the HTTP tree with the bytes pane — Wireshark usually shows all headers; "I see no extra ones" is also a correct answer if that's the case.
:::
::::

## Part B — the conditional GET (304)

Browsers keep copies of pages in a **cache**. When you open the same page again, the browser asks the server: "has it changed since…?".

:::steps
1. Clear the cache, start capturing.
2. Open the page, and right after that **refresh** it (or type the address again).
3. Stop capturing, filter `http`.
:::

::::task Questions B1–B4
1. Is there an `If-Modified-Since` header in the **first** GET?
2. Does the first response contain the file content? How can you tell?
3. Is there an `If-Modified-Since` in the **second** GET? Which date does it contain and where did the browser get it from?
4. What status code does the second response have, and does it contain a body?

:::answer Model answers (dates depend on your capture)
1. No — the browser doesn't have a copy yet (the cache was empty).
2. Yes — code 200, `Content-Length` > 0 and Wireshark shows a *Line-based text data* section with the HTML.
3. Yes — the date is **the same as `Last-Modified` from the first response**: the browser stored it together with the copy.
4. `304 Not Modified` — **no body**; the browser shows the cached copy. (A note from the instructions: Chrome readily sends `If-Modified-Since`, Firefox and Safari can be finicky.)
:::
::::

## Part C — a long document

When an HTML file is long (about 4500 bytes in the lab), **one** HTTP response doesn't fit in **one** TCP segment. TCP splits it into several segments, and Wireshark marks them as *"TCP segment of a reassembled PDU"*.

::::task Questions C1–C4
1. How many GET requests did the browser send? Which packet (number from the list) contains the GET for the long file?
2. Which packet contains the response's status line?
3. What are the code and phrase?
4. How many data-carrying TCP segments were needed for the whole response?

:::hint Where to look
Clear the filter (so you can see TCP segments too). In the packet with the HTTP response expand **"[N Reassembled TCP Segments]"** — Wireshark lists how many segments it reassembled and which packets it used. The status line is in the **first** of them, but Wireshark shows the HTTP response at the **last** one.
:::
::::

## Part D — a page with embedded objects

A page can contain images stored on **other servers**. The HTML only contains the images' **URLs**, so the browser must send separate requests for them.

::::task Questions D1–D2
1. How many GET requests did the browser send, and to which IP addresses?
2. Were the images downloaded **one after another** (serially) or **in parallel**? How can you check?

:::hint How to check
Look at the **Time** column and at the **TCP source ports**: if the second GET left before the answer to the first arrived, and over a different TCP connection (a different source port) — the download was parallel. If each GET waits for the previous one to finish — serial.
:::
::::

## Part E — a password-protected page (HTTP Basic)

The lab has a page that requires a login (the user name and password are given in the lab instructions).

::::task Questions E1–E3
1. What does the server answer to the **first** GET (code and phrase)?
2. Which **new field** appears in the **second** GET (after typing the password)?
3. Decode the value of that field. What do you see?

:::answer Model answers
1. `401 Unauthorized` (with a `WWW-Authenticate` header asking for credentials).
2. `Authorization: Basic <a string>`.
3. The string is **Base64** of the text `login:password`. That is **not encryption** — anyone who captures the packet can decode it (see [W6](w6-security.html)).
:::
::::

You can decode it with a Java program — no websites needed:

@code(c2/Base64Peek.java)

```bash
javac Base64Peek.java
java Base64Peek "Basic c3R1ZGVudDpzaWVjMTIz"
# Decoded  : student:siec123
```

## An offline exercise: your own server [[own]]

You can do the whole lab locally: run `MiniWebServer` from [C3](c3-tcp-server.html) and watch the traffic in Wireshark on the **Loopback** interface (Windows: "Adapter for loopback traffic capture" from Npcap; Linux: `lo`), with the filter `tcp.port == 8080`.

```bash
# a 200 response with all headers
curl -v http://localhost:8080/index.html
# conditional GET -> 304
curl -v -H "If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT" http://localhost:8080/index.html
```

:::cheatsheet
### Before capturing
- No VPN, no HTTP/3/QUIC, an `http://` address, an empty cache.
- Filter `http`; ignore `favicon.ico`.

### Where things are
| Question | Where to look |
|---|---|
| HTTP version | request line / status line |
| languages | `Accept-Language` |
| client/server IP | IP layer: Source / Destination |
| status code | response status line |
| modification date | `Last-Modified` |
| body size | `Content-Length` |
| conditional GET | `If-Modified-Since` → `304 Not Modified` (no body) |
| many segments | "[N Reassembled TCP Segments]" |
| parallel or serial | Time column + TCP source ports |
| login | `401` → `Authorization: Basic` (Base64!) |

### Filters
`http`, `http.request.method == "GET"`, `http.response.code == 304`, `ip.addr == X`, `tcp.port == 8080`; Follow → TCP Stream.

### Codes
200 OK · 301 moved · 304 not modified · 400 bad format · 401 login · 404 missing · 405 wrong method.
:::
