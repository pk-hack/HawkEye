<#macro vs_title games>
    <#list games as game>${game.displayName?html}<#sep> vs </#list>
</#macro>