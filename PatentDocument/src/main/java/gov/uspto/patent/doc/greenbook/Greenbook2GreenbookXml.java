package gov.uspto.patent.doc.greenbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import gov.uspto.parser.dom4j.keyvalue.KeyValue;
import gov.uspto.parser.dom4j.keyvalue.KvReader;
import gov.uspto.patent.PatentReaderException;

/**
 * Convert Patent Greenbook to XML
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Greenbook2GreenbookXml {

    /*
	private static final Set<String> SECTIONS = new HashSet<String>(20);
	static {
		SECTIONS.add("PATN");
		SECTIONS.add("INVT");
		SECTIONS.add("ASSG");
		SECTIONS.add("PRIR");
		SECTIONS.add("REIS");
		SECTIONS.add("RLAP");
		SECTIONS.add("CLAS");
		SECTIONS.add("UREF");
		SECTIONS.add("FREF");
		SECTIONS.add("OREF");
		SECTIONS.add("LREP");
		SECTIONS.add("PCTA");
		SECTIONS.add("ABST");
		SECTIONS.add("GOVT");
		SECTIONS.add("PARN");
		SECTIONS.add("BSUM");
		SECTIONS.add("DRWD");
		SECTIONS.add("DETD");
		SECTIONS.add("CLMS");
		SECTIONS.add("DCLM");
	}
    */

    public Document parse(File file) throws IOException, PatentReaderException{
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            KvReader kvReader = new KvReader();
            List<KeyValue> keyValues = kvReader.parse(reader);
            return kvReader.genXml(keyValues);
        }
    }
    
	public static void writeFile(Document document, Path outDir, String outFileName) throws IOException{
	    OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter( new FileWriter( outDir.resolve(outFileName).toFile() ) );
        writer.write( document );
        writer.close();
	}

	public static void stdout(Document document) throws IOException{
        OutputFormat format = OutputFormat.createPrettyPrint();
        //OutputFormat format = OutputFormat.createCompactFormat();
        XMLWriter writer = new XMLWriter( System.out, format );
        writer.write( document );
	}
	
	public static void main(String[] args) throws PatentReaderException, IOException {
		String filename = args[0];
		File file = new File(filename);

		Greenbook2GreenbookXml g2xml = new Greenbook2GreenbookXml();

		if (file.isDirectory()) {
			int count = 1;
			for (File subfile : file.listFiles()) {
				System.out.println(count++ + " " + subfile.getAbsolutePath());
				Document dom4jDoc = g2xml.parse(subfile);
				String outFileName = subfile.getName()+".xml";
				Path outDir = Paths.get(".");
				try {
					Greenbook2GreenbookXml.writeFile(dom4jDoc, outDir, outFileName);
				} catch (IOException e) {
					System.err.println("Failed to write: "+ outFileName);
					e.printStackTrace();
				}
			}
		} else {
			Document dom4jDoc = g2xml.parse(file);
			try {
				Greenbook2GreenbookXml.stdout(dom4jDoc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
