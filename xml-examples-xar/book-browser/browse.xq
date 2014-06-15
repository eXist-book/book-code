xquery version "3.0" encoding "UTF-8";
(:~
  Main browser page for the eXist book examples browser
:)
(:============================================================================:)
(:== SETUP: ==:)

import module namespace book-browser = "http://www.exist-db.org/namespaces/book/browser"
  at "book-browser.xqm";
  
declare option exist:serialize "method=html media-type=text/html indent=no";
  
(:============================================================================:)
(:== GLOBAL VARIABLES: ==:)

declare variable $CONTENTS-DESCRIPTION-RESOURCE-NAME as xs:string := 'examples-contents.xml';

declare variable $page-title as xs:string := "eXist book examples browser";
declare variable $location-parameter as xs:string := 'location';
declare variable $location as xs:string := request:get-parameter($location-parameter, '');
declare variable $collection as xs:string := 
  concat($book-browser:BOOK-EXAMPLES-ROOT-LOCATION, $location);

(:============================================================================:)
(:== FUNCTIONS: ==:)


declare function local:full-contents-description-resource-name(
  $location as xs:string
) as xs:string
{
  concat($book-browser:BOOK-EXAMPLES-ROOT-LOCATION, $location, '/', 
    $CONTENTS-DESCRIPTION-RESOURCE-NAME)
};

(:----------------------------------------------------------------------------:)

declare function local:has-contents-description-resource(
  $location as xs:string
) as xs:boolean
{
  doc-available(local:full-contents-description-resource-name($location))
};

(:----------------------------------------------------------------------------:)

declare function local:contents-description-title(
  $location as xs:string
) as xs:string
{
  (doc(local:full-contents-description-resource-name($location))/*/Title, $location)[1]  
};

(:----------------------------------------------------------------------------:)

declare function local:contents-root(
  $location as xs:string
) as element()
{
  doc(local:full-contents-description-resource-name($location))/*
};

(:----------------------------------------------------------------------------:)

declare function local:has-contents-examples(
  $location as xs:string
) as xs:boolean
{
  exists(doc(local:full-contents-description-resource-name($location))/*/Example)
};

(:----------------------------------------------------------------------------:)

declare function local:contents-chapter-nr(
  $location as xs:string
) as xs:integer
{
  xs:integer(doc(local:full-contents-description-resource-name($location))/*/@chapter) 
};

(:----------------------------------------------------------------------------:)

declare function local:show-examples() as element()*
{
  if (not(local:has-contents-examples($location)))
    then ()
    else (
      <h3>Examples for: {local:contents-description-title($location)}</h3>,
      let $examples-description as xs:string := 
        string(local:contents-root($location)/Description)
      return
        if ($examples-description ne '')
          then <p>{ $examples-description }</p>
          else (),
      <ul>
      {
        for $example in local:contents-root($location)/Example
          let $href as xs:string := string($example/@href)
          let $show-source as xs:boolean :=
            if (empty($example/@show-source))
              then true()
              else xs:boolean($example/@show-source)
          let $is-rest-call as xs:boolean :=
            if (empty($example/@rest))
              then false()
              else xs:boolean($example/@rest)
          let $href-full := 
            if ($is-rest-call)
              then concat('/exist/rest/db/apps/', $book-browser:APPLICATION-NAME, $location, '/', $href)
              else concat('/exist/apps/', $book-browser:APPLICATION-NAME, $location, '/', $href)
          let $resource-full := concat($collection, '/', $href)
          return
            <li>
              <a href="{$href-full}">{string($example/Title)}</a>
              {
                if ($show-source)
                  then ( ' (', <a href="view-source?resource={encode-for-uri($resource-full)}">{string($href)}</a>, ')' )
                  else ()
              }
              {((: Show description (if any): :))}
              {
                if (exists($example/Description))
                  then (
                    <br/>,
                    string($example/Description)
                  )
                  else ()
              }
              {((: Show additional sources (if any): :))}
              {
                if (exists($example/AdditionalSource[@href]))
                  then (
                    <br/>,
                    'Additional sources:',
                     for $additional-resource-href in $example/AdditionalSource/@href/string()
                       let $addition-resource-full := concat($collection, '/', $additional-resource-href)
                       return (
                        '&#160;&#160;',
                         <a href="view-source?resource={$addition-resource-full}">{$additional-resource-href}</a>
                       )
                  )
                  else ()
              }
            </li>
      }
      </ul>
    )
};

(:----------------------------------------------------------------------------:)

declare function local:show-sub-collections-with-examples() as element()*
{
  let $sub-locations-with-examples as xs:string* :=
    for $col in xmldb:get-child-collections(xs:anyURI($collection))
      let $sub-location := concat($location, '/', $col)
      order by local:contents-chapter-nr($sub-location)
      return
        if (local:has-contents-description-resource($sub-location))
          then $sub-location 
          else ()
  return
   if (exists($sub-locations-with-examples))
     then (
         <h3>Sub-collections with examples:</h3>,
        <ul>
        {
          for $sub-location in $sub-locations-with-examples
            return
              <li><a href="?{$location-parameter}={encode-for-uri($sub-location)}"
                >{local:contents-description-title($sub-location)}</a></li>
        }
        </ul>
     ) 
     else ()
};

(:============================================================================:)
(:== MAIN: ==:)

<html>
		<head>
			<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
			<title>{$page-title}</title>
			<style>
				h1    {{ font-family: Arial; font-size: 18 pt; font-weight: bold; margin-bottom: 20; margin-top: 10 }}
				h2    {{ font-family: Arial; font-size: 14 pt; font-weight: bold; margin-bottom: 10; margin-top: 10 }}
				h3    {{ font-family: Arial; font-size: 12 pt; margin-bottom: 5; margin-top: 10 }}
				p     {{ font-family: Times; font-size: 11 pt; margin-top: 0; margin-bottom: 1 }}
				li    {{ font-family: Times; font-size: 11 pt; margin-top: 3; margin-bottom: 10 }}
				ul    {{ font-family: Times; font-size: 11 pt; margin-top: 0; margin-bottom: 1 }}
			</style>
		</head>
		<body>
		
      {((: Header of the page: :))}
      <h1><img src="book-cover.png" width="10%"/> {$page-title}</h1>
      <br/>
      <p>Current collection: <code>{$collection}</code>
        {
          if (not($location = ('', '/')))
            then ('&#160;&#160;', <a href="javascript:window.history.back()">back</a>)
            else ()
        }
      </p>  
      <br/>
      
      {((: Show all examples :))}
			{
        local:show-examples()
			}
			
			{((: Show sub-collections with examples (if any): :))}
			{  
        local:show-sub-collections-with-examples()
			}
			
		</body>
</html>

(:============================================================================:)
