xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $page-title as xs:string := "Views Home Page";

<html>
		<head>
			<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
			<title>{$page-title}</title>
		</head>
		<body>
			<h1>{$page-title}</h1>
			<ul>
			 <li><a href="createmodel.xql">Raw input to creating the views</a></li>
			 <li><a href="view1">View with single XSLT</a></li>
			 <li><a href="view2">View with double XSLT</a></li>
			</ul>
		</body>
</html>
