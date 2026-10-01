---
title: Brzeg sieci — hosty, sieci dostępowe i media fizyczne
short: Brzeg sieci
description: Jak Twój telefon czy laptop „wchodzi” do Internetu, jak szybkie są różne łącza i ile trwa wypchnięcie pakietu do łącza (L/R).
kind: mixed
source: Wykład 1 — Chapter 1, slajdy 1-10…1-24 (Kurose & Ross)
---

## W skrócie

- **Brzeg sieci** (*network edge*) to hosty: **klienci** i **serwery** (serwery zwykle w centrach danych).
- **Sieć dostępowa** (*access network*) łączy host z pierwszym routerem („routerem brzegowym”). Przykłady: kablówka, DSL, WiFi, 4G/5G, Ethernet w firmie.
- Przy sieci dostępowej pytamy o dwie rzeczy: **jaka szybkość** (bit/s) i czy łącze jest **współdzielone**, czy **dedykowane**.
- Host dzieli komunikat na **pakiety** po $L$ bitów i wysyła je z szybkością $R$. Wypchnięcie jednego pakietu trwa $L/R$.
- **Media fizyczne**: kierowane (skrętka, koncentryk, światłowód) i niekierowane (radio).

## Brzeg sieci: kto tu mieszka?

Na brzegu sieci są **hosty** — urządzenia, na których działają aplikacje. Dzielimy je na:

- **klientów** — np. Twoja przeglądarka, aplikacja w telefonie,
- **serwery** — np. serwer WWW, serwer poczty; dziś najczęściej stoją w **centrach danych** (*data centers*).

Pomiędzy hostami a rdzeniem sieci są **sieci dostępowe** i **media fizyczne** (kable, radio). To właśnie ten temat.

## Sieci dostępowe — jak dostać się do Internetu?

Pytanie z wykładu: **jak połączyć host z routerem brzegowym?** Są trzy grupy sieci dostępowych:

1. **domowe** (*residential*) — kablówka, DSL, światłowód do domu,
2. **instytucjonalne** — szkoła, firma, uczelnia,
3. **mobilne** — WiFi, 4G/5G.

Na co patrzymy?

- **Szybkość transmisji** sieci dostępowej (bit/s).
- Czy dostęp jest **współdzielony** (*shared*) z innymi użytkownikami, czy **dedykowany** (*dedicated*) tylko dla Ciebie.

### Dostęp przez telewizję kablową (HFC)

Telewizja kablowa wysyła wiele rzeczy **jednym kablem** — każdą w innym paśmie częstotliwości. To jest **FDM** (*Frequency Division Multiplexing*, zwielokrotnienie z podziałem częstotliwości): kanały wideo, kanały danych i kanał sterujący leżą obok siebie na różnych częstotliwościach.

:::own Analogia: FDM jak stacje radiowe
W radiu jednocześnie nadaje wiele stacji, a Ty wybierasz jedną, kręcąc gałką częstotliwości. W kablu jest tak samo: telewizja na jednych częstotliwościach, Internet na innych — i nie przeszkadzają sobie.
:::

Najważniejsze fakty z wykładu:

- **HFC** (*Hybrid Fiber Coax*) — sieć mieszana: światłowód + kabel koncentryczny łączy domy z routerem ISP.
- Po stronie operatora (*cable headend*) stoi **CMTS** (*cable modem termination system*), a w domu **modem kablowy** i **splitter**.
- Łącze jest **asymetryczne**: do domu (*downstream*) od 40 Mb/s do 1,2 Gb/s, z domu (*upstream*) 30–100 Mb/s.
- Domy **współdzielą** sieć dostępową aż do *cable headend* — gdy wieczorem wszyscy oglądają filmy, każdy dostaje kawałek.

### DSL — Internet po linii telefonicznej

- **DSL** (*Digital Subscriber Line*) używa **istniejącej linii telefonicznej** do **centrali** (*central office*).
- W centrali stoi **DSLAM** (*DSL access multiplexer*): dane idą do Internetu, głos do sieci telefonicznej.
- Głos i dane są przesyłane na **różnych częstotliwościach** (znów FDM), więc można jednocześnie rozmawiać i surfować.
- Linia jest **dedykowana** (nie dzielisz jej z sąsiadami): 24–52 Mb/s do domu, 3,5–16 Mb/s z domu.

### Sieć domowa

Typowy dom ma:

- **modem** kablowy lub DSL (połączenie z operatorem),
- **router** z **firewallem** i **NAT**,
- **punkt dostępowy WiFi** (54 lub 450 Mb/s),
- przewodowy **Ethernet** (1 Gb/s).

Bardzo często modem, router i WiFi są **w jednym pudełku** — tym, które dostałeś od operatora.

### Bezprzewodowe sieci dostępowe

Współdzielona sieć bezprzewodowa łączy host z routerem przez **stację bazową**, czyli **punkt dostępowy** (*access point*).

| | WLAN (WiFi) | Sieć komórkowa (rozległa) |
|---|---|---|
| Zasięg | w budynku i wokół (~30 m, na slajdzie „~100 ft”) | dziesiątki km |
| Kto dostarcza | Ty / firma / uczelnia | operator komórkowy |
| Standardy i szybkości z wykładu | 802.11b/g/n: 11, 54, 450 Mb/s | 4G: dziesiątki Mb/s (5G w drodze) |

### Sieci firmowe i uczelniane (enterprise)

Firmy i uczelnie mają **mieszankę** łączy przewodowych i bezprzewodowych, **switchy** i **routerów**:

- Ethernet: 100 Mb/s, 1 Gb/s, 10 Gb/s,
- WiFi: 11, 54, 450 Mb/s,
- jeden **router instytucjonalny** łączy całą sieć z ISP; w środku są też serwery firmy (poczta, WWW).

## Host wysyła pakiety: wzór L/R

Co robi host, gdy aplikacja chce coś wysłać?

:::steps
1. Bierze **komunikat aplikacji** (np. plik).
2. Dzieli go na mniejsze kawałki — **pakiety** — każdy o długości $L$ bitów.
3. Wysyła pakiety do sieci dostępowej z szybkością transmisji $R$ (bit/s). $R$ to inaczej **przepustowość łącza** (*link capacity, bandwidth*).
:::

Czas potrzebny, żeby **wypchnąć** cały pakiet do łącza, to **opóźnienie transmisji**:

$$
d_{trans} = \frac{L\ \text{[bit]}}{R\ \text{[bit/s]}}
$$

:::example Liczymy krok po kroku [[own]]
Pakiet ma 1500 bajtów, łącze 100 Mb/s. Ile trwa wysłanie pakietu?

1. Zamień bajty na bity: $L = 1500 \cdot 8 = 12\,000$ bit.
2. Zamień Mb/s na bit/s: $R = 100 \cdot 10^6 = 10^8$ bit/s.
3. Podziel: $d_{trans} = 12\,000 / 10^8 = 0{,}00012$ s $= 0{,}12$ ms $= 120\ \mu s$.
:::

:::danger Najczęstsze pułapki z jednostkami [[own]]
- **B** (bajt) ≠ **b** (bit): $1\ \text{B} = 8\ \text{b}$. „MB” to megabajty, „Mb” to megabity.
- W szybkościach łączy „kilo”, „mega”, „giga” to potęgi **10**: $1\ \text{Mb/s} = 10^6$ bit/s (nie $2^{20}$).
- Wynik w sekundach często warto zamienić na ms ($\times 1000$) lub µs ($\times 10^6$).
:::

## Media fizyczne

- **Bit** przemieszcza się między parami nadajnik–odbiornik.
- **Łącze fizyczne** to to, co leży między nadajnikiem a odbiornikiem.
- **Media kierowane** (*guided*): sygnał biegnie w ciele stałym — miedź, światłowód, koncentryk.
- **Media niekierowane** (*unguided*): sygnał rozchodzi się swobodnie — radio.

| Medium | Jak działa | Szybkości i cechy z wykładu |
|---|---|---|
| **Skrętka** (*twisted pair*) | dwa izolowane, skręcone przewody miedziane | kat. 5: 100 Mb/s i 1 Gb/s Ethernet; kat. 6: 10 Gb/s |
| **Kabel koncentryczny** (*coax*) | dwa współosiowe przewodniki miedziane | dwukierunkowy; szerokopasmowy (wiele kanałów); setki Mb/s na kanał |
| **Światłowód** (*fiber*) | szklane włókno, impulsy światła — każdy impuls to bit | dziesiątki–setki Gb/s; mało błędów; wzmacniacze daleko od siebie; odporny na zakłócenia elektromagnetyczne |
| **Radio** | sygnał w widmie elektromagnetycznym, bez „kabla” | rozgłoszeniowe, *half-duplex*; problemy: odbicia, przeszkody, interferencje |

Rodzaje łączy radiowych z wykładu:

- **mikrofale naziemne** — kanały do 45 Mb/s,
- **WiFi (WLAN)** — do setek Mb/s,
- **sieć komórkowa** (4G) — dziesiątki Mb/s,
- **satelita** — do 45 Mb/s na kanał, **270 ms** opóźnienia end-to-end; satelity **geostacjonarne** vs **niskoorbitalne** (LEO).

:::own Co znaczy „half-duplex”?
**Half-duplex** — w danej chwili nadaje tylko jedna strona (jak krótkofalówka: „odbiór!”). **Full-duplex** — obie strony nadają jednocześnie (jak rozmowa telefoniczna). Dlatego radio jest trudniejsze niż kabel: urządzenia muszą się „umawiać”, kto teraz mówi.
:::

## Sprawdź się [[own]]

:::check Dlaczego w kablówce wieczorem Internet może zwolnić, a w DSL zwykle nie?
Bo w HFC sieć dostępowa jest **współdzielona** przez domy aż do *cable headend*. W DSL każdy ma **dedykowaną** linię do centrali.
:::

:::check Ile trwa wypchnięcie pakietu 1000 B do łącza 1 Gb/s?
$L = 8000$ bit, $R = 10^9$ bit/s, więc $d_{trans} = 8 \cdot 10^{-6}$ s $= 8\ \mu s$.
:::

:::check Które medium jest odporne na zakłócenia elektromagnetyczne i ma najmniej błędów?
Światłowód.
:::

:::check Co oznacza, że łącze kablowe jest asymetryczne?
Szybkość do domu (*downstream*) jest dużo większa niż z domu (*upstream*), np. 1,2 Gb/s vs 100 Mb/s.
:::

:::cheatsheet
### Sieci dostępowe — liczby z wykładu
| Typ | Szybkość | Współdzielona? |
|---|---|---|
| Kablówka HFC | ↓ 40 Mb/s–1,2 Gb/s, ↑ 30–100 Mb/s | tak (do headend) |
| DSL | ↓ 24–52 Mb/s, ↑ 3,5–16 Mb/s | nie (dedykowana) |
| WiFi 802.11b/g/n | 11 / 54 / 450 Mb/s | tak, zasięg ~30 m |
| 4G | dziesiątki Mb/s | tak, zasięg km |
| Ethernet | 100 Mb/s, 1 Gb/s, 10 Gb/s | — |
| Satelita | do 45 Mb/s, 270 ms opóźnienia | — |

### Media
- Skrętka kat. 5: 100 Mb/s–1 Gb/s; kat. 6: 10 Gb/s.
- Koncentryk: wiele kanałów, setki Mb/s na kanał.
- Światłowód: 10–100+ Gb/s, odporny na zakłócenia.
- Radio: half-duplex, odbicia, przeszkody, interferencje.

### Opóźnienie transmisji — krok po kroku
1. $L$ w bajtach → $\times 8$ → bity.
2. $R$ w Mb/s → $\times 10^6$ → bit/s.
3. $d_{trans} = L / R$ [s].
4. Wynik ×1000 → ms.

Przykład: 1500 B przy 100 Mb/s → 12 000 / 10⁸ = 0,12 ms.
:::
