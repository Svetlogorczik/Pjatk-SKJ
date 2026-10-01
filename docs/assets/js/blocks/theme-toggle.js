/* Block: theme-toggle — switches light/dark theme and remembers the choice. */
(function () {
  'use strict';
  var KEY = 'skj-theme';
  var root = document.documentElement;
  var media = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null;

  function saved() {
    try { return localStorage.getItem(KEY); } catch (e) { return null; }
  }

  function apply(theme) {
    root.setAttribute('data-theme', theme);
  }

  document.querySelectorAll('.theme-toggle').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var next = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      apply(next);
      try { localStorage.setItem(KEY, next); } catch (e) { /* private mode */ }
    });
  });

  // Follow the system setting while the user has not chosen a theme explicitly.
  if (media && media.addEventListener) {
    media.addEventListener('change', function (e) {
      var s = saved();
      if (s !== 'light' && s !== 'dark') apply(e.matches ? 'dark' : 'light');
    });
  }
})();
