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
| address/street | variation: address-1 address-2
| agent | fix: if missing use "correspondence-address" field |
| description | fix to corresponds with non-xml patent versions, improvement since individual sections are often searched on: break description into individual sections by XML Processing Instructions |
| claim | improvement: identify independent and dependent claims; capture dependent claim hierarchy |
| IPC classification | variations: classification-ipc and classification-ipcr, first flat other separated in sections |
| classification | normalization: CPC, IPC and USPC patent classifications |
| documentId / patentId | normalization; including removing leading 0 padding, currently added to patent ids with length less than 8 digits, in the near future patent ids may increase to 13 digits |
| country | improvement: mapping of country codes to country name, current and historic codes used before 1978 or individual codes dropped or changed since |
| address and name | not-fixed, lookout for switched value errors: within name the first-name and last-name or middle name switched; within address the country and state switched ; farther back in time more likely to see these data errors. Older Greenbook patents sometimes have first name or last name switched with middle name (presented as an initial), making searching by a person's name more difficult |


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


## CLI Tool to export all Patents as indexable JSON:
    Single Line Per Record: 
       gov.uspto.patent.TransformerCli --input="ipa_corpusApps_2005.zip"
       
    Single File Per Record:
       gov.uspto.patent.TransformerCli --input="ipa_corpusApps_2005.zip" --outBulk=false

     Options:
         --input="FILE.zip"    Patent Bulk Zip
         --outdir="output"     Output Directory      
         --outBulk=true        Single file, JSON record per line
         --limit=100           Total Record Limit
         --flat=false          Denormalized/Flat JSON or Objecet Hierarchy
         --pettyPrint=true     Pretty Print JSON
         --stdout=true         Write to Terminal instead of file 
         

## Example Usage:
```JAVA
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import gov.uspto.common.file.filter.FileFilterChain;
import gov.uspto.common.file.filter.SuffixFileFilter;
import gov.uspto.patent.bulk.DumpFileAps;
import gov.uspto.patent.bulk.DumpFileXml;
import gov.uspto.patent.bulk.DumpReader;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.serialize.DocumentBuilder;
import gov.uspto.patent.serialize.JsonMapper;
import gov.uspto.patent.serialize.JsonMapperFlat;

public class ReadBulkPatentZip {

    public static void main(String... args) throws IOException, PatentReaderException {
        
        File inputFile = new File(args[0]);
        int skip = 100;
        int limit = 1;
        boolean flatJson = false;
        boolean jsonPrettyPrint = true;
        boolean writeFile = false;

        PatentDocFormat patentDocFormat = new PatentDocFormatDetect().fromFileName(inputFile);

        DumpReader dumpReader;
        switch (patentDocFormat) {
        case Greenbook:
            dumpReader = new DumpFileAps(inputFile);
            break;
        default:
            dumpReader = new DumpFileXml(inputFile);
            FileFilterChain filters = new FileFilterChain();
            //filters.addRule(new PathFileFilter(""));
            filters.addRule(new SuffixFileFilter("xml"));
            dumpReader.setFileFilter(filters);
        }

        dumpReader.open();
        if (skip > 0) {
            dumpReader.skip(skip);
        }

        DocumentBuilder<Patent> json;
        if (flatJson) {
            json = new JsonMapperFlat(jsonPrettyPrint, false);
        } else {
            json = new JsonMapper(jsonPrettyPrint, false);
        }

        for (int i = 1; dumpReader.hasNext() && i <= limit; i++) {
            String xmlDocStr = (String) dumpReader.next();
            try (PatentReader patentReader = new PatentReader(patentDocFormat)) {
                Patent patent = patentReader.read(xmlDocStr);
                String patentId = patent.getDocumentId().toText();
                
                System.out.println(patentId);
                //System.out.println("Patent Object: " + patent.toString());

                Writer writer;
                if (writeFile) {
                    writer = new FileWriter(patentId + ".json");
                } else {
                    writer = new StringWriter();
                }

                try {
                    json.write(patent, writer);
                    if (!writeFile) {
                        System.out.println("JSON: " + writer.toString());
                    }
                } catch (IOException e) {
                    System.err.println("Failed to write file for: " + patentId + "\n" + e.getStackTrace());
                } finally {
                    writer.close();
                }
            }
        }

        dumpReader.close();
    }
}
```
