// @author Dimitris Samaras <dimitris.samaras@iti.gr>

var warfiles = {};
// the graph configuration object
var graphConf = [];
// flag in case topology's parameters are not fully set
var notDirty = false;
// the element object from graphConf caught on pointerdown (click)
var theObj = [];
// graphical element id that is being manipulated
var objectId = "";
//objectId on form focus IN
var focusObjectId = "";
// buffer variable to pass target node ID  to routing_manager in case of connection -->route
var targetId_for_routing = "";
//targetId on form focus IN
var target_in_endpoint = "";
//The routing instance selected in routing form
var routing_instance = "";

// attributes' object to prettify links - transitions
var linkAttrs = {
    'fill': 'none',
    'stroke-linejoin': 'round',
    'stroke-width': '2',
    'stroke': '#4b4a67'
}


function adjustVertices(graph, cell) {

    // If the cell is a view, find its model.
    cell = cell.model || cell;

    if (cell instanceof joint.dia.Element) {
        _.chain(graph.getConnectedLinks(cell)).groupBy(function(link) {
            // the key of the group is the model id of the link's source or target, but not our cell id.
            return _.omit([link.get('source').id, link.get('target').id], cell.id)[0];
        }).each(function(group, key) {
            // If the member of the group has both source and target model adjust vertices.
            if (key !== 'undefined')
                adjustVertices(graph, _.first(group));
        });

        return;
    }

    // The cell is a link. Let's find its source and target models.
    var srcId = cell.get('source').id || cell.previous('source').id;
    var trgId = cell.get('target').id || cell.previous('target').id;

    // If one of the ends is not a model, the link has no siblings.
    if (!srcId || !trgId)
        return;

    var siblings = _.filter(graph.getLinks(), function(sibling) {
        var _srcId = sibling.get('source').id;
        var _trgId = sibling.get('target').id;
        return (_srcId === srcId && _trgId === trgId) || (_srcId === trgId && _trgId === srcId);
    });

    switch (siblings.length) {

        case 0:
            // The link was removed and had no siblings.
            break;

        case 1:
            // There is only one link between the source and target. No vertices needed.
            cell.unset('vertices');
            break;

        default:

            // There is more than one siblings. We need to create vertices.

            // First of all we'll find the middle point of the link.
            var srcCenter = graph.getCell(srcId).getBBox().center();
            var trgCenter = graph.getCell(trgId).getBBox().center();
            var midPoint = g.line(srcCenter, trgCenter).midpoint();

            // Then find the angle it forms.
            var theta = srcCenter.theta(trgCenter);

            // This is the maximum distance between links
            var gap = 20;

            _.each(siblings, function(sibling, index) {

                // We want the offset values to be calculated as follows 0, 20, 20, 40, 40, 60, 60 ..
                var offset = gap * Math.ceil(index / 2);

                // Now we need the vertices to be placed at points which are 'offset' pixels distant
                // from the first link and forms a perpendicular angle to it. And as index goes up
                // alternate left and right.
                //
                //  ^  odd indexes 
                //  |
                //  |---->  index 0 line (straight line between a source center and a target center.
                //  |
                //  v  even indexes
                var sign = index % 2 ? 1 : -1;
                var angle = g.toRad(theta + sign * 90);

                // We found the vertex.
                var vertex = g.point.fromPolar(offset, angle, midPoint);

                sibling.set('vertices', [{x: vertex.x, y: vertex.y}]);
            });
    }
}

function connection_manager_reload(sourceId, targetId, theObj, objectId) {
    //else close all forms and clean their fields to reload them
    //console.log(sourceId +" "+targetId);
    if (typeof sourceId==="undefined" || typeof targetId==="undefined"){
//        cellD =graph.getCell(objectId);
//        cellD.remove();
        return;    
    }else{
    $('#service_manager').hide();
    $('#connection_manager').hide();
    //$('#routing_field').hide();
    $('#routing_manager').hide();
    $(".addToConnectionForm").remove();
    $("#connection_form").removeClass();
    $("#inEndpointsList").empty();
    $("#outEndpointsList").empty();
    $('#conn_mapping').val('conn_available').change();
    $('#routing_keys').val('');

    $("#connection_form").prepend('<div class="addToConnectionForm"><label>Connection <i>' + sourceId + '</i> to <i>' + targetId + '</i> configuration</label><hr></div>');
    $("#connection_form").addClass(objectId);

    targetId_for_routing = targetId;

    var s = localStorage[sourceId];
    var sf = JSON.parse(s);
    var sourceEndpoints = sf.out_ep;


    var t = localStorage[targetId];
    var tf = JSON.parse(t);

    var targetEndpoints = tf.in_ep;


    var outEndpoints = "<option value='blank' disabled selected></option>";
    sourceEndpoints.map(function(item) {
        outEndpoints += "<option value='" + item + "'>" + item.substring(item.lastIndexOf(".") + 1) + "</option>";
    });
    $("#outEndpointsList").html(outEndpoints);


    var inEndpoints = "<option value='blank' disabled selected></option>";
    targetEndpoints.map(function(item) {
        inEndpoints += "<option value='" + item + "'>" + item.substring(item.lastIndexOf(".") + 1) + "</option>";
    });

    $("#inEndpointsList").html(inEndpoints);

    //if service is already configured load its values
    if (theObj.length > 0) {
        var selects = $('#connection_form select');
        selects.each(function() {
            var inp = this.name
            var clmnt = $.grep(theObj[0].conf, function(e) {
                return e.name == inp
            });

            if (typeof clmnt[0] === 'undefined') {
                $(this).val("null");
            } else {
                $(this).val(clmnt[0].value);

                target_in_endpoint = $("#inEndpointsList").val();

                if (clmnt[0].value === "conn_route") {
                    routing_manager_reload(targetId, objectId, target_in_endpoint);

                }
            }
        })
    }

    $('#connection_manager').show(200);
}
}


function routing_manager_reload(targetId, connId, target_in_endpoint) {
    if (!target_in_endpoint || target_in_endpoint === "null" || target_in_endpoint === "undefined") {
        alertify.error("no target endpoint selected");
        $("#conn_mapping").val('blank');
        $("#inEndpointsList").focus();
    }
    else {
        //console.log("routing_manager_reload" + " " + connId);

        var instances = $.grep(graphConf, function(e) {
            return e.objectId == targetId
        })

        var instancesNum = 0;

        if (typeof instances[0] === 'undefined') {
            alertify.log("target instances not set");
            $("#conn_mapping").val('blank');
            //instancesNum = 1;
        } else {
            focusConnId = connId;
            for (i = 0; i < instances[0].conf.length; i++) {
                if (instances[0].conf[i].name == "instances") {
                    instancesNum = instances[0].conf[i].value
                }
            }

            var optionInstances = "<option value='blank' disabled selected></option>";
            for (y = 0; y < instancesNum; y++) {
                optionInstances += "<option value='" + y + "'>instance " + y + "</option>";
            }

            $("#route_mapping_instance").html(optionInstances);

            var lmnt = $.grep(graphConf, function(e) {
                return e.objectId == connId
            })

            if (typeof lmnt[0].instances !== 'undefined') {
                $("#route_mapping_instance").val(lmnt[0].instances[0].instance);

            }
            else {
                $("#route_mapping_keys").val('');
            }

            show_key_vals();

            $('#routing_manager').show();

        }

    }
}

function show_key_vals() {
    //console.log('objectId  ' + objectId + 'focusConnId   ' + focusConnId)
    $("#route_mapping_keys").val('');
    routing_instance = $("#route_mapping_instance").val();

    //console.log("load val " + focusTargetId_for_routing + " " + routing_instance)
    var lmnt = $.grep(graphConf, function(e) {
        return e.objectId == focusConnId
    })
    if (typeof lmnt[0].instances !== "undefined") {
        for (var f = 0; f < lmnt[0].instances.length; f++) {
            if (lmnt[0].instances[f].instance === routing_instance) {
                $("#route_mapping_keys").val(lmnt[0].instances[f].keys);
            }
        }
    }
}

function serializeFormToGraphConf(form, focusObjectId) {
    var srvConf = "";

    var lobj = $.grep(graphConf, function(e) {
        return e.objectId === focusObjectId;
    });

    srvConf = form.serializeArray();

    if (typeof lobj[0] !== 'undefined' && typeof lobj[0].instances !== 'undefined') {

        obj = {objectId: focusObjectId, conf: srvConf, instances: lobj[0].instances};

        graphConf = $.grep(graphConf, function(e) {
            return e.objectId != focusObjectId;
        });

        graphConf.push(obj);
    }
    else {

        graphConf = $.grep(graphConf, function(e) {
            return e.objectId != focusObjectId;
        });

        obj = {objectId: focusObjectId, conf: srvConf};
        graphConf.push(obj);
    }

}


var graph;
var paper;

function loadWars(){
        var wf = localStorage["WAR"];
        warfiles = JSON.parse(wf);
    }
    
    
$(document).ready(function() {
    
    fetchServicesList(loadWars);
    
    $('#service_manager').hide();
    $('#connection_manager').hide();
    //$('#routing_field').hide();
    $('#routing_manager').hide();


    graph = new joint.dia.Graph;
    link = new joint.dia.Link;

    paper = new joint.dia.Paper({
        el: $('#paper'),
//        width: 800,
        width: $('#paper').width(),
//        height: 600,
        height: $('#paper').width()*3/4,
        gridSize: 1,
        model: graph,
        elementView: ClickableView
    });

    var uml = joint.shapes.uml;

    // var servicesCounter = 0;
    // var services = {
    //     hold0: new uml.StartState({
    //         id: "transition-source",
    //         position: {x: 600, y: 20},
    //         size: {width: 30, height: 30}
    //     }),
    //     hold1: new uml.StartState({
    //         id: "transition-target",
    //         position: {x: 700, y: 20},
    //         size: {width: 30, height: 30}
    //     })
    // }
    var services = {
        hold0: new uml.StartState({
            id: "transition-source",
            position: {x: 0.75*$('#paper').width(), y: 0.033*$('#paper').height()},
            size: {width: 0.0375*$('#paper').width(), height: 0.0375*$('#paper').width()}
        }),
        hold1: new uml.StartState({
            id: "transition-target",
            position: {x: 0.875*$('#paper').width(), y: 0.033*$('#paper').height()},
            size: {width: 0.0375*$('#paper').width(), height: 0.0375*$('#paper').width()}
        })
    }

    graph.addCells(services);

    var topologyName = $('#topologyName').text();
    var url = "/OpenZUI/KeyValueServlet?action=topology&name=" + topologyName;

    $.getJSON(url, function(result) {
        if ("graph_object" in result["response"]) {
            //console.log(result);
            var graphcomplete = result["response"]["graph_object"];
            graph.fromJSON(graphcomplete.graph);
            //graph = graphcomplete.graph;
            graphConf = graphcomplete.graphConfiguration;
            graph.addCells(services);
        }
        else {
            graph.addCells(services);
        }

        var bbox = graph.getBBox(graph.getElements());
        // console.log("bbox.x = " + bbox.x);
        // console.log("bbox.y = " + bbox.y);
        // console.log("bbox.width = " + bbox.width);
        // console.log("bbox.height = " + bbox.height);

        if ((bbox.x + bbox.width > $('#paper').width()) || (bbox.y + bbox.height > $('#paper').height()))
        {
            console.log("Rescaling");
            var opt = {};
            opt.padding = 10;
            // opt.maxScaleX = 1;
            // opt.maxScaleY = 1;
            paper.scaleContentToFit(opt);
        }
    });


    var myAdjustVertices = _.partial(adjustVertices, graph);

    // adjust vertices when a cell is removed or its source/target was changed
    graph.on('add remove change:source change:target', myAdjustVertices);

    graph.on('remove', function(cell) {
        objectId = cell.id;

        graphConf = $.grep(graphConf, function(e) {
            return e.objectId != objectId;
        });

        if ($("#service_form").hasClass(objectId)) {
            $('#service_manager').hide();
        }
        else if ($("#connection_form").hasClass(objectId)) {
            $('#connection_manager').hide();
            //$('#routing_field').hide();
            $('#routing_manager').hide();


        }
    })

    graph.on('change:source change:target', function(cell) {

        $('#connection_manager').hide();
        //$('#routing_field').hide();
        $('#routing_manager').hide();
 
        if (("id" in cell.attributes.source) && ("id" in cell.attributes.target)) { 
            objectId = cell.id;

            graphConf = $.grep(graphConf, function(e) {
                return e.objectId != objectId;
            });
            //empty the element holder
            theObj = [];

            var sourceId = cell.attributes.source.id;
            var targetId = cell.attributes.target.id;

            if (!("id" in cell.attributes.source) || !("id" in cell.attributes.target)) {

                console.log("...");
                cell.remove();
                return;

            }
            else if (((sourceId == "transition-source") && (targetId == "transition-target")) || ((targetId == "transition-source") && (sourceId == "transition-target"))) {
                return;
            }
            else if (sourceId == targetId) {

                alertify.error("no self links allowed")
                cell.remove();

            }
            else if (((sourceId == "transition-source") || (sourceId == "transition-target")) && (targetId !== "transition-target")) {
                //console.log("target only" + targetId)

                var t = localStorage[targetId];
                var tf = JSON.parse(t);

                if (tf.hasOwnProperty('in_ep') && tf.in_ep.length > 0) {

                    //connection_manager_reload(sourceId, targetId);

                }
                else {
                    alertify.error("no target endpoints defined")
                    cell.remove();
                }
                //console.log(tf.in_ep);

            } else if (((targetId == "transition-target") || (targetId == "transition-source")) && (sourceId !== "transition-source")) {
                //console.log("source only" + sourceId)

                var s = localStorage[sourceId];
                var sf = JSON.parse(s);

                if (sf.hasOwnProperty('out_ep') && sf.out_ep.length > 0) {

                    //connection_manager_reload(sourceId, targetId);
                }

                else {
                    alertify.error("no source endpoints defined")
                    cell.remove();
                }

                //console.log(sf.out_ep);

            } else {
                //console.log("both" + targetId + sourceId )
                var t = localStorage[targetId];
                var tf = JSON.parse(t);

                var s = localStorage[sourceId];
                var sf = JSON.parse(s);

                if (tf.hasOwnProperty('in_ep') && sf.hasOwnProperty('out_ep')) {

                    if (tf.in_ep.length <= 0) {

                        alertify.error("no target endpoints defined")
                        cell.remove();

                    }
                    else if (sf.out_ep.length <= 0) {

                        alertify.error("no source endpoints defined")
                        cell.remove();

                    }
                    else {

                        connection_manager_reload(sourceId, targetId, theObj, objectId);
                    }
                }
                else {

                    cell.remove();
                }
            }
        }
    });

//    paper.on('cell:pointerup', function(cellView){
//       var objectType = cellView.model.attributes.type;
//       if (objectType === 'uml.Transition'){
//           var sourceId = cellView.model.attributes.source.id;
//           var targetId = cellView.model.attributes.target.id;
//           console.log(sourceId);
//           console.log(targetId);
//           if (typeof sourceId ==="undefined" ||typeof targetId ==="undefined"){
//               cellView.remove();
//           }
//       }
//    });
    
    paper.on('cell:pointerup', myAdjustVertices);

    paper.on('cell:pointerdown', function(cellView, evt, x, y) {

        objectId = cellView.model.id;
        focusObjectId = "";
        var objectType = cellView.model.attributes.type
        //console.log('cell view ' + objectId + ' a ' +  objectType + ' was clicked'); 

        theObj = $.grep(graphConf, function(e) {
            return e.objectId == objectId;
        });


        switch (objectType) {
            case 'uml.State':

                if ($("#service_form").hasClass(objectId) && $("#service_manager").is(":visible") == true) {
                    //if service_manager form for this service is open do nothing... 
                }
                else {
                    //else close all forms and clean their fields to reload them/
                    $('#service_manager').hide();
                    $('#connection_manager').hide();
                    //$('#routing_field').hide();
                    $('#routing_manager').hide();
                    $('#instances').val('1');
                    $('#tpc').val('0');
                    $(".addToServiceForm").remove();
                    $("#service_form").removeClass();
                    $('#instances').attr('readonly', false);
                    $('#tpc').attr('readonly', false);

                    for (var i = 0; i < warfiles.length; i++) {
                        if (warfiles[i].component_id == objectId) {

                            //get requires flieds from WAR
                            if (warfiles[i].hasOwnProperty('requires') && warfiles[i]['requires'].length > 0) {
                                var reqs = warfiles[i].requires;
                                for (var y = 0; y < reqs.length; y++) {
                                    //console.log(reqs[y]);
                                    if (reqs[y] != '')
                                        $("#service_form").prepend('<div class="addToServiceForm"><label for="' + reqs[y] + '">' + reqs[y] + '</label><input type="text" class="form-control" id="' + reqs[y] + '" name="' + reqs[y] + '"></div>');
                                }
                            }

                            $("#service_form").prepend('<div class="addToServiceForm"><label>Service <i>' + warfiles[i].component_id + '</i> configuration</label><hr></div>');
                            $("#service_form").addClass(objectId);

                            //if service is already configured load its values

                            if (theObj.length > 0) {
                                //console.log("in");
                                var inputs = $('#service_form :input');
                                inputs.each(function() {
                                    var inp = this.name;
                                    var lmnt = $.grep(theObj[0].conf, function(e) {
                                        return e.name == inp
                                    });
                                    if (typeof lmnt[0] === "undefined") {
                                        $(this).val("null");
                                    } else {
                                        $(this).val(lmnt[0].value);
                                    }
                                });
                            }
                                                       
                          if (warfiles[i].hasOwnProperty('requires') === false || warfiles[i]['requires'].length === 0) {
                              serializeFormToGraphConf($("#service_form"), objectId);
                          }   
                        }
                    }
                    var localObj = JSON.parse(localStorage[objectId]);

                    if (localObj.hasOwnProperty('type') && localObj.type==="broker"){
                        $('#instances').value=1;
                        $('#instances').attr('readonly', true);
                        $('#tpc').value=0;
                        $('#tpc').attr('readonly', true);
                    }
                    $('#service_manager').show(200);

                }
                break;

            case 'uml.Transition':
                var sourceId = cellView.model.attributes.source.id;
                var targetId = cellView.model.attributes.target.id;

                if ((sourceId == "transition-source") || (targetId == "transition-target") || (targetId == "transition-source") || (sourceId == "transition-target")) {
                    $('#service_manager').hide();
                }
                else if ($("#connection_form").hasClass(objectId) && $("#connection_manager").is(":visible") == true) {
                    //if connection_manager form for this service is open do nothing... 
                }
                else {

                    connection_manager_reload(sourceId, targetId, theObj, objectId);
                }
                break;
        }
    }
    );

    //Add Service
    $("#openzooServiceSelectBtn").on('click', function() {

        var serviceName = $('#openzooServiceSelect').find(":selected").text();
        var serviceType = JSON.parse(localStorage[serviceName]).type;
        var serviceAttrs;
        if (serviceType === "broker") serviceAttrs = {'.uml-state-body': { fill: 'rgba(236, 151, 31, 0.1)', stroke: 'rgba(236, 151, 31, 0.5)', 'stroke-width': 1.5 }, '.uml-state-separator': { stroke: 'rgba(236, 151, 31, 0.4)' }};
        else serviceAttrs = {'.uml-state-body': { fill: 'rgba(101, 176, 69, 0.1)', stroke: 'rgba(101, 176, 69, 0.5)', 'stroke-width': 1.5 }, '.uml-state-separator': { stroke: 'rgba(101, 176, 69, 0.4)' }};

        // servicesCounter = servicesCounter +1;
        var srv = new uml.State({
            id: serviceName,
            position: {x: 20, y: 20},
            size: {width: 160, height: 60},
            name: serviceName,
            events: ["type: " + serviceType],
            attrs: serviceAttrs
            //attrs: {'.uml-state-body': { fill: 'rgba(48, 208, 198, 0.1)', stroke: 'rgba(48, 208, 198, 0.5)', 'stroke-width': 1.5 }, '.uml-state-separator': { stroke: 'rgba(48, 208, 198, 0.4)' }}
            // uml-state-body.fill is the fill color of the box
            // uml-state-body.stroke is the color of the box shape
            // uml-state-separator.stroke is the color of the separator between header and main body
        });
        services[serviceName] = srv;
        graph.addCell(srv);
    });


    $("#addLinkBtn").on('click', function() {

        var transitions = [
            new uml.Transition({source: {id: services.hold0.id}, target: {id: services.hold1.id}, attrs: {'.connection': linkAttrs}})
        ];

        graph.addCells(transitions);

    });


    $("#service_form, #connection_form, #routing_form").focusin(function(e) {
        focusObjectId = objectId;

//        if ($(this).attr("id") === "service_form" && $(this).children(".addToServiceForm").length === 1) {
//            serializeFormToGraphConf($(this), focusObjectId);
//        }
        
    });

    $("#service_form, #connection_form, #routing_form").focusout(function(e) {
        focusObjectId = "";
    });

    //$("#service_form, #connection_form").focusout(function() {
    $("#service_form, #connection_form").bind('input propertychange change paste keyup', function() {

        //e.preventDefault();
        //console.log('form bind' + " " + objectId)
        if (objectId === focusObjectId) {
            serializeFormToGraphConf($(this), focusObjectId);
           
        }
    });


    $("#conn_mapping").change(function() {
        if ($("#conn_mapping").val() == "conn_route") {
            if (!target_in_endpoint || target_in_endpoint === "null" || target_in_endpoint === "undefined") {
                alertify.error("no target endpoint selected");
                $("#conn_mapping").val('blank');
                $("#inEndpointsList").focus();
            }
            else {
                objectId = $('#connection_form').attr('class');
                routing_manager_reload(targetId_for_routing, objectId, target_in_endpoint)
            }
        }
        else {
            $('#routing_keys').val('')
            //$('#routing_field').hide();
            $('#routing_manager').hide();
        }

    })


    $("#inEndpointsList").change(function() {
        if ($("#routing_manager").is(":visible") == true) {

            /*If while configuring routing connection parameters target ep is changed, instance and routing keys parameters will not be saved properly*/
            alertify.error("Target endpoint cannot be changed on routing configuration. Change connection type to restart routing process.");
            $(this).val(target_in_endpoint);

        } else {
            //GET INENDPOINT VALUE
            target_in_endpoint = $(this).val();
        }
    })


    $("#route_mapping_instance").change(function() {
        show_key_vals();
    });


    //$("#route_mapping_keys").focusout(function() {
    $("#route_mapping_keys").bind('input propertychange change paste keyup', function() {

        var keys_associations = {};
        var kvlmnt_instances_tmp = [];

        keys_associations = $.grep(graphConf, function(e) {
            return e.objectId === focusConnId
        });

        //look for service instances
        if (typeof keys_associations[0] !== 'undefined') {
            var kvlmnt = keys_associations[0].instances
        }

        if (typeof kvlmnt !== 'undefined') {
            //delete old values for instance
            var kvlmnt_instances = $.grep(kvlmnt, function(e) {
                return e.instance !== routing_instance
            });

            if (typeof kvlmnt_instances[0] !== "undefined") {
                kvlmnt_instances_tmp = kvlmnt_instances;
            }
        }

        var keys = $("#route_mapping_keys").val()

        //write new value for instance ---> all instances here!
        kvlmnt_instances_tmp.push({instance: routing_instance, endpoint: target_in_endpoint, keys: keys});

        for (var g = 0; g < graphConf.length; g++) {
            if (graphConf[g].objectId === focusConnId) {
                graphConf[g].instances = kvlmnt_instances_tmp
            }
        }
    });


    $("#submitTopoBtn").on('click', function() {
        var tCells =graph.attributes.cells.models;
        var tFlag=true;
        for (var c in tCells){
            if (tCells[c].attributes.type ==="uml.Transition")
                if (!("id" in tCells[c].attributes.source) || !("id" in tCells[c].attributes.target)){
                    tFlag=false;
                }
        }
        if (graphConf.length != (graph.attributes.cells.models.length - 2)) {
            notDirty = false;
            mArr = [];
            gArr = [];

            alertify.error("Topology services and connections not fully configured")

            $(graph.attributes.cells.models).each(function() {
                if (this.id !== "transition-source" && this.id !== "transition-target") {
                    mArr.push(this.id);
                }
            });

            $(graphConf).each(function() {
                gArr.push(this.objectId);
            })

            //differences array
            var diff = $(mArr).not(gArr).get();

            alertify.error("Check: " + diff)
        }
        if (tFlag===false){
            alertify.error("There are stray links, remove them and submit")
        }
        else {
            notDirty = true;

            var graphComplete = {graph: graph, graphConfiguration: graphConf, fully_configured: notDirty};
            localStorage.setItem("graphComplete", JSON.stringify(graphComplete));
            $("#topo-graph").val(JSON.stringify(graphComplete));
            $("#topoSubmitForm").submit();
        }



    });

    $("#cancelTopoBtn").on('click', function() {

        $("#topo-graph").val('');
        $("#topoSubmitForm").submit();
    });

});
