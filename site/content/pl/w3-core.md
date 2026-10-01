---
title: Rdzeń sieci — przełączanie pakietów i przełączanie obwodów
short: Rdzeń sieci
description: Co dzieje się z pakietem w środku sieci — store-and-forward, kolejki i straty, forwarding vs routing — i dlaczego Internet wybrał pakiety, a nie „rezerwację linii” jak telefon.
kind: mixed
source: Wykład 1 — Chapter 1, slajdy 1-25…1-33 (Kurose & Ross)
---

## W skrócie

- **Rdzeń sieci** to siatka połączonych **routerów**.
- **Przełączanie pakietów**: host dzieli dane na pakiety, a routery przekazują je od jednego do drugiego, **pełną szybkością łącza**.
- **Store-and-forward**: router musi odebrać **cały pakiet**, zanim zacznie go wysyłać dalej.
- Gdy pakiety przychodzą szybciej, niż router może je wysłać — tworzy się **kolejka**, a gdy bufor się zapełni — pakiety **giną**.
- **Forwarding** = lokalna decyzja „którym wyjściem?”, **routing** = globalne wyznaczanie tras.
- **Przełączanie obwodów** (telefon) rezerwuje zasoby na całą rozmowę. Pakiety pozwalają obsłużyć **więcej użytkowników**, ale bez gwarancji.

## Rdzeń sieci: siatka routerów

Rdzeń sieci (*network core*) to **siatka** (*mesh*) połączonych routerów. Działa na zasadzie **przełączania pakietów** (*packet switching*):

- hosty dzielą komunikaty warstwy aplikacji na **pakiety**,
- routery **przekazują** pakiety od jednego routera do następnego, łączami na ścieżce od nadawcy do odbiorcy,
- każdy pakiet jest wysyłany z **pełną przepustowością łącza**.

## Store-and-forward („zapamiętaj i przekaż”)

Zasada z wykładu: **cały pakiet musi dotrzeć do routera, zanim router zacznie go wysyłać** następnym łączem.

Wypchnięcie pakietu $L$ bitów do łącza $R$ bit/s trwa $L/R$ sekund. Na drodze **nadawca → router → odbiorca** (dwa łącza o szybkości $R$, opóźnienie propagacji pomijamy):

$$
d_{end\text{-}end} = 2\,\frac{L}{R}
$$

:::example Przykład z wykładu (jeden przeskok)
$L = 10$ kbit, $R = 100$ Mb/s. Opóźnienie transmisji na jednym łączu:

$$
\frac{L}{R} = \frac{10\,000\ \text{bit}}{100\,000\,000\ \text{bit/s}} = 0{,}0001\ \text{s} = 0{,}1\ \text{ms}
$$
:::

:::own Uogólnienie: N łączy i P pakietów
To jest z podręcznika (nie ze slajdów), ale bardzo przydaje się w zadaniach:

- **jeden pakiet**, $N$ łączy (czyli $N-1$ routerów po drodze): $d = N \cdot \dfrac{L}{R}$,
- **$P$ pakietów** wysłanych jeden po drugim przez $N$ łączy: $d = (N + P - 1)\cdot \dfrac{L}{R}$.

Dlaczego? Pierwszy pakiet potrzebuje $N$ „taktów” po $L/R$, żeby dojść do celu. Każdy kolejny dochodzi **jeden takt później**, bo idą „gęsiego”.

| Czas | Pakiet 1 | Pakiet 2 | Pakiet 3 |
|---|---|---|---|
| $1\cdot L/R$ | w routerze | — | — |
| $2\cdot L/R$ | **u odbiorcy** | w routerze | — |
| $3\cdot L/R$ | | **u odbiorcy** | w routerze |
| $4\cdot L/R$ | | | **u odbiorcy** |

Dla $N = 2$, $P = 3$: $(2 + 3 - 1) \cdot L/R = 4\,L/R$ — zgadza się z tabelką.
:::

## Kolejki i utrata pakietów

Przykład z wykładu: hosty A i B wysyłają do routera łączami 100 Mb/s, a router ma do E łącze tylko **1,5 Mb/s**.

Jeśli przez jakiś czas **szybkość napływu** (bit/s) do łącza jest większa niż **szybkość transmisji** tego łącza:

- pakiety **ustawiają się w kolejce** i czekają na wysłanie,
- gdy pamięć (bufor) routera się zapełni, nowe pakiety są **odrzucane** — **giną** (*packet loss*).

:::own Analogia: kasa w sklepie
Kasjerka obsługuje 1 klienta na minutę. Jeśli przez 10 minut przychodzi 3 klientów na minutę, robi się kolejka. A gdy kolejka nie mieści się w sklepie, nowi klienci odchodzą — to jest „utrata pakietów”.
:::

O tym, ile trwa czekanie w kolejce, jest temat [W5](w5-performance.html).

## Dwie kluczowe funkcje rdzenia: forwarding i routing

| | **Forwarding** (przekazywanie) | **Routing** (wyznaczanie tras) |
|---|---|---|
| Zasięg | **lokalna** akcja w jednym routerze | **globalna** akcja dla całej sieci |
| Co robi | przenosi pakiet z łącza wejściowego na właściwe **łącze wyjściowe** | ustala **ścieżki** od nadawcy do odbiorcy |
| Jak | patrzy w **lokalną tablicę przekazywania** (*forwarding table*) | **algorytmy routingu** wypełniają te tablice |

Przykład tablicy przekazywania ze slajdu (wartość w nagłówku pakietu → numer łącza wyjściowego):

| Wartość w nagłówku | Łącze wyjściowe |
|---|---|
| 0100 | 3 |
| 0101 | 2 |
| 0111 | 2 |
| 1001 | 1 |

Router czyta **adres docelowy** z nagłówka przychodzącego pakietu, znajduje go w tablicy i wysyła pakiet odpowiednim wyjściem.

:::own Analogia i związek z projektem
- **Routing** = planowanie trasy w nawigacji przed wyjazdem. **Forwarding** = skręt na każdym skrzyżowaniu według tej trasy.
- W **projekcie** Twoje proxy też ma „tablicę routingu”: **nazwa klucza → gdzie go szukać** (serwer albo inne proxy). Obsługa komendy `GET VALUE x` to właśnie forwarding — patrzysz w tablicę i przekazujesz żądanie dalej. Zobacz [Projekt](project.html).
:::

## Alternatywa: przełączanie obwodów (circuit switching)

W **przełączaniu obwodów** zasoby end-to-end są **rezerwowane** na całe „połączenie” (*call*) między nadawcą i odbiorcą:

- na slajdzie każde łącze ma 4 obwody; połączenie dostaje np. 2. obwód na górnym łączu i 1. na prawym,
- zasoby są **dedykowane**, bez współdzielenia → **gwarantowana** wydajność, jak w obwodzie,
- jeśli połączenie nic nie wysyła, jego obwód **stoi bezczynnie** (nikt inny go nie użyje),
- tak działały **tradycyjne sieci telefoniczne**.

### FDM i TDM — jak podzielić łącze na obwody

- **FDM** (*Frequency Division Multiplexing*): pasmo częstotliwości dzieli się na **wąskie pasma**; każde połączenie dostaje **swoje pasmo** i może nadawać z maksymalną szybkością tego wąskiego pasma — ale **cały czas**.
- **TDM** (*Time Division Multiplexing*): czas dzieli się na **szczeliny** (*slots*); każde połączenie dostaje **okresowe szczeliny** i nadaje z maksymalną szybkością **szerokiego** pasma — ale **tylko w swoich szczelinach**.

:::own Zadanie typu „ile trwa wysłanie pliku w sieci z obwodami?”
Klasyczne zadanie z podręcznika: plik 640 000 bitów, łącze TDM 1,536 Mb/s z **24** szczelinami, zestawienie obwodu trwa 500 ms.

1. Szybkość jednego obwodu: $1{,}536\ \text{Mb/s} / 24 = 64\ \text{kb/s}$.
2. Czas wysyłania: $640\,000 / 64\,000 = 10$ s.
3. Plus zestawienie obwodu: $10 + 0{,}5 = 10{,}5$ s.
:::

## Pakiety kontra obwody — ilu użytkowników się zmieści?

Przykład z wykładu:

- łącze **1 Gb/s**,
- każdy użytkownik, gdy jest **aktywny**, nadaje **100 Mb/s**,
- użytkownik jest aktywny tylko **10%** czasu.

**Przełączanie obwodów:** każdy musi mieć zarezerwowane 100 Mb/s, więc zmieści się $1\ \text{Gb/s} / 100\ \text{Mb/s} = 10$ użytkowników.

**Przełączanie pakietów:** przy **35** użytkownikach prawdopodobieństwo, że **więcej niż 10** będzie aktywnych naraz (czyli że łącze się „zatka”), jest **mniejsze niż 0,0004**.

### Skąd 0,0004? [[own]]

Wykład zadaje to pytanie. Odpowiedź: to **rozkład dwumianowy**. Każdy z $N = 35$ użytkowników jest aktywny niezależnie z prawdopodobieństwem $p = 0{,}1$. Liczba aktywnych $X$ ma rozkład $B(35;\ 0{,}1)$:

$$
P(X = k) = \binom{35}{k}\, 0{,}1^{k}\, 0{,}9^{35-k}
$$

Kłopot zaczyna się, gdy aktywnych jest **więcej niż 10**:

$$
P(X > 10) = 1 - \sum_{k=0}^{10} \binom{35}{k}\, 0{,}1^{k}\, 0{,}9^{35-k} \approx 0{,}000424
$$

:::steps
1. Policz, ilu użytkowników mieści łącze naraz: $k = \lfloor C / r \rfloor = 10$.
2. Zapisz rozkład: $N = 35$ prób, sukces = „aktywny” z $p = 0{,}1$.
3. Policz $P(X \le 10)$ jako sumę 11 wyrazów (k = 0…10) — w praktyce kalkulatorem lub programem.
4. Odejmij od 1: $P(X > 10) \approx 0{,}000424$, czyli około 0,0004 — slajd po prostu zaokrągla tę wartość.
:::

A co, gdy użytkowników jest więcej niż 35 (kolejne pytanie z wykładu)? Prawdopodobieństwo przeciążenia szybko rośnie: 40 → ok. 0,0015; 50 → ok. 0,009; 70 → ok. 0,087; 100 → ok. 0,42. Sprawdź sam:

:::calc binom
:::

### Czy pakiety zawsze wygrywają?

Z wykładu:

- **Plusy** przełączania pakietów: świetne dla ruchu **„zrywowego”** (*bursty*) — czasem dużo danych, czasem nic; **współdzielenie zasobów**; prostsze, **bez zestawiania połączenia**.
- **Minusy**: możliwe **przeciążenie** — opóźnienia i utrata pakietów przy przepełnieniu buforów. Potrzebne są protokoły do **niezawodnego przesyłania** i **kontroli przeciążenia** (to robi TCP).
- Jak uzyskać zachowanie „jak w obwodzie”? **Gwarancje przepustowości** — tradycyjnie potrzebne dla audio/wideo.

:::own Ludzkie analogie (pytanie z wykładu)
- **Rezerwacja zasobów** (obwody): stolik zarezerwowany w restauracji — czeka na Ciebie, nawet jeśli się spóźnisz, ale nikt inny przy nim nie usiądzie.
- **Przydział na żądanie** (pakiety): kolejka w barze szybkiej obsługi — nic nie rezerwujesz, zwykle jest szybko, ale w godzinach szczytu czekasz.
:::

## Sprawdź się [[own]]

:::check Ile trwa przesłanie 1 pakietu 8000 bitów przez 3 łącza po 2 Mb/s (store-and-forward, bez propagacji)?
$N \cdot L/R = 3 \cdot 8000 / 2\,000\,000 = 0{,}012$ s $= 12$ ms.
:::

:::check A ile trwa przesłanie 4 takich pakietów tą samą trasą?
$(N + P - 1)\cdot L/R = (3 + 4 - 1)\cdot 4$ ms $= 24$ ms.
:::

:::check Czym różni się forwarding od routingu?
Forwarding to **lokalna** decyzja routera: którym wyjściem wysłać pakiet (wg tablicy). Routing to **globalne** wyznaczanie tras, które wypełnia te tablice.
:::

:::check Dlaczego przy przełączaniu obwodów zasoby mogą się marnować?
Bo zarezerwowany obwód jest **bezczynny**, kiedy połączenie nic nie wysyła — i nikt inny nie może go wtedy użyć.
:::

:::cheatsheet
### Wzory
- Jedno łącze: $d_{trans} = L/R$.
- Store-and-forward, 1 pakiet, $N$ łączy: $N \cdot L/R$.
- $P$ pakietów, $N$ łączy: $(N + P - 1)\cdot L/R$.
- TDM: szybkość obwodu $= R / \text{liczba szczelin}$.
- Obwody: liczba użytkowników $= \lfloor C / r \rfloor$.
- Pakiety: $P(X > k) = 1 - \sum_{i=0}^{k}\binom{N}{i}p^i(1-p)^{N-i}$.

### Fakty
- Router: cały pakiet musi dojść, zanim pójdzie dalej.
- Napływ > wyjście → kolejka; pełny bufor → utrata.
- Forwarding = lokalnie (tablica), routing = globalnie (algorytmy).
- Obwody: rezerwacja, gwarancja, marnowanie; FDM (pasma), TDM (szczeliny).
- Przykład z wykładu: 1 Gb/s, 100 Mb/s, 10% → obwody 10 osób, pakiety 35 osób przy P ≈ 0,0004.

### Zadanie „plik przez obwód TDM” krok po kroku
1. Szybkość obwodu = R / liczba szczelin.
2. Czas = rozmiar pliku [bit] / szybkość obwodu.
3. Dodaj czas zestawienia połączenia.
:::
