var timerId = null;

var updateMenu = function (sid) {
    $.get("/api/simulations", function (data, status) {
        var menu = $("#menu");
        menu.empty();
        $(data).each(function (i, item) {
            var selected = item === sid ? 'list-group-item-primary' : '';
            var a = $(`<a href="#" class="list-group-item list-group-item-action ${selected}"/>`).text(item);
            $(a).click(function (e) {
                updateData(item);
            });
            menu.append(a);
        });
    });
};

updateMenu();

var createTable = function (id, header, data) {
    var table = $(id);
    table.empty();
    var hcells = header.map(function (h) {
        return `<td>${h}</td>`
    });

    var header = $(`<thead></thead>`).addClass("thead-light");
    header.append(hcells);
    table.append(header);

    for (let row of data) {
        var trow = $(`<tr></tr>`);
        for (let cell of row) {
            trow.append(`<td>${cell}</td>`);
        }
        table.append(trow);
    }
};
let updateBalanceCard = function (sid) {
    $.get(`/api/simulation/${sid}/start-balances`, function (data, status) {
        if (!$.isEmptyObject(data)) {
            let bals = data.map(el => [el.symbol, el.available, el.usd])
                .sort((a, b) => a[0].localeCompare(b[0]));
            let total = bals.map(el => el[2]).reduce((acc, cur) => acc + cur);
            createTable("#start > table", ["Symbol", "Value", "USD"], bals);
            let totalEl = $("#start").find(".total");
            totalEl.html(`<b>Total:</b> ${total}`);
        }
    });
}

var fromLapReport = function (id, lapReport) {
    var table = $(id).find("table");
    var time = $(id).find(".time");
    var total = $(id).find(".total");
    table.empty();
    time.empty();
    total.empty();
    if (lapReport != null) {
        time.text(lapReport.dateTime);
        total.text(lapReport.balanceListing.total);
        var balances = Object.values(lapReport.balanceListing.balances);
        var values = balances.map(el => [el.symbol, el.available, el.available * el.currency.usd]);
        createTable(table, ["Symbol", "Value", "USD"], values);
    }
}

var updatePlots = function (sid) {
    $.get(`/api/simulation/${sid}/plots`, function (data, status) {
        if (!$.isEmptyObject(data)) {
            var lineData = data.map(function (el) {
                if (el.type === 'bar') {
                    el.type = "scatter";
                    el.mode = "markers";
                    el.marker = { size: 12 };
                    el.yaxis = "y2";
                }
                return el;
            });
            if (!$.isEmptyObject(lineData)) {
                var layout = {
                    title: 'Action Over Time',
                    yaxis: { title: 'Avg Value' },
                    yaxis2: {
                        title: 'Buys and Sells',
                        titlefont: { color: 'rgb(148, 103, 189)' },
                        tickfont: { color: 'rgb(148, 103, 189)' },
                        overlaying: 'y',
                        side: 'right'
                    }
                };
                Plotly.newPlot("avg-chart", lineData, layout);
            }
        }
    });
};
var updateData = function (sid) {
    $("#simId").text(sid);
    $("#loading").html('<i class="fas fa-spinner fa-pulse"></i>');
    updateMenu(sid);
    $.get(`/api/simulation/${sid}`, function (data, status) {
        if (!$.isEmptyObject(data)) {
            if (data.complete) {
                $("#loading").css("color", "Tomato").empty().html(`<i class="fas fa-check-square"></i>`);
                if (timerId) {
                    clearInterval(timerId);
                    timerId = null;
                }
            } else {
                if (timerId === null) {
                    timerId = setInterval(function () {
                        updateData(sid)
                    }, 5000);
                }
            }

            var params = Object.entries(data.params);
            createTable("#params", ["Name", "Value"], params);
            updateReports(data.lapReports);
            fromLapReport("#end", data.lapReports.slice(-1)[0]);
        }
    });
    updateBalanceCard(sid);
    updatePlots(sid);
};

let updateReports = function (reports) {
    let reportsDiv = $("#lap-reports");
    for (const [index, report] of reports.entries()) {
        let id = `report-${index}`
        let reportEl = $(`
        <div class="card">
            <div class="card-header" id="heading-${id}">
            <h5 class="mb-0">
                <button class="btn btn-link" data-toggle="collapse" data-target="#${id}" aria-expanded="true" aria-controls="${id}">
                ${report.dateTime} 
                </button>
                ${report.balanceListing.total}
            </h5>
            </div>
            <div id="${id}" class="collapse" aria-labelledby="heading-${id}" data-parent="#accordion">
                <div class="card-body">
                    <div class="balances"><h4>Balances</h4><table></table></div>
                    <div class="row">
                        <div class="orders col"></div>
                        <div class="details col"></div>
                    </div>
                </div>
            </div>
        </div>`);
        var btable = reportEl.find(".balances > table").addClass("table table-small table-bordered");
        var balances = Object.values(report.balanceListing.balances);
        var values = balances.map(el => [el.symbol, el.available, el.available * el.currency.usd]);
        createTable(btable, ["Symbol", "Value", "USD"], values);

        reportsDiv.append(reportEl);
        $("#" + id).on('shown.bs.collapse', function () {
            let orders = report.processedOrders;
            $("#" + id).find(".orders").empty().append(createOrderTable(orders, reportEl));

        });
    }
};

let createOrderTable = function (porders, reportEl) {
    let table = $("<table/>").addClass("table table-sm table-hover");
    let header = `<thead class="thead-dark">
        <tr>
        <th>TradePair</th>
        <th>Order Type</th>
        <th>Percent Allocated</th>
        <th>Success</th>
        <th>Message</th>
        <th>Decision</th>
        </tr>
    </thead>
    <tbody></tbody>`;
    table.append(header);
    for (const porder of porders.sort((a, b) => b.success - a.success)) {
        let order = porder.order;
        let decision = order.decision;
        let tr = $(`<tr>
                <td>${order.tradePair.label}</td>
                <td>${order.orderType}</td>
                <td>${order.percentage * 100}% </td>
                <td>${porder.success}</td>
                <td>${porder.message || ""}</td>
                <td>${((decision.shortAvg - decision.longAvg) / decision.longAvg) * 100}</td>
            </tr>`);
        tr.click(function (el) {
            $(table).find("tr").removeClass("table-active");
            $(tr).addClass("table-active");
            let receipt = porder.receipt;
            let details = reportEl.find(".details");
            console.log(details);
            let unit = receipt.unitPrice;
            details.html(`<h3>Receipt</h3>
                <div>To: ${receipt.product.symbol} ${receipt.product.available}($${receipt.product.usd})</div>
                <div> From: ${receipt.price.symbol} ${receipt.price.available}(${receipt.price.usd})</div>
                <div> Fee: ${receipt.fee}</div>
                <div> Unit: ${unit} ($${unit * receipt.product.currency.usd})</div>
            `);
        });
        table.append(tr);
    }
    return table;
}

$("#run").click(
    function (e) {
        $("#simconfig").modal('hide');
        let sbals = {};
        $(".balance-row").each(function (i, el) {
            console.log(el);
            let symbol = $(el).find(".balance-symbol").val();
            sbals[symbol] = $(el).find(".balance-amount").val();
        });
        console.log(sbals);
        let postBody = {
            tickRate: $("#tick").val(),
            shortAvg: $("#short").val(),
            longAvg: $("#long").val(),
            portfolioSize: $("#portfolioSize").val(),
            startingBalances: sbals
        };
        console.log("body %O", postBody);
        console.log("run simulation");
        $.ajax({
            url: "/api/simulate",
            type: "POST",
            data: JSON.stringify(postBody),
            contentType: "application/json; charset=utf-8",
            success: function (data, status) {
                let sid = data;
                updateData(sid)
            }
        });
    });

$("#addbalance").click(
    function (e) {
        var bcount = $(".balance-row").length;
        var id = `balance.${bcount}`;
        var sid = `${id}.symbol`;
        console.log("add balance to form");
        var input = `<div class="form-row balance-row">
              <div class="form-group col-md6">
                <label for="${id}" class="col-form-label">Balance ${bcount}:</label> 
                <input id="${id} type="text" class="form-control balance-amount" >
              </div>
              <div class="form-group col-md6">
                <label for="${sid}" class="col-form-label">Symbol:</label> 
                <input id="${sid} type="text" class="form-control balance-symbol" ">
              </div>
            </div>`;
        $("#simconfig form").append(input);

        let pSizeEl = $("#portfolioSize");
        if (pSizeEl.val() < bcount) {
            pSizeEl.val(pSizeEl.val() + 1);
        }
    });
