xquery version "3.0" encoding "UTF-8";
(:~
  This module serves only one purpose: To get the location of our eXist-book 
  application in the database. It has to be in an XQuery module because the
  function system:get-module-load-path() only returns sensible results for 
  a module, not for a normal XQuery script.
:)
(:============================================================================:)

module namespace book-browser="http://www.exist-db.org/namespaces/book/browser";

declare variable $book-browser:BOOK-EXAMPLES-ROOT-LOCATION as xs:string := 
  replace(system:get-module-load-path(), '^(xmldb:exist://)?(embedded-eXist-server)?(.+)/.+$', '$3');
  
declare variable $book-browser:APPLICATION-NAME as xs:string :=
   replace($book-browser:BOOK-EXAMPLES-ROOT-LOCATION, '.*[/\\]([^/\\]+)$', '$1');

(:============================================================================:)
