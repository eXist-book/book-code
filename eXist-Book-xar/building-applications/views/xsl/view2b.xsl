<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" version="2.0" exclude-result-prefixes="#all">
  <!-- -->
    <xsl:variable name="page-title" as="xs:string" select="'Output after double view'"/>
    <xsl:template match="/">
        <html>
            <head>
                <meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
                <title>
                    <xsl:value-of select="$page-title"/>
                </title>
            </head>
            <body>
                <h1>
                    <xsl:value-of select="$page-title"/>
                </h1>
                <ul>
                    <xsl:for-each select="//Item">
                        <li>Item <xsl:value-of select="@id"/>: <xsl:value-of select="."/>
                        </li>
                    </xsl:for-each>
                </ul>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>