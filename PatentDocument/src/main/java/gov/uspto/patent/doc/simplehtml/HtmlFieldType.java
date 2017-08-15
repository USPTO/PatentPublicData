package gov.uspto.patent.doc.simplehtml;

public enum HtmlFieldType {
	HEADER("h", "h1", "h2", "h3", "h4", "h5", "h6", "heading", "p[id^=h-]"),
	TABLE("table", "tbody", "th", "tr", "td", "entry", "row", "pre[class=freetext-table]"),
	LIST("ul", "ol", "li", "dl", "dt", "dd"),

	MATHML("math", "span[class=math]"),
	FIGREF("a[class=figref]"),
	CLAIMREF("a[class=claim-ref]"),
	PATCITE("a[class=patcite]"),
	NPLCITE("a[class=nplcite]"),
	CROSSREF("a[class=crossref]");

	private String[] nodeNames;

	private HtmlFieldType(String... nodeNames){
		this.nodeNames = nodeNames;
	}

	public String[] getNodeNames(){
		return nodeNames;
	}
}
