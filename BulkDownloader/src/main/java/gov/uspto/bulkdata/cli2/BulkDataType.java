package gov.uspto.bulkdata.cli2;

import java.net.MalformedURLException;

public enum BulkDataType {
		
	GAZETTE("data2/patent/officialgazzette/", "zip"),

	GRANT_MULTI_PAGE_IMAGES("data3/patent/grant/multipagetiff/", "zip"),
	GRANT_SINGLE_PAGE_IMAGES("data/patent/grant/yellowbook/", "tar"),
	GRANT_REDBOOK_WITH_IMAGES("grant/redbook/", "tar"),
	GRANT_REDBOOK_TEXT("data/patent/grant/redbook/fulltext/", "zip"),
	GRANT_REDBOOK_BIBLIO("data/patent/grant/redbook/bibliographic/", "zip"),

	APPLICATION_MULTI_PAGE_IMAGES("data3/patent/application/multipagetiff/", "zip"),
	APPLICATION_SINGLE_PAGE_IMAGES("data/patent/application/yellowbook/", "tar"),
	APPLICATION_REDBOOK_WITH_IMAGES("data/patent/application/redbook/", "tar"),
	APPLICATION_REDBOOK_TEXT("data/patent/application/redbook/fulltext/", "zip"),
	APPLICATION_REDBOOK_BIBLIO("data/patent/application/redbook/bibliographic/", "zip");

	private static String BASEURL = "https://bulkdata.uspto.gov/";
	private String restPath;
	private String suffix;

	BulkDataType(String restPath, String suffix){
		this.restPath = restPath;
		this.suffix = suffix;
	}

	public String getRestPath(){
		return restPath;
	}

	public String getSuffix(){
		return suffix;
	}

	public String getURL(String year) throws MalformedURLException{
		StringBuilder stb = new StringBuilder();
		return stb.append(BASEURL).append(this.restPath).append(year).append("/").toString();
	}

	public String getURL(Integer year) throws MalformedURLException{
		return this.getURL(String.valueOf(year));
	}
}
