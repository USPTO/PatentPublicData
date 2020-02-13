package gov.uspto.patent.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Non-Patent Literature (NPL)
 *
 * <ul>
 * NPL can include a large variety of different citation types, for example:
 * <li>Books</li>
 * <li>Magazines</li>
 * <li>Academic Journals</li>
 * <li>Product Literature</li>
 * <li>Websites: (Blogs, Social Media, Online Sale of Similar Product)</li>
 * <li>Online Videos: (Youtube)</li>
 * </ul>
 *
 * <ul>
 * Note: Patent numbers occur within NPL, such as, but not limited, and may
 * change, the following:
 * <li>Unpublished "Applications"</li>
 * <li>Correspondence from US and foreign patent applications (office actions,
 * search reports, ect)</li>
 * <li>Litigation involving the application</li>
 * </ul>
 *
 * @author Brian G. Feldman <brian.feldman@uspto>
 *
 */
public class NplCitation extends Citation {

	private static Pattern QUOTED_TEXT = Pattern.compile("(?:[\"�]|<i>)([^\"�]+)(?:[\"�]|<\\/i>)");
	private String citeText; // raw cite text
	private DocumentId patDocId; // Patent DocumentId parsed from raw text.

	public NplCitation(String num, String citeText, Citation.CitedBy citedBy) {
		super(num, CitationType.NPLCIT, citedBy);
		this.citeText = citeText;
	}

	public void setPatentId(DocumentId docId) {
		this.patDocId = docId;
	}

	public DocumentId getPatentId() {
		return patDocId;
	}

	public String getQuotedText() {
		Matcher matcher = QUOTED_TEXT.matcher(citeText);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

	public String getCiteText() {
		return citeText;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof NplCitation) || this.citeText == null) {
			return false;
		} else {
			NplCitation other = (NplCitation) o;
			return this.citeText.equals(other.citeText);
		}
	}

	@Override
	public String toString() {
		return "NplCitation [num=" + super.getNum() + ", citeText=" + citeText + ", quotedText()=" + getQuotedText()
				+ " patentId=" + getPatentId() + ", citedBy=" + super.getCitedBy() + " ]";
	}
}
