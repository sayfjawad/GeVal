<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xml="http://www.w3.org/XML/1998/namespace">
<xsl:output method="xml" indent="no"/>

<xsl:param name="code"/>
<xsl:param name="omschrijving"/>

<xsl:template match="/">
	<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
		<soap:Body>
            <soap:Fault>
                <soap:Code>
                    <soap:Value>soap:<xsl:value-of select="$code"/></soap:Value>
                </soap:Code>
                <soap:Reason>
                    <soap:Text xml:lang="nl"><xsl:value-of select="$omschrijving"/></soap:Text>
                </soap:Reason>
                <soap:Detail>
                    <xsl:apply-templates />
                </soap:Detail>
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