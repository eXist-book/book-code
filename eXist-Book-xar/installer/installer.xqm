xquery version "3.0" encoding "UTF-8";

(: Module with support code for installing index configurations during package installation :)

module namespace installer="http://www.exist-db.org/book/installer";

(:----------------------------------------------------------------------------:)

declare function installer:get-base-path($path as xs:string) as xs:string
(: Returns the path leading up to the last part in $path. E.g. /a/b/c/d ==> /a/b/c :)
{
  replace($path, '(.*)[/\\][^/\\]+$', '$1')  
};

(:----------------------------------------------------------------------------:)

declare function installer:get-name($path as xs:string) as xs:string
(: Returns the final name of a path. E.g. /a/b/c/d ==> d :)
{
  replace($path, '.*[/\\]([^/\\]+)$', '$1')
};

(:----------------------------------------------------------------------------:)

declare function installer:get-load-path() as xs:string
(: Returns the load path of the application: Where did the repositiry manager installed it? :)
{
  (: The function system:get-module-load-path() returns something with the string 'embedded-eXist-server' in it. 
     Strange. The following regexp makes this a normal path, even if this string string should disappear in a
     future release. :)
    installer:get-base-path(
      replace(system:get-module-load-path(), '^(xmldb:exist://)?(embedded-eXist-server)?(.+)$', '$3')
    )
};

(:----------------------------------------------------------------------------:)

declare function installer:report-error($msg-parts as xs:string*) as item()?
{
  error(xs:QName('installer:ERROR'), string-join($msg-parts, ''))
};

(:----------------------------------------------------------------------------:)

declare function installer:create-collection-path($collection-path as xs:string) as xs:boolean
(: Creates the given collection path (and all sub-collections leading up to it) :)
{
  if (xmldb:collection-available($collection-path)) 
    then true() 
    else
      (: The collection does not exist. First make sure the path leading up to this
         exists and afterwards create it: :)    
      let $collection := installer:get-name($collection-path)
      let $base-path := installer:get-base-path($collection-path)
      return
        if (installer:create-collection-path($base-path))
          then 
            if (exists(xmldb:create-collection($base-path, $collection)))
              then true()
              else installer:report-error(('Error creating collection ', $collection, ' in ', $base-path))
          else false()
};

(:----------------------------------------------------------------------------:)

declare function installer:install-index-definition(
  $collection-to-index-relative as xs:string,
  $index-definion-document-relative as xs:string
) as xs:boolean
(: Installs the given index definition for the given collection path. All paths must be relative 
   to the application's root path and must not start with a / :)
{
  let $final-index-definition-document-name := 'collection.xconf'
  let $application-base-collection := installer:get-load-path()
  
  (: Compute where (which collection) the index definition document must finally be copied to: :)
  let $collection-to-index := concat($application-base-collection, '/', $collection-to-index-relative)
  let $index-definions-base-collection := '/db/system/config'
  let $index-definition-collection := concat($index-definions-base-collection, $collection-to-index)
  
  (: Get the absolute path to the source of the definition collection document: :)
  let $index-definition-source-document := concat($application-base-collection, '/', $index-definion-document-relative)

  return
    (: Check if source document exists: :)
    if (doc-available($index-definition-source-document))
      then
        (: Create the collection path for the index definition: :)
        if (installer:create-collection-path($index-definition-collection))
          then
            (: Copy the source index defintion: :)
            if (exists(xmldb:store($index-definition-collection, $final-index-definition-document-name, doc($index-definition-source-document))))
              then 
                (: Re-index: :)
                if (xmldb:reindex($collection-to-index))
                  then true()
                  else installer:report-error(('Could not re-index ', $collection-to-index))
              else installer:report-error(('Could not store index defintion in ', $index-definition-collection, '/', $final-index-definition-document-name))
          else installer:report-error(('Could not create index definition collection ', $index-definition-collection))
      else installer:report-error(('Index defintion source document not found: ', $index-definition-source-document))           
};

(:----------------------------------------------------------------------------:)