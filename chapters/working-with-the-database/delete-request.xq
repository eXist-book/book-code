xquery version "1.0" encoding "UTF-8";

let $URI := 'http://localhost:8080/exist/rest/db/apps/exist-book/data/put-example.xml'
return
    httpclient:delete(xs:anyURI($URI), false(), ())