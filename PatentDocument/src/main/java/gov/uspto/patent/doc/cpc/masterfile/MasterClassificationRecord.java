package gov.uspto.patent.doc.cpc.masterfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.classification.CpcClassification;

public class MasterClassificationRecord {

    private final DocumentId appId;
    private final DocumentId pubId;
    private final Collection<CpcClassification> cpcList;

    public MasterClassificationRecord(DocumentId appId, DocumentId pubId, Collection<CpcClassification> cpcList) {
        this.appId = appId;
        this.pubId = pubId;
        this.cpcList = cpcList;
    }

    public DocumentId getAppId() {
        return appId;
    }

    public DocumentId getPubId() {
        return pubId;
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
        List<CpcClassification> futherCPC = new ArrayList<CpcClassification>();
        for (CpcClassification cpc : cpcList) {
            if (!cpc.isMainClassification()) {
                futherCPC.add(cpc);
            }
        }
        return futherCPC;
    }

    @Override
    public String toString() {
        return "MasterClassificationRecord [appId=" + appId + ", pubId=" + pubId + ", cpcList=" + cpcList + "]";
    }
}
