package gov.uspto.bulkdata.grep;

public class OutputMatchConfig {

	public enum OUTPUT_MATCHING {
		RECORD, RECORD_LOCATION, RECORD_ID, PATTERN_CONTAINED, PATTERN_COVERED, COUNT, TOTAL_COUNT
	}

	private final OUTPUT_MATCHING outputType;
	private String recordSeperator = "\n"; // "\0"
	private int matchingWithinRecordLimit = -1;
	private boolean noSource = false;
	private boolean noCount = false;
	private boolean invertMatch = false;

	public OutputMatchConfig(OUTPUT_MATCHING outputType) {
		this.outputType = outputType;
	}

	public OUTPUT_MATCHING getOutputType() {
		return outputType;
	}

	public void setMatchingWithinRecordLimit(int maxlimit) {
		this.matchingWithinRecordLimit  = maxlimit;
	}

	public int getMatchingWithinRecordLimit() {
		return matchingWithinRecordLimit;
	}

	public boolean isNoSource() {
		return noSource;
	}

	public void setNoSource(boolean noSource) {
		this.noSource = noSource;
	}

	public boolean isNoCount() {
		return noCount;
	}

	public void setNoCount(boolean noCount) {
		this.noCount = noCount;
	}

	public boolean isInvertMatch() {
		return invertMatch;
	}

	public void setInvertMatch(boolean invertMatch) {
		this.invertMatch = invertMatch;
	}

	public String getRecordSeperator() {
		return recordSeperator;
	}

	public void setRecordSeperator(String recordSeperator) {
		this.recordSeperator = recordSeperator;
	}

}
