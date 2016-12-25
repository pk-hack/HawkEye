<#import "layout.ftl" as cp/>
<@cp.page games=games >
    <div class="container-fluid">
    <#list index as indexEntry>
        <div class="row">
            <div class="col-md-12">
                <h2>${indexEntry.type} #${indexEntry.id}</h2>
            </div>

            <#if indexEntry.isIsomorphic>
                <table class="table-bordered table-striped">
                    <tr>
                        <#list games as game>
                            <th>${game.displayName?html}</td>
                        </#list>
                    </tr>
                    <#list indexEntry.linesTable as linesTableEntry>
                        <#if linesTableEntry.isLinesSame>
                            <tr class="understated">
                        <#else>
                            <tr>
                        </#if>
                            <#list linesTableEntry.lines as line>
                                <td><pre>${line?html}</pre></td>
                            </#list>
                        </tr>
                    </#list>
                </table>
            <#else>
                <table class="table-bordered">
                    <tr>
                        <#list games as game>
                            <th>${game.displayName}</th>
                        </#list>
                    </tr>
                    <tr>
                        <#list indexEntry.linesTables as linesTable>
                            <td class="valign-top">
                                <table class="table-bordered table-striped">
                                    <tr>
                                        <th>Text</th>
                                    </tr>
                                    <#list linesTable as linesTableEntry>
                                        <tr>
                                            <td><pre>${linesTableEntry.key?html}</pre></td>
                                        </tr>
                                    </#list>
                                </table>
                            </td>
                        </#list>
                    </tr>
                </table>
            </#if>
        </div>
    <#else>
        (No differences)
    </#list>
    </div>
</@cp.page>