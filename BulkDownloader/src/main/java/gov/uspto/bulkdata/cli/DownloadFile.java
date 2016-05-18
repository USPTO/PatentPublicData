package gov.uspto.bulkdata.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import okhttp3.OkHttpClient;

import gov.uspto.bulkdata.downloader.Downloader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Download File from URL.
 * 
 *<p>
 *--url="http://opennlp.sourceforge.net/models-1.5/en-sent.bin" --dir="../download"
 *<p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class DownloadFile extends Downloader {
	private final transient static OkHttpClient client = new OkHttpClient();

	public DownloadFile(){
		super(client, 3);
	}

	public static void main(String[] args) throws IOException {

		OptionParser parser = new OptionParser() {
			{
				accepts("url").withRequiredArg().ofType(String.class).describedAs("url string").required();
				accepts("dir").withOptionalArg().ofType(String.class).describedAs("download directory").defaultsTo("download");
			}
		};

		OptionSet options = parser.parse(args);
		String url = (String) options.valueOf("url");
		String dir = (String) options.valueOf("dir");

		Path downloadDir = Paths.get(dir);

		DownloadFile downloader = new DownloadFile();
		downloader.setup(downloadDir);
		downloader.download(url, downloadDir);
	}

}
