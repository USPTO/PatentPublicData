package gov.uspto.patent.doc.simplehtml;

import static org.junit.Assert.assertEquals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

public class HtmlToPlainTextTest {

	@Test
	public void Superscript() {
		FreetextConfig config = new FreetextConfig(true, false);

		String xml = "<p>metallic barium ion can lose 2 electrons to become Ba<sup>2+</sup></p>";

		String expect = "\nmetallic barium ion can lose 2 electrons to become Ba^{2+}\n";

		Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser());
		HtmlToPlainText converter = new HtmlToPlainText(config);
		String plainText = converter.getPlainText(jsoupDoc);
		// System.out.println(plainText);

		assertEquals(expect, plainText);
	}

	@Test
	public void Subscript() {
		FreetextConfig config = new FreetextConfig(true, false);

		String xml = "<p>Water or H<sub>2</sub>O</p>";

		String expect = "\nWater or H_{2}O\n";

		Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser());
		HtmlToPlainText converter = new HtmlToPlainText(config);
		String plainText = converter.getPlainText(jsoupDoc);
		// System.out.println(plainText);

		assertEquals(expect, plainText);
	}

	@Test
	public void ParagraphIndent() {
		FreetextConfig config = new FreetextConfig(true, true);

		String xml = "<p>This is a paragraph.</p>";

		String expect = "\n\tThis is a paragraph.\n";

		Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser());
		HtmlToPlainText converter = new HtmlToPlainText(config);
		String plainText = converter.getPlainText(jsoupDoc);
		// System.out.println(plainText);

		assertEquals(expect, plainText);
	}

	@Test
	public void Remove_Delete_Annotation() {
		FreetextConfig config = new FreetextConfig(false, false);
		config.remove("del");

		String xml = "<p>This was <del>deleted</del><ins>inserted</p>";

		String expect = "\\nThis was inserted\\n";

		Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser());
		HtmlToPlainText converter = new HtmlToPlainText(config);
		String plainText = converter.getPlainText(jsoupDoc);
		// System.out.println(plainText);

		assertEquals(expect, plainText);
	}

	@Test
	public void Replace_Annotation() {
		FreetextConfig config = new FreetextConfig(true, false);
		config.replace("figref", "Patent-Figure");

		String xml = "<p>According to <figref>Fig 1.</figref>:</p>";

		String expect = "\nAccording to Patent-Figure:\n";

		Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser());
		HtmlToPlainText converter = new HtmlToPlainText(config);
		String plainText = converter.getPlainText(jsoupDoc);
		// System.out.println(plainText);

		assertEquals(expect, plainText);
	}

	@Test
	public void HTML_Quote_Tag() {
		FreetextConfig config = new FreetextConfig(false, false);
		config.remove("del");

		String xml = "<p><q>My quote of the day.</q></p>";

		String expect = "\\n“My quote of the day.”\\n";

		Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser());
		HtmlToPlainText converter = new HtmlToPlainText(config);
		String plainText = converter.getPlainText(jsoupDoc);
		// System.out.println(plainText);

		assertEquals(expect, plainText);
	}

	@Test
	public void HTML_BlockQuote() {
		FreetextConfig config = new FreetextConfig(false, false);
		config.remove("del");

		String xml = "<p><blockquote><p>My quote of the day.<br/>My quote second line.</p></blockquote></p>";

		String expect = "\\n\\n\\n\tMy quote of the day.\\n\tMy quote second line.\\n\\n";

		Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser());
		HtmlToPlainText converter = new HtmlToPlainText(config);
		String plainText = converter.getPlainText(jsoupDoc);
		// System.out.println(plainText);

		assertEquals(expect, plainText);
	}

}
