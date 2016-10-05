package gov.uspto.patent.model;

public class CountryCodeHistoric {
    private final String name;
    private final int yearStart;
    private final int yearEnd;
    private final CountryCode[] becameCountryCode;

    public CountryCodeHistoric(String name, int yearStart, int yearEnd, CountryCode... countryCodes) {
        this.name = name;
        this.yearStart = yearStart;
        this.yearEnd = yearEnd;
        this.becameCountryCode = countryCodes;
    }

    public String getName() {
        return name;
    }

    public int getYearStart() {
        return yearStart;
    }

    public int getYearEnd() {
        return yearEnd;
    }

    public boolean inRange(int year) {
        return (year >= yearStart && year <= yearEnd);
    }

    public CountryCode[] getBecameCountryCode() {
        return becameCountryCode;
    }
}
