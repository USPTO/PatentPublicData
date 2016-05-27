package gov.uspto.patent.mathml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.dom4j.DocumentException;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jmathml.ASTNode;
import org.jmathml.ASTRootNode;
import org.jmathml.ASTToXMLElementVisitor;
import org.jmathml.FormulaFormatter;
import org.jmathml.MathMLReader;
import org.jmathml.TextToASTNodeMathParser2;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import gov.uspto.patent.PatentParserException;
import uk.ac.ed.ph.snuggletex.SerializationMethod;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.XMLStringOutputOptions;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptionDefinitions;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptions;
import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

/**
 * 
 * Difference from Presentation MathML and Content MathML
 * -- Presentation MathML has content such as mrow
 * -- Content MathML has apply.
 * 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class MathML3 {

	private String mathml;
	private ASTNode mathNode;

	private static SnuggleEngine engine = new SnuggleEngine();

	public MathML3(ASTNode mathNode) {
		this(null, mathNode);
	}

	public MathML3(String mathml, ASTNode mathNode) {
		this.mathNode = mathNode;
		this.mathml = mathml;
	}

	public String getMathML() {
		if (mathml != null) {
			return mathml;
		} else {
			return ASTtoXML(mathNode);
		}
	}

	/**
	 * Math String into a C style Arithmetic expression
	 * 
	 * @return
	 */
	public String toMathText() {
		FormulaFormatter formatter = new FormulaFormatter();
		String text = formatter.formulaToString(mathNode);

		System.out.println("Formula: " + text);

		boolean grandChildExists = false;
		for (ASTNode n : mathNode.getChildren()) {
			if (n.getNumChildren() > 0) {
				grandChildExists = true;
				break;
			}
		}
		if (!grandChildExists && text.startsWith("(") && text.endsWith(")")) {
			text = text.substring(1, text.length() - 1).trim();
		}

		return text;
	}

	/**
	 * ASTNode to XML
	 * 
	 * @param node
	 * @return
	 */
	private String ASTtoXML(ASTNode node) {
		ASTToXMLElementVisitor visitor = new ASTToXMLElementVisitor();
		//ASTRootNode root = new ASTRootNode();
		node.accept(visitor);

		Element element = visitor.getElement();

		org.jdom.Document mathDoc = new org.jdom.Document();
		mathDoc.setRootElement(element);
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		String xml = xmlOut.outputString(mathDoc);
		xml = xml.replaceAll("math:", "");
		return xml;
	}

	/**
	 * Parse MathML
	 * 
	 * @param mathml
	 * @return
	 * @throws IOException
	 * @throws SAXException 
	 */
	public static void parseMathML(String mathml) throws IOException, SAXException {

		//Document doc = MathMLUtilities.parseMathMLDocumentString(mathml);
		//System.out.println("Doc: " + doc.toString());

		SnuggleEngine engine = new SnuggleEngine();
		SnuggleSession session = engine.createSession();
		SnuggleInput input = new SnuggleInput(mathml);
		session.parseInput(input);
		
		UpConversionOptions options = new UpConversionOptions();
		options.setSpecifiedOption(UpConversionOptionDefinitions.DO_CONTENT_MATHML_NAME, "true");
		options.setSpecifiedOption(UpConversionOptionDefinitions.DO_MAXIMA_NAME, "true");
		options.setSpecifiedOption(UpConversionOptionDefinitions.ADD_OPTIONS_ANNOTATION_NAME, "true");

		UpConvertingPostProcessor upconvert = new UpConvertingPostProcessor(options);
		
		//Document updoc = new MathMLUpConverter().upConvertASCIIMathML(doc, defaultOptions);

		/* Specify how we want the resulting XML */
		XMLStringOutputOptions xmlOptions = new XMLStringOutputOptions();
		xmlOptions.setSerializationMethod(SerializationMethod.XHTML);
		xmlOptions.setIndenting(true);
		xmlOptions.setEncoding("UTF-8");
		xmlOptions.setAddingMathSourceAnnotations(true);
		xmlOptions.setUsingNamedEntities(true); /* (Only used if caller has an XSLT 2.0 processor) */
		
		System.out.println(session.buildXMLString(xmlOptions));
	}

	/**
	 * Parse from C style Arithmetic expression
	 * 
	 * @param mathText
	 * @return
	 * @throws IOException
	 */
	public static void parseLaTex(String mathText) throws IOException {
		SnuggleEngine engine = new SnuggleEngine();
		SnuggleSession session = engine.createSession();
		SnuggleInput input = new SnuggleInput(mathText);
		session.parseInput(input);

		/* Specify how we want the resulting XML */
		XMLStringOutputOptions options = new XMLStringOutputOptions();
		options.setSerializationMethod(SerializationMethod.XHTML);
		options.setIndenting(true);
		options.setEncoding("UTF-8");
		options.setAddingMathSourceAnnotations(true);
		options.setUsingNamedEntities(true); /* (Only used if caller has an XSLT 2.0 processor) */

		/* Convert the results to an XML String, which in this case will
		 * be a single MathML <math>...</math> element. */
		System.out.println(session.buildXMLString(options));
	}

	public static void main(String[] args) throws PatentParserException, DocumentException, SAXException, IOException {
		String filename = args[0];

		/*
		FileReader reader = new FileReader(new File(filename));
		
		SAXReader sax = new SAXReader(false);
		sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Document document = sax.read(reader);
		
		MathML2 mathml = new MathML2(document.getRootElement());
		*/

		String content = new String(Files.readAllBytes(Paths.get(filename)));
		MathML3.parseMathML(content);

		//MathML3.parseLaTex("$$ \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a} $$");

		//System.out.println("MATHML: " + mathml.getMathML());
		//System.out.println("Text: " + mathml.toMathText());
	}
}
