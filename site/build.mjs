// Static site generator for the SKJ study site.
// Usage:  cd site && npm install && npm run build   ->  output in ../docs (GitHub Pages: /docs folder)
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

import { SITE, LANGS, DEFAULT_LANG, PAGES, DATES } from './config.mjs';
import { createRenderer, parseFrontMatter } from './lib/markdown.mjs';
import { makeWidgets } from './lib/widgets.mjs';
import { renderPage } from './lib/layout.mjs';
import { escapeHtml, wordCount } from './lib/util.mjs';

const ROOT = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.resolve(ROOT, '..', 'docs');
const CONTENT = path.join(ROOT, 'content');
const STATIC = path.join(ROOT, 'static');
const STYLES = path.join(ROOT, 'styles');
const SNIPPETS = path.join(ROOT, 'snippets');
const DIAGRAMS = path.join(ROOT, 'diagrams');

const ui = JSON.parse(fs.readFileSync(path.join(ROOT, 'i18n', 'ui.json'), 'utf8'));
const glossary = JSON.parse(fs.readFileSync(path.join(CONTENT, 'glossary.json'), 'utf8'));
const buildDate = process.env.BUILD_DATE ?? new Date().toISOString().slice(0, 10);

// Scripts needed by widgets (assets/js/blocks/<name>.js).
const WIDGET_SCRIPTS = { calc: 'calc', glossary: 'glossary', dates: 'dates', cards: 'progress' };

function rmrf(p) { fs.rmSync(p, { recursive: true, force: true }); }
function copyDir(src, dst) {
  if (!fs.existsSync(src)) return;
  fs.mkdirSync(dst, { recursive: true });
  for (const e of fs.readdirSync(src, { withFileTypes: true })) {
    const s = path.join(src, e.name);
    const d = path.join(dst, e.name);
    if (e.isDirectory()) copyDir(s, d);
    else fs.copyFileSync(s, d);
  }
}
function write(file, content) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, content);
}

/** Inlines `@import url("...")` statements of styles/main.css into one file (sources stay split per BEM block). */
function bundleCss(entry) {
  const dir = path.dirname(entry);
  return fs.readFileSync(entry, 'utf8').replace(/@import\s+url\(["']?([^"')]+)["']?\);?/g, (m, rel) => {
    const file = path.join(dir, rel);
    return `/* ==== ${rel} ==== */\n${fs.readFileSync(file, 'utf8').trim()}\n`;
  });
}

function main() {
  rmrf(OUT);
  copyDir(STATIC, OUT);
  write(path.join(OUT, '.nojekyll'), '');

  // CSS bundle
  write(path.join(OUT, 'assets/css/main.css'), bundleCss(path.join(STYLES, 'main.css')));

  // KaTeX (vendored: CSS + fonts; formulas are pre-rendered at build time)
  const katexDist = path.join(ROOT, 'node_modules/katex/dist');
  const katexOut = path.join(OUT, 'assets/vendor/katex');
  fs.mkdirSync(katexOut, { recursive: true });
  fs.copyFileSync(path.join(katexDist, 'katex.min.css'), path.join(katexOut, 'katex.min.css'));
  copyDir(path.join(katexDist, 'fonts'), path.join(katexOut, 'fonts'));
  fs.copyFileSync(path.join(ROOT, 'node_modules/katex/LICENSE'), path.join(katexOut, 'LICENSE'));
  // Runtime scripts: copy-tex (copying a formula gives its LaTeX source) is loaded on pages with math;
  // katex.min.js + auto-render are shipped for pages you may later write by hand with $...$ formulas.
  fs.copyFileSync(path.join(katexDist, 'katex.min.js'), path.join(katexOut, 'katex.min.js'));
  fs.mkdirSync(path.join(katexOut, 'contrib'), { recursive: true });
  for (const f of ['copy-tex.min.js', 'auto-render.min.js']) {
    fs.copyFileSync(path.join(katexDist, 'contrib', f), path.join(katexOut, 'contrib', f));
  }

  // Downloadable source files
  copyDir(SNIPPETS, path.join(OUT, 'files'));

  const pagerOrder = PAGES.filter((p) => !p.home && !p.generated && p.id !== 'glossary');
  const report = [];

  for (const lang of LANGS) {
    const t = ui[lang.code];
    // Pass 1: front matter of every page (titles for nav/cards/pager)
    const sources = {};
    const pageMeta = {};
    for (const p of PAGES) {
      if (p.generated) {
        pageMeta[p.id] = { title: t.allCheatsheets, short: t.allCheatsheets, description: t.cheatsheetHint, kind: 'own' };
        continue;
      }
      const file = path.join(CONTENT, lang.code, `${p.id}.md`);
      if (!fs.existsSync(file)) throw new Error(`Missing content file: ${path.relative(ROOT, file)}`);
      const { data, body } = parseFrontMatter(fs.readFileSync(file, 'utf8'));
      sources[p.id] = body;
      pageMeta[p.id] = data;
    }

    const widgets = makeWidgets({ t, lang: lang.code, locale: lang.locale, pages: PAGES, pageMeta, dates: DATES, glossary, buildDate });
    const render = createRenderer({
      lang: lang.code, t, snippetsDir: SNIPPETS, diagramsDir: DIAGRAMS, filesUrl: '../files', widgets,
    });

    const sheets = [];
    let sheetsMath = false;

    // Pass 2: render pages
    for (const p of PAGES) {
      if (p.generated) continue;
      const meta = pageMeta[p.id];
      let result;
      try {
        result = render(sources[p.id], { pageId: p.id });
      } catch (e) {
        throw new Error(`[${lang.code}/${p.id}] ${e.message}`);
      }
      if (result.cheatsheet) {
        sheets.push({ page: p, meta, html: result.cheatsheet });
        if (result.hasMath) sheetsMath = true;
      }
      const idx = pagerOrder.findIndex((x) => x.id === p.id);
      const prev = idx > 0 ? pagerOrder[idx - 1] : null;
      const next = idx >= 0 && idx < pagerOrder.length - 1 ? pagerOrder[idx + 1] : null;
      const scripts = [...new Set([...result.widgets].map((w) => WIDGET_SCRIPTS[w]).filter(Boolean))];
      const words = wordCount(result.html);
      const html = renderPage({
        lang: lang.code, langs: LANGS, t, page: p, meta, pages: PAGES, pageMeta,
        body: result.html, toc: p.home ? [] : result.toc, cheatsheet: result.cheatsheet, hasMath: result.hasMath,
        scripts, readingMinutes: p.home ? 0 : Math.max(1, Math.round(words / 180)),
        prev: prev && { id: prev.id, title: pageMeta[prev.id].title },
        next: next && { id: next.id, title: pageMeta[next.id].title },
        buildDate, siteRepo: SITE.repoUrl,
      });
      write(path.join(OUT, lang.code, `${p.id}.html`), html);
      report.push(`${lang.code}/${p.id}.html  words=${words}${result.cheatsheet ? '  +cheatsheet' : ''}${result.hasMath ? '  +math' : ''}`);
    }

    // Generated page: all cheat sheets in one printable page
    const sheetPage = PAGES.find((p) => p.generated);
    const toc = sheets.map((s) => ({ id: `sheet-${s.page.id}`, depth: 2, text: `${s.page.num ? `${s.page.num} · ` : ''}${s.meta.title}` }));
    const body = `<p class="content__paragraph">${escapeHtml(t.cheatsheetHint)}. <button type="button" class="button button--ghost button--small" onclick="window.print()">🖨 ${escapeHtml(t.print)}</button></p>
${sheets.map((s) => `<section class="sheet">
<h2 class="content__heading content__heading--h2 sheet__title" id="sheet-${s.page.id}"><a class="sheet__link" href="${s.page.id}.html">${s.page.num ? `<span class="sheet__num">${s.page.num}</span>` : ''}${escapeHtml(s.meta.title)}</a></h2>
<div class="sheet__body">${s.html}</div>
</section>`).join('\n')}`;
    write(path.join(OUT, lang.code, `${sheetPage.id}.html`), renderPage({
      lang: lang.code, langs: LANGS, t, page: sheetPage, meta: pageMeta[sheetPage.id], pages: PAGES, pageMeta,
      body, toc, cheatsheet: '', hasMath: sheetsMath, scripts: ['code-block'], readingMinutes: 0, buildDate, siteRepo: SITE.repoUrl,
    }));
  }

  // Root: pick language (saved choice -> browser language -> default)
  const codes = LANGS.map((l) => l.code);
  write(path.join(OUT, 'index.html'), `<!DOCTYPE html>
<html lang="${DEFAULT_LANG}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${escapeHtml(ui[DEFAULT_LANG].siteTitle)}</title>
<link rel="icon" href="assets/img/favicon.svg" type="image/svg+xml">
<script>
(function () {
  var langs = ${JSON.stringify(codes)}, pick = null;
  try { pick = localStorage.getItem('skj-lang'); } catch (e) {}
  if (langs.indexOf(pick) < 0) {
    var list = navigator.languages || [navigator.language || ''];
    for (var i = 0; i < list.length && !pick; i++) {
      var c = String(list[i]).slice(0, 2).toLowerCase();
      if (langs.indexOf(c) >= 0) pick = c;
    }
  }
  if (langs.indexOf(pick) < 0) pick = '${DEFAULT_LANG}';
  location.replace(pick + '/index.html');
})();
</script>
<style>body{font:16px/1.6 system-ui,sans-serif;max-width:32rem;margin:15vh auto;padding:0 1rem;text-align:center}a{display:inline-block;margin:.25rem .5rem}</style>
</head>
<body>
<h1>SKJ</h1>
<p>${LANGS.map((l) => `<a href="${l.code}/index.html" hreflang="${l.code}">${escapeHtml(l.label)}</a>`).join(' ')}</p>
</body>
</html>
`);

  // 404 for GitHub Pages
  write(path.join(OUT, '404.html'), `<!DOCTYPE html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>404 — SKJ</title>
<style>body{font:16px/1.6 system-ui,sans-serif;max-width:32rem;margin:15vh auto;padding:0 1rem;text-align:center}</style></head>
<body><h1>404</h1><p>${LANGS.map((l) => `<a href="/" data-l="${l.code}">${escapeHtml(l.label)}</a>`).join(' · ')}</p>
<script>/* Go to the site root whatever sub-path GitHub Pages uses (e.g. /Repo/docs/). */
(function(){var p=location.pathname.split('/');var i=p.indexOf('pl');if(i<0)i=p.indexOf('en');if(i<0)i=p.indexOf('ru');var base=i>0?p.slice(0,i).join('/')+'/':'/';document.querySelectorAll('a').forEach(function(a){a.href=base+a.getAttribute('data-l')+'/index.html';});})();</script>
</body></html>
`);

  console.log(report.join('\n'));
  console.log(`\nBuilt ${report.length + LANGS.length} pages into ${path.relative(process.cwd(), OUT) || OUT} (build date ${buildDate}).`);
}

main();
