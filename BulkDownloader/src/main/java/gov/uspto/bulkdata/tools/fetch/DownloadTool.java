package gov.uspto.bulkdata.tools.fetch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ListMultimap;

import gov.uspto.bulkdata.PageLinkScraper;
import gov.uspto.bulkdata.RecordProcessor;
import gov.uspto.bulkdata.RunStats;
import gov.uspto.bulkdata.cli2.BulkDataType;
import gov.uspto.bulkdata.downloader.DownloadFile;
import gov.uspto.bulkdata.downloader.DownloadJob;
import gov.uspto.bulkdata.downloader.Downloader;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.common.DateRange;
import gov.uspto.common.io.DummyWriter;
import gov.uspto.patent.PatentReaderException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class DownloadTool {
	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadTool.class);

	private final transient OkHttpClient client = new OkHttpClient();
	private final DownloadConfig config;

	private Downloader downloader;
	private Queue<HttpUrl> bulkFileQueue = new ArrayDeque<HttpUrl>();
	private List<HttpUrl> urls;
	private DownloadFileProcessor downloadProcessor;

	public DownloadTool(DownloadConfig config) {
		this.config = config;
		this.downloader = new Downloader(client);
	}

	public <T extends RecordProcessor> DownloadTool(DownloadConfig config, T... processors) {
		this.config = config;
		this.downloadProcessor = new DownloadFileProcessor(config, processors);
		this.downloader = new Downloader(client);
	}

	public void exec() throws IOException, DocumentException, PatentReaderException {
		downloader.setup(config.getOutputDir());
		RunStats runStats;

		if (downloadProcessor != null) {
			LOGGER.info("--- Start ---");
			LOGGER.info("Download Processor: {}", downloadProcessor.getClass().getName());
			enqueue();
			runStats = downloadAndProcessFiles();
		} else {
			DownloadJob job;
			if (config.isRestart()) {
				LOGGER.info("--- Restart ---");
				job = this.restart();
			} else {
				LOGGER.info("--- Start ---");
				enqueue();
				job = download(bulkFileQueue);
			}
			runStats = runStatsFromJob(job);
		}

		LOGGER.info(runStats.toString());
		LOGGER.info("--- Finished ---");
	}

	private RunStats runStatsFromJob(DownloadJob job) {
		RunStats runStats = new RunStats("Download Job");
		runStats.setSuccessCount(job.getTaskCompleted());
		runStats.setRecordCount(job.getTaskTotal());
		runStats.setFailCount(job.getTaskTotal() - job.getTaskCompleted());
		return runStats;
	}

	private RunStats downloadAndProcessFiles() throws IOException, DocumentException, PatentReaderException {
		RunStats runStats = new RunStats("DownloadAndProcess");

		//Writer writer = new DummyWriter();
		Writer writer = new BufferedWriter(new OutputStreamWriter(System.out));

		try {
			downloadProcessor.initialize(writer);
		} catch (Exception e) {
			e.printStackTrace();
		}

		HttpUrl url;
	    while ((url = bulkFileQueue.poll()) != null) {
			DownloadJob job = download(url);

			DownloadFile dfile = job.getDownloadTasks().get(0);
			File file = dfile.getOutFile();

			RunStats fileRunStats = downloadProcessor.process(file);
			runStats.add(fileRunStats);
			
			if (config.isDelete()) {
				file.delete();
			}
		}

		downloadProcessor.finish(writer);
		
		return runStats;
	}

	public void enqueue(Collection<HttpUrl> bulkFiles) {
		bulkFileQueue.addAll(bulkFiles);
		LOGGER.info("Files enqueued for download: [{}]", bulkFileQueue.size());
	}

	public void enqueue() throws IOException {
		List<HttpUrl> bulkFiles = getDownloadURLs();
		enqueue(bulkFiles);
	}

	public List<HttpUrl> getDownloadURLs() throws IOException {
		List<HttpUrl> urls = new ArrayList<HttpUrl>();

		if (config.getMatchFilenames() != null && !config.getMatchFilenames().isEmpty()) {
			LOGGER.info("Fetching URLS matching filenames...");
			urls = fetchLinks(config.getMatchFilenames());
		} else {
			LOGGER.info("Fetching URLS...");
			urls = fetchLinks(config.getDownloadLimit());
		}

		return urls;
	}

	public DownloadJob restart() throws IOException {
		return this.restart(config.getOutputDir());
	}

	public DownloadJob restart(Path downloadDirectory) throws IOException {
		Downloader downloader = new Downloader(new OkHttpClient());
		DownloadJob job = DownloadJob.restore(downloadDirectory);
		downloader.download(job);
		return job;
	}

	/**
	 * Download
	 * 
	 * @param skip  - number of files to skip over.
	 * @param limit - max amount of files to download, limit of 0 is unlimited.
	 * @return
	 * @throws IOException
	 */
	public DownloadJob download(int skip, int limit) throws IOException {
		if (limit == 0) {
			urls = fetchLinks(skip);
		} else {
			urls = fetchLinks(skip, limit);
		}

		// LOGGER.info("URLS[{}]: {}", urls.size(), urls);
		return download(urls);
	}

	public DownloadJob downloadRandom(int limit) throws IOException {
		List<HttpUrl> urls = fetchLinks(config.getDownloadLimit());
		List<HttpUrl> randomUrls = new ArrayList<HttpUrl>();

		Random random = new Random();
		for (int i = 0; i < config.getDownloadLimit(); i++) {
			int index = random.nextInt(urls.size());
			randomUrls.add(urls.get(index));
		}

		LOGGER.info("URLS[{}]: {}", randomUrls.size(), randomUrls);
		return download(randomUrls);
	}

	public DownloadJob download(Collection<HttpUrl> urls) throws IOException {
		DownloadJob job = new DownloadJob(urls, config.getOutputDir());
		download(job);
		return job;
	}

	public DownloadJob download(HttpUrl url) throws IOException {
		DownloadJob job = new DownloadJob(url, config.getOutputDir());
		download(job);
		return job;
	}

	public void download(DownloadJob downloadJob) throws IOException {
		if (config.isAsync()) {
			downloader.enqueueDownload(downloadJob);
		} else {
			downloader.download(downloadJob);
		}
	}

	public DownloadJob downloadAll() throws IOException {
		List<HttpUrl> urls = fetchLinks(0);

		LOGGER.info("URLS[{}]: {}", urls.size(), urls);

		DownloadJob job = new DownloadJob(urls, config.getOutputDir());
		download(job);

		return job;
	}

	/**
	 * Fetch Links
	 * 
	 * <p>
	 * Date Year is required to get the page containing links. Links are partitioned
	 * on separate pages by year.
	 * </p>
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<HttpUrl> fetchLinks() throws IOException {
		List<HttpUrl> urls = new LinkedList<HttpUrl>();
		PageLinkScraper scrapper = new PageLinkScraper(client);
		ListMultimap<String, DateRange> dateRanges = config.getDateRangs();
		Iterator<String> yearIterator = dateRanges.keySet().iterator();
		BulkDataType dataType = config.getDataType();
		while (yearIterator.hasNext()) {
			String year = yearIterator.next();
			// String fileRegex = "[A-z]{3,6}" + yearMap.get(year) + ".*?" + "\\." +
			// dataType.getSuffix() + "$";
			HttpUrl url = HttpUrl.parse(dataType.getURL(year));
			LOGGER.info("URL: {}, Matcher: {}", url, dateRanges.get(year));
			List<HttpUrl> yearUrls = scrapper.fetchLinks(url, dateRanges.get(year), dataType.getSuffix());
			urls.addAll(yearUrls);
		}

		if (urls.isEmpty()) {
			LOGGER.warn("Fetched URL list is empty");
		}
		return urls;
	}

	private List<HttpUrl> fetchLinks(Iterable<String> filenames) throws IOException {
		List<HttpUrl> urls = new LinkedList<HttpUrl>();
		PageLinkScraper scrapper = new PageLinkScraper(client);
		ListMultimap<String, DateRange> dateRanges = config.getDateRangs();
		Iterator<String> yearIterator = dateRanges.keySet().iterator();
		BulkDataType dataType = config.getDataType();
		while (yearIterator.hasNext()) {
			String year = yearIterator.next();
			// String fileRegex = "[A-z]{3,6}" + yearMap.get(year) + ".*?" + "\\." +
			// dataType.getSuffix() + "$";
			HttpUrl url = HttpUrl.parse(dataType.getURL(year));
			LOGGER.info("URL: {}, Matcher: {}", url, dateRanges.get(year));
			List<HttpUrl> yearUrls = scrapper.fetchLinks(url, filenames);
			urls.addAll(yearUrls);
		}

		if (urls.isEmpty()) {
			LOGGER.warn("Fetched URL list is empty");
		}
		return urls;
	}

	public List<HttpUrl> fetchLinks(int skip) throws IOException {
		List<HttpUrl> urls = fetchLinks();
		return urls.subList(skip, urls.size());
	}

	private List<HttpUrl> fetchLinks(int skip, int limit) throws IOException {
		List<HttpUrl> urls = fetchLinks();
		int size = (skip + limit) > urls.size() ? urls.size() : skip + limit;
		return urls.subList(skip, size);
	}
}
