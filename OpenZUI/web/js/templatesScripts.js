$(document).ready(function(){
   
    $('.tmpl-workerType').on('click',function(){
        $('#tmpl-hasInput').prop("disabled", false);
        $('#tmpl-numOutputs').attr("min", 0);
        if ($(this).val()==="broker"){
            $('#tmpl-hasInput').prop("checked", true).prop('disabled', true);
            if($('#tmpl-numOutputs').val()<1){
                $('#tmpl-numOutputs').val(1);
            }
            $('#tmpl-numOutputs').attr("min", 1);
        }
    })
    
    $('#template_create').submit(function(e){
       if ($('input[name=tmpl-workerType]:checked').val()==="operator" && $('#tmpl-numOutputs').val()<1 && !$('#tmpl-hasInput').is(":checked")){
           e.preventDefault();
           alertify.error("Operator service must have at least one endpoint (input or output)");
           $('input[name=tmpl-workerType]:checked').focus();
       } 
       else{
           alertify.success('Creating...')
           return;
       }
    })
});

