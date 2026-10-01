/* Block: calc — small step-by-step calculators (delay, RTT, packet vs circuit switching, throughput). */
(function () {
  'use strict';
  var lang = document.documentElement.lang || 'en';

  function fmt(x, digits) {
    if (!isFinite(x)) return String(x);
    return x.toLocaleString(lang, { maximumFractionDigits: digits == null ? 4 : digits });
  }

  function esc(s) {
    return String(s).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; });
  }

  function num(form, name) {
    var el = form.elements[name];
    var v = parseFloat(String(el.value).replace(',', '.'));
    var zeroOk = el.hasAttribute('data-zero-ok');
    if (!isFinite(v) || v < 0 || (!zeroOk && v === 0)) throw new Error('invalid');
    return v;
  }

  function steps(S, lines, result) {
    return '<p class="calc__heading">' + esc(S.resultTitle || '') + '</p><ol class="calc__steps">' +
      lines.map(function (l) { return '<li><span class="calc__step-name">' + esc(l[0]) + ':</span> ' + esc(l[1]) + '</li>'; }).join('') +
      '</ol>' + (result ? '<p class="calc__result">' + esc(result) + '</p>' : '');
  }

  var CALCS = {
    delay: function (f, S) {
      var L = num(f, 'L'), R = num(f, 'R'), d = num(f, 'd'), s = num(f, 's'), N = Math.round(num(f, 'N'));
      var proc = num(f, 'proc'), queue = num(f, 'queue');
      var bits = L * 8, rbps = R * 1e6, meters = d * 1000;
      var trans = bits / rbps * 1000;          // ms
      var prop = meters / s * 1000;            // ms
      var nodal = proc + queue + trans + prop; // ms
      var total = N * nodal;
      return steps(S, [
        [S.bits, 'L = ' + fmt(L) + ' B × 8 = ' + fmt(bits) + ' bit'],
        [S.convert, 'R = ' + fmt(R) + ' Mb/s = ' + fmt(rbps) + ' bit/s;  d = ' + fmt(d) + ' km = ' + fmt(meters) + ' m'],
        [S.trans, 'd_trans = L / R = ' + fmt(bits) + ' / ' + fmt(rbps) + ' s = ' + fmt(trans) + ' ms'],
        [S.prop, 'd_prop = d / s = ' + fmt(meters) + ' / ' + fmt(s) + ' s = ' + fmt(prop) + ' ms'],
        [S.nodal, 'd_proc + d_queue + d_trans + d_prop = ' + fmt(proc) + ' + ' + fmt(queue) + ' + ' + fmt(trans) + ' + ' + fmt(prop) + ' = ' + fmt(nodal) + ' ms'],
        [S.total, 'N × d = ' + N + ' × ' + fmt(nodal) + ' = ' + fmt(total) + ' ms']
      ], '≈ ' + fmt(total, 3) + ' ms');
    },

    rtt: function (f, S) {
      var raw = String(f.elements.samples.value).split(/[;,\s]+/).filter(Boolean).map(function (x) { return parseFloat(x.replace(',', '.')); });
      if (!raw.length || raw.some(function (x) { return !isFinite(x) || x <= 0; })) throw new Error('invalid');
      var a = num(f, 'alpha'), b = num(f, 'beta');
      if (a >= 1 || b >= 1) throw new Error('invalid');
      var est = raw[0], dev = raw[0] / 2;
      var rows = [[1, raw[0], est, dev, est + 4 * dev, 'EstimatedRTT = SampleRTT₁ = ' + fmt(est)]];
      for (var i = 1; i < raw.length; i++) {
        var sample = raw[i];
        var newDev = (1 - b) * dev + b * Math.abs(sample - est);
        var newEst = (1 - a) * est + a * sample;
        rows.push([i + 1, sample, newEst, newDev, newEst + 4 * newDev,
          'EstimatedRTT = ' + fmt(1 - a) + '×' + fmt(est) + ' + ' + fmt(a) + '×' + fmt(sample) + ' = ' + fmt(newEst)]);
        est = newEst; dev = newDev;
      }
      var table = '<table class="calc__table"><thead><tr><th>' + esc(S.i) + '</th><th>' + esc(S.sample) + '</th><th>' + esc(S.est) +
        '</th><th>' + esc(S.dev) + '</th><th>' + esc(S.timeout) + '</th></tr></thead><tbody>' +
        rows.map(function (r) {
          return '<tr><td>' + r[0] + '</td><td>' + fmt(r[1], 3) + '</td><td>' + fmt(r[2], 3) + '</td><td>' + fmt(r[3], 3) + '</td><td>' + fmt(r[4], 3) + '</td></tr>';
        }).join('') + '</tbody></table>';
      var lines = rows.map(function (r) { return ['#' + r[0], r[5]]; });
      lines.unshift([S.init, 'α = ' + fmt(a) + ', β = ' + fmt(b) + '; TimeoutInterval = EstimatedRTT + 4·DevRTT']);
      return steps(S, lines, '') + table;
    },

    binom: function (f, S) {
      var C = num(f, 'C'), r = num(f, 'r'), p = num(f, 'p'), N = Math.round(num(f, 'N'));
      if (p >= 1) throw new Error('invalid');
      var k = Math.floor(C / r);
      // P(X <= k) for X ~ Binomial(N, p), terms computed iteratively.
      var term = Math.pow(1 - p, N), cdf = 0;
      for (var i = 0; i <= Math.min(k, N); i++) {
        cdf += term;
        term = term * (N - i) / (i + 1) * p / (1 - p);
      }
      var tail = Math.max(0, 1 - cdf);
      var text = tail < 0.01 ? S.rare : S.often;
      return steps(S, [
        [S.circuit, '⌊C / r⌋ = ⌊' + fmt(C) + ' / ' + fmt(r) + '⌋ = ' + k],
        [S.packet.replace('{k}', k), '1 − Σ(i=0..' + k + ') C(' + N + ', i) · ' + fmt(p) + '^i · ' + fmt(1 - p) + '^(' + N + '−i) = ' + tail.toExponential(3)],
        [S.meaning, text]
      ], 'P ≈ ' + fmt(tail, 6));
    },

    tput: function (f, S) {
      var Rs = num(f, 'Rs'), Rc = num(f, 'Rc'), R = num(f, 'R'), n = Math.round(num(f, 'n')), F = num(f, 'F');
      var share = R / n;
      var min = Math.min(Rs, Rc, share);
      var bits = F * 8e6;
      var time = bits / (min * 1e6);
      return steps(S, [
        [S.perConn, 'R / n = ' + fmt(R) + ' / ' + n + ' = ' + fmt(share) + ' Mb/s'],
        [S.bottleneck, 'min(Rs, Rc, R/n) = min(' + fmt(Rs) + ', ' + fmt(Rc) + ', ' + fmt(share) + ') = ' + fmt(min) + ' Mb/s'],
        [S.bits, 'F = ' + fmt(F) + ' MB × 8 = ' + fmt(F * 8) + ' Mb'],
        [S.time, 'F / throughput = ' + fmt(F * 8) + ' / ' + fmt(min) + ' = ' + fmt(time, 2) + ' s']
      ], '≈ ' + fmt(time, 2) + ' s');
    }
  };

  document.querySelectorAll('.calc[data-calc]').forEach(function (form) {
    var type = form.getAttribute('data-calc');
    var S = {};
    try { S = JSON.parse(form.getAttribute('data-strings')); } catch (e) { /* keep defaults */ }
    var out = form.querySelector('.calc__output');

    function run(e) {
      if (e) e.preventDefault();
      try {
        out.innerHTML = CALCS[type](form, S);
      } catch (err) {
        out.innerHTML = '<p class="calc__error">' + esc(S.invalid || 'Invalid input') + '</p>';
      }
    }

    form.addEventListener('submit', run);
    run();
  });
})();
