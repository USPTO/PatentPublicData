package gov.uspto.patent.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Build Claim Tree by adding child claims to each claim a claim is dependent on; 
 * In other words adding the current claim as a child to the claim referenced.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ClaimTreeBuilder {

    private final List<Claim> claims;

    public ClaimTreeBuilder(final List<Claim> claims) {
        this.claims = claims;
    }

    public void build() {
        for (Claim claim : claims) {
            if (claim.getDependentIds() != null && claim.getDependentIds().size() > 0) {
                List<Claim> dependentClaims = getClaims(claim.getDependentIds());
                for (Claim patentClaim : dependentClaims) {
                    patentClaim.addChildClaim(claim);
                }
            }
        }

        for (Claim claim : claims) {
            if (ClaimType.INDEPENDENT.equals(claim.getClaimType())) {
                claim.setClaimTreeLevel(0);
                createLevel(claim);
            }
        }
    }

    /**
     * Recursively iterate over claims adding it's tree level or depth to each claim. 
     * 
     * @param claim
     */
    public void createLevel(Claim claim) {
        for (Claim childClaim : claim.getChildClaims()) {
            if (childClaim.getClaimTreeLevel() == -1) {
                childClaim.setClaimTreeLevel(claim.getClaimTreeLevel() + 1);
                createLevel(childClaim);
            }
        }
    }

    /**
     * Get Claim list from list of claim ids.
     * 
     * @param claimIds
     * @return
     */
    public List<Claim> getClaims(Collection<String> claimIds) {
        List<Claim> foundClaims = new ArrayList<Claim>();
        for (String claimId : claimIds) {
            for (Claim claim : this.claims) {
                if (claim.getId().equals(claimId)) {
                    foundClaims.add(claim);
                }
            }
        }
        return foundClaims;
    }
}
