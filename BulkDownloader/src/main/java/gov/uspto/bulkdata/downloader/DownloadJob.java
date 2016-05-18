package gov.uspto.bulkdata.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import okhttp3.HttpUrl;

@JsonSerialize
@JsonIgnoreProperties({ "taskTotal", "taskCompleted", "complete" })
public class DownloadJob implements Serializable, Iterable<DownloadFile> {

	private static final long serialVersionUID = 2129147088965621551L;

	private static final String DOWNLOAD_STATUS_FILE = "DownloadJobStatus.json";

	private static ObjectMapper JSON_MAPPER = new ObjectMapper();
	static {
		JSON_MAPPER.registerModule(new Jdk7Module());
	}
	
	private Path downloadDir;
	private boolean isJobComplete;
	private int taskTotal;
	private final List<DownloadFile> downloadTasks;

	public DownloadJob(HttpUrl url, Path downloadDir) throws IOException {
		this.downloadDir = downloadDir;
		this.taskTotal = 1;

		this.downloadTasks = new ArrayList<DownloadFile>();
		DownloadFile download = new DownloadFile(url, downloadDir);
		downloadTasks.add(download);
	}

	public DownloadJob(Collection<HttpUrl> urls, Path downloadDir) throws IOException {
		this.downloadDir = downloadDir;
		this.taskTotal = urls.size();

		this.downloadTasks = new ArrayList<DownloadFile>();
		for (HttpUrl url : urls) {
			DownloadFile download = new DownloadFile(url, downloadDir);
			downloadTasks.add(download);
		}
	}

	/**
	 * Used by Jackson to deserialize from JSON.
	 */
	@JsonCreator
	private DownloadJob(@JsonProperty("downloadTasks") ArrayList<LinkedHashMap> downloadTasks) throws IOException {
		this.downloadTasks = JSON_MAPPER.convertValue(downloadTasks, new TypeReference<List<DownloadFile>>() {});
		this.taskTotal = downloadTasks.size();
	}	
	
	public Path getDownloadDir() {
		return downloadDir;
	}

	public int getTaskTotal() {
		return taskTotal;
	}

	public int getTaskCompleted() {
		int count = 0;

		for (DownloadFile task : downloadTasks) {
			if (task.isComplete()) {
				count++;
			}
		}

		return count;
	}

	public boolean isComplete() {
		return isJobComplete;
	}

	public void setComplete() {
		this.isJobComplete = true;
	}

	public List<DownloadFile> getDownloadTasks() {
		return downloadTasks;
	}

	@Override
	public Iterator<DownloadFile> iterator() {
		return downloadTasks.iterator();
	}

	@Override
	public String toString() {
		return "DownloadJob [downloadTasks=" + downloadTasks.toString() + ", isJobComplete=" + isJobComplete + "]";
	}

	/**
	 * Persist State to Disk.
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		File downloadStatusFile = new File(downloadDir.toString(), DOWNLOAD_STATUS_FILE);
		Writer outFile = new OutputStreamWriter(new FileOutputStream(downloadStatusFile));

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jdk7Module());
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(outFile, this);
	}

	/**
	 * Restore State from Disk.
	 * 
	 * @param downloadDir
	 * @return
	 * @throws IOException
	 */
	public static DownloadJob restore(Path downloadDir) throws IOException {
		File downloadStatusFile = new File(downloadDir.toString(), DOWNLOAD_STATUS_FILE);
		return (DownloadJob) JSON_MAPPER.readValue(downloadStatusFile, DownloadJob.class);
	}
}
