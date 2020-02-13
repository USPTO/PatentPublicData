package gov.uspto.bulkdata.cli;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import gov.uspto.bulkdata.tools.fetch.DownloadConfig;
import gov.uspto.bulkdata.tools.fetch.DownloadTool;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.bulkdata.tools.grep.GrepConfig;
import gov.uspto.bulkdata.tools.grep.GrepRecordProcessor;
import gov.uspto.bulkdata.tools.transformer.TransformerConfig;
import gov.uspto.bulkdata.tools.transformer.TransformerRecordProcessor;
import gov.uspto.patent.PatentReaderException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Fetch/Download Bulk Patent Data
 *
 * <p>
 * Download Patent Bulk files using wanted criteria [month,year,type] to a
 * directory. Optionally, each download can be processed.
 * </p>
 *
 * <h2>Example Usage</h2>
 *
 * <h3>Download</h2>
 * <p>
 * -f="." --fetch-type="grant" --fetch-date="20181101-20181115"
 * --outDir="./target/output"
 * </p>
 *
 * <h3>Sequentially download and transform (Download, and Transform tools)</h3>
 * <p>
 * -f="." --fetch-type="grant" --fetch-date="20181101-20181115"
 * --outDir="./target/output --transform
 * </p>
 *
 * <h3>Sequentially download, match and transform (Download, Grep, and Transform
 * tools)</h3>
 * <p>
 * -f="." --fetch-type="grant" --outDir="./target/output"
 * --fetch-date="20181101-20181115" --type="json" --bulkKV=true
 * --outputBulkFile=true --xpath="//invention-title[starts-with(text(),
 * 'Food')]"
 * </p>
 *
 * <h3>Corpus Generation: Sequentially download and match</h3>
 * <p>
 * -f="." --fetch-type="grant" --outDir="./target/output"
 * --fetch-date="20181101-20181115"
 * --xpath="//publication-reference/document-id/doc-number/text()"
 * --values="D0833118"
 * <p>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class Fetch {

	public static void main(String[] args)
			throws PatentReaderException, IOException, DocumentException, XPathExpressionException {

		DownloadConfig downloadConfig = new DownloadConfig();
		OptionParser opParser = downloadConfig.buildArgs();
		opParser.accepts("transform").withOptionalArg().ofType(Boolean.class).describedAs("Transform while downloading");

		GrepConfig grepConfig = new GrepConfig();
		grepConfig.buildArgs(opParser);

		TransformerConfig transConfig = new TransformerConfig();
		transConfig.buildArgs(opParser);

		downloadConfig.parseArgs(args);
		downloadConfig.readOptions();

		grepConfig.parseArgs(args);
		grepConfig.readOptions();
		boolean doGrep = grepConfig.getMatcher() != null ? true : false;
		
		transConfig.parseArgs(args);
		transConfig.readOptions();
		
		OptionSet options = opParser.parse(args);
		boolean doTransform = options.has("transform");// | transConfig.isBulkOutput() ? true : false;

		DownloadTool tool = null;
		if (doTransform) {
			TransformerRecordProcessor processor = new TransformerRecordProcessor(transConfig);
			if (grepConfig.getMatcher() != null) {
				processor.setMatchProcessor(new GrepRecordProcessor(grepConfig));
			}
			tool = new DownloadTool(downloadConfig, processor);
		}
		else if (doGrep) {
			tool = new DownloadTool(downloadConfig, new GrepRecordProcessor(grepConfig));
		} 
		else {
			tool = new DownloadTool(downloadConfig);
		}

		tool.exec();
	}

}
