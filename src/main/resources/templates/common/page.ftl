<#macro page title>
    <html>
        <head>
            <title>${title?html}</title>
            <meta charset="utf-8" />
        </head>
    <body>

    <#nested/>

    </body>
    </html>
</#macro>