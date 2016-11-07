package gov.uspto.bulkdata.cli2;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Range;

import gov.uspto.bulkdata.PageLinkScraper;
import gov.uspto.bulkdata.downloader.DownloadJob;
import gov.uspto.bulkdata.downloader.Downloader;
import gov.uspto.common.DateRange;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Download from USPTO's Bulk Download Site. 
 * 
 *<pre>
 * Usage
 * --limit=2 --type=application --years="2016, 2016" --outdir="../download"
 *</pre>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class BulkData {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkData.class);

    private static final Pattern DATE_REGEX = Pattern.compile("^\\d{4}(0[1-9]|1[0-2])(0[1-9]|1[0-9]|2[0-9]|3[0-1])?");

    private final transient OkHttpClient client = new OkHttpClient();

    private PageLinkScraper scrapper;
    private Downloader downloader;
    private BulkDataType dataType;
    private Path downloadDir;
    private List<HttpUrl> urls;
    private boolean isAsync = false;
    private ListMultimap<String, DateRange> yearMap;
    private Iterator<String> yearIterator;

    /*
    public BulkData(Path downloadTo, BulkDataType dataType, Map<String, String> yearMap, boolean isAsync) {
        this(downloadTo, dataType, yearMap, isAsync);
    }
    */

    public BulkData(Path downloadTo, BulkDataType dataType, ListMultimap<String, DateRange> yearMap, boolean isAsync) {
        this.downloadDir = downloadTo;
        this.dataType = dataType;
        this.scrapper = new PageLinkScraper(client);
        this.downloader = new Downloader(client);
        this.yearMap = yearMap;
        this.isAsync = isAsync;
        this.yearIterator = yearMap.keySet().iterator();
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
        List<HttpUrl> urls = fetchLinks();
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
        List<HttpUrl> urls = fetchLinks();

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

    private List<HttpUrl> fetchLinks() throws IOException {
        List<HttpUrl> urls = new LinkedList<HttpUrl>();
        while (yearIterator.hasNext()) {
            String year = yearIterator.next();
            //String fileRegex = "[A-z]{3,6}" + yearMap.get(year) + ".*?" + "\\." + dataType.getSuffix() + "$";
            HttpUrl url = HttpUrl.parse(dataType.getURL(year));
            LOGGER.info("URL: {}, Matcher: {}", url, yearMap.get(year));
            List<HttpUrl> yearUrls = scrapper.fetchLinks(url, yearMap.get(year), dataType.getSuffix());
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

    public HttpUrl findUrl(String filename) throws IOException {
        List<HttpUrl> urls = scrapper.fetchLinks(dataType.getURL(yearIterator.next()), dataType.getSuffix());
        for (HttpUrl url : urls) {
            if (url.toString().endsWith(filename)) {
                return url;
            }
        }
        return null;
    }

    public Path getDownloadDir() {
        return downloadDir;
    }

    public static boolean validateYears(Iterator<Integer> years, String type) {

        if ("application".equals(type)) {
            while (years.hasNext()) {
                Integer year = years.next();
                if (year < 2001) {
                    throw new IllegalArgumentException(
                            "Patent Applications not available before March 2001: " + years.toString());
                }
            }
        }
        return true;
    }

    public static void main(String... args) throws IOException {
        LOGGER.info("--- Start ---");

        OptionParser parser = new OptionParser() {
            {
                accepts("type").withRequiredArg().ofType(String.class)
                        .describedAs("Patent Document Type [grant, application, gazette]").required();
                accepts("date").withRequiredArg().ofType(String.class)
                        .describedAs("Single Date Range or list, example: 20150801-20150901,20160501-20160601")
                        .required();
                accepts("limit").withOptionalArg().ofType(Integer.class).describedAs("download file limit")
                        .defaultsTo(0);
                accepts("skip").withRequiredArg().ofType(Integer.class).describedAs("skip number of files")
                        .defaultsTo(0);
                accepts("async").withOptionalArg().ofType(Boolean.class).describedAs("async download")
                        .defaultsTo(false);
                accepts("outdir").withOptionalArg().ofType(String.class).describedAs("directory")
                        .defaultsTo("download");
                accepts("filename").withOptionalArg().ofType(String.class)
                        .describedAs("parse links for file name and download");
                accepts("restart").withOptionalArg().ofType(String.class)
                        .describedAs("Restart failed download from job file in download directory.");
            }
        };

        OptionSet options = parser.parse(args);
        if (!options.hasOptions()) {
            parser.printHelpOn(System.out);
            System.exit(1);
        }

        Path downloadDir = Paths.get((String) options.valueOf("outdir"));

        if (options.has("restart")) {
            LOGGER.info("--- Restart ---");
            Downloader downloader = new Downloader(new OkHttpClient());
            DownloadJob job = DownloadJob.restore(downloadDir);
            downloader.download(job);
            LOGGER.info("--- Finished --- {}", job.getTaskCompleted());
            System.exit(0);
        }

        int skip = (Integer) options.valueOf("skip");
        int downloadLimit = (Integer) options.valueOf("limit");
        boolean isAsync = (boolean) options.valueOf("async");
        String type = (String) options.valueOf("type");

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

        String filename = null;
        if (options.has("filename")) {
            filename = (String) options.valueOf("filename");
        }

        BulkDataType dataType = null;
        switch (type.toLowerCase()) {
        case "grant":
            dataType = BulkDataType.GRANT_REDBOOK_TEXT;
            break;
        case "application":
            dataType = BulkDataType.APPLICATION_REDBOOK_TEXT;
            for (String yearStr : yearMap.keySet()) {
                Integer year = Integer.parseInt(yearStr);
                if (year < 2001) {
                    throw new IllegalArgumentException(
                            "Patent Applications not available before March 2001: " + yearMap.keySet());
                }
            }
            break;
        case "gazette":
            dataType = BulkDataType.GAZETTE;
            break;
        default:
            throw new IllegalArgumentException("Unknown Download Source: " + type);
        }

        BulkData bulkData = new BulkData(downloadDir, dataType, yearMap, isAsync);

        DownloadJob job;
        if (filename != null) {
            HttpUrl url = bulkData.findUrl(filename);
            job = bulkData.download(url);
        } else {
            job = bulkData.download(skip, downloadLimit);
        }

        LOGGER.info("--- Finished --- {}", job.getTaskCompleted());
    }

}
