package gov.uspto.patent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.google.common.base.Preconditions;

import gov.uspto.patent.greenbook.Greenbook;
import gov.uspto.patent.model.Patent;

/**
 * PatentParser 
 * 
 * detects and initializes parser.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PatentParser {

	private PatentXmlParser xmlParser = new PatentXmlParser();

	/**
	 * Parse CharSequence (String, StringBuffer, StringBuilder, CharBuffer)
	 *  
	 * @param xmlString
	 * @return
	 * @throws PatentParserException
	 */
	public Patent parse(CharSequence xmlString) throws PatentParserException {
		Preconditions.checkNotNull(xmlString, "xmlString can not be Null");
		return parse(new StringReader(xmlString.toString()));
	}

	/**
	 * Parse File
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws PatentParserException
	 */
	public Patent parse(File file) throws FileNotFoundException, PatentParserException {
		Preconditions.checkNotNull(file, "File can not be Null");
		return parse(new FileReader(file));
	}

	/**
	 * Parse InputStream (ByteArrayInputStream, FileInputStream, FilterInputStream, InputStream, ObjectInputStream, PipedInputStream, SequenceInputStream, StringBufferInputStream)
	 * 
	 * @param inputStream
	 * @return
	 * @throws PatentParserException
	 */
	public Patent parse(InputStream inputStream) throws PatentParserException {
		Preconditions.checkNotNull(inputStream, "InputStream can not be Null");
		return parse(new InputStreamReader(inputStream));
	}

	/**
	 * Parse Reader (BufferedReader, CharArrayReader, FilterReader, InputStreamReader, PipedReader, StringReader)
	 * 
	 * @param reader
	 * @return
	 * @throws PatentParserException
	 */
	public Patent parse(Reader reader) throws PatentParserException {
		Preconditions.checkNotNull(reader, "Reader can not be Null");
		

		BufferedReader buffread = new BufferedReader(reader);
		try {
			String firstLine = buffread.readLine();
			/*
			 * Route XML or SGML to PatentXmlParser.
			 */
			//if (firstLine.trim().startsWith("<?xml")){
			//	reader.reset();
			//	return xmlParser.parse(reader);
			//}
			if (firstLine.trim().startsWith("PATN")){
				reader.reset();
				return new Greenbook().parse(reader);
				//return new Greenbook(false, "PATN", "", new GreenbookMapper()).parse(reader);
			}
			else {
				reader.reset();
				return xmlParser.parse(reader);
			}
			
			//else {
			//	throw new PatentParserException("Failed to Detect Patent Type");
			//}
		} catch (IOException e) {
			throw new PatentParserException("Failed During Patent Type Detection", e);
		}
	}

}
