xquery version "3.0";

module namespace ex = "http://example/restxq/2";

import module namespace rest = "http://exquery.org/ns/restxq";
declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare namespace http = "http://expath.org/ns/http-client";

declare
    %rest:GET
    %rest:HEAD
    %rest:POST
    %rest:PUT
    %rest:DELETE
    %output:method("html5")
function ex:not-found() {
    (
    <rest:response>
        <http:response status="404"/>
    </rest:response>
    ,
    <html>
        <head><title>Document not found!</title></head>
        <body>
            <p>Sorry, we could not find the document that you requested :-(</p>
        </body>
    </html>
    )
};
