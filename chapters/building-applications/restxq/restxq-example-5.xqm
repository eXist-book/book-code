xquery version "3.0";

module namespace ex = "http://example/restxq/5";

import module namespace rest = "http://exquery.org/ns/restxq";

declare
    %rest:POST("{$body}")
    %rest:consumes("application/xml", "text/xml")
function ex:echo($body) {
    <received>{$body}</received>
};