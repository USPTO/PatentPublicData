package gov.uspto.bulkdata.corpusbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import gov.uspto.bulkdata.DumpXmlReader;
import gov.uspto.bulkdata.PatternMatcher;
import gov.uspto.bulkdata.PatternXPath;
import gov.uspto.bulkdata.cli2.BulkData;
import gov.uspto.bulkdata.cli2.BulkDataType;
import gov.uspto.bulkdata.downloader.DownloadJob;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.UspcClassification;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import okhttp3.HttpUrl;

/**
 * Download Weekly Bulk Downloads, keeping one at a time, extract out Patent Documents which match specified CPC Classifications.
 * 
 *<pre>
 * Example Usage:
 *    gov.uspto.bulkdata.corpusbuilder.Corpus --type application --outdir="../download" --years=2014,2016 --cpc=H04N21/00 --uspc=725
 *<pre>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Corpus {
	private static final Logger LOGGER = LoggerFactory.getLogger(Corpus.class);

	private final List<Classification> wantedClasses;
	private final Writer corpusWriter;
	private final BulkData downloader;
	private Queue<HttpUrl> bulkFileQueue = new ArrayDeque<HttpUrl>();
	private HttpUrl currentbulkFileUrl;
	private DumpXmlReader currentBulkFile;

	private PatternMatcher matcher;
	private int bulkFileCount = 0;
	private int writeCount = 0;

	public Corpus(final Path downloadDir, final BulkDataType dataType, final Iterator<Integer> years,
			final List<Classification> wantedClasses, final Writer corpusWriter) {
		this.wantedClasses = wantedClasses;
		this.downloader = new BulkData(downloadDir, dataType, years, false);
		this.corpusWriter = corpusWriter;
	}

	public Corpus setup() throws IOException, XPathExpressionException {
		matcher = new PatternMatcher();

		List<Classification> cpcClasses = Classification.getByType(wantedClasses, ClassificationType.CPC);
		for (Classification cpcClass : cpcClasses) {
			String CPCXpathStr = buildCPCxPathString((CpcClassification) cpcClass);
			PatternXPath CPC = new PatternXPath(CPCXpathStr);
			matcher.add(CPC);
		}

		List<Classification> uspcClasses = Classification.getByType(wantedClasses, ClassificationType.USPC);
		for (Classification uspcClass : uspcClasses) {
			String UspcXpathStr = buildUSPCxPathString((UspcClassification) uspcClass);
			PatternXPath USPC = new PatternXPath(UspcXpathStr);
			matcher.add(USPC);
		}

		if (!corpusWriter.isOpen()){
			corpusWriter.open();
		}

		return this;
	}

	public String buildUSPCxPathString(UspcClassification uspcClass) throws XPathExpressionException {
		StringBuilder stb = new StringBuilder();
		stb.append("//classification-national/main-classification");
		stb.append("[starts-with(.,'").append(uspcClass.getMainClass()).append("')]");
		return stb.toString();
	}

	/**
	 * 
	 * Build XPath Expression for CPC Classification lookup.
	 * 
	 * "//classifications-cpc/main-cpc/classification-cpc[section/text()='H' and class/text()='04' and subclass/text()='N' and main-group[starts-with(.,'21')]]"
	 * 
	 * @param cpcClass
	 * @return
	 * @throws XPathExpressionException
	 */
	public String buildCPCxPathString(CpcClassification cpcClass) throws XPathExpressionException {

		StringBuilder stb = new StringBuilder();
		stb.append("//classifications-cpc/main-cpc/classification-cpc");
		stb.append("[");
		stb.append("section/text()='").append(cpcClass.getSection()).append("'");
		stb.append(" and ");
		stb.append("class/text()='").append(cpcClass.getMainClass()).append("'");
		stb.append(" and ");
		stb.append("subclass/text()='").append(cpcClass.getSubClass()).append("'");
		stb.append(" and ");
		stb.append("main-group[starts-with(.,'").append(cpcClass.getMainGroup()).append("')]");
		stb.append("]");

		return stb.toString();
	}

	public Corpus enqueue(Collection<HttpUrl> bulkFiles) {
		bulkFileQueue.addAll(bulkFiles);
		return this;
	}

	public Corpus enqueue() throws IOException {
		List<HttpUrl> bulkFiles = downloader.getDownloadURLs();
		enqueue(bulkFiles);
		return this;
	}

	/**
	 * Shrink the Queue to only URLS matching specified filenames.
	 * This method should be called after enqueue(), to keep only wanted filenames.
	 * 
	 * @param filenames
	 * @return
	 */
	public Corpus queueShrink(List<String> filenames){
		Queue<HttpUrl> newQueue =  new ArrayDeque<HttpUrl>();		
		Iterator<HttpUrl> queueIt = bulkFileQueue.iterator();
		while(queueIt.hasNext()){
			HttpUrl url = queueIt.next();
			List<String> urlSegments = url.pathSegments();
			String fileSegment = urlSegments.get(urlSegments.size()-1);
			if (filenames.contains(fileSegment)){
				newQueue.add(url);
			}
		}
		bulkFileQueue = newQueue;
		return this;
	}

	/**
	 * Skip or purge specified number of items from queue.
	 * 
	 * @param skip
	 * @return
	 */
	public Corpus skip(int skip) {
		for (int i = 0; i < skip; i++) {
			bulkFileQueue.poll();
		}
		return this;
	}

	public void processAllBulks(boolean deleteDone) {
		while (!bulkFileQueue.isEmpty()) {
			try {
				nextBulkFile();
				readAndWrite();

				currentBulkFile.close();
				if (deleteDone) {
					currentBulkFile.getFile().delete();
				}
			} catch (IOException e) {
				LOGGER.error("Exception during download of '{}'", currentbulkFileUrl, e);
			}
		}
	}

	/**
	 * Get next bulk file, download if needed.
	 * 
	 * @throws IOException
	 */
	public void nextBulkFile() throws IOException {
		LOGGER.info("Bulk File Queue:[{}]", bulkFileQueue.size());
		HttpUrl currentbulkFileUrl = bulkFileQueue.remove();

		DownloadJob job = downloader.download(currentbulkFileUrl);
		File currentFile = job.getDownloadTasks().get(0).getOutFile();
		currentBulkFile = new DumpXmlReader(currentFile, "us-patent");
		currentBulkFile.open();

		bulkFileCount++;
		LOGGER.info("Bulk File:[{}] '{}'", bulkFileCount, currentFile);
	}

	public void readAndWrite() throws IOException {
		String xmlDocStr;

		while (currentBulkFile.hasNext()) {
			try {
				xmlDocStr = currentBulkFile.next();
			} catch (NoSuchElementException e) {
				break;
			}

			if (matcher.match(xmlDocStr)) {
				write(xmlDocStr);
			} else {
				continue;
			}

			/*
			try {
				Patent patent = patentParser.parse(xmlDocStr);
			} catch (PatentParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
	}

	public void write(String xmlDocStr) throws IOException {
		writeCount++;
		LOGGER.info("Found matching:[{}]", writeCount);
		corpusWriter.write(xmlDocStr.getBytes());
	}

	public void close() throws IOException {
		corpusWriter.close();
	}

	public int getBulkFileCount() {
		return bulkFileCount;
	}

	public int getWriteCount() {
		return writeCount;
	}

	public static void main(String... args) throws IOException, XPathExpressionException, ParseException {
		LOGGER.info("--- Start ---");

		OptionParser parser = new OptionParser() {
			{
				accepts("type").withRequiredArg().ofType(String.class)
						.describedAs("Patent Document Type [grant, application]").required();
				accepts("years").withRequiredArg().ofType(String.class).describedAs("Years; comma for individual years; dash for year range")
						.required();
				accepts("skip").withOptionalArg().ofType(Integer.class).describedAs("Number of bulk files to skip")
						.defaultsTo(0);
				accepts("delete").withOptionalArg().ofType(Boolean.class)
						.describedAs("Delete each bulk file before moving to next.").defaultsTo(true);
				accepts("outdir").withOptionalArg().ofType(String.class).describedAs("directory")
						.defaultsTo("download");
				accepts("cpc").withRequiredArg().ofType(String.class).describedAs("CPC Classification").required();
				accepts("uspc").withRequiredArg().ofType(String.class).describedAs("USPC Classification").required();
				accepts("files").withOptionalArg().ofType(String.class).describedAs("File names to download and parse");
				accepts("out").withOptionalArg().ofType(String.class).describedAs("Output Type: xml or zip").defaultsTo("xml");
				accepts("name").withOptionalArg().ofType(String.class).describedAs("Name to give output file").defaultsTo("corpus");
			}
		};

		OptionSet options = parser.parse(args);
		if (!options.hasOptions()) {
			parser.printHelpOn(System.out);
			System.exit(1);
		}

		String type = (String) options.valueOf("type");
		int skip = (Integer) options.valueOf("skip");
		Boolean deleteDone = (Boolean) options.valueOf("delete");
		Path downloadDir = Paths.get((String) options.valueOf("outdir"));

		List<String> filenames = null;
		if (options.has("filename")) {
			String files = (String) options.valueOf("files");
			filenames = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(files);
		}

		String out = (String) options.valueOf("out");
		String name = (String) options.valueOf("name");
		String cpc = (String) options.valueOf("cpc");
		String uspc = (String) options.valueOf("uspc");
		String yearRangeStr = (String) options.valueOf("years");

		/*
		 * Setup and Execution.
		 */
		BulkDataType dataType = null;
		switch (type.toLowerCase()) {
		case "grant":
			dataType = BulkDataType.GRANT_REDBOOK_TEXT;
			break;
		case "application":
			dataType = BulkDataType.APPLICATION_REDBOOK_TEXT;
			break;
		default:
			throw new IllegalArgumentException("Unknown Download Source: " + type);
		}

		Iterator<Integer> years =  Ints.stringConverter().convertAll( Splitter.on(",").omitEmptyStrings().trimResults().splitToList(yearRangeStr) ).iterator();
		List<String> yearsDash = Splitter.on("-").omitEmptyStrings().trimResults().splitToList(yearRangeStr);
		if (yearsDash.size() == 2){
			Integer range1 = Integer.valueOf(yearsDash.get(0));
			Integer range2 = Integer.valueOf(yearsDash.get(1));
			years = (Iterator<Integer>) ContiguousSet.create(Range.closed(range1, range2), DiscreteDomain.integers()).iterator();
		}

		List<Classification> wantedClasses = new ArrayList<Classification>();
		List<String> cpcs = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(cpc);
		for (String cpcStr : cpcs) {
			CpcClassification cpcClass = CpcClassification.fromText(cpcStr);
			wantedClasses.add(cpcClass);
		}

		List<String> uspcs = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(uspc);
		for (String uspcStr : uspcs) {
			UspcClassification usClass = UspcClassification.fromText(uspcStr);
			wantedClasses.add(usClass);
		}

		Writer writer;
		if ("zip".equals(out.toLowerCase())) {
			Path zipFilePath = downloadDir.resolve(name + ".zip");
			writer = new ZipArchive(zipFilePath);
		} else {
			Path xmlFilePath = downloadDir.resolve(name + ".xml");
			writer = new SingleXml(xmlFilePath, true);
		}

		Corpus corpus = new Corpus(downloadDir, dataType, years, wantedClasses, writer);
		corpus.setup();

		if (filenames != null){
			corpus.enqueue();
			corpus.queueShrink(filenames);
		} else {
			corpus.enqueue();
		}

		if (skip > 0) {
			corpus.skip(skip);
		}
		corpus.processAllBulks(deleteDone);
		corpus.close();

		LOGGER.info("--- Finished ---, bulk:{} , wrote:{}", corpus.getBulkFileCount(), corpus.getWriteCount());
	}

}
