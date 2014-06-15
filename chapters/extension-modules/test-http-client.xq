xquery version "3.0" encoding "UTF-8";

let $uri := "http://localhost:8080/exist/rest/db"
let $http-request-data := 
    <request xmlns="http://expath.org/ns/http-client" method="GET" href="{$uri}"/>
return
    <Result timestamp="{current-dateTime()}" uri="{$uri}">
    {
        http:send-request($http-request-data)
    }
    </Result>