
$(document).ready(function(){/* jQuery toggle layout */
    $('#btnToggle').click(function(){
        if ($(this).hasClass('on')) {
            $('#main .col-md-6').addClass('col-md-4').removeClass('col-md-6');
            $(this).removeClass('on');
        }
        else {
            $('#main .col-md-4').addClass('col-md-6').removeClass('col-md-4');
            $(this).addClass('on');
        }
    });

    for(var i = 0; i < logcontainer.length; i++){
        var log = logcontainer[i];
        if (log.indexOf("INFO:") === 0)
            alertify.success(log.substring(log.indexOf(":") + 1));
        else if (log.indexOf("ERROR:") === 0)
            alertify.error(log.substring(log.indexOf(":") + 1));
        else alertify.log(log.substring(log.indexOf(":") + 1));
    }
});

 function detailsServer(name){
            
    var URL = "/OpenZUI/KeyValueServlet?action=server&name="+name;
    $.getJSON(URL, function(result){
        $("#srv-upd-name").val(name);
        $("#srv-upd-ip").val(result.response.address);
        $("#tmc-upd-port").val(result.response.port);
        $("#tmc-upd-user").val(result.response.user);
        $("#tmc-upd-pass").val(result.response.passwd);
        $("#tmc-upd-status").val(result.response.status);

        $("#stats-cpu").val("");
        $("#stats-mem").val("");
        $("#stats-disc").val("");

        var StatsURL = "http://" + result.response.address + ":" + result.response.port + "/ServerResources/resources/manage";
        $.getJSON(StatsURL, function(sresult){
            $("#stats-cpu").val("" + Math.floor(sresult.cpu.systemCpuLoad * 100.0) + " %");
            $("#stats-mem").val("" + Math.floor(sresult.mem.physicalFree / 1024 / 1024) + " MB free / " + Math.floor(sresult.mem.physicalTotal / 1024 / 1024) + " MB total");
            $("#stats-disc").val("" + Math.floor(sresult.space.free / 1024 / 1024) + " MB free / " + Math.floor(sresult.space.total / 1024 / 1024) + " MB total");
        });

        $('#detailsModal').modal('show');
    });
 };

 function detailsWarfile(comp_id){
            
    var URL = "/OpenZUI/KeyValueServlet?action=war&name="+comp_id;
    $.getJSON(URL, function(result){
        $("#war-upd-compoid").val(comp_id);
        $("#war-upd-name").val(result.response.name);
        $("#war-upd-servpath").val(result.response.service_path);
        $("#war-upd-descr").val(result.response.description);
        $("#war-upd-filename").val(result.response.filename);
        $("#war-upd-version").val(result.response.version);
        $("#war-upd-status").val(result.response.status);
        $('#detailsModal').modal('show');
    });
 };

 function detailsTopology(name){

    var URL = "/OpenZUI/KeyValueServlet?action=topology&name="+name;
    $.getJSON(URL, function(result){
        $("#topo-upd-name").val(name);
        $("#topo-upd-descr").val(result.response.description);
        $("#topo-upd-rhost").val(result.response.rabbit.host);
        $("#topo-upd-rport").val(result.response.rabbit.port);
        $("#topo-upd-ruser").val(result.response.rabbit.user);
        $("#topo-upd-rpass").val(result.response.rabbit.passwd);
        $("#topo-upd-mhost").val(result.response.mongo.host);
        $("#topo-upd-mport").val(result.response.mongo.port);
        $("#topo-upd-muser").val(result.response.mongo.user);
        $("#topo-upd-mpass").val(result.response.mongo.passwd);

        switch (result.response.status)
        {
            case 'CREATED':
                $("#deleteBtn").show();
                $("#deployBtn").hide();
                $("#undeployBtn").hide();
                $("#startBtn").hide();
                $("#stopBtn").hide();
                break;
            case 'DESIGNED':
                $("#deleteBtn").show();
                $("#deployBtn").show();
                $("#undeployBtn").hide();
                $("#startBtn").hide();
                $("#stopBtn").hide();
                break;
            case 'SEMIDEPLOYED':
                $("#deleteBtn").hide();
                $("#deployBtn").show();
                $("#undeployBtn").show();
                $("#startBtn").hide();
                $("#stopBtn").hide();
                break;
            case 'DEPLOYED':
                $("#deleteBtn").hide();
                $("#deployBtn").hide();
                $("#undeployBtn").show();
                $("#startBtn").show();
                $("#stopBtn").hide();
                break;
            case 'SEMISTARTED':
                $("#deleteBtn").hide();
                $("#deployBtn").hide();
                $("#undeployBtn").hide();
                $("#startBtn").show();
                $("#stopBtn").show();
                break;
            case 'STARTED':
                $("#deleteBtn").hide();
                $("#deployBtn").hide();
                $("#undeployBtn").hide();
                $("#startBtn").hide();
                $("#stopBtn").show();
                break;
        }

        $('#detailsModal').modal('show');
    });
 };

 function detailsService(name){
            
    
    $('#detailsModal').modal('show');
 };

 $('#deleteBtn').on('click',function(){

    $("#post-action").val('delete');
 });

 $('#updateBtn').on('click',function(){

    $("#post-action").val('update');
 });

 $('#startBtn').on('click',function(){

    $("#post-action").val('start');
 });

 $('#stopBtn').on('click',function(){

    $("#post-action").val('stop');
 });

 $('#deployBtn').on('click',function(){

    //$("#post-action").val('deploy');
    $("#post-action").val('create_config');
 });

 $('#undeployBtn').on('click',function(){

    $("#post-action").val('undeploy');
 });


function fetchServicesList(callback){
    var url = "/OpenZUI/KeyValueServlet?action=war";

  $.getJSON(url,function(result){
        var listItems= "";
        //var services =[];
        localStorage["WAR"] = JSON.stringify(result['response']);
        
        $.each(result['response'], function(key, val){
            
            var svc={};
            var svc_in=[];
            var svc_out=[];

            if (val.workers.length>0){
                for (var i = 0; i < val.workers.length; i++){
                    for(var x = 0; x < val.workers[i].endpoints.length; x++){
                        if (val.workers[i].endpoints[x].type == 'in'){
                            svc_in.push(val.workers[i].worker_id + ":" + val.workers[i].endpoints[x].endpoint_id)   
                        }
                        else if (val.workers[i].endpoints[x].type == 'out'){
                            svc_out.push(val.workers[i].worker_id + ":" + val.workers[i].endpoints[x].endpoint_id)   
                        }     
                    }
                }

                svc={name:val.component_id, in_ep:svc_in, out_ep:svc_out, type:val.type}                    
                //svc={ in_ep:svc_in, out_ep:svc_out}    
                listItems+= "<option value='" + val.component_id + "'>" + val.component_id + "</option>";
                //console.log(svc);
                localStorage[val.component_id] = JSON.stringify(svc);
                //services.push(svc);
            }
        });
        
        $("#openzooServiceSelect").html(listItems);   
    }).complete(function(){
        callback();
    });

};

SVGElement.prototype.getTransformToElement = SVGElement.prototype.getTransformToElement || function(toElement) {
return toElement.getScreenCTM().inverse().multiply(this.getScreenCTM());
};