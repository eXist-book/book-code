xquery version "1.0" encoding "UTF-8";

let $xml-input := <Bogus/>
let $parameters := 
    <parameters>
        <param name="par1" value="value of par1"/>
        <param name="par2" value="value of par2"/>
    </parameters>
let $xslt-uri := xs:anyURI('xmldb:exist:///db/apps/exist-book/other-xml-technologies/parameters.xslt')  
return
    transform:transform($xml-input, $xslt-uri, $parameters)