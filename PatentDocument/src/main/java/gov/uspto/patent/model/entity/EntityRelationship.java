package gov.uspto.patent.model.entity;

public class EntityRelationship {
	
	private final Name name;
	private final RelationshipType relType;
	
	public EntityRelationship(Name name, RelationshipType relType){
		this.name = name;
		this.relType = relType;
	}

	public Name getName() {
		return name;
	}

	public RelationshipType getRelType() {
		return relType;
	}

	@Override
	public String toString() {
		return "EntityRelationship[name=" + name + ", relType=" + relType + "]";
	}
}
