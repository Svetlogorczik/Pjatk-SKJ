export function escapeHtml(s = '') {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/** Unicode-aware slug: keeps letters of any script (Polish, Cyrillic), digits and dashes. */
export function slugify(s) {
  return String(s)
    .normalize('NFKD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/ł/g, 'l').replace(/Ł/g, 'L')
    .toLowerCase()
    .replace(/[^\p{L}\p{N}]+/gu, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 64) || 'section';
}

export function format(str, vars) {
  return str.replace(/\{(\w+)\}/g, (m, k) => (k in vars ? vars[k] : m));
}

export function wordCount(html) {
  return html
    .replace(/<pre[\s\S]*?<\/pre>/g, ' ')
    .replace(/<span class="katex[\s\S]*?<\/span>/g, ' ')
    .replace(/<[^>]+>/g, ' ')
    .split(/\s+/)
    .filter(Boolean).length;
}
