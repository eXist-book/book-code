xquery version "1.0" encoding "UTF-8";
(: Example URL Rewriting Controller :)

(: External variables available to the controller: :)
declare variable $exist:path external;
declare variable $exist:resource external;
declare variable $exist:controller external;

(: Other variables :)
declare variable $home-page-url := 'home';

(: Function to get the extension of a filename: :)
declare function local:get-extension($filename as xs:string) as xs:string {
  let $name := replace($filename, '.*[/\\]([^/\\]+)$', '$1')
  return
    if (contains($name, '.'))
    then replace($name, '.*\.([^\.]+)$', '$1')
    else ''
};

(: If there is no resource specified, go to the home page.
   This is a redirect, forcing the browser to perform a redirect. So this request
   will pass through the controller again... :)
if ($exist:resource eq '')
  then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
      <redirect url="{$home-page-url}"/>
    </dispatch>

(: Check if there is no extension. If not, assume it is an Xquery file and forward to this. 
   Because we use forward here, the browser will not be informed of the change and the user will
   still see a URL without a .xql extension. :)
else if (local:get-extension($exist:resource) eq '') then
  <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
    <forward url="{concat($exist:controller, $exist:path, '.xql')}"/>
  </dispatch>

(: Anything else, pass through: :)
else
  <ignore xmlns="http://exist.sourceforge.net/NS/exist">
    <cache-control cache="yes"/> 
  </ignore>
