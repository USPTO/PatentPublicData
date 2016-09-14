package gov.uspto.patent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatentDocFormatDetect {
	private static final Logger LOGGER = LoggerFactory.getLogger(PatentDocFormatDetect.class);

	/**
	 * Determine XML body tag from bulk zip file name
	 * 
	 * @param filename
	 * @return
	 */
	public PatentDocFormat fromFileName(File file) {
	    PatentDocFormat format = PatentDocFormat.findMimeType(file.getName());
		if (format == PatentDocFormat.Unknown) {

			if (file.getName().endsWith(".greenbook") || file.getName().endsWith(".gbk")) {
			    format = PatentDocFormat.Greenbook;
			}
		}

		LOGGER.info("PatentDocFormat fromFileName: {}", format);

		return format;
	}

	public PatentDocFormat fromContent(String content) throws IOException {
		try (StringReader reader = new StringReader(content)) {
			return fromContent(reader);
		}
	}

	public PatentDocFormat fromContent(BufferedReader br) throws IOException {
	    PatentDocFormat foundMimeType = PatentDocFormat.Unknown;
		br.mark(1000);
		LINES: for (int i = 0; br.ready() && i < 150; i++) {
			String line = br.readLine();
			for (PatentDocFormat type : PatentDocFormat.values()) {
				if (line.contains(type.getMatch())) {
					foundMimeType = type;
					break LINES;
				}
			}
		}
		br.reset();

		LOGGER.info("PatentType fromContent: {}", foundMimeType);

		return foundMimeType;
	}

	public PatentDocFormat fromContent(Reader reader) throws IOException {
	    PatentDocFormat foundMimeType = PatentDocFormat.Unknown;
		try (BufferedReader br = new BufferedReader(reader)) {
			// PAP-XML contains list of entities for each image embodiment first
			// number of lines (seen 38+ lines in header).
			LINES: for (int i = 0; br.ready() && i < 150; i++) {
				String line = br.readLine();
				if (line != null) {
					for (PatentDocFormat type : PatentDocFormat.values()) {
						if (line.contains(type.getMatch())) {
							foundMimeType = type;
							break LINES;
						}
					}
				}
			}
		}

		LOGGER.info("PatentType fromContent: {}", foundMimeType);

		return foundMimeType;
	}

	private PatentDocFormat fromContent(File file) throws IOException {
		if (file.getName().endsWith(".zip")) {
			return PatentDocFormat.Unknown;
		} else {
			return fromContent(new FileReader(file));
		}
	}

	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);

		PatentDocFormatDetect detector = new PatentDocFormatDetect();

		PatentDocFormat formatFileName = detector.fromFileName(file);
		PatentDocFormat formatFileContent = detector.fromContent(file);

		System.out.println("MimeType from name: " + formatFileName.getMimeType() + " --- MimeType from content: "
				+ formatFileContent.getMimeType());
	}
}
