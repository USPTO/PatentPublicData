package gov.uspto.patent;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import gov.uspto.parser.dom4j.Dom4j;
import gov.uspto.patent.doc.greenbook.Greenbook;
import gov.uspto.patent.doc.pap.PatentAppPubParser;
import gov.uspto.patent.doc.sgml.Sgml;
import gov.uspto.patent.doc.xml.ApplicationParser;
import gov.uspto.patent.doc.xml.GrantParser;
import gov.uspto.patent.model.Patent;

/**
 * Detect Patent Document Type and/or Parse Document into Patent Object
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PatentReader implements PatentDocReader<Patent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PatentReader.class);

	private static final long DEFAULT_MAX_BYTES = 100000000; // about 100 MB.

	private PatentDocFormat patentDocFormat;
	private long maxByteSize = DEFAULT_MAX_BYTES;

	private static Map<PatentDocFormat, Class<? extends Dom4j>> FORMAT_PARSER = new HashMap<>();
	static {
		FORMAT_PARSER.put(PatentDocFormat.Greenbook, Greenbook.class);
		FORMAT_PARSER.put(PatentDocFormat.Pap, PatentAppPubParser.class);
		FORMAT_PARSER.put(PatentDocFormat.RedbookApplication, ApplicationParser.class);
		FORMAT_PARSER.put(PatentDocFormat.RedbookGrant, GrantParser.class);
		FORMAT_PARSER.put(PatentDocFormat.Sgml, Sgml.class);
	}

	/**
	 * Load Reader
	 * 
	 * @param reader
	 * @param PatentType
	 */
	public PatentReader(final PatentDocFormat patentDocFormat) {
		Preconditions.checkNotNull(patentDocFormat, "patentType can not be Null");
		this.patentDocFormat = patentDocFormat;
	}

	public void setMaxByteSize(long maxByteSize) {
		this.maxByteSize = maxByteSize;
	}

	/**
	 * Parse Document and Return Patent Object.
	 * 
	 * @param reader
	 * @return
	 * @throws PatentReaderException
	 * @throws IOException
	 */
	@Override
	public Patent read(Reader reader) throws PatentReaderException, IOException {
		Preconditions.checkNotNull(reader, "reader can not be Null");
		Preconditions.checkNotNull(patentDocFormat, "patentDocFormat can not be Null");

		if (!checkSize(reader)) {
			return readLarge(reader);
			//throw new PatentReaderException("Patent too Large");

		} else if (FORMAT_PARSER.get(patentDocFormat) == null) {
			throw new PatentReaderException("Detected Patent Type Not Defined" + patentDocFormat);
		}

		try {
			return FORMAT_PARSER.get(patentDocFormat).newInstance().parse(reader);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new PatentReaderException(e);
		}
	}

	/**
	 * Partially Read Large Patents
	 *
	 * <p>Drop the fields likely to contain the large content (the description 
	 * and/or the claim fields).</p> 
	 *
	 * <p>Justification: Chemical Patents can contain large multiple page tables in the
	 * claims, other Patents can have large Detailed Descriptions.</p>
	 *
	 * @param reader
	 * @return Patent
	 * @throws PatentReaderException
	 */
	public Patent readLarge(Reader reader) throws PatentReaderException {
		LOGGER.warn("Patent size over threshold of '{}' bytes, Performing partial read of the large patent. "
				+ "Skipping the description and claim fields.",	maxByteSize);
		if (FORMAT_PARSER.get(patentDocFormat) == null) {
			throw new PatentReaderException("Detected Patent Type Not Defined" + patentDocFormat);
		}

		String[] skipExactPaths = new String[] { 
			"/us-patent-grant/description",
			"/us-patent-grant/claims",
			"/us-patent-application/description",
			"/us-patent-application/claims",
			"/PATDOC/SDODE", // Patent SGML Description
			"/PATDOC/SDOCL" // Patent SGML Claims
		};

		try {
			return FORMAT_PARSER.get(patentDocFormat).newInstance().parse(reader, Arrays.asList(skipExactPaths));
		} catch (InstantiationException | IllegalAccessException e) {
			throw new PatentReaderException(e);
		}
	}

	public boolean checkSize(Reader reader) throws IOException {
		int c;
		long charCount = 0;
		while (-1 != (c = reader.read())) {
			charCount++;
		}
		reader.reset();

		return (charCount * 2 < maxByteSize);
	}
}
