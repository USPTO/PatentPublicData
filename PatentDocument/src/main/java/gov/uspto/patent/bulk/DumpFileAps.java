package gov.uspto.patent.bulk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumpFileAps extends DumpFile {
	private static final Logger LOGGER = LoggerFactory.getLogger(DumpFileAps.class);

	private static final String startTag = "PATN";

	private boolean startTagSeen = false;
	private int currentRecCount;

	public DumpFileAps(File file) {
		super(file);
	}

	public DumpFileAps(String name, BufferedReader reader) {
		super(name, reader);
	}

	@Override
	public String read() {
		StringBuilder content = new StringBuilder();

		try {
			String line;
			while (super.getReader().ready() && (line = super.getReader().readLine()) != null) {
				if (startTagSeen == false) {
					if (line.startsWith(startTag)) {
						startTagSeen = true;
					}
				} else {
					if (line.startsWith(startTag)) {
						currentRecCount++;
						return startTag + "\n" + content.toString();
					} else {
						content.append(line).append('\n');
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Error while reading file: {}:{}", super.getFile(), currentRecCount, e);
		}

		if (content.length() == 0) {
			return null;
		} else {
			return startTag + "\n" + content.toString();
		}
	}

	@Override
	public void skip(int skipCount) throws IOException {
		for (int i = 1; i < skipCount; i++) {
			super.next();
			currentRecCount++;
		}
	}

	@Override
	public int getCurrentRecCount() {
		return currentRecCount;
	}
}
