/* Block: sidebar/nav — off-canvas menu on small screens. */
(function () {
  'use strict';
  var sidebar = document.getElementById('sidebar');
  var overlay = document.querySelector('.layout__overlay');
  var openBtn = document.querySelector('[data-nav-open]');
  if (!sidebar || !openBtn) return;

  function setOpen(open) {
    sidebar.classList.toggle('sidebar--open', open);
    document.body.classList.toggle('page--nav-open', open);
    openBtn.setAttribute('aria-expanded', String(open));
    if (overlay) overlay.hidden = !open;
    if (open) {
      var active = sidebar.querySelector('.nav__link--active') || sidebar.querySelector('a');
      if (active) active.focus({ preventScroll: false });
    } else {
      openBtn.focus();
    }
  }

  openBtn.addEventListener('click', function () { setOpen(true); });
  document.querySelectorAll('[data-nav-close]').forEach(function (el) {
    el.addEventListener('click', function () { setOpen(false); });
  });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && sidebar.classList.contains('sidebar--open')) setOpen(false);
  });
  sidebar.querySelectorAll('a').forEach(function (a) {
    a.addEventListener('click', function () {
      if (sidebar.classList.contains('sidebar--open')) setOpen(false);
    });
  });

  // Keep the active link visible inside a long sidebar.
  var active = sidebar.querySelector('.nav__link--active');
  if (active && window.matchMedia('(min-width: 960px)').matches) {
    active.scrollIntoView({ block: 'center' });
    window.scrollTo(0, 0);
  }
})();
