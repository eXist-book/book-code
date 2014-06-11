xquery version "3.0";

let $msg := 'Hello XQuery'
return
  <results timestamp="{current-dateTime()}">
     <message>{$msg}</message>
  </results>
