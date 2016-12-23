package gov.uspto.patent.serialize;

import java.io.IOException;
import java.util.Arrays;

import javax.json.JsonObject;

import org.junit.Test;

import gov.uspto.patent.FreetextField;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.xml.FormattedText;
import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.PatentGranted;
import gov.uspto.patent.model.PatentType;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;
import gov.uspto.patent.serialize.JsonMapper;

public class JsonMapperTest {

    @Test
    public void buildJsonTest() throws InvalidDataException, IOException {
        
        FormattedText textParserNormalizer = new FormattedText();
        
        DocumentId docId = new DocumentId(CountryCode.US, "123456789");
        PatentGranted patent = new PatentGranted(docId, PatentType.UTILITY);
        patent.setDateProduced(new DocumentDate("20160101"));
        patent.setDatePublished(new DocumentDate("20160202"));
        patent.setInventor( Arrays.asList(new Inventor(new NamePerson("Bob", "Inventee"), new Address("123 Main St", "Alexandria", "VA", "22314", CountryCode.US)) )  );
        patent.setAssignee( Arrays.asList(new Assignee(new NameOrg("Inventee Inc."), new Address("123 Main St", "Alexandria", "VA", "22314", CountryCode.US)) ));
        patent.setTitle("Test Patent");
       
        
        patent.setAbstract(new Abstract("This is the Abstract Section.", textParserNormalizer));
        
        Description desc = new Description();
        desc.addSection(new DescriptionSection(DescSection.DRAWING_DESC, "Drawing Desc Text", textParserNormalizer));
        desc.addSection(new DescriptionSection(DescSection.REL_APP_DESC, "Rell App Desc Text", textParserNormalizer));
        desc.addSection(new DescriptionSection(DescSection.BRIEF_SUMMARY, "Brief Summar Desc Text", textParserNormalizer));
        desc.addSection(new DescriptionSection(DescSection.DETAILED_DESC, "Detailed Description Text", textParserNormalizer));
        patent.setDescription(desc);
 
        JsonMapper json = new JsonMapper(false, false);
        JsonObject jsonObj = json.buildJson(patent);
        
        //System.out.println(jsonObj.toString());

        //System.out.println(json.getPrettyPrint(jsonObj));
    }
    
}
