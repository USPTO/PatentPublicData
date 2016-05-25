package gov.uspto.bulkdata.reader;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

public class Demo {

	public void write(InputStream inputStream) throws XMLStreamException, TransformerException{
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader reader = xif.createXMLStreamReader(inputStream);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();

        while(reader.hasNext()){
        	 int next = reader.next();

        	 if (next == XMLStreamConstants.START_ELEMENT){
        		 StAXSource source = new StAXSource(reader);
        		 StreamResult out = new StreamResult(System.out);
        		 t.transform(source, out);
        	 }
        }
	}

    public static void main(String[] args) throws Exception  {

    }

} 