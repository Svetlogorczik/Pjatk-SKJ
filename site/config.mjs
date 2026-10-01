// Site configuration: languages, sections, page order and important dates.
// Page titles/descriptions live in the front matter of content/<lang>/<id>.md.

export const SITE = {
  // Used only for the <meta name="generator"> tag and footer.
  name: 'SKJ',
  repoUrl: 'https://github.com/Svetlogorczik/Pjatk-SKJ',
};

export const LANGS = [
  { code: 'pl', label: 'Polski', short: 'PL', locale: 'pl-PL' },
  { code: 'en', label: 'English', short: 'EN', locale: 'en-GB' },
  { code: 'ru', label: 'Русский', short: 'RU', locale: 'ru-RU' },
];

export const DEFAULT_LANG = 'pl';

// Sidebar sections (labels come from i18n/ui.json -> sections.<id>).
export const SECTIONS = ['start', 'lectures', 'labs', 'exam', 'project'];

// Order matters: it defines the sidebar and prev/next links.
export const PAGES = [
  { id: 'index', section: null, home: true },
  { id: 'start', section: 'start', icon: '🧭' },
  { id: 'course', section: 'start', icon: '🎓' },
  { id: 'glossary', section: 'start', icon: '📖' },
  { id: 'cheatsheets', section: 'start', icon: '🗂️', generated: true },

  { id: 'w1-internet', section: 'lectures', num: 'W1', icon: '🌐' },
  { id: 'w2-edge', section: 'lectures', num: 'W2', icon: '📶' },
  { id: 'w3-core', section: 'lectures', num: 'W3', icon: '🔀' },
  { id: 'w4-structure', section: 'lectures', num: 'W4', icon: '🕸️' },
  { id: 'w5-performance', section: 'lectures', num: 'W5', icon: '⏱️' },
  { id: 'w6-security', section: 'lectures', num: 'W6', icon: '🛡️' },
  { id: 'w7-layers', section: 'lectures', num: 'W7', icon: '🧅' },
  { id: 'w8-history', section: 'lectures', num: 'W8', icon: '📜' },

  { id: 'c1-relay', section: 'labs', num: 'C1', icon: '🏃' },
  { id: 'c2-http', section: 'labs', num: 'C2', icon: '🔎' },
  { id: 'c3-tcp-server', section: 'labs', num: 'C3', icon: '🖥️' },
  { id: 'c4-text-protocol', section: 'labs', num: 'C4', icon: '💬' },
  { id: 'c5-tcp-udp', section: 'labs', num: 'C5', icon: '📨' },
  { id: 'c6-dns', section: 'labs', num: 'C6', icon: '📇' },
  { id: 'c7-tcp-wireshark', section: 'labs', num: 'C7', icon: '🦈' },

  { id: 'exam', section: 'exam', icon: '📝' },
  { id: 'project', section: 'project', icon: '🛠️' },
];

// Dates found in the repository materials (edition 2025/2026).
// Status (past / upcoming) is computed at build time and refreshed in the browser.
export const DATES = [
  {
    id: 'spec',
    date: '2025-12-05',
    time: null,
    source: 'Specyfikacja projektu 2025 2026.zip',
  },
  {
    id: 'project',
    date: '2025-12-19',
    time: '23:55',
    source: 'Specyfikacja projektu.pdf, pkt 5',
  },
  {
    id: 'test',
    date: '2026-01-11',
    time: '12:45',
    source: 'Test_template/output.txt',
  },
];
