xquery version "3.0";

module namespace ex = "http://example/restxq/6";

import module namespace rest = "http://exquery.org/ns/restxq";

declare
    %rest:POST("{$body}")
    %rest:consumes("application/xml", "text/xml")
    %rest:produces("application/xml")
function ex:echo($body) {
    <received>{$body}</received>
};