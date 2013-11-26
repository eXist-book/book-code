xquery version "3.0" encoding "UTF-8";
(:~
  Show the source for a resource
:)

(:============================================================================:)
(:== GLOBAL VARIABLES: ==:)

declare variable $resource as xs:string := request:get-parameter('resource', '');

(:============================================================================:)
(:== MAIN: ==:)

if (util:binary-doc-available($resource))
  then response:stream-binary(util:binary-doc($resource), 'text/plain', ())
else if (doc-available($resource))
  then response:stream-binary(
    util:string-to-binary( util:serialize(doc($resource), 
      'method=xml media-type=text/xml indent=yes expand-xincludes=no process-xsl-pi=no'), 'UTF-8'),
      'text/xml', ())
else
  error((), concat('Could not access resource &quot;', $resource, '&quot;'))

(:============================================================================:)
