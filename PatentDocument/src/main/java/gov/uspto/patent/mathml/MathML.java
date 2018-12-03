package gov.uspto.patent.mathml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;

import gov.uspto.patent.PatentReaderException;

/**
 * MathML - Parse to support Information Retrieval of Math Formulas
 * 
 * Convert MathML XML tree to string format.
 *
 * <p>
 * 
 * <pre>
 *{@code
 *<math xmlns="http://www.w3.org/1998/Math/MathML">
 *  <mrow>
 *  <mi>a</mi>
 *  <mo>+</mo>
 *  <msup><mi>b</mi><mn>2</mn></msup>
 *</mrow>
 *</math>
 *}
 * </pre>
 * </p>
 * <p>
 * String form:<br/>
 * math(mrow(mi(a)mo(+)msup(mi(b)mn(2))))
 * </p>
 * <p>
 * Normalized:<br/>
 * math(mrow(mi(id)mo(+)msup(mi(id)mn(2))))
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class MathML {

	private final Node mathNode;

	private final static Pattern REPLACE_VARS = Pattern.compile("mi\\(([a-zA-Z])\\)");
	private final static Pattern REPLACE_CONS = Pattern.compile("mn\\(([0-9]+)\\)");

	public MathML(Node node) {
		this.mathNode = node;
	}

	public Node getXMLNode() {
		return mathNode;
	}

	public String getXML() {
		return mathNode.asXML();
	}

	/**
	 * String Form
	 * 
	 * <p>
	 * math(mrow(mi(a)mo(+)msup(mi(b)mn(2))))
	 * </p>
	 *
	 * @return
	 */
	public String getStringForm() {
		return parse(mathNode);
	}

	protected String parse(Node node) {
		StringBuilder sb = new StringBuilder();
		sb.append("math(");
		mathmlToString(sb, node);
		sb.append(")");
		return sb.toString();
	}

	private void mathmlToString(StringBuilder sb, Node node) {

		List<Node> children = node.selectNodes("*");

		if (isMrowOrMathOrMfenced(node.getName()) && children.size() <= 1) {
			if (children.size() == 1) {
				mathmlToString(sb, children.get(0));
			}
		} else {
			sb.append(node.getName());

			Element el = (Element) node;
			el.attributes();
			for (int i = 0; i < el.attributeCount(); i++) {
				String attrName = el.attribute(i).getName();
				String value = el.getStringValue();
				sb.append("[").append(attrName).append("=").append(value).append("]");
			}
		}

		if (children.size() > 1 || isMtable(node.getName())) {
			sb.append("(");
			for (int i = 0; i < children.size(); i++) {
				mathmlToString(sb, children.get(i));
			}
			sb.append(")");
		} else {
			String value = node.getText().trim();
			if (!value.isEmpty()) {
				sb.append("(").append(value).append(")");
			}
		}
	}

	public String generalizeVariables(String mathMLString) {
		Matcher mReplace = REPLACE_VARS.matcher(mathMLString);
		if (mReplace.find()) {
			mathMLString = mReplace.replaceAll("mi(id)");
		}

		return mathMLString;
	}

	public String normalizeVariables(String mathMLString) {
		int currentConstantNum = 0;
		Map<String, Integer> constantMap = new HashMap<String, Integer>();
		Matcher mReplace = REPLACE_VARS.matcher(mathMLString);
		StringBuffer stb = new StringBuffer();
		while (mReplace.find()) {
			String constant = mReplace.group(1);
			if (!constantMap.containsKey(constant)) {
				constantMap.put(constant, ++currentConstantNum);
			}
			mReplace.appendReplacement(stb, "mi(id" + constantMap.get(constant) + ")");
		}

		return stb.toString();
	}

	public String generalizeConstance(String mathMLString) {
		Matcher mReplace = REPLACE_CONS.matcher(mathMLString);
		if (mReplace.find()) {
			mathMLString = mReplace.replaceAll("mn(con)");
		}

		return mathMLString;
	}

	private List<String> getValues(String mathMLString) {
		List<String> values = new ArrayList<String>();

		List<Node> textNodes = mathNode.selectNodes("//*[text()]");
		for (Node node : textNodes) {
			String value = node.getText().trim();
			if (!value.isEmpty() && !"=".equals(value)) {
				values.add(value);
			}
		}

		return values;
	}

	/**
	 * Tokenized Mathematical Equation for Searching
	 * 
	 * <p>
	 * Each descending token in the list returned is more generalized, less
	 * exacting, or more fuzzy of a match.
	 * </p>
	 *
	 * <ul>
	 * <li>Text Form</li>
	 * <li>Text Form, normalize variables</li>
	 * <li>Text Form, normalize variables and generalize constants</li>
	 * <li>Text Form, generalized variables and original constants</li>
	 * <li>Text Form, original variables and generalized constants</li>
	 * <li>Text Form, generalized variables and constants</li>
	 * <li>list of variables and constants</li>
	 * <li>sorted list of variables and constants</li>
	 * </ul>
	 *
	 * @return
	 */
	public List<String> tokenize() {
		String mathMLString = parse(mathNode);
		List<String> tokens = new ArrayList<String>();
		tokens.add(mathMLString);
		tokens.add(normalizeVariables(mathMLString));
		tokens.add(generalizeConstance(normalizeVariables(mathMLString)));
		tokens.add(generalizeVariables(mathMLString));
		tokens.add(generalizeConstance(mathMLString));
		tokens.add(generalizeConstance(generalizeVariables(mathMLString)));

		List<String> valueLst = getValues(mathMLString);
		String values = Joiner.on(",").join(valueLst);
		tokens.add(values);

		Collections.sort(valueLst);
		String sortedValues = Joiner.on(",").join(valueLst);
		tokens.add(sortedValues);

		return tokens;
	}

	private static boolean isMtable(String nodeName) {
		return nodeName != null && (nodeName.equals("mtable") || nodeName.equals("mtr") || nodeName.equals("mtd"));
	}

	private static boolean isMrowOrMathOrMfenced(String nodeName) {
		return nodeName != null && (nodeName.equals("math") || nodeName.equals("mrow") || nodeName.equals("mfenced"));
	}

	@Override
	public String toString() {
		return "MathML [mathNode=" + getXML() + ",\n getStringForm()=" + getStringForm() + ",\n parse()="
				+ parse(mathNode) + ",\n tokenize()=" + tokenize() + "]";
	}

	/**
	 * @FIXME does not yet work.
	 * 
	 * @param string
	 * @return
	 */
	public static MathML fromText(String string) {
		// convert back to XML format. math(mrow(mi(a)mo(+)msup(mi(b)mn(2))))

		Pattern pattern = Pattern.compile("(\\w+\\((?:[^()]+)*\\))");

		Pattern outerPattern = Pattern.compile("(\\w+\\(" + pattern + "*\\))");

		// Document document = DocumentHelper.createDocument();
		// Element root = document.addElement("math");

		Matcher matcher = outerPattern.matcher(string);
		while (matcher.find()) {
			String matched = matcher.group(1);
			System.out.println(matched);
		}

		// Element author1 = root.addElement( "author" ).addText( "James Strachan" );
		// return new MathML(document.getRootElement());
		return null;
	}

	public static MathML read(Reader reader) throws SAXException, DocumentException {
		SAXReader sax = new SAXReader(false);
		sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Document document = sax.read(reader);

		return new MathML(document.getRootElement());
	}

	public static void main(String[] args)
			throws FileNotFoundException, PatentReaderException, DocumentException, SAXException {
		String filename = args[0];

		FileReader reader = new FileReader(new File(filename));
		MathML mathml = MathML.read(reader);

		System.out.println(mathml.toString());
	}
}
