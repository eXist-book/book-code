xquery version "3.0" encoding "UTF-8";

let $URI := 'http://localhost:8080/exist/rest/doesnotmatter'
let $query := 'for $i in 1 to 10 return <Result index="{$i}"/>'
let $request := 
  <query xmlns="http://exist.sourceforge.net/NS/exist" start="3" max="3">
    <text>{$query}</text>
  </query>
return 
  httpclient:post(xs:anyURI($URI), $request, false(), ())