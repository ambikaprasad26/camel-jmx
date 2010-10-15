<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:jmx="urn:org.apache.camel.component:jmx"
    exclude-result-prefixes="jmx">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="jmx:timestamp"/>
    <xsl:template match="jmx:dateTime"/>
    
</xsl:stylesheet>
