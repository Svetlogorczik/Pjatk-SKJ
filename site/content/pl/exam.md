---
title: Kolokwium praktyczne — trening z serwerem DrillServer
short: Kolokwium: trening
description: Jak wygląda test z łączeniem się do serwera prowadzącego, jakie umiejętności sprawdza i jak go przećwiczyć w domu na naszym serwerze treningowym (5 typów zadań, każde powtarzane kilka razy).
kind: lab
source: logika szablonu testu z repozytorium (Test_template — opis zadań i klient) — zadania przerobione
---

## W skrócie

- Na kolokwium piszesz **klienta TCP**, który łączy się z **serwerem prowadzącego**, loguje się i rozwiązuje zadania przesyłane **liniami tekstu**.
- Każde zadanie trzeba wykonać **kilka razy z rzędu** (w edycji 2025/26: **5 razy**), a serwer od razu ocenia odpowiedź.
- Typy zadań: jedna linia z liczbami → jedna odpowiedź; kilka linii → jedna odpowiedź; wiele linii → kilka odpowiedzi; **strumień** (odpowiadasz natychmiast, żeby dostać kolejną liczbę); strumień **bez podanej długości**.
- Test ma **twardy termin** (godzina zakończenia jest podana w opisie od serwera).
- Poniżej: nasz **serwer treningowy** z tymi samymi umiejętnościami, ale innymi działaniami — możesz ćwiczyć w domu.

:::origin
W repozytorium jest szablon testu z poprzedniej edycji: klient łączy się z serwerem na zajęciach, wysyła swój identyfikator, dostaje **hasło** i **opis zadań**, a potem loguje się hasłem i rozwiązuje 5 zadań, każde 5 razy. W oryginale zadania polegały na **sklejaniu** (konkatenacji) liczb: z jednej linii, z kilku linii, parami, „pierwsza połowa z drugą” ze znanym i nieznanym K. Opis definiował też „linię” jako bajty ASCII zakończone LF, CR albo CRLF. Nasz trening ma **te same typy zadań, ale inne działania** (suma, odwrócona kolejność, sumy par `BigInteger`, maksimum par).
:::

## Co sprawdza kolokwium (umiejętności)

| Umiejętność | Gdzie ją ćwiczyłeś |
|---|---|
| połączenie TCP, `readLine`, `flush` | [C3](c3-tcp-server.html), [C4](c4-text-protocol.html) |
| odczytanie wartości z odpowiedzi (np. hasła) i użycie jej dalej | zadanie „logowanie” niżej |
| parsowanie liczb z linii (`split`, `parseLong`) | T1 |
| czytanie znanej liczby linii | T2, T3 |
| liczby **większe niż `long`** (`BigInteger` albo operacje na tekście) | T3–T5 |
| wysyłanie **kilku linii** odpowiedzi | T3 |
| odpowiadanie **natychmiast** w trakcie strumienia | T4 |
| wykrycie końca „serii” po **ciszy** (`setSoTimeout`) | T5 |

:::note Definicja „linii” (z opisu testu)
Linia to ciąg bajtów ASCII zakończony **LF** (`\n`), **CR** (`\r`) albo **CRLF** (`\r\n`). Dobra wiadomość: `BufferedReader.readLine()` rozpoznaje **wszystkie trzy** zakończenia i zwraca linię bez nich. Wysyłając, trzymaj się jednego — `\r\n` jest najbezpieczniejsze.
:::

## Protokół serwera treningowego DrillServer

| Krok | Kto | Linia |
|---|---|---|
| 1 | klient | `LOGIN <nick>` |
| 2 | serwer | `TOKEN <6 znaków>` |
| 3 | klient | `START <ten sam token>` |
| 4 | serwer | dane zadania T1, runda 1 … |
| po każdej odpowiedzi | serwer | `OK` albo `WRONG expected=<poprawna odpowiedź>` |
| na końcu | serwer | `FINISHED <punkty>/<maks>` i zamknięcie połączenia |
| cisza klienta > 20 s | serwer | `TIMEOUT` i zamknięcie |

### Zadania (każde powtarzane R razy, domyślnie 3)

| Zadanie | Serwer wysyła | Ty odsyłasz | Przykład |
|---|---|---|---|
| **T1** | 1 linię: `a b c` | 1 linię: **a + b + c** | `5 -2 10` → `13` |
| **T2** | 4 linie, po jednej liczbie | 1 linię: liczby **od końca**, rozdzielone przecinkami | `1`,`2`,`3`,`4` → `4,3,2,1` |
| **T3** | 6 linii z **dużymi** liczbami | 3 linie: sumy par (1+2, 3+4, 5+6) | `1…6` → `3`,`7`,`11` |
| **T4** | linię `K`, potem K liczb $a_1…a_K$, potem $b_1$ | po każdym $b_i$ **od razu** linię $\max(a_i, b_i)$; dopiero wtedy przyjdzie $b_{i+1}$ | K=2: `2`,`10`,`20`,`15` → `15`, potem `5` → `20` |
| **T5** | jak T4, ale **bez linii K** | jak T4 — K musisz wykryć sam | — |

Po T1, T2, T3 serwer sprawdza odpowiedź od razu; w T4 i T5 — po ostatniej parze.

## Pliki do pobrania

@code(exam/DrillServer.java)

@code(exam/DrillClient.java, title=DrillClient.java — szkielet do uzupełnienia)

## Jak ćwiczyć krok po kroku

:::steps
1. Skompiluj: `javac DrillServer.java DrillClient.java`.
2. Uruchom serwer: `java DrillServer 7777 3` (port 7777, każde zadanie 3 razy).
3. Uzupełnij `TODO` w `DrillClient` — **jedno zadanie naraz**. Pozostałe tymczasowo pomiń, wysyłając cokolwiek.
4. Uruchom klienta: `java DrillClient localhost 7777 3` i patrz na logi `->` / `<-` oraz na konsolę serwera (pisze `OK` / `WRONG expected=…`).
5. Gdy wszystko daje `OK`, zmień liczbę powtórzeń na 5 i zmierz czas.
6. Dopiero na końcu porównaj z rozwiązaniem wzorcowym (niżej).
:::

Konsola serwera przy poprawnym rozwiązaniu (prawdziwy wydruk z testu):

```text
[drill] student /127.0.0.1:43174: T1 OK
[drill] student /127.0.0.1:43174: T2 OK
...
[drill] student /127.0.0.1:43174: T5 OK
[drill] student /127.0.0.1:43174: finished with 15/15
```

## Wskazówki do każdego zadania

:::hint T1 — parsowanie jednej linii
`for (String part : line.trim().split("\\s+")) sum += Long.parseLong(part);` — `split("\\s+")` radzi sobie z wieloma spacjami, `trim()` z początkiem/końcem. Używaj `long` — suma trzech `int` może przekroczyć zakres `int`.
:::

:::hint T2 — znana liczba linii
Przeczytaj dokładnie 4 linie do listy, potem złóż odpowiedź od końca. Nie czytaj „do końca” — serwer nie zamknie połączenia, tylko czeka na Ciebie.
:::

:::hint T3 — duże liczby
Liczby mają do 30 cyfr — nie zmieszczą się w `long` (max ~9,2·10¹⁸). Użyj `new BigInteger(line.trim())` i `.add(...)`. Wyślij **3 osobne linie**.
:::

:::hint T4 — odpowiadaj natychmiast
Przeczytaj K, potem K liczb do listy. Następnie w pętli `i = 0..K-1`: przeczytaj $b_i$ i **od razu** wyślij `a.get(i).max(b)` + `flush`. Jeśli spróbujesz najpierw przeczytać wszystkie $b$ — klient się zawiesi, bo serwer wyśle $b_{i+1}$ dopiero po Twojej odpowiedzi.
:::

:::hint T5 — nieznane K: wykryj ciszę [[own]]
Serwer wysyła **seryjnie** $a_1…a_K$ i $b_1$, a potem **milknie**, bo czeka na odpowiedź. Ustaw `socket.setSoTimeout(500)` i czytaj w pętli, aż poleci `SocketTimeoutException` — wtedy przeczytałeś $K + 1$ liczb, więc $K = \text{liczba} - 1$, a ostatnia przeczytana to $b_1$. Po wszystkim wróć do `setSoTimeout(0)`. To jest moje rozwiązanie zagadki z treści testu („jak się domyślić, ile liczb zostanie przesłanych?”).
:::

:::answer Rozwiązanie wzorcowe (zajrzyj dopiero po własnej próbie)
@code(exam/DrillSolution.java)
:::

## Jak podejść do prawdziwego kolokwium [[own]]

:::steps
1. **Przygotuj w domu szablon**: połączenie, metody `send` / `read` z logowaniem, zapis do pliku, `BigInteger`, obsługa timeoutu. Na teście nie ma czasu na pisanie tego od zera.
2. **Pierwsze połączenie = zapisz opis zadań do pliku** (jak w [C4](c4-text-protocol.html)) — będziesz go czytać spokojnie, a nie w konsoli.
3. Przeczytaj **godzinę zakończenia** testu i zostaw sobie zapas.
4. Rób zadania **po kolei** i po każdym sprawdź odpowiedź serwera. Zła odpowiedź w jednym zadaniu może przesunąć czytanie kolejnych linii.
5. Każde zadanie wykonaj **tyle razy, ile wymaga treść** (pętla!).
6. Pisz kod zgodny z **Javą 8** — metody typu `List.getFirst()` (Java 21) mogą nie istnieć na komputerze w laboratorium.
7. Gdy coś się „zawiesza” — prawie zawsze czekasz na linię, której serwer nie wyśle (zły licznik, nieprzeczytany werdykt, brak `flush`).
:::

### Częste błędy

| Błąd | Objaw | Naprawa |
|---|---|---|
| nie przeczytany werdykt `OK/WRONG` | od kolejnego zadania wszystko „przesunięte o linię” | po każdej odpowiedzi przeczytaj 1 linię werdyktu |
| `in.available()` zamiast liczenia linii | raz działa, raz nie | czytaj znaną liczbę linii albo używaj timeoutu |
| `int` / `long` dla dużych liczb | `NumberFormatException` albo złe wyniki | `BigInteger` |
| brak `flush()` | serwer czeka, Ty czekasz → `TIMEOUT` | `flush()` po każdej odpowiedzi |
| spacje na końcu linii | `NumberFormatException` | `trim()` |
| zapomniany powrót do `setSoTimeout(0)` | `SocketTimeoutException` w kolejnych zadaniach | przywróć w `finally` |

:::cheatsheet
### Szablon klienta (rdzeń)
- `Socket s = new Socket(host, port);`
- `in = new BufferedReader(new InputStreamReader(s.getInputStream(), UTF_8));`
- `out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), UTF_8));`
- `send(x)`: `out.print(x + "\r\n"); out.flush();` + log
- `read()`: `readLine()`; `null` → wyjątek; log

### Wzorce zadań
- **1 linia, wiele liczb** → `split("\\s+")` + `Long.parseLong`.
- **N linii** → `for (i < N) list.add(read())`.
- **duże liczby** → `new BigInteger(s)`, `.add`, `.max`, `.compareTo`.
- **kilka linii odpowiedzi** → osobny `send` dla każdej.
- **strumień** → czytaj 1, odpowiadaj 1, `flush`.
- **nieznana długość** → `setSoTimeout(500)` + `catch (SocketTimeoutException)` → przywróć `setSoTimeout(0)`.

### Kolejność na teście
1. Zapisz opis do pliku. 2. Sprawdź termin. 3. Zadanie po zadaniu, z pętlą powtórzeń. 4. Po każdej odpowiedzi czytaj werdykt.

### Trening
`java DrillServer 7777 3` → `java DrillClient localhost 7777 3` → cel: `FINISHED 15/15`.
:::
