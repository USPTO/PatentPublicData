package gov.uspto.patent.doc.cpc.masterfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;

import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.classification.CpcClassification;

public class MasterClassificationRecord {

    private final DocumentId grantId;
    private final DocumentId appId;
    private final Collection<CpcClassification> cpcList;

    public MasterClassificationRecord(DocumentId grantId, DocumentId appId, Collection<CpcClassification> cpcList) {
        this.grantId = grantId;
        this.appId = appId;
        this.cpcList = cpcList;
    }

    public DocumentId getGrantId() {
        return grantId;
    }

    public DocumentId getAppId() {
        return appId;
    }

    public Collection<CpcClassification> getCpcList() {
        return cpcList;
    }

    public CpcClassification getMainCPC() {
        for (CpcClassification cpc : cpcList) {
            if (cpc.isMainClassification()) {
                return cpc;
            }
        }
        return null;
    }

    public List<CpcClassification> getFutherCPC() {
        List<CpcClassification> futherCPC = new ArrayList<CpcClassification>(cpcList.size()-1);
        for (CpcClassification cpc : cpcList) {
            if (!cpc.isMainClassification()) {
                futherCPC.add(cpc);
            }
        }
        return futherCPC;
    }

    @Override
    public String toString() {
        return "MasterClassificationRecord [\n\tgrantId=" + grantId + ",\n\t appId=" + appId + ",\n\t cpcList=" + cpcList + "\n\t]";
    }
}
