---
title: Gniazda TCP w Javie — własny mini-serwer HTTP i klient
short: TCP w Javie: mini-serwer HTTP
description: Piszemy klienta, który wysyła poprawne żądanie HTTP/1.1 i czyta całą odpowiedź, oraz serwer z trzema stronami, który odpowiada 200, 304 albo 404. Krok po kroku: strumienie, readLine, flush, pusta linia.
kind: lab
source: logika ćwiczeń 3 (klient HTTP + serwer z obsługą 304 i 404) — zadanie przerobione
---

## W skrócie

- Serwer TCP: `new ServerSocket(port)` → `accept()` → strumienie → czytaj żądanie → wyślij odpowiedź → `close()`.
- Klient TCP: `new Socket(host, port)` → wyślij żądanie → **`flush()`** → czytaj odpowiedź aż do końca.
- HTTP/1.1 wymaga: linie zakończone **`\r\n`**, nagłówek **`Host`**, a na końcu nagłówków **pusta linia**.
- Logika serwera: brak strony → **404**; strona się nie zmieniła od `If-Modified-Since` → **304** (bez treści); inaczej → **200** z treścią.

:::origin
Na ćwiczeniach 3 był klient TCP, który wysyłał żądanie GET (cel: dostać `200 OK` zamiast `400` i zobaczyć **całą** odpowiedź), oraz prosty serwer obsługujący 3 „udawane” pliki: odsyłał `304`, gdy żądanie miało datę ostatniej modyfikacji, i `404` dla nieistniejących plików. Poniżej **nasza wersja**: poprawny HTTP/1.1 z prawdziwym formatem dat HTTP, pełnym czytaniem nagłówków i dodatkowymi kodami 400/405.
:::

## Jak wygląda rozmowa przez gniazda TCP

@svg(sockets-tcp, caption=Serwer czeka w accept(), klient łączy się konstruktorem Socket. Potem obie strony piszą i czytają strumienie. ⏳ = metoda blokuje (czeka).)

Strumienie w Javie są „warstwowe” — owijamy surowe bajty w wygodniejsze klasy:

| Klasa | Co daje | Uwaga |
|---|---|---|
| `InputStream` / `OutputStream` | surowe bajty z gniazda | `socket.getInputStream()` |
| `InputStreamReader` | bajty → znaki | podaj kodowanie, np. `StandardCharsets.UTF_8` |
| `BufferedReader` | `readLine()` — czyta całą linię | zwraca `null`, gdy druga strona zamknęła połączenie |
| `PrintWriter` / `BufferedWriter` | wygodne pisanie tekstu | dane siedzą w buforze **do `flush()`** |

:::danger Trzy błędy, przez które program „wisi” [[own]]
1. **Brak `flush()`** — dane zostały w buforze, druga strona nic nie dostała i czeka.
2. **Brak końca linii** — `readLine()` po drugiej stronie czeka na `\n`, którego nigdy nie było (`print("abc")` zamiast `println`/`"abc\r\n"`).
3. **Czytanie „do końca”, gdy nikt nie zamyka** — pętla `while ((line = in.readLine()) != null)` skończy się dopiero, gdy druga strona **zamknie połączenie**. W HTTP wymuszasz to nagłówkiem `Connection: close`.
:::

## Dlaczego serwer odpowiada 400 Bad Request?

Klasyczny problem z zajęć. Porównaj:

```text
ŹLE:                                          DOBRZE:
GET /index.html HTTP/1.1\r\n                  GET /index.html HTTP/1.1\r\n
If-Modified-Since: 2018-05-05T11:50:56\r\n    Host: localhost\r\n
                                              Connection: close\r\n
                                              If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT\r\n
                                              \r\n
```

| Problem w wersji „źle” | Skutek |
|---|---|
| brak nagłówka `Host` | prawdziwe serwery HTTP/1.1 odpowiadają **400** |
| brak pustej linii na końcu | serwer czeka na dalsze nagłówki — **zawiesza się** albo odpowiada po timeoucie |
| data w złym formacie | serwer ignoruje nagłówek albo odrzuca żądanie |

Data w HTTP **zawsze** ma format `Mon, 06 Oct 2025 09:00:00 GMT` (angielskie skróty, strefa GMT). [[own]]

## Serwer: MiniWebServer

@code(c3/MiniWebServer.java)

Co robi serwer, krok po kroku:

:::steps
1. Otwiera port (`ServerSocket`) i w pętli czeka na klienta (`accept()`).
2. Czyta **linię żądania**, np. `GET /index.html HTTP/1.1`.
3. Czyta **nagłówki** linia po linii, aż do **pustej linii**; zapisuje je w mapie (nazwy małymi literami).
4. Sprawdza format: 3 części i wersja `HTTP/...` — inaczej **400**. Metoda inna niż GET — **405**.
5. Szuka strony w mapie. Nie ma — **404**.
6. Jeśli jest `If-Modified-Since` i strona **nie jest nowsza** niż ta data — **304** bez treści.
7. W pozostałych przypadkach — **200** z treścią, `Content-Length` i `Last-Modified`.
8. Zamyka połączenie (`try-with-resources`).
:::

:::note Logika 304 w jednym zdaniu
„Wyślij treść tylko wtedy, gdy strona na serwerze jest **nowsza** niż kopia klienta”: `if (!page.lastModified.isAfter(clientCopy)) → 304`.
:::

## Klient: HttpProbe

@code(c3/HttpProbe.java)

- Buduje żądanie z `\r\n` na końcu każdej linii i **pustą linią** na końcu.
- Wysyła `Connection: close`, więc serwer po odpowiedzi zamknie połączenie, a pętla `readLine() != null` się skończy — dostajemy **całą** odpowiedź.
- Pierwsza linia odpowiedzi to linia statusu: `split(" ", 3)` daje wersję, kod i opis.

## Uruchomienie

:::steps
1. `javac MiniWebServer.java HttpProbe.java`
2. Terminal 1: `java MiniWebServer 8080`
3. Terminal 2: `java HttpProbe localhost 8080 /index.html` → **200**
4. `java HttpProbe localhost 8080 /index.html "Mon, 06 Oct 2025 09:00:00 GMT"` → **304**
5. `java HttpProbe localhost 8080 /nie-ma.html` → **404**
6. Otwórz w przeglądarce `http://localhost:8080/schedule.html` i podejrzyj ruch w Wiresharku (Loopback, filtr `tcp.port == 8080`).
:::

Wynik kroku 4 (prawdziwy wydruk z testu tej strony):

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

## Zadania

::::task 1. Czwarta strona
Dodaj stronę `/faq.html` z datą modyfikacji 1 grudnia 2025, 10:00 GMT. Sprawdź 200 i 304.

:::answer
W konstruktorze: `pages.put("/faq.html", new Page("<h1>FAQ</h1>", "Mon, 01 Dec 2025 10:00:00 GMT"));` — data musi mieć dokładnie format HTTP (dzień tygodnia musi się zgadzać z datą, inaczej parsowanie rzuci wyjątek).
:::
::::

::::task 2. Metoda HEAD
`HEAD` działa jak `GET`, ale serwer wysyła **same nagłówki** (z tym samym `Content-Length`), bez treści. Dodaj ją.

:::answer
Zamiast odrzucać wszystko poza GET: `boolean head = parts[0].equals("HEAD"); if (!head && !parts[0].equals("GET")) → 405`. W metodzie `send` wypisuj `Content-Length` jak dotąd, ale gdy `head == true`, **nie wywołuj** `out.write(bodyBytes)`. Test: `curl -I http://localhost:8080/index.html`.
:::
::::

::::task 3. Wielu klientów naraz
Teraz serwer obsługuje klientów **po kolei**. Spraw, żeby każdy klient był obsługiwany w osobnym wątku.

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
Mapa `pages` jest tylko czytana, więc jest bezpieczna dla wielu wątków. Jeśli kiedyś będziesz ją zmieniać — użyj `ConcurrentHashMap`.
:::
::::

::::task 4. Wymuś nagłówek Host
Zgodnie z HTTP/1.1 żądanie bez nagłówka `Host` powinno dostać **400**. Dodaj tę regułę i sprawdź ją przez `nc`/`telnet`.

:::answer
Po wczytaniu nagłówków: `if (parts[2].equals("HTTP/1.1") && !headers.containsKey("host")) { send(out, 400, "Bad Request", null, "<h1>Host header required</h1>"); return; }`. Test: `printf 'GET / HTTP/1.1\r\n\r\n' | nc localhost 8080`.
:::
::::

::::task 5. Prawdziwy serwer
Użyj `HttpProbe` na stronie z laboratorium HTTP (adres w [C2](c2-http.html)), port 80. Potem powtórz z `If-Modified-Since` równym dacie z `Last-Modified`. Co dostajesz?
::::

:::cheatsheet
### Serwer TCP — szkielet
1. `ServerSocket ss = new ServerSocket(port);`
2. `Socket s = ss.accept();` (czeka)
3. `BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), UTF_8));`
4. `PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), UTF_8));`
5. czytaj → przetwórz → `out.print(...); out.flush();`
6. `s.close();` (i wróć do `accept()`)

### Klient TCP — szkielet
1. `Socket s = new Socket(host, port);`
2. wyślij linie + `flush()`
3. czytaj `while ((line = in.readLine()) != null)` (jeśli serwer zamyka) albo do znacznika końca
4. `s.close();`

### Żądanie HTTP/1.1 — szablon
```text
GET /sciezka HTTP/1.1\r\n
Host: nazwa-serwera\r\n
Connection: close\r\n
[If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT\r\n]
\r\n
```

### Decyzja serwera
| Warunek | Odpowiedź |
|---|---|
| zła linia żądania | 400 |
| metoda ≠ GET | 405 |
| brak strony | 404 |
| strona nie nowsza niż If-Modified-Since | 304 (bez treści) |
| inaczej | 200 + treść |

### „Wisi”? Sprawdź
`flush()` · koniec linii `\r\n` · pusta linia po nagłówkach · `Connection: close`.
:::
