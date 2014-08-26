xquery version "1.0" encoding "UTF-8";

let $URI := 'http://localhost:8080/exist/rest/db/apps/exist-book/data/put-example.xml'
return
    httpclient:put(xs:anyURI($URI), <new-file-by-rest-put/>, false(), ())