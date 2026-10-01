// Build-time widgets inserted into Markdown with @directives or ::: containers.
import { escapeHtml, format } from './util.mjs';

const CALCS = {
  delay: [
    { name: 'L', value: 1500 },
    { name: 'R', value: 100 },
    { name: 'd', value: 1000 },
    { name: 's', value: 200000000 },
    { name: 'N', value: 3, step: 1 },
    { name: 'proc', value: 0, zero: true },
    { name: 'queue', value: 0, zero: true },
  ],
  rtt: [
    { name: 'samples', value: '28, 32, 25, 40, 30', type: 'text', wide: true },
    { name: 'alpha', value: 0.125 },
    { name: 'beta', value: 0.25 },
  ],
  binom: [
    { name: 'C', value: 1000 },
    { name: 'r', value: 100 },
    { name: 'p', value: 0.1 },
    { name: 'N', value: 35, step: 1 },
  ],
  tput: [
    { name: 'Rs', value: 100 },
    { name: 'Rc', value: 50 },
    { name: 'R', value: 400 },
    { name: 'n', value: 10, step: 1 },
    { name: 'F', value: 100 },
  ],
};

export function renderCalc(type, t) {
  const fields = CALCS[type];
  if (!fields) throw new Error(`Unknown calculator ${type}`);
  const L = t.calc[type];
  const strings = { ...t.calc.s, invalid: t.calc.invalid, resultTitle: t.calc.result, ...L };
  const inputs = fields.map((f) => `<label class="calc__field${f.wide ? ' calc__field--wide' : ''}"><span class="calc__label">${escapeHtml(L[f.name])}</span><input class="calc__input" name="${f.name}" type="${f.type ?? 'number'}" ${f.type === 'text' ? '' : `step="${f.step ?? 'any'}" min="0" inputmode="decimal"`} value="${f.value}"${f.zero ? ' data-zero-ok' : ''}></label>`).join('');
  return `<form class="calc" data-calc="${type}" data-strings="${escapeHtml(JSON.stringify(strings))}" novalidate>
<p class="calc__title"><span aria-hidden="true">🧮</span> ${escapeHtml(L.title)} <span class="badge badge--own" title="${escapeHtml(t.ownBadgeTitle)}">✍️ ${escapeHtml(t.ownBadge)}</span></p>
<div class="calc__fields">${inputs}</div>
<button class="button button--primary calc__run" type="submit">${escapeHtml(t.calc.run)}</button>
<div class="calc__output" aria-live="polite"></div>
</form>`;
}

export function makeWidgets({ t, lang, locale, pages, pageMeta, dates, glossary, buildDate }) {
  const fmtDate = (d, time) => {
    const [y, m, day] = d.split('-');
    return `${day}.${m}.${y}${time ? `, ${time}` : ''}`;
  };

  function dateStatus(iso) {
    const target = new Date(`${iso}T00:00:00Z`).getTime();
    const today = new Date(`${buildDate}T00:00:00Z`).getTime();
    const diff = Math.round((target - today) / 86400000);
    if (diff === 0) return { cls: 'today', text: t.dates.today };
    if (diff < 0) return { cls: 'past', text: `${t.dates.past} · ${format(t.dates.daysAgo, { n: -diff })}` };
    return { cls: 'upcoming', text: format(t.dates.inDays, { n: diff }) };
  }

  return {
    legend() {
      return `<div class="legend"><p class="legend__title">${escapeHtml(t.legendTitle)}</p><ul class="legend__list">
<li class="legend__item"><span class="badge badge--lecture">📘</span> ${escapeHtml(t.legend.lecture)}</li>
<li class="legend__item"><span class="badge badge--own">✍️ ${escapeHtml(t.ownBadge)}</span> ${escapeHtml(t.legend.own)}</li>
<li class="legend__item"><span class="badge badge--lab">🔁</span> ${escapeHtml(t.legend.rewritten)}</li>
</ul></div>`;
    },

    dates() {
      const items = dates.map((d) => {
        const st = dateStatus(d.date);
        const iso = d.time ? `${d.date}T${d.time}` : d.date;
        return `<li class="dates__item dates__item--${st.cls}" data-date="${d.date}">
<time class="dates__date" datetime="${iso}">${fmtDate(d.date, d.time)}</time>
<span class="dates__label">${escapeHtml(t.dates.items[d.id])}</span>
<span class="dates__status" data-past="${escapeHtml(t.dates.past)}" data-today="${escapeHtml(t.dates.today)}" data-in-days="${escapeHtml(t.dates.inDays)}" data-days-ago="${escapeHtml(t.dates.daysAgo)}">${escapeHtml(st.text)}</span>
<span class="dates__source">${escapeHtml(t.dates.sourceLabel)}: <code class="content__code">${escapeHtml(d.source)}</code></span>
</li>`;
      }).join('');
      return `<section class="dates" aria-label="${escapeHtml(t.dates.title)}"><p class="dates__title">📅 ${escapeHtml(t.dates.title)} <span class="dates__edition">${escapeHtml(t.dates.edition)} 2025/2026</span></p><ul class="dates__list">${items}</ul><p class="dates__note">${escapeHtml(t.dates.archival)}</p></section>`;
    },

    cards() {
      const groups = {};
      for (const p of pages) {
        if (!p.section || p.generated) continue;
        (groups[p.section] ??= []).push(p);
      }
      const sectionIcons = { start: '🧭', lectures: '📘', labs: '🧪', exam: '📝', project: '🛠️' };
      const html = Object.entries(groups).map(([sec, list]) => `<section class="cards__item card">
<h2 class="card__title"><span class="card__icon" aria-hidden="true">${sectionIcons[sec]}</span>${escapeHtml(t.sections[sec])}</h2>
<ul class="card__list">${list.map((p) => `<li class="card__list-item"><a class="card__link" href="${p.id}.html" data-page-link="${p.id}">${p.num ? `<span class="card__num">${p.num}</span>` : `<span class="card__num" aria-hidden="true">${p.icon ?? '•'}</span>`}<span class="card__text">${escapeHtml(pageMeta[p.id]?.short || pageMeta[p.id]?.title || p.id)}</span></a></li>`).join('')}</ul>
</section>`).join('');
      return `<div class="cards">${html}</div>`;
    },

    glossary() {
      const rows = glossary.map((g) => `<tr class="glossary__row"><td class="glossary__cell glossary__cell--term" lang="pl">${escapeHtml(g.pl)}</td><td class="glossary__cell glossary__cell--term" lang="en">${escapeHtml(g.en)}</td><td class="glossary__cell glossary__cell--term" lang="ru">${escapeHtml(g.ru)}</td><td class="glossary__cell">${escapeHtml(g.desc[lang])}</td></tr>`).join('');
      return `<div class="glossary"><label class="glossary__search"><span class="visually-hidden">${escapeHtml(t.glossary.filter)}</span><input class="glossary__input" type="search" placeholder="${escapeHtml(t.glossary.filter)}" autocomplete="off"></label>
<div class="content__table-wrap" tabindex="0"><table class="glossary__table"><thead><tr><th class="glossary__head">${t.glossary.pl}</th><th class="glossary__head">${t.glossary.en}</th><th class="glossary__head">${t.glossary.ru}</th><th class="glossary__head">${escapeHtml(t.glossary.desc)}</th></tr></thead><tbody>${rows}</tbody></table></div></div>`;
    },
  };
}
