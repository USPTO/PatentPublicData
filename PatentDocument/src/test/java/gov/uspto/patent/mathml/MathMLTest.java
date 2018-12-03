package gov.uspto.patent.mathml;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.List;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class MathMLTest {

	@Test 
	public void MathTable() throws SAXException, DocumentException {
		String xmlForm = "<math><mtable><mtr><mtd><mrow><mi>a</mi><mo>/</mo><mi>b</mi></mrow></mtd></mtr></mtable></math>";
        String expect = "math(mtable(mtr(mtd(mrow(mi(a)mo(/)mi(b))))))";

        MathML mathML = MathML.read(new StringReader(xmlForm));
        String stringForm = mathML.getStringForm();
        System.out.println(stringForm);
        assertEquals(expect, stringForm);
	}
	
    @Test
    public void MathML2Short() throws SAXException, DocumentException {
        String xmlForm = "<math><mrow><mi>x</mi><mo>+</mo><mrow><mi>a</mi><mo>/</mo><mi>b</mi></mrow></mrow></math>";
        String expect = "math(mrow(mi(x)mo(+)mrow(mi(a)mo(/)mi(b))))";

        MathML mathML = MathML.read(new StringReader(xmlForm));
        String stringForm = mathML.getStringForm();

        assertEquals(expect, stringForm);
    }
    
    @Test
    public void MathML2Short_NormIds() throws SAXException, DocumentException {
        String xmlForm = "<math><mrow><mi>x</mi><mo>+</mo><mrow><mi>a</mi><mo>/</mo><mi>b</mi></mrow></mrow></math>";
        String expect = "math(mrow(mi(id1)mo(+)mrow(mi(id2)mo(/)mi(id3)";

        MathML mathML = MathML.read(new StringReader(xmlForm));
        String stringForm = mathML.getStringForm();
        stringForm = mathML.normalizeVariables(stringForm);
        
        assertEquals(expect, stringForm);
    }

    @Test
    public void MathML2Short_NormCons() throws SAXException, DocumentException {
        String xmlForm = "<math><mrow><mi>a</mi><mo>+</mo> <msup> <mi>b</mi><mn>2</mn></msup></mrow></math>";
        String expect = "math(mrow(mi(a)mo(+)msup(mi(b)mn(con))))";
        
        MathML mathML = MathML.read(new StringReader(xmlForm));
        String stringForm = mathML.getStringForm();
        stringForm = mathML.generalizeConstance(stringForm);
        
        assertEquals(expect, stringForm);
    }

    @Test
    public void MathML_Tokenized() throws SAXException, DocumentException {
        String xmlForm = "<math><mrow><mi>a</mi><mo>+</mo> <msup> <mi>b</mi><mn>2</mn></msup></mrow></math>";

        String expect1 = "math(mrow(mi(a)mo(+)msup(mi(b)mn(2))))";   // short form.
        String expect2 = "math(mrow(mi(id1)mo(+)msup(mi(id2)";   // normalized ids.
        String expect3 = "math(mrow(mi(id1)mo(+)msup(mi(id2)"; // normalized ids and generalized constants
        String expect4 = "math(mrow(mi(id)mo(+)msup(mi(id)mn(2))))"; // generalized ids and original constants
        String expect5 = "math(mrow(mi(a)mo(+)msup(mi(b)mn(con))))"; // original ids and generalized constants
        String expect6 = "math(mrow(mi(id)mo(+)msup(mi(id)mn(con))))"; //  generalized ids and generalized constants
        String expect7 = "a,+,b,2"; // list of values.
        String expect8 = "+,2,a,b"; // list of values sorted.

        MathML mathML = MathML.read(new StringReader(xmlForm));
        List<String> mathForms = mathML.tokenize();
        assertEquals(expect1, mathForms.get(0));
        assertEquals(expect2, mathForms.get(1));
        assertEquals(expect3, mathForms.get(2));
        assertEquals(expect4, mathForms.get(3));
        assertEquals(expect5, mathForms.get(4));
        assertEquals(expect6, mathForms.get(5));
        assertEquals(expect7, mathForms.get(6));
        assertEquals(expect8, mathForms.get(7));
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
