{
    "service":
    {
        "component_id": "${ComponentID}",
        "name": "${ComponentID}Service",
        "path": "/resources/manage",
        "description": "${Description}"<#if IsBroker??>,
        "type": "broker"</#if>
    },
    "workers":
    [
        {
            "worker_id": "gr.iti.openzoo.service.impl.${ComponentID}Worker",
            "endpoints":
            [
<#if HasInput??>
                {
                    "endpoint_id": "input",
                    "type": "in"
                }<#if (NumOutputs > 0)>,</#if>
</#if>
<#if (NumOutputs > 0)>
 <#list 1..NumOutputs as i>
                {
                    "endpoint_id": "output_${i}",
                    "type": "out"
                }<#sep>,</#sep>
 </#list>
</#if>
            ]
        }
    ]<#if RequiredParameters??>,
    "requires":
    [
 <#list RequiredParameters as Parameter>
        "${Parameter}"<#sep>,</#sep>
 </#list>
    ]
</#if>
}