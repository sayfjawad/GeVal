<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="no" />

    <xsl:template match="/">
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <soap:Body>
                <xsl:apply-templates />
            </soap:Body>
        </soap:Envelope>
    </xsl:template>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*" />
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>