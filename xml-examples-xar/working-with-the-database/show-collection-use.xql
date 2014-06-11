xquery version "3.0" encoding "UTF-8";

declare namespace xhtml="http://www.w3.org/1999/xhtml";

declare option exist:serialize "method=xhtml media-type=text/html indent=no";

(:============================================================================:)

declare variable $page-title as xs:string := "Demo of collection function";
declare variable $collection as xs:string := '/db/apps/eXist-book/getting-started';

(:============================================================================:)

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta HTTP-EQUIV="Content-Type" content="text/xhtml; charset=UTF-8"/>
    <title>{ $page-title }</title>
  </head>
  <body>
    <h1>{ $page-title }</h1>
    <p>Collection: <code>{$collection}</code></p>
    <br/>
    <table border="1" cellspacing="0">
      <tr>
        <th>Resource</th>
        <th>XML?</th>
      </tr>
      {
        for $doc in collection($collection)      
          return
            <tr>
              <td>{base-uri($doc)}</td>
              <td>{exists($doc/*)}</td>
            </tr>
      }
    </table>
  </body>
</html>

(:============================================================================:)
