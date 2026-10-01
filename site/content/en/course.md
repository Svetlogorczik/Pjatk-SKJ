---
title: How to pass SKJ — components, rules and dates
short: Course rules & dates
description: What the course consists of, what is known for sure from the materials (500-point project, server-based test, documentation, anti-plagiarism), what the materials don't say, and the study plan I suggest.
kind: mixed
source: the 2025/26 project specification, documentation template, test template and lectures from the repository; plan and advice — original guide
---

## In short

- The course: **SKJ — Computer Networks and Network Programming in Java** (*Sieci komputerowe i programowanie sieciowe w Javie*). The lectures are given by **dr inż. Kazimierz Zieliński**, based on the Kurose & Ross textbook.
- The materials show two main practical components: a **server-based test** (tasks over TCP) and a **project** (a proxy network, up to **500 pts**).
- A project **without the PDF documentation is not graded**; **plagiarism** gives **negative points**.
- The dates in the materials belong to the **2025/2026** edition — they are already **archival**. The instructor will announce new ones.

@dates

:::warning How current are the dates?
Today's status (passed / in how many days) is computed automatically in your browser. All dates above come from the previous edition's files, so most likely they **don't apply** to your semester — treat them as a rough guide to when things happen. [[own]]
:::

## Course components

| Component | What it is | Details on this site |
|---|---|---|
| **Lectures** | chapter 1 of Kurose & Ross: the Internet, protocols, network edge and core, delay, security, layers, history | [W1–W8](w1-internet.html) |
| **Labs** | Wireshark (HTTP, DNS, TCP) + Java programs with TCP/UDP sockets | [C1–C7](c1-relay.html) |
| **Practical test** | a TCP client solving tasks sent by the instructor's server; every task several times | [Practical test](exam.html) |
| **Project** | a proxy network connecting TCP/UDP key servers; protocol documentation | [Project](project.html) |

## What is known for sure (from the materials)

### The project (2025/26 edition)

- A complete, correct project is worth **500 points**; tiers: 150 / 300 / 300 / 400 / 500 (details in the [Project](project.html)).
- You **declare** the project for a specific number of points and it is tested only for that — if it doesn't meet **all** requirements for that number, it gets **0**.
- Java 8, standard classes only; files without packages; compilation with `javac *.java`; tests on localhost.
- You submit in **GAKKO**: your `.java` files, optionally scripts, **"Dokumentacja (index number).pdf"** (a mandatory condition for grading).
- Deadline in the specification: **19.12.2025, 23:55** (the group instructor could change it).
- Anti-plagiarism: plagiarised projects **are not checked**, plagiarism gives **negative points**. The instructor writes plainly that it doesn't matter whether the author was a friend, an AI or StackOverflow — independent work is what counts.

### The practical test (2025/26 edition)

- You connect to the instructor's server, log in, and receive a **password** and a **task description**.
- **5 tasks**, each must be done **5 times**; the server grades the answers immediately.
- The description contained the **end time of the test** (in 2025/26: **11.01.2026, 12:45**) — changes after it are not taken into account.

### Wireshark labs

- Kurose & Ross labs: **HTTP**, **DNS**, **TCP** — answering questions based on your own traffic capture (or the ready-made *trace* files).

## What the materials DON'T say — ask your instructor

:::danger No data in the repository [[own]]
The files contain no information about:

- the **grading scale** and the passing threshold (how many points for a 3.0 / 5.0),
- the **weight** of the test vs the project,
- whether there is an **exam / test on the lectures** and what it looks like,
- **attendance** and **retake** rules.

I don't guess these — check the syllabus, GAKKO, or ask your instructor in the first classes. If there is a lecture test, the **Cheat sheet** button in every topic W1–W8 and the [All cheat sheets](cheatsheets.html) page will help.
:::

## A suggested semester plan [[own]]

The dates of the previous edition suggest roughly this rhythm: labs from October, the project until mid-December, the test in January. Based on that I suggest:

:::steps
1. **October** — lectures W1–W8 (1–2 topics a week) + [C1](c1-relay.html), [C2](c2-http.html), [C3](c3-tcp-server.html). Install the JDK and Wireshark ([Start](start.html)).
2. **November** — [C4](c4-text-protocol.html), [C5](c5-tcp-udp.html), [C6](c6-dns.html), [C7](c7-tcp-wireshark.html). Start training with the [DrillServer](exam.html) — once a week, against the clock.
3. **As soon as the project specification is published** — build the 150-point stage immediately, then the next ones. Write the documentation **in parallel**, not the last night.
4. **A week before the project deadline** — tests on **another computer**, `javac *.java` in an empty folder, the PDF, submission in GAKKO.
5. **Before the test** — 2–3 full `DrillServer` trainings with 5 repetitions; a ready client template.
:::

## Checklist before submitting the project [[own]]

- [ ] the folder contains **only** my `.java` files, scripts and the PDF (no `.class`, no IntelliJ project, no provided servers/clients),
- [ ] no file has a `package ...` line,
- [ ] `javac *.java` in an **empty** folder compiles without errors (ideally `javac --release 8 *.java`),
- [ ] no hard-coded IP addresses, ports, paths,
- [ ] all requirements for the **declared** number of points work,
- [ ] the PDF is named with the index number and contains a **complete protocol description**,
- [ ] the template's table is filled in (author, index, points, test script),
- [ ] submitted in GAKKO **before** the deadline.

:::cheatsheet
### Components
- Lectures (Kurose & Ross, ch. 1) · Labs (Wireshark + Java) · Server-based test · Proxy project (up to 500 pts).

### Rules known for sure (2025/26)
- Project: you declare points → everything must work, otherwise 0.
- PDF documentation = condition for grading; protocol description like an RFC.
- Java 8, no packages, `javac *.java`, localhost, no hard-coding.
- Plagiarism → negative points.
- Test: 5 tasks × 5 repetitions, hard deadline.

### 2025/26 dates (archival)
- Project: 19.12.2025, 23:55 (GAKKO).
- End of the test: 11.01.2026, 12:45.

### Ask your instructor
- grading scale and threshold · weight of test/project · lecture exam? · attendance and retakes · current deadlines.
:::
