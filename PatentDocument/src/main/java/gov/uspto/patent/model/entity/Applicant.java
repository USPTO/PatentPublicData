package gov.uspto.patent.model.entity;

/**
 * The person who files a patent application. 
 * In the US the applicant is always the Inventor. 
 * In other countries, the applicant and assignee are the same.
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
