# Patent Document Parser

All patent document formats from 1976 to current parse into a single common Patent model.  Field validation is applied to many fields, where invalid values and their document fragment are logged.  Flexibility and completeness is stressed over efficiency. 

## Formats

| Format    | Prefix | Years       | Revisions  |  Documentation |
| :-------- | :-- | ------------| :-------------------:| :-------------: |
| Green Book <br> &nbsp; (freetext key values; multi-line indented values) | pftaps | 1976-2001 | revisions until 1997 | <a href="http://www.uspto.gov/sites/default/files/products/PatentFullTextAPSGreenBook-Documentation.pdf"> Greenbook Documentation </a> |
| Red Book SGML | pg | 2001-2004 | ST32-US-Grant-025xml.dtd | <a href="http://www.uspto.gov/sites/default/files/products/PatentGrantSGMLv19-Documentation.pdf">Redbook SGML v1.9</a> <br> <a href="http://www.uspto.gov/sites/default/files/products/PatentGrantSGMLv24-Documentation.pdf">Redbook SGML v2.4</a> <br><br> <a href="http://www.wipo.int/export/sites/www/standards/en/pdf/03-32-01.pdf">WIPO ST. 32 - SGML Standard</a>|
| Red Book PAP XML | pa | 2002-2004 | Published Patent Applications (PAP) from 2002-2004 <li> pap-v15-2001-01-31.dtd <li> pap-v16-2002-01-01.dtd |
| Red Book XML | ipg</br>ipa | 2004-Current | <B>Grants:</B></br></br> <li>us-patent-grant-v40-2004-12-02.dtd <li> us-patent-grant-v41-2005-08-25.dtd <li> us-patent-grant-v42-2006-08-23.dtd <li> us-patent-grant-v43-2012-12-04.dtd <li> us-patent-grant-v44-2013-05-16.dtd <li> us-patent-grant-v45-2014-04-03.dtd <hr><B>Applications:</B></br></br><li> us-patent-application-v40-2004-12-02.dtd <li> us-patent-application-v41-2005-08-25.dtd <li> us-patent-application-v42-2006-08-23.dtd <li> us-patent-application-v43-2012-12-04.dtd <li> us-patent-application-v44-2014-04-03.dtd|  <a href="http://www.uspto.gov/learning-and-resources/xml-resources">Redbook XML Documentation</a> <br><br> <a href="http://www.wipo.int/export/sites/www/standards/en/pdf/03-36-01.pdf">WIPO ST. 36 - XML Standard</a> |

## Redbook XML variations/fixes/normalization/improvements
Short list of some of the XML variations handled and improvements made by the Patent Document Parser

| Field           |  Description       |
| :-------------- | ---------------------------------- |
| parties | variation: us-parties |
| applicant | variation: us-parties/us-applicants/us-applicant |
| references-cited | variation: us-references-cited |
| citation | variation: us-citation |
| inventor | variation: Applicant with attribute "app-type" value "applicant-inventor" |
| assignee | variation: Applicant with attribute "applicant-authority-category" value "assignee" |
| applicant | missing address with attribute "applicant-authority-category value "assignee" ; read address from matching assignee |
| address/street | variation: address-1 address-2
| agent | fix: if missing use "correspondence-address" field |
| description | fix to corresponds with non-xml patent versions, improvement since individual sections are often searched on: break description into individual sections by XML Processing Instructions |
| claim | improvement: identify independent and dependent claims; capture dependent claim hierarchy |
| IPC classification | variations: classification-ipc and classification-ipcr, first flat other separated in sections |
| classification | normalization: CPC, IPC and USPC patent classifications |
| documentId / patentId | normalization; including removing leading 0 padding, currently added to patent ids with length less than 8 digits, in the near future patent ids may increase to 13 digits |
| country | improvement: mapping of country codes to country name, current and historic codes used before 1978 or individual codes dropped or changed since |
| patent title and all names | normalization; inconsistent case, normalize case |
| Org Names | parse suffixes and short name ; save variations as synonyms |
| address and name | not-fixed, lookout for switched value errors: within name the first-name and last-name or middle name switched; within address the country and state switched ; farther back in time more likely to see these data errors. Older Greenbook patents sometimes have first name or last name switched with middle name (presented as an initial), making searching by a person's name more difficult |
| description | not-fixed, Node elements within the description fields have "id" and "num" attributes which can be incorrect with duplicates, since they are entered by humans, by the filing law firms. Data Scientist may wish to create their own id and number for these data elements i.e. (heading \\| p \\| li \\| table). The num attribute is more likely to be off, since it is not as visbile to the examiner. |

## Also Parses
<ul>
<li><a href=" http://www.uspto.gov/sites/default/files/products/AIA_CPC_XML_Documentation.pdf">CPC Classification XML<a/></li>
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
         

## Example Usage:

<a href="https://github.com/USPTO/PatentPublicData/blob/master/BulkDownloader/src/main/java/gov/uspto/bulkdata/example/Example.java">Example.java</a>
<br/><br/>
<a href="https://github.com/USPTO/PatentPublicData/blob/master/BulkDownloader/src/main/java/gov/uspto/bulkdata/example/Example2.java">Example2.java</a>

