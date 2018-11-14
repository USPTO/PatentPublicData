package gov.uspto.bulkdata.tools.fetch;

import java.io.File;
import java.io.IOException;
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

		if (config.isRestart()) {
			this.restart();
		} else if (config.getMatchFilenames() != null && config.getMatchFilenames().length > 0) {
			Collection<HttpUrl> urls = findUrl(config.getMatchFilenames());
			enqueue(urls);
		} else {
			enqueue();
		}

		if (downloadProcessor == null) {
			download(bulkFileQueue);
		} else {
			LOGGER.info("Download Processor: {}", downloadProcessor.getClass().getName());
			try {
				downloadProcessor.initialize(new DummyWriter());
			} catch (Exception e) {
				e.printStackTrace();
			}
			for(HttpUrl url: bulkFileQueue) {
				DownloadJob job = download(url);
				DownloadFile dfile = job.getDownloadTasks().get(0);
				File file = dfile.getOutFile();
				downloadProcessor.process(file);
				//file.delete();
			}
			downloadProcessor.finish(null);
		}
	}

	public void enqueue(Collection<HttpUrl> bulkFiles) {
		bulkFileQueue.addAll(bulkFiles);
	}

	public void enqueue() throws IOException {
		List<HttpUrl> bulkFiles = getDownloadURLs();
		enqueue(bulkFiles);
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

	public List<HttpUrl> getDownloadURLs() throws IOException {
		if (urls == null) {
			urls = fetchLinks(config.getDownloadLimit());
		}

		return urls;
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

    private List<HttpUrl> fetchLinks() throws IOException {
        List<HttpUrl> urls = new LinkedList<HttpUrl>();
        PageLinkScraper scrapper = new PageLinkScraper(client);
        ListMultimap<String, DateRange> dateRanges = config.getDateRangs();
        Iterator<String> yearIterator = dateRanges.keySet().iterator();
        BulkDataType dataType = config.getDataType();
        while (yearIterator.hasNext()) {
            String year = yearIterator.next();
            //String fileRegex = "[A-z]{3,6}" + yearMap.get(year) + ".*?" + "\\." + dataType.getSuffix() + "$";
            HttpUrl url = HttpUrl.parse(dataType.getURL(year));
            LOGGER.info("URL: {}, Matcher: {}", url, dateRanges.get(year));
            List<HttpUrl> yearUrls = scrapper.fetchLinks(url, dateRanges.get(year), dataType.getSuffix());
            urls.addAll(yearUrls);
        }
        return urls;
    }

    public List<HttpUrl> fetchLinks(int skip) throws IOException {
        List<HttpUrl> urls = fetchLinks();
        return urls.subList(skip, urls.size());
    }

    private List<HttpUrl> fetchLinks(int skip, int limit) throws IOException {
        List<HttpUrl> urls = fetchLinks();
        if (urls.isEmpty()) {
            LOGGER.warn("Fetched URL list is empty");
        }
        int size = (skip + limit) > urls.size() ? urls.size() : skip + limit;
        return urls.subList(skip, size);
    }
    
	public List<HttpUrl> findUrl(String... filenames) throws IOException {
		List<HttpUrl> urls = fetchLinks();
		List<HttpUrl> matchedUrls = new ArrayList<HttpUrl>();
		for (String filename : filenames) {
			for (HttpUrl url : urls) {
				if (url.toString().endsWith(filename)) {
					matchedUrls.add(url);
					break;
				}
			}
		}
		return matchedUrls;
	}

	public List<HttpUrl> getUrls() {
		return urls;
	}

	public void setUrls(List<HttpUrl> urls) {
		this.urls = urls;
	}
}
