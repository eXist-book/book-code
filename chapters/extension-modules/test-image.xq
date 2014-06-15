xquery version "3.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html";

let $image-uri as xs:anyURI := xs:anyURI("/db/apps/exist-book/book-browser/book-cover.png")
let $image := util:binary-doc($image-uri)
let $page-title := "Image module demo"
return 
    <html>
        <head>
            <meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
            <title>{$page-title}</title>
        </head>
        <body>
            <h1>{$page-title}</h1>
            <p>Image width: {image:get-width($image)}</p>
            <img src="../book-browser/book-cover.png"/>
            <p>This call was made at {current-dateTime()}</p>
        </body>
    </html>