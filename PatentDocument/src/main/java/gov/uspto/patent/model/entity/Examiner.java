package gov.uspto.patent.model.entity;

import gov.uspto.patent.model.ExaminerType;

public class Examiner extends Entity {

	private final String department;
	private final ExaminerType type;

	public Examiner(Name name, String department, ExaminerType type){
		super(EntityType.EXAMINER, name);
		this.department = department;
		this.type = type;
	}

	public String getDepartment() {
		return department;
	}

	public ExaminerType getExaminerType(){
		return type;
	}

	@Override
	public String toString() {
		return "Examiner [ name=" + getName() + ", department=" + department + ", type=" + type + "]";
	}
}
