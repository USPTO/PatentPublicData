package gov.uspto.patent.doc.cpc.masterfile;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
		try {
			return cpcList.stream().filter(cpc -> cpc.isMainOrInventive()).findFirst().get();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public List<CpcClassification> getFutherCPC() {
		return cpcList.stream().filter(cpc -> !cpc.isMainOrInventive()).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return "MasterClassificationRecord [appId=" + appId + ", pubId=" + pubId + ", cpcList=" + cpcList + "]";
	}
}
