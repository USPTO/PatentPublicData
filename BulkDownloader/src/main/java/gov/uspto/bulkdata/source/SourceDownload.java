package gov.uspto.bulkdata.source;

import javax.xml.bind.annotation.XmlElement;

import okhttp3.HttpUrl;

public class SourceDownload {
    private HttpUrl downloadUrl;
    private String scrapeUrl;
    private String count;
    private Predicate predicate;

    public String getScrapeUrl() {
        return scrapeUrl;
    }

    @XmlElement(name = "scrape")
    public void setScrapeUrl(String scrapeUrl) {
        this.scrapeUrl = scrapeUrl;
    }

    public HttpUrl getDownloadUrl() {
        return downloadUrl;
    }

    @XmlElement(name = "url")
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = HttpUrl.parse(downloadUrl);
    }

    @XmlElement(name = "count")
    public void setCount(String count) {
        this.count = count;
    }

    public String getCount() {
        return count;
    }

    @XmlElement(name = "predicate", required = true)
    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    public String toString() {
        return "DownloadConfig [downloadUrl=" + downloadUrl + ", scrapeUrl=" + scrapeUrl + ", count=" + count
                + ", predicate=" + predicate + "]";
    }
}
