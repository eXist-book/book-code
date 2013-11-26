xquery version "3.0" encoding "UTF-8";

import module namespace content="http://exist-db.org/xquery/contentextraction"
  at "java:org.exist.contentextraction.xquery.ContentExtractionModule";

declare option exist:serialize "method=xml media-type=text/xml";

declare variable $file as xs:string := '/db/apps/eXist-book/extension-modules/data/chapter-2.pdf';

<Content file="{$file}">
{
  content:get-metadata-and-content(util:binary-doc($file))
}
</Content>