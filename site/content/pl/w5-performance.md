---
title: Wydajność sieci — opóźnienia, straty i przepustowość
short: Opóźnienia i przepustowość
description: Cztery składniki opóźnienia pakietu, analogia z karawaną, natężenie ruchu La/R, traceroute, utrata pakietów i wąskie gardło — ze wzorami i kalkulatorami krok po kroku.
kind: mixed
source: Wykład 1 — Chapter 1, slajdy 1-44…1-56 (Kurose & Ross)
---

## W skrócie

- Opóźnienie pakietu w jednym węźle to suma **czterech** składników:
  $d_{nodal} = d_{proc} + d_{queue} + d_{trans} + d_{prop}$.
- $d_{trans} = L/R$ zależy od **rozmiaru pakietu i szybkości łącza**; $d_{prop} = d/s$ zależy od **długości łącza** — to dwie zupełnie różne rzeczy!
- **Natężenie ruchu** $La/R$: blisko 0 — kolejki małe; blisko 1 — kolejki rosną gwałtownie; powyżej 1 — opóźnienie „nieskończone”.
- **traceroute** mierzy opóźnienie do kolejnych routerów na trasie.
- **Przepustowość** (*throughput*) end-to-end = szybkość **najwolniejszego** łącza na trasie — **wąskiego gardła**.

## Skąd biorą się opóźnienia i straty?

Pakiety **czekają w kolejkach** w buforach routerów. Dzieje się tak, gdy **szybkość napływu** do łącza chwilowo przekracza **przepustowość łącza wyjściowego**. Wtedy:

- jeden pakiet jest właśnie **transmitowany** (opóźnienie transmisji),
- inne **czekają w buforze** (opóźnienie kolejkowania),
- jeśli w buforze **nie ma wolnego miejsca**, przychodzący pakiet jest **odrzucany** (strata).

## Cztery źródła opóźnienia

$$
d_{nodal} = d_{proc} + d_{queue} + d_{trans} + d_{prop}
$$

@svg(delay, caption=Pakiet z A do B przechodzi przez router: przetwarzanie → kolejka → transmisja (wypchnięcie bitów) → propagacja (podróż sygnału).)

| Składnik | Co to jest | Od czego zależy | Typowo |
|---|---|---|---|
| $d_{proc}$ — **przetwarzanie** | router sprawdza błędy bitowe i decyduje, którym łączem wysłać pakiet | moc routera | < 1 ms |
| $d_{queue}$ — **kolejkowanie** | czas czekania w kolejce do łącza wyjściowego | **poziom przeciążenia** routera | od 0 do bardzo dużo |
| $d_{trans}$ — **transmisja** | czas „wypchnięcia” wszystkich bitów pakietu do łącza | $L$ — długość pakietu [bit], $R$ — szybkość łącza [bit/s] | $d_{trans} = L/R$ |
| $d_{prop}$ — **propagacja** | czas, w którym **jeden bit** przebiega łącze | $d$ — długość łącza [m], $s$ — prędkość sygnału ≈ $2 \cdot 10^8$ m/s | $d_{prop} = d/s$ |

:::warning $d_{trans}$ i $d_{prop}$ — bardzo różne rzeczy (zdanie z wykładu)
- $d_{trans}$ **nie zależy od odległości** — tylko od tego, ile bitów trzeba wypchnąć i jak szybko łącze je przyjmuje.
- $d_{prop}$ **nie zależy od rozmiaru pakietu** — tylko od tego, jak daleko jest drugi koniec łącza.
:::

:::own Jak to zapamiętać?
Wyobraź sobie wlewanie wody do rury: **transmisja** to czas wlewania całego wiadra (zależy od wielkości wiadra i szerokości lejka), a **propagacja** to czas, w którym pierwsza kropla dopłynie do końca rury (zależy od długości rury).
:::

## Analogia z karawaną (z wykładu)

Karawana **10 samochodów** (to „pakiet” 10 bitów) jedzie przez **bramki autostradowe** (to „routery”) oddalone od siebie o **100 km**.

- Samochody „propagują się” z prędkością **100 km/h**.
- Bramka obsługuje jeden samochód w **12 s** (to „czas transmisji bitu”).
- Samochód ≈ bit, karawana ≈ pakiet.

**Pytanie:** po jakim czasie cała karawana stoi przed drugą bramką?

:::steps
1. Czas „wypchnięcia” całej karawany przez bramkę (transmisja): $12\ \text{s} \cdot 10 = 120\ \text{s} = 2$ min.
2. Czas przejazdu ostatniego auta do drugiej bramki (propagacja): $100\ \text{km} / (100\ \text{km/h}) = 1$ h $= 60$ min.
3. Razem: $2 + 60 = 62$ minuty.
:::

**Druga wersja z wykładu:** samochody jadą **1000 km/h**, a bramka obsługuje samochód przez **1 minutę**. Czy auta dojadą do drugiej bramki, zanim wszystkie przejdą przez pierwszą?

:::steps
1. Pierwsze auto przechodzi przez bramkę po 1 min.
2. Jedzie 100 km z prędkością 1000 km/h: $100/1000$ h $= 6$ min.
3. Dojeżdża do drugiej bramki po $1 + 6 = 7$ min.
4. W tym czasie przez pierwszą bramkę przeszło 7 aut — **3 jeszcze czekają**.
:::

**Tak!** Pierwsze bity pakietu mogą już dotrzeć do następnego routera, gdy ostatnie bity wciąż są w poprzednim (gdy $d_{trans} > d_{prop}$).

## Opóźnienie kolejkowania i natężenie ruchu

Oznaczenia z wykładu:

- $R$ — szybkość łącza [bit/s],
- $L$ — długość pakietu [bit],
- $a$ — średnia liczba pakietów przychodzących na sekundę.

**Natężenie ruchu** (*traffic intensity*):

$$
I = \frac{L \cdot a}{R}
$$

| Wartość $La/R$ | Co się dzieje z kolejką |
|---|---|
| $\approx 0$ | średnie opóźnienie kolejkowania **małe** |
| $\to 1$ | średnie opóźnienie kolejkowania **duże** (rośnie bardzo szybko) |
| $> 1$ | przychodzi więcej „pracy”, niż da się obsłużyć — średnie opóźnienie **nieskończone** |

:::example Policz natężenie ruchu [[own]]
Łącze $R = 10$ Mb/s, pakiety po 1500 B, średnio 500 pakietów na sekundę.

1. $L = 1500 \cdot 8 = 12\,000$ bit.
2. $L \cdot a = 12\,000 \cdot 500 = 6\,000\,000$ bit/s.
3. $I = 6 \cdot 10^6 / 10^7 = 0{,}6$ — kolejki już zauważalne, ale stabilne.
4. Przy 800 pakietach/s: $I = 0{,}96$ — opóźnienia ogromne. Przy 900/s: $I = 1{,}08 > 1$ — kolejka rośnie bez końca, pakiety zaczną ginąć.
:::

:::tip Wniosek dla projektu [[own]]
Nigdy nie projektuj systemu „na styk” ($I \approx 1$). Nawet małe zwiększenie ruchu daje wtedy ogromny wzrost opóźnień — to dotyczy też Twojego proxy, jeśli obsługuje klientów **po kolei** w jednym wątku.
:::

## Prawdziwe opóźnienia: traceroute

Program **traceroute** mierzy opóźnienie od nadawcy do **każdego routera** na trasie do celu. Jak działa (z wykładu):

:::steps
1. Dla każdego $i$ wysyła **3 pakiety**, które mają dotrzeć do routera $i$ na trasie (pole **TTL = i**).
2. Router $i$ odsyła do nadawcy odpowiedź.
3. Nadawca mierzy czas między wysłaniem a odpowiedzią — to RTT do routera $i$.
:::

Fragment przykładu z wykładu (trasa `gaia.cs.umass.edu` → `www.eurecom.fr`):

```text
 1  cs-gw (128.119.240.254)  1 ms  1 ms  2 ms
 ...
 7  nycm-wash.abilene.ucaid.edu (198.32.8.46)  22 ms  22 ms  22 ms
 8  62.40.103.253 (62.40.103.253)  104 ms  109 ms  106 ms   <- łącze transoceaniczne
 ...
17  * * *                                                    <- brak odpowiedzi
19  fantasia.eurecom.fr (193.55.113.142)  132 ms  128 ms  136 ms
```

- Każdy wiersz to jeden router, **3 pomiary** opóźnienia.
- Skok z 22 ms do 104 ms między 7 a 8 przeskokiem to **łącze przez ocean** (długie $d_{prop}$).
- `* * *` — brak odpowiedzi (pakiet zgubiony albo router nie odpowiada).

:::own Dlaczego opóźnienia czasem „maleją” dalej na trasie? (pytanie z wykładu)
Bo każdy pomiar to **osobny pakiet wysłany w innej chwili**. Kolejki w routerach zmieniają się z milisekundy na milisekundę, więc pakiet do dalszego routera mógł akurat trafić na puste kolejki. Do tego routery często odpowiadają na takie pakiety „z niskim priorytetem”.

Sprawdź sam (konsola):

```bash
# Windows
tracert pjwstk.edu.pl
# Linux / macOS
traceroute pjwstk.edu.pl
# samo RTT do hosta
ping pjwstk.edu.pl
```
:::

## Utrata pakietów (packet loss)

- Kolejka (bufor) przed łączem ma **skończoną pojemność**.
- Pakiet, który trafi na **pełną kolejkę**, jest **odrzucany** (gubiony).
- Zgubiony pakiet może zostać wysłany ponownie przez **poprzedni węzeł**, przez **nadawcę** — albo **wcale**.

:::own Kto wysyła ponownie? Zależy od protokołu
- **TCP** wykryje brak potwierdzenia i **wyśle segment ponownie** — dlatego w Javie przy `Socket` nie martwisz się o zgubione dane.
- **UDP nie robi nic** — dlatego przy `DatagramSocket` musisz sam ustawić `setSoTimeout(...)` i zdecydować, co zrobić, gdy odpowiedź nie przyjdzie. Zobacz [C5](c5-tcp-udp.html).
:::

## Przepustowość (throughput)

**Przepustowość** (*throughput*) to szybkość (bity na jednostkę czasu), z jaką bity **docierają** od nadawcy do odbiorcy:

- **chwilowa** — w danym momencie,
- **średnia** — w dłuższym okresie.

Wykład porównuje łącza do **rur**, przez które płynie „ciecz” bitów. Serwer wysyła plik $F$ bitów do klienta; serwer ma łącze $R_s$, klient $R_c$.

| Sytuacja | Średnia przepustowość end-to-end |
|---|---|
| $R_s < R_c$ | $R_s$ |
| $R_s > R_c$ | $R_c$ |

**Wąskie gardło** (*bottleneck link*) — łącze na trasie, które **ogranicza** przepustowość end-to-end. Ogólnie:

$$
\text{throughput} = \min(R_1, R_2, \dots, R_N)
$$

**Scenariusz sieciowy z wykładu:** 10 połączeń (serwer–klient) **uczciwie dzieli** wspólne łącze szkieletowe $R$:

$$
\text{throughput na połączenie} = \min\left(R_c,\ R_s,\ \frac{R}{10}\right)
$$

W praktyce wąskim gardłem jest zwykle $R_c$ albo $R_s$ (łącza dostępowe), a nie szkielet.

:::example Ile trwa pobranie pliku? [[own]]
Plik 4 MB, serwer $R_s = 2$ Mb/s, klient $R_c = 10$ Mb/s, szkielet nieobciążony.

1. $F = 4 \cdot 8 = 32$ Mb.
2. Wąskie gardło: $\min(2, 10) = 2$ Mb/s.
3. Czas: $32 / 2 = 16$ s.
:::

:::calc tput
:::

## Kalkulator opóźnienia end-to-end [[own]]

Poniżej policzysz opóźnienie pakietu przez $N$ jednakowych łączy (store-and-forward): $d_{end\text{-}end} = N\,(d_{proc} + d_{queue} + d_{trans} + d_{prop})$. Ten wzór „na całą trasę” jest z podręcznika, nie ze slajdów.

:::calc delay
:::

## Sprawdź się [[own]]

:::check Pakiet 1000 B, łącze 1 Mb/s o długości 2000 km, s = 2·10⁸ m/s. Ile wynosi d_trans i d_prop?
$d_{trans} = 8000 / 10^6 = 8$ ms. $d_{prop} = 2 \cdot 10^6 / (2 \cdot 10^8) = 0{,}01$ s $= 10$ ms.
:::

:::check Natężenie ruchu wynosi 1,2. Co się stanie?
Napływa więcej danych, niż łącze może wysłać. Kolejka rośnie bez końca, a gdy bufor się zapełni — pakiety giną.
:::

:::check Klient 50 Mb/s, serwer 100 Mb/s, wspólne łącze 400 Mb/s dzielone przez 10 połączeń. Jaka jest przepustowość jednego połączenia?
$\min(50, 100, 400/10) = \min(50, 100, 40) = 40$ Mb/s — wąskim gardłem jest wspólne łącze.
:::

:::check Po co traceroute wysyła pakiety z TTL = 1, 2, 3…?
Żeby pakiet „umarł” dokładnie na routerze nr $i$ — ten router odeśle odpowiedź i dzięki temu poznamy jego adres i RTT do niego.
:::

:::cheatsheet
### Wzory
- $d_{nodal} = d_{proc} + d_{queue} + d_{trans} + d_{prop}$
- $d_{trans} = L / R$ (L w bitach!)
- $d_{prop} = d / s$, $s \approx 2 \cdot 10^8$ m/s
- Natężenie ruchu: $I = La / R$ (≈0 małe, →1 duże, >1 nieskończone)
- Throughput $= \min(R_s, R_c, R/n)$; czas pobrania $= F / \text{throughput}$
- Trasa $N$ łączy: $N \cdot d_{nodal}$ (podręcznik)

### Zadanie „policz opóźnienie” krok po kroku
1. Wypisz dane i **zamień jednostki**: B → bit (×8), Mb/s → bit/s (×10⁶), km → m (×1000).
2. $d_{trans} = L/R$.
3. $d_{prop} = d/s$.
4. Dodaj $d_{proc}$ i $d_{queue}$ (jeśli podane; zwykle 0).
5. Pomnóż przez liczbę łączy (store-and-forward).
6. Wynik w ms (×1000).

### Karawana
- 10 aut × 12 s = 120 s + 100 km / 100 km/h = 60 min → **62 min**.
- 1000 km/h, 1 min/auto → pierwsze auto przy 2. bramce po **7 min**, 3 auta czekają.

### traceroute
- TTL = i → odpowiada router i; 3 pomiary na router; `* * *` = brak odpowiedzi.
- Windows: `tracert`, Linux/macOS: `traceroute`.
:::
