xquery version "1.0" encoding "UTF-8";
(:~
  Example module with xqDoc information
  @version 1.0
  @author Erik Siegel
:)

module namespace xquerydoc="http://www.exist-db.org/book/XQueryDoc";

declare variable $xquerydoc:global as xs:string := 'some globaal value';

(:~
  Example dummy function
  @param $in The input to the function
:)
declare function xquerydoc:test($in as xs:string+) as xs:string 
{
  'Dummy'
};