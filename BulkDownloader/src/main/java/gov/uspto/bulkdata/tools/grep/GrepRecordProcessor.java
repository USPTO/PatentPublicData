package gov.uspto.bulkdata.tools.grep;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;

import gov.uspto.bulkdata.RecordProcessor;
import gov.uspto.bulkdata.tools.grep.OutputMatchConfig.OUTPUT_MATCHING;
import gov.uspto.patent.PatentDocFormat;

public class GrepRecordProcessor implements RecordProcessor {

	private final GrepConfig config;
	private final Match<MatchPattern> matcher;
	private final OutputMatchConfig matchArgs;
	private final OUTPUT_MATCHING outputType;
	private final String recordSeperator;
	private long recordsChecked = 0;
	private long recordsMatched = 0;
	private Writer currentWriter;
	private String currentFilename;
	private boolean fixRecord = false;

	public GrepRecordProcessor(GrepConfig config) throws XPathExpressionException {
		this.config = config;
		this.matcher = config.getMatcher();
		this.matchArgs = config.getOutputConfig();
		this.outputType = matchArgs.getOutputType();
		this.recordSeperator = matchArgs.getRecordSeperator();

	}

	@Override
	public Boolean process(String sourceTxt, String rawRecord, Writer writer) throws DocumentException, IOException {
		if (fixRecord) {
			// After first record fails within file, fix all records.
			rawRecord = fixXML(rawRecord);
		}

		Boolean matched = false;
		Reader reader = null;
		try {
			reader = new InputStreamReader(new ByteArrayInputStream(rawRecord.getBytes()), "UTF-8");
			recordsChecked++;

			try {
				matched = hasMatch(sourceTxt, reader, writer);
			} catch (DocumentException e1) {
				fixRecord = true;
				reader.close();
				reader = new InputStreamReader(new ByteArrayInputStream(fixXML(rawRecord).getBytes()), "UTF-8");
				matched = hasMatch(sourceTxt, reader, writer);
			}
			
			if (matched) {
				recordsMatched++;
				writer(sourceTxt, rawRecord, writer);
			}			
			
		} catch (UnsupportedEncodingException e) {
			// ignore...
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return matched;
	}

	public boolean hasMatch(String sourceTxt, Reader reader, Writer writer) throws DocumentException, IOException {
		boolean matched = false;

		if (outputType == OUTPUT_MATCHING.RECORD || outputType == OUTPUT_MATCHING.TOTAL_COUNT
				|| outputType == OUTPUT_MATCHING.RECORD_LOCATION) {
			matched = matcher.match(reader);
		} else {
			matched = matcher.match(sourceTxt, reader, writer, false);
		}

		// return matcher.isNegate() ? !matched : matched;
		return matched;
	}

	public void writer(String sourceTxt, String rawRecord, Writer writer) throws IOException {
		if (outputType == OUTPUT_MATCHING.RECORD) {
			String filename = sourceTxt.replaceFirst("\\.zip:\\d+$", "");
			filename = filename + ".out";
			if (!filename.equals(currentFilename)) {
				if (currentWriter != null) {
					currentWriter.close();
				}
				currentWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(config.getOutputDir().resolve(filename).toFile(), true),
						StandardCharsets.UTF_8));
				currentFilename = filename;
			}
			write(currentWriter, rawRecord, recordSeperator);
		} else if (outputType == OUTPUT_MATCHING.RECORD_LOCATION) {
			write(writer, sourceTxt, recordSeperator);
		}
	}

	public void write(Writer writer, String... content) throws IOException {
		for (String el : content) {
			writer.write(el);
		}
		writer.flush();
	}

	@Override
	public void initialize(Writer writer) throws IOException {
		// empty
	}

	@Override
	public void finish(Writer writer) throws IOException {
		if (!matchArgs.isNoCount()) {
			writer.write("\n\nRecords Matched: ");
			writer.write(String.valueOf(recordsMatched));
			writer.write(" of ");
			writer.write(String.valueOf(recordsChecked));
			writer.write("\n");
			writer.flush();
		}
	}

	@Override
	public void setPatentDocFormat(PatentDocFormat docFormat) {
		// not used.
	}
	
	public String fixXML(String xml) throws IOException, DocumentException {
		org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<body>" + xml + "</body>", "",
				Parser.xmlParser().settings(ParseSettings.preserveCase));
		jsoupDoc.outputSettings().prettyPrint(false);
		String doc = jsoupDoc.select("body").html();
		// Add HTML DTD to ensure HTML entities do not cause any problems.
        doc = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + doc;
        return doc;
	}

}
