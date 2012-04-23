<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xhtml="http://www.w3.org/1999/xhtml" version="1.0">
	<xsl:output encoding="ISO-8859-1" method="html"
		omit-xml-declaration="no" />

<!-- 

All textual output is XML-encoded for XSS-security reasons

 -->

	<xsl:template match="/">
		<html>
			<head>
				<title>
					Model
					<xsl:value-of select="/xmodel/@xid" />
					[
					<xsl:value-of select="/xmodel/@revision" />
					]
				</title>
				<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
				<link href="/s/xyadmin.css" rel="stylesheet" type="text/css" />
			</head>
			<body>
				<div>
					<xsl:apply-templates select="*" />
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="node()">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="xmodel">
		<div class="xmodel">
			<span class="xid">
				<xsl:value-of select="@xid" />
			</span>
			<span class="rev">
				<xsl:value-of select="@revision" />
			</span>
			<ol>
				<xsl:apply-templates select="*">
					<xsl:sort select="@xid" />
				</xsl:apply-templates>
			</ol>
		</div>
	</xsl:template>

	<xsl:template match="xobject">
		<li>
			<div class="xobject">
				<span class="xid">
					<xsl:value-of select="@xid" />
				</span>
				<span class="rev">
					<xsl:value-of select="@revision" />
				</span>
				<table>
					<tr>
						<th>Field</th>
						<th>Value</th>
					</tr>
					<xsl:for-each select="xfield">
						<xsl:sort select="@xid" />
						<tr>
							<xsl:apply-templates select="." />
						</tr>
					</xsl:for-each>
				</table>
			</div>
		</li>
	</xsl:template>

	<xsl:template match="xfield">
		<td>
			<div class="xfield">
				<span class="xid">
					<xsl:value-of select="@xid" />
				</span>
				<span class="rev">
					<xsl:value-of select="@revision" />
				</span>
			</div>
		</td>
		<td>
			<xsl:apply-templates select="*" />
		</td>
	</xsl:template>

	<!-- Containers -->
	<xsl:template match="*[contains(name(.),'List')]">
		<span class="type">
			<xsl:value-of select="name(.)" />
			:
		</span>
		<ol>
			<xsl:for-each select="*">
				<li class="value">
					<xsl:apply-templates select="." />
				</li>
			</xsl:for-each>
		</ol>
	</xsl:template>

	<xsl:template match="*[contains(name(.),'Set')]">
		<span class="type">
			<xsl:value-of select="name(.)" />
			:
		</span>
		<ul>
			<xsl:for-each select="*">
				<li class="value">
					<xsl:apply-templates select="." />
				</li>
			</xsl:for-each>
		</ul>
	</xsl:template>

	<!-- Items -->
	<xsl:template match="*[@isNull]">
		<span class="null">NULL</span>
		<span class="type">
			<xsl:value-of select="name(.)" />
		</span>
	</xsl:template>

	<xsl:template match="*">
		<xsl:value-of select="." />
		<span class="type">
			<xsl:value-of select="name(.)" />
		</span>
	</xsl:template>

    <xsl:template match="*" mode="escape">
        <!-- Begin opening tag -->
        <xsl:text>&lt;</xsl:text>
        <xsl:value-of select="name()"/>

        <!-- Namespaces -->
        <xsl:for-each select="namespace::*">
            <xsl:text> xmlns</xsl:text>
            <xsl:if test="name() != ''">
                <xsl:text>:</xsl:text>
                <xsl:value-of select="name()"/>
            </xsl:if>
            <xsl:text>='</xsl:text>
            <xsl:call-template name="escape-xml">
                <xsl:with-param name="text" select="."/>
            </xsl:call-template>
            <xsl:text>'</xsl:text>
        </xsl:for-each>

        <!-- Attributes -->
        <xsl:for-each select="@*">
            <xsl:text> </xsl:text>
            <xsl:value-of select="name()"/>
            <xsl:text>='</xsl:text>
            <xsl:call-template name="escape-xml">
                <xsl:with-param name="text" select="."/>
            </xsl:call-template>
            <xsl:text>'</xsl:text>
        </xsl:for-each>

        <!-- End opening tag -->
        <xsl:text>&gt;</xsl:text>

        <!-- Content (child elements, text nodes, and PIs) -->
        <xsl:apply-templates select="node()" mode="escape" />

        <!-- Closing tag -->
        <xsl:text>&lt;/</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>&gt;</xsl:text>
    </xsl:template>

    <xsl:template match="text()" mode="escape">
        <xsl:call-template name="escape-xml">
            <xsl:with-param name="text" select="."/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="processing-instruction()" mode="escape">
        <xsl:text>&lt;?</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text> </xsl:text>
        <xsl:call-template name="escape-xml">
            <xsl:with-param name="text" select="."/>
        </xsl:call-template>
        <xsl:text>?&gt;</xsl:text>
    </xsl:template>

    <xsl:template name="escape-xml">
        <xsl:param name="text"/>
        <xsl:if test="$text != ''">
            <xsl:variable name="head" select="substring($text, 1, 1)"/>
            <xsl:variable name="tail" select="substring($text, 2)"/>
            <xsl:choose>
                <xsl:when test="$head = '&amp;'">&amp;amp;</xsl:when>
                <xsl:when test="$head = '&lt;'">&amp;lt;</xsl:when>
                <xsl:when test="$head = '&gt;'">&amp;gt;</xsl:when>
                <xsl:when test="$head = '&quot;'">&amp;quot;</xsl:when>
                <xsl:when test="$head = &quot;&apos;&quot;">&amp;apos;</xsl:when>
                <xsl:otherwise><xsl:value-of select="$head"/></xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="escape-xml">
                <xsl:with-param name="text" select="$tail"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>



	<!-- filterting interesting elements -->
	<xsl:template match="xhtml:div">
		<xsl:choose>
			<xsl:when test="@class|@id">
				<ul>
					<li>
						<xsl:value-of select="@class" />
						(id=
						<xsl:value-of select="@id" />
						)
						<br />
						<font color="lightgray">
							<xsl:value-of select="substring(.,0,30)" />
							...
							<xsl:value-of select="substring(.,string-length(.)-30,string-length(.))" />
						</font>
					</li>
					<xsl:apply-templates />
				</ul>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates />
			</xsl:otherwise>
		</xsl:choose>


	</xsl:template>

</xsl:stylesheet>