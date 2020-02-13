package gov.uspto.patent.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import gov.uspto.patent.model.CountryCode;

/**
 * 
 * Entity can have a second name associated with it. Such as the business or
 * organization they work for.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public abstract class Entity {

	private final EntityType entityType;
	private final Name name;
	private final Address address;
	private final List<EntityRelationship> relations = new ArrayList<EntityRelationship>();

	public Entity(EntityType type, Name name, Address address) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(name);
		this.entityType = type;
		this.name = name;
		if (address != null) {
			this.address = address;
		} else {
			this.address = new Address("", "", CountryCode.UNDEFINED);
		}
	}

	public void addRelationship(EntityRelationship relationship) {
		relations.add(relationship);
	}

	public void addRelationship(Name name, RelationshipType relType) {
		relations.add(new EntityRelationship(name, relType));
	}

	public List<EntityRelationship> getRelations() {
		return relations;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public Name getName() {
		return name;
	}

	public Address getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return " name=" + name + ", relations=" + relations + ", address=" + address;
	}
}
