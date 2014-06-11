xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $page-title as xs:string := "Upload example (binary file)";

<html>
		<head>
			<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
			<title>{$page-title}</title>
		</head>
		<body>
			<h1>{$page-title}</h1>
      <form enctype="multipart/form-data" method="post" action="upload-process.xql">
        <p>Upload binary file:         
          <input type="file" size="80" name="file-upload"/>
          <br/>
          <input type="submit"/>
        </p>
      </form>
		</body>
</html>