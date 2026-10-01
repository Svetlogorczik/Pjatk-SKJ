---
title: Wireshark i HTTP — czytamy rozmowę przeglądarki z serwerem
short: Wireshark + HTTP
description: Jak przechwycić ruch HTTP w Wiresharku i odczytać z niego wersję protokołu, nagłówki, kody statusu, warunkowy GET (304), dzielenie odpowiedzi na segmenty TCP, osadzone obiekty i uwierzytelnianie Basic.
kind: lab
source: logika laboratorium „Wireshark Lab: HTTP” (Kurose & Ross) z zajęć — pytania przerobione
---

## W skrócie

- **Wireshark** kopiuje każdą ramkę, która przechodzi przez kartę sieciową, i pokazuje ją warstwa po warstwie.
- **HTTP** to tekstowy protokół: żądanie (`GET /strona HTTP/1.1` + nagłówki + pusta linia) i odpowiedź (`HTTP/1.1 200 OK` + nagłówki + pusta linia + treść).
- **Warunkowy GET**: przeglądarka wysyła `If-Modified-Since`, a serwer odpowiada `304 Not Modified` **bez treści**, jeśli plik się nie zmienił.
- Długa odpowiedź HTTP jest dzielona przez TCP na **wiele segmentów**.
- `Authorization: Basic ...` to tylko **Base64** — hasło da się odczytać.

:::origin
Na zajęciach robi się laboratorium Wireshark HTTP z podręcznika Kurose & Ross: przechwytuje się ruch przeglądarki do kilku prostych stron na serwerze `gaia.cs.umass.edu` i odpowiada na pytania o nagłówki, kody statusu, warunkowy GET, długie dokumenty, obrazki osadzone w stronie i stronę chronioną hasłem. Poniżej masz **własny zestaw pytań** o te same rzeczy oraz instrukcję, **gdzie szukać odpowiedzi** — konkretne wartości zależą od Twojego przechwycenia.
:::

## Zanim zaczniesz (z instrukcji laboratorium)

:::steps
1. **Wyłącz VPN** — przy VPN dane wyższych warstw są zaszyfrowane i nic nie zobaczysz.
2. **Wyłącz HTTP/3 i QUIC** w przeglądarce — one też szyfrują ruch (od 2025 r. większość przeglądarek ma je domyślnie włączone).
3. Wpisuj adresy zaczynające się od **`http://`**, nie `https://`.
4. **Wyczyść pamięć podręczną** (cache) i historię przeglądarki; wyłącz ustawienia „prywatności”, które mogą zmieniać zapytania.
5. W polu filtra Wiresharka wpisz **`http`** (małymi literami) — zobaczysz tylko pakiety HTTP.
6. Pomiń zapytania o **`favicon.ico`** — to przeglądarka sama pyta o ikonkę strony.
:::

:::tip Nie możesz przechwycić ruchu na żywo?
Laboratorium udostępnia gotowe pliki przechwyceń (*trace files*, archiwum `wireshark-traces-9e.zip` na stronie autorów). Otwierasz je przez **File → Open** i odpowiadasz na pytania tak samo.
:::

## Wireshark w 2 minuty [[own]]

Okno Wiresharka ma trzy panele:

1. **Lista pakietów** — jeden wiersz na ramkę: numer (nadany przez Wiresharka, nie istnieje w pakiecie!), czas, źródło, cel, protokół, opis.
2. **Szczegóły pakietu** — drzewko warstw: Frame → Ethernet → IP → TCP → HTTP (to enkapsulacja z [W7](w7-layers.html)).
3. **Bajty** — surowa zawartość w hex i ASCII.

Najprzydatniejsze filtry:

| Filtr | Co pokazuje |
|---|---|
| `http` | tylko HTTP |
| `http.request.method == "GET"` | tylko żądania GET |
| `http.response.code == 304` | tylko odpowiedzi 304 |
| `ip.addr == 128.119.245.12` | ruch do/z danego adresu |
| `tcp.port == 8080` | ruch na porcie 8080 (np. Twój serwer z [C3](c3-tcp-server.html)) |
| `tcp` / `udp` / `dns` | dany protokół |

Kliknij prawym na pakiet → **Follow → TCP Stream** — zobaczysz całą rozmowę jako zwykły tekst (żądanie na czerwono, odpowiedź na niebiesko).

## Anatomia wiadomości HTTP [[own]]

Przykład (wymyślony, ale w dokładnie takim formacie jak prawdziwy):

```http
GET /lab/hello.html HTTP/1.1
Host: www.example.org
User-Agent: Mozilla/5.0 (Windows NT 10.0)
Accept: text/html
Accept-Language: pl,en-US;q=0.7
If-Modified-Since: Tue, 14 Oct 2025 08:15:00 GMT
Connection: keep-alive

```

```http
HTTP/1.1 200 OK
Date: Wed, 15 Oct 2025 10:02:11 GMT
Server: Apache
Last-Modified: Wed, 15 Oct 2025 09:59:00 GMT
Content-Length: 128
Content-Type: text/html; charset=UTF-8

<html>...128 bajtów treści...</html>
```

| Element | Gdzie | Co znaczy |
|---|---|---|
| `GET /lab/hello.html HTTP/1.1` | **linia żądania** | metoda, ścieżka, **wersja HTTP klienta** |
| `Host` | nagłówek | nazwa serwera (w HTTP/1.1 obowiązkowa) |
| `Accept-Language` | nagłówek | **języki**, które przeglądarka akceptuje (`q` = preferencja) |
| `If-Modified-Since` | nagłówek | „przyślij tylko, jeśli zmieniło się po tej dacie” |
| pusta linia | po nagłówkach | **koniec nagłówków** — bez niej serwer czeka w nieskończoność |
| `HTTP/1.1 200 OK` | **linia statusu** | **wersja HTTP serwera**, kod, opis |
| `Last-Modified` | nagłówek | kiedy plik zmieniono na serwerze |
| `Content-Length` | nagłówek | ile **bajtów treści** (bez nagłówków) |

Najważniejsze kody statusu:

| Kod | Znaczenie |
|---|---|
| 200 OK | wszystko dobrze, treść w odpowiedzi |
| 301 Moved Permanently | zasób przeniesiony (nagłówek `Location`) |
| 304 Not Modified | nie zmieniło się — **użyj swojej kopii z cache**, brak treści |
| 400 Bad Request | serwer nie zrozumiał żądania (np. zły format) |
| 401 Unauthorized | potrzebne logowanie (nagłówek `WWW-Authenticate`) |
| 404 Not Found | nie ma takiego zasobu |
| 405 Method Not Allowed | metoda (np. POST) niedozwolona |

## Część A — prosty GET i odpowiedź

Otwórz prostą, krótką stronę (np. pierwszy plik z laboratorium) i znajdź dwa pakiety: **GET** i odpowiedź.

::::task Pytania A1–A7
1. Jaką wersję HTTP deklaruje przeglądarka, a jaką serwer?
2. Jak nazywa się nagłówek z listą preferowanych języków i jakie języki w nim są?
3. Jaki jest adres IP Twojego komputera, a jaki serwera?
4. Jaki kod i opis statusu zwrócił serwer?
5. Kiedy plik był ostatnio zmieniony na serwerze?
6. Ile bajtów treści przysłał serwer?
7. Czy w surowych bajtach widać jakiś nagłówek, którego nie ma w liście pakietów?

:::hint Gdzie szukać
1. Linia żądania w pakiecie GET (ostatni wyraz) i linia statusu w odpowiedzi (pierwszy wyraz).
2. W GET: `Accept-Language`.
3. Warstwa IP pakietu GET: **Source** = Twój komputer, **Destination** = serwer. W odpowiedzi odwrotnie.
4. Linia statusu odpowiedzi, np. `200 OK`.
5. Nagłówek `Last-Modified` w odpowiedzi. Uwaga: serwer laboratorium **co minutę** ustawia datę modyfikacji na „teraz” — dlatego instrukcja każe odczekać ponad minutę przed przechwyceniem.
6. Nagłówek `Content-Length` (to długość samej treści). Nie myl z długością segmentu TCP (treść + nagłówki HTTP).
7. Porównaj drzewko HTTP z panelem bajtów — Wireshark zwykle pokazuje wszystkie nagłówki; odpowiedź „nie widzę dodatkowych” też jest poprawna, jeśli tak jest.
:::
::::

## Część B — warunkowy GET (304)

Przeglądarki trzymają kopie stron w **cache**. Gdy otwierasz tę samą stronę drugi raz, przeglądarka pyta serwer: „czy zmieniło się od…?”.

:::steps
1. Wyczyść cache, uruchom przechwytywanie.
2. Otwórz stronę, a zaraz potem **odśwież** ją (albo wpisz adres jeszcze raz).
3. Zatrzymaj przechwytywanie, filtr `http`.
:::

::::task Pytania B1–B4
1. Czy w **pierwszym** GET jest nagłówek `If-Modified-Since`?
2. Czy pierwsza odpowiedź zawiera treść pliku? Po czym to poznać?
3. Czy w **drugim** GET jest `If-Modified-Since`? Jaka data w nim jest i skąd przeglądarka ją wzięła?
4. Jaki kod statusu ma druga odpowiedź i czy zawiera treść?

:::answer Odpowiedzi „wzorcowe” (wartości dat zależą od przechwycenia)
1. Nie — przeglądarka nie ma jeszcze kopii (cache był pusty).
2. Tak — kod 200, nagłówek `Content-Length` > 0 i w Wiresharku widać sekcję *Line-based text data* z HTML-em.
3. Tak — data jest **taka sama jak `Last-Modified` z pierwszej odpowiedzi**: przeglądarka zapamiętała ją razem z kopią.
4. `304 Not Modified` — **bez treści**; przeglądarka pokazuje kopię z cache. (Uwaga z instrukcji: Chrome chętnie wysyła `If-Modified-Since`, Firefox i Safari bywają kapryśne.)
:::
::::

## Część C — długi dokument

Gdy plik HTML jest długi (w laboratorium ok. 4500 bajtów), **jedna** odpowiedź HTTP nie zmieści się w **jednym** segmencie TCP. TCP dzieli ją na kilka segmentów, a Wireshark oznacza je jako *„TCP segment of a reassembled PDU”*.

::::task Pytania C1–C4
1. Ile żądań GET wysłała przeglądarka? W którym pakiecie (numer z listy) jest GET po długi plik?
2. W którym pakiecie jest linia statusu odpowiedzi?
3. Jaki to kod i opis?
4. Ile segmentów TCP z danymi było potrzebnych na całą odpowiedź?

:::hint Gdzie szukać
Wyczyść filtr (żeby widzieć też segmenty TCP). W pakiecie z odpowiedzią HTTP rozwiń sekcję **„[N Reassembled TCP Segments]”** — Wireshark wypisze, ile segmentów złożył i których pakietów użył. Linia statusu jest w **pierwszym** z nich, ale Wireshark wyświetla odpowiedź HTTP przy **ostatnim**.
:::
::::

## Część D — strona z osadzonymi obiektami

Strona może zawierać obrazki leżące na **innych serwerach**. HTML zawiera tylko **adresy (URL)** obrazków, więc przeglądarka musi wysłać po nie osobne żądania.

::::task Pytania D1–D2
1. Ile żądań GET wysłała przeglądarka i na jakie adresy IP?
2. Czy obrazki pobierano **po kolei** (szeregowo), czy **równolegle**? Jak to sprawdzić?

:::hint Jak to sprawdzić
Spójrz na kolumnę **Time** i na **porty źródłowe TCP**: jeśli drugi GET wyszedł, zanim przyszła odpowiedź na pierwszy, i to innym połączeniem TCP (inny port źródłowy) — pobieranie było równoległe. Jeśli każdy GET czeka na zakończenie poprzedniego — szeregowe.
:::
::::

## Część E — strona chroniona hasłem (HTTP Basic)

W laboratorium jest strona, do której trzeba się zalogować (login i hasło podaje instrukcja laboratorium).

::::task Pytania E1–E3
1. Co serwer odpowiada na **pierwszy** GET (kod i opis)?
2. Jakie **nowe pole** pojawia się w **drugim** GET (po wpisaniu hasła)?
3. Odkoduj wartość tego pola. Co widzisz?

:::answer Odpowiedzi „wzorcowe”
1. `401 Unauthorized` (z nagłówkiem `WWW-Authenticate`, który prosi o logowanie).
2. `Authorization: Basic <ciąg znaków>`.
3. Ciąg to **Base64** z tekstu `login:hasło`. To **nie jest szyfrowanie** — każdy, kto przechwyci pakiet, może go odkodować (zobacz [W6](w6-security.html)).
:::
::::

Odkodować możesz programem w Javie — bez żadnych stron internetowych:

@code(c2/Base64Peek.java)

```bash
javac Base64Peek.java
java Base64Peek "Basic c3R1ZGVudDpzaWVjMTIz"
# Decoded  : student:siec123
```

## Ćwiczenie bez Internetu: Twój własny serwer [[own]]

Możesz zrobić całe laboratorium lokalnie: uruchom `MiniWebServer` z [C3](c3-tcp-server.html) i podglądaj ruch w Wiresharku na interfejsie **Loopback** (Windows: „Adapter for loopback traffic capture” z Npcap; Linux: `lo`), z filtrem `tcp.port == 8080`.

```bash
# odpowiedź 200 i wszystkie nagłówki
curl -v http://localhost:8080/index.html
# warunkowy GET -> 304
curl -v -H "If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT" http://localhost:8080/index.html
```

:::cheatsheet
### Przed przechwyceniem
- Bez VPN, bez HTTP/3/QUIC, adres `http://`, pusty cache.
- Filtr `http`; ignoruj `favicon.ico`.

### Gdzie co jest
| Pytanie | Gdzie szukać |
|---|---|
| wersja HTTP | linia żądania / linia statusu |
| języki | `Accept-Language` |
| IP klienta/serwera | warstwa IP: Source / Destination |
| kod statusu | linia statusu odpowiedzi |
| data modyfikacji | `Last-Modified` |
| rozmiar treści | `Content-Length` |
| warunkowy GET | `If-Modified-Since` → `304 Not Modified` (bez treści) |
| wiele segmentów | „[N Reassembled TCP Segments]” |
| równolegle czy po kolei | kolumna Time + porty źródłowe TCP |
| logowanie | `401` → `Authorization: Basic` (Base64!) |

### Filtry
`http`, `http.request.method == "GET"`, `http.response.code == 304`, `ip.addr == X`, `tcp.port == 8080`; Follow → TCP Stream.

### Kody
200 OK · 301 przeniesiono · 304 bez zmian · 400 zły format · 401 logowanie · 404 brak · 405 zła metoda.
:::
