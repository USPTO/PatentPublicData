package gov.uspto.common.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gov.uspto.common.text.WordUtil;

public class WordUtilTest {

    @Before
    public void setUp() throws Exception {
    }
 
    @Test
    public void isAlphaNumbericTest() {
        assertTrue(WordUtil.isAlphaNumberic("TEST1"));
        assertTrue(WordUtil.isAlphaNumberic("test1"));
    }

    @Test
    public void isUpperAlphaNumbericTest() {
        assertTrue("Uppercase Letters with Number", WordUtil.isUpperAlphaNumberic("TEST1"));
        assertFalse("Lowercase Letters with Number", WordUtil.isUpperAlphaNumberic("test1"));
    }

    @Test
    public void initialsTest() {
        String initials = WordUtil.initials("POST of Office");
        assertEquals("PO", initials);

        String initials2 = WordUtil.initials("The post-office", new char[] { ' ', '-' });
        assertEquals("po", initials2);
    }

    @Test
    public void capitalTest() {
        String capitals = WordUtil.getCapital("Wireless LAN");
        assertEquals("WLAN", capitals);
    }

    @Test
    public void capitalizeTitle() {
        String capitals = WordUtil.capitalizeTitle("president of the club", new char[] { ' ' });
        assertEquals("President of the Club", capitals);

        String capitals2 = WordUtil.capitalizeTitle("president Of The club", new char[] { ' ' });
        assertEquals("President of the Club", capitals2);

        String capitals3 = WordUtil.capitalizeTitle("president Of The CLUB", new char[] { ' ' });
        assertEquals("President of the Club", capitals3);
    }

    @Test
    public void hasWord() {
        List<String> stopwords = new ArrayList<String>();
        stopwords.add("stop");

        boolean found = WordUtil.hasWord("one two stop three", stopwords);
        ;
        assertEquals(true, found);

        boolean found2 = WordUtil.hasWord("one two three", stopwords);
        ;
        assertEquals(false, found2);
    }

    @Test
    public void hasLeadWord() {
        List<String> stopwords = new ArrayList<String>();
        stopwords.add("stop");

        boolean found = WordUtil.hasLeadWord("stop two stop three", stopwords, false);
        ;
        assertEquals(true, found);

        boolean found2 = WordUtil.hasLeadWord("one stop two three", stopwords, false);
        assertEquals(false, found2);
    }

    @Test
    public void hasCaracter() {
        boolean found = WordUtil.hasCharacter("one-two", "-");
        assertEquals(true, found);

        boolean found2 = WordUtil.hasCharacter("one two", "-");
        assertEquals(false, found2);
    }

    @Test
    public void wordCount() {
        int wordCount = WordUtil.countWords("Wireless LAN", new char[] { ' ' });
        assertEquals(2, wordCount);
    }

    @Test
    public void longestCommonSubstringTest() {
        String longestCommon = WordUtil.longestCommonSubstring("Local Area Network Connection", "Local Area Network");
        assertEquals("Local Area Network", longestCommon);

        String longestCommon1 = WordUtil.longestCommonSubstring("Ethernet Local Area Network Connection",
                "Local Area Network");
        assertEquals("Local Area Network", longestCommon1);

        String longestCommon2 = WordUtil.longestCommonSubstring("Ethernet Local Area Network Connection", "123");
        assertEquals("", longestCommon2);
    }

}
