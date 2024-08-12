<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="no"/>

<xsl:param name="code"/>
<xsl:param name="omschrijving"/>

<xsl:template match="/">
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Body>
            <soap:Fault>
                <faultcode>soap:<xsl:value-of select="$code"/></faultcode>
                <faultstring><xsl:value-of select="$omschrijving"/></faultstring>
                <detail>
                    <xsl:apply-templates />
                </detail>
            </soap:Fault>
        </soap:Body>
    </soap:Envelope>
</xsl:template>

<xsl:template match="node()|@*">
    <xsl:copy>
        <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
</xsl:template>

</xsl:stylesheet>