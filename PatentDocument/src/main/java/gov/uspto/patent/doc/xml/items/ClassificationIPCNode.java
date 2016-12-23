package gov.uspto.patent.doc.xml.items;

import java.text.ParseException;
import java.util.List;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.IpcClassification;

public class ClassificationIPCNode extends ItemReader<PatentClassification> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationIPCNode.class);

    public ClassificationIPCNode(Node itemNode) {
        super(itemNode);
    }

    @Override
    public PatentClassification read() {
        if ("classification-ipcr".equals(itemNode.getName())){
            return readSectionedFormat();
        }
        else if ("classification-ipc".equals(itemNode.getName())){
            return readFlatFormat();
        }
        return null;
    }

    /**
     *
     * 
     *<classifications-ipcr>
     *  <classification-ipcr>
     *     <ipc-version-indicator><date>20060101</date></ipc-version-indicator>
     *     <classification-level>A</classification-level>
     *     <section>F</section>
     *     <class>02</class>
     *     <subclass>K</subclass>
     *     <main-group>3</main-group>
     *     <subgroup>12</subgroup>
     *     <symbol-position>F</symbol-position>
     *     <classification-value>I</classification-value>
     *     <action-date><date>20150101</date></action-date>
     *     <generating-office><country>US</country></generating-office>
     *     <classification-status>B</classification-status>
     *     <classification-data-source>H</classification-data-source>
     *  </classification-ipcr>
     *<classifications-ipcr>
     * 
     * @return
     */
    public PatentClassification readSectionedFormat() {
        String section = itemNode.selectSingleNode("section").getText();
        String mainClass = itemNode.selectSingleNode("class").getText();
        String subclass = itemNode.selectSingleNode("subclass").getText();
        String mainGroup = itemNode.selectSingleNode("main-group").getText();
        String subgroup = itemNode.selectSingleNode("subgroup").getText();

        IpcClassification ipcClass = new IpcClassification();
        ipcClass.setSection(section);
        ipcClass.setMainClass(mainClass);
        ipcClass.setSubClass(subclass);
        ipcClass.setMainGroup(mainGroup);
        ipcClass.setSubGroup(subgroup);

        LOGGER.trace("{}", ipcClass);

        return ipcClass;
    }

    /**
    *
    * 
    *<classification-ipc>
    *   <edition>07</edition>
    *   <main-classification>H04N007/167</main-classification>
    *  <further-classification>H04K001/00</further-classification>
    *</classification-ipc>
    * 
    * @return
    */    
    public PatentClassification readFlatFormat() {
        Node mainClass = itemNode.selectSingleNode("main-classification");
        if (mainClass == null) {
            return null;
        }

        String mainClassTxt = mainClass.getText();
        if ("None".equalsIgnoreCase(mainClassTxt)) {
            LOGGER.trace("Invalid USPC classification 'main-classification': 'None'");
            return null;
        }

        IpcClassification classification;
        try {
            classification = new IpcClassification();
            classification.parseText(mainClass.getText());
            classification.setIsMainClassification(true);
        } catch (ParseException e1) {
            LOGGER.warn("Failed to parse IPC classification 'main-classification': {}", mainClass.asXML(), e1);
            return null;
        }

        @SuppressWarnings("unchecked")
        List<Node> furtherClasses = itemNode.selectNodes("further-classification");
        for (Node subclass : furtherClasses) {
            try {
                IpcClassification usClass = new IpcClassification();
                usClass.parseText(subclass.getText());
                classification.addChild(usClass);
            } catch (ParseException e) {
                LOGGER.warn("Failed to parse IPC classification 'further-classification': {}", subclass.asXML(), e);
            }
        }

        return classification;
    }
}
