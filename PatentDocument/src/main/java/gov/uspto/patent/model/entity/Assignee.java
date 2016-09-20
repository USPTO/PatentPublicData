package gov.uspto.patent.model.entity;

import java.util.HashMap;
import java.util.Map;

import gov.uspto.patent.InvalidDataException;

public class Assignee extends Entity {
	
	public static final Map<String, String> AssigneeRoleType = new HashMap<String, String>();
	static {
		AssigneeRoleType.put("01", "Unassigned");
		AssigneeRoleType.put("02", "U.S. company or corporation");
		AssigneeRoleType.put("03", "Foreign company or corporation");
		AssigneeRoleType.put("04", "U.S individual");
		AssigneeRoleType.put("05", "Foreign individual");
		AssigneeRoleType.put("06", "U.S. Federal government");
		AssigneeRoleType.put("07", "Foreign government");
		AssigneeRoleType.put("08", "U.S. county government");
		AssigneeRoleType.put("09", "U.S. state government");
	}

	private String roleType;

	public Assignee(Name name, Address address){
		super(EntityType.ASSIGNEE, name, address);
	}

	public String getRole() {
		return roleType;
	}

	public String getRoleDesc(){
		return AssigneeRoleType.get(roleType);
	}

	public void setRole(String roleType) throws InvalidDataException {
		if (roleType == null || roleType.trim().length() == 0){
			return;
		}

		if (roleType.length() == 1){
			roleType = "0" + roleType;
		}

		if (AssigneeRoleType.containsKey(roleType)){
			this.roleType = roleType;
		} else {
			throw new InvalidDataException("Invalid Assignee Role Type: "+ roleType);
		}
	}

	@Override
	public String toString() {
		return "Assignee[name=" + getName() + "roleType=" + roleType + ", address=" + getAddress() + "]";
	}	
}
