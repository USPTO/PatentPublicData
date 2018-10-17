package gov.uspto.bulkdata.tools.xslt;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import gov.uspto.bulkdata.RecordProcessor;

public class XsltRecordProcessor implements RecordProcessor {
	
	private final XsltConfig config;
	private TransformerFactory factory;
	private Templates template;
	private Transformer transformer;

	public XsltRecordProcessor(XsltConfig config) throws TransformerConfigurationException {
		this.config = config;
		this.factory = TransformerFactory.newInstance();
		Source xslt = new StreamSource(config.getXsltFile().toFile());
		this.template = factory.newTemplates(xslt);
		this.transformer = template.newTransformer();
		
		if (config.isPrettyPrint()) {
			transformer.setOutputProperty("indent", "yes");
		} else {
			transformer.setOutputProperty("indent", "no");
		}
	}

	@Override
	public Boolean process(String sourceTxt, String rawRecord, Writer writer) throws IOException {

		try {
			transformer.transform(new StreamSource(new StringReader(rawRecord)), new StreamResult(writer));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public void finish(Writer writer) throws IOException {
		// empty.
	}
}
