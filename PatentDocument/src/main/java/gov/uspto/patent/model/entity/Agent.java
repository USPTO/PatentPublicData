package gov.uspto.patent.model.entity;

public class Agent extends Entity {

	private final Address address;
	private final AgentRepType repType;
	private String sequence;

	public Agent(Name name, Address address, AgentRepType repType) {
		super(EntityType.AGENT, name);
		this.address = address;
		this.repType = repType;
	}

	public AgentRepType getRepType() {
		return repType;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public Address getAddress() {
		return address;
	}
	
	@Override
	public String toString() {
		return "Agent[Name=" + getName() + ", Address="+ address +", repType=" + repType + ", sequence=" + sequence + "]";
	}
}
