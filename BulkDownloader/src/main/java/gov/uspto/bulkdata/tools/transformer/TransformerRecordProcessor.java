package gov.uspto.bulkdata.tools.transformer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.bulkdata.RecordProcessor;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.bulkdata.tools.grep.GrepRecordProcessor;
import gov.uspto.common.io.DummyWriter;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.serialize.DocumentBuilder;
import gov.uspto.patent.serialize.JsonMapperFlat;
import gov.uspto.patent.serialize.JsonMapperPATFT;
import gov.uspto.patent.serialize.JsonMapperStream;
import gov.uspto.patent.serialize.PlainText;
import gov.uspto.patent.serialize.solr.JsonMapperSolr;

public class TransformerRecordProcessor implements RecordProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(TransformerRecordProcessor.class);

	private final TransformerConfig config;
	private PatentReader patentReader;
	private String currentFilename;
	private Writer currentWriter;
	private GrepRecordProcessor matchProcessor;
	private final String fileExt;

	public TransformerRecordProcessor(TransformerConfig config) {
		this.config = config;
		this.fileExt = config.isBulkKV() ? ".tsv" : "." + config.getOutputType();
	}

	@Override
	public void setPatentDocFormat(PatentDocFormat docFormat) {
		this.patentReader = new PatentReader(docFormat);
	}

	@Override
	public void initialize(Writer writer) throws IOException, TransformerConfigurationException {
		// empty.
	}

	public void setMatchProcessor(GrepRecordProcessor grepProcessor) {
		this.matchProcessor = grepProcessor;
	}

	@Override
	public Boolean process(String sourceTxt, String rawRecord, Writer writer)
			throws PatentReaderException, DocumentException, IOException {
		MDC.put("DOCID", sourceTxt);

		if (matchProcessor != null && !matchProcessor.process(sourceTxt, rawRecord, new DummyWriter())) {
			return false;
		}

		Patent patent = patentReader.read(new StringReader(rawRecord));

		String patentId = patent.getDocumentId() != null ? patent.getDocumentId().toText() : "";
		MDC.put("DOCID", patentId);

		String sourceFilename = sourceTxt.replaceFirst("\\.zip:\\d+$", "");

		if (!config.isBulkOutput()) {
			Path outPath = config.getOutputDir().resolve(sourceFilename);
			if (!outPath.toFile().isDirectory()) {
				outPath.toFile().mkdirs();
			}

			String currentFileName = patentId + fileExt;

			File outputFile = outPath.resolve(currentFileName).toFile();
			try (Writer currentWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_16))) {
				writeOutputType(sourceTxt, patent, currentWriter);
			}

		} else {
			String filename = sourceFilename + fileExt;
			if (!filename.equals(currentFilename)) {
				if (currentWriter != null) {
					currentWriter.close();
				}
				File outputFile = config.getOutputDir().resolve(filename).toFile();
				currentWriter = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(outputFile, true), StandardCharsets.UTF_16));
				currentFilename = filename;
			}

			try {
				writeOutputType(sourceTxt, patent, currentWriter);
				currentWriter.write('\n');
				currentWriter.flush();
			} catch (IOException e) {
				LOGGER.error("File Write Failed", e);
				try {
					currentWriter.close();
				} catch (IOException e1) {
					// do nothing.
				}
				throw e;
			}

		}

		return true;
	}

	public void writeOutputType(String sourceText, Patent patent, Writer writer) throws IOException {
		Boolean prettyPrint = config.isPrettyPrint();

		if (config.isBulkKV()) {
			writer.write(patent.getDocumentId().toText() + ":" + sourceText);
			writer.write("\t");
		}

		switch (config.getOutputType().toLowerCase()) {
		case "plaintext":
		case "text":
		case "txt":
			new PlainText(prettyPrint).write(patent, writer);
			break;
		case "json":
		case "js":
			// writer.write("Patent JSON:\n");
			JsonMapperStream fileBuilder = new JsonMapperStream(prettyPrint, false);
			fileBuilder.write(patent, writer);
			break;
		case "solr":
			// writer.write("Patent JSON:\n");
			JsonMapperSolr solrFileBuilder = new JsonMapperSolr(prettyPrint, true, false);
			solrFileBuilder.write(patent, writer);
			break;	
		case "patft":
		case "apft":
			// writer.write("Patent JSON:\n");
			JsonMapperPATFT builderPatft = new JsonMapperPATFT(prettyPrint, false);
			builderPatft.write(patent, writer);
			break;
		case "json_flat":
		case "jsonflat":
		case "flatjson":
			// writer.write("Patent JSON FLAT:\n");
			DocumentBuilder<Patent> fileBuilder2 = new JsonMapperFlat(prettyPrint, false);
			fileBuilder2.write(patent, writer);
			break;
		case "object":
		case "obj":
			writer.write(patent.toString());
			break;
		default:
			throw new RuntimeException("Unknown output type: " + config.getOutputType().toLowerCase());
		}
	}

	@Override
	public void finish(Writer writer) throws IOException {
		if (currentWriter != null) {
			currentWriter.close();
		}

		/*
		 * if (totalCount >= totalLimit) {
		 * LOGGER.info("Process Complete, Total Record Limit Reached [{}]", totalCount);
		 * } else { LOGGER.info("Process Complete, Total Records [{}]", totalCount); }
		 */
	}

}
