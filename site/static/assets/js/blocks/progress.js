/* Block: progress — per-browser "done" marks for topics (stored only in localStorage). */
(function () {
  'use strict';
  var KEY = 'skj-done';

  function load() {
    try { return JSON.parse(localStorage.getItem(KEY) || '{}') || {}; } catch (e) { return {}; }
  }
  function save(data) {
    try { localStorage.setItem(KEY, JSON.stringify(data)); } catch (e) { /* ignore */ }
  }

  function render() {
    var done = load();
    document.querySelectorAll('[data-page-link]').forEach(function (a) {
      var id = a.getAttribute('data-page-link');
      a.classList.toggle(a.classList.contains('card__link') ? 'card__link--done' : 'nav__link--done', !!done[id]);
    });
    var trackable = document.querySelectorAll('.nav__link[data-page-link]');
    var total = 0;
    var count = 0;
    trackable.forEach(function (a) {
      var id = a.getAttribute('data-page-link');
      if (id === 'glossary' || id === 'cheatsheets') return;
      total++;
      if (done[id]) count++;
    });
    var pct = total ? Math.round((count / total) * 100) : 0;
    document.querySelectorAll('[data-progress]').forEach(function (p) {
      var fill = p.querySelector('.progress__fill');
      var value = p.querySelector('.progress__value');
      if (fill) fill.style.width = pct + '%';
      if (value) value.textContent = count + '/' + total;
    });
    document.querySelectorAll('[data-done-toggle]').forEach(function (btn) {
      var on = !!done[btn.getAttribute('data-done-toggle')];
      btn.setAttribute('aria-pressed', String(on));
      var text = btn.querySelector('.button__text');
      if (text) text.textContent = btn.getAttribute(on ? 'data-label-on' : 'data-label-off');
    });
  }

  document.querySelectorAll('[data-done-toggle]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var data = load();
      var id = btn.getAttribute('data-done-toggle');
      if (data[id]) delete data[id]; else data[id] = Date.now();
      save(data);
      render();
    });
  });

  render();
})();
