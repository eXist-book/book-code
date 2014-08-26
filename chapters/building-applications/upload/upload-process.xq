xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $page-title as xs:string := "Upload example process (binary file)";

let $field-name as xs:string := "file-upload"
let $store-collection as xs:anyURI := xs:anyURI('/db/apps/exist-book/data')

let $upload-name as xs:string := request:get-uploaded-file-name($field-name)
let $upload-size as xs:double := request:get-uploaded-file-size($field-name)
let $stored-file as xs:string? := xmldb:store($store-collection, $upload-name, 
  request:get-uploaded-file-data($field-name), 'application/octet-stream')  

return
  <html>
  		<head>
  			<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
  			<title>{$page-title}</title>
  		</head>
  		<body>
  			<h1>{$page-title}</h1>
  			<p>Stored: <code>{$stored-file}</code></p>
  			<p>Original name: {$upload-name}</p>
  			<p>Size: {$upload-size}</p>
  			<p>MIME type: {xmldb:get-mime-type(xs:anyURI($stored-file))}</p>
  		</body>
  </html>