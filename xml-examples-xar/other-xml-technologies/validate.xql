xquery version "1.0" encoding "UTF-8";

let $xml-valid as xs:anyURI := xs:anyURI('/db/apps/eXist-book/other-xml-technologies/simple-xml-valid.xml')
let $xml-invalid as xs:anyURI := xs:anyURI('/db/apps/eXist-book/other-xml-technologies/simple-xml-invalid.xml')
let $xsd as xs:anyURI := xs:anyURI('/db/apps/eXist-book/other-xml-technologies/simple.xsd')
return 
  <ValidationResults>
    <ValidateValid>
    {
      validation:jing-report($xml-valid, $xsd)
    }
    </ValidateValid>
    <ValidateInvalid>
    {
      validation:jing-report($xml-invalid, $xsd)
    }
    </ValidateInvalid>
  </ValidationResults>  