package gov.uspto.bulkdata.source;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "sources")
public class Sources {
    private List<Source> sources;

    @XmlElement(name = "source")
    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public List<Source> getSources() {
        return sources;
    }

    public Source getSource(String name, String type) {
        if (type == null || type.isEmpty()){
            type = name;
        }
        name = name.toLowerCase();
        type = type.toLowerCase();
        for (Source source : sources) {
            if (name.equals(source.getName()) && type.equals(source.getDocType())) {
                return source;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Sources [sources=" + Arrays.toString(sources.toArray()) + "]";
    }

    public static Sources read() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Sources.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        
        Sources sources = null;
        
        File file = new File("sources.xml");
        if (file.exists()){
            sources = (Sources) jaxbUnmarshaller.unmarshal(file);
        } else {
            InputStream input = Sources.class.getResourceAsStream("/sources.xml");
            if (input == null){
                throw new IllegalArgumentException("Could not find source.xml");
            }          
            sources = (Sources) jaxbUnmarshaller.unmarshal(input);
        }
        return sources;
    }

    public static void main(String... args) throws JAXBException {
        Sources sources = Sources.read();

        System.out.println(sources);
    }
}
