$(document).ready(function(){

    $('.serverConf').each(function() {
        var list = document.getElementById(this.id);
        Sortable.create(list, { group: "servers", sort: true, 
            onAdd: function (evt) {
                var source = evt.item.innerHTML;
                //var comp_id = source.substring(0, source.indexOf(":"));
                var comp_id = source.substring(source.indexOf("</span>") + 7);
                var tList = evt.to.children;
                for (var i = 0; i < tList.length; i++)
                {
                    var target = tList[i].innerHTML;
                    if (source === target) continue;
                    if (target.indexOf(comp_id) >= 0 )
                    {
                        console.log(comp_id + " is already there");
                        evt.to.removeChild(evt.item);
                        evt.from.appendChild(evt.item);
                        break;
                    }
                }
            }
        });
    });


    $("#cancelTopoBtn").on('click', function() {

        $("#topo-config").val('');
        $("#topoSubmitForm").submit();
    });

    $("#submitTopoBtn").on('click', function() {

        var config = [];

        $('#submitTopoBtn').toggleClass('active');

        $('.serverConf').each(function() {
            var server_id = this.id;
            var list = document.getElementById(server_id);
            for (var i = 0; i < list.children.length; i++)
            {
                var txt = list.children[i].innerHTML;
                // var comp_id = txt.substring(0, txt.indexOf(":"));
                var comp_id = txt.substring(txt.indexOf("</span>") + 7);
                //var instance_id = txt.substring(txt.indexOf(":")+1);
                // var instance_id = list.children[i].children[0].innerHTML;
                var instance_id = txt.substring(txt.indexOf(">") + 1);
                instance_id = instance_id.substring(0, instance_id.indexOf("<"));
                var instance = {};
                instance["server_id"] = server_id;
                instance["component_id"] = comp_id;
                instance["instance_id"] = instance_id;
                config.push(instance);
            }
        });

        $("#topo-config").val(JSON.stringify(config));
        $("#topoSubmitForm").submit();
    });

});