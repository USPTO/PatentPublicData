package gov.uspto.patent.bulk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.MDC;

public class DumpFileAps extends DumpFile {

	private static final String START_TAG = "PATN";

	private final String startTag;
	private boolean startTagSeen = false;
	private int currentRecCount;

	public DumpFileAps(File file) {
		this(file, START_TAG);
	}

	public DumpFileAps(String name, BufferedReader reader) {
		this(name, reader, START_TAG);
	}

	public DumpFileAps(File file, String startTag) {
		super(file, StandardCharsets.ISO_8859_1);
		this.startTag = startTag;
	}

	public DumpFileAps(String name, BufferedReader reader, String startTag) {
		super(name, reader);
		this.startTag = startTag;
	}

	@Override
	public String read() throws IOException {
		StringBuilder content = new StringBuilder();

		String line;
		while (super.getReader().ready() && (line = super.getReader().readLine()) != null) {
			if (line.startsWith(startTag)) {
				if (startTagSeen) {
					currentRecCount++;
					MDC.put("RECNUM", String.valueOf(currentRecCount));
					return START_TAG + "\n" + content.toString();
				}
				startTagSeen = true;
			} else {
				content.append(line).append('\n');
			}
		}

		if (content.length() == 0) {
			return null; // no more records.
		} else {
			// last record
			currentRecCount++;
			MDC.put("RECNUM", String.valueOf(currentRecCount));
			return START_TAG + "\n" + content.toString();
		}
	}

	@Override
	public int getCurrentRecCount() {
		return currentRecCount;
	}
}
