/* Block: cheatsheet — opens the topic summary in a native <dialog>, supports printing and #konspekt links. */
(function () {
  'use strict';
  var dialog = document.getElementById('cheatsheet');
  if (!dialog) return;
  var lastFocus = null;

  function open() {
    lastFocus = document.activeElement;
    if (typeof dialog.showModal === 'function') dialog.showModal();
    else dialog.setAttribute('open', '');
    document.body.style.overflow = 'hidden';
  }

  function close() {
    if (typeof dialog.close === 'function') dialog.close();
    else dialog.removeAttribute('open');
  }

  dialog.addEventListener('close', function () {
    document.body.style.overflow = '';
    if (location.hash === '#cheatsheet') history.replaceState(null, '', location.pathname + location.search);
    if (lastFocus) lastFocus.focus();
  });

  document.querySelectorAll('[data-cheatsheet-open]').forEach(function (btn) {
    btn.addEventListener('click', open);
  });
  dialog.querySelectorAll('[data-cheatsheet-close]').forEach(function (btn) {
    btn.addEventListener('click', close);
  });
  // Click on the backdrop closes the dialog.
  dialog.addEventListener('click', function (e) {
    if (e.target === dialog) close();
  });
  dialog.querySelectorAll('[data-cheatsheet-print]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      document.body.classList.add('page--print-sheet');
      window.print();
    });
  });
  window.addEventListener('afterprint', function () {
    document.body.classList.remove('page--print-sheet');
  });

  if (location.hash === '#cheatsheet') open();
})();
