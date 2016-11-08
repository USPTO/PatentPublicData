package gov.uspto.patent.mathml;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class MathMLTest {

    @Test
    public void MathML2Short() throws SAXException, DocumentException {
        String xmlForm = "<math><mrow><mi>x</mi><mo>+</mo><mrow><mi>a</mi><mo>/</mo><mi>b</mi></mrow></mrow></math>";
        String expect = "math(mrow(mi(x)mo(+)mrow(mi(a)mo(/)mi(b))))";

        MathML mathML = MathML.read(new StringReader(xmlForm));
        String stringForm = mathML.getStringForm();

        assertEquals(expect, stringForm);
    }
    
    //@Test  functionality does not yet work.
    public void short2Mathml(){
        String stringForm = "math(mrow(mi(x)mo(+)mrow(mi(a)mo(/)mi(b))))";
        String expect = "<math><mrow><mi>x</mi><mo>+</mo><mrow><mi>a</mi><mo>/</mo><mi>b</mi></mrow></mrow></math>";
        
        MathML mathML = MathML.fromText(stringForm);
        String xmlForm = mathML.getXML();

        assertEquals(expect, xmlForm);
    }

}
