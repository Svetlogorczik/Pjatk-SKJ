/* Block: glossary — live filter of the PL/EN/RU terms table. */
(function () {
  'use strict';
  document.querySelectorAll('.glossary').forEach(function (g) {
    var input = g.querySelector('.glossary__input');
    var rows = g.querySelectorAll('.glossary__row');
    if (!input) return;
    input.addEventListener('input', function () {
      var q = input.value.trim().toLowerCase();
      rows.forEach(function (r) {
        r.classList.toggle('glossary__row--hidden', q !== '' && r.textContent.toLowerCase().indexOf(q) < 0);
      });
    });
  });
})();
