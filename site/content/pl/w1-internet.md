---
title: Czym jest Internet i czym jest protokół?
short: Internet i protokoły
description: Dwa spojrzenia na Internet — „z czego jest zbudowany” i „co daje aplikacjom” — oraz najważniejsze słowo całego przedmiotu: protokół.
kind: mixed
source: Wykład 1 — Chapter 1, slajdy 1-1…1-9 (Kurose & Ross)
---

## W skrócie

- Internet to **sieć sieci**: miliardy urządzeń połączonych przez tysiące dostawców (ISP).
- Można na niego patrzeć na dwa sposoby: **z czego jest zbudowany** (urządzenia, łącza, routery) i **jaką usługę daje aplikacjom**.
- **Protokół** = umowa, która określa **format** komunikatów, ich **kolejność** i **akcje** po wysłaniu/odebraniu.
- Standardy Internetu to dokumenty **RFC** (Request for Comments), które przygotowuje **IETF**.

## Mapa rozdziału 1

Wykład otwiera rozdział 1 podręcznika Kurose & Ross. Jego cel: złapać „ogólny obraz” i słownictwo — szczegóły przyjdą później. Na tej stronie każdy punkt planu z wykładu ma osobny temat:

| Punkt planu z wykładu | Temat na stronie |
|---|---|
| Czym jest Internet? Czym jest protokół? | W1 (ta strona) |
| Brzeg sieci: hosty, sieci dostępowe, media fizyczne | [W2](w2-edge.html) |
| Rdzeń sieci: przełączanie pakietów/obwodów | [W3](w3-core.html), [W4](w4-structure.html) |
| Wydajność: straty, opóźnienia, przepustowość | [W5](w5-performance.html) |
| Bezpieczeństwo | [W6](w6-security.html) |
| Warstwy protokołów, modele usług | [W7](w7-layers.html) |
| Historia | [W8](w8-history.html) |

## Spojrzenie nr 1: z czego zbudowany jest Internet („śrubki i nakrętki”)

Wyobraź sobie, że rozkręcasz Internet na części. Znajdziesz trzy rodzaje „klocków”:

| Klocek | Co to jest | Przykłady |
|---|---|---|
| **Host** (*end system*, system końcowy) | urządzenie na **brzegu** sieci, na którym działają aplikacje sieciowe | laptop, telefon, serwer WWW, konsola, „inteligentna” lodówka |
| **Przełącznik pakietów** (*packet switch*) | urządzenie, które **przekazuje dalej** pakiety (porcje danych) | **router**, **switch** |
| **Łącze komunikacyjne** (*link*) | „droga”, po której płyną bity | światłowód, miedź, radio, satelita |

Każde łącze ma **szybkość transmisji** (*transmission rate*), nazywaną też **przepustowością** (*bandwidth*), mierzoną w **bitach na sekundę** (bit/s, Mb/s, Gb/s).

**Sieć** (*network*) to zbiór urządzeń, routerów i łączy, którym **zarządza jedna organizacja** — np. sieć domowa, sieć firmy, sieć uczelni, sieć operatora komórkowego.

@svg(internet-overview, caption=Brzeg sieci (hosty w domach, firmach, sieciach komórkowych i centrach danych) oraz rdzeń sieci (połączone routery R).)

:::own Analogia: Internet jak poczta i drogi
- **Host** to dom — tam ktoś pisze i czyta listy (aplikacje).
- **Pakiet** to list w kopercie — duży tekst dzieli się na wiele kopert.
- **Router** to skrzyżowanie z drogowskazami — patrzy na adres na kopercie i decyduje, którą drogą wysłać ją dalej.
- **Łącze** to droga — jedna jest autostradą (światłowód), inna wąską ścieżką (stary kabel).
:::

Na wykładzie pokazano też „zabawne” urządzenia podłączone do Internetu: rozrusznik serca z monitorem, Amazon Echo, ramka na zdjęcia, toster z prognozą pogody, lodówka, kamera, łóżko z czujnikami, Fitbit, urządzenia AR… Wniosek: **hostem może być prawie wszystko**, nie tylko komputer.

### Internet = „sieć sieci”

Internet nie jest jedną siecią. To **wiele połączonych ze sobą dostawców** (*ISP — Internet Service Provider*): lokalnych, regionalnych, krajowych i globalnych. Szczegóły tej struktury są w temacie [W4](w4-structure.html).

Wszędzie działają **protokoły**, które sterują wysyłaniem i odbieraniem komunikatów, np. HTTP (WWW), strumieniowanie wideo, Skype, TCP, IP, WiFi, 4G, Ethernet.

**Standardy Internetu**:

- **RFC** — *Request for Comments*, dokumenty opisujące protokoły,
- **IETF** — *Internet Engineering Task Force*, organizacja, która je przygotowuje.

:::own Przykładowe RFC, które przydadzą się na tym przedmiocie
- RFC 768 — UDP (krótkie, warto przejrzeć!),
- RFC 9293 — TCP (następca RFC 793),
- RFC 1035 — DNS (format wiadomości, którego użyjesz w [C6](c6-dns.html)),
- RFC 9110 i 9112 — HTTP (semantyka i HTTP/1.1, przydatne w [C2](c2-http.html) i [C3](c3-tcp-server.html)).

W szablonie dokumentacji projektu prowadzący podaje RFC jako wzór tego, jak opisać protokół — więc warto zobaczyć, jak wygląda prawdziwe RFC.
:::

## Spojrzenie nr 2: Internet jako usługa

Z punktu widzenia programisty Internet to **infrastruktura, która daje usługi aplikacjom**: WWW, wideo, telekonferencje, e-mail, gry, sklepy internetowe, media społecznościowe, urządzenia domowe…

Internet daje też **interfejs programistyczny** dla aplikacji rozproszonych — „haczyki” (*hooks*), dzięki którym program może wysłać i odebrać dane, czyli „podłączyć się” do usługi transportowej. Wykład porównuje to do **poczty**: jest kilka opcji usługi (list zwykły, polecony…), a Ty wybierasz, której potrzebujesz.

:::own Te „haczyki” w Javie to gniazda (sockets)
Na ćwiczeniach użyjesz dokładnie tych „haczyków”:

- `Socket` i `ServerSocket` — usługa **TCP** (niezawodna, jak list polecony z potwierdzeniem),
- `DatagramSocket` i `DatagramPacket` — usługa **UDP** (szybka, bez gwarancji, jak pocztówka).

```java
// Połączenie TCP z serwerem WWW na porcie 80 - jedna linijka "podłącza" program do Internetu
Socket socket = new Socket("example.com", 80);
```
:::

## Czym jest protokół?

Najpierw ludzie. Kiedy pytasz kogoś na ulicy o godzinę, też działa „protokół”:

- wysyłasz **konkretne komunikaty** („Przepraszam…”, „Która godzina?”),
- po odebraniu odpowiedzi wykonujesz **konkretne akcje** (dziękujesz, odchodzisz),
- jeśli ktoś nie odpowie na „Przepraszam…”, **nie zadajesz** od razu pytania o godzinę.

W sieciach komunikaty wymieniają **urządzenia**, a nie ludzie — ale zasada jest ta sama. **Cała komunikacja w Internecie odbywa się według protokołów.**

:::note Definicja z wykładu
**Protokół** definiuje **format** i **kolejność** komunikatów wysyłanych i odbieranych przez urządzenia sieciowe oraz **akcje** podejmowane przy wysyłaniu i odbieraniu komunikatu.
:::

Wykład zestawia rozmowę ludzi z rozmową komputerów:

| Ludzie | Komputery |
|---|---|
| „Cześć” | żądanie połączenia TCP |
| „Cześć” | odpowiedź: połączenie TCP przyjęte |
| „Która godzina?” | `GET http://gaia.cs.umass.edu/kurose_ross` |
| „14:00” | `<plik>` — treść strony |

Na slajdzie pada też ważne zdanie: protokół to **algorytm rozproszony**, który działa dzięki komunikacji — każda strona wykonuje swoją część i reaguje na komunikaty drugiej.

### Trzy składniki każdego protokołu

:::steps
1. **Format** — jak wygląda pojedynczy komunikat? (słowa, separatory, koniec linii, kolejność pól, bajty)
2. **Kolejność** — kto mówi pierwszy? Na jaki komunikat jaka jest odpowiedź?
3. **Akcje** — co robimy po odebraniu komunikatu? A co, gdy przyjdzie coś niezrozumiałego albo nic nie przyjdzie?
:::

:::own Przykład: nasz własny protokół z ćwiczeń C4
Tak wygląda rozmowa klienta z serwerem `QuoteDesk` (zobacz [C4](c4-text-protocol.html)). `S:` to serwer, `C:` to klient:

```text
S: HELLO QuoteDesk 1.0      <- kolejność: serwer mówi pierwszy
C: NAME ola                 <- format: słowo + spacja + nick, koniec linii \r\n
S: OK ola
C: QUOTES 2
S: BEGIN 2                  <- akcja: serwer wysyła dokładnie 2 linie i znacznik END
S: UDP: I told you once. Did you get it? Not my problem.
S: DNS: it's always DNS.
S: END
C: XYZ
S: ERR unknown command: XYZ <- akcja na błąd
```

Widać wszystkie trzy składniki: **format** linii, **kolejność** (najpierw `HELLO`, potem `NAME`) i **akcje** (co serwer robi po `QUOTES`, a co po nieznanej komendzie).
:::

## Po co to na ćwiczeniach i w projekcie? [[own]]

- Na **kolokwium** dostajesz opis protokołu słowami — musisz dokładnie trzymać się formatu (np. jedna linia zakończona `\r\n`) i kolejności.
- W **projekcie** musisz **zaprojektować i opisać własny protokół** między węzłami proxy. Szablon dokumentacji mówi wprost: opis ma pozwolić komuś **napisać własną implementację w innym języku** — czyli musi zawierać właśnie format, kolejność i akcje.
- Gdy program „wisi”, to prawie zawsze problem z protokołem: jedna strona czeka na komunikat, którego druga nigdy nie wyśle.

## Sprawdź się [[own]]

:::check Czym różni się host od routera?
Host to **system końcowy** — na nim działają aplikacje (np. przeglądarka, serwer WWW). Router to **przełącznik pakietów** w środku sieci — nie uruchamia aplikacji użytkownika, tylko przekazuje pakiety dalej w stronę celu.
:::

:::check Jakie trzy rzeczy definiuje protokół?
**Format** komunikatów, ich **kolejność** oraz **akcje** po wysłaniu/odebraniu komunikatu.
:::

:::check Co to jest RFC i kto je przygotowuje?
RFC (*Request for Comments*) to dokumenty ze standardami Internetu, np. opisy protokołów. Przygotowuje je IETF (*Internet Engineering Task Force*).
:::

:::check Dlaczego mówi się, że Internet to „sieć sieci”?
Bo składa się z wielu niezależnych sieci (dostawców ISP, sieci firm, uczelni, operatorów), które są ze sobą połączone. Nie ma jednej „głównej” sieci, która by wszystkim zarządzała.
:::

:::cheatsheet
### Definicje (na pamięć)
- **Host / system końcowy** — urządzenie na brzegu sieci, uruchamia aplikacje.
- **Przełącznik pakietów** — router lub switch, przekazuje pakiety dalej.
- **Łącze** — światłowód, miedź, radio, satelita; ma **przepustowość** w bit/s.
- **Sieć** — urządzenia + routery + łącza zarządzane przez jedną organizację.
- **Internet** — sieć sieci, połączeni dostawcy ISP.
- **Protokół** — **format + kolejność + akcje** dla komunikatów.
- **RFC** — dokument ze standardem; **IETF** — kto je pisze.

### Dwa spojrzenia na Internet
1. „Śrubki i nakrętki”: hosty, routery/switche, łącza, sieci.
2. „Usługa”: infrastruktura dla aplikacji + interfejs programistyczny (w Javie: gniazda `Socket`, `DatagramSocket`).

### Jak opisać protokół krok po kroku (projekt, kolokwium)
1. Wypisz uczestników (klient, serwer, proxy).
2. Dla każdego komunikatu podaj **dokładny format** (słowa, spacje, `\r\n`).
3. Opisz **kolejność**: kto zaczyna, co jest odpowiedzią na co.
4. Opisz **akcje i błędy**: co zrobić z nieznaną komendą, brakiem odpowiedzi.
5. Dodaj **przykładową rozmowę** (jak w RFC).
:::
