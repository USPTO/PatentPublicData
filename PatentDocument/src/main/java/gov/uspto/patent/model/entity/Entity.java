package gov.uspto.patent.model.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Entity can have a second name associated with it. Such as the business or organization they work for.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public abstract class Entity {

	private final EntityType entityType;
	private final Name name;
	private final List<EntityRelationship> relations = new ArrayList<EntityRelationship>();
    private final Address address; 

	public Entity(EntityType type, Name name, Address address){
		this.entityType = type;
		this.name = name;
		this.address = address;
	}

	public void addRelationship(EntityRelationship relationship){
		relations.add(relationship);
	}
	
	public void addRelationship(Name name, RelationshipType relType){
		relations.add( new EntityRelationship(name, relType) );
	}
	
	public List<EntityRelationship> getRelations(){
		return relations;
	}

	public EntityType getEntityType(){
		return entityType;
	}

	public Name getName(){
		return name;
	}
	
	public Address getAddress(){
	    return address;
	}

	@Override
	public String toString() {
		return " name=" + name + ", relations=" + relations + ", address=" + address;
	}
}
