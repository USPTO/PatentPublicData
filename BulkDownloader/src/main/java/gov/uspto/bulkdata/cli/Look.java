package gov.uspto.bulkdata.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.MDC;

import gov.uspto.common.filter.FileFilterChain;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;
import gov.uspto.patent.bulk.DumpFileXml;
import gov.uspto.patent.bulk.DumpReader;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.serialize.DocumentBuilder;
import gov.uspto.patent.serialize.JsonMapperFlat;
import gov.uspto.patent.serialize.JsonMapperStream;
import gov.uspto.patent.serialize.PlainText;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Look is a CLI tool to a view a single patent document from Bulk Patent XML.
 *
 * View contents of listed fields
 * 	--source="download/ipa150101.zip" --limit 5 --skip 5 --fields=id,title
 * 
 * Dump a single Patent XML Document by location in zipfile; the 3rd document:
 * --source="download/ipa150305.zip" --num=3 --fields=xml --out=download/patent.xml
 * 
 * Dump a single Patent XML Document by ID (note it may be slow as it parse each document to check its id):
 * --source="download/ipa150305.zip" --id=3 --fields=xml --out=download/patent.xml
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Look {
	
    //private ContentStream contentStream;
	private String rawRecord;

    public void look(DumpReader dumpReader, int limit, Writer writer, String[] fields)
            throws PatentReaderException, IOException {

        PatentReader patentReader = new PatentReader(dumpReader.getPatentDocFormat());

        for (int i = 1; dumpReader.hasNext() && i <= limit; i++) {
			MDC.put("DOCID", dumpReader.getFile().getName() + ":" + dumpReader.getCurrentRecCount());

            try {
            	rawRecord = dumpReader.next();
            	//contentStream = dumpReader.next();
            } catch (NoSuchElementException e) {
                break;
            }

            if (fields.length == 1 && "raw".equalsIgnoreCase(fields[0]) || "rawparts".equalsIgnoreCase(fields[0])) {
                show(String.valueOf(dumpReader.getCurrentRecCount()+1), null, fields, writer);
            } else {
               //Patent patent = patentReader.read(contentStream);
               Patent patent = patentReader.read(new StringReader(rawRecord));
               show(String.valueOf(dumpReader.getCurrentRecCount()+1), patent, fields, writer);
            }
        }

        dumpReader.close();
    }

    public void look(DumpReader dumpReader, String docId, Writer writer, String[] fields)
            throws PatentReaderException, IOException {

        PatentReader patentReader = new PatentReader(dumpReader.getPatentDocFormat());

        while (dumpReader.hasNext()) {

            try {
            	rawRecord = dumpReader.next();;
            	//contentStream = dumpReader.next();
            } catch (NoSuchElementException e) {
                break;
            }

            //if (contentStream != null){
             //   Patent patent = patentReader.read(contentStream);
            if (rawRecord != null) {
            	Patent patent = patentReader.read(new StringReader(rawRecord));
                if (patent.getDocumentId().toText().equals(docId)) {
                    show(String.valueOf(dumpReader.getCurrentRecCount()+1), patent, fields, writer);
                    break;
                }
            }
        }

        dumpReader.close();
    }
   
    /**
     * STDOUT requested Patent fields.
     * 
     * @param patent
     * @param fields
     * @throws IOException 
     * 
     */
    private void show(String sourceLoc, Patent patent, String[] fields, Writer writer) throws IOException {
    	
        for (String field : fields) {
        	
        	String fieldName = field.toLowerCase();
        	if (fieldName.endsWith("s")) {
        		fieldName = fieldName.substring(0, fieldName.length()-1);
        	}

        	writer.write(sourceLoc);
        	writer.write(" ---------------------------\n");

            switch (fieldName) {
            case "fields":
            case "help":
            case "?":
            	writer.write("\nAvailable Patent Fields:\n\t");
            	writer.write(Arrays.toString(new PlainText().definedFields().toArray()));
            	writer.write("\n");
            	break;
            case "plaintext":
            case "text":
            	new PlainText().write(patent, writer);
                break;
            case "object":
            	writer.write(patent.toString());
                break;
            case "json":
            	writer.write("Patent JSON:\n");
            	JsonMapperStream fileBuilder = new JsonMapperStream(true);
        		fileBuilder.write(patent, writer);
        		break;
            case "jsonflat":
            case "flatjson":
            	writer.write("Patent JSON FLAT:\n");
        		DocumentBuilder<Patent> fileBuilder2 = new JsonMapperFlat(true, false);
        		fileBuilder2.write(patent, writer);
        		break;
            case "raw":
            	writer.write("Patent RAW:\n");
                //String prettyXml = prettyFormatXml(xmlDocStr);
                //writer.write(prettyXml + "\n");
            	
            	//IOUtils.copy(contentStream.getInputStream(), writer, "UTF-8");
            	
            	writer.write(rawRecord);
                break;
            case "rawpart":
            	writer.write("Patent RAW Parts:");
            	/*
            	for(String part: contentStream.getMarkedNames()) {
                    writer.write(part);
                	IOUtils.copy(contentStream.getInputStream(part), writer, "UTF-8");
            	}
            	*/
                break;
            case "id":
            	new PlainText("doc_id").write(patent, writer);
                break;
            case "family":
            case "related":
            	new PlainText("related_id").write(patent, writer);
                break;
            default:
            	if (PlainText.isDefinedField(fieldName)) {
            		new PlainText(fieldName).write(patent, writer);
            	} else {
            		System.err.println("Field Name not found " + field);
            	}
            	break;
            }
        }
        writer.flush();
    }

    /**
     * Pretty Print XML
     * 
     * @param xmlDocStr
     */
    private String prettyFormatXml(String xmlDocStr) {
        String result = null;
        try {

            org.dom4j.Document doc = DocumentHelper.parseText(xmlDocStr);
            StringWriter sw = new StringWriter();
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter xw = new XMLWriter(sw, format);
            xw.write(doc);
            xw.close();
            result = sw.toString();
            System.out.println(result);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static void main(String[] args) throws PatentReaderException, IOException {
        System.out.println("--- Start ---");

        OptionParser parser = new OptionParser() {
            {
                accepts("source").withRequiredArg().ofType(String.class).describedAs("zip file").required();
                accepts("fields").withOptionalArg().ofType(String.class)
                        .describedAs(
                                "comma seperated list of fields; options: [raw,xml,json,object,text]")
                        .defaultsTo("object");
                accepts("num").withOptionalArg().ofType(Integer.class).describedAs("Record Number to retrive");
                accepts("id").withOptionalArg().ofType(String.class).describedAs("Patent Id");
                accepts("limit").withOptionalArg().ofType(Integer.class).describedAs("record limit").defaultsTo(1);
                accepts("skip").withOptionalArg().ofType(Integer.class).describedAs("records to skip").defaultsTo(0);
                accepts("out").withOptionalArg().ofType(String.class).describedAs("out file");
                accepts("xmlBodyTag").withOptionalArg().ofType(String.class)
                        .describedAs("XML Body Tag which wrapps document: [us-patent, PATDOC, patent-application]")
                        .defaultsTo("us-patent");
                accepts("addHtmlEntities").withOptionalArg().ofType(Boolean.class)
                        .describedAs("Add Html Entities DTD to XML; Needed when reading Patents in PAP format.")
                        .defaultsTo(false);
                accepts("aps").withOptionalArg().ofType(Boolean.class)
                        .describedAs("Read APS - Greenbook Patent Document Format").defaultsTo(false);
            }
        };

        OptionSet options = parser.parse(args);
        String inFileStr = (String) options.valueOf("source");
        File inputFile = new File(inFileStr);

        int skip = (Integer) options.valueOf("skip");
        int limit = (Integer) options.valueOf("limit");
        String xmlBodyTag = (String) options.valueOf("xmlBodyTag");
        boolean addHtmlEntities = (Boolean) options.valueOf("addHtmlEntities");
        
       	//String format = (String) options.valueOf("format");
        
        boolean aps = (Boolean) options.valueOf("aps");

        if (options.has("num")) {
            skip = ((Integer) options.valueOf("num")) - 1;
            limit = 1;
        }

        String[] fields = ((String) options.valueOf("fields")).split(",");
        Look look = new Look();

        FileFilterChain filters = new FileFilterChain();

        DumpReader dumpReader;
        if (aps) {
            dumpReader = new DumpFileAps(inputFile);
            //filter.addRule(new SuffixFileFilter("txt"));
        } else {
            PatentDocFormat patentDocFormat = new PatentDocFormatDetect().fromFileName(inputFile);
            switch (patentDocFormat) {
            case Greenbook:
                aps = true;
                dumpReader = new DumpFileAps(inputFile);
                //filters.addRule(new PathFileFilter(""));
                //filters.addRule(new SuffixFilter("txt"));
                break;
            default:
            	DumpFileXml dumpXml = new DumpFileXml(inputFile);
    			if (PatentDocFormat.Pap.equals(patentDocFormat) || addHtmlEntities) {
    				dumpXml.addHTMLEntities();
    			}
                dumpReader = dumpXml;
                filters.addRule(new SuffixFileFilter(new String[] {"xml", "sgm", "sgml"}));
            }
        }

        dumpReader.setFileFilter(filters);

        dumpReader.open();
        dumpReader.skip(skip);

        Writer writer = null;
        if (options.has("out")) {
            String outStr = (String) options.valueOf("out");
            Path outFilePath = Paths.get(outStr);
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outFilePath.toFile()), Charset.forName("UTF-8")));
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(System.out, Charset.forName("UTF-8")));
        }

        try {
            if (options.has("id")) {
                String docid = (String) options.valueOf("id");
                look.look(dumpReader, docid, writer, fields);
            } else {
                look.look(dumpReader, limit, writer, fields);
            }
        } finally {
            dumpReader.close();
            writer.close();
        }

        System.out.println("--- Finished ---");
    }


    interface FileMethod {
 	   void method();
    }

    interface ObjectMethod {
    	   void get();
    }
}
