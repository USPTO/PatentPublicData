package gov.uspto.patent.doc.greenbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import gov.uspto.parser.keyvalue.KeyValue;
import gov.uspto.parser.keyvalue.KvReader;
import gov.uspto.patent.PatentReaderException;

/**
 * Convert Patent Greenbook to XML
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Greenbook2GreenbookXml {

	/*
	 * private static final Set<String> SECTIONS = new HashSet<String>(20); static {
	 * SECTIONS.add("PATN"); SECTIONS.add("INVT"); SECTIONS.add("ASSG");
	 * SECTIONS.add("PRIR"); SECTIONS.add("REIS"); SECTIONS.add("RLAP");
	 * SECTIONS.add("CLAS"); SECTIONS.add("UREF"); SECTIONS.add("FREF");
	 * SECTIONS.add("OREF"); SECTIONS.add("LREP"); SECTIONS.add("PCTA");
	 * SECTIONS.add("ABST"); SECTIONS.add("GOVT"); SECTIONS.add("PARN");
	 * SECTIONS.add("BSUM"); SECTIONS.add("DRWD"); SECTIONS.add("DETD");
	 * SECTIONS.add("CLMS"); SECTIONS.add("DCLM"); }
	 */

	private final OutputFormat outFormat;

	public Greenbook2GreenbookXml(boolean prettyPrint, Charset outCharset) {
		if (prettyPrint) {
			outFormat = OutputFormat.createPrettyPrint();
		} else {
			outFormat = OutputFormat.createCompactFormat();
		}
		outFormat.setEncoding(outCharset.name());
	}

	public Document parse(File file) throws IOException, PatentReaderException {
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			KvReader kvReader = new KvReader();
			List<KeyValue> keyValues = kvReader.parse(reader);
			return kvReader.genXml(keyValues);
		}
	}

	public void writeFile(Document document, Path outDir, String outFileName) throws IOException {
		XMLWriter writer = new XMLWriter(new FileWriter(outDir.resolve(outFileName).toFile()), outFormat);
		try {
			writer.write(document);
		} finally {
			writer.close();
		}
	}

	public void stdout(Document document) throws IOException {
		XMLWriter writer = new XMLWriter(System.out, outFormat);
		writer.write(document);
	}

	public static void main(String[] args) throws PatentReaderException, IOException {
		String filename = args[0];
		File file = new File(filename);
		
		Greenbook2GreenbookXml g2xml = new Greenbook2GreenbookXml(true, StandardCharsets.UTF_8);

		if (file.isDirectory()) {
			int count = 1;
			for (File subfile : file.listFiles()) {
				System.out.println(count++ + " " + subfile.getAbsolutePath());
				Document dom4jDoc = g2xml.parse(subfile);
				String outFileName = subfile.getName() + ".xml";
				Path outDir = Paths.get(".");
				try {
					g2xml.writeFile(dom4jDoc, outDir, outFileName);
				} catch (IOException e) {
					System.err.println("Failed to write: " + outFileName);
					e.printStackTrace();
				}
			}
		} else {
			Document dom4jDoc = g2xml.parse(file);
			try {
				g2xml.stdout(dom4jDoc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
