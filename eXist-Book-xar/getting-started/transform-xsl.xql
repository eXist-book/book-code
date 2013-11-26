xquery version "3.0";
declare option exist:serialize "method=html media-type=text/html";

transform:transform( doc('xml-example.xml'), doc('convert-items.xsl'), ())