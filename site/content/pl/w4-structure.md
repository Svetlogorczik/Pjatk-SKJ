---
title: Struktura Internetu — „sieć sieci”
short: Struktura Internetu
description: Jak miliony małych dostawców Internetu łączą się w jedną sieć — ISP, IXP, peering, sieci regionalne, tier-1 i sieci dostawców treści.
kind: mixed
source: Wykład 1 — Chapter 1, slajdy 1-34…1-43 (Kurose & Ross)
---

## W skrócie

- Hosty łączą się z Internetem przez **dostawców dostępu** (*access ISP*). Te z kolei muszą być połączone ze sobą, żeby **dowolne dwa hosty** mogły się komunikować.
- Połączenie „każdy z każdym” nie skaluje się — potrzeba $O(N^2)$ łączy.
- Dlatego powstała **hierarchia**: dostawcy dostępowi → **regionalni ISP** → globalni **tier-1**; do tego punkty wymiany ruchu **IXP** i sieci **dostawców treści** (Google, Microsoft, Akamai).
- O kształcie tej sieci zdecydowały głównie **ekonomia** i **polityka państw**.

## Problem: miliony dostawców dostępu

Hosty łączą się przez **dostawców dostępu do Internetu** (*access ISP*): domowych, firmowych, uczelnianych, komercyjnych. Ale dostawcy dostępu też muszą być **połączeni ze sobą** — inaczej klient jednego ISP nie wyśle nic klientowi innego.

Wynikowa „sieć sieci” jest bardzo złożona. Wykład buduje ją **krok po kroku**, zaczynając od pytania: **jak połączyć miliony dostawców dostępu?**

### Pomysł 1: każdy z każdym — nie skaluje się

Połączenie każdego ISP z każdym innym bezpośrednio wymaga $O(N^2)$ połączeń.

:::own Ile to dokładnie łączy?
Dla $N$ sieci liczba par to:

$$
\binom{N}{2} = \frac{N(N-1)}{2}
$$

- 10 sieci → 45 łączy,
- 1000 sieci → 499 500 łączy,
- 1 000 000 sieci → ok. $5 \cdot 10^{11}$ łączy. Zupełnie nierealne.
:::

### Pomysł 2: jeden globalny ISP tranzytowy

Każdy dostawca dostępu łączy się z **jednym globalnym ISP tranzytowym** (*global transit ISP*). Między **klientem** (dostawcą dostępu) a **dostawcą** (globalnym ISP) jest **umowa ekonomiczna** — klient płaci za tranzyt.

### Pomysł 3: konkurencja i punkty wymiany ruchu (IXP)

Jeśli jeden globalny ISP to dobry biznes, pojawią się **konkurenci**: ISP A, ISP B, ISP C… A ich klienci chcą rozmawiać ze sobą, więc ci globalni ISP też muszą się połączyć:

- **IXP** (*Internet exchange point*) — **punkt wymiany ruchu**, miejsce, gdzie wiele sieci łączy się naraz,
- **łącza peeringowe** (*peering link*) — bezpośrednie połączenie dwóch sieci „po partnersku”.

### Pomysł 4: sieci regionalne

Mogą powstać **regionalni ISP**, którzy zbierają ruch od lokalnych dostawców dostępu i łączą go z większymi ISP.

### Pomysł 5: sieci dostawców treści

**Dostawcy treści** (*content provider networks*, np. Google, Microsoft, Akamai) budują **własne sieci**, żeby przynieść usługi i treści **blisko użytkowników**.

## Obraz końcowy

Na samej górze („w centrze”) jest **mała liczba dobrze połączonych, dużych sieci**:

- komercyjni **ISP tier-1** (np. Level 3, Sprint, AT&T, NTT) — zasięg krajowy i międzynarodowy,
- **sieci dostawców treści** (np. Google, Facebook) — prywatna sieć, która łączy ich centra danych z Internetem, często **omijając** tier-1 i regionalnych ISP.

```text
          [Tier-1 ISP] ===== [Tier-1 ISP] ===== [Google]
              |    \            /     |            |
            [IXP]   \__[IXP]__/     [IXP]          |
              |                       |            |
       [Regional ISP]          [Regional ISP]      |
        /     |     \            /     \           |
  [access] [access] [access] [access] [access] ----+
     ISP      ISP      ISP      ISP      ISP
```

Wykład pokazuje też mapę sieci tier-1 **Sprint (2019)**: punkty obecności **POP** (*point of presence*) — miejsca, gdzie sieć ma sprzęt i gdzie podłączają się klienci — oraz łącza do sieci partnerskich (peering) i do sieci klientów.

:::own Kto komu płaci? (uproszczenie)
- **Klient → dostawca**: mniejsza sieć płaci większej za **tranzyt** (dostęp do „całego Internetu”).
- **Peering**: dwie sieci podobnej wielkości wymieniają ruch między swoimi klientami — często **bez opłat**, bo obie na tym zyskują.
- **IXP**: wspólna „hala”, gdzie wiele sieci może się połączyć naraz, zamiast kłaść osobne kable do każdej.

Przykłady IXP: w Polsce m.in. PLIX i EPIX; w Europie bardzo duże są DE-CIX (Frankfurt) i AMS-IX (Amsterdam).
:::

:::own Dlaczego Google ma własną sieć?
Żeby film na YouTube nie płynął przez pół świata. Własna sieć + serwery blisko użytkowników = **mniejsze opóźnienie**, mniej przeciążeń i mniejsze koszty tranzytu. Ścieżkę pakietów przez różne sieci możesz zobaczyć programem `traceroute` / `tracert` — o nim w [W5](w5-performance.html).
:::

## Sprawdź się [[own]]

:::check Dlaczego nie łączy się każdego ISP z każdym?
Bo liczba łączy rośnie jak $N(N-1)/2$, czyli $O(N^2)$ — dla milionów sieci to miliardy łączy.
:::

:::check Czym jest IXP?
Punktem wymiany ruchu: miejscem, w którym wiele sieci (ISP, dostawców treści) łączy się ze sobą i wymienia ruch.
:::

:::check Podaj dwa przykłady sieci tier-1 z wykładu i dwa przykłady sieci dostawców treści.
Tier-1: Level 3, Sprint, AT&T, NTT. Dostawcy treści: Google, Microsoft, Akamai, Facebook.
:::

:::check Co to jest POP?
*Point of presence* — miejsce w sieci dostawcy (grupa routerów), do którego mogą podłączyć się klienci.
:::

:::cheatsheet
### Hierarchia (od dołu)
1. **Access ISP** — dostawca dostępu (dom, firma, uczelnia).
2. **Regional ISP** — zbiera ruch od lokalnych dostawców.
3. **Tier-1 ISP** — globalni (Level 3, Sprint, AT&T, NTT).
4. **Content provider networks** — Google, Microsoft, Akamai, Facebook: własne sieci, omijają tier-1.

### Słowa-klucze
- **Klient–dostawca**: umowa ekonomiczna, klient płaci za tranzyt.
- **IXP**: punkt wymiany ruchu wielu sieci.
- **Peering**: bezpośrednie połączenie dwóch sieci.
- **POP**: punkt obecności sieci dostawcy.
- Struktura wynika z **ekonomii i polityki**, nie z jednego planu.

### Liczenie połączeń „każdy z każdym”
- $N(N-1)/2$, np. 1000 sieci → 499 500 łączy → $O(N^2)$, nie skaluje się.
:::
