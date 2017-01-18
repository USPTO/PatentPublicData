package gov.uspto.patent.doc.cpc.masterfile;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.serialize.DocumentBuilder;

public class MasterCpcCsvBuilder implements DocumentBuilder<MasterClassificationRecord> {

    //private static final String CSV_HEADER = "pubIdFull,pubCC,pubNumId,pubKindCode,appIdFull,appCC,appNum cpcLevel,cpcClass\n";

    @Override
    public void write(MasterClassificationRecord record, Writer writer) throws IOException {
        List<String> ret = build(record);
        for (String row : ret) {
            writer.write(row);
        }
    }

    public List<String> build(MasterClassificationRecord record) {
        List<String> ret = new ArrayList<String>();

        String appNum = record.getAppId().getDocNumber();
        String appCC = record.getAppId().getCountryCode().toString();
        String appIdFull = !appNum.isEmpty() ? record.getAppId().toText() : "";

        String pubIdFull = record.getPubId().toText(7);
        String pubCC = record.getPubId().getCountryCode().toString();
        String pubNumId = record.getPubId().getDocNumber();
        String pubKindCode = record.getPubId().getKindCode();

        String classLevel;
        String classText;
        if (record.getMainCPC() != null) {
            classLevel = "main";
            classText = record.getMainCPC().toText();

            String[] rowEls = new String[] { pubIdFull, pubCC, pubNumId, pubKindCode, appIdFull, appCC, appNum,
                    classLevel, classText };
            ret.add(toCSV(rowEls));
        }

        List<CpcClassification> furtherCpcClasses = record.getFutherCPC();
        if (!furtherCpcClasses.isEmpty()) {
            for (CpcClassification cpcClass : furtherCpcClasses) {
                classLevel = "further";
                classText = cpcClass.toText();

                String[] rowEls = new String[] { pubIdFull, pubCC, pubNumId, pubKindCode, appIdFull, appCC, appNum,
                        classLevel, classText };
                ret.add(toCSV(rowEls));
            }
        }

        return ret;
    }

    private String toCSV(String[] strings) {
        StringBuilder stb = new StringBuilder();
        for (String str : strings) {
            stb.append(str).append(",");
        }
        stb.delete(stb.length() - 1, stb.length());
        stb.append("\n");
        return stb.toString();
    }

}
