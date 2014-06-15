xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $page-title as xs:string := "XSLT processor information";

declare variable $xslt as document-node() := document {
    <xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
      exclude-result-prefixes="#all">
      
        <xsl:variable name="SystemProperties" as="xs:string+" select="('xsl:vendor', 'xsl:vendor-url', 'xsl:product-name', 'xsl:product-version')"/>
        <xsl:template match="/">
            <XsltInfo>
                <xsl:for-each select="$SystemProperties">
                    <Info property="{{.}}">
                        <xsl:value-of select="system-property(.)"/>
                    </Info>
                </xsl:for-each>
            </XsltInfo>
        </xsl:template>
    
    </xsl:stylesheet>
};

<html>
    <head>
        <meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
        <title>{$page-title}</title>
    </head>
    <body>
        <h1>{$page-title}</h1>
        <ul>
        {
            for $info in transform:transform(<dummy/>, $xslt, ())//Info
            return
                <li>{string($info/@property)} = {string($info)}</li>
        }
        </ul>
    </body>
</html>
