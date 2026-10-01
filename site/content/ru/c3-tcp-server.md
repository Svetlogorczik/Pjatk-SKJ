---
title: TCP-сокеты в Java — свой мини HTTP-сервер и клиент
short: TCP в Java: мини HTTP-сервер
description: Пишем клиента, который отправляет правильный запрос HTTP/1.1 и читает весь ответ, и сервер с тремя страницами, отвечающий 200, 304 или 404. По шагам: потоки, readLine, flush, пустая строка.
kind: lab
source: логика ćwiczeń 3 (HTTP-клиент + сервер с поддержкой 304 и 404) — задание переделано
---

## Коротко

- TCP-сервер: `new ServerSocket(port)` → `accept()` → потоки → прочитать запрос → отправить ответ → `close()`.
- TCP-клиент: `new Socket(host, port)` → отправить запрос → **`flush()`** → читать ответ до конца.
- HTTP/1.1 требует: строки заканчиваются **`\r\n`**, есть заголовок **`Host`**, после заголовков — **пустая строка**.
- Логика сервера: нет страницы → **404**; страница не менялась с `If-Modified-Since` → **304** (без тела); иначе → **200** с телом.

:::origin
На ćwiczeniach 3 был TCP-клиент, отправлявший запрос GET (цель: получить `200 OK` вместо `400` и увидеть **весь** ответ), и простой сервер с 3 «фальшивыми» файлами: он отвечал `304`, когда в запросе была дата последнего изменения, и `404` для несуществующих файлов. Ниже — **наша версия**: правильный HTTP/1.1 с настоящим форматом дат HTTP, полным чтением заголовков и дополнительными кодами 400/405.
:::

## Как выглядит разговор через TCP-сокеты

@svg(sockets-tcp, caption=Сервер ждёт в accept(), клиент подключается конструктором Socket. Потом обе стороны пишут и читают потоки. ⏳ = метод блокирует (ждёт).)

Потоки в Java «слоёные» — сырые байты оборачиваем в более удобные классы:

| Класс | Что даёт | Заметка |
|---|---|---|
| `InputStream` / `OutputStream` | сырые байты из сокета | `socket.getInputStream()` |
| `InputStreamReader` | байты → символы | укажи кодировку, например `StandardCharsets.UTF_8` |
| `BufferedReader` | `readLine()` — читает целую строку | возвращает `null`, когда другая сторона закрыла соединение |
| `PrintWriter` / `BufferedWriter` | удобная запись текста | данные лежат в буфере **до `flush()`** |

:::danger Три ошибки, из-за которых программа «висит» [[own]]
1. **Нет `flush()`** — данные остались в буфере, другая сторона ничего не получила и ждёт.
2. **Нет конца строки** — `readLine()` на другой стороне ждёт `\n`, которого так и не было (`print("abc")` вместо `println`/`"abc\r\n"`).
3. **Чтение «до конца», когда никто не закрывает** — цикл `while ((line = in.readLine()) != null)` закончится, только когда другая сторона **закроет соединение**. В HTTP это делается заголовком `Connection: close`.
:::

## Почему сервер отвечает 400 Bad Request?

Классическая проблема с занятий. Сравни:

```text
НЕПРАВИЛЬНО:                                  ПРАВИЛЬНО:
GET /index.html HTTP/1.1\r\n                  GET /index.html HTTP/1.1\r\n
If-Modified-Since: 2018-05-05T11:50:56\r\n    Host: localhost\r\n
                                              Connection: close\r\n
                                              If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT\r\n
                                              \r\n
```

| Проблема в «неправильной» версии | Последствие |
|---|---|
| нет заголовка `Host` | настоящие серверы HTTP/1.1 отвечают **400** |
| нет пустой строки в конце | сервер ждёт ещё заголовков — **зависает** или отвечает по таймауту |
| дата в неверном формате | сервер игнорирует заголовок или отклоняет запрос |

Дата в HTTP **всегда** в формате `Mon, 06 Oct 2025 09:00:00 GMT` (английские сокращения, зона GMT). [[own]]

## Сервер: MiniWebServer

@code(c3/MiniWebServer.java)

Что делает сервер, по шагам:

:::steps
1. Открывает порт (`ServerSocket`) и в цикле ждёт клиента (`accept()`).
2. Читает **строку запроса**, например `GET /index.html HTTP/1.1`.
3. Читает **заголовки** строка за строкой до **пустой строки**; кладёт их в map (имена строчными буквами).
4. Проверяет формат: 3 части и версия `HTTP/...` — иначе **400**. Метод не GET — **405**.
5. Ищет страницу в map. Нет — **404**.
6. Если есть `If-Modified-Since` и страница **не новее** этой даты — **304** без тела.
7. Во всех остальных случаях — **200** с телом, `Content-Length` и `Last-Modified`.
8. Закрывает соединение (`try-with-resources`).
:::

:::note Логика 304 одним предложением
«Отправляй тело, только если страница на сервере **новее** копии клиента»: `if (!page.lastModified.isAfter(clientCopy)) → 304`.
:::

## Клиент: HttpProbe

@code(c3/HttpProbe.java)

- Собирает запрос с `\r\n` в конце каждой строки и **пустой строкой** в конце.
- Отправляет `Connection: close`, поэтому после ответа сервер закроет соединение, и цикл `readLine() != null` закончится — получаем **весь** ответ.
- Первая строка ответа — строка статуса: `split(" ", 3)` даёт версию, код и описание.

## Запуск

:::steps
1. `javac MiniWebServer.java HttpProbe.java`
2. Терминал 1: `java MiniWebServer 8080`
3. Терминал 2: `java HttpProbe localhost 8080 /index.html` → **200**
4. `java HttpProbe localhost 8080 /index.html "Mon, 06 Oct 2025 09:00:00 GMT"` → **304**
5. `java HttpProbe localhost 8080 /net-takoj.html` → **404**
6. Открой в браузере `http://localhost:8080/schedule.html` и посмотри трафик в Wireshark (Loopback, фильтр `tcp.port == 8080`).
:::

Вывод шага 4 (настоящая распечатка из проверки этой страницы):

```text
> GET /index.html HTTP/1.1
> Host: localhost
> User-Agent: HttpProbe/1.0
> Connection: close
> If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT
>
< HTTP/1.1 304 Not Modified
< Date: Thu, 01 Oct 2026 08:35:08 GMT
< Server: MiniWebServer/1.0
< Last-Modified: Mon, 06 Oct 2025 09:00:00 GMT
< Connection: close
<
Status code: 304 (Not Modified)
```

## Задания

::::task 1. Четвёртая страница
Добавь страницу `/faq.html` с датой изменения 1 декабря 2025, 10:00 GMT. Проверь 200 и 304.

:::answer
В конструкторе: `pages.put("/faq.html", new Page("<h1>FAQ</h1>", "Mon, 01 Dec 2025 10:00:00 GMT"));` — дата должна быть строго в формате HTTP (день недели должен совпадать с датой, иначе парсинг бросит исключение).
:::
::::

::::task 2. Метод HEAD
`HEAD` работает как `GET`, но сервер отправляет **только заголовки** (с тем же `Content-Length`), без тела. Добавь его.

:::answer
Вместо отказа всем, кроме GET: `boolean head = parts[0].equals("HEAD"); if (!head && !parts[0].equals("GET")) → 405`. В `send` выводи `Content-Length` как раньше, но при `head == true` **не вызывай** `out.write(bodyBytes)`. Проверка: `curl -I http://localhost:8080/index.html`.
:::
::::

::::task 3. Много клиентов сразу
Сейчас сервер обслуживает клиентов **по очереди**. Сделай так, чтобы каждый клиент обслуживался в отдельном потоке.

:::answer
```java
while (true) {
    Socket client = server.accept();
    new Thread(() -> {
        try (Socket c = client) {
            handle(c);
        } catch (IOException e) {
            log("client error: " + e.getMessage());
        }
    }).start();
}
```
Map `pages` только читается, поэтому безопасна для многих потоков. Если когда-нибудь будешь её менять — используй `ConcurrentHashMap`.
:::
::::

::::task 4. Обязательный Host
По HTTP/1.1 запрос без заголовка `Host` должен получать **400**. Добавь это правило и проверь через `nc`/`telnet`.

:::answer
После чтения заголовков: `if (parts[2].equals("HTTP/1.1") && !headers.containsKey("host")) { send(out, 400, "Bad Request", null, "<h1>Host header required</h1>"); return; }`. Проверка: `printf 'GET / HTTP/1.1\r\n\r\n' | nc localhost 8080`.
:::
::::

::::task 5. Настоящий сервер
Запусти `HttpProbe` на странице из лабораторной по HTTP (адрес в [C2](c2-http.html)), порт 80. Потом повтори с `If-Modified-Since`, равным дате из `Last-Modified`. Что получится?
::::

:::cheatsheet
### TCP-сервер — каркас
1. `ServerSocket ss = new ServerSocket(port);`
2. `Socket s = ss.accept();` (ждёт)
3. `BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), UTF_8));`
4. `PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), UTF_8));`
5. прочитать → обработать → `out.print(...); out.flush();`
6. `s.close();` (и снова к `accept()`)

### TCP-клиент — каркас
1. `Socket s = new Socket(host, port);`
2. отправить строки + `flush()`
3. читать `while ((line = in.readLine()) != null)` (если сервер закрывает) или до маркера конца
4. `s.close();`

### Запрос HTTP/1.1 — шаблон
```text
GET /put HTTP/1.1\r\n
Host: imya-servera\r\n
Connection: close\r\n
[If-Modified-Since: Mon, 06 Oct 2025 09:00:00 GMT\r\n]
\r\n
```

### Решение сервера
| Условие | Ответ |
|---|---|
| плохая строка запроса | 400 |
| метод ≠ GET | 405 |
| нет страницы | 404 |
| страница не новее If-Modified-Since | 304 (без тела) |
| иначе | 200 + тело |

### «Висит»? Проверь
`flush()` · конец строки `\r\n` · пустая строка после заголовков · `Connection: close`.
:::
