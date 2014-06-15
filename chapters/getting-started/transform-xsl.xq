xquery version "3.0";

declare option exist:serialize "method=html media-type=text/html";

transform:transform(
    doc("/db/apps/exist-book/getting-started/xml-example.xml"),
    doc("/db/apps/exist-book/getting-started/convert-items.xslt"),
    ()
)
