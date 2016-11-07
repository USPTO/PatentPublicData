package gov.uspto.patent.doc.cpc.masterfile;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.serialize.DocumentBuilder;

public class MasterCpcCsvBuilder implements DocumentBuilder<MasterClassificationRecord> {

    //private static final String CSV_HEADER = "grantIdFull,grantCC,grantId,grantKind,appIdFull,appCC,appId,appKind,cpcLevel,cpcClass\n";

    @Override
    public void write(MasterClassificationRecord record, Writer writer) throws IOException {
        List<String> ret = build(record);
        for (String row : ret) {
            writer.write(row);
        }
    }

    public List<String> build(MasterClassificationRecord record) {
        List<String> ret = new ArrayList<String>();

        String grantFullIdStr = record.getGrantId().toText(7);
        String grantCC = record.getGrantId().getCountryCode().toString();
        String grantIdNum = record.getGrantId().getDocNumber();
        String grantKind = record.getGrantId().getKindCode();

        String appIdNum = record.getAppId().getDocNumber();
        String appFullIdStr = "";
        String appIdCC;
        String appKind;
        if (appIdNum != "") {
            appFullIdStr = record.getAppId().toText();
            appIdCC = record.getAppId().getCountryCode().toString();
            appKind = record.getAppId().getKindCode();
        } else {
            appIdCC = "";
            appKind = "";
        }

        String classLevel;
        String classText;
        if (record.getMainCPC() != null) {
            classLevel = "main";
            classText = record.getMainCPC().toText();

            String[] rowEls = new String[] { grantFullIdStr, grantCC, grantIdNum, grantKind, appFullIdStr, appIdCC,
                    appIdNum, appKind, classLevel, classText };
            ret.add(toCSV(rowEls));
        }

        List<CpcClassification> furtherCpcClasses = record.getFutherCPC();
        if (!furtherCpcClasses.isEmpty()) {
            for (CpcClassification cpcClass : furtherCpcClasses) {
                classLevel = "further";
                classText = cpcClass.toText();

                String[] rowEls = new String[] { grantFullIdStr, grantCC, grantIdNum, grantKind, appFullIdStr, appIdCC,
                        appIdNum, appKind, classLevel, classText };
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
