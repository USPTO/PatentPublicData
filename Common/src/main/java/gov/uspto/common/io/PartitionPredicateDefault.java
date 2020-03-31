package gov.uspto.common.io;

/**
 * Partition Predicate with threshold on line record row count and file size
 * 
 * @author Brian G. Feldman<brian.feldman@uspto.gov>
 *
 */
public class PartitionPredicateDefault implements PartitionPredicate {
	private int recordCount = 0;
	private long currentSize = 0;

	private final int recordLimit;
	private final long sizeLimitMB;

	/**
	 * Uses recordLimit default of Integer.MAX_VALUE (2,147,483,647).
	 * @param sizeLimitMB - size limit in MB
	 */
	PartitionPredicateDefault(final int sizeLimitMB) {
		this(Integer.MAX_VALUE, sizeLimitMB);
	}

	/**
	 * @param recordLimit - integer with max value of 2,147,483,647 is large enough to keep partition size digestible
	 * @param sizeLimitMB - size limit in MB
	 */
	PartitionPredicateDefault(final int recordLimit, final int sizeLimitMB) {
		this.recordLimit = recordLimit;
		this.sizeLimitMB = sizeLimitMB * 1048576;
	}

	@Override
	public int getRecordCount() {
		return recordCount;
	}

	@Override
	public boolean thresholdReached(String str) {
		currentSize += str.length();
		recordCount++;
		if (recordCount == recordLimit || currentSize >= sizeLimitMB) {
			return true;
		}
		return false;
	}

	@Override
	public void restCounts() {
		recordCount = 0;
		currentSize = 0;
	}

	@Override
	public boolean hasRecordLimit() {
		return recordLimit > 0 || recordLimit != Integer.MAX_VALUE;
	}

	@Override
	public String toString() {
		return "PartitionPredicateDefault [recordLimit=" + recordLimit + ", sizeLimitMB=" + sizeLimitMB + "]";
	}

}