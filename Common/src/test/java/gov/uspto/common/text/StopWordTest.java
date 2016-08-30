package gov.uspto.common.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gov.uspto.common.text.StopWord;

public class StopWordTest {
	private StopWord stopword;

	@Before
	public void setup() throws IOException {
		Path stopWordFile = Paths.get("src/test/resources/stopwords.txt");
		stopword = new StopWord(stopWordFile);
		stopword.load();
	}

	@Test(expected = IllegalArgumentException.class)
	public void fileExistCheck() throws IOException {
		Path stopWordFile = Paths.get("123BADFILE");
		stopword = new StopWord(stopWordFile);
	}

	@Test
	public void stopWordContainedString() throws IOException {
		assertTrue(stopword.contains("the mountain"));
	}

	@Test
	public void stopWordNotContainedString() throws IOException {
		assertFalse(stopword.contains("tall mountain"));
	}

	@Test
	public void stopWordContainedStringArray() throws IOException {
		assertTrue(stopword.contains(new String[] { "the", "mountain" }));
	}

	@Test
	public void stopWordContainedList() throws IOException {
		List<String> list = new ArrayList<String>();
		list.add("the");
		list.add("mountain");
		assertTrue(stopword.contains(list));
	}

	@Test
	public void stopWordIsTest() throws IOException {
		assertTrue(stopword.isStopWord("the"));
	}

	@Test
	public void stopWordRemoveString() throws IOException {
		assertEquals(stopword.remove("the mountain"), "mountain");
	}

	@Test
	public void stopWordRemoveArray() throws IOException {
		assertEquals(stopword.remove(new String[] { "the", "mountain" }), new String[] { "mountain" });
	}

	@Test
	public void stopWordRemoveArrayLocation() throws IOException {
		assertEquals(stopword.remove(new String[] { "the", "mountain" }, StopWord.LOCATION.ANY),
				new String[] { "mountain" });
	}

	@Test
	public void stopWordRemoveLeadingString() throws IOException {
		assertEquals(stopword.removeLeading("the mountain the"), "mountain the");
	}

	@Test
	public void stopWordRemoveLeadingStringLocation() throws IOException {
		assertEquals(stopword.remove("the mountain the", StopWord.LOCATION.LEADING), "mountain the");
	}

	@Test
	public void stopWordRemoveTrailingString() throws IOException {
		assertEquals(stopword.removeTrailing("the mountain the"), "the mountain");
	}

	@Test
	public void stopWordRemoveTrailingStringLocation() throws IOException {
		assertEquals(stopword.remove("the mountain the", StopWord.LOCATION.TRAILING), "the mountain");
	}

	@Test
	public void stopWordRemoveTrailingStringArray() throws IOException {
		assertEquals(stopword.removeTrailing(new String[] { "the", "mountain", "the" }),
				new String[] { "the", "mountain" });
	}

	@Test
	public void stopWordRemoveTrailingStringArrayLocation() throws IOException {
		assertEquals(stopword.remove(new String[] { "the", "mountain", "the" }, StopWord.LOCATION.TRAILING),
				new String[] { "the", "mountain" });
	}

	@Test
	public void stopWordRemoveEdgeString() throws IOException {
		assertEquals(stopword.removeEdge("the mountain the"), "mountain");
	}

	@Test
	public void stopWordRemoveEdgeStringLocation() throws IOException {
		assertEquals(stopword.remove("the mountain the", StopWord.LOCATION.EDGE), "mountain");
	}

	@Test
	public void stopWordRemoveContainsStringLocationThusKill() throws IOException {
		assertEquals(stopword.remove("the mountain", StopWord.LOCATION.CONTAINS), "");
	}

	@Test
	public void stopWordRemoveEdgeStringArray() throws IOException {
		assertEquals(stopword.removeTrailing(new String[] { "the", "mountain", "the" }),
				new String[] { "the", "mountain" });
	}

	@Test
	public void stopWordLeading() throws IOException {
		assertEquals(stopword.hasLeading("the mountain"), true);
	}

	@Test
	public void stopWordLeadingLocation() throws IOException {
		assertTrue(stopword.has("the mountain", StopWord.LOCATION.LEADING));
	}

	@Test
	public void stopWordTrailing() throws IOException {
		assertTrue(stopword.hasTrailing(new String[] { "mountain", "the" }));
	}

	@Test
	public void stopWordTrailingStringTrue() throws IOException {
		assertTrue(stopword.hasTrailing("mountain the"));
	}

	@Test
	public void stopWordTrailingStringFalse() throws IOException {
		assertFalse(stopword.hasTrailing("mountain tall"));
	}

	@Test
	public void stopWordTrailingArrayFalse() throws IOException {
		List<String> newList = new ArrayList<String>();
		newList.add("mountain");
		newList.add("tall");
		assertFalse(stopword.hasTrailing(newList));
	}

	@Test
	public void stopWordTrailingArrayTrue() throws IOException {
		List<String> newList = new ArrayList<String>();
		newList.add("mountain");
		newList.add("this");
		assertFalse(stopword.hasTrailing(newList));
	}

	@Test
	public void stopWordEdge() throws IOException {
		assertTrue(stopword.hasEdge(new String[] { "mountain", "the" }));
	}

	@Test
	public void stopWordEdgeStringTrue() throws IOException {
		assertTrue(stopword.hasEdge("mountain the"));
	}

	@Test
	public void stopWordEdgeStringFalse() throws IOException {
		assertFalse(stopword.hasEdge("mountain tall"));
	}

	@Test
	public void stopWordEdgeArrayFalse() throws IOException {
		List<String> newList = new ArrayList<String>();
		newList.add("mountain");
		newList.add("tall");
		assertFalse(stopword.hasEdge(newList));
	}

	@Test
	public void stopWordEdgeArrayTrue() throws IOException {
		List<String> newList = new ArrayList<String>();
		newList.add("mountain");
		newList.add("this");
		assertFalse(stopword.hasEdge(newList));
	}
}
