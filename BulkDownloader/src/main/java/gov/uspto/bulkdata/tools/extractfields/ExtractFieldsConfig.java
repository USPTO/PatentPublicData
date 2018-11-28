package gov.uspto.bulkdata.tools.extractfields;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import gov.uspto.bulkdata.BulkReaderArguments;
import joptsimple.OptionParser;

/**
 * Extract Config from Command-line args
 * 
 * <code><pre>
 * ExtractConfig config = new ExtractConfig();
 * config.parseArgs(args);
 * config.readOptions();
 *</pre></code>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class ExtractFieldsConfig extends BulkReaderArguments {
	private Map<String, List<XPathExpression>> fields;
	private String outputType = "fields";
	private XPathExpression matchExpression;

	public ExtractFieldsConfig() {
		buildArgs(new OptionParser());
	}

	public OptionParser buildArgs(OptionParser opParser) {
		super.buildArgs(opParser);

		opParser.accepts("match").withOptionalArg().ofType(String.class).describedAs("Find documents which match XPATH expression");

		opParser.accepts("f").withRequiredArg().ofType(String.class)
				.describedAs("field to extract, multiple definitions are ok, in format: -f=\"fieldName:/XPATH\"");

		//opParser.accepts("type").withOptionalArg().ofType(String.class)
			//	.describedAs("types options: [csv,xml,json,text]").defaultsTo("csv");

		return opParser;
	}

	public void readOptions() {
		super.readOptions();

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		if (options.has("match")) {
			String match = (String) options.valueOf("match");
			XPathExpression matchExpression;
			try {
				matchExpression = xpath.compile(match);
				setMatchExpression(matchExpression);
			} catch (XPathExpressionException e) {
				System.err.println("Invalid XPath expression: " + match);
				System.exit(1);
			}
		}

		@SuppressWarnings("unchecked")
		List<String> fields = (List<String>) options.valuesOf("f");
		Map<String, List<XPathExpression>> wantedFields = new LinkedHashMap<String, List<XPathExpression>>(
				fields.size());



		for (String field : fields) {
			int idx = field.indexOf(':');
			if (idx > 0) {
				String fieldKey = field.substring(0, idx);
				String expressions = field.substring(idx + 1);

				// System.out.println(fieldKey + "-->" + expressions);

				try {
					List<XPathExpression> xpathList = new ArrayList<XPathExpression>();
					if (expressions.startsWith("(") && expressions.endsWith("")) {
						String xpathStrs = expressions.substring(1, expressions.length() - 1);
						for (String xpathStr : xpathStrs.split("\\s*,\\s*")) {
							xpathList.add(xpath.compile(xpathStr));
						}
					} else {
						xpathList.add(xpath.compile(expressions));
					}
					wantedFields.put(fieldKey, xpathList);
				} catch (XPathExpressionException e) {
					System.err.println("Invalid XPath expression: " + field);
					System.exit(1);
				}
			} else {
				System.err.println("Invalid Field definition: " + field);
				System.exit(1);
			}
		}
		setWantedFields(wantedFields);

		//wantedFields.entrySet().stream().forEach(System.out::println);
	}

	private void setMatchExpression(XPathExpression matchExpression) {
		this.matchExpression = matchExpression;
	}
	
	public XPathExpression getMatchExpression() {
		return this.matchExpression;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getOutputType() {
		return this.outputType;
	}

	public void setWantedFields(Map<String, List<XPathExpression>> fields) {
		this.fields = fields;
	}

	public Map<String, List<XPathExpression>> getWantedFields() {
		return this.fields;
	}

}