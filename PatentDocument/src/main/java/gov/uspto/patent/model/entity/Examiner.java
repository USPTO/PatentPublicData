package gov.uspto.patent.model.entity;

import gov.uspto.patent.model.ExaminerType;

public class Examiner extends Entity {

	private final ExaminerType type;
	private String department;

	public Examiner(Name name, String department, ExaminerType type) {
		super(EntityType.EXAMINER, name, null);
		this.department = department;
		this.type = type;
	}

	public void setDepartment(String artUnit) {
		this.department = artUnit;
	}

	public String getDepartment() {
		return department;
	}

	public ExaminerType getExaminerType() {
		return type;
	}

	@Override
	public String toString() {
		return "Examiner [ name=" + getName() + ", department=" + department + ", type=" + type + "]";
	}
}
