package gov.uspto.bulkdata.downloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Supports HTTP and HTTPS.
 * 
 * OKHttp defaults: 
 * 		-- Follows Redirects.
 * 		-- Supports HTTP 2, SPDY, and HTTP 1.1.
 * 		-- default timeouts are 10000ms == 10sec.
 * 
 * Async downloads defaults: single host 5, overall 64.
 * 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 */
public class Downloader {
	private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class.getName());

	protected final static int MAX_RETRY_DEFAULT = 3;
	//private final static String DOWNLOAD_DIR = "download";
	
	private final OkHttpClient client;
	private final int maxRetryAttempts;
	
	public Downloader(OkHttpClient client){
		this(client, MAX_RETRY_DEFAULT);
	}

	public Downloader(OkHttpClient client, int maxRetryAttempts){
		Preconditions.checkNotNull(client, "OkHttpClient can not be null");

		this.client = client;
		this.maxRetryAttempts = maxRetryAttempts;
	}

	public void setup(Path downloadDir){
		downloadDir.toFile().mkdir();
	}

	public void download(String url, Path destination) throws IOException{
		DownloadFile downloadTask = new DownloadFile(url, destination);
		download(downloadTask);
	}

	public void download(DownloadJob downloadJob) throws IOException{
		Iterator<DownloadFile> downloadTasks = downloadJob.iterator();
		while(downloadTasks.hasNext()){
			DownloadFile file = downloadTasks.next();

			if (file.isComplete()){
				continue;
			}

			if ( download(file) ){
				file.setComplete();
				downloadJob.save();
			}
		}

		downloadJob.setComplete();
	}

	public boolean download(DownloadFile download) throws IOException{
		
		  LOGGER.info("Downloading: {} - {}", download.getOutFile(), download.getTempFile());

		  Request request = new Request.Builder().url(download.getUrl()).build();

		  Call call = client.newCall(request);
		  Response response = call.execute();

		  if (!response.isSuccessful()) {
			LOGGER.error("Download Failed, failure in server resposes !! : {}", request);
		    throw new IOException("Unexpected code " + response);
		  }

		  try {
			  FileWriteAction writer = new FileWriteAction(response.body(), download);
			  writer.write();
			  download.setComplete();
		  } catch(FileAlreadyExistsException e){
				LOGGER.error("Download Failed !! {}", download, e);
				throw e;
		  } catch(FileNotFoundException e){
				LOGGER.error("Download Failed, Path Not Found for output file: {} ; {}", download.getOutFile().getAbsolutePath(), request, e);
				throw e;
		  } catch(IOException e){
			if (download.getTries() <= maxRetryAttempts){
				download.incrementTries();
				LOGGER.error("Download Failed, retrying[{} of {}]... {}", download.getTries(), maxRetryAttempts, request, e);
				return download(download);
			} else {
				LOGGER.error("Download Failed !! no more retries !! : {}", request, e);
				throw e;
			}
		  }
		  
		  return true;
	}

	public void enqueueDownload(DownloadJob downloadJob){
		Iterator<DownloadFile> downloadTasks = downloadJob.iterator();
		while(downloadTasks.hasNext()){
			DownloadFile file = downloadTasks.next();

			if (file.isComplete()){
				continue;
			}
			
			enqueueDownload(file);
		}
	}

	public void enqueueDownload(DownloadFile download){
		  Request request = new Request.Builder().url(download.getUrl()).build();

		  Call call = client.newCall(request);
		  
		  Callback responseCallback = new DownloadCallback(this, download);

		  call.enqueue(responseCallback);
	}

	/**
	 * Count of download request waiting in Queue, when making async request.
	 */
	public int waitingDownloadCount(){
		return client.dispatcher().queuedCallsCount();
	}
	
	/**
	 * Count of Active Downloads, when making async request.
	 */
	public int activeDownloadCount(){
		return client.dispatcher().runningCallsCount();
	}

	private void retry(DownloadFile download, IOException e) throws IOException {
		if (download.getTries() <= maxRetryAttempts){
			download.incrementTries();
			LOGGER.error("Download failed, retrying[{} of {}]... {}", download.getTries(), maxRetryAttempts, download, e);
			download(download);
		} else {
			LOGGER.error("Download Failed, no more retries !! : {}", download, e);
			throw e;
		}
	}
	
	public int getMaxRetryAttempts(){
		return maxRetryAttempts;
	}

}
