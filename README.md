# SKJ — сайт для учёбы (PL / EN / RU)

Неофициальный учебный сайт по предмету **SKJ — Sieci komputerowe i programowanie sieciowe w Javie** (PJATK):
лекции простым языком, переделанные задания с ćwiczeń с рабочим кодом на Java, тренировка к коллоквиуму,
путеводитель по проекту, конспект к каждой теме, словарь PL ↔ EN ↔ RU, светлая и тёмная тема.

*Nieoficjalna strona do nauki SKJ (PL/EN/RU). Unofficial study site for SKJ (PL/EN/RU).*

- Готовый сайт: папка [`docs/`](docs/) (статический HTML, работает на GitHub Pages и просто с диска).
- Исходники сайта: папка [`site/`](site/) (Markdown + генератор на Node.js).
- Остальные папки репозитория (`1`, `2`, `3`, … , `Wyklady`, `Specyfikacja projektu 2025 2026`, PDF) — исходные материалы курса, сайт их **не копирует**: задания переделаны (та же логика, другой текст и код).

## Как опубликовать на GitHub Pages

1. Слей ветку с сайтом в `main` (или выбери эту ветку в настройках ниже).
2. На GitHub: **Settings → Pages → Build and deployment**.
3. **Source:** *Deploy from a branch*. **Branch:** `main`, папка **`/docs`** → **Save**.
4. Через 1–2 минуты сайт будет по адресу `https://<логин>.github.io/<репозиторий>/` (для этого репозитория: `https://svetlogorczik.github.io/Pjatk-SKJ/`).

Если в настройках выбрать папку `/ (root)`, тоже сработает: корневой `index.html` перенаправит в `docs/`.

Открыть локально без сервера: дважды щёлкни `docs/index.html` (всё работает, кроме буфера обмена в некоторых браузерах — тогда текст выделяется для ручного копирования).

## Как пересобрать сайт после правок

```bash
cd site
npm install        # один раз: marked, highlight.js, katex
npm run build      # пишет готовый сайт в ../docs
npm run test:java  # компилирует все примеры Java с javac --release 8
```

`docs/` генерируется целиком — **не правь файлы в `docs/` руками**, правь `site/`.

## Структура исходников

```text
site/
  build.mjs              генератор: Markdown -> HTML (3 языка), KaTeX, подсветка кода
  config.mjs             языки, порядок страниц, важные даты
  i18n/ui.json           все надписи интерфейса на PL / EN / RU
  content/<pl|en|ru>/    тексты страниц (Markdown с front matter)
  content/glossary.json  словарь терминов
  snippets/              код примеров (Java, .properties, .bat, .sh) — попадает в docs/files/
  diagrams/              SVG-схемы, подписи переводятся через {{ключ}}
  styles/                CSS по методологии BEM: base/ + blocks/<блок>.css (собирается в один main.css)
  static/assets/js/blocks/  JS по блокам: theme-toggle, nav, code-block, cheatsheet, calc, …
  lib/                   markdown.mjs, layout.mjs, widgets.mjs, util.mjs
```

### Что можно писать в Markdown

| Синтаксис | Результат |
|---|---|
| `$L/R$`, `$$ … $$` | формулы KaTeX (рендерятся при сборке) |
| `:::note`, `:::tip`, `:::warning`, `:::danger`, `:::example`, `:::analogy`, `:::formula` | цветные блоки-выноски |
| `:::own Заголовок` | блок «добавлено автором сайта — этого нет в лекции» |
| `[[own]]` | маленький значок «от автора» внутри текста |
| `:::origin` | «откуда задание» (кратко об исходном задании с занятий) |
| `::::task …` + `:::answer` / `:::hint` / `:::check` | задание с раскрывающимся решением / подсказкой / вопросом |
| `:::steps` + нумерованный список | шаги «по порядку» |
| `:::timeline` + список | линия времени |
| `:::cheatsheet` | конспект темы (кнопка «Конспект» + страница всех конспектов) |
| `:::calc delay` / `rtt` / `binom` / `tput` | интерактивный калькулятор с решением по шагам |
| `@code(c3/MiniWebServer.java)` | блок кода из `snippets/` с кнопками «Копировать» и «Скачать» |
| `@svg(delay, caption=…)` | SVG-схема из `diagrams/` |
| `@dates`, `@cards`, `@legend`, `@glossary` | виджеты (даты, карточки разделов, легенда, словарь) |

### BEM

Каждый блок интерфейса (`header`, `nav`, `callout`, `code-block`, `cheatsheet`, `calc`, …) — отдельный CSS-файл
в `site/styles/blocks/` и, если нужен, отдельный JS-файл в `site/static/assets/js/blocks/`.
Классы: `block`, `block__element`, `block--modifier`. Даже текст из Markdown получает BEM-классы блока `content`
(`content__paragraph`, `content__table`, …).

### KaTeX

Формулы рендерятся **во время сборки** (быстро, без мигания, работают без JavaScript).
В сайт положены `katex.min.css`, шрифты, `katex.min.js`, `auto-render` и `copy-tex`
(`docs/assets/vendor/katex/`) — внешние CDN не используются. `copy-tex` позволяет скопировать формулу как LaTeX.

## Важно

- Задания на сайте **переделаны**: логика как на занятиях, но другой текст, другие данные и свой код — чтобы не было похоже на работы прошлых лет.
- Полного решения проекта на сайте **нет специально** — проекты проверяются антиплагиатом.
- Всё, что добавлено автором сайта (объяснения, примеры, советы, калькуляторы), помечено значком **✍️**.
- Даты взяты из материалов набора 2025/2026 и помечены как архивные; актуальные сроки сообщает преподаватель.
- Лекции основаны на J. F. Kurose, K. W. Ross, *Computer Networking: A Top-Down Approach*; Wireshark Labs © J.F. Kurose & K.W. Ross.
