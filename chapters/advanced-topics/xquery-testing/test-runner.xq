xquery version "3.0";

import module namespace inspect = "http://exist-db.org/xquery/inspection";
import module namespace test = "http://exist-db.org/xquery/xqsuite" at "resource:org/exist/xquery/lib/xqsuite/xqsuite.xql";

let $modules := (xs:anyURI("/db/apps/exist-book/chapters/advanced-topics/xquery-testing/id.xqm")) (: sequence of URIs to modules to XQSuite run tests for :)
let $functions := $modules ! inspect:module-functions(.) 
return
    test:suite($functions)