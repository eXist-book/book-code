xquery version "3.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html";

let $page-title := "Cache module example"
let $cache := cache:cache("test")
let $previous-value := cache:put("test", "KEY", current-dateTime())
return
    <html>
        <head>
            <meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
            <title>{$page-title}</title>
        </head>
        <body>
            <h1>{$page-title}</h1>
            {
                if(empty($previous-value))then
                    <p>There was no previous call to this script during the uptime of the database</p>
                else
                    <p>The previous call was at: {$previous-value}</p>
            }
            <p>This call was made at {current-dateTime()}</p>
        </body>
    </html>