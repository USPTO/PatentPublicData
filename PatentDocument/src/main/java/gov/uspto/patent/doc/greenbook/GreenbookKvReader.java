package gov.uspto.patent.doc.greenbook;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.keyvalue.KvReader;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;

/**
 * A {@link KvReader} implementation providing optional normalization of {@link DotCodes}.
 * 
 * @see <a href="https://github.com/USPTO/PatentPublicData/issues/59">https://github.com/USPTO/PatentPublicData/issues/59</a>
 * 
 * @author Luc Boruta (luc@thunken.com)
 */
public class GreenbookKvReader extends KvReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreenbookKvReader.class);

    private final boolean normalize;

    public GreenbookKvReader() {
        this(false);
    }

    /**
     * @param normalize
     *            whether {@link DotCodes} should be replaced by their Unicode or
     *            XML equivalents
     */
    public GreenbookKvReader(final boolean normalize) {
        this.normalize = normalize;
    }

    @Override
    protected void genXmlValue(final Element element, final String value) {
        Document document = null;
        if (normalize && value != null) {
            final String normalized = DotCodes.replaceSubSupHTML(value);
            if (!normalized.equals(value)) {
                /*
                 * Wrap a dummy <span> around the value, to avoid
                 * "Content is not allowed in prolog." errors.
                 */
                try {
                    document = PatentReader.getJDOM(new StringReader("<span>" + normalized + "</span>"));
                } catch (final PatentReaderException e) {
                    LOGGER.warn("Failed to parse and normalize value");
                    document = null;
                }
            }
        }
        if (document == null) {
            super.genXmlValue(element, value);
        } else {
            element.appendContent((Element) document.node(0));
        }
    }

    @Override
    protected String normalizeValue(final String string) {
        return normalize && string != null ? DotCodes.replace(string) : string;
    }

}