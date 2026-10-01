/* Block: lang-switch — remembers the chosen language and keeps the current section (#hash). */
(function () {
  'use strict';
  document.querySelectorAll('.lang-switch__link').forEach(function (link) {
    link.addEventListener('click', function () {
      try { localStorage.setItem('skj-lang', link.getAttribute('data-lang')); } catch (e) { /* ignore */ }
    });
  });
})();
