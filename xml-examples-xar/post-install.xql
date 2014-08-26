xquery version "1.0" encoding "UTF-8";

import module namespace installer="http://www.exist-db.org/book/installer" 
  at "installer/installer.xqm";

<Install timestamp="{current-dateTime()}" load-path="{installer:get-load-path()}">
  
  <Task name="Install index definitions">
    {
      installer:install-index-definition('indexing/data', 'indexing/collection.xconf')
    }
  </Task>
  
  <Task name="Adjust permissions">
    {
      let $data-collection := concat(installer:get-load-path(), '/data')
      let $demo-zip-xq := concat(installer:get-load-path(), '/extension-modules/demo-zip.xq')
      return (
        sm:chmod($data-collection, 'rwxrwxrwx'),
        sm:chmod($demo-zip-xq, 'rwxrwxr--') 
      )
    }
  </Task>
  
</Install>
