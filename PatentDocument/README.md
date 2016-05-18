# Patent Document Parser

All patent document formats parse into a single common Patent model.  Field validation is applied to many fields, where invalid values and their document fragment are logged.  Flexibility and completeness is stressed over efficiency.

## Patent Document Formats

| Format           | Years        | Revisions  |  Documentation |
| :-------------- | ------------| :-------------------:| :-------------: |
| Green Book <br> &nbsp; (freetext key values; multi-line indented values) | 1976-2001 | revisions until 1997 | <a href="http://www.uspto.gov/sites/default/files/products/PatentFullTextAPSGreenBook-Documentation.pdf"> Greenbook Documentation </a> |
| Red Book SGML | 2001-2004 | ST32-US-Grant-025xml.dtd | <a href="http://www.uspto.gov/sites/default/files/products/PatentGrantSGMLv19-Documentation.pdf">Redbook SGML v1.9</a> <br> <a href="http://www.uspto.gov/sites/default/files/products/PatentGrantSGMLv24-Documentation.pdf">Redbook SGML v2.4</a> <br><br> <a href="http://www.wipo.int/export/sites/www/standards/en/pdf/03-32-01.pdf">WIPO ST. 32 - SGML Standard</a>|
| Red Book PAP XML | 2002-2004 | Published Patent Applications (PAP) from 2002-2004 <li> pap-v15-2001-01-31.dtd <li> pap-v16-2002-01-01.dtd |
| Red Book XML | 2004-Current | <B>Grants:</B></br></br> <li>us-patent-grant-v40-2004-12-02.dtd <li> us-patent-grant-v41-2005-08-25.dtd <li> us-patent-grant-v42-2006-08-23.dtd <li> us-patent-grant-v43-2012-12-04.dtd <li> us-patent-grant-v44-2013-05-16.dtd <li> us-patent-grant-v45-2014-04-03.dtd <hr><B>Applications:</B></br></br><li> us-patent-application-v40-2004-12-02.dtd <li> us-patent-application-v41-2005-08-25.dtd <li> us-patent-application-v42-2006-08-23.dtd <li> us-patent-application-v43-2012-12-04.dtd <li> us-patent-application-v44-2014-04-03.dtd|  <a href="http://www.uspto.gov/learning-and-resources/xml-resources">Redbook XML Documentation</a> <br><br> <a href="http://www.wipo.int/export/sites/www/standards/en/pdf/03-36-01.pdf">WIPO ST. 36 - XML Standard</a> |

## Also Parses
<ul>
<li><a href=" http://www.uspto.gov/sites/default/files/products/AIA_CPC_XML_Documentation.pdf">CPC Patent Classification Definition XML<a/></li>
</ul>

## Validate and Normalize
| Field           |  Description        |
| :-------------- | ---------------------------------- |
| Patent          | instantiated with DocumentId |
| Assignee Type   | valid USPTO assigne type (00 through 09)|
| CountryCode     | <a href="http://www.wipo.int/export/sites/www/standards/en/pdf/03-03-01.pdf">WIPO standard ST.3. two-letter codes</a> |
| Classification  | parse and normalize Classification (USPC, IPC, CPC) |
| Date            | date format yyyyMMdd |
| Address         | must have a country |
