xquery version "1.0" encoding "UTF-8";

if($exist:resource eq "")then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
        <redirect url="home.xq"/>
    </dispatch>

else if($exist:resource eq "view1")then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
        <forward url="{concat($exist:controller, "/createmodel.xq")}"/>
        <view>
            <forward servlet="XSLTServlet">
                <set-attribute name="xslt.stylesheet" value="{concat($exist:root, $exist:controller, "/xslt/view1.xslt")}"/>        
            </forward>
        </view>
    </dispatch>    
    
else if($exist:resource eq "view2")then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
        <forward url="{concat($exist:controller, "/createmodel.xq")}"/>
        <view>
            <forward servlet="XSLTServlet">
                <set-attribute name="xslt.stylesheet" value="{concat($exist:root, $exist:controller, "/xslt/view2a.xslt")}"/>        
            </forward>
            <forward servlet="XSLTServlet">
                <set-attribute name="xslt.stylesheet" value="{concat($exist:root, $exist:controller, "/xslt/view2b.xslt")}"/>        
            </forward>
        </view>
    </dispatch>   
    
else if(ends-with($exist:resource, ".xq"))then
    <dispatch xmlns="http://exist.sourceforge.net/NS/exist">
        <forward url="{concat($exist:controller, $exist:path)}"/>
    </dispatch>    

else
    <ignore xmlns="http://exist.sourceforge.net/NS/exist">
        <cache-control cache="yes"/> 
    </ignore>