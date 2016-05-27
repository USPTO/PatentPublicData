package gov.uspto.patent.mathml;

import java.io.IOException;

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
import org.xml.sax.SAXException;

import gov.uspto.patent.PatentParserException;

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
public class MathML2 {

	private String mathml;
	private ASTNode mathNode;

	public MathML2(ASTNode mathNode) {
		this(null, mathNode);
	}

	public MathML2(String mathml, ASTNode mathNode) {
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
	 */
	public static MathML2 parseMathML(String mathml) throws IOException {
		MathMLReader reader = new MathMLReader();
		ASTNode mathNode = reader.parseMathMLFromString(mathml);
		return new MathML2(mathml, mathNode);
	}

	/**
	 * Parse from C style Arithmetic expression
	 * 
	 * @param mathText
	 * @return
	 * @throws IOException
	 */
	public static MathML2 parseText(String mathText) throws IOException {
		TextToASTNodeMathParser2 parser = new TextToASTNodeMathParser2();
		ASTRootNode root = new ASTRootNode();
		parser.parseString(mathText, root);

		MathML2 mathml = new MathML2(root);
		return mathml;
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

		//String content = new String(Files.readAllBytes(Paths.get(filename)));
		//MathML2 mathml = MathML2.parseMathML(content);

		MathML2 mathml = MathML2.parseText("(1 + 2) * 2");

		System.out.println("MATHML: " + mathml.getMathML());
		System.out.println("Text: " + mathml.toMathText());
	}
}
