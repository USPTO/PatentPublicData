package gov.uspto.common.io;

public interface PartitionPredicate {
	boolean thresholdReached(String str);
	boolean hasRecordLimit();
	void restCounts();
}
