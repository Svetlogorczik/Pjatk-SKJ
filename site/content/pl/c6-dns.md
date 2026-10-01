---
title: DNS od środka — zapytanie zbudowane ręcznie, bajt po bajcie
short: DNS ręcznie (UDP, bajty)
description: Budujemy w Javie zapytanie DNS z bajtów, wysyłamy je przez UDP na port 53 i rozkodowujemy odpowiedź — nagłówek, flagi, nazwy z kompresją, TTL, adresy. Plus analiza DNS w nslookup i Wiresharku.
kind: lab
source: logika ćwiczeń 6 (zapytanie DNS jako tablica bajtów wysłane do 8.8.8.8 i analiza odpowiedzi) oraz „Wireshark Lab: DNS” (Kurose & Ross) — zadania przerobione
---

## W skrócie

- **DNS** tłumaczy nazwy (np. `pjwstk.edu.pl`) na adresy IP. Zapytania idą zwykle **UDP na port 53**.
- Wiadomość DNS = **nagłówek 12 bajtów** + sekcja **pytań** + sekcje **odpowiedzi**, **autorytatywna** i **dodatkowa**.
- Nazwa domeny jest zapisana jako **etykiety**: `07 example 03 com 00`.
- W odpowiedziach nazwy są często **skompresowane**: dwa bajty `C0 0C` = „nazwa jak pod offsetem 12”.
- W Javie `byte` ma znak (−128…127) — zawsze rób **`& 0xFF`**.

:::origin
Na ćwiczeniach 6 zapytanie DNS było wpisane w programie jako gotowa tablica bajtów (dla jednej konkretnej domeny), wysyłane przez UDP do publicznego serwera DNS na port 53, a odpowiedź trzeba było rozebrać na pola: ID, flagi, liczniki, nazwę, typ, klasę, TTL i adres IP. Do tego laboratorium Wireshark DNS (nslookup, cache DNS, śledzenie zapytań). Nasza wersja **sama buduje** zapytanie dla dowolnej domeny i typu, obsługuje kompresję nazw i kilka typów rekordów.
:::

## Co robi DNS? [[own]]

Komputer zna adresy IP, ludzie wolą nazwy. Gdy wpisujesz `www.example.com`:

:::steps
1. Twój komputer sprawdza **własny cache DNS** — może zna już odpowiedź.
2. Jeśli nie, pyta **lokalny serwer DNS** (np. od operatora albo `8.8.8.8`).
3. Lokalny serwer w razie potrzeby pyta po kolei serwery **root** → **TLD** (`.com`) → **autorytatywny** dla `example.com` i zapamiętuje wynik na czas **TTL**.
4. Twój komputer dostaje odpowiedź — z punktu widzenia klienta to jedno pytanie i jedna odpowiedź.
:::

Typy rekordów, które spotkasz:

| Typ | Kod | Co zawiera |
|---|---|---|
| **A** | 1 | adres IPv4 |
| **NS** | 2 | nazwa serwera autorytatywnego dla domeny |
| **CNAME** | 5 | „to jest alias, prawdziwa nazwa to…” |
| **SOA** | 6 | informacje o strefie (często w odpowiedzi „nie ma takiej nazwy”) |
| **MX** | 15 | serwer poczty (z priorytetem) |
| **AAAA** | 28 | adres IPv6 |

## Format wiadomości DNS

### Nagłówek — 12 bajtów

| Bajty | Pole | Znaczenie |
|---|---|---|
| 0–1 | **ID** | numer zapytania; odpowiedź ma **ten sam** ID |
| 2–3 | **Flags** | flagi (niżej) |
| 4–5 | **QDCOUNT** | liczba pytań |
| 6–7 | **ANCOUNT** | liczba rekordów odpowiedzi |
| 8–9 | **NSCOUNT** | liczba rekordów autorytatywnych |
| 10–11 | **ARCOUNT** | liczba rekordów dodatkowych |

Liczby 2-bajtowe są zapisane **big-endian** (najpierw starszy bajt): `00 02` = 2.

### Flagi (16 bitów)

| Bit(y) | Pole | Znaczenie |
|---|---|---|
| 15 | **QR** | 0 = zapytanie, 1 = odpowiedź |
| 14–11 | **OPCODE** | 0 = zwykłe zapytanie |
| 10 | **AA** | odpowiedź autorytatywna |
| 9 | **TC** | odpowiedź ucięta (*truncated*) |
| 8 | **RD** | proszę o rekurencję (*recursion desired*) |
| 7 | **RA** | serwer obsługuje rekurencję (*recursion available*) |
| 6–4 | **Z** | zarezerwowane (0) |
| 3–0 | **RCODE** | 0 = OK, 3 = **NXDOMAIN** (nie ma takiej nazwy) |

:::example Rozkodowanie flag krok po kroku [[own]]
Zapytanie ma flagi `0x0100`, odpowiedź `0x8180`.

1. `0x0100` = `0000 0001 0000 0000` → tylko bit 8 = 1 → **RD = 1** („szukaj za mnie do skutku”).
2. `0x8180` = `1000 0001 1000 0000` → bit 15 (**QR = 1**, to odpowiedź), bit 8 (**RD = 1**, skopiowany z pytania), bit 7 (**RA = 1**, serwer umie rekurencję), RCODE = 0 (OK).
3. W Javie: `int qr = flags >> 15 & 1; int rcode = flags & 0xF;`
:::

### Pytanie (question)

- **QNAME** — nazwa jako ciąg etykiet: bajt długości + litery, na końcu bajt `00`.
- **QTYPE** — 2 bajty, np. `00 01` = A.
- **QCLASS** — 2 bajty, `00 01` = IN (Internet).

`www.example.com` → `03 77 77 77 07 65 78 61 6D 70 6C 65 03 63 6F 6D 00` (3 litery `www`, 7 liter `example`, 3 litery `com`, koniec).

### Rekord odpowiedzi (resource record)

| Pole | Rozmiar | Uwagi |
|---|---|---|
| NAME | zmienny | często **wskaźnik** `C0 0C` |
| TYPE | 2 B | np. 1 = A |
| CLASS | 2 B | 1 = IN |
| TTL | **4 B** | ile sekund wolno trzymać w cache |
| RDLENGTH | 2 B | długość danych |
| RDATA | RDLENGTH | np. 4 bajty adresu IPv4 |

:::note Kompresja nazw (wskaźnik)
Jeśli bajt długości zaczyna się od bitów `11` (czyli `>= 0xC0`), to **nie jest** etykieta, tylko **wskaźnik**: dwa bajty, z których 14 bitów to offset w wiadomości. `C0 0C` → offset `0x0C` = 12 — tam zaczyna się nazwa z pytania (zaraz po 12-bajtowym nagłówku). Dzięki temu nazwa nie jest powtarzana w każdej odpowiedzi.

Offset liczysz tak: `((b1 & 0x3F) << 8) | (b2 & 0xFF)`.
:::

## Przykład bajt po bajcie (prawdziwe dane z testu tej strony)

Zapytanie o `example.com`, typ A (29 bajtów):

```text
70 C5 01 00 00 01 00 00 00 00 00 00 07 65 78 61
6D 70 6C 65 03 63 6F 6D 00 00 01 00 01
```

| Bajty | Wartość | Znaczenie |
|---|---|---|
| `70 C5` | ID = 0x70C5 | losowy numer zapytania |
| `01 00` | flagi 0x0100 | RD = 1 |
| `00 01` | QDCOUNT = 1 | jedno pytanie |
| `00 00` ×3 | 0, 0, 0 | brak odpowiedzi i rekordów dodatkowych |
| `07 65 78 61 6D 70 6C 65` | 7 + „example” | pierwsza etykieta |
| `03 63 6F 6D` | 3 + „com” | druga etykieta |
| `00` | — | koniec nazwy |
| `00 01` | QTYPE = 1 | A |
| `00 01` | QCLASS = 1 | IN |

Odpowiedź z `8.8.8.8` (61 bajtów):

```text
70 C5 81 80 00 01 00 02 00 00 00 00 07 65 78 61
6D 70 6C 65 03 63 6F 6D 00 00 01 00 01 C0 0C 00
01 00 01 00 00 01 2C 00 04 AC 42 93 F3 C0 0C 00
01 00 01 00 00 01 2C 00 04 68 14 17 9A
```

| Bajty | Znaczenie |
|---|---|
| `70 C5` | ten sam ID — to odpowiedź na **nasze** pytanie |
| `81 80` | QR = 1, RD = 1, RA = 1, RCODE = 0 |
| `00 01 00 02 00 00 00 00` | 1 pytanie, **2 odpowiedzi** |
| `07 65 … 00 00 01 00 01` | powtórzone pytanie |
| `C0 0C` | NAME = wskaźnik na offset 12 → `example.com` |
| `00 01 00 01` | TYPE = A, CLASS = IN |
| `00 00 01 2C` | TTL = 0x12C = **300 s** |
| `00 04` | RDLENGTH = 4 |
| `AC 42 93 F3` | 172.66.147.243 (0xAC = 172, 0x42 = 66, 0x93 = 147, 0xF3 = 243) |
| `C0 0C … 68 14 17 9A` | druga odpowiedź: 104.20.23.154 |

## Program DnsLookup

@code(c6/DnsLookup.java)

Najważniejsze fragmenty:

- `DataOutputStream.writeShort` zapisuje 2 bajty w kolejności **big-endian** — dokładnie tak, jak chce DNS.
- `u16` / `u32` czytają liczby bez znaku: `(m[i] & 0xFF) << 8 | (m[i+1] & 0xFF)`.
- `readName` obsługuje etykiety **i** wskaźniki; po wskaźniku nazwa się kończy, więc pozycja przesuwa się tylko o 2 bajty.
- Bufor 512 bajtów — klasyczny limit DNS przez UDP (większe odpowiedzi mają ustawione TC = 1).

:::danger `byte` w Javie ma znak [[own]]
Bajt `0xF3` w Javie to **−13**, nie 243. Bez `& 0xFF` adres IP wypisze się jako `-84.66.-109.-13`. Zawsze: `int value = b & 0xFF;`.
:::

### Uruchomienie

```bash
javac DnsLookup.java
java DnsLookup example.com
java DnsLookup pjwstk.edu.pl MX
java DnsLookup www.wikipedia.org A 1.1.1.1
```

Fragmenty prawdziwych wyników:

```text
Answer: pjwstk.edu.pl  type=MX  TTL=3600 s  data=pref 1 ASPMX.L.GOOGLE.COM
Answer: www.wikipedia.org  type=CNAME  TTL=20707 s  data=dyna.wikimedia.org
Answer: dyna.wikimedia.org  type=A  TTL=180 s  data=208.80.153.224
Flags: 0x8183  QR=1 OPCODE=0 AA=0 TC=0 RD=1 RA=1 RCODE=3
RCODE 3 = NXDOMAIN (no such name)
```

Zwróć uwagę na **CNAME**: `www.wikipedia.org` to alias — serwer od razu dorzucił rekord A dla prawdziwej nazwy.

## DNS w nslookup i Wiresharku

### nslookup — podstawy (z laboratorium)

```bash
nslookup www.example.com            # rekord A (domyślnie)
nslookup -type=NS example.com       # serwery autorytatywne domeny
nslookup -type=MX pjwstk.edu.pl     # serwery poczty
nslookup 8.8.8.8                    # odwrotne zapytanie: IP -> nazwa
nslookup www.example.com 1.1.1.1    # zapytaj konkretny serwer DNS
```

- Pierwsze linie wyniku to **serwer DNS, który odpowiedział** (nazwa i IP).
- **„Non-authoritative answer”** = odpowiedź z **cache** jakiegoś serwera, a nie od serwera autorytatywnego.

### Cache DNS na Twoim komputerze (z laboratorium)

Komputer pamięta ostatnie odpowiedzi. Żeby zobaczyć zapytania w Wiresharku, najpierw wyczyść cache:

| System | Polecenie |
|---|---|
| Windows | `ipconfig /flushdns` |
| macOS | `sudo killall -HUP mDNSResponder` |
| Linux (systemd) | `sudo resolvectl flush-caches` |

### Pytania do przechwycenia

Wyczyść cache DNS i przeglądarki, włącz Wireshark z filtrem `dns` (albo `ip.addr == <Twoje IP>`), otwórz dowolną stronę przez `http://`, a potem zrób kilka `nslookup`.

::::task Pytania D1–D8
1. Czy zapytanie DNS idzie przez UDP czy TCP?
2. Jaki jest **port docelowy** zapytania i **port źródłowy** odpowiedzi?
3. Na jaki adres IP wysłano zapytanie? Czy to Twój domyślny lokalny serwer DNS?
4. Ile pytań i ile odpowiedzi jest w **zapytaniu**? A w **odpowiedzi**?
5. Jakiego **typu** jest zapytanie wysłane przez `nslookup www...`? A przez `nslookup -type=NS ...`?
6. Co zawierają odpowiedzi typu NS? Czy są rekordy dodatkowe (*additional*) i co w nich jest?
7. Strona odwołuje się do obrazka z **tego samego** serwera. Czy przed drugim GET-em widać nowe zapytanie DNS? Dlaczego?
8. Znajdź parę zapytanie–odpowiedź po **ID**. Czy się zgadzają?

:::answer Odpowiedzi „wzorcowe” (konkretne liczby zależą od przechwycenia)
1. UDP.
2. Port docelowy zapytania **53**; port źródłowy odpowiedzi **53** (port klienta jest losowy).
3. Adres lokalnego serwera DNS — porównaj z `ipconfig /all` (Windows) albo `resolvectl status` / `/etc/resolv.conf` (Linux).
4. Zapytanie: 1 pytanie, 0 odpowiedzi. Odpowiedź: 1 pytanie (powtórzone) i ≥ 1 odpowiedź.
5. `nslookup www...` → typ **A** (często też osobno **AAAA**). `-type=NS` → typ **NS**.
6. Nazwy serwerów autorytatywnych; w sekcji *additional* często są ich **adresy IP** (rekordy A/AAAA) — „gratis”, choć o nie nie pytaliśmy.
7. Nie — adres jest już w **cache DNS** komputera, więc drugi GET idzie od razu.
8. Tak — odpowiedź zawsze ma ten sam ID co zapytanie; po tym klient je dopasowuje.
:::
::::

## Zadania

::::task 1. Inny serwer DNS
Zapytaj o tę samą nazwę serwery `8.8.8.8`, `1.1.1.1` i swój lokalny. Porównaj TTL. Dlaczego mogą się różnić?

:::answer
TTL w odpowiedzi z cache to **pozostały** czas życia wpisu — każdy serwer zapamiętał rekord w innym momencie, więc „odlicza” od innego miejsca.
:::
::::

::::task 2. Rekord TXT
Dodaj obsługę typu **TXT** (kod 16). RDATA to ciąg: bajt długości + tekst (może być kilka takich ciągów).

:::answer
W `typeCode` dodaj `case "TXT": return 16;`, w `typeName` `case 16: return "TXT";`, a w `rdata`: pętla `int p = start; while (p < start + len) { int l = m[p] & 0xFF; sb.append(new String(m, p + 1, l, StandardCharsets.US_ASCII)); p += 1 + l; }`.
:::
::::

::::task 3. Rozbierz bajty z Wiresharka
W Wiresharku kliknij prawym na sekcję DNS odpowiedzi → *Copy → …as Hex Stream*. Wklej do programu jako tablicę bajtów i przepuść przez `decode(...)`. Czy wynik zgadza się z drzewkiem Wiresharka?
::::

:::cheatsheet
### Nagłówek (12 B)
ID(2) · FLAGS(2) · QDCOUNT(2) · ANCOUNT(2) · NSCOUNT(2) · ARCOUNT(2) — big-endian.

### Flagi
QR(15) · OPCODE(14–11) · AA(10) · TC(9) · RD(8) · RA(7) · Z(6–4) · RCODE(3–0).
- zapytanie: `0x0100` (RD=1); typowa odpowiedź: `0x8180`; NXDOMAIN: RCODE = 3.

### Budowa zapytania krok po kroku
1. `writeShort(id)`, `writeShort(0x0100)`, `writeShort(1)`, 3× `writeShort(0)`.
2. Dla każdej etykiety: `writeByte(dł.)` + bajty liter; na końcu `writeByte(0)`.
3. `writeShort(QTYPE)` (A=1, NS=2, CNAME=5, MX=15, AAAA=28), `writeShort(1)` (IN).
4. Wyślij UDP na **port 53**, bufor odpowiedzi 512 B, timeout.

### Rozbiór odpowiedzi krok po kroku
1. Sprawdź **ID**, potem flagi i RCODE.
2. Przeczytaj liczniki.
3. Przeskocz pytanie: nazwa + 4 bajty.
4. Dla każdego rekordu: NAME (uwaga na `C0 xx`), TYPE, CLASS, **TTL (4 B)**, RDLENGTH, RDATA.
5. A → 4 bajty `& 0xFF` z kropkami.

### Pułapki
- `byte` ze znakiem → `& 0xFF`.
- Wskaźnik: `b >= 0xC0`, offset = `((b1 & 0x3F) << 8) | b2`; po wskaźniku nazwa się kończy.

### nslookup / Wireshark
- `nslookup nazwa`, `-type=NS`, `-type=MX`, `nslookup IP` (odwrotne).
- „Non-authoritative” = z cache.
- Filtr `dns`, `udp.port == 53`; flush: `ipconfig /flushdns`.
:::
