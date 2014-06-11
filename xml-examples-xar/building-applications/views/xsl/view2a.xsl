<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" version="2.0" exclude-result-prefixes="#all">
  <!-- -->
    <xsl:template match="@* | node()">
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
  <!-- -->
    <xsl:template match="Item">
        <xsl:copy copy-namespaces="no">
            <xsl:copy-of select="@*" copy-namespaces="no"/>
            <xsl:value-of select="."/> - After extra processing step! </xsl:copy>
    </xsl:template>
</xsl:stylesheet>