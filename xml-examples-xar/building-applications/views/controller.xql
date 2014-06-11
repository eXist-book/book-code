xquery version "1.0" encoding "UTF-8";

if ($exist:resource eq '')
  then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
      <redirect url="home.xql"/>
    </dispatch>

else if ($exist:resource eq 'view1')
  then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
      <forward url="{concat($exist:controller, '/createmodel.xql')}"/>
      <view>
        <forward servlet="XSLTServlet">
          <set-attribute name="xslt.stylesheet" value="{concat($exist:root, $exist:controller, '/xsl/view1.xsl')}"/>        
        </forward>
      </view>
    </dispatch>    
    
else if ($exist:resource eq 'view2')
  then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
      <forward url="{concat($exist:controller, '/createmodel.xql')}"/>
      <view>
         <forward servlet="XSLTServlet">
          <set-attribute name="xslt.stylesheet" value="{concat($exist:root, $exist:controller, '/xsl/view2a.xsl')}"/>        
        </forward>
        <forward servlet="XSLTServlet">
          <set-attribute name="xslt.stylesheet" value="{concat($exist:root, $exist:controller, '/xsl/view2b.xsl')}"/>        
        </forward>
      </view>
    </dispatch>   
    
else if (ends-with($exist:resource, '.xql'))
  then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
      <forward url="{concat($exist:controller, $exist:path)}"/>
    </dispatch>    

else
  <ignore xmlns="http://exist.sourceforge.net/NS/exist">
    <cache-control cache="yes"/> 
  </ignore>