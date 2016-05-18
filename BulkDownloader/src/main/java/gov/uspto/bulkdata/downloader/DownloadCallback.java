package gov.uspto.bulkdata.downloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DownloadCallback implements Callback {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class.getName());

	private final Downloader downloader;
	private final DownloadFile download;

	public DownloadCallback(Downloader downloader, DownloadFile download){
		this.downloader = downloader;
		this.download = download;
	}

	@Override
	public void onFailure(Call call, IOException e) {
		try {
			retry(e);
		} catch (IOException e1) {
			// logging in the retry..
		}		
	}

	@Override
	public void onResponse(Call call, Response response) throws IOException {
		LOGGER.info("Downloading: {} - {}", download.getOutFile(), download.getTempFile());

		if (!response.isSuccessful()) {
			 throw new IOException("Unexpected code " + response);
		}

	    try {	    	
			FileWriteAction writer = new FileWriteAction(response.body(), download);
			writer.write();
			download.isComplete();
		} catch(FileAlreadyExistsException e){
			LOGGER.error("Download Failed !! {}", download, e);
			throw e;
	    } catch(FileNotFoundException e){
			LOGGER.error("Download Failed, Path Not Found for output file: {} ; {}", download.getOutFile().getAbsolutePath(), download, e);
			throw e;
		} catch (IOException e) {
			retry(e);
		}
	}

	private void retry(IOException e) throws IOException {
		if (download.getTries() <= downloader.getMaxRetryAttempts()){
			download.incrementTries();
			LOGGER.error("Download failed, retrying[{} of {}]... {}", download.getTries(), downloader.getMaxRetryAttempts(), download, e);
			downloader.enqueueDownload(download);
		} else {
			LOGGER.error("Download Failed, no more retries !! : {}", download, e);
			throw e;
		}
	}
}
