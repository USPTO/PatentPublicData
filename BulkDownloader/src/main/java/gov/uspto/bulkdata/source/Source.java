package gov.uspto.bulkdata.source;

import javax.xml.bind.annotation.XmlElement;

public class Source {
    private String name;
    private String docType;   
    private SourceDownload download;
    
    public String getName() {
        return name;
    }

    @XmlElement(name = "name", required = true)
    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public String getDocType() {
        return docType;
    }

    @XmlElement(name = "type", required = true)
    public void setDocType(String docType) {
        this.docType = docType.toLowerCase();
    }

    public SourceDownload getDownload() {
        return download;
    }

    @XmlElement(name = "download")
    public void setDownload(SourceDownload download) {
        this.download = download;
    }

    @Override
    public String toString() {
        return "Source [name=" + name + ", docType=" + docType + ", download=" + download + "]";
    }
}
