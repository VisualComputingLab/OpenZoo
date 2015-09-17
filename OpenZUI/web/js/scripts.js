
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

});



function fetchdataAndShowModalOld(name, address, port, user, passwd, status){
            
    $("#detailsModalBody").empty();
    $("#detailsModalBody").append("<table id='jsondata' class='table table-striped'>");
    $("#jsondata").append("<tr><th>key</th><th>Val</th></tr>");
    $("#jsondata").append("<tr><td>Name</td><td>"+name+"</td></tr>");
    $("#jsondata").append("<tr><td>Address</td><td>"+address+"</td></tr>");
    $("#jsondata").append("<tr><td>Port</td><td>"+port+"</td></tr>");
    $("#jsondata").append("<tr><td>User</td><td>"+user+"</td></tr>");
    $("#jsondata").append("<tr><td>Passwd</td><td>"+passwd+"</td></tr>");
    $("#jsondata").append("<tr><td>Status</td><td>"+status+"</td></tr>");

    $("#detailsModalBody").append("</table>");
 	  $('#detailsModal').modal('show');
 };

 function detailsServer(name){
            
    var URL = "/OpenZUI/KeyValueServlet?action=server&name="+name;
    $.getJSON(URL, function(result){
        $("#srv-upd-name").val(name);
        $("#srv-upd-ip").val(result.response.address);
        $("#tmc-upd-port").val(result.response.port);
        $("#tmc-upd-user").val(result.response.user);
        $("#tmc-upd-pass").val(result.response.passwd);
        $("#tmc-upd-status").val(result.response.status);

        var StatsURL = "http://" + result.response.address + ":" + result.response.port + "/ServerStatistics/resources/stats";
        $.getJSON(StatsURL, function(sresult){
            $("#stats-cpu").val("" + Math.floor(sresult.cpu.systemCpuLoad * 100.0) + " %");
            $("#stats-mem").val("" + Math.floor(sresult.mem.physicalFree / 1024 / 1024) + " MB free / " + Math.floor(sresult.mem.physicalTotal / 1024 / 1024) + " MB total");
            $("#stats-disc").val("" + Math.floor(sresult.space.free / 1024 / 1024) + " MB free / " + Math.floor(sresult.space.total / 1024 / 1024) + " MB total");
        });

        $('#detailsModal').modal('show');
    });
 };

 function detailsWarfile(filename){
            
    var URL = "/OpenZUI/KeyValueServlet?action=war&name="+filename;
    $.getJSON(URL, function(result){
        $("#war-upd-compoid").val(result.response.component_id);
        $("#war-upd-name").val(result.response.name);
        $("#war-upd-servpath").val(result.response.service_path);
        $("#war-upd-descr").val(result.response.description);
        $("#war-upd-filename").val(filename);
        $("#war-upd-folder").val(result.response.folder);
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


function fetchServicesList(){

    var url = "/OpenZUI/KeyValueServlet?action=war";

    $.getJSON(url,function(result){
      
        //console.log(result);
        var listItems= "";
        var services =[];
        $.each(result['response'], function(key, val){
            var svc={};
            var svc_in=[];
            var svc_out=[];

            if (val.workers.length>0){
                for (var i = 0; i < val.workers.length; i++){
                    for(var x = 0; x < val.workers[i].endpoints.length; x++){
                        if (val.workers[i].endpoints[x].type == 'in'){
                            svc_in.push(val.workers[i].endpoints[x].endpoint_id)   
                        }
                        else if (val.workers[i].endpoints[x].type == 'out'){
                            svc_out.push(val.workers[i].endpoints[x].endpoint_id)   
                        }     
                    }
                }

                svc={name:val.filename, in_ep:svc_in, out_ep:svc_out}                    
                //svc={ in_ep:svc_in, out_ep:svc_out}    
                listItems+= "<option value='" + val.filename + "'>" + val.filename + "</option>";
                //console.log(svc);
                localStorage[val.filename] = JSON.stringify(svc);
                //services.push(svc);
            }
        });

        localStorage["WAR"] = JSON.stringify(result['response']);
        $("#openzooServiceSelect").html(listItems);      
    });
};

