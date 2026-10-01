// Page layout (BEM blocks: page, header, logo, lang-switch, theme-toggle, layout, sidebar, nav,
// article, toc, pager, cheatsheet, footer).
import { escapeHtml } from './util.mjs';

const ICONS = {
  menu: '<svg class="header__icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16"/></svg>',
  sun: '<svg class="theme-toggle__icon theme-toggle__icon--sun" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>',
  moon: '<svg class="theme-toggle__icon theme-toggle__icon--moon" viewBox="0 0 24 24" aria-hidden="true"><path d="M20 14.5A8.5 8.5 0 0 1 9.5 4a8.5 8.5 0 1 0 10.5 10.5Z"/></svg>',
  close: '<svg class="cheatsheet__icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6 6 18"/></svg>',
  print: '<svg class="cheatsheet__icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 9V3h10v6M7 17H5a2 2 0 0 1-2-2v-4a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2h-2M7 14h10v7H7z"/></svg>',
  notes: '<svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 3h9l4 4v14H6zM14 3v5h5M9 12h7M9 16h7"/></svg>',
  check: '<svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true"><path d="m5 12 4 4 10-10"/></svg>',
  arrowL: '<svg class="pager__icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M15 6l-6 6 6 6"/></svg>',
  arrowR: '<svg class="pager__icon" viewBox="0 0 24 24" aria-hidden="true"><path d="m9 6 6 6-6 6"/></svg>',
};

// Runs before first paint to avoid a flash of the wrong theme.
const THEME_BOOT = `(function(){try{var t=localStorage.getItem('skj-theme');if(t!=='light'&&t!=='dark'){t=window.matchMedia('(prefers-color-scheme: dark)').matches?'dark':'light';}document.documentElement.setAttribute('data-theme',t);}catch(e){document.documentElement.setAttribute('data-theme','light');}})();`;

function navHtml({ t, pages, pageMeta, current }) {
  const groups = [];
  for (const p of pages) {
    if (!p.section) continue;
    let g = groups.find((x) => x.id === p.section);
    if (!g) { g = { id: p.section, items: [] }; groups.push(g); }
    g.items.push(p);
  }
  return groups.map((g) => `<div class="nav__section">
<p class="nav__title">${escapeHtml(t.sections[g.id])}</p>
<ul class="nav__list">${g.items.map((p) => {
    const active = p.id === current;
    return `<li class="nav__item"><a class="nav__link${active ? ' nav__link--active' : ''}" href="${p.id}.html" data-page-link="${p.id}"${active ? ' aria-current="page"' : ''}><span class="nav__num" aria-hidden="true">${p.num ?? p.icon ?? ''}</span><span class="nav__text">${escapeHtml(pageMeta[p.id]?.short || pageMeta[p.id]?.title || p.id)}</span><span class="nav__done" aria-hidden="true">✓</span></a></li>`;
  }).join('')}</ul>
</div>`).join('');
}

function tocHtml(toc, t, modifier) {
  if (!toc.length) return '';
  const items = toc.map((h) => `<li class="toc__item toc__item--h${h.depth}"><a class="toc__link" href="#${h.id}">${escapeHtml(h.text)}</a></li>`).join('');
  if (modifier === 'mobile') {
    return `<details class="toc toc--mobile"><summary class="toc__title toc__title--toggle">${escapeHtml(t.toc)}</summary><ol class="toc__list">${items}</ol></details>`;
  }
  return `<nav class="toc toc--desktop" aria-label="${escapeHtml(t.toc)}"><p class="toc__title">${escapeHtml(t.toc)}</p><ol class="toc__list">${items}</ol></nav>`;
}

export function renderPage(o) {
  const {
    lang, langs, t, page, meta, pages, pageMeta, body, toc = [], cheatsheet = '', hasMath, scripts = [],
    prev, next, readingMinutes, buildDate, siteRepo,
  } = o;
  const isHome = !!page.home;
  const title = isHome ? t.siteTitle : `${meta.title} — SKJ`;
  const alternates = langs.map((l) => `<link rel="alternate" hreflang="${l.code}" href="../${l.code}/${page.id}.html">`).join('\n');
  const langLinks = langs.map((l) => `<a class="lang-switch__link${l.code === lang ? ' lang-switch__link--active' : ''}" href="../${l.code}/${page.id}.html" hreflang="${l.code}" lang="${l.code}" data-lang="${l.code}"${l.code === lang ? ' aria-current="true"' : ''} title="${escapeHtml(l.label)}">${l.short}</a>`).join('');

  const kindBadge = meta.kind ? `<span class="badge badge--${meta.kind === 'lab' ? 'lab' : meta.kind === 'own' ? 'own' : 'lecture'}">${meta.kind === 'lab' ? '🔁' : meta.kind === 'own' ? '✍️' : '📘'} ${escapeHtml(t.kinds[meta.kind] ?? meta.kind)}</span>` : '';
  const eyebrow = page.section ? `${escapeHtml(t.sections[page.section])}${page.num ? ` · ${page.num}` : ''}` : '';

  const articleHeader = isHome ? '' : `<header class="article__header">
<p class="article__eyebrow">${eyebrow}</p>
<h1 class="article__title">${page.icon ? `<span class="article__icon" aria-hidden="true">${page.icon}</span>` : ''}${escapeHtml(meta.title)}</h1>
${meta.description ? `<p class="article__lead">${escapeHtml(meta.description)}</p>` : ''}
<div class="article__meta">${kindBadge}${meta.source ? `<span class="article__source">${escapeHtml(t.source)}: ${escapeHtml(meta.source)}</span>` : ''}${readingMinutes ? `<span class="article__time">⏱ ${readingMinutes} ${escapeHtml(t.readingTime)}</span>` : ''}</div>
<div class="article__actions">${cheatsheet ? `<button type="button" class="button button--primary" data-cheatsheet-open aria-haspopup="dialog" aria-controls="cheatsheet" title="${escapeHtml(t.cheatsheetHint)}">${ICONS.notes}<span class="button__text">${escapeHtml(t.cheatsheet)}</span></button>` : ''}${page.generated ? '' : `<button type="button" class="button button--ghost" data-done-toggle="${page.id}" aria-pressed="false" data-label-off="${escapeHtml(t.markDone)}" data-label-on="${escapeHtml(t.done)}">${ICONS.check}<span class="button__text">${escapeHtml(t.markDone)}</span></button>`}</div>
</header>`;

  const pager = (prev || next) ? `<nav class="pager" aria-label="${escapeHtml(t.prev)} / ${escapeHtml(t.next)}">
${prev ? `<a class="pager__link pager__link--prev" href="${prev.id}.html">${ICONS.arrowL}<span class="pager__text"><span class="pager__label">${escapeHtml(t.prev)}</span><span class="pager__title">${escapeHtml(prev.title)}</span></span></a>` : '<span></span>'}
${next ? `<a class="pager__link pager__link--next" href="${next.id}.html"><span class="pager__text"><span class="pager__label">${escapeHtml(t.next)}</span><span class="pager__title">${escapeHtml(next.title)}</span></span>${ICONS.arrowR}</a>` : ''}
</nav>` : '';

  const dialog = cheatsheet ? `<dialog class="cheatsheet" id="cheatsheet" aria-labelledby="cheatsheet-title">
<div class="cheatsheet__header"><h2 class="cheatsheet__title" id="cheatsheet-title">📝 ${escapeHtml(t.cheatsheetTitle)}: ${escapeHtml(meta.title)}</h2>
<div class="cheatsheet__tools"><button type="button" class="cheatsheet__button" data-cheatsheet-print title="${escapeHtml(t.print)}">${ICONS.print}<span class="visually-hidden">${escapeHtml(t.print)}</span></button><button type="button" class="cheatsheet__button" data-cheatsheet-close title="${escapeHtml(t.close)}">${ICONS.close}<span class="visually-hidden">${escapeHtml(t.close)}</span></button></div></div>
<div class="cheatsheet__body content">${cheatsheet}</div>
<div class="cheatsheet__footer"><a class="cheatsheet__all" href="cheatsheets.html">🗂️ ${escapeHtml(t.allCheatsheets)}</a></div>
</dialog>` : '';

  const scriptTags = ['theme-toggle', 'lang-switch', 'nav', 'code-block', 'progress', 'toc', ...(cheatsheet ? ['cheatsheet'] : []), ...scripts]
    .map((s) => `<script src="../assets/js/blocks/${s}.js" defer></script>`).join('\n')
    + (hasMath ? '\n<script src="../assets/vendor/katex/contrib/copy-tex.min.js" defer></script>' : '');

  return `<!DOCTYPE html>
<html lang="${lang}" data-theme="light">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>${escapeHtml(title)}</title>
<meta name="description" content="${escapeHtml(meta.description ?? t.siteTitle)}">
<meta name="theme-color" content="#f7f8fb" media="(prefers-color-scheme: light)">
<meta name="theme-color" content="#0f1420" media="(prefers-color-scheme: dark)">
<meta name="color-scheme" content="light dark">
<script>${THEME_BOOT}</script>
${alternates}
<link rel="icon" href="../assets/img/favicon.svg" type="image/svg+xml">
${hasMath ? '<link rel="stylesheet" href="../assets/vendor/katex/katex.min.css">\n' : ''}<link rel="stylesheet" href="../assets/css/main.css">
</head>
<body class="page${isHome ? ' page--home' : ''}" data-page-id="${page.id}">
<a class="skip-link" href="#main">${escapeHtml(t.skip)}</a>
<header class="header">
<div class="header__inner">
<button type="button" class="header__menu" aria-controls="sidebar" aria-expanded="false" aria-label="${escapeHtml(t.menu)}" data-nav-open>${ICONS.menu}</button>
<a class="header__logo logo" href="index.html"><span class="logo__mark" aria-hidden="true">SKJ</span><span class="logo__text">${escapeHtml(t.logoText)}</span></a>
<div class="header__actions">
<nav class="lang-switch" aria-label="${escapeHtml(t.lang)}">${langLinks}</nav>
<button type="button" class="theme-toggle" aria-label="${escapeHtml(t.theme)}" title="${escapeHtml(t.theme)}">${ICONS.sun}${ICONS.moon}</button>
</div>
</div>
</header>
<div class="layout${toc.length ? ' layout--with-toc' : ''}">
<aside class="layout__sidebar sidebar" id="sidebar" aria-label="${escapeHtml(t.menu)}">
<div class="sidebar__head"><span class="sidebar__title">${escapeHtml(t.menu)}</span><button type="button" class="sidebar__close" data-nav-close aria-label="${escapeHtml(t.closeMenu)}">✕</button></div>
<nav class="nav">
<a class="nav__home${isHome ? ' nav__home--active' : ''}" href="index.html">🏠 ${escapeHtml(t.logoText)}</a>
<div class="nav__progress progress" data-progress data-label="${escapeHtml(t.progress)}"><span class="progress__label">${escapeHtml(t.progress)}</span><span class="progress__bar"><span class="progress__fill"></span></span><span class="progress__value">0%</span></div>
${navHtml({ t, pages, pageMeta, current: page.id })}
</nav>
</aside>
<div class="layout__overlay" data-nav-close hidden></div>
<main class="layout__main" id="main" tabindex="-1">
<article class="article${isHome ? ' article--home' : ''}">
${articleHeader}
${tocHtml(toc, t, 'mobile')}
<div class="article__body content">
${body}
</div>
${pager}
</article>
</main>
${toc.length ? `<aside class="layout__toc">${tocHtml(toc, t, 'desktop')}</aside>` : ''}
</div>
${dialog}
<footer class="footer">
<div class="footer__inner">
<p class="footer__text">${escapeHtml(t.footer.disclaimer)}</p>
<p class="footer__text footer__text--small">${escapeHtml(t.footer.credits)}</p>
<p class="footer__text footer__text--small"><a class="footer__link" href="${siteRepo}" target="_blank" rel="noopener noreferrer">${escapeHtml(t.footer.source)}</a> · ${buildDate}</p>
</div>
</footer>
${scriptTags}
</body>
</html>
`;
}
