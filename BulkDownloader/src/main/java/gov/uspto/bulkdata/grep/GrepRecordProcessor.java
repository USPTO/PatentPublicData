package gov.uspto.bulkdata.grep;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.xpath.XPathExpressionException;

import gov.uspto.bulkdata.RecordProcessor;
import gov.uspto.bulkdata.grep.OutputMatchConfig.OUTPUT_MATCHING;

public class GrepRecordProcessor implements RecordProcessor {
	
	private final GrepConfig config;
	private final Match<MatchPattern> matcher;
	private final OutputMatchConfig matchArgs;
	private final OUTPUT_MATCHING outputType;
	private final String recordSeperator;
	private long recordsChecked = 0;
	private long recordsMatched = 0;

	public GrepRecordProcessor(GrepConfig config) throws XPathExpressionException {
		this.config = config;
		this.matcher = config.getMatcher();
		this.matchArgs = config.getOutputConfig();
		this.outputType = matchArgs.getOutputType();
		this.recordSeperator = matchArgs.getRecordSeperator();

	}

	@Override
	public Boolean process(String sourceTxt, String rawRecord, Writer writer) throws DocumentException, IOException {
		Boolean matched = false;
        try {
			Reader reader = new InputStreamReader(new ByteArrayInputStream(rawRecord.getBytes()), "UTF-8");			
			recordsChecked++;
			if (hasMatch(sourceTxt, reader, writer)) {
				matched = true;
				recordsMatched++;
				writer(sourceTxt, rawRecord, writer);
			}
		} catch (UnsupportedEncodingException e) {
			// ignore...
		}

        return matched;
	}

	public boolean hasMatch(String sourceTxt, Reader reader, Writer writer) throws DocumentException, IOException {
		boolean matched = false;
	
		if (outputType == OUTPUT_MATCHING.RECORD ||
				outputType == OUTPUT_MATCHING.TOTAL_COUNT ||
						outputType == OUTPUT_MATCHING.RECORD_LOCATION) {
				matched = matcher.match(reader);
		}
		else {
			matched = matcher.match(sourceTxt, reader, writer, false);
		}

		//return matcher.isNegate() ? !matched : matched;
		return matched;
	}

	public void writer(String sourceTxt, String rawRecord, Writer writer) throws IOException {
		if (outputType == OUTPUT_MATCHING.RECORD) {
			write(writer, rawRecord, recordSeperator);
		}
		else if (outputType == OUTPUT_MATCHING.RECORD_LOCATION) {
			write(writer, sourceTxt, recordSeperator);
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
		if (!matchArgs.isNoCount()) {
			writer.write("\n\nRecords Matched: ");
			writer.write(String.valueOf(recordsMatched));
			writer.write(" of ");
			writer.write(String.valueOf(recordsChecked));
			writer.write("\n");
			writer.flush();
		}
	}
}
