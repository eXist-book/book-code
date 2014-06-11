xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $page-title as xs:string := "Mini Application Hello Page";

<html>
		<head>
			<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
			<title>{$page-title}</title>
		</head>
		<body>
			<h1>{$page-title}</h1>
			<p>Hello <i>{request:get-parameter('personname', '?')}</i></p>
      <p><a href="home">home</a></p>
      <br/>
      <p>Powered by:</p>
      <img src="images/existdb.png" width="10%"/>
		</body>
</html>
