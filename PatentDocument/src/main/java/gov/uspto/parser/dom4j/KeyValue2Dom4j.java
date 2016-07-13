package gov.uspto.parser.dom4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.google.common.base.Preconditions;

import gov.uspto.patent.PatentReaderException;

/**
 *<h3>Key Value to Dom4j Document</h3>
 *
 *<p>
 *Create a Dom4j {@link Document} from key-value freetext document.
 *Format is key-valued, with support of multi-line values.
 *</p>
 *<p>
 *Note: Requires a list of Sections to held delineate fields from text continuing on the next line.
 *</p>
 *<p>
 *<b>Example:</b>
 *<pre>
 *{@code
 *SECTION
 *FIELDNAME value
 *FIELDNAME value start
 *  value continues.
 *}
 *</pre>
 *<b>Becomes:</b>
 *<pre>
 *{@code
 *<SECTION>
 *   <FIELDNAME>value<FIELDNAME>
 *   <FIELDNAME>value start
 *     value continues.</FIELDNAME>
 *</SECTION>
 *}
 *</pre>
 *</p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 */
public class KeyValue2Dom4j {
	//private static final Logger LOGGER = LoggerFactory.getLogger(KeyValue2Dom4j.class);

	private final static Pattern KEY_VALUE_PATTERN = Pattern.compile("^([A-Z][A-Z0-9]{2,3})\\s(.+?)$");

	private Collection<String> sectionNames;

	public KeyValue2Dom4j(Collection<String> sectionNames){
		Preconditions.checkNotNull(sectionNames, "Provided sections can not be Null.");
		Preconditions.checkArgument(!sectionNames.isEmpty(), "Provided sections can not be empty.");
		this.sectionNames = sectionNames;
	}

	public Document parse(Path docPath)	throws PatentReaderException, IOException {
		return parse(docPath.toFile());
	}

	public Document parse(File file) throws PatentReaderException, IOException {
		try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")){
			return parse(reader);
		}
	}

	public Document parse(Reader reader) throws PatentReaderException {
		Document document = DocumentHelper.createDocument();
		Element rootNode = document.addElement("DOCUMENT");

		try (BufferedReader buffreader = new BufferedReader(reader)) {

			String line;
			String lastLineValue = "";

			org.dom4j.Element currentSection = null;
			String currentField = null;

			while (buffreader.ready() && (line = buffreader.readLine()) != null) {

				line = line.replaceFirst("\\s+$", "");

				String[] lineParts = processLine(line);

				if (lineParts.length == 1) {

					/*
					 * Key only and SectionName is defined.
					 */
					if (isSection(lineParts[0])) {
						//LOGGER.info("section: {}", lineParts[0]);

						/*
						 * Add Last Section and proceed with new Section.
						 */
						if (currentSection != null) {
							if (currentField != null) {

								Element field = DocumentHelper.createElement(currentField);
								field.setText(lastLineValue.trim());
								currentSection.add(field);
								
								currentField = null;
								lastLineValue += "";
							}
							rootNode.add(currentSection);
						}

						String sectionName = lineParts[0];
						currentSection = DocumentHelper.createElement(sectionName);

					} else {
						/*
						 * Field value continues on next line, so append.
						 */
						lastLineValue += lineParts[0];
					}
				} else if (lineParts.length == 2) {

						//LOGGER.info("\t field: {}", lineParts[0]);

						/*
						 * Add last Field and proceed to new Field.
						 */
						if (currentField != null) {
							if (currentSection != null) {
								Element field = DocumentHelper.createElement(currentField);
								field.setText(lastLineValue.trim());
								currentSection.add(field);
							} else {
								throw new PatentReaderException("SECTION is not defined for: " + currentField);
							}
						}

						currentField = lineParts[0];
						lastLineValue = lineParts[1];

				}
			}

			if (document != null) {
				/*
				 * Add Last Field and Section.
				 */
				Element field = DocumentHelper.createElement(currentField);
				field.setText(lastLineValue.trim());
				currentSection.add(field);
				rootNode.add(currentSection);
			}
		} catch (IOException e) {
			throw new PatentReaderException(e);
		}

		return document;
	}

	private Boolean isSection(String name) {
		return sectionNames.contains(name);
	}

	private String[] processLine(final String line) {
		Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
		if (matcher.find()) {
			String key = matcher.group(1);
			String value = matcher.group(2);
			return new String[] { key, value.replaceAll("\\s+", " ") };
		} else {
			return new String[] { line.replaceAll("\\s+", " ") };
		}
	}

	public static void main(String[] args) throws PatentReaderException, IOException {

		/*
		 * Greenbook Sections
		 */
		Set<String> sections = new HashSet<String>();
		sections.add("PATN");
		sections.add("INVT");
		sections.add("ASSG");
		sections.add("PRIR");
		sections.add("REIS");
		sections.add("RLAP");
		sections.add("CLAS");
		sections.add("UREF");
		sections.add("FREF");
		sections.add("OREF");
		sections.add("LREP");
		sections.add("PCTA");
		sections.add("ABST");
		sections.add("GOVT");
		sections.add("PARN");
		sections.add("BSUM");
		sections.add("DRWD");
		sections.add("DETD");
		sections.add("CLMS");
		sections.add("DCLM");

		KeyValue2Dom4j parser = new KeyValue2Dom4j(sections);
		Document document = parser.parse(Paths.get(args[0]));

		System.out.println(Dom4jUtil.toText(document));
	}

}
