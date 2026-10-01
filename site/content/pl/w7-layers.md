---
title: Warstwy protokołów, enkapsulacja i model OSI
short: Warstwy i enkapsulacja
description: Dlaczego sieć dzieli się na warstwy (analogia z podróżą samolotem), pięć warstw Internetu, nazwy jednostek danych, enkapsulacja nagłówków i siedem warstw modelu ISO/OSI.
kind: mixed
source: Wykład 1 — Chapter 1, slajdy 1-63…1-71 oraz 1-80 (Kurose & Ross)
---

## W skrócie

- Sieci są **skomplikowane**, więc dzielimy je na **warstwy**. Każda warstwa daje **usługę** warstwie wyżej i **korzysta** z usług warstwy niżej.
- Stos Internetu ma **5 warstw**: aplikacji, transportowa, sieciowa, łącza danych, fizyczna.
- Jednostki danych: **komunikat** → **segment** → **datagram/pakiet** → **ramka** → **bity**.
- **Enkapsulacja**: każda warstwa dokleja **swój nagłówek** do danych z warstwy wyżej ($M$ → $H_tM$ → $H_nH_tM$ → $H_lH_nH_tM$).
- Model **ISO/OSI** ma **7 warstw** — dodatkowo **prezentacji** i **sesji**, których w Internecie nie ma (w razie potrzeby robi je aplikacja).

## Problem: sieć ma mnóstwo części

Sieć to hosty, routery, łącza różnych typów, aplikacje, protokoły, sprzęt, oprogramowanie… Pytanie z wykładu: **czy da się to jakoś uporządkować** — choćby w naszej rozmowie o sieciach?

## Analogia z wykładu: podróż samolotem

Podróż samolotem to ciąg kroków, w których bierze udział wiele usług:

| Wylot | Usługa (warstwa) | Przylot |
|---|---|---|
| bilet (kupno) | obsługa biletów | bilet (reklamacja) |
| bagaż (nadanie) | obsługa bagażu | bagaż (odbiór) |
| bramka (wejście) | obsługa bramek | bramka (wyjście) |
| start z pasa | obsługa pasa startowego | lądowanie |
| prowadzenie samolotu | **wyznaczanie trasy** | prowadzenie samolotu |

**Warstwy**: każda warstwa realizuje **usługę**

- dzięki **własnym działaniom** wewnątrz warstwy,
- **korzystając z usług** warstwy poniżej.

Przykład: obsługa bagażu nie wie, jak się prowadzi samolot — po prostu oddaje walizkę „w dół” i ufa, że dotrze.

## Po co warstwy?

Z wykładu:

- **Jawna struktura** pozwala nazwać części złożonego systemu i opisać, jak się ze sobą wiążą — **model warstwowy** jako wspólny język.
- **Modularność** ułatwia utrzymanie i aktualizację: zmiana **implementacji** usługi w jednej warstwie jest **niewidoczna** dla reszty systemu. Np. zmiana procedury przy bramce nie wpływa na resztę podróży.

Wykład zadaje też pytanie: *„warstwy uważane za szkodliwe?”*

:::own Kiedy warstwy przeszkadzają?
- **Powielanie pracy** — np. sprawdzanie błędów w kilku warstwach naraz.
- **Narzut** — każdy nagłówek to dodatkowe bajty (policzymy to niżej).
- Czasem warstwa **potrzebuje informacji z innej warstwy** (np. aplikacja wideo chciałaby wiedzieć, jak obciążona jest sieć), a ścisły podział warstw to utrudnia.
:::

## Stos protokołów Internetu (5 warstw)

| Nr | Warstwa | Zadanie | Przykładowe protokoły |
|---|---|---|---|
| 5 | **aplikacji** (*application*) | wspiera aplikacje sieciowe | IMAP, SMTP, HTTP |
| 4 | **transportowa** (*transport*) | przesyłanie danych **między procesami** | TCP, UDP |
| 3 | **sieciowa** (*network*) | **routing datagramów** od źródła do celu | IP, protokoły routingu |
| 2 | **łącza danych** (*link*) | przesyłanie danych między **sąsiednimi** elementami sieci | Ethernet, 802.11 (WiFi), PPP |
| 1 | **fizyczna** (*physical*) | bity „na kablu” | — |

### Jak nazywają się dane w każdej warstwie?

| Warstwa | Jednostka danych |
|---|---|
| aplikacji | **komunikat** (*message*) |
| transportowa | **segment** |
| sieciowa | **pakiet / datagram** |
| łącza danych | **ramka** (*frame*) |
| fizyczna | **bity** |

@svg(encapsulation, caption=Warstwy, nazwy jednostek danych i nagłówki doklejane przy enkapsulacji.)

## Enkapsulacja

Gdy nadawca wysyła komunikat $M$, dane idą **w dół** stosu, a każda warstwa dokleja **swój nagłówek**:

:::steps
1. Aplikacja tworzy **komunikat** $M$.
2. Warstwa transportowa dokleja nagłówek $H_t$ → **segment** $H_t M$.
3. Warstwa sieciowa dokleja $H_n$ → **datagram** $H_n H_t M$.
4. Warstwa łącza dokleja $H_l$ → **ramka** $H_l H_n H_t M$.
5. Warstwa fizyczna wysyła **bity**.
:::

Po drodze:

- **switch** (przełącznik) zagląda tylko do warstwy **łącza** (i fizycznej),
- **router** zagląda do warstwy **sieciowej** (i niższych): rozpakowuje ramkę, czyta nagłówek IP, pakuje w nową ramkę na kolejne łącze.

U odbiorcy dzieje się odwrotnie (**dekapsulacja**): każda warstwa zdejmuje **swój** nagłówek i oddaje resztę warstwie wyżej, aż aplikacja dostanie samo $M$.

:::own Analogia: list w kopertach
Piszesz list ($M$), wkładasz go do koperty z numerem pokoju (transport), tę kopertę do większej z adresem budynku (sieć), a tę do worka kuriera z numerem trasy (łącze). Każdy pośrednik otwiera tylko „swoją” kopertę.
:::

:::example Ile kosztują nagłówki? [[own]]
Typowe rozmiary: nagłówek TCP 20 B, IP 20 B, Ethernet 18 B (14 B nagłówka + 4 B sumy kontrolnej). Wysyłasz 100 B danych aplikacji.

1. Razem nagłówki: $20 + 20 + 18 = 58$ B.
2. Cała ramka: $100 + 58 = 158$ B.
3. „Sprawność”: $100 / 158 \approx 63\%$ — ponad jedna trzecia to sam narzut!
4. Dla 1460 B danych: $1460 / 1518 \approx 96\%$. Wniosek: małe pakiety marnują łącze.
:::

## Model odniesienia ISO/OSI (7 warstw)

| Nr | Warstwa OSI | Jest w Internecie? |
|---|---|---|
| 7 | aplikacji | tak |
| 6 | **prezentacji** (*presentation*) | **nie** |
| 5 | **sesji** (*session*) | **nie** |
| 4 | transportowa | tak |
| 3 | sieciowa | tak |
| 2 | łącza danych | tak |
| 1 | fizyczna | tak |

- **Prezentacji**: pozwala aplikacjom **interpretować znaczenie** danych — szyfrowanie, kompresja, konwencje zależne od maszyny.
- **Sesji**: **synchronizacja**, punkty kontrolne (*checkpointing*), **odtwarzanie** wymiany danych.

W stosie Internetu **tych dwóch warstw brakuje**. Jeśli takie usługi są potrzebne, musi je zrealizować **aplikacja**. Wykład pyta: *czy są potrzebne?*

:::own Przykład z naszych zajęć
Kodowanie znaków (`StandardCharsets.UTF_8` w Javie) albo Base64 w HTTP to właśnie „prezentacja” — i robi je **Twoja aplikacja**. Mnemotechnika dla 7 warstw OSI (od dołu): **P**lease **D**o **N**ot **T**hrow **S**ausage **P**izza **A**way = Physical, Data link, Network, Transport, Session, Presentation, Application.
:::

## Wireshark a warstwy

Slajd z wykładu pokazuje, jak działa **Wireshark**:

- **przechwytywanie pakietów** (*packet capture*, pcap) otrzymuje **kopię każdej ramki Ethernet** wysłanej i odebranej przez komputer,
- **analizator pakietów** rozpakowuje ją warstwa po warstwie: łącze (Ethernet) → sieć (IP) → transport (TCP/UDP) → aplikacja (np. przeglądarka, klient poczty).

:::own Gdzie to zobaczysz w Wiresharku?
Środkowy panel Wiresharka pokazuje drzewko dokładnie w kolejności enkapsulacji:

```text
Frame 42: 589 bytes on wire            <- warstwa fizyczna (ile bajtów przeszło)
Ethernet II, Src: ..., Dst: ...        <- warstwa łącza (Hl)
Internet Protocol Version 4, Src: ...  <- warstwa sieciowa (Hn)
Transmission Control Protocol, Src Port: 51234, Dst Port: 80  <- transport (Ht)
Hypertext Transfer Protocol            <- aplikacja (M)
```

Kliknięcie w wiersz podświetla odpowiadające mu bajty w panelu na dole.
:::

## Warstwy a Java na naszych zajęciach [[own]]

| Warstwa | Co robimy na zajęciach | Kto to robi |
|---|---|---|
| aplikacji | HTTP ([C2](c2-http.html), [C3](c3-tcp-server.html)), DNS ([C6](c6-dns.html)), własne protokoły tekstowe ([C4](c4-text-protocol.html), kolokwium, projekt) | **Ty** — piszesz komunikaty |
| transportowa | TCP: `Socket`, `ServerSocket`; UDP: `DatagramSocket` | system operacyjny, Ty wybierasz TCP lub UDP |
| sieciowa | adresy IP: `InetAddress`, `127.0.0.1` | system operacyjny |
| łącza / fizyczna | Ethernet, WiFi | karta sieciowa i sterowniki |

## Sprawdź się [[own]]

:::check Jak nazywa się jednostka danych w warstwie transportowej, sieciowej i łącza?
Segment, datagram (pakiet), ramka.
:::

:::check Do której warstwy zagląda switch, a do której router?
Switch — do warstwy łącza danych (2). Router — do warstwy sieciowej (3).
:::

:::check Które warstwy OSI nie występują w stosie Internetu i kto przejmuje ich zadania?
Prezentacji i sesji. Ich zadania (np. szyfrowanie, kompresja, synchronizacja) realizuje aplikacja.
:::

:::check Co znaczy, że zmiana implementacji warstwy jest „przezroczysta”?
Że pozostałe warstwy jej nie zauważą — dopóki warstwa daje tę samą usługę, można ją dowolnie przebudować (np. zamienić kabel na WiFi bez zmiany przeglądarki).
:::

:::cheatsheet
### 5 warstw Internetu (od góry)
| # | Warstwa | Jednostka | Protokoły |
|---|---|---|---|
| 5 | aplikacji | komunikat | HTTP, SMTP, IMAP, DNS |
| 4 | transportowa | segment | TCP, UDP |
| 3 | sieciowa | datagram/pakiet | IP, routing |
| 2 | łącza | ramka | Ethernet, WiFi, PPP |
| 1 | fizyczna | bity | — |

### Enkapsulacja
- $M$ → $H_tM$ → $H_nH_tM$ → $H_lH_nH_tM$ → bity.
- Switch: warstwy 1–2. Router: warstwy 1–3. Host: 1–5.

### OSI = 7 warstw
- Internet + **prezentacja** (szyfrowanie, kompresja) + **sesja** (synchronizacja, checkpointy).
- Mnemotechnika: Please Do Not Throw Sausage Pizza Away.

### Narzut nagłówków krok po kroku
1. Zsumuj nagłówki (np. TCP 20 + IP 20 + Ethernet 18 = 58 B).
2. Ramka = dane + nagłówki.
3. Sprawność = dane / ramka.
:::
