$(document).ready(function(){

    FillTopologyBox();
});


function FillTopologyBox(){

    var URL = "/OpenZUI/KeyValueServlet?action=topology";

    $.getJSON(URL, function(result){

        if (result['response'] == null) return;

        var allTopologies = result.response;
        var TopologiesObj = {};
        for (i = 0; i < allTopologies.length; i++)
        {
            TopologiesObj[allTopologies[i].name] = allTopologies[i];
        }
        localStorage.setItem("allTopologies", JSON.stringify(TopologiesObj));

        // var lg_topo = $('#lg-topo');
        $("#lg-topo").empty();
        var lg_topo = document.getElementById("lg-topo");
        var i = 0;

        Object.keys(TopologiesObj).forEach(function (key) { 
            var currentTopo = TopologiesObj[key];
            var topo_name = currentTopo.name;
            var topo_status = currentTopo.status;
            var topo_conf = currentTopo.conf_object;
            
            var elem_a = document.createElement("a");
            elem_a.className = "list-group-item";
            if (i == 0) {
                elem_a.className += " active";
            }
            var span = document.createElement('span');
            span.className = 'badge';
            span.innerHTML = topo_status;
            elem_a.appendChild(span);
            elem_a.appendChild(document.createTextNode(' ' + topo_name));
            // elem_a.setAttribute("id", "element4");
            elem_a.addEventListener("click", function(e){ TopoSelectedEvent(elem_a, topo_name); });
            lg_topo.appendChild(elem_a);
            if (i == 0) {
                TopoSelectedEvent(elem_a, topo_name);
                i = 1;
            }
        })

            
    });
}

function TopoSelectedEvent(element, topo_name){

    console.log("Pressed " + topo_name);

    //$(".list-group a").removeClass("active"); // for all lists
    $("#lg-topo a").removeClass("active"); // for this list
    element.className += " active";

    $("#lg-comp").empty();

    $('#selectedTopo').val(topo_name);

    var allTopos = JSON.parse(localStorage["allTopologies"]);
    var thisTopo = allTopos[topo_name];
    var conf_object = thisTopo["conf_object"];

    if (conf_object != null)
    {
        var lg_comp = document.getElementById("lg-comp");
        var i = 0;

        Object.keys(conf_object).forEach(function (service_id) { 
            var servers = conf_object[service_id];

            var elem_a = document.createElement("a");
            elem_a.className = "list-group-item";
            if (i == 0) {
                elem_a.className += " active";
            }
            elem_a.appendChild(document.createTextNode(' ' + service_id));
            // elem_a.setAttribute("id", "element4");
            elem_a.addEventListener("click", function(e){ CompSelectedEvent(elem_a, topo_name, service_id); });
            lg_comp.appendChild(elem_a);
            if (i == 0) {
                CompSelectedEvent(elem_a, topo_name, service_id);
                i = 1;
            }
        });
    }

    getEndpointStats(topo_name);
    //if (thisTopo.status == "CREATED" || thisTopo.status == "DESIGNED") return;

    
}

function getEndpointStats(topo_name)
{
    var URL = "/OpenZUI/KeyValueServlet?action=endpointstats&name="+topo_name;

    $.getJSON(URL, function(result){

        if (result['response'] == null) return;

        var allServices = result.response;
        var ServicesObj = {};

        Object.keys(allServices).forEach(function (service_id) { 
            var allInstances = allServices[service_id];
            Object.keys(allInstances).forEach(function (instance_id) { 
                var allEndpoints = allInstances[instance_id];
                Object.keys(allEndpoints).forEach(function (endpoint_id) { 
                    var endpoint = allEndpoints[endpoint_id];
                    var messages = endpoint[0];
                    var bytes = endpoint[1];

                    console.log(service_id + " " + instance_id + " " + endpoint_id + " " + messages + " " + bytes);
                });
            });
        });

        // read old vals from local storage
        // update vals
        // write vals back to local storage

        // ServicesObj[topo_name] = allTopologies[i];
        // localStorage.setItem("allTopologies", JSON.stringify(TopologiesObj));

        // for (i = 0; i < allTopologies.length; i++)
        // {
        //     TopologiesObj[allTopologies[i].name] = allTopologies[i];
        // }
    });
}

function CompSelectedEvent(element, topo_name, service_id){
    console.log("Pressed " + topo_name + " " + service_id);


}
