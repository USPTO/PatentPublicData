package gov.uspto.patent;

import org.dom4j.Document;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.Patent;

/**
 * Content handler proxy which builds a Patent document by executing each Fragment/Section Reader.
 */
public class Dom4JContentHander {

	private DOMFragmentReader[] readers;

	private Dom4JContentHander(DOMFragmentReader... readers){
		this.readers = readers;
	}

    public Patent process(Document document, Patent patent){
        for (DOMFragmentReader reader : readers) {
        	//reader.read(document, patent);
        }
        return patent;
    }
}
