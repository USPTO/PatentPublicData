package gov.uspto.common.io;

public interface PartitionPredicate {
	public int getRecordCount();
	boolean thresholdReached(String str);
	boolean hasRecordLimit();
	void restCounts();
}
