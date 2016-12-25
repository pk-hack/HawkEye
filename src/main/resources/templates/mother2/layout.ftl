<#import "common/util.ftl" as util/>

<#macro page games>
    <html>
    <head>
        <meta charset="utf-8"/>
        <title><@util.vs_title games=games /></title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
        <link rel="stylesheet" href="static/css/jquery.tocify.css">
        <link rel="stylesheet" href="static/css/style.css">
    </head>
    <body>

    <nav class="navbar navbar-default navbar-fixed-top">
        <div class="container">
            <div class="navbar-header">
                <a class="navbar-brand" href="#"><@util.vs_title games=games /></a>
            </div>
            <div id="navbar" class="navbar-collapse collapse">
                <ul class="nav navbar-nav">
                    <li><a href="index.html">Items</a></li>
                    <li><a href="npcs.html">NPCs</a></li>
                    <li><a href="doors.html">Doors</a></li>
                    <li><a href="actions.html">Actions</a></li>
                    <li><a href="enemies.html">Enemies</a></li>
                    <li><a href="psi.html">PSI Descriptions</a></li>
                    <li><a href="phone.html">Phone calls</a></li>
                    <li><a href="deliveries.html">Deliveries</a></li>
                    <li><a href="other.html">Other</a></li>
                    <li class="divide"></li>
                </ul>
                <p class="navbar-text navbar-right">
                    ${lastUpdated?date}
                </p>
            </div>
        </div>
    </nav>

    <div class="row">
        <div class="col-md-2">
            <div id="toc" style="position: fixed;"></div>
        </div>
        <div class="col-md-9">
            <#nested/>
        </div>
    </div>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
    <script src="https://code.jquery.com/ui/1.11.3/jquery-ui.min.js"></script>
    <script src="static/js/jquery.tocify.min.js"></script>

    <script>
        $(function() {
            $("#toc").tocify({ selectors: "h2, h3, h4, h5", scrollTo: 70 }).data("toc-tocify");
        });



    </script>

    </body>
    </html>
</#macro>