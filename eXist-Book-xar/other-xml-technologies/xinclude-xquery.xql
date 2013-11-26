xquery version "1.0" encoding "UTF-8";

declare variable $par1 external;

<XQueryXql>
  <CurrentDoc>{ $xinclude:current-doc }</CurrentDoc>
  <CurrentCollection>{ $xinclude:current-collection }</CurrentCollection>
  <Par1>{ $par1 }</Par1>
</XQueryXql>