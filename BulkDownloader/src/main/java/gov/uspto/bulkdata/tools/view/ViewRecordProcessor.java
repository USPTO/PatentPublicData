package gov.uspto.bulkdata.tools.view;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;

import gov.uspto.bulkdata.RecordProcessor;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.serialize.DocumentBuilder;
import gov.uspto.patent.serialize.JsonMapperFlat;
import gov.uspto.patent.serialize.JsonMapperStream;
import gov.uspto.patent.serialize.PlainText;

public class ViewRecordProcessor implements RecordProcessor {
	
	private final ViewConfig config;
	private PatentReader patentReader;

	public ViewRecordProcessor(ViewConfig config, PatentReader patentReader) {
		this.patentReader = patentReader;
		this.config = config;
	}

	@Override
	public Boolean process(String sourceTxt, String rawRecord, Writer writer) throws IOException {
		if ("raw".equals(config.getOutputType())) {
			write(writer, " ---------------------------\n", "Patent RAW:\n", rawRecord);
		}
		else {
			try {
				Patent patent = patentReader.read(new StringReader(rawRecord));
				if ("fields".equals(config.getOutputType())) {
					writeField(sourceTxt, patent, writer);
				} else {
					writeOutputType(sourceTxt, patent, writer);	
				}
				
			} catch (PatentReaderException e) {
				e.printStackTrace();
			}			

		}
		
		writer.flush();
		return true;
	}

	public void writeOutputType(String sourceText, Patent patent, Writer writer) throws IOException {
    	writer.write(sourceText);
    	writer.write(" ---------------------------\n");

		switch (config.getOutputType().toLowerCase()) {
			case "plaintext":
        	case "text":
        	case "txt":
        		new PlainText().write(patent, writer);
        		break;
			case "json":
			case "js":
            	//writer.write("Patent JSON:\n");
            	JsonMapperStream fileBuilder = new JsonMapperStream(true);
        		fileBuilder.write(patent, writer);
        		break;
            case "json_flat":
            case "jsonflat":
            case "flatjson":
            	//writer.write("Patent JSON FLAT:\n");
        		DocumentBuilder<Patent> fileBuilder2 = new JsonMapperFlat(true, false);
        		fileBuilder2.write(patent, writer);
        		break;
            case "object":
            case "obj":
            	writer.write(patent.toString());
                break;
		}
	}

	public void writeField(String sourceText, Patent patent, Writer writer) throws IOException {
    	writer.write(sourceText);
    	writer.write(" ---------------------------\n");
		
        for (String field : config.getFields()) {
        	
        	String fieldName = field.toLowerCase();
        	if (fieldName.endsWith("s")) {
        		fieldName = fieldName.substring(0, fieldName.length()-1);
        	}

            switch (fieldName) {
            case "fields":
            case "help":
            case "?":
            	writer.write("\nAvailable Patent Fields:\n\t");
            	writer.write(Arrays.toString(new PlainText().definedFields().toArray()));
            	writer.write("\n");
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
	}

	public void write(Writer writer, String... content) throws IOException {
		for(String el: content) {
			writer.write(el);
		}
		writer.flush();
	}

	@Override
	public void finish(Writer writer) throws IOException {
		// empty.
	}
}
