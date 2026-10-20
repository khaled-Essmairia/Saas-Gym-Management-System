/* Reusable table search + pagination.
   Usage: wrap a table in an element with the data-pager attribute and add
   elements with data-role="size" | "search" | "info" | "nav". */
(function () {
  'use strict';

  function init(root) {
    var table = root.querySelector('table');
    var sizeSelect = root.querySelector('[data-role="size"]');
    var searchInput = root.querySelector('[data-role="search"]');
    var info = root.querySelector('[data-role="info"]');
    var nav = root.querySelector('[data-role="nav"]');
    var tbody = table.tBodies[0];

    var rows = Array.prototype.filter.call(tbody.rows, function (r) {
      return !r.hasAttribute('data-pager-ignore');
    });
    var colCount = table.tHead.rows[0].cells.length;

    // Row shown when the search matches nothing
    var noMatch = document.createElement('tr');
    noMatch.style.display = 'none';
    noMatch.innerHTML = '<td colspan="' + colCount + '" class="empty-state">No matching records found.</td>';
    tbody.appendChild(noMatch);

    var page = 1;
    var size = parseInt(sizeSelect.value, 10);
    var query = '';

    function rowText(row) {
      if (!row._text) {
        row._text = Array.prototype.filter.call(row.cells, function (c) {
          return !c.hasAttribute('data-nosearch');
        }).map(function (c) {
          return c.textContent;
        }).join(' ').replace(/\s+/g, ' ').toLowerCase();
      }
      return row._text;
    }

    function addItem(ul, label, target, disabled, active) {
      var li = document.createElement('li');
      if (disabled) li.className = 'disabled';
      if (active) li.className = 'active';
      var a = document.createElement('a');
      a.href = '#';
      a.textContent = label;
      a.addEventListener('click', function (e) {
        e.preventDefault();
        if (!disabled && !active) { page = target; render(); }
      });
      li.appendChild(a);
      ul.appendChild(li);
    }

    function buildNav(pages) {
      nav.innerHTML = '';
      if (pages <= 1) return;
      var wrap = document.createElement('div');
      wrap.className = 'pagination';
      var ul = document.createElement('ul');
      addItem(ul, '« Prev', page - 1, page === 1, false);
      var last = 0;
      for (var p = 1; p <= pages; p++) {
        if (p === 1 || p === pages || Math.abs(p - page) <= 1) {
          if (last && p - last > 1) addItem(ul, '…', p, true, false);
          addItem(ul, String(p), p, false, p === page);
          last = p;
        }
      }
      addItem(ul, 'Next »', page + 1, page === pages, false);
      wrap.appendChild(ul);
      nav.appendChild(wrap);
    }

    function render() {
      var q = query.trim().toLowerCase();
      var matched = rows.filter(function (r) {
        return q === '' || rowText(r).indexOf(q) !== -1;
      });

      var pages = Math.max(1, Math.ceil(matched.length / size));
      if (page > pages) page = pages;
      var start = (page - 1) * size;
      var end = Math.min(start + size, matched.length);

      rows.forEach(function (r) { r.style.display = 'none'; });
      matched.slice(start, end).forEach(function (r, i) {
        r.style.display = '';
        var cell = r.cells[0];
        (cell.querySelector('div') || cell).textContent = start + i + 1; // renumber
      });

      noMatch.style.display = (rows.length > 0 && matched.length === 0) ? '' : 'none';

      if (matched.length === 0) {
        info.textContent = rows.length ? 'Showing 0 of 0 entries' : '';
      } else {
        info.textContent = 'Showing ' + (start + 1) + ' to ' + end + ' of ' + matched.length + ' entries' +
          (q && matched.length !== rows.length ? ' (filtered from ' + rows.length + ' total)' : '');
      }
      buildNav(pages);
    }

    sizeSelect.addEventListener('change', function () {
      size = parseInt(sizeSelect.value, 10);
      page = 1;
      render();
    });
    searchInput.addEventListener('input', function () {
      query = searchInput.value;
      page = 1;
      render();
    });

    render();
  }

  document.addEventListener('DOMContentLoaded', function () {
    Array.prototype.forEach.call(document.querySelectorAll('[data-pager]'), init);
  });
})();