package gov.uspto.bulkdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Run Stats
 * 
 * <p>
 * Keep track of stats of running process, optionally include locations of
 * failure.
 * </p>
 *
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class RunStats {

	private String taskName;
	private long records = 0;
	private long success = 0;
	private long failure = 0;
	private List<String> failureSourceLocations;
	private List<RunStats> childRunStats; // such as stats per file, using name

	public RunStats(String taskName) {
		this.setTaskName(taskName);
	}

	public RunStats(String taskName, long records, long success, long fail) {
		this.setTaskName(taskName);
		this.setRecordCount(records);
		this.setSuccessCount(success);
		this.setFailCount(fail);
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return this.taskName;
	}

	public void setRecordCount(long records) {
		this.records = records;
	}

	public void incrementRecord() {
		this.addRecord(1);
	}

	public void addRecord(long count) {
		this.records = this.records + count;
	}

	public long getRecord() {
		return this.records;
	}

	public void setSuccessCount(long success) {
		this.success = success;
	}

	public void incrementSucess() {
		this.addSuccess(1);
	}

	public void addSuccess(long count) {
		this.success = this.success + count;
	}

	public long getSuccess() {
		return success;
	}

	public void setFailCount(long failure) {
		this.failure = failure;
	}

	public void incrementFail() {
		this.addFailure(1);
	}

	public void addFailure(long count) {
		this.failure = this.failure + count;
	}

	public void addFailureSourceLocation(String sourceLocation) {
		if (failureSourceLocations == null) {
			failureSourceLocations = new ArrayList<String>();
		}

		if (sourceLocation != null) {
			failureSourceLocations.add(sourceLocation);
		}
	}

	public void incrementFailure(String sourceLocation) {
		this.addFailureSourceLocation(sourceLocation);
		this.addFailure(1);
	}

	public long getFailure() {
		return failure;
	}

	public void add(RunStats... runStatss) {
		if (childRunStats == null) {
			childRunStats = new ArrayList<RunStats>();
		}
		for (RunStats stats : runStatss) {
			childRunStats.add(stats);
			addRecord(stats.getRecord());
			addSuccess(stats.getSuccess());
			addFailure(stats.getFailure());
		}
	}

	public List<RunStats> getChildren() {
		return childRunStats;
	}

	public RunStats getChild(String taskName) {
		if (taskName == null || taskName.isEmpty()) {
			return null;
		}
		for(RunStats stat: childRunStats) {
			if (taskName.equals(stat.getTaskName())){
				return stat;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "RunStats [taskName=" + taskName + ", records=" + records + ", success=" + success + ", failure=" + failure
				+ ", failureSourceLocations=" + Arrays.toString(failureSourceLocations.toArray()) + ", childRunStats="
				+ Arrays.toString(childRunStats.toArray()) + "]";
	}
}
