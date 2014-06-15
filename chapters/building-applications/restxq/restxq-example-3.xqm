xquery version "3.0";

module namespace ex = "http://example/restxq/3";

import module namespace rest = "http://exquery.org/ns/restxq";

declare
    %rest:POST("{$body}")
function ex:echo($body) {
    <received>{$body}</received>
};