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
;

var graph;
var paper;


$(document).ready(function() {

    //graphConf=[];
    fetchServicesList();

    $('#service_manager').hide();
    $('#connection_manager').hide();
    $('#routing_field').hide();


    graph = new joint.dia.Graph;

    var graphConf = [];

    paper = new joint.dia.Paper({
        el: $('#paper'),
        width: 800,
        height: 600,
        gridSize: 1,
        model: graph,
        elementView: ClickableView
    });

    var uml = joint.shapes.uml;

    // var servicesCounter = 0;
    var services = {
        hold0: new uml.StartState({
            id: "transition-source",
            position: {x: 600, y: 20},
            size: {width: 30, height: 30}
        }),
        hold1: new uml.StartState({
            id: "transition-target",
            position: {x: 700, y: 20},
            size: {width: 30, height: 30}
        })
    };

    var linkAttrs = {
        'fill': 'none',
        'stroke-linejoin': 'round',
        'stroke-width': '2',
        'stroke': '#4b4a67'
    };


    var topologyName = $('#topologyName').text();
    var url = "/OpenZUI/KeyValueServlet?action=topology&name=" + topologyName;

    $.getJSON(url, function(result) {
        if ("graph_object" in result["response"]) {
            console.log(result);
            var graphcomplete = result["response"]["graph_object"];
            graph.fromJSON(graphcomplete.graph);
            //graph = graphcomplete.graph;
            graphConf = graphcomplete.graphConfiguration;
            graph.addCells(services);
        }
        else {
            graph.addCells(services);
        }
    });



    var lastAction = false;

    var myAdjustVertices = _.partial(adjustVertices, graph);

    // adjust vertices when a cell is removed or its source/target was changed
    graph.on('add remove change:source change:target', myAdjustVertices);

    graph.on('remove', function(cell) {
        objectId = cell.id;

        if ($("#service_form").hasClass(objectId)) {
            $('#service_manager').hide();
        }
        else if ($("#connection_form").hasClass(objectId)) {
            $('#connection_manager').hide();
        }
    });


    graph.on('change:source change:target', function(cell) {

        // console.log(cell);
        if (("id" in cell.attributes.source) && ("id" in cell.attributes.target)) {
            //console.log(cell);
            objectId = cell.id;

            var sourceId = cell.attributes.source.id;
            var targetId = cell.attributes.target.id;

            if (sourceId == "undefined" || targetId == "undefined") {
                console.log("...");
                return;
            }

            if ((sourceId == "transition-source") && (targetId == "transition-target")) {
                return;
            }
            else if (sourceId == targetId) {
                alert("no self links allowed")

                cell.remove();
            }
            else if ((sourceId == "transition-source") && (targetId !== "transition-target") /*&& targetEndpoints.length<0*/) {
                //console.log("target only" + targetId)

                var t = localStorage[targetId];
                var tf = JSON.parse(t);
                if (tf.hasOwnProperty('in_ep')) {
                    if (tf.in_ep.length <= 0) {
                        alert("no target endpoints defined");
                        cell.remove();
                    }
                }
                else {
                    cell.remove();
                }

                //console.log(tf.in_ep);

            } else if ((targetId == "transition-target") && (sourceId !== "transition-source")/*&& sourceEndpoints.length<0*/) {

                //console.log("source only" + sourceId)

                var s = localStorage[sourceId];
                var sf = JSON.parse(s);

                if (sf.hasOwnProperty('out_ep')) {
                    if (sf.out_ep.length <= 0) {
                        alert("no target endpoints defined");

                        cell.remove();
                    }
                }
                else {
                    cell.remove();
                }

                //console.log(sf.out_ep);

            } else {

                //console.log("both" + targetId + sourceId )
                var t = localStorage[targetId];
                var tf = JSON.parse(t);

                if (tf.hasOwnProperty('in_ep')) {
                    if (tf.in_ep.length <= 0) {
                        alert("no target endpoints defined");
                        cell.remove();
                    }
                }
                else {
                    cell.remove();
                }

                var s = localStorage[sourceId];
                var sf = JSON.parse(s);

                if (sf.hasOwnProperty('out_ep')) {
                    if (sf.out_ep.length <= 0) {
                        alert("no target endpoints defined");
                        cell.remove();
                    }
                }
                else {
                    cell.remove();
                }
            }
        }
    });


    paper.on('cell:pointerup', myAdjustVertices);


    //Track all clicks
    var objectId = "";
    var insertedElement = [];

    var wf = localStorage["WAR"];
    var warfiles = JSON.parse(wf);

    paper.on('cell:pointerdown', function(cellView, evt, x, y) {

        objectId = cellView.model.id;
        var objectType = cellView.model.attributes.type
        //console.log('cell view ' + objectId + ' a ' +  objectType + ' was clicked'); 
        //console.log(cellView.model);
        insertedElement = $.grep(graphConf, function(e) {
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
                    $('#instances').val('1');
                    $('#wpc').val('0');

                    for (var i = 0; i < warfiles.length; i++) {
                        if (warfiles[i].component_id == objectId) {
                            // //get requires flieds from WAR
                            // var reqs =  warfiles[i].requires;
                            // $(".addToServiceForm").remove();
                            // $("#service_form").removeClass();

                            // for(var y = 0; y < reqs.length; y++){
                            //     //console.log(reqs[y]);
                            //     $("#service_form").prepend('<div class="addToServiceForm"><label for="'+reqs[y]+'">'+reqs[y]+'</label><input type="text" class="form-control" id="'+reqs[y]+'" name="'+reqs[y]+'"></div>');
                            // }

                            $(".addToServiceForm").remove();
                            $("#service_form").removeClass();
                            //get requires flieds from WAR
                            if (warfiles[i].hasOwnProperty('requires')) {
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
                            if (insertedElement.length > 0) {
                                var $inputs = $('#service_form :input');
                                $inputs.each(function() {
                                    if (this.type !== "submit") {
                                        var inp = this.name;
                                        var lmnt = $.grep(insertedElement[0].conf, function(e) {
                                            return e.name == inp;
                                        });
                                        this.value = lmnt[0].value;
                                    }
                                });
                            }
                        }
                    }

                    $('#service_manager').show(200);

                }
                break;

            case 'uml.Transition':
                var sourceId = cellView.model.attributes.source.id;
                var targetId = cellView.model.attributes.target.id;

                if ((sourceId == "transition-source") || (targetId == "transition-target")) {
                    $('#service_manager').hide();
                }
                else if ($("#connection_form").hasClass(objectId) && $("#connection_manager").is(":visible") == true) {
                    //if connection_manager form for this service is open do nothing... 
                }
                else {
                    //else close all forms and clean their fields to reload them
                    $('#service_manager').hide();
                    $('#connection_manager').hide();
                    $(".addToConnectionForm").remove();
                    $("#connection_form").removeClass();
                    $("#inEndpointsList").empty();
                    $("#outEndpointsList").empty();

                    $('#conn_mapping').val('conn_available').change();
                    $('#routing_field').hide();
                    $('#routing').val('');



                    $("#connection_form").prepend('<div class="addToConnectionForm"><label><strong>' + sourceId + '</strong><span> --> </span><strong>' + targetId + '</strong></label><hr></div>');
                    $("#connection_form").addClass(objectId);


                    var s = localStorage[sourceId];
                    var sf = JSON.parse(s);
                    var sourceEndpoints = sf.out_ep;

                    var t = localStorage[targetId];
                    var tf = JSON.parse(t);

                    var targetEndpoints = tf.in_ep;

                    var outEndpoints = "";
                    sourceEndpoints.map(function(item) {
                        outEndpoints += "<option value='" + item + "'>" + item.substring(item.lastIndexOf(".") + 1) + "</option>";
                    });
                    $("#outEndpointsList").html(outEndpoints);


                    var inEndpoints = "";
                    targetEndpoints.map(function(item) {
                        inEndpoints += "<option value='" + item + "'>" + item.substring(item.lastIndexOf(".") + 1) + "</option>";
                    });

                    $("#inEndpointsList").html(inEndpoints);

                    //if service is already configured load its values
                    if (insertedElement.length > 0) {
                        var $selects = $('#connection_form select');
                        $selects.each(function() {
                            var inp = this.name
                            var lmnt = $.grep(insertedElement[0].conf, function(e) {
                                return e.name == inp;
                            });

                            $(this).val(lmnt[0].value);

                            if (lmnt[0].value == "conn_route") {
                                var lmnt = $.grep(insertedElement[0].conf, function(e) {
                                    return e.name == "routing";
                                });
                                $('#routing').val(lmnt[0].value);
                                $('#routing_field').show();
                            }
                        });
                    }

                    $('#connection_manager').show(200);
                }
                break;
        }
    }
    );

    //Add Service
    $("#openzooServiceSelectBtn").on('click', function() {

        var serviceName = $('#openzooServiceSelect').find(":selected").text();
        // servicesCounter = servicesCounter +1;
        var srv = new uml.State({
            id: serviceName,
            position: {x: 20, y: 20},
            size: {width: 160, height: 60},
            name: serviceName,
            events: ["status: --"]
        });
        services[serviceName] = srv;
        graph.addCell(srv);

        //console.log("openzooServiceSelectBtn:click " );
        //console.log(services);
    });

    //Add Link
    $("#addLinkBtn").on('click', function() {

        var transitons = [
            new uml.Transition({source: {id: services.hold0.id}, target: {id: services.hold1.id}, attrs: {'.connection': linkAttrs}})
        ];

        graph.addCells(transitons);

    });

    $("#service_form, #connection_form").focusout(function(e) {
        //in order the element has been previously saced filter by the opposite predicate:
        graphConf = $.grep(graphConf, function(e) {
            return e.objectId != objectId;
        });

        var srvConf = $(this).serializeArray();
        graphConf.push({objectId: objectId, conf: srvConf});
    });


    $("#conn_mapping").change(function() {
        if ($("#conn_mapping").val() == "conn_route") {
            $('#routing_field').show(200);
        }
        else {
            $('#routing').val('');
            $('#routing_field').hide();
        }
    });


    $("#submitTopoBtn").on('click', function() {

        //var submitGraph = graph.toJSON();

        var graphComplete = {graph: graph, graphConfiguration: graphConf};
        //console.log(graphComplete);
        localStorage.setItem("graphComplete", JSON.stringify(graphComplete));
        $("#topo-graph").val(JSON.stringify(graphComplete));
        $("#topoSubmitForm").submit();
    });

    $("#cancelTopoBtn").on('click', function() {

        $("#topo-graph").val('');
        $("#topoSubmitForm").submit();
    });
});
