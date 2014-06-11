xquery version "3.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html";

let $page-title := 'Compression module example - Zipping content'
let $stuff-to-compress as item()+ := (xs:anyURI('/db/apps/eXist-book/getting-started'),
    <entry name="EXTRA/x.xml" type="xml">
      <Extra>Some extra stuff...</Extra>
    </entry>
)
let $file-uri := '/test/compression-test.zip'
let $zipfile := compression:zip($stuff-to-compress, true())

return
   <html>
  	 <head>
  	   <meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
  			 <title>{$page-title}</title>
  	 </head>
  	 <body>
  	   <h1>{$page-title}</h1>
       <p>Compressed to {$file-uri}: {file:serialize-binary( compression:zip($stuff-to-compress, true()), $file-uri)}</p>
  	 </body>
  </html>
