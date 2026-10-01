---
title: Jak zaliczyć SKJ — elementy, zasady i daty
short: Zaliczenie i daty
description: Z czego składa się przedmiot, co wiadomo na pewno z materiałów (projekt 500 pkt, kolokwium z serwerem, dokumentacja, antyplagiat), czego materiały nie mówią i jaki plan nauki proponuję.
kind: mixed
source: specyfikacja projektu 2025/26, szablon dokumentacji, szablon testu i wykłady z repozytorium; plan i porady — opracowanie własne
---

## W skrócie

- Przedmiot: **SKJ — Sieci komputerowe i programowanie sieciowe w Javie** (*Computer Networks and Network Programming in Java*). Wykłady prowadzi **dr inż. Kazimierz Zieliński**, na podstawie podręcznika Kurose & Ross.
- Z materiałów wynikają dwa główne elementy praktyczne: **kolokwium z serwerem** (zadania przez TCP) i **projekt** (sieć proxy, do **500 pkt**).
- Projekt **bez dokumentacji PDF nie jest oceniany**; za **plagiat** są **punkty ujemne**.
- Daty w materiałach dotyczą edycji **2025/2026** — są już **archiwalne**. Nowe terminy ogłosi prowadzący.

@dates

:::warning Aktualność dat
Dzisiejszy status (minęło / za ile dni) liczy się automatycznie w Twojej przeglądarce. Wszystkie powyższe daty pochodzą z plików poprzedniej edycji, więc najpewniej **nie dotyczą** Twojego semestru — traktuj je jako orientację, kiedy mniej więcej co się dzieje. [[own]]
:::

## Elementy przedmiotu

| Element | Co to jest | Szczegóły na stronie |
|---|---|---|
| **Wykłady** | rozdział 1 Kurose & Ross: Internet, protokoły, brzeg i rdzeń sieci, opóźnienia, bezpieczeństwo, warstwy, historia | [W1–W8](w1-internet.html) |
| **Ćwiczenia** | Wireshark (HTTP, DNS, TCP) + programy w Javie z gniazdami TCP/UDP | [C1–C7](c1-relay.html) |
| **Kolokwium** | klient TCP rozwiązujący zadania wysyłane przez serwer prowadzącego; każde zadanie kilka razy | [Kolokwium](exam.html) |
| **Projekt** | sieć proxy łącząca serwery kluczy TCP/UDP; dokumentacja protokołu | [Projekt](project.html) |

## Co wiadomo na pewno (z materiałów)

### Projekt (edycja 2025/26)

- Pełny, poprawny projekt jest wart **500 punktów**; progi: 150 / 300 / 300 / 400 / 500 (szczegóły w [Projekcie](project.html)).
- Projekt **deklarujesz** na konkretną liczbę punktów i jest testowany tylko na nią — jeśli nie spełnia **wszystkich** wymagań dla tej liczby, dostaje **0**.
- Java 8, tylko standardowe klasy; pliki bez pakietów; kompilacja `javac *.java`; testy na localhost.
- Oddajesz w **GAKKO**: swoje `.java`, opcjonalnie skrypty, **„Dokumentacja (nr indeksu).pdf”** (warunek konieczny oceny).
- Termin w specyfikacji: **19.12.2025, 23:55** (mógł go zmienić prowadzący grupy).
- Antyplagiat: projekty „splagiatowane” **nie są sprawdzane**, za plagiat są **ujemne punkty**. Prowadzący pisze wprost, że nie ma znaczenia, czy autorem był kolega, AI czy StackOverflow — liczy się samodzielna praca.

### Kolokwium praktyczne (edycja 2025/26)

- Łączysz się z serwerem prowadzącego, logujesz się, dostajesz **hasło** i **opis zadań**.
- **5 zadań**, każde trzeba wykonać **5 razy**; serwer od razu ocenia odpowiedzi.
- Opis zawierał **godzinę zakończenia testu** (w edycji 2025/26: **11.01.2026, 12:45**) — po niej zmiany nie są uwzględniane.

### Ćwiczenia z Wiresharkiem

- Laboratoria Kurose & Ross: **HTTP**, **DNS**, **TCP** — odpowiedzi na pytania na podstawie własnego przechwycenia ruchu (albo gotowych plików *trace*).

## Czego materiały NIE mówią — zapytaj prowadzącego

:::danger Brak danych w repozytorium [[own]]
W plikach nie ma informacji o:

- **skali ocen** i progu zaliczenia (ile punktów na 3,0 / 5,0),
- **wadze** kolokwium względem projektu,
- tym, czy jest **egzamin / test z wykładów** i jak wygląda,
- zasadach **obecności** i **poprawkach**.

Nie zgaduję tych rzeczy — sprawdź je w sylabusie, w GAKKO albo zapytaj prowadzącego na pierwszych zajęciach. Jeśli jest test z wykładów, przyda się przycisk **Konspekt** w każdym temacie W1–W8 i strona [Wszystkie konspekty](cheatsheets.html).
:::

## Proponowany plan semestru [[own]]

Daty z poprzedniej edycji sugerują mniej więcej taki rytm: laboratoria od października, projekt do połowy grudnia, kolokwium w styczniu. Na tej podstawie proponuję:

:::steps
1. **Październik** — wykłady W1–W8 (po 1–2 tematy tygodniowo) + [C1](c1-relay.html), [C2](c2-http.html), [C3](c3-tcp-server.html). Zainstaluj JDK i Wiresharka ([Start](start.html)).
2. **Listopad** — [C4](c4-text-protocol.html), [C5](c5-tcp-udp.html), [C6](c6-dns.html), [C7](c7-tcp-wireshark.html). Zacznij trenować z [DrillServer](exam.html) — raz w tygodniu, na czas.
3. **Od momentu publikacji specyfikacji projektu** — od razu zbuduj etap na 150 pkt, potem kolejne. Dokumentację pisz **równolegle**, nie w ostatnią noc.
4. **Tydzień przed terminem projektu** — testy na **innym komputerze**, `javac *.java` w pustym folderze, PDF, wysyłka w GAKKO.
5. **Przed kolokwium** — 2–3 pełne treningi `DrillServer` z 5 powtórzeniami; gotowy szablon klienta.
:::

## Lista kontrolna przed oddaniem projektu [[own]]

- [ ] w folderze są **tylko** moje pliki `.java`, skrypty i PDF (bez `.class`, bez projektu IntelliJ, bez dostarczonych serwerów/klientów),
- [ ] żaden plik nie ma linii `package ...`,
- [ ] `javac *.java` w **pustym** folderze kompiluje się bez błędów (najlepiej `javac --release 8 *.java`),
- [ ] brak zahardkodowanych adresów IP, portów, ścieżek,
- [ ] wszystkie wymagania dla **zadeklarowanej** liczby punktów działają,
- [ ] PDF ma nazwę z numerem indeksu i zawiera **pełny opis protokołu**,
- [ ] w PDF wypełniona tabelka z szablonu (autor, indeks, punkty, skrypt testowy),
- [ ] wysłane w GAKKO **przed** terminem.

:::cheatsheet
### Elementy
- Wykłady (Kurose & Ross, rozdz. 1) · Ćwiczenia (Wireshark + Java) · Kolokwium z serwerem · Projekt proxy (do 500 pkt).

### Pewne zasady (2025/26)
- Projekt: deklarujesz punkty → wszystko musi działać, inaczej 0.
- Dokumentacja PDF = warunek oceny; opis protokołu jak RFC.
- Java 8, bez pakietów, `javac *.java`, localhost, bez hardkodowania.
- Plagiat → ujemne punkty.
- Kolokwium: 5 zadań × 5 powtórzeń, twardy termin.

### Daty 2025/26 (archiwalne)
- Projekt: 19.12.2025, 23:55 (GAKKO).
- Koniec testu: 11.01.2026, 12:45.

### Zapytaj prowadzącego
- skala ocen i próg · waga kolokwium/projektu · egzamin z wykładów? · obecności i poprawki · aktualne terminy.
:::
