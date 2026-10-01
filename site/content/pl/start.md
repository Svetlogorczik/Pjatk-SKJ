---
title: Zacznij tutaj — jak korzystać ze strony i przygotować komputer
short: Start i środowisko
description: Jak zbudowana jest strona i jak się uczyć z niej najskuteczniej, a także instalacja JDK i Wiresharka, uruchamianie programów z terminala, kilka okien naraz i zgodność z Javą 8.
kind: own
source: opracowanie własne
---

## Jak czytać tę stronę

@legend

- **Wykłady (W1–W8)** — cały materiał z wykładów, opisany zwykłym językiem, z przykładami i wzorami.
- **Ćwiczenia (C1–C7)** — zadania z zajęć **przerobione** tak, żeby logika była ta sama, ale treść i kod — inne (nie kopiujesz prac z poprzednich lat).
- **Kolokwium** i **Projekt** — co trzeba umieć i jak się przygotować.
- Przycisk **📝 Konspekt** w każdym temacie otwiera krótką ściągę „krok po kroku”. Wszystkie naraz (do druku) są na stronie [Wszystkie konspekty](cheatsheets.html).
- Przycisk **✓ Oznacz jako przerobione** zapisuje postęp w Twojej przeglądarce (pasek w menu).
- Przy każdym bloku kodu jest **Kopiuj**, a przy plikach — **Pobierz**.
- W prawym górnym rogu: **język** (PL / EN / RU) i **motyw** (jasny / ciemny).

## Proponowana kolejność nauki

:::steps
1. Przeczytaj [Zaliczenie i daty](course.html) — żeby wiedzieć, o co toczy się gra.
2. Przygotuj komputer (niżej).
3. Wykłady [W1](w1-internet.html) → [W8](w8-history.html) — po każdym kliknij **Konspekt** i odpowiedz na „Sprawdź się”.
4. Ćwiczenia [C1](c1-relay.html) → [C7](c7-tcp-wireshark.html) — **uruchamiaj** każdy program, nie tylko czytaj.
5. [Kolokwium](exam.html) — trening z serwerem DrillServer.
6. [Projekt](project.html) — etapami, od 150 punktów w górę.
:::

## Instalacja Javy (JDK)

Potrzebujesz **JDK** (nie samego JRE) — w nim jest kompilator `javac`.

:::steps
1. Zainstaluj JDK (np. Eclipse Temurin / OpenJDK). Wersja 8, 11, 17 lub 21 — każda wystarczy, ale kod pisz zgodny z **Javą 8** (niżej).
2. Sprawdź w terminalu: `java -version` i `javac -version`.
3. Jeśli `javac` „nie jest rozpoznany” — dodaj folder `bin` z JDK do zmiennej `PATH` (Windows: *Zmienne środowiskowe*).
:::

### Kompilacja i uruchamianie z terminala

```bash
cd folder-z-plikami
javac MiniWebServer.java          # powstaje MiniWebServer.class
java MiniWebServer 8080           # uruchamiasz KLASĘ (bez .class), argumenty po spacji
javac *.java                      # wszystkie pliki naraz (tak kompiluje prowadzący projekt!)
javac --release 8 *.java          # sprawdzenie, czy kod jest zgodny z Javą 8
```

### Kilka programów naraz

Programy sieciowe prawie zawsze uruchamiasz **w kilku terminalach** (serwer + klient, kilka węzłów).

| System | Jak otworzyć kolejne okno |
|---|---|
| Windows | nowe okno `cmd`/PowerShell albo w skrypcie `.bat`: `start "nazwa" cmd /k java Program` |
| Linux / macOS | nowa karta terminala albo w skrypcie: `java Program &` (w tle) |
| IntelliJ | kilka konfiguracji uruchomieniowych; **Run → Edit Configurations → Program arguments** dla argumentów; opcja *Allow multiple instances* |

## Zgodność z Javą 8

Projekt musi być w **Javie 8**. Te konstrukcje są **nowsze** — nie używaj ich:

| Nie używaj | Od wersji | Zamiast tego (Java 8) |
|---|---|---|
| `var x = ...` | 10 | jawny typ: `String x = ...` |
| `List.of(...)`, `Map.of(...)` | 9 | `Arrays.asList(...)`, `new HashMap<>()` + `put` |
| `"ab".repeat(3)` | 11 | pętla / `StringBuilder` |
| `str.isBlank()`, `strip()` | 11 | `trim().isEmpty()`, `trim()` |
| `list.getFirst()`, `getLast()` | 21 | `list.get(0)`, `list.get(list.size() - 1)` |
| bloki tekstowe `"""..."""` | 15 | zwykłe stringi z `\n` |
| `record`, `switch` ze strzałkami | 14–16 | klasa, klasyczny `switch` z `break` |

`javac --release 8` od razu pokaże takie błędy, nawet jeśli masz nowszy JDK.

## Wireshark

:::steps
1. Pobierz Wiresharka ze strony projektu i zainstaluj. Na **Windows** zaznacz instalację **Npcap** (z obsługą ruchu **loopback**, żeby widzieć `localhost`).
2. Na **Linuksie** dodaj użytkownika do grupy `wireshark` (albo uruchamiaj przez `sudo`).
3. Po uruchomieniu wybierz interfejs: **Wi-Fi / Ethernet** dla Internetu, **Loopback / lo** dla programów na `localhost`.
4. Start (niebieska płetwa) → zrób coś w sieci → Stop (czerwony kwadrat) → wpisz filtr, np. `http`.
:::

## Przydatne narzędzia w konsoli

| Narzędzie | Do czego | Przykład |
|---|---|---|
| `ping` | czy host odpowiada, RTT | `ping pjwstk.edu.pl` |
| `tracert` / `traceroute` | trasa przez routery | `tracert pjwstk.edu.pl` |
| `nslookup` | zapytania DNS | `nslookup -type=MX pjwstk.edu.pl` |
| `curl -v` | żądanie HTTP z nagłówkami | `curl -v http://localhost:8080/` |
| `nc` / `telnet` | „ręczna” rozmowa TCP | `nc -C localhost 5050` |
| `netstat` / `ss` / `lsof` | kto zajmuje port | `netstat -ano \| findstr 8080` |
| `ipconfig` / `ip a` | mój adres IP | `ipconfig /all` |

:::tip Zapora sieciowa
Przy pierwszym uruchomieniu serwera Windows może zapytać o dostęp do sieci dla Javy — zezwól dla **sieci prywatnych**. Jeśli testujesz tylko na `localhost`, zapora zwykle nie przeszkadza.
:::

:::cheatsheet
### Strona
- 📝 Konspekt = ściąga tematu · ✓ = postęp · Kopiuj / Pobierz przy kodzie · PL/EN/RU · jasny/ciemny.
- ✍️ = dodatek autora strony · 🔁 = zadanie przerobione · 📘 = z wykładu.

### Java
- `javac Plik.java` → `java Plik arg1 arg2`
- `javac *.java` · `javac --release 8 *.java`
- unikaj: `var`, `List.of`, `repeat`, `isBlank`, `getFirst`, text blocks, records.

### Wiele okien
- Windows `.bat`: `start "nazwa" cmd /k java Program`
- Linux/mac: `java Program &`

### Wireshark
- Windows: Npcap + loopback · interfejs Loopback dla `localhost` · filtry: `http`, `dns`, `tcp.port == 8080`.

### Konsola
ping · tracert/traceroute · nslookup · curl -v · nc -C / telnet · netstat/ss/lsof · ipconfig / ip a.
:::
