package gov.uspto.bulkdata.corpusbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
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
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import gov.uspto.bulkdata.cli2.BulkData;
import gov.uspto.bulkdata.cli2.BulkDataType;
import gov.uspto.bulkdata.downloader.DownloadJob;
import gov.uspto.common.DateRange;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;
import gov.uspto.patent.bulk.DumpFileXml;
import gov.uspto.patent.bulk.DumpReader;
import gov.uspto.patent.model.classification.PatentClassification;
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

	private final CorpusMatch<?> corpusMatch;
	private final Writer corpusWriter;
	private final BulkData downloader;
	private Queue<HttpUrl> bulkFileQueue = new ArrayDeque<HttpUrl>();
	private HttpUrl currentbulkFileUrl;
	private DumpReader currentBulkFile;

	private long bulkFileCount = 0;
	private long writeCount = 0;

	public Corpus(final BulkData downloader, final CorpusMatch<?> corpusMatch, final Writer corpusWriter) {
		this.corpusMatch = corpusMatch;
		this.downloader = downloader;
		this.corpusWriter = corpusWriter;
	}

	public Corpus setup() throws IOException, XPathExpressionException {
		corpusMatch.setup();

		if (!corpusWriter.isOpen()) {
			corpusWriter.open();
		}

		return this;
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
	public Corpus queueShrink(List<String> filenames) {
		Queue<HttpUrl> newQueue = new ArrayDeque<HttpUrl>();
		Iterator<HttpUrl> queueIt = bulkFileQueue.iterator();
		while (queueIt.hasNext()) {
			HttpUrl url = queueIt.next();
			List<String> urlSegments = url.pathSegments();
			String fileSegment = urlSegments.get(urlSegments.size() - 1);
			if (filenames.contains(fileSegment)) {
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

		PatentDocFormat patentDocFormat = new PatentDocFormatDetect().fromFileName(currentFile);

		switch(patentDocFormat){
		case Greenbook:
			currentBulkFile = new DumpFileAps(currentFile);
			break;
		default:
			currentBulkFile = new DumpFileXml(currentFile);
		}

		currentBulkFile.open();

		bulkFileCount++;
		LOGGER.info("Bulk File:[{}] '{}'", bulkFileCount, currentFile);
	}

	public void readAndWrite() throws IOException {
		try {
			int record = 0;
			while (currentBulkFile.hasNext()) {
				String docStr;
				try {
					docStr = currentBulkFile.next();
				} catch (NoSuchElementException e) {
					break;
				}

				try {
					if (corpusMatch.on(docStr, currentBulkFile.getPatentDocFormat()).match()) {
						LOGGER.info("Found matching:[{}] at {}:{} ; matched: {}", getWriteCount() + 1,
								currentBulkFile.getFile().getName(), currentBulkFile.getCurrentRecCount(),
								corpusMatch.getLastMatchPattern());
						write(docStr);
					}
				} catch (PatentReaderException e) {
					LOGGER.error("Error reading Patent {}:{}", currentBulkFile.getFile().getName(), currentBulkFile.getCurrentRecCount(), e);
				}
			}
		} finally {
			currentBulkFile.close();
		}
	}

	public void write(String xmlDocStr) throws IOException {
		corpusWriter.write(xmlDocStr.getBytes());
		writeCount++;
	}

	public void close() throws IOException {
		corpusWriter.close();
	}

	public long getBulkFileCount() {
		return bulkFileCount;
	}

	public long getWriteCount() {
		return writeCount;
	}

	public static void main(String... args) throws IOException, XPathExpressionException, ParseException {
		LOGGER.info("--- Start ---");

		OptionParser parser = new OptionParser() {
			{
				accepts("type").withRequiredArg().ofType(String.class)
						.describedAs("Patent Document Type [grant, application]").required();
                accepts("date").withRequiredArg().ofType(String.class)
                .describedAs("Single Date Range or list, example: 20150801-20150901,20160501-20160601")
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
				accepts("out").withOptionalArg().ofType(String.class).describedAs("Output Type: xml or zip")
						.defaultsTo("xml");
				accepts("name").withOptionalArg().ofType(String.class).describedAs("Name to give output file")
						.defaultsTo("corpus");
				accepts("eval").withOptionalArg().ofType(String.class).describedAs("Eval [xml, patent]: XML (Xpath XML lookup) or Patent to Instatiate Patent Object")
				.defaultsTo("xml");
				accepts("xmlBodyTag").withOptionalArg().ofType(String.class).describedAs("XML Body Tag which wrapps document: [us-patent, PATDOC, patent-application-publication]").defaultsTo("us-patent");
			}
		};

		OptionSet options = parser.parse(args);
		if (!options.hasOptions()) {
			parser.printHelpOn(System.out);
			System.exit(1);
		}

		String eval = (String) options.valueOf("eval");
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

        String dataInpuStr = (String) options.valueOf("date");

        ListMultimap<String, DateRange> yearMap = LinkedListMultimap.create();
        Iterable<String> dateInputList = Splitter.on(",").omitEmptyStrings().trimResults().split(dataInpuStr);
        for (String dateStr : dateInputList) {
            List<String> dateInputRange = Splitter.on("-").omitEmptyStrings().trimResults().splitToList(dateStr);
            if (dateInputRange.size() == 2) {
                DateRange dateRange = DateRange.parse(dateInputRange.get(0), dateInputRange.get(1),
                        DateTimeFormatter.BASIC_ISO_DATE);
                for (Integer year : dateRange.getYearsBetween()) {
                    yearMap.put(String.valueOf(year), dateRange);
                }
            } else {
                LOGGER.warn("Invalid DateRange has more than two dashs: {}", dateInputRange);
            }
        }

        LOGGER.info("Request: {}", yearMap);

		List<PatentClassification> wantedClasses = new ArrayList<PatentClassification>();
		List<String> cpcs = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(cpc);
		for (String cpcStr : cpcs) {
			CpcClassification cpcClass = new CpcClassification();
			cpcClass.parseText(cpcStr);
			wantedClasses.add(cpcClass);
		}

		List<String> uspcs = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(uspc);
		for (String uspcStr : uspcs) {
			UspcClassification usClass = new UspcClassification();
			usClass.parseText(uspcStr);
			wantedClasses.add(usClass);
		}

        BulkData downloader = new BulkData(downloadDir, dataType, yearMap, false);

		CorpusMatch<?> corpusMatch;
		if ("xml".equalsIgnoreCase(eval)){
			corpusMatch = new MatchClassificationXPath(wantedClasses);
			//corpusMatch = new MatchClassificationXPathSGML(wantedClasses);
		} else {
			corpusMatch = new MatchClassificationPatent(wantedClasses);
		}

		Writer writer;
		if ("zip".equalsIgnoreCase(out)) {
			Path zipFilePath = downloadDir.resolve(name + ".zip");
			writer = new ZipArchiveWriter(zipFilePath);
		}
		else if ("dummy".equalsIgnoreCase(out)) {
			writer = new DummyWriter();
		}
		else {
			Path xmlFilePath = downloadDir.resolve(name + ".xml");
			writer = new SingleXmlWriter(xmlFilePath, true);
		}

		Corpus corpus = new Corpus(downloader, corpusMatch, writer);
		corpus.setup();

		if (filenames != null) {
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
