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

    //console.log("Pressed " + topo_name);

    getEndpointStats(topo_name);

    $('#redeployBtn').hide();

    //$(".list-group a").removeClass("active"); // for all lists
    $("#lg-topo a").removeClass("active"); // for this list
    element.className += " active";

    $("#lg-comp").empty();
    $("#lg-serv").empty();
    $("#lg-endp-comp").empty();
    $("#lg-serv-load").empty();
    $("#lg-endp-serv").empty();

    $('#selectedTopo').val(topo_name);

    var allTopos = JSON.parse(localStorage["allTopologies"]);
    var thisTopo = allTopos[topo_name];
    var conf_object = thisTopo["conf_object"];

    if (conf_object != null)
    {
        $('#redeployBtn').show();

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
}

function getEndpointStats(topo_name)
{
    var URL = "/OpenZUI/KeyValueServlet?action=endpointstats&name="+topo_name;

    $.ajax({
        url: URL,
        dataType: 'json',
        async: false,
        //data: myData,
        success: function(result) {
            if (result['response'] == null) return;

            var allServices = result.response;
            // var ServicesObj = {};
            var allTopos = JSON.parse(localStorage["allTopologies"]);
            var thisTopo = allTopos[topo_name];
            var thisConf = thisTopo["conf_object"];

            Object.keys(thisConf).forEach(function (service_id) { 
                var allInstances = thisConf[service_id];
                var thisService = allServices[service_id];
                Object.keys(allInstances).forEach(function (server_id) { 
                    var thisServer = allInstances[server_id];
                    thisServer.endpoints = thisService[thisServer["instance_id"]];
                });
            });

            localStorage.setItem("allTopologies", JSON.stringify(allTopos));
        }
    });
}

function CompSelectedEvent(element, topo_name, service_id){
    //console.log("Pressed " + topo_name + " " + service_id);

    $("#lg-comp a").removeClass("active"); // for this list
    element.className += " active";

    $("#lg-serv").empty();
    $("#lg-endp-comp").empty();
    $("#lg-serv-load").empty();
    $("#lg-endp-serv").empty();

    $('#selectedComponent').val(service_id);


    var allTopos = JSON.parse(localStorage["allTopologies"]);
    var thisTopo = allTopos[topo_name];
    var conf_object = thisTopo["conf_object"];
    var compEndpoints = {};

    if (conf_object != null)
    {
        var thisComponent = conf_object[service_id];
        var lg_serv = document.getElementById("lg-serv");
        var i = 0;

        Object.keys(thisComponent).forEach(function (server_id) { 
            var thisServer = thisComponent[server_id];

            //<a onclick="console.log('vseen clicked');"> vseen</a>
            //<a onclick="console.log('basement_107 clicked');"> basement_107</a> 
            var elem_a = document.createElement("a");
            elem_a.className = "list-group-item";
            if (i == 0) {
                elem_a.className += " active";
            }
            var span = document.createElement('span');
            span.className = 'badge';
            span.innerHTML = thisServer.status;
            elem_a.appendChild(span);
            elem_a.appendChild(document.createTextNode(' ' + server_id));
            elem_a.addEventListener("click", function(e){ ServSelectedEvent(elem_a, topo_name, service_id, server_id); });
            lg_serv.appendChild(elem_a);

            // sum up enpoint stats for this component
            if (thisServer.endpoints != null)
                Object.keys(thisServer.endpoints).forEach(function (endpoint_id) { 
                    var thisEndpoint = thisServer.endpoints[endpoint_id];
                    if (endpoint_id in compEndpoints)
                    {
                        compEndpoints[endpoint_id][0] += thisEndpoint[0];
                        compEndpoints[endpoint_id][1] += thisEndpoint[1];
                    }
                    else compEndpoints[endpoint_id] = thisEndpoint;
                });

            if (i == 0) {
                ServSelectedEvent(elem_a, topo_name, service_id, server_id);
                i = 1;
            }
        });

        var lg_endp_comp = document.getElementById("lg-endp-comp");
        Object.keys(compEndpoints).forEach(function (endpoint_id) { 
            var thisEndpoint = compEndpoints[endpoint_id];
            var elem_li = document.createElement("li");
            elem_li.className = "list-group-item";
            var span = document.createElement('span');
            span.className = 'badge';
            span.innerHTML = "" + formatNum(thisEndpoint[0]) + " (" + formatBytes(thisEndpoint[1]) + ")";
            elem_li.appendChild(span);
            elem_li.appendChild(document.createTextNode(' ' + endpoint_id));
            // ------------------------------------------------
            //<h4 class="list-group-item-heading">First List Group Item Heading</h4>
            //<p class="list-group-item-text">List Group Item Text</p>
            // var elem_a = document.createElement("a");
            // elem_a.className = "list-group-item";
            // var h5 = document.createElement('h5');
            // h5.className = 'list-group-item-heading';
            // h5.innerHTML = ' ' + endpoint_id;
            // elem_a.appendChild(h5);
            // var pp = document.createElement('p');
            // pp.className = 'list-group-item-text';
            // pp.innerHTML = "" + formatNum(thisEndpoint[0]) + " messages (" + formatBytes(thisEndpoint[1]) + ")";
            // elem_a.appendChild(pp);
            // elem_li.appendChild(document.createTextNode(' ' + endpoint_id));
            // ------------------------------------------------

            lg_endp_comp.appendChild(elem_li);
            // lg_endp_comp.appendChild(elem_a);
        });
    }
}

function ServSelectedEvent(element, topo_name, service_id, server_id){
    //console.log("Pressed " + topo_name + " " + service_id + " " + server_id);

    $("#lg-serv a").removeClass("active"); // for this list
    element.className += " active";

    $("#lg-serv-load").empty();
    $("#lg-endp-serv").empty();

    $('#selectedServer').val(server_id);

    var allTopos = JSON.parse(localStorage["allTopologies"]);
    var thisTopo = allTopos[topo_name];
    var conf_object = thisTopo["conf_object"];

    if (conf_object != null)
    {
        var thisComponent = conf_object[service_id];
        var thisServer = thisComponent[server_id];

        // instance endpoints
        var lg_endp_serv = document.getElementById("lg-endp-serv");
        if (thisServer.endpoints != null)
            Object.keys(thisServer.endpoints).forEach(function (endpoint_id) { 
                var thisEndpoint = thisServer.endpoints[endpoint_id];
                var elem_li = document.createElement("li");
                elem_li.className = "list-group-item";
                var span = document.createElement('span');
                span.className = 'badge';
                span.innerHTML = "" + formatNum(thisEndpoint[0]) + " (" + formatBytes(thisEndpoint[1]) + ")";
                elem_li.appendChild(span);
                elem_li.appendChild(document.createTextNode(' ' + endpoint_id)); // ------------------------------------------------
                lg_endp_serv.appendChild(elem_li);
            });

        // server loads
        var lg_serv_load = document.getElementById("lg-serv-load");

        var URL = "/OpenZUI/KeyValueServlet?action=server&name="+server_id;
        $.getJSON(URL, function(result){
            if (result.response == null) return;
            var StatsURL = "http://" + result.response.address + ":" + result.response.port + "/ServerStatistics/resources/stats";
            $.getJSON(StatsURL, function(sresult){
                var cpu = Math.floor(sresult.cpu.systemCpuLoad * 100.0);
                var memFree = sresult.mem.physicalFree;
                var memFreeHuman = formatBytes(memFree);
                var memTotal = sresult.mem.physicalTotal;
                var memPc = Math.floor((memTotal - memFree) / memTotal * 100);
                var discFree = sresult.space.free;
                var discFreeHuman = formatBytes(discFree);
                var discTotal = sresult.space.total;
                var discPc = Math.floor((discTotal - discFree) / discTotal * 100);

                var elem_c = document.createElement("a");
                elem_c.className = "list-group-item";
                var h5 = document.createElement('h5');
                h5.className = 'list-group-item-heading';
                h5.innerHTML = "CPU Usage";
                elem_c.appendChild(h5);
                var div_c = document.createElement('div');
                div_c.className = 'progress';
                var div_c_in = document.createElement('div');
                div_c_in.className = 'progress-bar';
                if (cpu <= 30) div_c_in.className += ' progress-bar-info';
                else if (cpu <= 70) div_c_in.className += ' progress-bar-warning';
                else div_c_in.className += ' progress-bar-danger';
                div_c_in.setAttribute("role", "progressbar");
                div_c_in.setAttribute("aria-valuenow", "" + cpu);
                div_c_in.setAttribute("aria-valuemin", "0");
                div_c_in.setAttribute("aria-valuemax", "100");
                div_c_in.setAttribute("style", "min-width: 2em; width: " + cpu + "%;");
                div_c_in.innerHTML = "" + cpu + " %";
                div_c.appendChild(div_c_in);
                elem_c.appendChild(div_c);
                lg_serv_load.appendChild(elem_c);

                var elem_m = document.createElement("a");
                elem_m.className = "list-group-item";
                var h5 = document.createElement('h5');
                h5.className = 'list-group-item-heading';
                h5.innerHTML = "Memory (" + memFreeHuman + " free)";
                elem_m.appendChild(h5);
                var div_m = document.createElement('div');
                div_m.className = 'progress';
                var div_m_in = document.createElement('div');
                div_m_in.className = 'progress-bar';
                if (memPc <= 30) div_m_in.className += ' progress-bar-info';
                else if (memPc <= 70) div_m_in.className += ' progress-bar-warning';
                else div_m_in.className += ' progress-bar-danger';
                div_m_in.setAttribute("role", "progressbar");
                div_m_in.setAttribute("aria-valuenow", "" + memPc);
                div_m_in.setAttribute("aria-valuemin", "0");
                div_m_in.setAttribute("aria-valuemax", "100");
                div_m_in.setAttribute("style", "min-width: 2em; width: " + memPc + "%;");
                div_m_in.innerHTML = "" + memPc + " %";
                div_m.appendChild(div_m_in);
                elem_m.appendChild(div_m);
                lg_serv_load.appendChild(elem_m);

                var elem_d = document.createElement("a");
                elem_d.className = "list-group-item";
                var h5 = document.createElement('h5');
                h5.className = 'list-group-item-heading';
                h5.innerHTML = "Disc (" + discFreeHuman + " free)";
                elem_d.appendChild(h5);
                var div_d = document.createElement('div');
                div_d.className = 'progress';
                var div_d_in = document.createElement('div');
                div_d_in.className = 'progress-bar';
                if (discPc <= 30) div_d_in.className += ' progress-bar-info';
                else if (discPc <= 70) div_d_in.className += ' progress-bar-warning';
                else div_d_in.className += ' progress-bar-danger';
                div_d_in.setAttribute("role", "progressbar");
                div_d_in.setAttribute("aria-valuenow", "" + discPc);
                div_d_in.setAttribute("aria-valuemin", "0");
                div_d_in.setAttribute("aria-valuemax", "100");
                div_d_in.setAttribute("style", "min-width: 2em; width: " + discPc + "%;");
                div_d_in.innerHTML = "" + discPc + " %";
                div_d.appendChild(div_d_in);
                elem_d.appendChild(div_d);
                lg_serv_load.appendChild(elem_d);
            });
        });        
    }
}

function formatBytes(numBytes)
{
    var numBytesHuman = numBytes;
    var numUnit = 0;
    var numUnitHuman;
    while (numBytesHuman >= 1024)
    {
        numBytesHuman = Math.floor(numBytesHuman / 1024);
        numUnit++;
    }
    switch (numUnit) {
        case 0: numUnitHuman = "B"; break;
        case 1: numUnitHuman = "KB"; break;
        case 2: numUnitHuman = "MB"; break;
        case 3: numUnitHuman = "GB"; break;
        default: numUnitHuman = "TB";
    }

    return "" + numBytesHuman + " " + numUnitHuman;
}

function formatNum(num)
{
    var numHuman = num;
    var numUnit = 0;
    var numUnitHuman;
    while (numHuman >= 1000)
    {
        numHuman = Math.floor(numHuman / 1000);
        numUnit++;
    }
    switch (numUnit) {
        case 0: numUnitHuman = ""; break;
        case 1: numUnitHuman = "K"; break;
        case 2: numUnitHuman = "Mio"; break;
        default: numUnitHuman = "Bio";
    }

    return "" + numHuman + " " + numUnitHuman;
}