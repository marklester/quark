$(document).ready(
        function() {
			var updateData = function(sid) {
				console.log("waiting for data");
				$("#simId").text(sid);
				$.get(`/api/simulation/${sid}/plots`, function(data,
						status) {
					if (!$.isEmptyObject(data)) {
						var lineData = data.filter(function(el) {
							return el.type === 'scatter'
						});

						var barData = data.filter(function(el) {
							return el.type === 'bar'
						});
						Plotly.newPlot("avg-chart", lineData);
						Plotly.newPlot("buy-sell-chart", barData);
						clearInterval(timerId);
					}
				})
				$.get(`/api/simulation/${sid}/orders`, function(data,
						status) {
					console.log("data" + data);
					if (!$.isEmptyObject(data)) {
						var table = $("<table></table>").addClass("table table-sm table-striped")
						var trow = $(`<tr>
                        <th>TIME</th>
						<th>SYMBOL</th>
						<th>TYPE</th>
						<th>PRICE</th>
						<th>AMOUNT</th>
						</tr>
						`);
						table.append(trow);
						var sorted = data.sort(function(a,b){
						    var da = Date.parse(a.order.time);
						    var db = Date.parse(b.order.time);
						    return da-db;
						})
						for(let item of sorted){
						  var ktype = item.order.orderType==="BUY"?'table-success':'table-danger';
						  var trow = $(`<tr>
                                     <td>${item.order.time}</td>
									<td>${item.order.tradePair.symbol}</td>
									<td class=${ktype}>${item.order.orderType}</td>
									<td>${item.receipt.price.available} ${item.receipt.price.currency.symbol}</td>
									<td>${item.receipt.amount}</td>
									</tr>`);
								table.append(trow);
							$("#orders").html(table);							
						}

					}
				})
			};
			var timerId = null;
			$.get("/api/simulations", function(data, status) {
				console.log("simulations");
				$(data).each(function(i, item) {
					var li
					var li = $('<li class="nav-item">');
					var a = $('<a class="nav-link" href="#">').text(item);
					li.append(a);
					$(a).click(function(e) {
						console.log("click" + item);
						updateData(item);
					});
					$("#menu").append(li);
				});
			});

			$("#run").submit(
					function(e) {
						e.preventDefault();
						console.log("run simulation");
						$.get("/api/simulate", {
							tickRate : 1,
							shortAvg : 1,
							longAvg : 3
						}, function(data, status) {
							console.log("received data:" + data);
							var ctx = $('#avg-chart').html(
									'<i class="fas fa-spinner fa-pulse"></i>');
							var currentSim = data;
							timerId = setInterval(function() {
								updateData(currentSim)
							}, 5000);
						})
					});
		});