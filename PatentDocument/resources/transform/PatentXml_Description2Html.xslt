<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ******************************************************
     *
	 *  Transform Patent XML description field into HTML
	 *
	 *  Author: Brian G. Feldman (brian.feldman@uspto.gov)
	 *
	 * Reference:
	 *    Oasis CALS XML Exchange Table Model 19990315
	 *	  https://www.oasis-open.org/specs/tm9901.htm
	 *    https://www.loc.gov/ead/tglib/element_index.html
	 *
	 *    Mathematical Markup Language (W3C MathML 2.0)
	 *    https://www.w3.org/TR/MathML2/
	 *    https://developer.mozilla.org/en-US/docs/Web/MathML
     *
     ****************************************************** -->
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="2.0"
  >

  <xsl:output
  	 method="html"
  	 version="5.0"
  	 encoding="UTF-8"
  	 indent="yes" />

 <xsl:strip-space elements="*"/>
 <xsl:preserve-space elements="p pre td"/>
 
   <xsl:template match="/">
    <html>
      <head>
        <meta charset="UTF-8"/>       
    	<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:400,400italic,500,500italic,700"/>
    	<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Product+Sans"/>
       	<link rel="stylesheet" type="text/css" href="descstyle.css"/>
		<!--
		<style type="text/css">
		body, table {font-family: 'Roboto', sans-serif;background-color:#fff;color:#333;font-size:13px;line-height:20px}
		body ::selection{background-color: #C6DAFC;color: #333;}
		p{line-height:20px;text-indent:8px;display:block;word-break:break-word; -webkit-margin-before:1em; -webkit-margin-after:1em;
		-webkit-margin-start:0px; -webkit-margin-end:0px;}
		table.pgwide-1{width:100%}
		table.pgwide-0{width:100%}
		table {border-collapse:collapse;}
		table.border-all{box-shadow:0 2px 3px rgba(0,0,0,0.06);}
		table.border-sides{box-shadow:0 0 3px rgba(0,0,0,0.06);}
		table.border-topbot{box-shadow:0 3px 0 rgba(0,0,0,0.06);}
		td, th {padding-left:12px;padding-right: 12px;}
		th {background-color:#f1f1f1f1;line-height:23px;}
  	 	.border-all{border:1px solid rgba(150,150,150,0.3);border-bottom:1px solid rgba(125,125,125,0.3);}
   	 	.border-none{border: none;}
   	 	.border-topbot{border-top:1px solid rgba(150,150,150,0.3);border-bottom:1px solid rgba(125,125,125,0.3);}
   	 	.border-sides{border-left:1px solid rgba(150,150,150,0.4);border-right:1px solid rgba(150,150,150,0.4);}
   	 	.border-top{border-top:1px solid rgba(150,150,150,0.3);}
   	 	.border-bottom{border-bottom:1px solid rgba(125,125,125,0.3);}
   	 	.border-undefined{border-collapse: collapse;}

   		h2.level-1, h4.level-1{text-indent:12px;}
   		h2.level-2, h4.level-2{text-indent:24px;}
   		h2.level-3, h4.level-3{text-indent:36px;}
   		span.figref, span.clmref, span.patcite, span.nplcite{font-weight:bold;}

		entry{display:table-column;}
   		sup2{vertical-align:65%;font-size:smaller;}
   		sub2{vertical-align:-65%;font-size:smaller;}
   		ul.ul-dash{list-style:none;margin-left:0;padding-left:1em;}
   		ul.ul-dash > li:before {display:inline-block;content:"-";width:1em;margin-left:-1em;}
   		o{text-decoration:overline;}
   		o.single{text-decoration:overline;}
   		u.single{text-decoration:underline;text-decoration-style:solid;}
   		u.double, o.double{text-decoration-style:double;}
   		u.dots, o.dots{text-decoration-style:dotted;}
   		u.dash, o.dash{text-decoration-style:dashed;}
   		smallcaps{font-variant: small-caps;}
   		smallcapsFAKE{font-size: smaller;text-transform:uppercase;}
   		</style>
		-->
      </head>
      <body>
		<xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

<!-- *********** HEADINGS *********** -->
  <xsl:template match="heading">
	<xsl:element name="h2">
		<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
		<xsl:if test="not(@id)">
			<xsl:attribute name="id">H-<xsl:value-of select="format-number(position(), '0000')"/></xsl:attribute>
		</xsl:if>
		<!--Heading.  Level attribute determines indentation and style.-->
		<xsl:attribute name="class">
			<xsl:text>level-</xsl:text>
			<xsl:value-of select="@level|@lvl"/>
			<xsl:if test="not(@level|@lvl)">
				<xsl:value-of select="count(ancestor::*)+1"/>
			</xsl:if>
		</xsl:attribute>
		<xsl:apply-templates/>
	</xsl:element>
  </xsl:template>

  <xsl:template match="p[starts-with(@id,'h-')]">
	<!-- <xsl:if test="string-length(text()) &lt; 100"> -->
		<xsl:element name="h4">
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			<xsl:attribute name="class">
				<xsl:text>level-</xsl:text>
				<xsl:value-of select="@level|@lvl"/>
				<xsl:if test="not(@level|@lvl)">
					<xsl:value-of select="count(ancestor::*)+1"/>
				</xsl:if>
			</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	<!-- </xsl:if> -->
  </xsl:template>

<!-- *********** PARAGRAPH *********** -->
  <xsl:template match="p">
    <xsl:copy>
	    <xsl:copy-of select="@*"/>
	    <!--  
	    <xsl:attribute name="genId">  
          <xsl:text>p-</xsl:text>
         // <xsl:number level='any' format="00001" count='p' /> 
          <xsl:number level='any' format="00001" value="position()" />
        </xsl:attribute>
         <xsl:attribute name="genNum">
          <xsl:number level='any' format="00001" count='p|heading|li'/>
        </xsl:attribute>
        -->
    	<xsl:apply-templates/>
  	</xsl:copy>
  </xsl:template>

<!-- *********** LISTS *********** -->
<xsl:template match="ul">
	<xsl:element name="ul">
		<xsl:attribute name="id">
			<xsl:value-of select="@id"/>
		</xsl:attribute>
		<xsl:choose>
			<xsl:when test="@list-style = 'none'">
				<xsl:attribute name="style"><xsl:text>list-style-type:none</xsl:text></xsl:attribute>
			</xsl:when>
			<xsl:when test="@list-style = 'bullet'">
				<xsl:attribute name="style"><xsl:text>list-style-type:disc</xsl:text></xsl:attribute>
			</xsl:when>
			<xsl:when test="@list-style = 'dash'">
				<xsl:attribute name="class"><xsl:text>ul-dash</xsl:text></xsl:attribute>
			</xsl:when>
		</xsl:choose>
		<!-- 
		<xsl:if test="@list-style">
			<xsl:attribute name="class"><xsl:text>ul-</xsl:text><xsl:value-of select="@list-style"/></xsl:attribute>
		</xsl:if>
		-->
		<xsl:apply-templates select="li"/>
	</xsl:element>
</xsl:template>

<xsl:template match="ol">
	<xsl:element name="ol">
		<xsl:attribute name="id">
			<xsl:value-of select="@id"/>
		</xsl:attribute>
		<xsl:if test="@style|@ol-style">
			<xsl:attribute name="type"><xsl:value-of select="@style|@ol-style"/></xsl:attribute>
		</xsl:if>
		<xsl:apply-templates select="li"/>
	</xsl:element>
</xsl:template>

<xsl:template match="li">
 	<xsl:copy>
	    <xsl:copy-of select="@*"/>
	    <xsl:apply-templates/>
	</xsl:copy>
</xsl:template>

<!-- *********** TABLE *********** -->
<xsl:template match="tables">
	<xsl:apply-templates select="table"/>
</xsl:template>
<xsl:template match="table">
	<xsl:element name="table">
		<xsl:attribute name="id"><xsl:value-of select="ancestor::tables[1]/@id"/></xsl:attribute>
		<xsl:attribute name="num"><xsl:value-of select="ancestor::tables[1]/@num"/></xsl:attribute>
		
		<!-- 
		<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
		<xsl:attribute name="id">TBL-<xsl:value-of select="format-number(position(), '0000')"/></xsl:attribute>
		-->

		<xsl:variable name="classes">
			<!-- for XHTML pgwide of 0 or 1 sets to 100% page width. -->
			<xsl:if test="@pgwide"><xsl:text> pgwide-</xsl:text><xsl:value-of select="@pgwide"/></xsl:if>
			<xsl:if test="not(@pgwide)"> pgwide-undefined</xsl:if>
			<!-- @frame [all,none,topbot,sides,top,bottom] if missing border-undefined -->
			<xsl:if test="@frame"><xsl:text> border-</xsl:text><xsl:value-of select="@frame"/></xsl:if>
			<xsl:if test="not(@frame)"><xsl:text> border-undefined</xsl:text></xsl:if>
		</xsl:variable>

		<xsl:attribute name="class">
        	<xsl:value-of select="normalize-space($classes)"/>
      	</xsl:attribute>

		<xsl:for-each select="tgroup">
			<xsl:element name="colgroup">
				<xsl:for-each select="colspec">	
					<xsl:element name="col">
						<xsl:attribute name="width"><xsl:value-of select="@colwidth"/></xsl:attribute>
						<xsl:attribute name="align"><xsl:value-of select="@align"/></xsl:attribute>
						<xsl:text> </xsl:text>
					</xsl:element>
				</xsl:for-each>
			</xsl:element>

			<xsl:for-each select="thead">
				<xsl:element name="thead">
					<xsl:for-each select="row">
						<xsl:element name="tr">
							<xsl:for-each select="entry">
								<xsl:element name="th">
									<xsl:if test="@align">
										<xsl:attribute name="align"><xsl:value-of select="@align"/></xsl:attribute>
									</xsl:if>

									<!-- namest , nameend colspan="2" -->
									<xsl:variable name="namest" select="@namest"/>
	    							<xsl:variable name="nameend" select="@nameend"/>
							    	<xsl:variable name="namestPos" select="count(ancestor::tgroup/colspec[@colname=$namest]/preceding-sibling::colspec)"/>
	    							<xsl:variable name="nameendPos" select="count(ancestor::tgroup/colspec[@colname=$nameend]/preceding-sibling::colspec)"/>
	    							<xsl:variable name="span" select="$nameendPos - $namestPos + 1"/>
	    							<xsl:if test="$span &gt; 1">
										<xsl:attribute name="colspan"><xsl:value-of select="$span"/></xsl:attribute>
									</xsl:if>

									<xsl:value-of select="."/>
								</xsl:element>
							</xsl:for-each>
						</xsl:element>
					</xsl:for-each>
				</xsl:element>
			</xsl:for-each>
		
			<xsl:for-each select="tbody">
				<xsl:element name="tbody">
					<xsl:if test="@valign">
						<xsl:attribute name="valign"><xsl:value-of select="@valign"/></xsl:attribute>
					</xsl:if>
				
					<xsl:for-each select="row">
						<xsl:element name="tr">
							<xsl:for-each select="entry">
								<xsl:element name="td">
									<xsl:if test="@morerows">
										<xsl:attribute name="rowspan"><xsl:value-of select="@morerows+1"/></xsl:attribute>
									</xsl:if>
									
									<!-- HTML5  style="width:100px" 
									<xsl:attribute name="style">width:<xsl:value-of select="ancestor::tgroup/colspec[@colname=current()/@colname]/@colwidth"/></xsl:attribute>
									-->
									<xsl:variable name="col_spec_name" select="ancestor::tgroup/colspec[@colname=current()/@colname]"/>
									
									<!-- 
									<xsl:variable name="col_spec_num" select="ancestor::tgroup/colspec[@colnum=position()/@colnum]"/>
									-->

									<xsl:choose>
										<xsl:when test="@valign"><xsl:attribute name="valign"><xsl:value-of select="@valign"/></xsl:attribute></xsl:when>
										<xsl:when test="$col_spec_name/@valign"><xsl:attribute name="valign"><xsl:value-of select="$col_spec_name/@valign"/></xsl:attribute></xsl:when>
									</xsl:choose>

									<xsl:choose>
										<xsl:when test="$col_spec_name/@colwidth"><xsl:attribute name="width"><xsl:value-of select="$col_spec_name/@colwidth"/></xsl:attribute></xsl:when>
										<!-- <xsl:when test="$col_spec_num/@colwidth"><xsl:attribute name="width"><xsl:value-of select="$col_spec_num/@colwidth"/></xsl:attribute></xsl:when> -->
									</xsl:choose>

									<xsl:choose>
										<xsl:when test="@align"><xsl:attribute name="align"><xsl:value-of select="@align"/></xsl:attribute></xsl:when>
										<xsl:when test="$col_spec_name/@align">
											<xsl:attribute name="align"><xsl:value-of select="$col_spec_name/@align"/></xsl:attribute>
										</xsl:when>
										<!-- <xsl:when test="$col_spec_num/@align"><xsl:attribute name="align"><xsl:value-of select="$col_spec_name/@align"/></xsl:attribute></xsl:when> -->
									</xsl:choose>

									<xsl:choose>
										<xsl:when test="(@rowsep='1') and (@colsep='1')">
											<xsl:attribute name="class"><xsl:text>border-all</xsl:text></xsl:attribute>
										</xsl:when>
										<xsl:when test="(@rowsep='1') and (@colsep='0')">
											<xsl:attribute name="class"><xsl:text>border-bottom</xsl:text></xsl:attribute>
										</xsl:when>
										<xsl:when test="(@rowsep=0) and (@colsep=1)">
											<xsl:attribute name="style"><xsl:text>border-sides</xsl:text></xsl:attribute>
										</xsl:when>
										<xsl:otherwise>
											<xsl:attribute name="class"><xsl:text>border-none</xsl:text></xsl:attribute>
										</xsl:otherwise>
									</xsl:choose>

									<!-- calculate colspan from namest and nameend -->
									<xsl:variable name="namest" select="@namest"/>
	    							<xsl:variable name="nameend" select="@nameend"/>
							    	<xsl:variable name="namestPos" select="count(ancestor::tgroup/colspec[@colname=$namest]/preceding-sibling::colspec)"/>
	    							<xsl:variable name="nameendPos" select="count(ancestor::tgroup/colspec[@colname=$nameend]/preceding-sibling::colspec)"/>
	    							<xsl:variable name="span" select="$nameendPos - $namestPos + 1"/>
	    							<xsl:if test="$span &gt; 1">
										<xsl:attribute name="colspan"><xsl:value-of select="$span"/></xsl:attribute>
									</xsl:if>

									<xsl:value-of select="."/>
								</xsl:element>
							</xsl:for-each>
						</xsl:element>
					</xsl:for-each>
				</xsl:element>
			</xsl:for-each>
			
		</xsl:for-each>
	</xsl:element>
</xsl:template>

<!-- *********** MATH *********** -->
<xsl:template match="maths">
	<xsl:apply-templates select="math"/>
</xsl:template>
<xsl:template match="math">
		<xsl:element name="span">
			<xsl:attribute name="id"><xsl:value-of select="ancestor::maths[1]/@id"/></xsl:attribute>
			<xsl:attribute name="num"><xsl:value-of select="ancestor::maths[1]/@num"/></xsl:attribute>
			<xsl:attribute name="class">math</xsl:attribute>
   	  	    <xsl:attribute name="format">mathml</xsl:attribute>
		  	 <xsl:copy>
	               <xsl:apply-templates select="@*|node()" />
	         </xsl:copy>
		</xsl:element>
</xsl:template>
<xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
</xsl:template>

<!-- *********** TEXT FORMAT *********** -->
  <xsl:template match="bold">
	<xsl:element name="b"><xsl:apply-templates/></xsl:element>
  </xsl:template>

  <xsl:template match="sup2">
  	<!-- The browser will correctly display two nested sup elements as "sup2" (sup2 is always nested under a sup element) -->
	<!-- <xsl:element name="sup"><xsl:apply-templates/></xsl:element> -->
	 <xsl:copy>
	    <xsl:apply-templates/>
	</xsl:copy>
  </xsl:template>

  <xsl:template match="sub2">
  	 <xsl:copy>
	    <xsl:apply-templates/>
	</xsl:copy>
	<!-- <xsl:element name="sub"><xsl:apply-templates/></xsl:element> -->
  </xsl:template>

  <xsl:template match="o">
    <!-- Overscore/Overline: ostyle:(single | double | dots | dash | leftarrow | rightarrow | leftrightarrow); single is default. -->
	<xsl:element name="o">
		<xsl:choose>
			<xsl:when test="not(@ostyle)">
				<xsl:attribute name="class"><xsl:text>single</xsl:text></xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="class"><xsl:value-of select="@ostyle"/></xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates/>
	</xsl:element>
  </xsl:template>

   <xsl:template match="u">
    <!-- underline: style:(single | double | dots | dash); single is default. -->
    	<xsl:element name="u">
			<xsl:choose>
				<xsl:when test="@style">
					<xsl:attribute name="class"><xsl:value-of select="@style"/></xsl:attribute>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates/>
		</xsl:element>
   </xsl:template>

    <xsl:template match="smallcaps">
	 	<xsl:copy>
		    <xsl:apply-templates/>
		</xsl:copy>
  </xsl:template>

  <xsl:template match="br">
	<xsl:text disable-output-escaping="yes"><![CDATA[<br/>]]></xsl:text>
  </xsl:template>

<!-- *********** Processing Instructions *********** -->
<xsl:template match="//processing-instruction()">
	<xsl:choose>
		<xsl:when test="contains(., ' end=&quot;lead&quot;')">		
			<xsl:text disable-output-escaping="yes"><![CDATA[<span class="]]></xsl:text>
			<xsl:value-of select="name()"/>
			<xsl:text disable-output-escaping="yes"><![CDATA[">]]></xsl:text>
		</xsl:when>
		<xsl:when test="contains(., ' end=&quot;tail&quot;')">
			<xsl:text disable-output-escaping="yes"><![CDATA[</span>]]></xsl:text>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<!-- *********** REFERENCES *********** -->
  <xsl:template match="figref|claim-ref">
	  <xsl:element name="span">
	    <xsl:attribute name="class">	    
		    <xsl:choose>
		     	<xsl:when test="name() = 'claim-ref'">clmref</xsl:when>
		    	<xsl:otherwise><xsl:value-of select="name()"/></xsl:otherwise>
		    </xsl:choose>
	    </xsl:attribute>

		<xsl:attribute name="idref">
			<xsl:value-of select="."/>
		</xsl:attribute>

		<xsl:attribute name="id">
			<xsl:value-of select="@id"/>
		</xsl:attribute>
		 <xsl:attribute name="id">FR-<xsl:value-of select="format-number(position(), '0000')"/></xsl:attribute>
		<xsl:apply-templates/>
	</xsl:element>
 </xsl:template>

  <xsl:template match="patcit|nplcit|crossref">
	  <xsl:element name="span">
	  	 <xsl:attribute name="class">
		    <xsl:choose>
		     	<xsl:when test="name() = 'patcit'">patcite</xsl:when>
		     	<xsl:when test="name() = 'nplcit'">nplcite</xsl:when>
		    	<xsl:otherwise><xsl:value-of select="name()"/></xsl:otherwise>
		    </xsl:choose>
	    </xsl:attribute>
		<xsl:apply-templates/>
	</xsl:element>
 </xsl:template>


</xsl:stylesheet>

  