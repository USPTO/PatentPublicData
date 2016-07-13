package gov.uspto.parser.dom4j;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SystemEntityResolver implements EntityResolver {

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

		 System.out.println("Resolving entity: " + publicId);

		if (publicId.equals("html-entities.dtd")) {
			InputStream resourceAsStream = getResource("../PatentDocument/src/main/resources/dtd/html-entities.dtd");
			if (resourceAsStream != null) {
				return new InputSource(resourceAsStream);
			}
		}

		throw new IllegalArgumentException("Unrecognized publicId: " + publicId);
	}

	private InputStream getResource(String dtdFile) {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		InputStream resourceAsStream = contextClassLoader.getResourceAsStream(dtdFile);
		return resourceAsStream;
	}
}
