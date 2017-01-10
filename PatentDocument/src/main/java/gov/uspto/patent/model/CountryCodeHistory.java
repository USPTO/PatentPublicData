package gov.uspto.patent.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Map Historic County Code to Current Country Codes.
 * 
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

    private static int FORMAT_CHANGE_DATE = 1977;

    private static ListMultimap<String, CountryCodeHistoric> COUNTRY_MAP = ArrayListMultimap.create();

    static {
        // Country Codes which did not change:
        COUNTRY_MAP.put("US", new CountryCodeHistoric("United States", 1776, FORMAT_CHANGE_DATE, CountryCode.US));

        // Note "EN" and "UK" are not an official country codes though they are used in addresses.
        COUNTRY_MAP.put("EN", new CountryCodeHistoric("United Kingdom", 1922, FORMAT_CHANGE_DATE, CountryCode.GB)); // 1922 Ireland broke union with United Kingdom.
        COUNTRY_MAP.put("UK", new CountryCodeHistoric("United Kingdom", 1922, FORMAT_CHANGE_DATE, CountryCode.GB)); // 1922 Ireland broke union with United Kingdom.

        // Country Codes which changed:
        COUNTRY_MAP.put("BH", new CountryCodeHistoric("Bhutan", 1949, FORMAT_CHANGE_DATE, CountryCode.BT));
        COUNTRY_MAP.put("BT", new CountryCodeHistoric("Botswana", 1966, FORMAT_CHANGE_DATE, CountryCode.BW));
        COUNTRY_MAP.put("BU", new CountryCodeHistoric("Myanmar", 1948, FORMAT_CHANGE_DATE, CountryCode.MM));
        COUNTRY_MAP.put("CD", new CountryCodeHistoric("Cambodia", 1953, FORMAT_CHANGE_DATE, CountryCode.KH));
        COUNTRY_MAP.put("CE", new CountryCodeHistoric("Chile", 1818, FORMAT_CHANGE_DATE, CountryCode.CL)); // 1883 gained territory from Peru and Bolivia from War of the Pacific.
        COUNTRY_MAP.put("CL", new CountryCodeHistoric("Sri Lanka", 1948, FORMAT_CHANGE_DATE, CountryCode.LK));
        COUNTRY_MAP.put("CS", new CountryCodeHistoric("Czechoslovakia", 1918, 1992, CountryCode.CZ, CountryCode.SK)); // country split into two.
        COUNTRY_MAP.put("CV", new CountryCodeHistoric("Vatican City State", 842, FORMAT_CHANGE_DATE, CountryCode.VA));
        COUNTRY_MAP.put("DA", new CountryCodeHistoric("Dahomey", 1974, FORMAT_CHANGE_DATE, CountryCode.BJ)); // Dahomey Name changed to Benin.
        COUNTRY_MAP.put("DT", new CountryCodeHistoric("Germany", 1949, FORMAT_CHANGE_DATE, CountryCode.DE));
        COUNTRY_MAP.put("DL", new CountryCodeHistoric("East Germany", 1949, FORMAT_CHANGE_DATE, CountryCode.DE));
        COUNTRY_MAP.put("DD", new CountryCodeHistoric("East Germany", 1978, 1990, CountryCode.DE)); // East and West Germany merge.
        COUNTRY_MAP.put("DR", new CountryCodeHistoric("Dominican Republic", 1924, FORMAT_CHANGE_DATE, CountryCode.DO));
        COUNTRY_MAP.put("EA", new CountryCodeHistoric("Ethiopia", 1889, FORMAT_CHANGE_DATE, CountryCode.ET)); // borders reshaped 1889??
        COUNTRY_MAP.put("EI", new CountryCodeHistoric("Ireland", 1921, FORMAT_CHANGE_DATE, CountryCode.IE));
        COUNTRY_MAP.put("ET", new CountryCodeHistoric("Egypt", 1922, FORMAT_CHANGE_DATE, CountryCode.EG));
        COUNTRY_MAP.put("FL", new CountryCodeHistoric("Liechtenstein", 1945, FORMAT_CHANGE_DATE, CountryCode.LI)); // 1945 end of World War 2 borders reshaped.
        COUNTRY_MAP.put("GE", new CountryCodeHistoric("Gambia", 1965, FORMAT_CHANGE_DATE, CountryCode.GM));
        COUNTRY_MAP.put("GU", new CountryCodeHistoric("Guatemala", 1823, FORMAT_CHANGE_DATE, CountryCode.GT));
        COUNTRY_MAP.put("GI", new CountryCodeHistoric("Guinea", 1958, FORMAT_CHANGE_DATE, CountryCode.GN));
        COUNTRY_MAP.put("HI", new CountryCodeHistoric("Haiti", 1849, FORMAT_CHANGE_DATE, CountryCode.HT));
        COUNTRY_MAP.put("HO", new CountryCodeHistoric("Honduras", 1838, FORMAT_CHANGE_DATE, CountryCode.HN));
        COUNTRY_MAP.put("HV", new CountryCodeHistoric("Upper Volta", 1978, 1984, CountryCode.BF));
        COUNTRY_MAP.put("IB", new CountryCodeHistoric("International Patent Institute", 1947, FORMAT_CHANGE_DATE, CountryCode.WO)); // existed 1947-1978; merged with EPO. International Bureau of WIPO.
        COUNTRY_MAP.put("JA", new CountryCodeHistoric("Japan", 500, FORMAT_CHANGE_DATE, CountryCode.JP));
        COUNTRY_MAP.put("KA", new CountryCodeHistoric("Cameroon", 1960, FORMAT_CHANGE_DATE, CountryCode.CM));
        COUNTRY_MAP.put("KN", new CountryCodeHistoric("North Korea", 1953, FORMAT_CHANGE_DATE, CountryCode.KP)); // Korean War (1950-1953)
        COUNTRY_MAP.put("KS", new CountryCodeHistoric("South Korea", 1953, FORMAT_CHANGE_DATE, CountryCode.KR)); // Korean War (1950-1953)
        COUNTRY_MAP.put("UV", new CountryCodeHistoric("Upper Volta", 1958, FORMAT_CHANGE_DATE, CountryCode.BF));
        COUNTRY_MAP.put("RC", new CountryCodeHistoric("China", 1949, FORMAT_CHANGE_DATE, CountryCode.CN)); // 1895 borders reshaped after war (Taiwan ceded to Japan), 1945 end of World War 2 China took back Taiwan, 1949 civil war Taiwan split off.
        COUNTRY_MAP.put("SL", new CountryCodeHistoric("El Salvador", 1841, FORMAT_CHANGE_DATE, CountryCode.SV));
        COUNTRY_MAP.put("SF", new CountryCodeHistoric("Finland", 1918, FORMAT_CHANGE_DATE, CountryCode.FI));
        COUNTRY_MAP.put("KU", new CountryCodeHistoric("Kuwait", 1922, FORMAT_CHANGE_DATE, CountryCode.KW)); // borders reshaped 1922.
        COUNTRY_MAP.put("MD", new CountryCodeHistoric("Madagascar", 1960, FORMAT_CHANGE_DATE, CountryCode.MG));
        COUNTRY_MAP.put("MJ", new CountryCodeHistoric("Mali", 1960, FORMAT_CHANGE_DATE, CountryCode.ML));
        COUNTRY_MAP.put("ML", new CountryCodeHistoric("Malta", 1964, FORMAT_CHANGE_DATE, CountryCode.MT));
        COUNTRY_MAP.put("MS", new CountryCodeHistoric("Mauritius", 1968, FORMAT_CHANGE_DATE, CountryCode.MU));
        COUNTRY_MAP.put("MT", new CountryCodeHistoric("Mauritania", 1960, FORMAT_CHANGE_DATE, CountryCode.MR));        
        COUNTRY_MAP.put("MO", new CountryCodeHistoric("Mongolia", 1911, FORMAT_CHANGE_DATE, CountryCode.MN));
        COUNTRY_MAP.put("MU", new CountryCodeHistoric("Oman", 1749, FORMAT_CHANGE_DATE, CountryCode.OM));
        COUNTRY_MAP.put("NA", new CountryCodeHistoric("Nicaragua", 1850, FORMAT_CHANGE_DATE, CountryCode.NI));
        COUNTRY_MAP.put("NI", new CountryCodeHistoric("Niger", 1958, FORMAT_CHANGE_DATE, CountryCode.NE));
        COUNTRY_MAP.put("PG", new CountryCodeHistoric("Paraguay", 1811, FORMAT_CHANGE_DATE, CountryCode.PY));
        COUNTRY_MAP.put("PM", new CountryCodeHistoric("Panama", 1903, FORMAT_CHANGE_DATE, CountryCode.PA));
        COUNTRY_MAP.put("PO", new CountryCodeHistoric("Poland", 1945, FORMAT_CHANGE_DATE, CountryCode.PL)); // 1945 end of World War 2 borders reshaped.
        COUNTRY_MAP.put("PP", new CountryCodeHistoric("Papua New Guinea", 1975, FORMAT_CHANGE_DATE, CountryCode.PG)); // sovereignty in 1975.
        COUNTRY_MAP.put("RU", new CountryCodeHistoric("Romania", 1940, FORMAT_CHANGE_DATE, CountryCode.RO)); // borders reshaped 1940.
        COUNTRY_MAP.put("SW", new CountryCodeHistoric("Sweden", 1905, FORMAT_CHANGE_DATE, CountryCode.SE));
        COUNTRY_MAP.put("SR", new CountryCodeHistoric("Syria", 1945, FORMAT_CHANGE_DATE, CountryCode.SY));
        COUNTRY_MAP.put("TA", new CountryCodeHistoric("Tanzania", 1961, FORMAT_CHANGE_DATE, CountryCode.TZ));
        COUNTRY_MAP.put("TD", new CountryCodeHistoric("Trinidad and Tobago", 1962, FORMAT_CHANGE_DATE, CountryCode.TT));
        COUNTRY_MAP.put("TI", new CountryCodeHistoric("Tonga", 1970, FORMAT_CHANGE_DATE, CountryCode.TO));
        COUNTRY_MAP.put("TP", new CountryCodeHistoric("Timor-Leste", 1975, FORMAT_CHANGE_DATE, CountryCode.TL));
        COUNTRY_MAP.put("TO", new CountryCodeHistoric("Togo", 1960, FORMAT_CHANGE_DATE, CountryCode.TG));
        COUNTRY_MAP.put("TS", new CountryCodeHistoric("Chad", 1960, FORMAT_CHANGE_DATE, CountryCode.TD));      
        COUNTRY_MAP.put("WL", new CountryCodeHistoric("Sierra Leone", 1961, FORMAT_CHANGE_DATE, CountryCode.SL));
        COUNTRY_MAP.put("WN", new CountryCodeHistoric("Nigeria", 1960, FORMAT_CHANGE_DATE, CountryCode.NG));
        COUNTRY_MAP.put("YD", new CountryCodeHistoric("South Yemen", 1978, 1990, CountryCode.YE)); // North and South Yemen merge.
        COUNTRY_MAP.put("YU", new CountryCodeHistoric("Yugoslavia/Serbia and Montenegro", 1922, FORMAT_CHANGE_DATE, CountryCode.YU)); // country renamed but kept old country code.        
        COUNTRY_MAP.put("ZB", new CountryCodeHistoric("Zambia", 1964, FORMAT_CHANGE_DATE, CountryCode.ZM));
        COUNTRY_MAP.put("ZR", new CountryCodeHistoric("Central African Republic", 1960, FORMAT_CHANGE_DATE, CountryCode.CF));
        COUNTRY_MAP.put("ZR", new CountryCodeHistoric("Zaire", 1971, FORMAT_CHANGE_DATE, CountryCode.CD)); // Zaire (1971-1997) now Democratic Republic of the Congo.
        COUNTRY_MAP.put("CB", new CountryCodeHistoric("Zaire", 1978, FORMAT_CHANGE_DATE, CountryCode.CD)); // Democratic Republic of the Congo CB ZR/CD        
        COUNTRY_MAP.put("SY", new CountryCodeHistoric("South Yemen", 1974, FORMAT_CHANGE_DATE, CountryCode.YE));
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
