xquery version "3.0";

module namespace ex = "http://example/restxq/7";

import module namespace rest = "http://exquery.org/ns/restxq";

declare
    %rest:GET
    %rest:path("/hello")
    %rest:query-param("name", "{$name}", "stranger")
function ex:say-hello($name) {
    <greeting>Hi there {$name}!</greeting>
};
