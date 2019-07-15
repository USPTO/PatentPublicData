package gov.uspto.patent.model.entity;

import gov.uspto.patent.InvalidDataException;

/**
 * Assignee (Patent Owner)
 * 
 * <p>
 * Note: Application Patent Documents do not contain an Assignee, since no right
 * of ownership exists until the patent has been granted.
 * </p>
 *
 *<p>
 * Note: Assignee Type Codes starting with a "1" signifies part interest
 *</p>
 *
 * @author Brian G. Feldman<brian.feldman@uspto.gov>
 *
 */
public class Assignee extends Entity {

	public enum RoleType {
		T1("Unassigned"),
		T2("U.S. company or corporation"),
		T3("Foreign company or corporation"),
		T4("U.S individual"),
		T5("Foreign individual"),
		T6("U.S. Federal government"),
		T7("Foreign government"),
		T8("U.S. county government"),
		T9("U.S. state government"),
		XX("INVALID");

		private final String desc;

		private RoleType(String desc) {
			this.desc = desc;
		}

		public String getDesc() {
			return desc;
		}
	}

	private RoleType roleType;
	private boolean partInterest = false;

	public Assignee(Name name, Address address) {
		super(EntityType.ASSIGNEE, name, address);
	}

	public RoleType getRole() {
		return roleType;
	}

	public String getRoleDesc() {
		if (partInterest) {
			return roleType.getDesc() + "; part interest";
		} else {
			return roleType.getDesc() ;
		}
	}

	public boolean hasPartInterest() {
		return partInterest;
	}

	public void setRole(final String roleType) throws InvalidDataException {

		if (roleType == null || roleType.trim().length() == 0) {
			this.roleType = RoleType.XX;
			return;
		}

		String roleType2;
		if (roleType.length() == 2) {
			if (roleType.startsWith("1")) {
				this.partInterest = true;
				roleType2 = "T" + roleType.substring(1, 2);
			}
			else if (roleType.startsWith("0")) {
				roleType2 = "T" + roleType.substring(1, 2);
			}
			else {
				roleType2 = "XX";
			}
		} else {
			roleType2 = "T" + roleType;
		}

		try {
			this.roleType = RoleType.valueOf(roleType2);
		} catch (IllegalArgumentException e) {
			this.roleType = RoleType.XX;
			throw new InvalidDataException("Invalid Assignee Role Type : '" + roleType + "'");
		}
	}

	@Override
	public String toString() {
		return "Assignee[name=" + getName() + ", partInterest=" + hasPartInterest() + ", roleType=" + getRole() + ", roleDesc=" + getRoleDesc() + ", address=" + getAddress() + "]";
	}
}
