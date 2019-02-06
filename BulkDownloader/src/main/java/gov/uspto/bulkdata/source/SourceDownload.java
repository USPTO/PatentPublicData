package gov.uspto.bulkdata.source;

import javax.xml.bind.annotation.XmlElement;

import okhttp3.HttpUrl;

public class SourceDownload {
	private String scrapeUrl;
	private String count;
	private String method;
	private Predicate predicate;
	private String url;

	public String getScrapeUrl() {
		return scrapeUrl;
	}

	@XmlElement(name = "scrape")
	public void setScrapeUrl(String scrapeUrl) {
		this.scrapeUrl = scrapeUrl;
	}

	public String getUrl() {
		return url;
	}

	@XmlElement(name = "url")
	public void setUrl(String downloadUrl) {
		this.url = downloadUrl;
	}

	@XmlElement(name = "method")
	public void setMethod(String method) {
		this.method = method;
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
		return "SourceDownload [url=" + url + ", scrapeUrl=" + scrapeUrl + ", method=" + method
				+ ", count=" + count + ", predicate=" + predicate + "]";
	}
}
