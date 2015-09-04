
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

 function detailsServer(name, address, port, user, passwd, status){
            
    $("#srv-upd-name").val(name);
    $("#srv-upd-ip").val(address);
    $("#tmc-upd-port").val(port);
    $("#tmc-upd-user").val(user);
    $("#tmc-upd-pass").val(passwd);
    $("#tmc-upd-status").val(status);
    $('#detailsModal').modal('show');
 };

 function detailsWarfile(filename, folder, version, status){
            
    $("#war-upd-filename").val(filename);
    $("#war-upd-folder").val(folder);
    $("#war-upd-version").val(version);
    $("#war-upd-status").val(status);
    $('#detailsModal').modal('show');
 };