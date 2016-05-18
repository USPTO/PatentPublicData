package gov.uspto.bulkdata.downloader;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.ResponseBody;

import okio.BufferedSink;
import okio.Okio;

public class FileWriteAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileWriteAction.class);

	private final ResponseBody body;
	private DownloadFile download;
	
	public FileWriteAction(ResponseBody body, DownloadFile download){
		this.body = body;
		this.download = download;
	}

	public void write() throws IOException{
		// Skip file if it already exists.
		if (download.getOutFile().exists()){
			body.close();
			LOGGER.info("Skipping file, File already exists: {}", download.getOutFile().getName());
			return;
		}

    	long start = System.currentTimeMillis();

    	// Buffer download and write to temp file.
		BufferedSink sink = Okio.buffer(Okio.sink(download.getTempFile()));
		sink.writeAll(body.source());
		sink.close();

		long end = System.currentTimeMillis();

		boolean renameSucess = download.getTempFile().renameTo(download.getOutFile());
		if (! renameSucess){
			 if (download.getOutFile().exists()){
				 throw new FileAlreadyExistsException(download.getTempFile().toString(), download.getOutFile().toString(), "File Already Exist, can not rename file.");
			 } else {
				 throw new IOException("Failed to rename file: " + download.getTempFile().toString() + " -> " + download.getOutFile().toString());
			 }
		}

		long seconds = (end - start) / 1000;
		long mb = (download.getOutFile().length() / 1024) / 1024;
		float rate = (float) mb / seconds;

		LOGGER.info("Successfully Downloaded File: {}, {} MB in {} sec ; {} MB per sec", download.getOutFile(), mb, seconds, rate);
	}
}
