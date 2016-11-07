package gov.uspto.bulkdata;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import gov.uspto.common.DateRange;
import gov.uspto.bulkdata.source.Source;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PageLinkScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageLinkScraper.class.getName());

    private final OkHttpClient client;

    public PageLinkScraper(OkHttpClient client) {
        this.client = client;
    }

    /**
     * Fetch Links using Suffix.
     * 
     * @param url
     * @param suffix
     * @return
     * @throws IOException
     */
    public List<HttpUrl> fetchLinks(String url, String suffix) throws IOException {
        Preconditions.checkNotNull(url, "URL can not be Null");
        return fetchLinks(HttpUrl.parse(url), new String(), suffix);
    }

    /**
     * Fetch Links using Prefix and Suffix
     * 
     * @param url
     * @param linkPrefix
     * @param suffix
     * @return
     * @throws IOException
     */
    /*
    public List<HttpUrl> fetchLinks(String url, String linkPrefix, String suffix) throws IOException {
        Preconditions.checkNotNull(url, "URL can not be Null");
    
        HttpUrl httpUrl = HttpUrl.parse(url);
    
        return fetchLinks(httpUrl, linkPrefix, suffix);
    }
    */

    /**
     * Fetch Links using Prefix and Suffix
     * 
     * @param url
     * @param linkPrefix
     * @param suffix without dot.
     * @return
     * @throws IOException
     */
    public List<HttpUrl> fetchLinks(HttpUrl url, String linkPrefix, String suffix) throws IOException {
        List<HttpUrl> list = new ArrayList<HttpUrl>();

        //String matchPrefix = "/?" + linkPrefix;

        String matchPrefix = linkPrefix;

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();

        String responseSource = response.networkResponse() != null
                ? ("network: " + response.networkResponse().code() + " over " + response.protocol()) : "cache";

        int responseCode = response.code();

        LOGGER.info("{}: {} ({})", responseCode, url, responseSource);

        String contentType = response.header("Content-Type");
        if (responseCode != 200 || contentType == null) {
            response.body().close();
            throw new IOException("Unexpected server response code " + responseCode);
        }

        Document document = Jsoup.parse(response.body().string(), url.toString());
        for (Element element : document.select("a[href$=." + suffix + "]")) {
            String relHref = element.attr("href");
            String href = element.attr("abs:href");

            if (linkPrefix == null || relHref.matches(matchPrefix)) {

                HttpUrl link = HttpUrl.parse(href);

                if (link != null) {
                    list.add(link);
                }
            }
        }

        return list;
    }

    private final static Pattern FILENAME_DATE = Pattern.compile("^[A-z]{2,6}([0-9]{6,8})(:?_[A-z]+[0-9]+)?\\.[a-z]+$");
    private final static DateTimeFormatter[] FILE_DATE_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyMMdd"), DateTimeFormatter.ofPattern("yyyyMMdd") };

    public LocalDate parseFileDate(String filename) {
        Matcher matcher = FILENAME_DATE.matcher(filename);
        if (matcher.matches()) {
            String fileDateStr = matcher.group(1);

            for (DateTimeFormatter fileDateFormat : FILE_DATE_FORMATS) {
                try {
                    return LocalDate.parse(fileDateStr, fileDateFormat);
                } catch (DateTimeParseException e) {
                    // ignore.
                }
            }
        } else {
            LOGGER.warn("Filename does not match file date regex: {}", filename);
        }

        throw new DateTimeParseException("Failed to create LocalDate from filename: " + filename, filename, 0);

    }

    public List<HttpUrl> fetchLinks(Source source) throws IOException { // method currently used by Download class.
        List<HttpUrl> list = new ArrayList<HttpUrl>();
               
        Request request = new Request.Builder().url(source.getDownload().getScrapeUrl()).build();
        Response response = client.newCall(request).execute();

        String responseSource = response.networkResponse() != null
                ? ("network: " + response.networkResponse().code() + " over " + response.protocol()) : "cache";

        int responseCode = response.code();

        LOGGER.info("{}: {} ({})", responseCode, source.getDownload().getScrapeUrl(), responseSource);

        String contentType = response.header("Content-Type");
        if (responseCode != 200 || contentType == null) {
            response.body().close();
            throw new IOException("Unexpected server response code " + responseCode);
        }

        Document document = Jsoup.parse(response.body().string(), source.getDownload().getScrapeUrl());
        //for (Element element : document.select("a[href$=." + suffix + "]")) {
        
        for (Element element : document.select("a[href]")) {
            String relHref = element.attr("href");

            // only want filename.
            if (relHref.contains("/")){ 
                relHref = relHref.substring(relHref.lastIndexOf('/')+1, relHref.length());               
            }
            LOGGER.trace(relHref);
            
            if (source.getDownload().getPredicate().test(relHref)){
                String href = element.attr("abs:href");
                HttpUrl link = HttpUrl.parse(href);
                list.add(link);  
            }
        }

        return list;
    }
    
    
    public List<HttpUrl> fetchLinks(HttpUrl url, List<DateRange> dateMatches, String suffix) throws IOException { 
        // method currently used by BulkData class.
        List<HttpUrl> list = new ArrayList<HttpUrl>();

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();

        String responseSource = response.networkResponse() != null
                ? ("network: " + response.networkResponse().code() + " over " + response.protocol()) : "cache";

        int responseCode = response.code();

        LOGGER.info("{}: {} ({})", responseCode, url, responseSource);

        String contentType = response.header("Content-Type");
        if (responseCode != 200 || contentType == null) {
            response.body().close();
            throw new IOException("Unexpected server response code " + responseCode);
        }

        Document document = Jsoup.parse(response.body().string(), url.toString());
        for (Element element : document.select("a[href$=." + suffix + "]")) {
            String relHref = element.attr("href");
            String href = element.attr("abs:href");

            LocalDate fileDate = parseFileDate(relHref);
            for (DateRange dateRange : dateMatches) {
                if (dateRange.between(fileDate)) {
                    HttpUrl link = HttpUrl.parse(href);
                    list.add(link);
                    break;
                }
            }
        }

        return list;
    }
}
