package gov.uspto.bulkdata.downloader;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import okhttp3.HttpUrl;

@JsonSerialize
public class DownloadFile implements Serializable {

	private static final long serialVersionUID = 9025228312178236682L;

	private HttpUrl url;
	//private URI url;
	private Path tempFile;
	private Path outFile;
	private transient int tries;
	private boolean isComplete = false;

	public DownloadFile(URI uri, Path downloadDir) throws IOException{
		this(HttpUrl.get(uri), downloadDir);
	}

	public DownloadFile(URL url, Path downloadDir) throws IOException{
		this(HttpUrl.get(url), downloadDir);
	}

	public DownloadFile(String urlStr, Path downloadDir) throws IOException{
		this(HttpUrl.parse(urlStr), downloadDir);
	}

	@JsonCreator
	public DownloadFile(@JsonProperty("url") String url, @JsonProperty("tempFile") String tempFile, @JsonProperty("outFile") Path outFile, @JsonProperty("isComplete") boolean isComplete, @JsonProperty("tries") int tries){
		this.url = HttpUrl.parse(url);
		this.tempFile = Paths.get(tempFile);
		this.outFile = outFile;
		this.isComplete = isComplete;
		this.tries = tries;
	}

	public DownloadFile(HttpUrl url, Path downloadDir) throws IOException{
		Preconditions.checkNotNull(url, "URL can not be Null.");
		Preconditions.checkArgument(Files.isDirectory(downloadDir, LinkOption.NOFOLLOW_LINKS), "Download directory does not exist: " + downloadDir);

		this.url = url;
	
		String filename = getFilename(url);
		String urlHash = getUrlHash(url);
		
		this.outFile = downloadDir.resolve(filename);
		this.tempFile = downloadDir.resolve(urlHash + ".tmp");

		//this.outFile = new File(downloadDir.toString(), filename);
		//this.tempFile = new File(downloadDir.toString(),  urlHash + ".tmp");
		//this.tempFile = File.createTempFile(urlHash, ".tmp");
	}

	private String getFilename(HttpUrl url){
		List<String> pathSegments = url.encodedPathSegments();
		String filename = pathSegments.get( pathSegments.size() - 1 );

		if (filename.length() > 3 && url.querySize() > 0){
			filename = url2filename(url);
		}

		return filename;
	}

	private String url2filename(final HttpUrl url){
		StringBuffer sb = new StringBuffer();
		sb.append( url.host().replaceAll("^www\\.", "") );
		sb.append('_');
		sb.append( Joiner.on("_").join( url.pathSegments() ) );

		if (url.query() != null){
			sb.append('_');
			sb.append( url.encodedQuery().replaceAll("[^A-Za-z0-9=&]", "_") );
		}

		String filename = sb.toString();
		
		if (filename.length() > 255){
			// trim to Max file name length.
			filename = filename.substring(0, 254);
		}
		
		return filename;
	}
	
	private String getUrlHash(HttpUrl url){
		HashFunction hf = Hashing.md5();
		HashCode hc = hf.newHasher().putString(url.uri().toString(), Charsets.UTF_8).hash();
		return hc.toString();
	}
	
	@JsonProperty("url")
	public String getURLAsStr(){
		return url.toString();
	}
	
	@JsonProperty("url")
	public void setURLfromStr(String urlStr){
		this.url = HttpUrl.parse(urlStr);
	}

	public File getTempFile() {
		return tempFile.toFile();
	}

	public File getOutFile() {
		return outFile.toFile();
	}

	public HttpUrl getUrl() {
		return url;
	}

	public int getTries() {
		return tries;
	}

	public void incrementTries(){
		this.tries++;
	}

	@JsonProperty("isComplete")
	public void setComplete(){
		this.isComplete = true;
	}

	@JsonProperty("isComplete")
	public boolean isComplete(){
		return isComplete;
	}

	@Override
	public String toString() {
		return "DownloadPackage [tempFile=" + tempFile + ", outFile=" + outFile + ", url=" + url + ", tries=" + tries
				+ "]";
	}
}
