<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="par1"/>
    <xsl:param name="par2"/>
    <xsl:template match="/">
        <p>Values passed were "<xsl:value-of select="$par1"/>" 
      and "<xsl:value-of select="$par2"/>"</p>
    </xsl:template>
</xsl:stylesheet>