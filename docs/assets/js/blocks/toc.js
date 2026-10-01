/* Block: toc — highlights the section currently in view. */
(function () {
  'use strict';
  var links = Array.prototype.slice.call(document.querySelectorAll('.toc--desktop .toc__link'));
  if (!links.length || !('IntersectionObserver' in window)) return;
  var map = {};
  links.forEach(function (l) { map[decodeURIComponent(l.hash.slice(1))] = l; });
  var headings = Object.keys(map).map(function (id) { return document.getElementById(id); }).filter(Boolean);
  var current = null;

  var observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting) {
        if (current) current.classList.remove('toc__link--active');
        current = map[entry.target.id];
        if (current) current.classList.add('toc__link--active');
      }
    });
  }, { rootMargin: '-70px 0px -70% 0px', threshold: 0 });

  headings.forEach(function (h) { observer.observe(h); });
})();
