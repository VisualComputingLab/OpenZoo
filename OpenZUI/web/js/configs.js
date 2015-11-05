$(document).ready(function(){

    FillTopologyBox();

    (function worker() {

        var topo_name = $('#selectedTopo').val();
        var allTopos = JSON.parse(localStorage["allTopologies"]);
        var thisTopo = allTopos[topo_name];

        if (thisTopo != null)
        {
            if (thisTopo.status == "STARTED" || thisTopo.status == "SEMISTARTED")
            {
                if ($('#logsToggleButton').is(':checked'))
                {
                    $.ajax({
                        url: "/OpenZUI/ServiceLogServlet?topo=" + topo_name + "&level=" + $('#logLevelDropdown option:selected').val(), 
                        success: function(data) {
                            // console.log(data);
                            if (data == null || data.response == null)
                                console.log("ServiceLogServlet returned nothing");
                            else if (data.response.length == 0)
                                console.log("ServiceLogServlet returned no logs");
                            else
                            {
                                $.each(data['response'], function(key,val){

                                    var line = "<" + val.type + "> : " + val.componentId + " : " + val.date + " : " + val.message;

                                    $('#serviceLogTextArea').val($('#serviceLogTextArea').val() + "\n" + line);
                                });

                                $('#serviceLogTextArea').scrollTop($('#serviceLogTextArea')[0].scrollHeight);
                            }
                        }//,
                        // complete: function() {
                     //         setTimeout(worker, 50000);
                        // }
                    });
                }
                else console.log('is not checked');

                getEndpointStats(topo_name, true);
            }
        }

        setTimeout(worker, 5000);

    })();

    $("#resetCompBtn").on('click', function()
    {
        var topo_name = $('#selectedTopo').val();
        var service_id = $('#selectedComponent').val();

        $("#cnf-action").val("reset");
        $("#cnf-topo").val(topo_name);
        $("#cnf-service").val(service_id);
        $("#updateComponentForm").submit();
    });

    $("#redeployCompBtn").on('click', function()
    {
        var topo_name = $('#selectedTopo').val();
        var service_id = $('#selectedComponent').val();

        $("#cnf-action").val("redeploy");
        $("#cnf-topo").val(topo_name);
        $("#cnf-service").val(service_id);
        $("#updateComponentForm").submit();
    });
});


function FillTopologyBox(){

    $('#redeployCompBtn').hide();
    $('#resetCompBtn').hide();
        
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

    $('#selectedTopo').val(topo_name);
    $('#serviceLogTextArea').val('');

    $('#redeployCompBtn').hide();
    $('#resetCompBtn').hide();

    //$(".list-group a").removeClass("active"); // for all lists
    $("#lg-topo a").removeClass("active"); // for this list
    element.className += " active";

    $("#lg-comp").empty();
    $("#lg-serv").empty();
    $("#lg-endp-comp").empty();
    $("#lg-serv-load").empty();
    $("#lg-endp-serv").empty();
    $("#paper").empty();

    var allTopos = JSON.parse(localStorage["allTopologies"]);
    var thisTopo = allTopos[topo_name];
    var conf_object = thisTopo["conf_object"];
    var graph_object = thisTopo["graph_object"];

    var graph = new joint.dia.Graph;

    var paper = new joint.dia.Paper({
        el: $('#paper'),
        width: 500,
        height: 375,
        gridSize: 1,
        model: graph,
        interactive: false
    });

    if (graph_object != null && graph_object.graph != null)
    {
        var simpleGraph = {};
        simpleGraph["cells"] = [];
        for (var i = 0; i < graph_object.graph.cells.length; i++)
        {
            if (graph_object.graph.cells[i].type != "uml.StartState" && graph_object.graph.cells[i].type != "uml.EndState")
            {
                var state = graph_object.graph.cells[i];
                // if (state.type == "uml.State" && state.attrs != null)
                // {
                //     state.attrs[".uml-state-name"]["font-size"] = 30;
                // }
                simpleGraph["cells"].push(state);
            }
        }

        graph.fromJSON(simpleGraph);
        var opt = {};
        opt.padding = 10;
        paper.scaleContentToFit(opt);
    }

    if (conf_object != null)
    {
        $('#redeployCompBtn').show();
        $('#resetCompBtn').show();

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

function CompSelectedEvent(element, topo_name, service_id){
    //console.log("Pressed " + topo_name + " " + service_id);

    $('#selectedComponent').val(service_id);

    $("#lg-comp a").removeClass("active"); // for this list
    element.className += " active";

    $("#lg-serv").empty();
    $("#lg-endp-comp").empty();
    $("#lg-serv-load").empty();
    $("#lg-endp-serv").empty();


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

            if (i == 0) {
                ServSelectedEvent(elem_a, topo_name, service_id, server_id);
                i = 1;
            }
        });
    }
}

function ServSelectedEvent(element, topo_name, service_id, server_id){
    //console.log("Pressed " + topo_name + " " + service_id + " " + server_id);

    $('#selectedServer').val(server_id);

    $("#lg-serv a").removeClass("active"); // for this list
    element.className += " active";

    $("#lg-serv-load").empty();
    $("#lg-endp-serv").empty();

    var allTopos = JSON.parse(localStorage["allTopologies"]);
    var thisTopo = allTopos[topo_name];
    var conf_object = thisTopo["conf_object"];

    if (conf_object != null)
    {
        var thisComponent = conf_object[service_id];
        var thisServer = thisComponent[server_id];

        // server loads
        var lg_serv_load = document.getElementById("lg-serv-load");

        var URL = "/OpenZUI/KeyValueServlet?action=server&name="+server_id;
        $.getJSON(URL, function(result){
            if (result.response == null) return;
            var StatsURL = "http://" + result.response.address + ":" + result.response.port + "/ServerResources/resources/manage";
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

        getEndpointStats(topo_name, true);
    }
}

function getEndpointStats(topo_name, asynchronous)
{
    var URL = "/OpenZUI/KeyValueServlet?action=endpointstats&name="+topo_name;

    $.ajax({
        url: URL,
        dataType: 'json',
        async: asynchronous,
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
                if (thisService != null)
                {
                    Object.keys(allInstances).forEach(function (server_id) { 
                        var thisServer = allInstances[server_id];
                        thisServer.endpoints = thisService[thisServer["instance_id"]];
                    });
                }
            });

            localStorage.setItem("allTopologies", JSON.stringify(allTopos));

            updateEndpointStats(topo_name);
        }
    });
}

function updateEndpointStats(topo_name)
{
    var allTopos = JSON.parse(localStorage["allTopologies"]);
    var thisTopo = allTopos[topo_name];
    var conf_object = thisTopo["conf_object"];
    var compEndpoints = {};

    if (conf_object != null)
    {
        var service_id = $('#selectedComponent').val();
        var thisComponent = conf_object[service_id];
        var i = 0;

        // sum up endpoint stats for this component
        Object.keys(thisComponent).forEach(function (server_id) { 
            var thisServer = thisComponent[server_id];

            if (thisServer.endpoints != null)
                Object.keys(thisServer.endpoints).forEach(function (endpoint_id) { 
                    var thisEndpoint = thisServer.endpoints[endpoint_id];

                    // sum up instance endpoints
                    if (endpoint_id in compEndpoints)
                    {
                        compEndpoints[endpoint_id][0] += thisEndpoint[0];
                        compEndpoints[endpoint_id][1] += thisEndpoint[1];
                    }
                    else
                    {
                        var newEp = [];
                        newEp[0] = thisEndpoint[0];
                        newEp[1] = thisEndpoint[1];
                        compEndpoints[endpoint_id] = newEp;
                    }
                });
        });

        // update endpoint(comp) stats
        $("#lg-endp-comp").empty();
        var lg_endp_comp = document.getElementById("lg-endp-comp");

        Object.keys(compEndpoints).forEach(function (endpoint_id) { 
            var thisEndpoint = compEndpoints[endpoint_id];
            var elem_li = document.createElement("li");
            elem_li.className = "list-group-item";
            var span = document.createElement('span');
            span.className = 'badge';
            span.innerHTML = "" + formatNum(thisEndpoint[0]) + " (" + formatBytes(thisEndpoint[1]) + ")";
            elem_li.appendChild(span);
            var endpoint_id_short = endpoint_id.substring(endpoint_id.lastIndexOf(":") + 1)
            elem_li.appendChild(document.createTextNode(' ' + endpoint_id_short));
            lg_endp_comp.appendChild(elem_li);
        });

        // update endpoint(inst) stats
        var srv_id = $('#selectedServer').val();
        var thisSrv = thisComponent[srv_id];
        $("#lg-endp-serv").empty();
        var lg_endp_serv = document.getElementById("lg-endp-serv");

        if (thisSrv.endpoints != null)
            Object.keys(thisSrv.endpoints).forEach(function (endpoint_id) { 
                var thisEndp = thisSrv.endpoints[endpoint_id];
                var elem_li = document.createElement("li");
                elem_li.className = "list-group-item";
                var span = document.createElement('span');
                span.className = 'badge';
                span.innerHTML = "" + formatNum(thisEndp[0]) + " (" + formatBytes(thisEndp[1]) + ")";
                elem_li.appendChild(span);
                var endpoint_id_short = endpoint_id.substring(endpoint_id.lastIndexOf(":") + 1)
                elem_li.appendChild(document.createTextNode(' ' + endpoint_id_short));
                lg_endp_serv.appendChild(elem_li);
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

function redeployComponent()
{
    var topo_name = $('#selectedTopo').val();
    var service_id = $('#selectedComponent').val();
    var allTopos = JSON.parse(localStorage["allTopologies"]);
    var thisTopo = allTopos[topo_name];

    if (thisTopo != null)
    {
        var conf_object = thisTopo["conf_object"];

        if (conf_object != null)
        {
            var thisComponent = conf_object[service_id];

            Object.keys(thisComponent).forEach(function (server_id) { 
                var thisServer = thisComponent[server_id];
                var instanceId = thisServer.instance_id;
                
                console.log("Redeploying for " + topo_name + " " + service_id + " " + thisTopo.status + " " + server_id + " " + instanceId);
            });
        }
    }
}

function resetComponent()
{
    var topo_name = $('#selectedTopo').val();
    var service_id = $('#selectedComponent').val();
    var allTopos = JSON.parse(localStorage["allTopologies"]);
    var thisTopo = allTopos[topo_name];
    
    if (thisTopo != null)
    {
        var conf_object = thisTopo["conf_object"];

        if (conf_object != null)
        {
            var thisComponent = conf_object[service_id];

            Object.keys(thisComponent).forEach(function (server_id) { 
                var thisServer = thisComponent[server_id];
                var instanceId = thisServer.instance_id;
                
                console.log("Resetting for " + topo_name + " " + service_id + " " + thisTopo.status + " " + server_id + " " + instanceId);
            });
        }
    }
}

