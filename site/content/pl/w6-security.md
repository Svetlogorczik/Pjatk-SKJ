---
title: Bezpieczeństwo sieci — kim są „źli goście” i co potrafią
short: Bezpieczeństwo
description: Malware (wirusy, robaki, spyware, botnety), ataki DoS/DDoS, podsłuchiwanie pakietów (sniffing) i podszywanie się pod adres IP (spoofing).
kind: mixed
source: Wykład 1 — Chapter 1, slajdy 1-57…1-62 (Kurose & Ross)
---

## W skrócie

- Internet **nie był projektowany z myślą o bezpieczeństwie** — zakładano grupę ufających sobie użytkowników. Dziś projektanci „gonią” atakujących, a bezpieczeństwo dotyczy **wszystkich warstw**.
- **Malware**: **wirus** (potrzebuje, żebyś coś uruchomił), **robak** (uruchamia się sam), **spyware** (szpieguje), zainfekowane komputery tworzą **botnet**.
- **DoS / DDoS**: zalewanie serwera lub łącza fałszywym ruchem, żeby prawdziwi użytkownicy nie mogli z niego korzystać.
- **Sniffing**: w mediach rozgłoszeniowych (WiFi, współdzielony Ethernet) można podsłuchać cudze pakiety — Wireshark to właśnie sniffer.
- **IP spoofing**: wysłanie pakietu z **fałszywym adresem nadawcy**.

## Czym zajmuje się bezpieczeństwo sieci?

Według wykładu ta dziedzina bada trzy rzeczy:

1. jak **atakujący** mogą atakować sieci komputerowe,
2. jak **bronić** sieci przed atakami,
3. jak **projektować** architektury odporne na ataki.

Problem: Internet **nie był projektowany z myślą o bezpieczeństwie**. Pierwotna wizja to *„grupa wzajemnie ufających sobie użytkowników podłączonych do przezroczystej sieci”*. Dlatego projektanci protokołów **nadrabiają zaległości** (*playing catch-up*), a kwestie bezpieczeństwa pojawiają się **we wszystkich warstwach** (o warstwach — [W7](w7-layers.html)).

## Malware — złośliwe oprogramowanie

Malware może dostać się na hosta jako:

| Rodzaj | Jak się rozprzestrzenia | Kluczowa różnica |
|---|---|---|
| **Wirus** | samoreplikująca się infekcja przez **odebranie i uruchomienie** obiektu, np. załącznika e-mail | potrzebuje **Twojej akcji** (otwarcia pliku) |
| **Robak** (*worm*) | samoreplikująca się infekcja przez **bierne odebranie** obiektu, który **sam się uruchamia** | rozprzestrzenia się **bez Twojego udziału** |

Dodatkowo:

- **Spyware** — może nagrywać naciśnięcia klawiszy, odwiedzane strony i wysyłać te dane na serwer atakującego.
- Zainfekowany host może zostać włączony do **botnetu** — armii przejętych komputerów, którą używa się do **spamu** albo ataków **DDoS**.

## Odmowa usługi: DoS i DDoS

**Denial of Service (DoS)** — atakujący sprawiają, że zasoby (serwer, przepustowość łącza) są **niedostępne dla prawdziwego ruchu**, bo **zalewają** je fałszywym ruchem.

Schemat ataku z wykładu (**rozproszony** DoS, czyli DDoS):

:::steps
1. **Wybierz cel** (np. serwer sklepu).
2. **Włam się do hostów** w różnych miejscach sieci (zbuduj botnet).
3. **Wysyłaj pakiety** do celu ze wszystkich przejętych hostów naraz.
:::

:::own Związek z W5 i z Twoim projektem
- DDoS to celowe doprowadzenie do natężenia ruchu $La/R > 1$ (zobacz [W5](w5-performance.html)) — kolejki rosną bez końca i normalne pakiety giną.
- „Mini-DoS” możesz przypadkiem zrobić sam sobie: jeśli Twój serwer albo proxy obsługuje klientów **po kolei** i czeka bez limitu na jednego wolnego klienta, wszyscy inni stoją. Rozwiązania: **timeouty** (`setSoTimeout`) i **wątki** na klientów.
:::

## Przechwytywanie pakietów: sniffing

**Podsłuchiwanie pakietów** (*packet sniffing*):

- działa w **mediach rozgłoszeniowych** — współdzielony Ethernet, sieci bezprzewodowe — gdzie sygnał dociera do wielu urządzeń naraz,
- karta sieciowa w trybie **„promiscuous”** czyta i zapisuje **wszystkie** przechodzące pakiety, nie tylko te adresowane do niej — **łącznie z hasłami!**

Na slajdzie: C podsłuchuje pakiet `src:B dest:A payload`, który wcale nie jest do niego.

**Wireshark**, którego używamy na laboratoriach, to właśnie (darmowy) **sniffer pakietów**.

:::own Zobacz to na własne oczy — HTTP Basic Auth
W [C2](c2-http.html) zobaczysz w Wiresharku nagłówek `Authorization: Basic c3R1ZGVudDpzaWVjMTIz`. To **nie jest szyfrowanie**, tylko kodowanie Base64 — każdy, kto przechwyci pakiet, odczyta login i hasło:

@code(c2/Base64Peek.java)

Dlatego hasła przesyła się tylko przez **HTTPS** (TLS szyfruje dane). Więcej o kryptografii jest w rozdziale 8 podręcznika.
:::

:::warning Etyka i prawo [[own]]
Podsłuchuj **tylko własny ruch** albo sieć, na którą masz zgodę (np. laboratorium). Przechwytywanie cudzej komunikacji bez uprawnień jest w Polsce przestępstwem (m.in. art. 267 Kodeksu karnego).
:::

## Fałszywa tożsamość: IP spoofing

**IP spoofing** — wysłanie pakietu z **fałszywym adresem źródłowym**. Na slajdzie host C wysyła pakiet z polem `src:B`, udając, że jest B.

Po co? Np. żeby ukryć prawdziwe źródło ataku DDoS albo „wrobić” kogoś innego. Wykład zapowiada: *dużo więcej o bezpieczeństwie — w całym kursie, a szczególnie w rozdziale 8.*

:::own Ciekawostka: dlaczego spoofing łatwiej robić z UDP niż z TCP?
TCP wymaga **trójetapowego nawiązania połączenia** (zobacz [C7](c7-tcp-wireshark.html)): odpowiedź serwera wraca na **prawdziwy** adres B, więc atakujący C jej nie zobaczy i nie dokończy połączenia. UDP nie ma żadnego „uścisku dłoni”, więc fałszywy datagram po prostu zostaje przyjęty.
:::

## Sprawdź się [[own]]

:::check Czym różni się wirus od robaka?
Wirus potrzebuje akcji użytkownika (uruchomienia zainfekowanego obiektu, np. załącznika). Robak rozprzestrzenia się sam — wystarczy, że host biernie odbierze obiekt, który sam się wykona.
:::

:::check Co to jest botnet i do czego służy?
Sieć przejętych (zainfekowanych) komputerów sterowanych przez atakującego. Używa się jej do spamu i ataków DDoS.
:::

:::check Dlaczego sniffing działa szczególnie łatwo w WiFi?
Bo radio to medium rozgłoszeniowe — sygnał dociera do wszystkich urządzeń w zasięgu, a karta w trybie promiscuous zapisze wszystkie pakiety.
:::

:::check Czy kodowanie Base64 chroni hasło?
Nie. Base64 to tylko zmiana zapisu, którą każdy może odwrócić. Ochronę daje szyfrowanie (np. HTTPS/TLS).
:::

:::cheatsheet
### Ataki — tabelka do zapamiętania
| Atak | Co robi |
|---|---|
| Wirus | infekcja po **uruchomieniu** obiektu (załącznik) |
| Robak | infekcja, która **sama się uruchamia** |
| Spyware | nagrywa klawisze, strony, wysyła dane |
| Botnet | armia zainfekowanych hostów (spam, DDoS) |
| DoS/DDoS | zalanie zasobu fałszywym ruchem |
| Sniffing | podsłuch pakietów w medium rozgłoszeniowym |
| IP spoofing | fałszywy adres nadawcy |

### Fakty z wykładu
- Internet projektowano dla „ufających sobie użytkowników” → bezpieczeństwo dodawane później, **we wszystkich warstwach**.
- DDoS: 1) wybierz cel, 2) przejmij hosty (botnet), 3) wysyłaj pakiety do celu.
- Wireshark = darmowy sniffer pakietów.

### Praktyka (od autora)
- Base64 ≠ szyfrowanie → hasła tylko przez HTTPS.
- Timeouty i wątki chronią Twój serwer przed „zawieszeniem” przez jednego klienta.
- Sniffuj tylko własny ruch.
:::
