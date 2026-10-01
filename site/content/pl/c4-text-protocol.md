---
title: Rozmowa z serwerem tekstowym — klient linia po linii
short: Klient protokołu tekstowego
description: Kto mówi pierwszy, kiedy przestać czytać i jak zapisać odpowiedź do pliku. Ćwiczymy na własnym serwerze QuoteDesk — to najlepszy trening przed kolokwium.
kind: lab
source: logika ćwiczeń 5 (klienci łączący się z serwerem na zajęciach, czytanie odpowiedzi, zapis do pliku) — zadanie przerobione
---

## W skrócie

- Protokół tekstowy = rozmowa **liniami**: każda wiadomość kończy się `\r\n` (albo `\n`).
- Zanim napiszesz klienta, ustal: **kto mówi pierwszy** (klient czy serwer) i **jak poznasz koniec odpowiedzi**.
- Są 4 sposoby na „koniec odpowiedzi”: znana liczba linii, linia-znacznik (np. `END`), zamknięcie połączenia przez serwer (`readLine() == null`) albo **cisza** (timeout).
- Odpowiedź często trzeba **zapisać do pliku** — bez pierwszej i ostatniej linii (nagłówek i stopka).

:::origin
Na ćwiczeniach 5 pisało się kilku klientów, którzy łączyli się z serwerami prowadzącego: jeden wysyłał linię z identyfikatorem i czytał wszystko do zamknięcia połączenia, zapisując środek odpowiedzi do pliku; inny musiał najpierw odczytać powitanie serwera, a dopiero potem coś wysłać. To był też trening przed kolokwium. Tutaj masz **własny serwer ćwiczeniowy** (`QuoteDesk`), więc możesz trenować w domu.
:::

## Cztery pytania przed napisaniem klienta

:::steps
1. **Kto mówi pierwszy?** Jeśli serwer wysyła powitanie, a Ty od razu czytasz swoją odpowiedź — wszystko się przesunie o jedną linię. Jeśli obie strony czekają na drugą — program **wisi**.
2. **Jaki jest format każdej linii?** Słowa, spacje, wielkie/małe litery, koniec linii.
3. **Skąd wiem, że odpowiedź się skończyła?** (tabela niżej)
4. **Co zrobić z błędem?** Np. serwer odpowiada `ERR ...` albo zamyka połączenie.
:::

### Jak poznać koniec odpowiedzi?

| Sposób | Jak w kodzie | Przykład |
|---|---|---|
| znana **liczba** linii | `for (int i = 0; i < n; i++) in.readLine();` | „serwer przyśle 4 liczby” |
| liczba podana **w nagłówku** | przeczytaj `BEGIN 3`, potem 3 linie | `BEGIN 3` + 3 cytaty |
| linia-**znacznik** końca | `while (!(line = in.readLine()).equals("END"))` | `END` |
| serwer **zamyka** połączenie | `while ((line = in.readLine()) != null)` | `DUMP` w QuoteDesk |
| **cisza** (nic nie przychodzi) | `socket.setSoTimeout(500)` + `catch (SocketTimeoutException e)` | zadanie 5 na [kolokwium](exam.html) |

:::danger Nie używaj `in.available()` do wykrywania końca wiadomości [[own]]
`available()` mówi tylko, ile bajtów **już leży w buforze** w tej chwili — a nie, czy serwer jeszcze coś wyśle. Dane z sieci przychodzą kawałkami, więc `available() == 0` może się zdarzyć w połowie odpowiedzi. Program raz działa, raz nie — najgorszy rodzaj błędu.
:::

## Protokół QuoteDesk

`S:` = serwer, `C:` = klient. Każda linia kończy się `\r\n`.

| Kto | Komunikat | Znaczenie |
|---|---|---|
| S | `HELLO QuoteDesk 1.0` | **powitanie — serwer mówi pierwszy** |
| C | `NAME <nick>` | przedstaw się (musi być pierwsze) |
| S | `OK <nick>` | przyjęto |
| C | `QUOTES <n>` (n = 1…9) | poproś o n cytatów |
| S | `BEGIN <n>`, n linii, `END` | odpowiedź wieloliniowa ze znacznikami |
| C | `DUMP` | poproś o wszystko |
| S | `ALL <k>`, k linii, `CLOSING` i **zamknięcie połączenia** | odpowiedź „do końca” |
| C | `BYE` | koniec |
| S | `BYE <nick>` i zamknięcie | pożegnanie |
| S | `ERR <powód>` | błąd (np. brak `NAME`, nieznana komenda) |

## Serwer ćwiczeniowy

Wielowątkowy — każdy klient dostaje osobny wątek, więc może się łączyć kilka osób naraz.

@code(c4/QuoteDesk.java)

## Klient

@code(c4/DeskClient.java)

Co warto zauważyć w kodzie klienta:

- `expect("HELLO")` — **najpierw czytamy powitanie**, dopiero potem wysyłamy `NAME`.
- Metody `send` / `read` / `expect` — mały „zestaw narzędzi”, dzięki któremu główny kod czyta się jak opis protokołu. Każda wysłana i odebrana linia jest logowana — przy debugowaniu to złoto.
- Część 1 czyta do znacznika `END`, część 2 do zamknięcia połączenia (`null`).
- Do pliku trafiają tylko linie **pomiędzy** znacznikami: `all.subList(1, all.size() - 1)` pomija pierwszą (`ALL 9`) i ostatnią (`CLOSING`).

## Uruchomienie

:::steps
1. `javac QuoteDesk.java DeskClient.java`
2. Terminal 1: `java QuoteDesk 5050`
3. Terminal 2: `java DeskClient localhost 5050 ola`
4. Sprawdź pliki `quotes.txt` (3 linie) i `all-quotes.txt` (9 linii).
:::

Fragment wydruku klienta:

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

## Rozmowa „ręczna” bez pisania kodu [[own]]

Zanim napiszesz klienta do nieznanego serwera, **porozmawiaj z nim ręcznie** — od razu zobaczysz, kto mówi pierwszy i jak wyglądają odpowiedzi:

```bash
# Linux / macOS (netcat); -C wysyła \r\n zamiast \n
nc -C localhost 5050
# Windows: włącz "Klient Telnet" w funkcjach systemu, potem:
telnet localhost 5050
```

Wpisuj komendy i naciskaj Enter. To samo działa z serwerem na kolokwium (jeśli prowadzący na to pozwala).

## Zadania

::::task 1. Numerowany zapis
Poproś o 5 cytatów i zapisz je do pliku w formacie `1. tekst`, `2. tekst`…

:::answer
W pętli czytającej do `END` trzymaj licznik: `quotes.add((quotes.size() + 1) + ". " + line);`, a komendę zmień na `QUOTES 5`.
:::
::::

::::task 2. Obsłuż błąd
Wyślij `QUOTES 0`. Co odpowiada serwer? Zmień klienta tak, żeby po odebraniu `ERR` wypisał czytelny komunikat i zakończył się bez wyjątku.

:::answer
Serwer odpowie `ERR unknown command: QUOTES 0` (dozwolone są tylko 1–9). W kliencie: po `read()` sprawdź `if (line.startsWith("ERR")) { System.out.println("Serwer zgłosił błąd: " + line); return; }`.
:::
::::

::::task 3. Nowa komenda w protokole
Dodaj do serwera komendę `COUNT`, na którą serwer odpowiada `COUNT <liczba cytatów>`. Zaktualizuj tabelę protokołu.

:::answer
W `serve`: `else if (cmd[0].equals("COUNT")) { reply(out, "COUNT " + QUOTES.length); }` — wstaw przed gałęzią `ERR`. Pamiętaj, że komenda działa dopiero po `NAME` (tak jest zbudowany warunek `nick == null`).
:::
::::

::::task 4. Odporność na „milczący” serwer
Co się stanie, jeśli serwer nigdy nie wyśle `END`? Zabezpiecz klienta przed czekaniem w nieskończoność.

:::answer
`readLine()` będzie czekał wiecznie. Ustaw `socket.setSoTimeout(5000);` — wtedy po 5 s bez danych `readLine()` rzuci `SocketTimeoutException`, którą możesz złapać i obsłużyć.
:::
::::

:::cheatsheet
### Przed pisaniem klienta
1. Kto mówi pierwszy? (sprawdź `nc -C host port`)
2. Format linii i koniec linii (`\r\n`).
3. Jak poznać koniec odpowiedzi?
4. Jak wygląda błąd?

### 5 sposobów na koniec odpowiedzi
- znana liczba linii → `for`
- liczba w nagłówku (`BEGIN n`) → przeczytaj n
- znacznik (`END`) → `while (!line.equals("END"))`
- serwer zamyka → `while ((line = in.readLine()) != null)`
- cisza → `setSoTimeout` + `SocketTimeoutException`

### Pomocnicze metody (wzór)
- `send(line)`: `out.print(line + "\r\n"); out.flush();` + log
- `read()`: `readLine()`, `null` → wyjątek „serwer zamknął”; log
- `expect(prefix)`: `read()` + sprawdzenie początku

### Zapis do pliku
- `BufferedWriter w = new BufferedWriter(new FileWriter("out.txt"));`
- bez pierwszej i ostatniej linii: `list.subList(1, list.size() - 1)`

### Nie rób
- `in.available()` jako „koniec wiadomości”.
- Czytania odpowiedzi przed odczytaniem powitania.
- Zapominania o `flush()`.
:::
