// Markdown -> HTML pipeline with BEM classes, callouts, KaTeX, code blocks and directives.
import fs from 'node:fs';
import path from 'node:path';
import { Marked } from 'marked';
import katex from 'katex';
import hljs from 'highlight.js/lib/core';
import java from 'highlight.js/lib/languages/java';
import bash from 'highlight.js/lib/languages/bash';
import dos from 'highlight.js/lib/languages/dos';
import http from 'highlight.js/lib/languages/http';
import ini from 'highlight.js/lib/languages/ini';
import plaintext from 'highlight.js/lib/languages/plaintext';
import json from 'highlight.js/lib/languages/json';
import xml from 'highlight.js/lib/languages/xml';

import { escapeHtml, slugify } from './util.mjs';
import { renderCalc } from './widgets.mjs';

hljs.registerLanguage('java', java);
hljs.registerLanguage('bash', bash);
hljs.registerLanguage('dos', dos);
hljs.registerLanguage('http', http);
hljs.registerLanguage('ini', ini);
hljs.registerLanguage('plaintext', plaintext);
hljs.registerLanguage('json', json);
hljs.registerLanguage('xml', xml);

const LANG_ALIASES = {
  sh: 'bash', shell: 'bash', console: 'bash', terminal: 'bash',
  bat: 'dos', cmd: 'dos', batch: 'dos',
  properties: 'ini', prop: 'ini',
  text: 'plaintext', txt: 'plaintext', '': 'plaintext',
  html: 'xml',
};

const LANG_LABELS = {
  java: 'Java', bash: 'Terminal', dos: 'Windows .bat', http: 'HTTP', ini: '.properties',
  plaintext: 'Text', json: 'JSON', xml: 'HTML/XML',
};

const EXT_LANG = { java: 'java', sh: 'bash', bat: 'dos', properties: 'ini', txt: 'plaintext', md: 'plaintext', http: 'http', json: 'json' };

const CALLOUT_ICONS = {
  note: '📌', tip: '💡', warning: '⚠️', own: '✍️', lecture: '📘', example: '🧩', task: '🎯',
  answer: '✅', hint: '🔦', analogy: '🎈', check: '❓', steps: '👣', formula: '🧮', danger: '🐞', origin: '🔁',
};
const COLLAPSIBLE = new Set(['answer', 'hint', 'check']);

// Classes added to bare tags produced by marked (BEM elements of the `content` block).
const TAG_CLASSES = {
  p: 'content__paragraph',
  ul: 'content__list content__list--bullet',
  ol: 'content__list content__list--ordered',
  li: 'content__list-item',
  blockquote: 'content__quote',
  hr: 'content__divider',
  code: 'content__code',
  table: 'content__table',
  thead: 'content__table-head',
  tbody: 'content__table-body',
  tr: 'content__table-row',
  th: 'content__table-heading',
  td: 'content__table-cell',
  kbd: 'content__kbd',
  img: 'content__image',
};

function addBemClasses(html, map = TAG_CLASSES) {
  return html.replace(/<(p|ul|ol|li|blockquote|hr|code|table|thead|tbody|tr|th|td|kbd|img)(?=[\s>/])([^>]*)>/g,
    (m, tag, attrs) => (/\bclass=/.test(attrs) ? m : `<${tag} class="${map[tag]}"${attrs}>`));
}

function renderTex(tex, displayMode) {
  return katex.renderToString(tex, {
    displayMode,
    throwOnError: false,
    output: 'htmlAndMathml',
    strict: 'ignore',
  });
}

export function highlight(code, lang) {
  const l = LANG_ALIASES[lang] ?? lang;
  if (l && hljs.getLanguage(l)) {
    return { html: hljs.highlight(code, { language: l, ignoreIllegals: true }).value, lang: l };
  }
  return { html: escapeHtml(code), lang: 'plaintext' };
}

/** Code block component (BEM block `code-block`). */
export function codeBlock(code, { lang = '', title = '', download = '', t }) {
  const text = code.replace(/\s+$/, '');
  const { html, lang: l } = highlight(text, lang);
  const lines = text.split('\n').length;
  const collapsible = lines > 32;
  const label = LANG_LABELS[l] ?? l;
  const icons = {
    copy: '<svg class="code-block__icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="9" y="9" width="11" height="11" rx="2"/><path d="M5 15V6a2 2 0 0 1 2-2h9"/></svg>',
    download: '<svg class="code-block__icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 4v11m0 0-4-4m4 4 4-4M5 20h14"/></svg>',
  };
  return `<figure class="code-block${collapsible ? ' code-block--collapsible' : ''}" data-lines="${lines}">
<figcaption class="code-block__bar"><span class="code-block__lang">${escapeHtml(label)}</span>${title ? `<span class="code-block__title">${escapeHtml(title)}</span>` : ''}<span class="code-block__actions">${download ? `<a class="code-block__btn code-block__btn--download" href="${download}" download>${icons.download}<span class="code-block__btn-text">${t.download}</span></a>` : ''}<button type="button" class="code-block__btn code-block__btn--copy" data-copied="${escapeHtml(t.copied)}" data-fail="${escapeHtml(t.copyFail)}">${icons.copy}<span class="code-block__btn-text">${t.copy}</span></button></span></figcaption>
<pre class="code-block__pre"><code class="code-block__code hljs language-${l}">${html}</code></pre>${collapsible ? `
<button type="button" class="code-block__expand" aria-expanded="false" data-expand="${escapeHtml(t.expand)}" data-collapse="${escapeHtml(t.collapse)}">${t.expand}</button>` : ''}
</figure>`;
}

function parseInfo(info = '') {
  const m = /^(\S*)\s*(.*)$/.exec(info.trim());
  const lang = (m?.[1] ?? '').toLowerCase();
  let title = '';
  const tm = /title="([^"]*)"/.exec(m?.[2] ?? '');
  if (tm) title = tm[1];
  else if (m?.[2]) title = m[2].trim();
  return { lang, title };
}

/** Splits markdown into plain markdown chunks, ::: containers and @directives. */
function parseSegments(text) {
  const root = { type: 'root', children: [] };
  const stack = [root];
  let buf = [];
  let fence = null;
  const flush = () => {
    if (buf.length) stack.at(-1).children.push({ type: 'md', text: buf.join('\n') });
    buf = [];
  };
  for (const line of text.split('\n')) {
    const trimmed = line.trim();
    if (fence) {
      buf.push(line);
      if (trimmed.startsWith(fence) && trimmed.replace(/[`~]/g, '') === '') fence = null;
      continue;
    }
    const f = /^(`{3,}|~{3,})/.exec(trimmed);
    if (f) { fence = f[1]; buf.push(line); continue; }
    const open = /^(:{3,})\s*([a-z][\w-]*)(?:\s+(.*))?$/.exec(trimmed);
    if (open) {
      flush();
      const node = { type: 'container', kind: open[2], title: open[3] ?? '', colons: open[1].length, children: [] };
      stack.at(-1).children.push(node);
      stack.push(node);
      continue;
    }
    const close = /^(:{3,})$/.exec(trimmed);
    if (close && stack.length > 1 && stack.at(-1).colons === close[1].length) {
      flush();
      stack.pop();
      continue;
    }
    const dir = /^@([a-z]+)(?:\((.*)\))?\s*$/.exec(trimmed);
    if (dir) {
      flush();
      stack.at(-1).children.push({ type: 'directive', name: dir[1], args: dir[2] ?? '' });
      continue;
    }
    buf.push(line);
  }
  flush();
  if (stack.length > 1) throw new Error(`Unclosed container ":::${stack.at(-1).kind}"`);
  return root;
}

function parseArgs(args) {
  // "path/to/file.java, Title text" or key=value pairs
  const parts = args.split(',').map((s) => s.trim()).filter(Boolean);
  const out = { _: [] };
  for (const p of parts) {
    const kv = /^(\w+)=(.*)$/.exec(p);
    if (kv) out[kv[1]] = kv[2].replace(/^"|"$/g, '');
    else out._.push(p.replace(/^"|"$/g, ''));
  }
  return out;
}

export function createRenderer(env) {
  // env: { root, lang, t, snippetsDir, diagramsDir, filesUrl, widgets }
  const { t } = env;

  function makeMarked(ctx) {
    const marked = new Marked({ gfm: true, breaks: false });
    marked.use({
      extensions: [
        {
          name: 'mathBlock',
          level: 'block',
          start(src) { const i = src.indexOf('$$'); return i < 0 ? undefined : i; },
          tokenizer(src) {
            const m = /^\$\$([\s\S]+?)\$\$[ \t]*(?:\n|$)/.exec(src);
            if (m) return { type: 'mathBlock', raw: m[0], text: m[1].trim() };
            return undefined;
          },
          renderer(tok) {
            ctx.hasMath = true;
            return `<div class="formula" role="math">${renderTex(tok.text, true)}</div>\n`;
          },
        },
        {
          name: 'mathInline',
          level: 'inline',
          start(src) { const i = src.indexOf('$'); return i < 0 ? undefined : i; },
          tokenizer(src) {
            const m = /^\$(?![\s$])((?:\\.|[^$\\\n])+?)(?<!\s)\$(?!\d)/.exec(src);
            if (m) return { type: 'mathInline', raw: m[0], text: m[1] };
            return undefined;
          },
          renderer(tok) {
            ctx.hasMath = true;
            return `<span class="formula formula--inline">${renderTex(tok.text, false)}</span>`;
          },
        },
        {
          name: 'ownBadge',
          level: 'inline',
          start(src) { const i = src.indexOf('[[own]]'); return i < 0 ? undefined : i; },
          tokenizer(src) {
            if (src.startsWith('[[own]]')) return { type: 'ownBadge', raw: '[[own]]' };
            return undefined;
          },
          renderer() {
            return `<span class="badge badge--own" title="${escapeHtml(t.ownBadgeTitle)}">✍️ ${escapeHtml(t.ownBadge)}</span>`;
          },
        },
      ],
      renderer: {
        code({ text, lang }) {
          const info = parseInfo(lang);
          return codeBlock(text, { lang: info.lang, title: info.title, t });
        },
        heading({ tokens, depth }) {
          const inner = this.parser.parseInline(tokens);
          const plain = inner.replace(/<span class="badge badge--own"[^>]*>[\s\S]*?<\/span>/g, ' ✍️')
            .replace(/<span class="katex-mathml">[\s\S]*?<\/span>/g, '')
            .replace(/<[^>]+>/g, '').replace(/&amp;/g, '&').replace(/&quot;/g, '"').replace(/&#39;/g, "'").trim();
          let id = (ctx.idPrefix ?? '') + slugify(plain.replace('✍️', ''));
          let n = 2;
          const base = id;
          while (ctx.ids.has(id)) id = `${base}-${n++}`;
          ctx.ids.add(id);
          if (!ctx.inCheatsheet && (depth === 2 || depth === 3)) ctx.toc.push({ id, depth, text: plain });
          return `<h${depth} class="content__heading content__heading--h${depth}" id="${id}">${inner}<a class="content__anchor" href="#${id}" aria-hidden="true" tabindex="-1">#</a></h${depth}>\n`;
        },
        link({ href, title, tokens }) {
          const text = this.parser.parseInline(tokens);
          const external = /^https?:\/\//.test(href);
          return `<a class="content__link${external ? ' content__link--external' : ''}" href="${escapeHtml(href)}"${title ? ` title="${escapeHtml(title)}"` : ''}${external ? ' target="_blank" rel="noopener noreferrer"' : ''}>${text}</a>`;
        },
        table(token) {
          let head = '';
          for (const cell of token.header) {
            const align = cell.align ? ` style="text-align:${cell.align}"` : '';
            head += `<th${align}>${this.parser.parseInline(cell.tokens)}</th>`;
          }
          let body = '';
          for (const row of token.rows) {
            body += '<tr>';
            for (const cell of row) {
              const align = cell.align ? ` style="text-align:${cell.align}"` : '';
              body += `<td${align}>${this.parser.parseInline(cell.tokens)}</td>`;
            }
            body += '</tr>';
          }
          return `<div class="content__table-wrap" tabindex="0"><table><thead><tr>${head}</tr></thead><tbody>${body}</tbody></table></div>\n`;
        },
      },
    });
    return marked;
  }

  function renderNode(node, ctx) {
    if (node.type === 'md') {
      return addBemClasses(ctx.marked.parse(node.text));
    }
    if (node.type === 'directive') return renderDirective(node, ctx);
    if (node.type === 'container') return renderContainer(node, ctx);
    return node.children.map((c) => renderNode(c, ctx)).join('\n');
  }

  function renderChildren(node, ctx) {
    return node.children.map((c) => renderNode(c, ctx)).join('\n');
  }

  function renderContainer(node, ctx) {
    const { kind } = node;
    if (kind === 'cheatsheet') {
      const sub = { ...ctx, inCheatsheet: true, idPrefix: `cs-${ctx.pageId ?? 'x'}-` };
      sub.marked = makeMarked(sub);
      ctx.cheatsheet = renderChildren(node, sub);
      if (sub.hasMath) ctx.hasMath = true;
      return '';
    }
    if (kind === 'calc') {
      ctx.widgets.add('calc');
      return renderCalc(node.title.trim(), t, env.lang);
    }
    if (kind === 'steps') {
      return `<div class="steps">${renderChildren(node, ctx)}</div>`;
    }
    if (kind === 'timeline') {
      const inner = renderChildren(node, ctx)
        .replace(/class="content__list content__list--bullet"/g, 'class="timeline__list"')
        .replace(/class="content__list-item"/g, 'class="timeline__item"');
      return `<div class="timeline">${inner}</div>`;
    }
    const icon = CALLOUT_ICONS[kind];
    if (!icon) throw new Error(`Unknown container kind ":::${kind}"`);
    const label = t.callouts[kind] ?? kind;
    const title = node.title ? `<span class="callout__title">${ctx.marked.parseInline(node.title)}</span>` : '';
    const body = renderChildren(node, ctx);
    if (COLLAPSIBLE.has(kind)) {
      return `<details class="callout callout--${kind}"><summary class="callout__header callout__header--toggle"><span class="callout__icon" aria-hidden="true">${icon}</span><span class="callout__label">${escapeHtml(label)}</span>${title}</summary><div class="callout__body">${body}</div></details>`;
    }
    return `<aside class="callout callout--${kind}"><div class="callout__header"><span class="callout__icon" aria-hidden="true">${icon}</span><span class="callout__label">${escapeHtml(label)}</span>${title}</div><div class="callout__body">${body}</div></aside>`;
  }

  function renderDirective(node, ctx) {
    const args = parseArgs(node.args);
    switch (node.name) {
      case 'code': {
        const rel = args._[0];
        const file = path.join(env.snippetsDir, rel);
        if (!fs.existsSync(file)) throw new Error(`@code: missing snippet ${rel}`);
        const code = fs.readFileSync(file, 'utf8');
        const ext = path.extname(rel).slice(1);
        ctx.files.add(rel);
        return codeBlock(code, {
          lang: args.lang ?? EXT_LANG[ext] ?? ext,
          title: args.title ?? args._[1] ?? path.basename(rel),
          download: args.download === 'no' ? '' : `${env.filesUrl}/${rel}`,
          t,
        });
      }
      case 'svg': {
        const name = args._[0];
        const file = path.join(env.diagramsDir, `${name}.svg`);
        if (!fs.existsSync(file)) throw new Error(`@svg: missing diagram ${name}`);
        const svg = fs.readFileSync(file, 'utf8').replace(/\{\{(\w+)\}\}/g, (m, k) => escapeHtml(t.dg[k] ?? k));
        const caption = args.caption ?? args._[1];
        return `<figure class="diagram">${svg}${caption ? `<figcaption class="diagram__caption">${ctx.marked.parseInline(caption)}</figcaption>` : ''}</figure>`;
      }
      default: {
        const widget = env.widgets?.[node.name];
        if (!widget) throw new Error(`Unknown directive @${node.name}`);
        ctx.widgets.add(node.name);
        return widget(args, ctx);
      }
    }
  }

  return function render(markdown, { pageId } = {}) {
    const ctx = {
      toc: [], ids: new Set(), hasMath: false, cheatsheet: '', files: new Set(), widgets: new Set(),
      inCheatsheet: false, idPrefix: '', pageId,
    };
    ctx.marked = makeMarked(ctx);
    const tree = parseSegments(markdown);
    const html = renderNode(tree, ctx);
    return { html, toc: ctx.toc, cheatsheet: ctx.cheatsheet, hasMath: ctx.hasMath, files: ctx.files, widgets: ctx.widgets };
  };
}

export function parseFrontMatter(src) {
  const m = /^---\n([\s\S]*?)\n---\n?/.exec(src);
  if (!m) return { data: {}, body: src };
  const data = {};
  for (const line of m[1].split('\n')) {
    const kv = /^(\w+):\s*(.*)$/.exec(line);
    if (kv) data[kv[1]] = kv[2].replace(/^"(.*)"$/, '$1');
  }
  return { data, body: src.slice(m[0].length) };
}
