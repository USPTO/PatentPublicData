package gov.uspto.bulkdata.reader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;

import com.google.common.base.Preconditions;

public class MultiEventReader implements XMLEventReader  {

    private XMLEventReader reader;
    private boolean isXMLEvent = false;
    private int level = 0;
    
	private File file;
	private String xmlBodyTag;
	private String xmlStartTag;
	private String xmlEndTag;

	public MultiEventReader(InputStream inputStream, String xmlBodyTag) throws XMLStreamException {
		Preconditions.checkNotNull(inputStream, "inputStream can not be Null");
		Preconditions.checkNotNull(xmlBodyTag, "XmlBodyTag can not be Null");

		this.xmlBodyTag = xmlBodyTag;
		this.xmlStartTag = "<" + xmlBodyTag;
		this.xmlEndTag = "</" + xmlBodyTag;
		
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
		    this.reader = factory.createXMLEventReader(inputStream);   
		} catch (XMLStreamException e) {
		    e.printStackTrace();
		}

		startXML();
	}

	/**
	 * Advance to Start of patent XML Document.
	 * @throws XMLStreamException
	 */
    private void startXML() throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            //System.out.println(event.toString());
            if (event.isStartElement()) {
            	StartElement startElement = event.asStartElement();
            	if (startElement.getName().getLocalPart().startsWith(xmlBodyTag) ){
            		System.out.println( "matched start" );
            	}
                //return;
            }
            
            if (event.isEndElement()) {
            	EndElement endElement = event.asEndElement();
            	if (endElement.getName().getLocalPart().startsWith(xmlBodyTag) ){
            		System.out.println( "matched end" );
            	}
               // return;
            }
        }
    }

    public boolean hasNextXML() {
        return reader.hasNext();
    }

    public XMLEvent nextXML() throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.peek();
            if (event.isStartElement()) {
                isXMLEvent = true;
                return event;
            }
            return reader.nextEvent();
        }
		return null;
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        if (event.isStartElement()) {
            level++;
        }
        if (event.isEndElement()) {
            level--;
            if (level == 0) {
                isXMLEvent = false;
            }
        }
        return event;
    }

    @Override
    public boolean hasNext() {
        return isXMLEvent;
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        XMLEvent event = reader.peek();
        if (level == 0) {
            while (event != null && !event.isStartElement() && reader.hasNext()) {
                reader.nextEvent();
                event = reader.peek();
            }
        }
        return event;
    }
    
    @Override
    public void close() throws XMLStreamException {
    	reader.close();
    }

    @Override
    public String getElementText() throws XMLStreamException {
    	throw new UnsupportedOperationException("getElementText not supported");
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
    	throw new UnsupportedOperationException("nextTag not supported");
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
    	throw new UnsupportedOperationException("getProperty not supported");
    }

    @Override
    public Object next() {
    	throw new UnsupportedOperationException("next not supported");
    }

    @Override
    public void remove() {
    	throw new UnsupportedOperationException("remove not supported");
    }
    
	public static void main(String[] args) throws XMLStreamException, TransformerException, IOException {
		
		final String RECORD_XML_TAG = "us-patent";
	
		Path zipFilePath = Paths.get("../download/ipa150305.zip");
		ZippedXmlReader xmlZip = new ZippedXmlReader(zipFilePath);
		InputStream xmlStream = xmlZip.open();
		
		new Demo().write(xmlStream);
		
		//MultiEventReader mer = new MultiEventReader(xmlStream, RECORD_XML_TAG);
		//XMLEvent event = mer.nextXML();
	

		
		
		//xsw.close();
	}

}
