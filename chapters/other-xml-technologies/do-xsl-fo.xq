xquery version "1.0" encoding "UTF-8";

let $xsl-fo-document as document-node() := doc("fop-readme.xml") 
let $media-type as xs:string := "application/pdf"
return
    response:stream-binary(
        xslfo:render($xsl-fo-document, $media-type, ()),
        $media-type,
        "output.pdf"
    )
