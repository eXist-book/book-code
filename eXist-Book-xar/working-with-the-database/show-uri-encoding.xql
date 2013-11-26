xquery version "3.0" encoding "UTF-8";

declare namespace xhtml="http://www.w3.org/1999/xhtml";

declare option exist:serialize "method=xhtml media-type=text/html indent=no";

(:============================================================================:)

declare variable $page-title as xs:string := "Show URI encoding";
declare variable $name-to-encode as xs:string := request:get-parameter('name-to-encode', '');

(:============================================================================:)

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta HTTP-EQUIV="Content-Type" content="text/xhtml; charset=UTF-8"/>
    <title>{ $page-title }</title>
  </head>
  <body>
    <h1>{ $page-title }</h1>
    {
      if ($name-to-encode ne '')
        then 
          <p>URI encoding of <code>{$name-to-encode}</code>: <code>{xmldb:encode-uri($name-to-encode)}</code></p>
        else ()
    }
    <br/>
    <form method="post" action="">
      <input type="text" name="name-to-encode" length="80"/>
      <input type="submit"/>
    </form>
  </body>
</html>

(:============================================================================:)
