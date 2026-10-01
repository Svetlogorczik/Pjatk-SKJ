---
title: Projekt — sieć proxy łącząca serwery TCP i UDP
short: Projekt: sieć proxy
description: Co trzeba zrobić w projekcie (wersja 2025/26), jak liczone są punkty, jak zaprojektować proxy krok po kroku — wykrywanie protokołu, tablica kluczy, przekazywanie żądań, cykle, QUIT — i jak napisać dokumentację protokołu.
kind: own
source: Specyfikacja projektu 2025/2026 i szablon dokumentacji z repozytorium; plan rozwiązania i porady — opracowanie własne
---

## W skrócie

- Dostajesz gotowe **serwery kluczy** (`TCPServer`, `UDPServer` — każdy pilnuje jednej pary *klucz = wartość*) i **klientów** (`TCPClient`, `UDPClient`). **Nie wolno ich zmieniać.**
- Piszesz program **`Proxy`**, który łączy wiele serwerów i innych proxy w jedną sieć: klient pyta **dowolne** proxy, a dostaje odpowiedź tak, jakby pytał „cały świat” — i to **niezależnie od protokołu** (np. klient TCP ↔ serwer UDP).
- Musisz **zaprojektować i opisać własny protokół** między węzłami proxy.
- Punkty: od **150** (jeden protokół, bez innych proxy) do **500** (oba protokoły, dowolna sieć proxy **z cyklami**).
- Bez **dokumentacji PDF** projekt nie jest oceniany. Projekty są sprawdzane **antyplagiatem**.

:::warning Ta strona nie zawiera gotowego rozwiązania [[own]]
Celowo nie publikuję całego `Proxy.java`. Projekty przechodzą przez system antyplagiatowy, a za plagiat są **ujemne punkty** — gdyby wiele osób skopiowało ten sam kod z publicznej strony, wszyscy by na tym stracili. Dostajesz za to: dokładne wyjaśnienie zadania, plan krok po kroku, małe fragmenty-cegiełki, pułapki dostarczonych programów (sprawdzone na prawdziwym kodzie) i skrypty testowe.
:::

:::origin
Specyfikacja z edycji 2025/26 opowiada historię planety „Numerlandii”, na której mieszkańcy i strażnicy wiedzy mówią dwoma językami (TCP i UDP), a mądry władca buduje sieć **pośredników**, którzy znają oba języki i wymieniają się wiedzą. Poniżej jest ta sama treść opisana prostymi słowami. **Sprawdź u prowadzącego, czy w Twojej edycji specyfikacja jest taka sama** — mogła się zmienić.
:::

## Co jest dane (nie zmieniasz tego)

### Uruchamianie gotowych programów

```bash
java TCPServer -port <numer> -key <nazwa> -value <liczba>
java UDPServer -port <numer> -key <nazwa> -value <liczba>
java TCPClient -address <adres> -port <numer> -command <komenda>
java UDPClient -address <adres> -port <numer> -command <komenda>
```

- Każdy serwer pilnuje **jednego** klucza. W całej sieci jest **co najwyżej jeden** serwer dla danej nazwy klucza.
- Klient TCP rozmawia tylko z serwerem TCP, klient UDP — tylko z UDP. Klient wysyła **jedną** komendę, czyta **jedną** odpowiedź i kończy pracę.
- Serwery nie utrzymują połączeń — obsługują żądania **po kolei**.

### Podstawowy protokół klient–serwer

Każda komenda i każda odpowiedź to **jedna linia** tekstu. W UDP cała wiadomość mieści się w **jednym datagramie**.

| Komenda | Odpowiedź | Uwagi |
|---|---|---|
| `GET NAMES` | `OK <liczba> <nazwa1> <nazwa2> …` | serwer bazowy zawsze `OK 1 <swój klucz>` |
| `GET VALUE <nazwa>` | `OK <wartość>` albo `NA` | `NA` = nie mam takiego klucza |
| `SET <nazwa> <wartość>` | `OK` albo `NA` | zmienia wartość klucza |
| `QUIT` | **brak odpowiedzi** | serwer kończy pracę |
| cokolwiek innego | `NA` | |

## Co ma robić Twoje proxy

```bash
java Proxy -port <port> -server <adres> <port> [-server <adres> <port> ...]
```

- `-port` — port, na którym proxy słucha **jednocześnie przez TCP i UDP** (dla klientów i innych proxy).
- `-server adres port` — co najmniej raz, może być wiele razy: węzeł, z którym proxy ma się połączyć — **serwer kluczy albo inne proxy**. Ta sama para może być podana kilku proxy.
- Proxy może rozmawiać **bezpośrednio tylko** z węzłami z parametrów; do pozostałych dociera **przez inne proxy**.
- Złe lub brakujące parametry → komunikat o błędzie i **koniec programu**.

Po uruchomieniu proxy:

:::steps
1. Otwiera port **TCP i UDP** dla klientów i innych proxy.
2. Kontaktuje się z węzłami z parametrów i ustala, **które to serwery, a które proxy**, oraz jakiego **protokołu** używa każdy serwer.
3. Zbiera informację, **jakie klucze są w sieci** i **jak do nich dotrzeć** (który serwer lub przez które proxy).
4. Obsługuje klientów obu protokołów:
   - `GET NAMES` → lista **wszystkich** kluczy w sieci (uwaga na **licznik** na drugiej pozycji!),
   - `GET VALUE` / `SET` → przekazuje do sieci tak, żeby trafiło do właściwego serwera, i oddaje klientowi jego odpowiedź; jeśli klucza nie ma w sieci, może od razu odpowiedzieć `NA`,
   - `QUIT` → przekazuje dalej w sieć i **zamyka podległe serwery**, potem sam kończy pracę.
5. **Proxy nie przechowuje wartości kluczy** — tylko wie, gdzie są.
:::

@svg(proxy-topology, caption=Po lewej drzewo proxy (bez cykli). Po prawej sieć z cyklem P1→P2→P3→P1 — wymagana na 500 punktów.)

## Punktacja (z edycji 2025/26)

| Maks. punktów | Co musi umieć proxy |
|---|---|
| **150** | łączy serwery z klientami (bez innych proxy), **jeden** protokół (TCP **albo** UDP) |
| **300** | łączy serwery z klientami (bez innych proxy), **oba** protokoły |
| **300** | serwery + **inne proxy**, sieć to **drzewo** (bez cykli), **jeden** protokół |
| **400** | serwery + inne proxy, **drzewo**, **oba** protokoły |
| **500** | serwery + inne proxy, sieć **dowolna (z cyklami)**, **oba** protokoły |

:::danger Ważna zasada z szablonu dokumentacji
Projekt deklarujesz „na” konkretną liczbę punktów i jest testowany **tylko** na nią. Jeśli projekt zgłoszony na 400 punktów nie spełni **wszystkich** wymagań dla 400 — dostaje **0**, a nie 300. Lepiej zadeklarować mniej i mieć pewność.
:::

## Wymagania formalne

Ze specyfikacji i z szablonu dokumentacji (edycja 2025/26):

| Wymaganie | Szczegóły |
|---|---|
| Język | **Java 8 (JDK 1.8)**, tylko standardowe klasy do komunikacji sieciowej |
| Pliki | tylko **Twoje** `.java`, skrypty uruchomieniowe (opcjonalnie) i **PDF** z dokumentacją |
| Nie dołączaj | dostarczonych serwerów i klientów, projektu IntelliJ |
| Pakiety | pliki `.java` **bez** deklaracji `package` |
| Kompilacja | automatycznie poleceniem `javac *.java`, uruchomienie skryptem/skryptami |
| Dokumentacja | plik **„Dokumentacja (nr indeksu).pdf”** — **warunek konieczny** oceny |
| Testy | wszystko na **localhost**, na jednym komputerze; nie hardkoduj ścieżek, IP, portów |
| Skrypt testowy | system (Windows/Linux) wybiera prowadzący ćwiczenia |
| Termin | w GAKKO; w edycji 2025/26: **19.12.2025, 23:55** (mógł go zmienić prowadzący grupy) — zobacz [Zaliczenie](course.html) |
| Ocena | najpierw poprawność i zgodność ze specyfikacją, potem jakość kodu i inżynieria oprogramowania |
| Niejasności | wyjaśniaj z prowadzącym — inaczej ryzyko niezaliczenia |
| Plagiat | projekty „splagiatowane” nie są sprawdzane, a za plagiat są **punkty ujemne** |

## Plan budowy proxy krok po kroku [[own]]

Najbezpieczniej budować projekt **etapami** — każdy etap to działający program na określoną liczbę punktów.

:::steps
1. **Parametry.** Wczytaj `-port` i listę `-server adres port`. Sprawdź liczby i kompletność; błąd → komunikat i `System.exit(1)`.
2. **Nasłuch na TCP i UDP** na tym samym numerze portu (to dwa różne porty — patrz [C5](c5-tcp-udp.html)). Każde żądanie w osobnym wątku.
3. **Jeden handler komend** — ta sama funkcja dostaje linię tekstu i zwraca linię odpowiedzi, niezależnie od tego, czy przyszła przez TCP, czy UDP.
4. **Wykrywanie serwerów** i zbudowanie **tablicy kluczy**: nazwa → (adres, port, protokół, rodzaj węzła).
5. **GET NAMES / GET VALUE / SET / QUIT** dla serwerów podanych w parametrach → to już jest **150–300 punktów**.
6. **Rozmowa z innymi proxy**: zaprojektuj własne komendy (przedstawienie się, wymiana listy kluczy, przekazanie żądania). Sieć w kształcie **drzewa** → **300–400 punktów**.
7. **Cykle**: identyfikator żądania + pamięć „już widziałem” + limit przeskoków → **500 punktów**.
8. **Dokumentacja** protokołu (niżej) i **skrypty testowe**.
:::

### Cegiełka 1: nasłuch TCP i UDP naraz

@code(project/frag-listeners.java, download=no, title=fragment — nasłuch TCP + UDP)

### Cegiełka 2: zapytaj węzeł i nie zawieś się

Każde wyjście do innego węzła **musi mieć timeout** — inaczej jeden martwy węzeł zawiesi całe proxy.

@code(project/frag-ask-tcp.java, download=no, title=fragment — zapytanie TCP z timeoutem)

@code(project/frag-ask-udp.java, download=no, title=fragment — zapytanie UDP z timeoutem)

### Cegiełka 3: wykryj protokół i rodzaj węzła (pseudokod)

@code(project/frag-detect.txt, download=no, lang=text, title=pseudokod — wykrywanie)

### Cegiełka 4: przekazywanie bez zapętlenia (pseudokod, 500 pkt)

@code(project/frag-forward.txt, download=no, lang=text, title=pseudokod — cykle)

## Pułapki dostarczonych programów (sprawdzone na ich kodzie) [[own]]

| Pułapka | Skąd się bierze | Co zrobić |
|---|---|---|
| UDP: `GET NAMES` wysłane **bez spacji na końcu** daje `NA` | `UDPServer` czyta Scannerem **cały 256-bajtowy bufor**, więc ostatnie słowo skleja się z bajtami zerowymi (`NAMES\0\0…`). Dlatego `UDPClient` dokleja spację. Sprawdziłem to: bez spacji odpowiedź to `NA`, ze spacją `OK 1 b`. | wysyłając do serwera UDP, dodaj na końcu spację (albo `\n`) |
| UDP: odpowiedź **bez końca linii** | `UDPServer` wysyła `output.getBytes()` | czytaj `new String(data, 0, getLength())` i `trim()` |
| UDP: bufor klienta **256 B** | `UDPClient` odbiera do `new byte[256]` | odpowiedź `GET NAMES` dla klienta UDP nie może być dłuższa (licz długość!) |
| TCP: klient czyta **jedną linię** | `TCPClient` robi jedno `readLine()` | odpowiedź = jedna linia zakończona `\n` (`println`) |
| `QUIT` bez odpowiedzi | oba klienty po `QUIT` nic nie czytają | proxy **nie** może czekać na odpowiedź po `QUIT` |
| niepełna komenda **zabija serwer** | serwery używają `Scanner.next()`/`nextInt()` i łapią tylko `IOException`; np. `SET a x` daje `InputMismatchException` | **sprawdzaj komendy** przed przekazaniem; nigdy nie wysyłaj „połówek” komend |
| serwer TCP obsługuje klientów **po kolei** | jedna pętla `accept` | krótkie połączenia, zawsze zamykaj gniazdo po odpowiedzi |

## Dodatkowe porady [[own]]

- **Licznik w `GET NAMES`**: `OK 3 a b c` — liczba musi być równa liczbie nazw, a każda nazwa ma się pojawić **raz** (w sieci z cyklem łatwo o duplikaty — używaj `Set`).
- **Wątki i współdzielone dane**: tablica kluczy jest czytana i zmieniana przez wiele wątków → `ConcurrentHashMap`, `ConcurrentHashMap.newKeySet()`.
- **Zakleszczenie w cyklu**: jeśli P1 czeka na odpowiedź P2, a P2 w tym samym czasie pyta P1 — przy obsłudze w **jednym** wątku obie strony czekają w nieskończoność. Każde żądanie obsługuj w osobnym wątku i ustawiaj timeouty.
- **Kolejność uruchamiania**: proxy może wystartować przed serwerem — ponawiaj wykrywanie co jakiś czas (jak w [C1](c1-relay.html)).
- **Logi**: wypisuj każdą wysłaną i odebraną wiadomość z nazwą węzła — przy 5 oknach to jedyny sposób, żeby coś zrozumieć.
- **Nie wymyślaj zbyt sprytnego protokołu**: prosty, dobrze opisany protokół jest lepszy niż sprytny i niejasny — za opis protokołu też się traci punkty.

## Skrypty testowe

Skrypty zakładają, że w folderze są dostarczone `TCPServer`, `UDPServer`, `TCPClient`, `UDPClient` i **Twój** `Proxy`. Porty możesz zmienić.

@code(project/test-tree.sh)

@code(project/test-tree.bat)

@code(project/test-cycle.sh)

Lista przypadków do sprawdzenia:

- [ ] `GET NAMES` z każdego proxy daje **wszystkie** klucze, każdy raz, z poprawnym licznikiem,
- [ ] `GET VALUE` dla klucza na serwerze TCP i na serwerze UDP, pytając przez TCP i przez UDP,
- [ ] `SET`, a potem `GET VALUE` z **innego** proxy pokazuje nową wartość,
- [ ] nieznany klucz → `NA`, błędna komenda → `NA`,
- [ ] w sieci z cyklem nic się nie zapętla (proxy nie „mieli” procesora, logi się kończą),
- [ ] `QUIT` zatrzymuje wszystkie proxy i serwery,
- [ ] złe parametry `Proxy` → błąd i koniec programu.

## Dokumentacja — jak ją napisać

Najważniejsza część dokumentacji to **opis protokołu**. Szablon prowadzącego mówi wprost: opis ma pozwolić komuś **napisać własne proxy w innym języku**, a jedyne, co będzie wspólne, to rozmowa przez sieć. Wzorem jest dokument **RFC**. **Nie opisujemy klas, metod ani zmiennych Javy.**

@code(project/doc-outline.txt, download=no, lang=text, title=konspekt dokumentacji)

:::own Jak opisać jeden komunikat (wzór, wypełnij swoimi komendami)
| Pole opisu | Co wpisać |
|---|---|
| Nazwa | np. „przedstawienie się proxy” |
| Kto → do kogo, jakim transportem | proxy → proxy, TCP |
| Dokładny format | słowa, kolejność pól, separator (spacja), koniec linii |
| Znaczenie pól | co oznacza każde pole, zakres wartości |
| Możliwe odpowiedzi | format odpowiedzi i ich znaczenie |
| Błędy i timeouty | co robi odbiorca przy złym formacie, co robi nadawca bez odpowiedzi |
| Przykład | jedna linia wysłana i jedna odebrana |
:::

## Sprawdź się [[own]]

:::check Dlaczego proxy nie może po prostu zapamiętać wartości kluczy?
Bo specyfikacja tego zabrania — wartości są tylko na serwerach (a `SET` zmienia je w jednym miejscu). Zapamiętana kopia szybko byłaby nieaktualna.
:::

:::check Klient UDP pyta proxy o klucz, który jest na serwerze TCP. Co robi proxy?
Odbiera datagram, rozpoznaje komendę, zagląda do tablicy, łączy się przez **TCP** z właściwym serwerem (albo przekazuje dalej do proxy), dostaje linię odpowiedzi i odsyła ją **datagramem UDP** na adres i port klienta.
:::

:::check Jak uniknąć nieskończonego krążenia żądania w sieci z cyklem?
Każde żądanie dostaje unikalny identyfikator; proxy pamięta identyfikatory już obsłużonych żądań i drugi raz odpowiada `NA` (nie przekazuje dalej). Dodatkowo limit przeskoków (TTL).
:::

:::cheatsheet
### Komendy bazowe
`GET NAMES` → `OK n k1 … kn` · `GET VALUE k` → `OK v`/`NA` · `SET k v` → `OK`/`NA` · `QUIT` → (nic) · inne → `NA`.

### Uruchomienie
`java Proxy -port P -server host port [-server host port ...]` — zły parametr → błąd + exit.

### Punkty
150 (1 protokół, bez proxy) · 300 (2 protokoły, bez proxy) · 300 (1 protokół, drzewo) · 400 (2 protokoły, drzewo) · 500 (2 protokoły, cykle). Deklarujesz → musi działać **wszystko** albo 0.

### Architektura
1. parametry → 2. nasłuch TCP+UDP (wątki) → 3. jeden handler komend → 4. wykrywanie + tablica kluczy → 5. przekazywanie z timeoutem → 6. protokół proxy–proxy → 7. ID + seen + TTL (cykle) → 8. QUIT do wszystkich.

### Pułapki
- UDP do `UDPServer`: **spacja na końcu** komendy.
- odpowiedzi: jedna linia; UDP ≤ 256 B.
- `QUIT`: bez czekania na odpowiedź.
- sprawdzaj komendy (niepełne zabijają serwer).
- `GET NAMES`: unikalne nazwy + poprawny licznik.
- timeouty wszędzie; `ConcurrentHashMap`.

### Oddanie
`javac *.java` bez pakietów · tylko Twoje .java + skrypty + „Dokumentacja (nr indeksu).pdf” · localhost · bez hardkodowanych IP/portów · GAKKO przed terminem.
:::
