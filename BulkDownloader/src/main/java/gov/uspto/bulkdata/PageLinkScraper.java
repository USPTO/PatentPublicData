package gov.uspto.bulkdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
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

		HttpUrl httpUrl = HttpUrl.parse(url);

		return fetchLinks(httpUrl, null, suffix);
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
	public List<HttpUrl> fetchLinks(String url, String linkPrefix, String suffix) throws IOException {
		Preconditions.checkNotNull(url, "URL can not be Null");

		HttpUrl httpUrl = HttpUrl.parse(url);

		return fetchLinks(httpUrl, linkPrefix, suffix);
	}

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

		String matchPrefix = "/" + linkPrefix;

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
			String href = element.attr("abs:href");

			if (linkPrefix == null || href.contains(matchPrefix)) {
				HttpUrl link = HttpUrl.parse(href);

				if (link != null) {
					list.add(link);
				}
			}
		}

		return list;
	}
}
