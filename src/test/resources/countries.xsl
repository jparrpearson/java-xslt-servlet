<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xsl xsd">

  <xsl:output method="html" indent="yes" />

  <xsl:param name="note" />

  <xsl:template match="/">
    <html>
      <body>
        <h1>Countries</h1>
        <table style="border: 1px solid black; border-collapse: collapse; text-align: left;" cellpadding="10px">
          <thead>
            <tr style="color: white; background-color: green;">
              <th>Name</th>
              <th>Capital</th>
              <th>Code</th>
            </tr>
          </thead>
          <tbody>
            <xsl:apply-templates />
          </tbody>
        </table>
        <xsl:if test="$note">
          <p>Note: <xsl:value-of select="$note" /></p>
        </xsl:if>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="country">
    <tr>
      <td><xsl:value-of select="name" /></td>
      <td><xsl:value-of select="capital" /></td>
      <td><xsl:value-of select="@code" /></td>
    </tr>
  </xsl:template>

</xsl:stylesheet>