/*
 * Google Charts used on the admin dashboard and the reports page
 * (same charts as admin/index.php and admin/reports.php).
 * Each function receives plain data passed from the server by Thymeleaf.
 */
(function (window) {
    var queue = [];
    var ready = false;

    google.charts.load('current', {packages: ['corechart']});
    google.charts.setOnLoadCallback(function () {
        ready = true;
        queue.forEach(function (fn) { fn(); });
        queue = [];
    });

    function whenReady(fn) {
        if (ready) { fn(); } else { queue.push(fn); }
    }

    function toRows(header, map) {
        var rows = [header];
        Object.keys(map || {}).forEach(function (k) { rows.push([k, Number(map[k])]); });
        if (rows.length === 1) { rows.push(['No data', 0]); }
        return rows;
    }

    function redrawOnResize(draw) {
        var t;
        window.addEventListener('resize', function () {
            clearTimeout(t);
            t = setTimeout(draw, 200);
        });
    }

    window.GymCharts = {
        /** Pie with a hole: members by gender, staff by designation, ... */
        donut: function (elementId, header, map) {
            whenReady(function () {
                var draw = function () {
                    var el = document.getElementById(elementId);
                    if (!el) { return; }
                    var data = google.visualization.arrayToDataTable(toRows(header, map));
                    new google.visualization.PieChart(el).draw(data, {pieHole: 0.4, chartArea: {width: '90%', height: '85%'}});
                };
                draw();
                redrawOnResize(draw);
            });
        },

        /** Vertical bars: members per service. */
        columns: function (elementId, header, map) {
            whenReady(function () {
                var draw = function () {
                    var el = document.getElementById(elementId);
                    if (!el) { return; }
                    var data = google.visualization.arrayToDataTable(toRows(header, map));
                    new google.visualization.ColumnChart(el).draw(data, {
                        legend: {position: 'none'},
                        bar: {groupWidth: '60%'},
                        vAxis: {minValue: 0, format: '0'}
                    });
                };
                draw();
                redrawOnResize(draw);
            });
        },

        /** Horizontal bars: total earnings vs. total expenses. */
        earnings: function (elementId, earnings, expenses) {
            whenReady(function () {
                var draw = function () {
                    var el = document.getElementById(elementId);
                    if (!el) { return; }
                    var data = google.visualization.arrayToDataTable([
                        ['Terms', 'Total Amount', {role: 'style'}],
                        ['Earnings', Number(earnings), '#28b779'],
                        ['Expenses', Number(expenses), '#da542e']
                    ]);
                    new google.visualization.BarChart(el).draw(data, {
                        legend: {position: 'none'},
                        bar: {groupWidth: '60%'},
                        hAxis: {minValue: 0, format: '$#,###'}
                    });
                };
                draw();
                redrawOnResize(draw);
            });
        }
    };
})(window);
