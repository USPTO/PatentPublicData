package gov.uspto.patent.model.entity;

/**
 * Applicant
 * 
 * <p>
 * The person who files a patent application.
 * </p>
 * <p>
 * In the US the applicant is by default always the Inventor. In other
 * countries, the applicant and assignee (patent owner) are the same.
 * </p>
 */
public class Applicant extends Entity {

	private String sequence;

	public Applicant(Name name, Address address) {
		super(EntityType.APPLICANT, name, address);
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	@Override
	public String toString() {
		return "Applicant[ name=" + getName() + " , sequence=" + sequence + ", address=" + getAddress() + "]";
	}
}
