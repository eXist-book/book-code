xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=yes";

declare function local:traverse-collection($collection as xs:anyURI,
    $indent as xs:integer) as element(p)*
{
    for $sub-collection in xmldb:get-child-collections($collection)
    return
    (
        <p style="margin-left: {$indent}pt"><b>{$sub-collection}</b></p>,
        local:traverse-collection(xs:anyURI(concat($collection, '/', $sub-collection)), $indent + 10),
        for $document in xmldb:get-child-resources($collection)
        return
            <p style="margin-left: {$indent + 5}pt">{$document}</p>
    )
};

<body>
{ 
    local:traverse-collection(xs:anyURI('/db/apps/exist-book'), 0) 
}
</body>