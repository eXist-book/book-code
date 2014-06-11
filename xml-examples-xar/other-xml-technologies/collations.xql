xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $PageTitle as xs:string := "Collations example";
declare variable $terms as xs:string+ := ('ä', 'ç', 'ë', 'a', 'b', 'c', 'd', 'e');

<html>
		<head>
			<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
			<title>{$PageTitle}</title>
		</head>
		<body >
			<h1>{$PageTitle}</h1>
			<p>Original terms: { $terms }</p>
			<p>Sorted according to the default collation: { for $t in $terms order by $t return $t }</p> 
			<p>Sorted according to the Dutch collation: { for $t in $terms order by $t collation '?lang=nl-NL' return $t }</p>
			<hr/>
			<p>All language collations supported: { for $c in util:collations() order by $c return $c }</p>
		</body>
</html>

(:============================================================================:)
