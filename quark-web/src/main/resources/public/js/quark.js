$(document).ready(function() {
    var timerId = null;
    
    var updateMenu= function(sid){
        $.get("/api/simulations", function(data, status) {
            console.log("simulations");
            var menu = $("#menu");
            menu.empty();
            $(data).each(function(i, item) {
                var selected = item===sid?'list-group-item-primary':'';
                var a = $(`<a href="#" class="list-group-item list-group-item-action ${selected}"/>`).text(item);
                $(a).click(function(e) {
                    updateData(item);
                });
                menu.append(a);
            });
        });
    };
    
    updateMenu();
    
    var createTable= function(id,header,data){
        var table = $(id);
        table.empty();
        var hcells = header.map(function(h){
            return `<td>${h}</td>`    
        });
      
        var header = $(`<thead></thead>`);
        header.append(hcells);
        table.append(header);
        
        for(let row of data){
            var trow = $(`<tr></tr>`);
            for(let cell of row){
               trow.append(`<td>${cell}</td>`);              
            }
            table.append(trow);
         }
    };
    
    var fromLapReport = function(id,lapReport){
        var table = $(id).find("table");
        var time = $(id).find(".time");
        table.empty();
        time.empty();
        if(lapReport!=null){
            time.text(lapReport.dateTime);
            
            var balances =Object.values(lapReport.balanceListing.balances);
            var values = balances.map(el=>[el.symbol,el.available,el.available*el.currency.usd]);        
            createTable(table,["Symbol","Value","USD"],values);            
        }
    }
    
    var updatePlots = function(sid){
        $.get(`/api/simulation/${sid}/plots`, function(data, status) {
           if (!$.isEmptyObject(data)) {
               var lineData = data.map(function(el) {
                   if(el.type === 'bar'){
                       el.type = "scatter";
                       el.mode = "markers";
                       el.marker = {size:12};
                       el.yaxis="y2";
                   }
                   return el;
               });                     
               if(!$.isEmptyObject(lineData)){
                   var layout = {
                       title: 'Action Over Time',
                       yaxis: {title: 'Avg Value'},
                       yaxis2: {
                                   title: 'Buys and Sells',
                                   titlefont: {color: 'rgb(148, 103, 189)'},
                                   tickfont: {color: 'rgb(148, 103, 189)'},
                                   overlaying: 'y',
                                   side: 'right'
                       }
                   };
                   Plotly.newPlot("avg-chart", lineData,layout);                           
               }
          }
        });
    };          
    var updateOrders = function(sid){
        $.get(`/api/simulation/${sid}/orders`,{success:true}, function(data, status) {
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
    }
    var updateData = function(sid) {
      console.log("waiting for data");
      $("#simId").text(sid);
      $("#loading").html('<i class="fas fa-spinner fa-pulse"></i>');
      updateMenu(sid);
      $.get(`/api/simulation/${sid}`, function(data, status) {
          if (!$.isEmptyObject(data)) {
              if(data.complete){
                  $("#loading").hide();
                  if(timerId){
                     clearInterval(timerId);   
                     timerId=null;
                  }
              }else{
                  if(timerId===null){
                      timerId = setInterval(function() {
                          updateData(sid)
                      }, 5000);
                  }
              }
              
              var params = Object.entries(data.params);
              createTable("#params", ["Name","Value"], params);
              
              fromLapReport("#start",data.lapReports[0]);
              fromLapReport("#end",data.lapReports.slice(-1)[0]);
          }
      });
      updatePlots(sid);
      updateOrders(sid);				
    };
    
    var manageTimer = function(){
        
    }
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
                    var sid = data;
                    updateData(sid)
                });
            });			

		});