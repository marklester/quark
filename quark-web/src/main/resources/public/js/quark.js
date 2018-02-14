$(document).ready(
        function() {
			var updateData = function(sid) {
				console.log("waiting for data");
				$("#simId").text(sid);
				$.get(`/api/simulation/${sid}/parameters`, function(data,
                        status) {
                    if (!$.isEmptyObject(data)) {
                            var table= $("#params");
                            table.empty();
                            for(var p in data){
                                var trow = $(`<tr>
                                        <td>${p}</td>
                                        <td>${data[p]}</td>
                                       </tr>`);
                                   table.append(trow);
                            }
                    }
                });
				
				$.get(`/api/simulation/${sid}/plots`, function(data,
						status) {
					if (!$.isEmptyObject(data)) {
						var lineData = data.filter(function(el) {
							return el.type === 'scatter'
						});

						var barData = data.filter(function(el) {
							return el.type === 'bar'
						});
						if(!$.isEmptyObject(lineData)){
	                        Plotly.newPlot("avg-chart", lineData);						    
						}
						if(!$.isEmptyObject(lineData)){
	                        Plotly.newPlot("buy-sell-chart", barData);						    
						}
					}
				})
				$.get(`/api/simulation/${sid}/orders`,{success:true}, function(data,
						status) {
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
						  var receipt = item.receipt;
						  var trow = $(`<tr>
                                     <td>${item.order.time}</td>
									<td>${item.order.tradePair.symbol}</td>
									<td class=${ktype}>${item.order.orderType}</td>
									<td>${item.receipt.price.available} ${item.receipt.price.currency.symbol}</td>
									<td>${item.receipt.amount}</td>
									</tr>`);
								table.append(trow);
						}
                        $("#orders").html(table);                           

					}
				});				
			};
			var timerId = null;
			$.get("/api/simulations", function(data, status) {
				console.log("simulations");
				$(data).each(function(i, item) {
					var li = $('<li class="list-group-item">');
					var a = $('<a href="#">').text(item);
					li.append(a);
					$(a).click(function(e) {
						console.log("click" + item);
						updateData(item);
					});
					$("#menu").append(li);
				});
			});

			$("#run").click(
					function(e) {
					    $("#simconfig").modal('hide');
						// e.preventDefault();
						console.log("run simulation");
						$.get("/api/simulate", {
							tickRate : $("#tick").val(),
							shortAvg : $("#short").val(),
							longAvg : $("#long").val(),
                            startFund : $("#startFund").val(),
                            portfolioSize : $("#portfolioSize").val()
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