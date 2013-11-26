xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $page-title as xs:string := "Search Demo";

<html>
		<head>
			<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
			<title>{$page-title}</title>
		</head>
		<body>
			<h1>{$page-title}</h1>
			<form method="post" action="search-demo-result.xql">
   			Enter the Lucene search expression:  
   			<input type="text" size="80" name="searchexpr"/>
   			<input type="submit"/>
			</form>
		</body>
</html>
