xquery version "3.0";

declare option exist:serialize "method=html media-type=text/html";

let $msg := 'Hello XQuery'
return
    <html>
        <head>
            <title>Hello XQuery</title>
        </head>
        <body>
            <h3>It is now {current-dateTime()} and so {$msg}!</h3>
        </body>
    </html>