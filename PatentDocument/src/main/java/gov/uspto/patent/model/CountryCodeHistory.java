package gov.uspto.patent.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Map Historic County Code to Current Country Codes.
 * In 1978 the Country Codes changed, some where reused, but since then new countries have been established and borders have changed.
 *
 * The mapping allows for possible country codes reused, year range can be used to disambiguate the correct country code. 
 *
 *<pre>
 * Pre-1978 codes are reused for different countries post-1978.
 * 
 * Code    Pre-1978               Post-1978
 * ----    -------               ---------
 * BH      Bhutan                Bahrain
 * BT      Botswana              Bhutan
 * CD      Cambodia              Congo (DRC)
 * CV      Vatican City State    Cape Verde
 * EA      Ethiopia              Eurasian Patent Organization (EAPO) 
 * GE      Gambia                Georgia
 * KN      North Korea           Saint Kitts and Nevis
 * MD      Madagascar            Moldova
 * MS      Mauritius             Montserrat
 * MT      Mauritania            Malta
 * MU      Oman                  Mauritius
 * NA      Nicaragua             Namibia
 * NI      Niger                 Nicaragua
 * PG      Paraguay              Papua New Guinea
 * RU      Romania               Russia
 * TO      Togo                  Tonga
 * ...
 *</pre> 
 * 
 * https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
 * https://en.wikipedia.org/wiki/ISO_3166-3
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class CountryCodeHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CountryCodeHistory.class);

    private static ListMultimap<String, CountryCodeHistoric> COUNTRY_MAP = ArrayListMultimap.create();

    static {
        COUNTRY_MAP.put("BH", new CountryCodeHistoric("Bhutan", 1949, 1977, CountryCode.BT));
        COUNTRY_MAP.put("BT", new CountryCodeHistoric("Botswana", 1966, 1977, CountryCode.BW));
        COUNTRY_MAP.put("BU", new CountryCodeHistoric("Myanmar", 1948, 1977, CountryCode.MM));
        COUNTRY_MAP.put("CD", new CountryCodeHistoric("Cambodia", 1953, 1977, CountryCode.KH));
        COUNTRY_MAP.put("CE", new CountryCodeHistoric("Chile", 1818, 1977, CountryCode.CL)); // 1883 gained territory from Peru and Bolivia from War of the Pacific.
        COUNTRY_MAP.put("CL", new CountryCodeHistoric("Sri Lanka", 1948, 1977, CountryCode.LK));
        COUNTRY_MAP.put("CS", new CountryCodeHistoric("Czechoslovakia", 1918, 1992, CountryCode.CZ, CountryCode.SK)); // country split into two.
        COUNTRY_MAP.put("CV", new CountryCodeHistoric("Vatican City State", 842, 1977, CountryCode.VA));
        COUNTRY_MAP.put("DA", new CountryCodeHistoric("Dahomey", 1974, 1977, CountryCode.BJ)); // Dahomey Name changed to Benin.
        COUNTRY_MAP.put("DT", new CountryCodeHistoric("Germany", 1949, 1977, CountryCode.DE));
        COUNTRY_MAP.put("DL", new CountryCodeHistoric("East Germany", 1949, 1977, CountryCode.DE));
        COUNTRY_MAP.put("DD", new CountryCodeHistoric("East Germany", 1978, 1990, CountryCode.DE)); // East and West Germany merge.
        COUNTRY_MAP.put("DR", new CountryCodeHistoric("Dominican Republic", 1924, 1977, CountryCode.DO));
        COUNTRY_MAP.put("EA", new CountryCodeHistoric("Ethiopia", 1889, 1977, CountryCode.ET)); // borders reshaped 1889??
        COUNTRY_MAP.put("EI", new CountryCodeHistoric("Ireland", 1921, 1977, CountryCode.IE));
        COUNTRY_MAP.put("ET", new CountryCodeHistoric("Egypt", 1922, 1977, CountryCode.EG));
        COUNTRY_MAP.put("FL", new CountryCodeHistoric("Liechtenstein", 1945, 1977, CountryCode.LI)); // 1945 end of World War 2 borders reshaped.
        COUNTRY_MAP.put("GE", new CountryCodeHistoric("Gambia", 1965, 1977, CountryCode.GM));
        COUNTRY_MAP.put("GU", new CountryCodeHistoric("Guatemala", 1823, 1977, CountryCode.GT));
        COUNTRY_MAP.put("GI", new CountryCodeHistoric("Guinea", 1958, 1977, CountryCode.GN));
        COUNTRY_MAP.put("HI", new CountryCodeHistoric("Haiti", 1849, 1977, CountryCode.HT));
        COUNTRY_MAP.put("HO", new CountryCodeHistoric("Honduras", 1838, 1977, CountryCode.HN));
        COUNTRY_MAP.put("HV", new CountryCodeHistoric("Upper Volta", 1978, 1984, CountryCode.BF));
        COUNTRY_MAP.put("IB", new CountryCodeHistoric("International Patent Institute", 1947, 1977, CountryCode.WO)); // existed 1947-1978; merged with EPO. International Bureau of WIPO.
        COUNTRY_MAP.put("JA", new CountryCodeHistoric("Japan", 500, 1977, CountryCode.JP));
        COUNTRY_MAP.put("KA", new CountryCodeHistoric("Cameroon", 1960, 1977, CountryCode.CM));
        COUNTRY_MAP.put("KN", new CountryCodeHistoric("North Korea", 1953, 1977, CountryCode.KP)); // Korean War (1950-1953)
        COUNTRY_MAP.put("KS", new CountryCodeHistoric("South Korea", 1953, 1977, CountryCode.KR)); // Korean War (1950-1953)
        COUNTRY_MAP.put("UV", new CountryCodeHistoric("Upper Volta", 1958, 1977, CountryCode.BF));
        COUNTRY_MAP.put("RC", new CountryCodeHistoric("China", 1949, 1977, CountryCode.CN)); // 1895 borders reshaped after war (Taiwan ceded to Japan), 1945 end of World War 2 China took back Taiwan, 1949 civil war Taiwan split off.
        COUNTRY_MAP.put("SL", new CountryCodeHistoric("El Salvador", 1841, 1977, CountryCode.SV));
        COUNTRY_MAP.put("SF", new CountryCodeHistoric("Finland", 1918, 1977, CountryCode.FI));
        COUNTRY_MAP.put("KU", new CountryCodeHistoric("Kuwait", 1922, 1977, CountryCode.KW)); // borders reshaped 1922.
        COUNTRY_MAP.put("MD", new CountryCodeHistoric("Madagascar", 1960, 1977, CountryCode.MG));
        COUNTRY_MAP.put("MJ", new CountryCodeHistoric("Mali", 1960, 1977, CountryCode.ML));
        COUNTRY_MAP.put("ML", new CountryCodeHistoric("Malta", 1964, 1977, CountryCode.MT));
        COUNTRY_MAP.put("MS", new CountryCodeHistoric("Mauritius", 1968, 1977, CountryCode.MU));
        COUNTRY_MAP.put("MT", new CountryCodeHistoric("Mauritania", 1960, 1977, CountryCode.MR));        
        COUNTRY_MAP.put("MO", new CountryCodeHistoric("Mongolia", 1911, 1977, CountryCode.MN));
        COUNTRY_MAP.put("MU", new CountryCodeHistoric("Oman", 1749, 1977, CountryCode.OM));
        COUNTRY_MAP.put("NA", new CountryCodeHistoric("Nicaragua", 1850, 1977, CountryCode.NI));
        COUNTRY_MAP.put("NI", new CountryCodeHistoric("Niger", 1958, 1977, CountryCode.NE));
        COUNTRY_MAP.put("PG", new CountryCodeHistoric("Paraguay", 1811, 1977, CountryCode.PY));
        COUNTRY_MAP.put("PM", new CountryCodeHistoric("Panama", 1903, 1977, CountryCode.PA));
        COUNTRY_MAP.put("PO", new CountryCodeHistoric("Poland", 1945, 1977, CountryCode.PL)); // 1945 end of World War 2 borders reshaped.
        COUNTRY_MAP.put("PP", new CountryCodeHistoric("Papua New Guinea", 1975, 1977, CountryCode.PG)); // sovereignty in 1975.
        COUNTRY_MAP.put("RU", new CountryCodeHistoric("Romania", 1940, 1977, CountryCode.RO)); // borders reshaped 1940.
        COUNTRY_MAP.put("SW", new CountryCodeHistoric("Sweden", 1905, 1977, CountryCode.SE));
        COUNTRY_MAP.put("SR", new CountryCodeHistoric("Syria", 1945, 1977, CountryCode.SY));
        COUNTRY_MAP.put("TA", new CountryCodeHistoric("Tanzania", 1961, 1977, CountryCode.TZ));
        COUNTRY_MAP.put("TD", new CountryCodeHistoric("Trinidad and Tobago", 1962, 1977, CountryCode.TT));
        COUNTRY_MAP.put("TI", new CountryCodeHistoric("Tonga", 1970, 1977, CountryCode.TO));
        COUNTRY_MAP.put("TP", new CountryCodeHistoric("Timor–Leste", 1975, 1977, CountryCode.TL));
        COUNTRY_MAP.put("TO", new CountryCodeHistoric("Togo", 1960, 1977, CountryCode.TG));
        COUNTRY_MAP.put("TS", new CountryCodeHistoric("Chad", 1960, 1977, CountryCode.TD));        
        COUNTRY_MAP.put("WL", new CountryCodeHistoric("Sierra Leone", 1961, 1977, CountryCode.SL));
        COUNTRY_MAP.put("WN", new CountryCodeHistoric("Nigeria", 1960, 1977, CountryCode.NG));
        COUNTRY_MAP.put("YD", new CountryCodeHistoric("South Yemen", 1978, 1990, CountryCode.YE)); // North and South Yemen merge.
        COUNTRY_MAP.put("YU", new CountryCodeHistoric("Yugoslavia/Serbia and Montenegro", 1922, 1977, CountryCode.YU)); // country renamed but kept old country code.        
        COUNTRY_MAP.put("ZB", new CountryCodeHistoric("Zambia", 1964, 1977, CountryCode.ZM));
        COUNTRY_MAP.put("ZR", new CountryCodeHistoric("Central African Republic", 1960, 1977, CountryCode.CF));
        COUNTRY_MAP.put("ZR", new CountryCodeHistoric("Zaire", 1971, 1977, CountryCode.CD)); // Zaire (1971-1997) now Democratic Republic of the Congo.
        COUNTRY_MAP.put("CB", new CountryCodeHistoric("Zaire", 1978, 1997, CountryCode.CD)); // Democratic Republic of the Congo CB ZR/CD        
        COUNTRY_MAP.put("SY", new CountryCodeHistoric("South Yemen", 1974, 1977, CountryCode.YE));
        COUNTRY_MAP.put("RH", new CountryCodeHistoric("Rhodesia", 1965, 1979, CountryCode.ZW));
        COUNTRY_MAP.put("AN", new CountryCodeHistoric("Netherlands Antilles", 1974, 2010, CountryCode.BQ));
        COUNTRY_MAP.put("SU", new CountryCodeHistoric("Soviet Union (USSR)", 1922, 1991, CountryCode.SU)); // split into multiple counties, for now keep as original.
        COUNTRY_MAP.put("NC", new CountryCodeHistoric("New Caledonia", 1978, 2016, CountryCode.UNDEFINED)); // territory of France, removed April 2016.
        COUNTRY_MAP.put("NF", new CountryCodeHistoric("Norfolk Island", 1978, 2016, CountryCode.UNDEFINED)); // territory of Australia, removed April 2016.
    }

    public static CountryCode getCurrentCode(String countryCode, int year) {

        List<CountryCodeHistoric> countryCodeHistorics = COUNTRY_MAP.get(countryCode);
        if (countryCodeHistorics.size() == 1 && countryCodeHistorics.get(0).inRange(year)) {
            if (countryCodeHistorics.get(0).getBecameCountryCode().length == 1) {
                return countryCodeHistorics.get(0).getBecameCountryCode()[0];
            } else {
                LOGGER.warn("Historic Country Code '{}' maps to multiple countries: {}", countryCode,
                        countryCodeHistorics.get(0).getBecameCountryCode());
            }
        }

        return CountryCode.UNKNOWN;
    }

    public static CountryCode getCurrentCode(String countryCode) {

        List<CountryCodeHistoric> countryCodeHistorics = COUNTRY_MAP.get(countryCode);
        if (countryCodeHistorics.size() == 1) {
            if (countryCodeHistorics.get(0).getBecameCountryCode().length == 1) {
                return countryCodeHistorics.get(0).getBecameCountryCode()[0];
            } else {
                LOGGER.warn("Historic Country Code '{}' maps to multiple countries: {}", countryCode,
                        countryCodeHistorics.get(0).getBecameCountryCode());
            }
        }

        return CountryCode.UNKNOWN;
    }

}
