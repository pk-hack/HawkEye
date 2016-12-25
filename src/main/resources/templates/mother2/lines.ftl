<#import "layout.ftl" as cp/>
<@cp.page games=games >

<div class="container-fluid">
    <#list games as game>
        <h2>${game.displayName?html}</h2>
        <#list lines[game?index] as line>
            <h3>${line.id}</h3>

            <table class="table-bordered table-striped">
            <#list line.text as textRow>
                <tr><td><pre>${textRow}</pre></td></tr>
            </#list>
            </table>
        </#list>
    </#list>
</div>

</@cp.page>