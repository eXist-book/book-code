xquery version "1.0" encoding "UTF-8";

import module namespace installer="http://www.exist-db.org/book/installer" 
  at "installer/installer.xqm";

<Install timestamp="{current-dateTime()}" load-path="{installer:get-load-path()}">
{
  installer:install-index-definition('indexing/data', 'indexing/collection.xconf')
}
</Install>
