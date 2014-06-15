xquery version "1.0" encoding "UTF-8";
(:~
  eXist Webapp URL rewriting controller for the eXist book browser
:)
(:============================================================================:)

(: eXist URL rewriting controller external variables: :)
declare variable $exist:path external;
declare variable $exist:resource external;
declare variable $exist:controller external;
declare variable $exist:prefix external;
declare variable $exist:root external;

(:============================================================================:)
(:== DO THE REDIRECTS/FORWARDS: :)

  (: See if we have an empty path. If so, redirect straight to the browser page: :)  
  (: Take care to distinguish between a path ending in a / or not! :)
  if ($exist:path eq '')
    then
      <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
        <redirect url="eXist-book/book-browser/browse"/>
      </dispatch>
  else if ($exist:path eq '/')
    then 
      <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
        <redirect url="book-browser/browse"/>
      </dispatch>
      
  (: If it has no extension or .xq, assume its an XQuery script and pass control to this: :)
  else if (not(contains($exist:resource, '.')) or ends-with($exist:resource, '.xq'))
    then  
      let $forward-url as xs:string := concat($exist:controller, $exist:path)
      let $full-forward-url as xs:string :=
       if (not(contains($exist:resource, '.')))
         then concat($forward-url, '.xq')
         else $forward-url
      return
        <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
          <forward url="{$full-forward-url}"/>
        </dispatch>
    
  (: Anything else, pass through: :)
  else 
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
      <cache-control cache="yes"/> 
    </dispatch>

(:============================================================================:)
