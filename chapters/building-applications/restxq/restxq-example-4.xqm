xquery version "3.0";

module namespace ex = "http://example/restxq/4";

import module namespace rest = "http://exquery.org/ns/restxq";

declare
    %rest:GET
    %rest:path("/hello/{$name}")
function ex:say-hello($name) {
    <greeting>Hi there {$name}!</greeting>
};
