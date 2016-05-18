package gov.uspto.parser.dom4j;

import org.dom4j.Document;

import gov.uspto.patent.TextProcessor;

/**
 * DOMFrgmentReader
 * 
 * Will generally have a Constuctor 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @param <T>
 */
public abstract class DOMFragmentReader<T> implements Reader<T> {

	protected Document document;
	protected TextProcessor textProcessor;

	public DOMFragmentReader(Document document){
		this.document = document;
	}
	
	public DOMFragmentReader(Document document, TextProcessor textProcessor){
		this.document = document;
		this.textProcessor = textProcessor;
	}
}
