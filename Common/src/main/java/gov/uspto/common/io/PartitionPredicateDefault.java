package gov.uspto.common.io;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Partition Predicate with threshold on line record row count and file size
 * 
 * @author Brian G. Feldman<brian.feldman@uspto.gov>
 *
 */
public class PartitionPredicateDefault implements PartitionPredicate {
	private AtomicInteger recordCount = new AtomicInteger(0);
	private AtomicInteger currentSize = new AtomicInteger(0);

	private final int recordLimit;
	private final int sizeLimitMB;

	PartitionPredicateDefault(final int sizeLimitMB) {
		this(Integer.MAX_VALUE, sizeLimitMB);
	}

	PartitionPredicateDefault(final int recordLimit, final int sizeLimitMB) {
		this.recordLimit = recordLimit;
		this.sizeLimitMB = sizeLimitMB * 1048576;
	}

	@Override
	public boolean thresholdReached(String str) {
		currentSize.addAndGet(str.length());
		recordCount.incrementAndGet();
		if (recordCount.intValue() == recordLimit || currentSize.intValue() >= sizeLimitMB) {
			return true;
		}
		return false;
	}

	@Override
	public void restCounts() {
		recordCount.set(0);
		currentSize.set(0);
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