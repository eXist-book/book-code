xquery version "1.0" encoding "UTF-8";

(:~
: Example URL Rewriting Controller
:)

(: All the external variables available to the controller: :)
declare variable $exist:path external;
declare variable $exist:resource external;
declare variable $exist:controller external;
declare variable $exist:prefix external;
declare variable $exist:root external;

<dispatch xmlns="http://exist.sourceforge.net/NS/exist">
  <forward url="{$exist:controller}/show-controller-variables.xq">
    <add-parameter name="exist.root" value="{$exist:root}"/>
    <add-parameter name="exist.path" value="{$exist:path}"/>
    <add-parameter name="exist.resource" value="{$exist:resource}"/>
    <add-parameter name="exist.controller" value="{$exist:controller}"/>
    <add-parameter name="exist.prefix" value="{$exist:prefix}"/>
  </forward>
</dispatch>
