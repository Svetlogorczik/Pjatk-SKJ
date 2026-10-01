---
title: TCP pod lupą — numery sekwencyjne, RTT, okno i slow start w Wiresharku
short: TCP w Wiresharku
description: Analizujemy przesyłanie dużego pliku przez TCP — 3-etapowe nawiązanie połączenia, numery seq/ack, EstimatedRTT, okno odbiorcy, retransmisje, przepustowość i kontrola przeciążenia — z obliczeniami krok po kroku.
kind: lab
source: logika laboratorium „Wireshark Lab: TCP” (Kurose & Ross) z zajęć — pytania i przykłady przerobione
---

## W skrócie

- Połączenie TCP zaczyna się **3-etapowo**: `SYN` → `SYN+ACK` → `ACK`.
- TCP numeruje **bajty**, nie pakiety: `seq` = numer pierwszego bajtu w segmencie, `ack` = numer **następnego oczekiwanego** bajtu.
- **SampleRTT** = czas od wysłania segmentu do przyjścia jego ACK. **EstimatedRTT** to wygładzona średnia: $(1-\alpha)\cdot\text{EstimatedRTT} + \alpha\cdot\text{SampleRTT}$, $\alpha = 0{,}125$.
- **Okno odbiorcy** (*rwnd*) mówi nadawcy, ile bajtów jeszcze zmieści się w buforze odbiorcy.
- **Przepustowość** = przesłane bajty / czas. **Slow start**: okno rośnie ×2 co RTT („flotylle” pakietów).

:::origin
Na zajęciach robi się laboratorium Wireshark TCP z podręcznika Kurose & Ross: przesyła się duży plik tekstowy (ok. 150 KB) metodą HTTP POST na serwer laboratorium, przechwytuje ruch i odpowiada na pytania o adresy i porty, numery sekwencyjne SYN/SYN-ACK, segment z POST, RTT i EstimatedRTT, długości segmentów, okno odbiorcy, retransmisje, potwierdzenia, przepustowość i fazy kontroli przeciążenia (wykres Stevensa). Poniżej masz **własne pytania i przykłady liczbowe**, a w każdym pytaniu wskazówkę, gdzie szukać odpowiedzi.
:::

## Przygotowanie (z instrukcji laboratorium)

:::steps
1. Pobierz plik tekstowy z laboratorium (link w instrukcji) i zapisz go na dysku.
2. Otwórz stronę laboratorium z formularzem wysyłania pliku, wybierz plik, **jeszcze nie wysyłaj**.
3. Uruchom przechwytywanie w Wiresharku.
4. Kliknij przycisk wysyłania — plik pójdzie na serwer metodą **POST**.
5. Zatrzymaj przechwytywanie i wpisz filtr **`tcp`**.
:::

:::tip Brak sieci?
Tak jak w HTTP — laboratorium ma gotowy plik przechwycenia (*tcp-wireshark-trace1-1* w archiwum `wireshark-traces-9e.zip`).
:::

## Nagłówek TCP w pigułce [[own]]

| Pole | Do czego służy |
|---|---|
| port źródłowy / docelowy | które procesy rozmawiają (np. 54321 → 80) |
| **sequence number** | numer pierwszego bajtu danych w tym segmencie |
| **acknowledgment number** | „otrzymałem wszystko do tego numeru — wyślij od niego” |
| flagi | **SYN** (nawiązanie), **ACK** (pole ack ważne), **FIN** (zamknięcie), **RST** (zerwanie), PSH |
| **window** | wolne miejsce w buforze odbiorcy (z ewentualnym mnożnikiem) |
| opcje | m.in. **MSS**, **window scale**, **SACK permitted**, znaczniki czasu |

@svg(handshake, caption=3-etapowe nawiązanie połączenia, dwa segmenty z danymi (po 1460 B) i jedno skumulowane potwierdzenie.)

## Numery seq i ack krok po kroku [[own]]

Przykład: klient losuje początkowy numer **2000**, serwer **9000**; segmenty z danymi mają po **1448 B**.

| # | Kto → kto | Flagi | seq | ack | dane | Wyjaśnienie |
|---|---|---|---|---|---|---|
| 1 | K → S | SYN | 2000 | — | 0 | SYN „zużywa” 1 numer |
| 2 | S → K | SYN, ACK | 9000 | **2001** | 0 | ack = seq klienta + 1 |
| 3 | K → S | ACK | 2001 | **9001** | 0 | ack = seq serwera + 1 |
| 4 | K → S | ACK, PSH | 2001 | 9001 | 1448 | pierwsze dane (np. początek POST) |
| 5 | K → S | ACK | **3449** | 9001 | 1448 | 2001 + 1448 = 3449 |
| 6 | S → K | ACK | 9001 | **4897** | 0 | jedno ACK potwierdza **oba** segmenty |

:::note Zasady do zapamiętania
- następny `seq` = poprzedni `seq` + liczba bajtów danych,
- `ack` = numer **następnego** bajtu, którego odbiorca się spodziewa,
- ACK jest **skumulowany** — potwierdza wszystko „do tego miejsca”,
- Wireshark domyślnie pokazuje numery **względne** (SYN ma seq = 0). Surowe zobaczysz jako *Sequence Number (raw)* albo po wyłączeniu opcji *Relative sequence numbers* (Edit → Preferences → Protocols → TCP).
:::

## RTT i EstimatedRTT

**SampleRTT** dla segmentu = (czas przyjścia ACK, który go potwierdza) − (czas wysłania segmentu). Jedna próbka „skacze”, więc TCP liczy średnią ważoną:

$$
\text{EstimatedRTT} = (1-\alpha)\cdot\text{EstimatedRTT} + \alpha\cdot\text{SampleRTT}, \qquad \alpha = 0{,}125
$$

W laboratorium przyjmuje się, że **pierwsze** EstimatedRTT = pierwsze zmierzone RTT.

:::example EstimatedRTT krok po kroku [[own]]
Próbki SampleRTT (ms): 28, 32, 25, 40, 30.

1. $\text{Est}_1 = 28$
2. $\text{Est}_2 = 0{,}875 \cdot 28 + 0{,}125 \cdot 32 = 24{,}5 + 4 = 28{,}5$
3. $\text{Est}_3 = 0{,}875 \cdot 28{,}5 + 0{,}125 \cdot 25 = 24{,}9375 + 3{,}125 = 28{,}0625$
4. $\text{Est}_4 = 0{,}875 \cdot 28{,}0625 + 0{,}125 \cdot 40 \approx 29{,}555$
5. $\text{Est}_5 = 0{,}875 \cdot 29{,}555 + 0{,}125 \cdot 30 \approx 29{,}610$

Widać, że jedna duża próbka (40 ms) podnosi średnią tylko trochę — o to chodzi.
:::

:::own Timeout (z podręcznika, nie z laboratorium)
TCP liczy też zmienność $\text{DevRTT} = (1-\beta)\cdot\text{DevRTT} + \beta\cdot|\text{SampleRTT} - \text{EstimatedRTT}|$, $\beta = 0{,}25$, i ustawia
$\text{TimeoutInterval} = \text{EstimatedRTT} + 4\cdot\text{DevRTT}$. Kalkulator poniżej liczy wszystko naraz (start: DevRTT = połowa pierwszej próbki, jak w RFC 6298).
:::

:::calc rtt
:::

W Wiresharku wykres RTT: zaznacz segment od klienta do serwera → **Statistics → TCP Stream Graphs → Round Trip Time**.

## Okno odbiorcy i skalowanie

Pole **window** w segmencie od odbiorcy mówi, ile bajtów nadawca może jeszcze wysłać bez czekania na ACK (kontrola przepływu). Jeśli w SYN uzgodniono **window scale**, prawdziwa wartość to:

$$
\text{okno [B]} = \text{Window Size Value} \cdot \text{mnożnik}
$$

Przykład: `Window Size Value: 501`, mnożnik 128 → $501 \cdot 128 = 64\,128$ B. Wireshark pokazuje to jako *Calculated window size*. Jeśli okno nigdy nie spada do (prawie) zera, odbiorca **nie spowalnia** nadawcy.

## Retransmisje i potwierdzenia

- **Retransmisja** = segment z **tym samym** `seq` wysłany ponownie. Wireshark oznacza go `[TCP Retransmission]`; filtr: `tcp.analysis.retransmission`.
- **Opóźnione ACK**: odbiorca często potwierdza **co drugi** segment (ack rośnie wtedy o ~2 × MSS). Odejmij kolejne wartości `ack`, żeby zobaczyć, ile bajtów potwierdza jedno ACK.

## Przepustowość połączenia

$$
\text{przepustowość} = \frac{\text{liczba przesłanych bajtów}}{\text{czas}}
$$

:::example Liczymy przepustowość [[own]]
Pierwszy segment z danymi wysłano w $t = 0{,}0245$ s, ostatnie ACK przyszło w $t = 0{,}2009$ s, przesłano $153\,000$ B.

1. Czas: $0{,}2009 - 0{,}0245 = 0{,}1764$ s.
2. Przepustowość: $153\,000 / 0{,}1764 \approx 867\,347$ B/s $\approx 0{,}87$ MB/s.
3. W bitach: $\cdot 8 \approx 6{,}94$ Mb/s.

Liczbę bajtów najłatwiej wziąć z różnicy numerów: `ack` ostatniego potwierdzenia − `seq` pierwszego segmentu z danymi.
:::

## Kontrola przeciążenia: slow start i unikanie przeciążenia

- **Slow start**: na początku okno przeciążenia (*cwnd*) rośnie **dwukrotnie co RTT** — nadawca wysyła „flotyllę” 2, 4, 8… segmentów naraz i czeka na ACK.
- **Congestion avoidance** (unikanie przeciążenia): po przekroczeniu progu okno rośnie **liniowo** — o 1 MSS na RTT.
- Na wykresie **Statistics → TCP Stream Graphs → Time-Sequence (Stevens)** każda kropka to wysłany segment; kropki ułożone pionowo to flotylla wysłana jedna po drugiej.
- Odstęp między flotyllami ≈ **RTT** (nadawca czeka na potwierdzenia).

:::own Jak rozpoznać fazę z wykresu?
Policz segmenty w kolejnych flotyllach. 2 → 4 → 8 → 16 to slow start. Ta sama liczba albo +1 na RTT to unikanie przeciążenia. Jeśli flotylle są **stale tej samej wielkości**, ograniczeniem może być okno odbiorcy albo aplikacja — w małym transferze (150 KB) TCP często **nie zdąży** wyjść ze slow startu.
:::

## Pytania do przechwycenia

::::task Pytania T1–T10
1. Jaki adres IP i port ma klient, a jaki serwer?
2. Jaki **surowy** numer sekwencyjny ma segment SYN? Po czym poznać, że to SYN? Czy klient zgłasza obsługę **SACK**?
3. Jaki numer sekwencyjny ma SYN-ACK i jaką wartość ma w nim pole ack? Skąd serwer ją wziął?
4. W którym segmencie jest początek POST i ile ma bajtów danych? Czy cały plik zmieścił się w jednym segmencie?
5. Zmierz SampleRTT dla pierwszych dwóch segmentów z danymi i policz EstimatedRTT po drugim.
6. Jaka jest długość (nagłówki + dane) pierwszych czterech segmentów z danymi?
7. Jakie jest najmniejsze okno ogłaszane przez serwer na początku? Czy okno ograniczało nadawcę?
8. Czy są retransmisje? Jak to sprawdziłeś?
9. Ile bajtów zwykle potwierdza jedno ACK? Czy widać ACK co drugi segment?
10. Jaka była przepustowość połączenia? Jak ją policzyłeś?

:::hint Gdzie szukać
1. Warstwa IP + TCP dowolnego segmentu od klienta (serwer HTTP ma port **80**).
2. Pole *Sequence Number (raw)*; flaga **SYN = 1**; w opcjach TCP szukaj **SACK permitted**.
3. ack w SYN-ACK = seq z SYN + 1.
4. Szukaj tekstu „POST” w panelu bajtów; długość danych = *TCP Segment Len*; plik ma > 150 KB, więc nie zmieści się (MSS ≈ 1460 B).
5. Kolumna Time: (czas ACK, którego `ack` ≥ koniec segmentu) − (czas wysłania segmentu); potem wzór z α = 0,125 — albo kalkulator wyżej.
6. *Frame length* albo *TCP Segment Len* + nagłówki; w przechwyceniu z laboratorium segmenty mają < 1480 B, bo IP ma limit 1500 B, a nagłówki TCP/IP to co najmniej 40 B.
7. *Window Size Value* × mnożnik (lub *Calculated window size*) w segmentach od serwera.
8. Filtr `tcp.analysis.retransmission` albo szukaj powtórzonego `seq`.
9. Różnice kolejnych wartości `ack` od serwera.
10. Bajty / czas — patrz przykład wyżej.
:::
::::

## Zadania obliczeniowe

::::task 1. EstimatedRTT
SampleRTT: 40, 36, 52, 44 ms, $\alpha = 0{,}125$, start = pierwsza próbka. Policz EstimatedRTT po każdej próbce.

:::answer
40 → $0{,}875 \cdot 40 + 0{,}125 \cdot 36 = 39{,}5$ → $0{,}875 \cdot 39{,}5 + 0{,}125 \cdot 52 = 41{,}0625$ → $0{,}875 \cdot 41{,}0625 + 0{,}125 \cdot 44 \approx 41{,}43$ ms. Sprawdź kalkulatorem.
:::
::::

::::task 2. seq/ack
Klient ma ISN = 500. Wysyła SYN, potem 3 segmenty po 1000 B. Podaj surowe `seq` każdego segmentu z danymi i `ack`, którym serwer potwierdzi wszystkie trzy.

:::answer
SYN: seq = 500. Dane: 501, 1501, 2501. Skumulowane ACK: **3501**.
:::
::::

::::task 3. Która faza?
Flotylle mają kolejno 2, 4, 8, 16, 17, 18 segmentów. Gdzie kończy się slow start?

:::answer
Do 16 okno się podwaja (slow start), potem rośnie o 1 na RTT — to unikanie przeciążenia (próg osiągnięty przy ok. 16 segmentach).
:::
::::

:::cheatsheet
### Nawiązanie połączenia
SYN (seq = x) → SYN+ACK (seq = y, ack = x+1) → ACK (ack = y+1).

### seq / ack
- następny seq = seq + długość danych;
- ack = następny oczekiwany bajt; ACK skumulowany;
- SYN i FIN zużywają po 1 numerze;
- Wireshark: numery względne (raw w osobnym polu).

### Wzory
- $\text{Est} = 0{,}875\cdot\text{Est} + 0{,}125\cdot\text{Sample}$ (start: Est = 1. próbka)
- $\text{Dev} = 0{,}75\cdot\text{Dev} + 0{,}25\cdot|\text{Sample} - \text{Est}|$; $\text{Timeout} = \text{Est} + 4\cdot\text{Dev}$
- okno = Window Size Value × mnożnik
- przepustowość = bajty / czas (×8 → bit/s)

### Wireshark
- filtr `tcp`, `tcp.flags.syn == 1`, `tcp.analysis.retransmission`, `tcp.stream eq 0`;
- Statistics → TCP Stream Graphs → Round Trip Time / Time-Sequence (Stevens);
- surowe seq: Preferences → Protocols → TCP → odznacz *Relative sequence numbers*.

### Slow start vs unikanie przeciążenia
- ×2 co RTT → slow start; +1 MSS co RTT → congestion avoidance;
- odstęp między flotyllami ≈ RTT.
:::
