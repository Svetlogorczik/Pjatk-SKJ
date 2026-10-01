---
title: Pierwszy program rozproszony — sztafeta procesów w pierścieniu
short: Sztafeta procesów (pierwsze uruchomienie)
description: Uruchamiasz kilka procesów, które rozmawiają ze sobą przez TCP. Poznajesz porty, localhost, pliki konfiguracyjne i typowe błędy „Connection refused” oraz „Address already in use”.
kind: lab
source: logika ćwiczeń 1 (uruchomienie gotowego programu rozproszonego z plikami .prop) — zadanie przerobione
---

## W skrócie

- Na pierwszych zajęciach chodzi o to, żeby **uruchomić kilka procesów naraz** i zobaczyć, że rozmawiają ze sobą przez sieć — nawet na jednym komputerze.
- Każdy proces ma **swój port** (swoje „drzwi”) i wie, **pod jaki adres i port** wysyłać dane dalej. To zapisujemy w **pliku konfiguracyjnym**.
- Najpierw trzeba **otworzyć drzwi** (`ServerSocket`), dopiero potem ktoś może do nich zapukać (`Socket`).
- Nasza wersja: trzy węzły A → B → C → A tworzą **pierścień** i przekazują sobie „pałeczkę” (*token*) jak w sztafecie.

:::origin
Na ćwiczeniach 1 uruchamiało się gotowy program złożony z kilku okienek, połączonych przez TCP w łańcuch; każde okienko czytało z pliku `.prop` adresy i porty sąsiadów. Poniżej jest **nasza, przerobiona wersja** tego samego pomysłu — konsolowa, z własnym kodem, w którym widać każdy krok.
:::

## Słowniczek na start

| Pojęcie | Co to znaczy | Przykład |
|---|---|---|
| **proces** | uruchomiony program | `java RelayNode relay-a.properties` |
| **adres IP** | „adres domu” komputera | `127.0.0.1`, `192.168.1.15` |
| **localhost** | „ten sam komputer” — zawsze `127.0.0.1` | `nextHost=localhost` |
| **port** | numer „drzwi” programu na komputerze (0–65535) | `7001` |
| **gniazdo** (*socket*) | końcówka połączenia: adres IP + port | `127.0.0.1:7001` |
| **serwer** | proces, który **czeka** na połączenia (`ServerSocket`) | węzeł nasłuchuje na 7002 |
| **klient** | proces, który **łączy się** z serwerem (`Socket`) | węzeł A łączy się z B |

:::own Ważne: jeden proces może być naraz serwerem i klientem
W pierścieniu **każdy** węzeł jest serwerem (czeka na pałeczkę od poprzednika) **i** klientem (wysyła ją następnemu). Tak samo będzie w projekcie: proxy jest serwerem dla klientów i klientem dla serwerów.
:::

## Jak działa nasza sztafeta

```text
        +-------------------+
        v                   |
   [A :7001] --> [B :7002] --> [C :7003]
   starter
```

:::steps
1. Każdy węzeł otwiera swój port (`ServerSocket`) i czeka.
2. Węzeł **A** (starter) wysyła do B linię `TOKEN 1 0 A` (runda 1, 0 przeskoków, ścieżka „A”).
3. Każdy węzeł po odebraniu linii: zwiększa licznik przeskoków, dopisuje swoją nazwę do ścieżki i wysyła dalej.
4. Gdy pałeczka wróci do A, A wypisuje podsumowanie rundy i zaczyna następną.
5. Po ostatniej rundzie A wysyła `STOP`, który obiega pierścień — każdy węzeł kończy pracę.
:::

**Protokół** (format, kolejność, akcje — patrz [W1](w1-internet.html)):

| Komunikat | Format | Akcja odbiorcy |
|---|---|---|
| pałeczka | `TOKEN <runda> <przeskoki> <ścieżka>` | +1 przeskok, dopisz swoją nazwę, wyślij dalej |
| koniec | `STOP` | wyślij `STOP` dalej i zakończ (starter tylko kończy) |

Każde połączenie TCP przenosi **dokładnie jedną linię**, potem jest zamykane.

## Pliki konfiguracyjne

Zamiast wpisywać porty w kodzie, każdy węzeł czyta je z pliku `.properties` (format `klucz=wartość`, `#` to komentarz):

@code(c1/relay-a.properties)

@code(c1/relay-b.properties)

@code(c1/relay-c.properties)

:::tip Dlaczego plik, a nie stałe w kodzie?
Ten sam program uruchamiasz 3 razy z różnymi plikami — i dostajesz 3 różne węzły. A w projekcie prowadzący wyraźnie zabrania **hardkodowania adresów i portów** (szablon dokumentacji, uwaga 12). [[own]]
:::

## Kod węzła

@code(c1/RelayNode.java)

Najważniejsze miejsca:

- `new ServerSocket(listenPort)` — otwieramy drzwi. Jeśli port jest zajęty, poleci `BindException: Address already in use`.
- `server.accept()` — **czekamy** (program „stoi”), aż ktoś się połączy.
- `in.readLine()` — czytamy **całą linię** zakończoną `\n`.
- `new Socket(nextHost, nextPort)` — łączymy się z następnym węzłem. Jeśli tam nikt nie nasłuchuje, poleci `ConnectException: Connection refused` — dlatego mamy **ponawianie** co 0,5 s.
- `PrintWriter(..., true)` — `true` oznacza automatyczne `flush()` po `println`. Bez flush dane mogą utknąć w buforze!

## Uruchomienie krok po kroku

:::steps
1. Pobierz `RelayNode.java` i trzy pliki `.properties` do **jednego folderu** (przyciski „Pobierz” wyżej).
2. Otwórz terminal w tym folderze i skompiluj: `javac RelayNode.java`.
3. Uruchom **trzy terminale** (albo użyj skryptu niżej).
4. W pierwszym: `java RelayNode relay-b.properties`, w drugim: `java RelayNode relay-c.properties`.
5. Na końcu w trzecim: `java RelayNode relay-a.properties` — A zaczyna sztafetę.
6. Obserwuj logi: `->` to wysłane, `<-` to odebrane linie.
:::

@code(c1/run-relay.bat)

@code(c1/run-relay.sh)

Oczekiwany wynik u węzła A (fragment):

```text
[A] listening on port 7001, next node = localhost:7002
[A] -> TOKEN 1 0 A
[A] <- TOKEN 1 2 A>B>C   (from 127.0.0.1:53318)
[A] round 1 finished: 3 hops, path A>B>C>A
...
[A] -> STOP
[A] <- STOP   (from 127.0.0.1:53340)
[A] STOP received - bye
```

:::own Zauważ port klienta
W logu `from 127.0.0.1:53318` port **53318** nie jest żadnym z naszych portów. To **port efemeryczny** — system losuje go dla każdego nowego połączenia po stronie klienta. Port „na stałe” ma tylko strona, która **nasłuchuje**.
:::

## Typowe błędy i co znaczą [[own]]

| Komunikat | Przyczyna | Co zrobić |
|---|---|---|
| `java.net.ConnectException: Connection refused` | pod tym adresem:portem **nikt nie nasłuchuje** (węzeł nie wystartował albo zły port) | uruchom najpierw „serwery”; sprawdź port w pliku |
| `java.net.BindException: Address already in use` | port jest **zajęty** — np. poprzednie uruchomienie wciąż działa | zamknij stary proces albo zmień port |
| `java.net.UnknownHostException` | nazwa hosta nie istnieje (literówka) | popraw `nextHost` |
| `NumberFormatException` | w pliku jest zły numer portu lub spacja/litera | popraw plik `.properties` |
| okienko „Zapora systemu Windows” | Windows pyta o zgodę na sieć dla Javy | zezwól (sieć prywatna) |
| program „wisi” bez błędu | czeka w `accept()` albo `readLine()` — druga strona nic nie wysłała | sprawdź kolejność uruchamiania i `flush()` |

Przydatne polecenia — kto zajmuje port?

```bash
# Windows
netstat -ano | findstr 7001
# Linux
ss -ltnp | grep 7001
# macOS
lsof -i :7001
```

## Zadania do samodzielnego wykonania

::::task 1. Dodaj czwarty węzeł D
Zmień **tylko pliki konfiguracyjne** (nie kod!), tak żeby pierścień miał postać A → B → C → D → A.

:::answer
Utwórz `relay-d.properties` z `name=D`, `listenPort=7004`, `nextPort=7001`. W `relay-c.properties` zmień `nextPort=7004`. Uruchom B, C, D, a na końcu A. W logu A zobaczysz `4 hops, path A>B>C>D>A`.
:::
::::

::::task 2. Pierścień na dwóch komputerach
Z kolegą/koleżanką: węzły A i B u Ciebie, C u drugiej osoby.

:::hint
Sprawdź swój adres IP: `ipconfig` (Windows) albo `ip a` (Linux) / `ifconfig` (macOS). W pliku zamiast `localhost` wpisz adres IP drugiego komputera. Oba komputery muszą być w tej samej sieci, a zapora musi przepuszczać porty 7001–7003.
:::
::::

::::task 3. Zepsuj i obserwuj
a) Usuń ponawianie z metody `send` i uruchom A jako pierwszy. b) Uruchom dwa razy `relay-b.properties`. Jakie wyjątki zobaczysz i dlaczego?

:::answer
a) `ConnectException: Connection refused` — B jeszcze nie nasłuchuje. b) Drugi B dostanie `BindException: Address already in use` — port 7002 jest już zajęty przez pierwszy proces.
:::
::::

::::task 4. Pałeczka z ładunkiem
Niech każdy węzeł dopisuje do komunikatu losową liczbę 1–9, a starter po powrocie pałeczki wypisze ich **sumę**. Zaprojektuj format komunikatu tak, żeby był jednoznaczny.

:::answer
Np. `TOKEN <runda> <przeskoki> <ścieżka> <suma>`: każdy węzeł robi `suma += los`, a starter wypisuje ostatnie pole. Ważne: pola rozdzielone **spacją**, ścieżka nie może zawierać spacji (dlatego `A>B>C`).
:::
::::

:::cheatsheet
### Pojęcia
- Port = „drzwi” procesu (0–65535). Gniazdo = IP + port.
- `localhost` = `127.0.0.1` = ten sam komputer.
- Serwer: `new ServerSocket(port)` → `accept()` (czeka).
- Klient: `new Socket(host, port)` → łączy się.
- Port klienta jest losowy (efemeryczny).

### Kolejność uruchamiania
1. Najpierw procesy, które **nasłuchują**.
2. Potem ten, który **zaczyna** wysyłać.
3. Albo: ponawiaj połączenie co kilkaset ms.

### Konfiguracja
- `Properties p = new Properties(); p.load(new FileInputStream(plik));`
- `p.getProperty("listenPort")` → `Integer.parseInt(...)`.
- Nigdy nie hardkoduj IP i portów.

### Błędy
- `Connection refused` → nikt nie nasłuchuje.
- `Address already in use` → port zajęty.
- „Wisi” → czeka na `accept()`/`readLine()`; sprawdź `flush()`.
- Kto zajmuje port: `netstat -ano | findstr PORT` / `ss -ltnp` / `lsof -i :PORT`.
:::
