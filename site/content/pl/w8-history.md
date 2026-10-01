---
title: Historia Internetu — od ARPAnetu do smartfonów
short: Historia Internetu
description: Najważniejsze daty i idee: przełączanie pakietów, ARPAnet, zasady Cerfa i Kahna, TCP/IP, DNS, WWW, komercjalizacja i Internet „wszędzie” — plus podsumowanie całego rozdziału 1.
kind: mixed
source: Wykład 1 — Chapter 1, slajdy 1-72…1-78 (Kurose & Ross)
---

## W skrócie

- **1961–1972**: rodzi się idea **przełączania pakietów** i **ARPAnet**.
- **1972–1980**: łączenie wielu sieci (*internetworking*) — **zasady Cerfa i Kahna**, które definiują dzisiejszy Internet.
- **1980–1990**: nowe protokoły — **TCP/IP, SMTP, DNS, FTP**, kontrola przeciążenia TCP.
- **1990–2000**: **WWW**, komercjalizacja, nowe aplikacje.
- **2005–dziś**: smartfony, szybki dostęp bezprzewodowy, sieci społecznościowe, chmura, własne sieci gigantów.

## 1961–1972: początki przełączania pakietów

:::timeline
- **1961** — Kleinrock: **teoria kolejek** pokazuje, że przełączanie pakietów jest efektywne.
- **1964** — Baran: przełączanie pakietów w **sieciach wojskowych**.
- **1967** — agencja ARPA (*Advanced Research Projects Agency*) projektuje **ARPAnet**.
- **1969** — pierwszy działający węzeł ARPAnetu.
- **1972** — publiczna demonstracja ARPAnetu; **NCP** (*Network Control Protocol*) — pierwszy protokół host–host; pierwszy program do **e-maila**; ARPAnet ma **15 węzłów**.
:::

## 1972–1980: łączenie sieci, nowe i firmowe sieci

:::timeline
- **1970** — **ALOHAnet**: sieć satelitarna na Hawajach.
- **1974** — **Cerf i Kahn**: architektura łączenia sieci (*internetworking*).
- **1976** — **Ethernet** w Xerox PARC.
- **koniec lat 70.** — firmowe (zamknięte) architektury: DECnet, SNA, XNA; przełączanie pakietów o **stałej długości** (zapowiedź ATM).
- **1979** — ARPAnet ma **200 węzłów**.
:::

**Zasady łączenia sieci Cerfa i Kahna** — to one definiują architekturę dzisiejszego Internetu:

| Zasada | Co znaczy (prosto) [[own]] |
|---|---|
| **minimalizm, autonomia** | aby połączyć sieci, **nie trzeba nic zmieniać w ich wnętrzu** |
| model usługi **best effort** | sieć „stara się”, ale **niczego nie gwarantuje** (pakiet może zginąć) |
| routing **bezstanowy** (*stateless*) | routery nie pamiętają połączeń — każdy pakiet obsługują osobno |
| **zdecentralizowane** sterowanie | nie ma jednego „szefa” Internetu |

:::own Gdzie widać te zasady na zajęciach?
„Best effort” to powód, dla którego **UDP może gubić datagramy**, a **TCP** musi sam wykrywać straty i wysyłać dane ponownie ([C5](c5-tcp-udp.html), [C7](c7-tcp-wireshark.html)). „Zdecentralizowane sterowanie” zobaczysz też w projekcie — sieć proxy nie ma centralnego serwera, każdy węzeł zna tylko sąsiadów.
:::

## 1980–1990: nowe protokoły, dużo nowych sieci

:::timeline
- **1982** — zdefiniowano protokół poczty **SMTP**.
- **1983** — wdrożenie **TCP/IP**; zdefiniowano **DNS** do tłumaczenia nazw na adresy IP.
- **1985** — zdefiniowano protokół **FTP**.
- **1988** — **kontrola przeciążenia TCP** (*congestion control*).
- **lata 80.** — nowe sieci krajowe: CSnet, BITnet, NSFnet, Minitel; **100 000 hostów** w „konfederacji sieci”.
:::

## 1990–2000: komercjalizacja, WWW, nowe aplikacje

:::timeline
- **początek lat 90.** — wyłączenie ARPAnetu.
- **1991** — NSF znosi ograniczenia na **komercyjne** użycie NSFnetu (NSFnet wyłączony w 1995).
- **początek lat 90.** — **WWW**: hipertekst (Bush 1945, Nelson w latach 60.), **HTML i HTTP** — Berners-Lee.
- **1994** — przeglądarka **Mosaic**, później **Netscape**.
- **koniec lat 90.** — komercjalizacja WWW.
- **koniec lat 90. – 2000s** — nowe „killer apps”: komunikatory, wymiana plików **P2P**; **bezpieczeństwo** wychodzi na pierwszy plan; ok. **50 mln hostów**, ponad **100 mln użytkowników**; łącza szkieletowe działają z prędkościami **Gb/s**.
:::

## 2005–dziś: Internet jest „wszędzie”

- ok. **18 mld urządzeń** podłączonych do Internetu (2017),
- **smartfony** (iPhone: 2007),
- agresywna rozbudowa dostępu **szerokopasmowego**,
- coraz powszechniejszy szybki dostęp bezprzewodowy: **4G/5G, WiFi**,
- **sieci społecznościowe** — Facebook: ok. 2,5 mld użytkowników,
- dostawcy usług (Google, Facebook, Microsoft) tworzą **własne sieci**, omijając komercyjny Internet, żeby być „blisko” użytkownika (zobacz [W4](w4-structure.html)),
- firmy uruchamiają usługi **w chmurze** (Amazon Web Services, Microsoft Azure).

## Podsumowanie rozdziału 1 (ze slajdu końcowego)

Przerobiliśmy „tonę” materiału:

- przegląd Internetu, czym jest protokół ([W1](w1-internet.html)),
- brzeg sieci, sieci dostępowe, rdzeń ([W2](w2-edge.html), [W3](w3-core.html)),
- przełączanie pakietów vs obwodów, struktura Internetu ([W3](w3-core.html), [W4](w4-structure.html)),
- wydajność: straty, opóźnienia, przepustowość ([W5](w5-performance.html)),
- warstwy i modele usług ([W7](w7-layers.html)),
- bezpieczeństwo ([W6](w6-security.html)),
- historia (ta strona).

Masz teraz **kontekst, ogólny obraz, słownictwo i „wyczucie” sieci**. Szczegóły i zabawa — dalej, na ćwiczeniach.

:::own Daty, które łączą się z ćwiczeniami
| Rok | Wydarzenie | Gdzie spotkasz na zajęciach |
|---|---|---|
| 1976 | Ethernet | warstwa łącza w Wiresharku ([W7](w7-layers.html)) |
| 1983 | TCP/IP, DNS | gniazda TCP ([C3](c3-tcp-server.html)), zapytanie DNS ręcznie ([C6](c6-dns.html)) |
| 1988 | kontrola przeciążenia TCP | slow start w Wiresharku ([C7](c7-tcp-wireshark.html)) |
| ~1990 | HTTP, HTML | analiza HTTP i własny serwer ([C2](c2-http.html), [C3](c3-tcp-server.html)) |
:::

## Sprawdź się [[own]]

:::check Kto i kiedy zaproponował architekturę łączenia sieci, na której opiera się Internet?
Cerf i Kahn, 1974.
:::

:::check W którym roku wdrożono TCP/IP i zdefiniowano DNS?
W 1983.
:::

:::check Co oznacza „best effort”?
Sieć stara się dostarczyć pakiet, ale niczego nie gwarantuje — pakiet może się zgubić, opóźnić albo przyjść w innej kolejności.
:::

:::check Kto stworzył HTML i HTTP?
Tim Berners-Lee (początek lat 90.).
:::

:::cheatsheet
### Oś czasu (minimum na egzamin)
- **1961** Kleinrock — teoria kolejek, pakiety są efektywne.
- **1964** Baran — pakiety w sieciach wojskowych.
- **1967** ARPA projektuje ARPAnet; **1969** pierwszy węzeł.
- **1970** ALOHAnet (Hawaje, satelita).
- **1972** demo ARPAnetu, NCP, pierwszy e-mail, 15 węzłów.
- **1974** Cerf i Kahn — internetworking.
- **1976** Ethernet (Xerox PARC).
- **1979** ARPAnet: 200 węzłów.
- **1982** SMTP; **1983** TCP/IP + DNS; **1985** FTP; **1988** kontrola przeciążenia TCP.
- **1991** komercyjne użycie NSFnet; **1995** NSFnet wyłączony.
- **~1990** WWW: HTML, HTTP (Berners-Lee); **1994** Mosaic → Netscape.
- **2007** iPhone; **2017** ~18 mld urządzeń.

### Zasady Cerfa i Kahna
1. Minimalizm, autonomia (bez zmian wewnątrz sieci).
2. Best effort.
3. Routing bezstanowy.
4. Zdecentralizowane sterowanie.
:::
