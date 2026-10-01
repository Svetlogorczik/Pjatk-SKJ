/* Block: dates — recomputes "passed / in N days" with the visitor's current date. */
(function () {
  'use strict';
  var today = new Date();
  var t0 = Date.UTC(today.getFullYear(), today.getMonth(), today.getDate());
  document.querySelectorAll('.dates__item[data-date]').forEach(function (item) {
    var p = item.getAttribute('data-date').split('-');
    var d = Date.UTC(+p[0], +p[1] - 1, +p[2]);
    var diff = Math.round((d - t0) / 86400000);
    var status = item.querySelector('.dates__status');
    item.classList.remove('dates__item--past', 'dates__item--today', 'dates__item--upcoming');
    if (diff === 0) {
      item.classList.add('dates__item--today');
      status.textContent = status.getAttribute('data-today');
    } else if (diff < 0) {
      item.classList.add('dates__item--past');
      status.textContent = status.getAttribute('data-past') + ' · ' + status.getAttribute('data-days-ago').replace('{n}', String(-diff));
    } else {
      item.classList.add('dates__item--upcoming');
      status.textContent = status.getAttribute('data-in-days').replace('{n}', String(diff));
    }
  });
})();
