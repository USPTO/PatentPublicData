package gov.uspto.patent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class PatentTypeDetect {
	/**
	 * Determine XML body tag from bulk zip file name
	 * 
	 * @param filename
	 * @return
	 */
	public PatentType fromFileName(File file) {
		PatentType type = PatentType.findMimeType(file.getName());
		if (type == PatentType.Unknown) {
			if (file.getName().endsWith(".greenbook")) {
				type = PatentType.Greenbook;
			}
		}
		return type;
	}

	public PatentType fromContent(String content) throws IOException {
		return fromContent(new StringReader(content));
	}

	public PatentType fromContent(BufferedReader br) throws IOException {
		PatentType foundMimeType = PatentType.Unknown;
		br.mark(1000);
		LINES: for (int i = 0; br.ready() && i < 150; i++) {
			String line = br.readLine();
			for (PatentType type : PatentType.values()) {
				if (line.contains(type.getMatch())) {
					foundMimeType = type;
					break LINES;
				}
			}
		}
		br.reset();

		return foundMimeType;
	}

	public PatentType fromContent(Reader reader) throws IOException {
		PatentType foundMimeType = PatentType.Unknown;
		try (BufferedReader br = new BufferedReader(reader)) {
			// PAP-XML contains list of entities for each image embodiment first number of lines (seen 38+ lines in header).
			LINES: for (int i = 0; br.ready() && i < 150; i++) {
				String line = br.readLine();
				for (PatentType type : PatentType.values()) {
					if (line.contains(type.getMatch())) {
						foundMimeType = type;
						break LINES;
					}
				}
			}
		}
		reader.reset();

		return foundMimeType;
	}

	private PatentType fromContent(File file) throws IOException {
		if (file.getName().endsWith(".zip")) {
			return PatentType.Unknown;
		} else {
			return fromContent(new FileReader(file));
		}
	}

	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);

		PatentTypeDetect detector = new PatentTypeDetect();

		PatentType typeFileName = detector.fromFileName(file);
		PatentType typeFileContent = detector.fromContent(file);

		System.out.println("MimeType from name: " + typeFileName.getMimeType() + " --- MimeType from content: "
				+ typeFileContent.getMimeType());
	}
}
