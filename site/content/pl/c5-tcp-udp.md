---
title: TCP i UDP razem — strumień kontra datagramy
short: TCP + UDP razem
description: Czym różni się UDP od TCP w praktyce, jak wysłać i odebrać datagram w Javie, jak odpowiedzieć nadawcy, jak zmierzyć RTT i jak dwa programy rozmawiają jednocześnie dwoma kanałami.
kind: lab
source: logika ćwiczeń „tcp udp” (klient/serwer TCP, klient/serwer UDP, dwa programy wymieniające się wiadomościami przez TCP i UDP w pętli) — zadanie przerobione
---

## W skrócie

- **TCP**: połączenie, niezawodność, kolejność — ale to **strumień bajtów** (granice wiadomości robisz sam, np. końcem linii).
- **UDP**: bez połączenia, bez gwarancji — ale **jeden `send()` = jeden datagram = jeden `receive()`**.
- W UDP odpowiadasz nadawcy na **adres i port zapisane w odebranym pakiecie** (`getAddress()`, `getPort()`).
- Z odebranego pakietu bierz tylko **`getLength()`** bajtów — reszta bufora to śmieci.
- UDP nie powie Ci, że pakiet zginął — sam ustaw **`setSoTimeout`**.

:::origin
Na ćwiczeniach „tcp udp” były: klient i serwer TCP (serwer odsyłał tekst wielkimi literami), klient i serwer UDP (to samo przez datagramy) oraz para programów, które w pętli wymieniały się wiadomościami raz przez TCP, raz przez UDP. Nasza wersja: **lustro UDP** (odwraca tekst), **ping UDP** z pomiarem RTT oraz para **Coordinator / Worker**, gdzie zadania idą przez TCP, a wyniki przez UDP.
:::

## TCP czy UDP? [[own]]

| Cecha | TCP | UDP |
|---|---|---|
| połączenie | tak (3-etapowe nawiązanie, [C7](c7-tcp-wireshark.html)) | nie |
| gwarancja dostarczenia | tak (retransmisje) | **nie** |
| kolejność | zachowana | może się zmienić |
| granice wiadomości | **brak** — strumień bajtów | **są** — każdy datagram osobno |
| narzut | większy (nagłówek 20 B + potwierdzenia) | mały (nagłówek 8 B) |
| klasy w Javie | `ServerSocket`, `Socket` | `DatagramSocket`, `DatagramPacket` |
| typowe użycie | HTTP, poczta, pliki | DNS ([C6](c6-dns.html)), gry, wideo na żywo |

:::analogy TCP to rozmowa telefoniczna, UDP to SMS-y [[own]]
Przy telefonie najpierw się łączysz, potem rozmowa płynie i słyszysz wszystko po kolei. SMS-y wysyłasz bez łączenia — każdy jest osobną „paczką”, mogą przyjść w innej kolejności albo wcale.
:::

## UDP w Javie

@svg(sockets-udp, caption=Serwer UDP wiąże się z portem i czeka w receive(). Klient wysyła datagram na host:port. Serwer odpowiada na adres i port nadawcy wzięte z odebranego pakietu.)

:::steps
1. **Gniazdo**: serwer `new DatagramSocket(port)` (stały port), klient `new DatagramSocket()` (dowolny wolny port).
2. **Wysyłanie**: zamień tekst na bajty (`getBytes(UTF_8)`), zapakuj w `new DatagramPacket(bajty, długość, adres, port)`, wywołaj `send`.
3. **Odbieranie**: przygotuj pusty pakiet z buforem (`new DatagramPacket(new byte[1500], 1500)`), wywołaj `receive` — metoda **czeka**.
4. **Odczyt**: `new String(p.getData(), 0, p.getLength(), UTF_8)` — **tylko `getLength()` bajtów!**
5. **Odpowiedź**: na `p.getAddress()` i `p.getPort()` z odebranego pakietu.
:::

:::danger Pułapki UDP [[own]]
- `new String(p.getData())` bez długości — dostaniesz tekst plus **śmieci / zera** z reszty bufora.
- Ten sam obiekt `DatagramPacket` użyty ponownie do `receive` ma **długość zmniejszoną** do poprzedniej wiadomości — dłuższa wiadomość zostanie **ucięta**. Twórz nowy pakiet w pętli albo wołaj `p.setLength(buffer.length)`.
- Wiadomość dłuższa niż bufor jest **ucinana bez żadnego błędu**.
- Bez `setSoTimeout` klient czeka na zgubioną odpowiedź **w nieskończoność**.
- TCP i UDP mają **osobne** numery portów: na porcie 6000 TCP i 6000 UDP mogą jednocześnie działać różne programy (przyda się w projekcie — proxy nasłuchuje na obu).
:::

### Lustro UDP (serwer)

@code(c5/UdpMirror.java)

### Ping UDP z pomiarem RTT (klient)

@code(c5/UdpPing.java)

Wynik (prawdziwy wydruk z testu):

```text
[ping] my local UDP port: 37141
[ping] reply 1: "1# PDU olleH"  RTT = 5.645 ms
[ping] reply 2: "2# PDU olleH"  RTT = 0.555 ms
[ping] reply 3: "3# PDU olleH"  RTT = 0.601 ms
[ping] reply 4: "4# PDU olleH"  RTT = 0.217 ms
```

:::own Dlaczego pierwszy pomiar jest najdłuższy?
Przy pierwszym pakiecie Java „rozgrzewa się” (ładowanie klas, kompilacja JIT), a system przygotowuje struktury dla nowego gniazda. To dobry przykład, że **jeden pomiar to za mało** — przy RTT liczy się średnią (zobacz EstimatedRTT w [C7](c7-tcp-wireshark.html)).
:::

Gdy wyślesz datagram na port, na którym nikt nie słucha, na Linuksie zobaczysz po prostu `datagram lost (no answer within 1 s)` — UDP nie zgłosi błędu.

## Dwa kanały naraz: Coordinator i Worker

Dwa programy rozmawiają **jednocześnie** przez TCP i UDP:

| Kanał | Kierunek | Komunikat |
|---|---|---|
| TCP | Coordinator → Worker | `TASK <runda> <a> <b>` |
| UDP | Worker → Coordinator | `RESULT <runda> <a+b>` |
| TCP | Coordinator → Worker | `ACK <runda> OK` lub `ACK <runda> BAD` |
| TCP | Coordinator → Worker | `BYE` (po 5 rundach) |

Kto czego słucha?

- Coordinator: **TCP 6100** (`ServerSocket`) i **UDP 6101** (`DatagramSocket(6101)`).
- Worker: łączy się przez TCP z 6100, a jego gniazdo UDP ma **dowolny** port — wysyła na 6101.

@code(c5/Coordinator.java)

@code(c5/Worker.java)

Uruchomienie: najpierw `java Coordinator 6100 6101`, potem `java Worker localhost 6100 6101`. Fragment wydruku Workera:

```text
[worker] connected over TCP, my UDP port is 49180
[worker] TCP <- TASK 1 3 1
[worker] UDP -> RESULT 1 4
[worker] TCP <- ACK 1 OK
[worker] TCP <- TASK 2 88 73
[worker] UDP -> RESULT 2 161
[worker] TCP <- ACK 2 OK
```

## Zadania

::::task 1. Statystyka RTT
Zmień `UdpPing` tak, żeby wysyłał 10 pakietów i na końcu wypisał **minimum, maksimum i średnie** RTT.

:::answer
Trzymaj `double min = Double.MAX_VALUE, max = 0, sum = 0; int ok = 0;` i po każdej odpowiedzi aktualizuj: `min = Math.min(min, rtt); max = Math.max(max, rtt); sum += rtt; ok++;`. Na końcu `sum / ok` (uważaj na `ok == 0`, gdy wszystko zginęło).
:::
::::

::::task 2. Zgubione datagramy i ponawianie
W `UdpMirror` nie odpowiadaj losowo na 30% wiadomości (`if (Math.random() < 0.3) continue;`). Dodaj w `UdpPing` **ponawianie** (maks. 3 próby) po timeoucie.

:::answer
Owiń wysyłanie i odbiór pętlą `for (int attempt = 1; attempt <= 3; attempt++) { send; try { receive; break; } catch (SocketTimeoutException e) { log("retry " + attempt); } }`. To jest dokładnie to, co TCP robi za Ciebie automatycznie.
:::
::::

::::task 3. Za mały bufor
Wyślij do `UdpMirror` tekst dłuższy niż 1500 bajtów (np. 2000 znaków `x`). Co dostaje serwer?

:::answer
Serwer dostaje tylko pierwsze 1500 bajtów — reszta datagramu przepada **bez wyjątku**. Wniosek: ustal w protokole maksymalny rozmiar wiadomości (w projekcie dostarczone programy używają bufora **256 bajtów**!).
:::
::::

::::task 4. Odpowiedź przez UDP
Niech Coordinator wysyła `ACK` nie przez TCP, ale **przez UDP** na adres i port, z którego przyszedł `RESULT`. Co musi zmienić Worker?

:::answer
Coordinator: `udp.send(new DatagramPacket(ack, ack.length, p.getAddress(), p.getPort()))`. Worker musi po wysłaniu `RESULT` wywołać `udp.receive(...)` na **tym samym** gnieździe UDP (bo jego port jest zapisany w pakiecie) i ustawić timeout. Kanał TCP służy wtedy tylko do zadań i `BYE`.
:::
::::

:::cheatsheet
### UDP — przepis
- Serwer: `DatagramSocket s = new DatagramSocket(port);`
- Klient: `DatagramSocket s = new DatagramSocket(); s.setSoTimeout(1000);`
- Wyślij: `byte[] b = text.getBytes(UTF_8); s.send(new DatagramPacket(b, b.length, InetAddress.getByName(host), port));`
- Odbierz: `DatagramPacket p = new DatagramPacket(new byte[1500], 1500); s.receive(p);`
- Tekst: `new String(p.getData(), 0, p.getLength(), UTF_8)`
- Odpowiedz: `new DatagramPacket(b, b.length, p.getAddress(), p.getPort())`

### TCP vs UDP
| | TCP | UDP |
|---|---|---|
| połączenie | tak | nie |
| gwarancje | tak | nie |
| granice wiadomości | nie (strumień) | tak (datagram) |
| zgubienie | retransmisja | timeout w Twoim kodzie |

### Pułapki
- zawsze `getLength()`;
- nowy pakiet do każdego `receive` (albo `setLength`);
- za mały bufor = ucięta wiadomość bez błędu;
- port TCP i UDP o tym samym numerze to dwa różne porty.

### RTT
`long t = System.nanoTime(); send...; receive...; (System.nanoTime() - t) / 1e6` = ms.
:::
