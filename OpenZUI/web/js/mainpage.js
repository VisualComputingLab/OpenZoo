$(document).ready(function(){

    detailsTopologyConfiguration($('#topologyDropdown option:selected').val());
    
    $('#topologyDropdown').on('change', function(){
    	$('#serviceLogTextArea').val('');
        detailsTopologyConfiguration($('#topologyDropdown option:selected').val());

    });

	// (function worker() {

 //        // console.log("Calling worker");
 //        if (localStorage["selectedTopologyStatus"] == "STARTED" || localStorage["selectedTopologyStatus"] == "SEMISTARTED")
 //        {
 //    		$.ajax({
 //    			url: "/OpenZUI/ServiceLogServlet?topo="+$('#topologyDropdown option:selected').val()+"&level="+$('#logLevelDropdown option:selected').val(), 
 //    			success: function(data) {
 //    		  		console.log(data);
 //    		  		if (data == null || data.response == null)
 //                        console.log("ServiceLogServlet returned nothing");
 //                    else if (data.response.length == 0)
 //                        console.log("ServiceLogServlet returned no logs");
 //                    else
 //                    {
 //                        $.each(data['response'], function(key,val){

 //                            var line = val.type + " (:) " + val.componentId + " (:) " + val.date + " (:) " + val.message;

 //                            $('#serviceLogTextArea').val($('#serviceLogTextArea').val() + "\n" + line);
 //                        });
 //                    }
 //    			}//,
 //    			// complete: function() {
 //    		 //  		setTimeout(worker, 50000);
 //    			// }
 //    		});
 //        }

 //        setTimeout(worker, 5000);

	// })();
});


function detailsTopologyConfiguration(name){

    var URL = "/OpenZUI/KeyValueServlet?action=topoconf&name="+name;
    // console.log(URL);
    $.getJSON(URL, function(result){

        // $("#topologyStatus").val(result.response.status);
        document.getElementById("topologyStatus").innerHTML = result.response.status;
        localStorage["selectedTopologyStatus"] = result.response.status;

        var tBody = $('#serverTableTbody');
        var server_id;

        //console.log(result);

        if (result['response']['servers'] != null)
        {
            $.each(result['response']['servers'], function(key,val){

                var services=[];

                server_id = val.server_id;
                for(var i = 0; i < val.services.length; i++)
                {
                    services.push(" " + val.services[i].service_id + ":" + val.services[i].service_status);
                }

                tBody.append('<tr><td>' + server_id + '</td><td>' + services.toString() + '</td></tr>');
            });
        }
    });
 };
