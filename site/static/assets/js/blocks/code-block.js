/* Block: code-block — copy to clipboard and expand/collapse long listings. */
(function () {
  'use strict';

  function fallbackCopy(text) {
    var ta = document.createElement('textarea');
    ta.value = text;
    ta.setAttribute('readonly', '');
    ta.style.position = 'fixed';
    ta.style.opacity = '0';
    document.body.appendChild(ta);
    ta.select();
    var ok = false;
    try { ok = document.execCommand('copy'); } catch (e) { ok = false; }
    document.body.removeChild(ta);
    return ok;
  }

  function copy(text) {
    if (navigator.clipboard && window.isSecureContext) {
      return navigator.clipboard.writeText(text).then(function () { return true; }, function () { return fallbackCopy(text); });
    }
    return Promise.resolve(fallbackCopy(text));
  }

  document.addEventListener('click', function (e) {
    var btn = e.target.closest('.code-block__btn--copy');
    if (btn) {
      var block = btn.closest('.code-block');
      var code = block.querySelector('.code-block__code');
      var label = btn.querySelector('.code-block__btn-text');
      var original = label ? label.textContent : '';
      copy(code.innerText.replace(/\n$/, '')).then(function (ok) {
        btn.classList.toggle('code-block__btn--done', ok);
        if (label) label.textContent = ok ? btn.getAttribute('data-copied') : btn.getAttribute('data-fail');
        btn.setAttribute('aria-label', label ? label.textContent : '');
        setTimeout(function () {
          btn.classList.remove('code-block__btn--done');
          if (label) label.textContent = original;
          btn.removeAttribute('aria-label');
        }, 1800);
      });
      return;
    }

    var expand = e.target.closest('.code-block__expand');
    if (expand) {
      var fig = expand.closest('.code-block');
      var open = fig.classList.toggle('code-block--expanded');
      expand.setAttribute('aria-expanded', String(open));
      expand.textContent = expand.getAttribute(open ? 'data-collapse' : 'data-expand');
      if (!open) fig.scrollIntoView({ block: 'nearest' });
    }
  });
})();
