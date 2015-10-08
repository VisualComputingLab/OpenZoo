$(document).ready(function(){

    detailsTopologyConfiguration($('#topologyDropdown option:selected').val());
    
    $('#topologyDropdown').on('change', function(){
        var selected = $('#topologyDropdown option:selected').val();
        detailsTopologyConfiguration(selected);
    });
});