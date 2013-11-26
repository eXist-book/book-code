xquery version "3.0" encoding "UTF-8";

import module namespace content="http://exist-db.org/xquery/contentextraction"
  at "java:org.exist.contentextraction.xquery.ContentExtractionModule";

declare namespace xhtml="http://www.w3.org/1999/xhtml";

declare variable $file as xs:string := '/db/apps/eXist-book/extension-modules/data/chapter-2.pdf';
declare variable $x as xs:string := 'x';

let $content := content:get-metadata-and-content(util:binary-doc($file))
let $index-definition :=
  <doc>
    <field name="title" store="yes">{ $content//xhtml:title/text() }</field>
    {
      for $page in $content//xhtml:div[@class = "page"]/xhtml:p
      return
        <field name="para" store="yes">{ $page/text() }</field>
    }
  </doc>
return
  <Indexing file="{$file}">
  {
    ft:remove-index($file),
    ft:index($file, $index-definition)
  }
  {
    ft:search($file, 'para:"decide upon before installing"')
  }
  </Indexing>
