
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



function fetchdataAndShowModal(rid){


 var url = "fetchDetails.php";
     url = url+"?rid="+rid;
    
  $.getJSON(url,function(result){
            
      $("#detailsModalBody").empty();
      $("#detailsModalBody").append("<table id='jsondata' class='table table-striped'>");
        
        $("#jsondata").append("<tr><td>key</td><td>Val</td></tr>");

        $.each(result, function(key, val){
                $("#jsondata").append("<tr><td>"+key+"</td><td>"+val+"</td></tr>");
		  });

    $("#detailsModalBody").append("</table>");
 	$('#detailsModal').modal('show');
    });
 };