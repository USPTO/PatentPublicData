package gov.uspto.bulkdata.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.bulkdata.PageLinkScraper;
import gov.uspto.bulkdata.downloader.DownloadJob;
import gov.uspto.bulkdata.downloader.Downloader;
import gov.uspto.bulkdata.source.Source;
import gov.uspto.bulkdata.source.Sources;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

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
    private final transient Downloader downloader;

    private final Source source;
    private final boolean async;
    private final Path downloadToDir;
    private List<HttpUrl> urls;

    public Download(Source source, Path downloadToDir) {
        this(source, downloadToDir, false);
    }

    public Download(Source source, Path downloadToDir, boolean async) {
        this.source = source;
        this.downloadToDir = downloadToDir;
        this.async = async;

        this.downloader = new Downloader(client);
        downloader.setup(downloadToDir);
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
        DownloadJob job = new DownloadJob(urls, downloadToDir);
        download(job);
        return job;
    }

    public DownloadJob download(HttpUrl url) throws IOException {
        DownloadJob job = new DownloadJob(url, downloadToDir);
        download(job);
        return job;
    }

    public void download(DownloadJob downloadJob) throws IOException {
        if (async) {
            downloader.enqueueDownload(downloadJob);
        } else {
            downloader.download(downloadJob);
        }
    }

    public DownloadJob downloadAll() throws IOException {
        List<HttpUrl> urls = fetchLinks(0);

        LOGGER.info("URLS[{}]: {}", urls.size(), urls);

        DownloadJob job = new DownloadJob(urls, downloadToDir);
        download(job);

        return job;
    }

    public DownloadJob downloadRestore() throws IOException {
        DownloadJob job = DownloadJob.restore(downloadToDir);
        download(job);
        return job;
    }

    private List<HttpUrl> fetchLinks() throws IOException {
        if (source.getDownload().getDownloadUrl() != null) {
            List<HttpUrl> urls = new ArrayList<HttpUrl>(1);
            urls.add(source.getDownload().getDownloadUrl());
            return urls;
        }

        String scrapUrl = source.getDownload().getScrapeUrl();
        if (scrapUrl != null && !scrapUrl.isEmpty()) {
            PageLinkScraper scrapper = new PageLinkScraper(client);
            return scrapper.fetchLinks(source);
        } else {
            return Collections.emptyList();
        }
    }

    private List<HttpUrl> fetchLinks(int skip) throws IOException {
        List<HttpUrl> urls = fetchLinks();
        return urls.subList(skip, urls.size());
    }

    private List<HttpUrl> fetchLinks(int skip, int limit) throws IOException {
        List<HttpUrl> urls = fetchLinks();
        return urls.subList(skip, skip + limit);
    }

    public HttpUrl findUrl(String filename) throws IOException {
        List<HttpUrl> urls = fetchLinks();
        for (HttpUrl url : urls) {
            if (url.toString().endsWith(filename)) {
                return url;
            }
        }
        return null;
    }

    public boolean isAsync() {
        return async;
    }

    public List<HttpUrl> getUrls() {
        return urls;
    }

    public void setUrls(List<HttpUrl> urls) {
        this.urls = urls;
    }

    public static void main(String... args) throws IOException, JAXBException {
        LOGGER.info("--- Start ---");

        OptionParser parser = new OptionParser() {
            {
                accepts("source").withRequiredArg().ofType(String.class).describedAs("source provider: [google, reedtech, cpc]")
                        .required();
                accepts("type").withOptionalArg().ofType(String.class).describedAs("data type: [patent_grant, patent_application, ]");
                accepts("skip").withRequiredArg().ofType(Integer.class).describedAs("skip number of files");
                accepts("limit").withRequiredArg().ofType(Integer.class)
                        .describedAs("download file limit ; 0 is unlimited").defaultsTo(0);
                accepts("async").withOptionalArg().ofType(Boolean.class).describedAs("async download")
                        .defaultsTo(false);
                accepts("outdir").withOptionalArg().ofType(String.class).describedAs("directory")
                        .defaultsTo("download");
                accepts("filename").withOptionalArg().ofType(String.class)
                        .describedAs("parse links for file name and download");
                accepts("restart").withOptionalArg().ofType(String.class)
                        .describedAs("Restart failed download from job file in download directory.");
                accepts("available").withOptionalArg().ofType(String.class)
                .describedAs("Show Available Sources.");
            }
        };

        OptionSet options = parser.parse(args);
        if (!options.hasOptions()) {
            parser.printHelpOn(System.out);
            System.exit(1);
        }
        
        if (options.has("available")) {
            Sources sources = Sources.read();
            List<Source> sourceList = sources.getSources();
            System.out.println("####  Available Sources ##### ");
            for(Source source: sourceList){
                System.out.println("--source=" + source.getName().toLowerCase() + " " + "--type=" + source.getDocType().toLowerCase());
            }
            System.exit(0);
        }
        
        Path downloadToDir = Paths.get("download");
        if (options.has("outdir")) {
            downloadToDir = Paths.get((String) options.valueOf("outdir"));
        }

        if (options.has("restart")) {
            LOGGER.info("--- Restart ---");
            Downloader downloader = new Downloader(new OkHttpClient());
            DownloadJob job = DownloadJob.restore(downloadToDir);
            downloader.download(job);
            LOGGER.info("--- Finished --- {}", job.getTaskCompleted());
            System.exit(0);
        }

        String source = (String) options.valueOf("source"); // google.

        String type = null;
        if (options.has("type")) {
            type = (String) options.valueOf("type"); // application
        }

        int skip = 0;
        if (options.has("skip")) {
            skip = (Integer) options.valueOf("skip");
        }

        int downloadLimit = (Integer) options.valueOf("limit");

        boolean isAsync = false;
        if (options.has("async")) {
            isAsync = (boolean) options.valueOf("async");
        }

        String filename = null;
        if (options.has("filename")) {
            filename = (String) options.valueOf("filename");
        }

        Sources sources = Sources.read();
        Source retSource = sources.getSource(source.toLowerCase(), type);
        LOGGER.info("Source: {}", retSource);
        if (retSource == null) {
            throw new IllegalArgumentException("Unknown Download Source: " + source);
        }

        Download app = new Download(retSource, downloadToDir, isAsync);

        DownloadJob job;
        if (filename != null) {
            HttpUrl url = app.findUrl(filename);
            job = app.download(url);
        } else {
            job = app.download(skip, downloadLimit);
        }

        //DownloadJob job = app.downloadRestore();

        LOGGER.info("--- Finished --- {}", job.getTaskCompleted());
    }

}
