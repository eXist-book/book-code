xquery version "3.0";

module namespace ex = "http://example/restxq/1";

import module namespace rest = "http://exquery.org/ns/restxq";

declare
    %rest:GET
function ex:not-found() {
    <result>The requested page could not be found!</result>
};