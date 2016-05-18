package gov.uspto.bulkdata.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import gov.uspto.bulkdata.PageLinkScraper;
import gov.uspto.bulkdata.downloader.DownloadJob;
import gov.uspto.bulkdata.downloader.Downloader;
import gov.uspto.bulkdata.source.CPC;
import gov.uspto.bulkdata.source.FDA;
import gov.uspto.bulkdata.source.Google;
import gov.uspto.bulkdata.source.PatentDocType;
import gov.uspto.bulkdata.source.Reedtech;

/**
 * Download files from Website matching prefix.
 * 
 * --source google --type application --limit 1
 * --source reedtech --type application --limit 1
 * --source cpc --limit 1
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Download {
	private static final Logger LOGGER = LoggerFactory.getLogger(Download.class);

	private final transient OkHttpClient client = new OkHttpClient();
	private final transient PageLinkScraper scrapper;
	private final transient Downloader downloader;

	private final String seedUrl;
	private final String prefix;
	private final boolean isAsync;
	private final Path downloadDir;
	private List<HttpUrl> urls;

	public Download(Path downloadDir, String seedUrl, String prefix) {
		this(downloadDir, seedUrl, prefix, false);
	}

	public Download(Path downloadDir, String seedUrl, String prefix, boolean isAsync) {
		Preconditions.checkArgument(Files.isDirectory(downloadDir, LinkOption.NOFOLLOW_LINKS),
				"Download directory does not exist: " + downloadDir);

		this.seedUrl = seedUrl;
		this.prefix = prefix;
		this.isAsync = isAsync;
		this.downloadDir = downloadDir;

		this.scrapper = new PageLinkScraper(client);
		this.downloader = new Downloader(client);
		downloader.setup(downloadDir);
	}

	/**
	 * Download
	 * 
	 * @param skip - number of files to skip over.
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
		
		LOGGER.info("URLS[{}]: {}", urls.size(), urls);
		return download(urls);
	}

	public List<HttpUrl> getDownloadURLs() throws IOException {
		if (urls == null) {
			urls = fetchLinks(0);
		}

		return urls;
	}

	public DownloadJob downloadRandom(int limit) throws IOException {
		List<HttpUrl> urls = fetchLinks(0);
		List<HttpUrl> randomUrls = new ArrayList<HttpUrl>();

		Random random = new Random();
		for (int i = 0; i < limit; i++) {
			int index = random.nextInt(urls.size());
			randomUrls.add(urls.get(index));
		}

		LOGGER.info("URLS[{}]: {}", randomUrls.size(), randomUrls);
		return download(randomUrls);
	}

	public DownloadJob download(Collection<HttpUrl> urls) throws IOException {
		DownloadJob job = new DownloadJob(urls, downloadDir);
		download(job);
		return job;
	}

	public DownloadJob download(HttpUrl url) throws IOException {
		DownloadJob job = new DownloadJob(url, downloadDir);
		download(job);
		return job;
	}

	public void download(DownloadJob downloadJob) throws IOException {
		if (isAsync) {
			downloader.enqueueDownload(downloadJob);
		} else {
			downloader.download(downloadJob);
		}
	}

	public DownloadJob downloadAll() throws IOException {
		List<HttpUrl> urls = fetchLinks(0);

		LOGGER.info("URLS[{}]: {}", urls.size(), urls);

		DownloadJob job = new DownloadJob(urls, downloadDir);
		download(job);

		return job;
	}

	public DownloadJob downloadRestore() throws IOException {
		DownloadJob job = DownloadJob.restore(downloadDir);
		download(job);
		return job;
	}

	private List<HttpUrl> fetchLinks(int skip) throws IOException {
		List<HttpUrl> urls = scrapper.fetchLinks(seedUrl, prefix, "zip");
		return urls.subList(skip, urls.size());
	}

	private List<HttpUrl> fetchLinks(int skip, int limit) throws IOException {
		List<HttpUrl> urls = scrapper.fetchLinks(seedUrl, prefix, "zip");
		return urls.subList(skip, skip+limit);
	}

	public HttpUrl findUrl(String filename) throws IOException {
		List<HttpUrl> urls = scrapper.fetchLinks(seedUrl, prefix, "zip");
		for(HttpUrl url : urls){
			if (url.toString().endsWith(filename)){
				return url;
			}
		}
		return null;
	}

	public String getSeedUrl() {
		return seedUrl;
	}

	public String getPrefix() {
		return prefix;
	}

	public Path getDownloadDir() {
		return downloadDir;
	}

	public boolean isAsync() {
		return isAsync;
	}

	public List<HttpUrl> getUrls() {
		return urls;
	}

	public void setUrls(List<HttpUrl> urls) {
		this.urls = urls;
	}

	public static void main(String... args) throws IOException {
		LOGGER.info("--- Start ---");

		OptionParser parser = new OptionParser(){{
			accepts("source").withRequiredArg().ofType(String.class).describedAs("[google, reedtech, cpc]").required();
			accepts("type").withOptionalArg().ofType(String.class).describedAs("file type");
			accepts("skip").withRequiredArg().ofType(Integer.class).describedAs("skip number of files");
			accepts("limit").withRequiredArg().ofType(Integer.class).describedAs("download file limit ; 0 is unlimited").required();
			accepts("async").withOptionalArg().ofType(Boolean.class).describedAs("async download").defaultsTo(false);
			accepts("outdir").withOptionalArg().ofType(String.class).describedAs("directory").defaultsTo("download");
			accepts("filename").withOptionalArg().ofType(String.class).describedAs("parse links for file name and download");
		}};

		OptionSet options = parser.parse(args);
		if (!options.hasOptions()){
			parser.printHelpOn( System.out );
			System.exit(1);
		}

		String source = (String) options.valueOf("source"); // google.

		String type = "NOT SET";
		if (options.has("type")){
			type = (String) options.valueOf("type"); // application
		}

		int skip = 0;
		if (options.has("skip")){
			skip = (Integer) options.valueOf("skip");
		}

		int downloadLimit = (Integer) options.valueOf("limit");
		
		Path downloadDir = Paths.get("download");
		if (options.has("outdir")){
			downloadDir =  Paths.get( (String) options.valueOf("outdir") );
		}

		boolean isAsync = false;
		if (options.has("async")){
			isAsync = (boolean) options.valueOf("async");
		}

		String filename = null;
		if (options.has("filename")){
			filename =  (String) options.valueOf("filename");
		}

		/*
		 * Setup and Execution.
		 */
		PatentDocType docType = null;
		try {
			docType = PatentDocType.valueOf(type.toUpperCase());
		} catch (IllegalArgumentException e) {
			LOGGER.error("Invalid Patent Document Type: '{}', valid options are {}", type, PatentDocType.values(), e);
			System.exit(1);
		}

		String seedUrl;
		String prefix;
		switch (source.toLowerCase()) {
		case "google":
			seedUrl = Google.getURL(docType);
			prefix = Google.getPrefix(docType);
			break;
		case "reedtech":
			seedUrl = Reedtech.getURL(docType);
			prefix = Reedtech.getPrefix(docType);
			break;
		case "cpc":
			seedUrl = CPC.getURL();
			prefix = CPC.getPrefix();
			break;
		case "fda":
			seedUrl = FDA.getURL();
			prefix = FDA.getPrefix();
			break;
		default:
			throw new IllegalArgumentException("Unknown Download Source: " + source);
		}

		Download app = new Download(downloadDir, seedUrl, prefix, isAsync);

		DownloadJob job;
		if (filename != null){
			HttpUrl url = app.findUrl(filename);
			job = app.download(url);
		} else {
			job = app.download(skip, downloadLimit);
		}

		//DownloadJob job = app.downloadRestore();

		LOGGER.info("--- Finished --- {}", job.getTaskCompleted());
	}

}
