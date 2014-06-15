xquery version "1.0" encoding "UTF-8";

declare variable $data-collection as xs:anyURI := xs:anyURI('/db/apps/exist-book/data');
declare variable $update-resource as xs:string := 'update-resource.xml';

declare function local:make-sure-update-resource-exists() as xs:string?
{
    if($update-resource = xmldb:get-child-resources($data-collection))then
        () (: Resource exists... :) 
    else
        (: Resource does not exist, create... :)
        xmldb:store($data-collection, $update-resource, <UpdateResource created="{current-dateTime()}"/>)
};

let $full-uri as xs:string := concat($data-collection, '/', $update-resource)
let $root-elm as element() := doc($full-uri)/*
return
    <UpdateResult document="{$full-uri}" created="{local:make-sure-update-resource-exists()}">
    {
        update insert attribute changed {current-dateTime()} into $root-elm,
        update insert <NewElement/> into $root-elm
    }
        Changed: {$full-uri}
    </UpdateResult>