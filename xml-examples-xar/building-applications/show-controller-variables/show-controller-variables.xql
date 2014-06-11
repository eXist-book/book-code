xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $page-title as xs:string := "Show eXist Controller Variables";

<html>
		<head>
			<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
			<title>{$page-title}</title>
		</head>
		<body>
			<h1>{$page-title}</h1>
			<p>Original URL: <code>{request:get-url()}</code></p>
      <ul>
      {
			   for $par in ('exist.root', 'exist.path', 'exist.resource', 'exist.controller', 'exist.prefix')
			   return <li>{$par} = <code>"{request:get-parameter($par, '?')}"</code></li> 
			}
			</ul>	
		</body>
</html>
