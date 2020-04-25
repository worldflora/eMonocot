package org.emonocot.model.constants;

public enum Source {
    gcc     ("gcc", "TICA", "TICA", "http://compositae.landcareresearch.co.nz/?Page=NameDetails&amp;NameId="),

    ildis   ("ildis", "ILDIS", "ILDIS", "http://www.ildis.org"),

    tro     ("tropicos", "TRO", "Tropicos", "http://www.tropicos.org"),

    cmp     ("wcsir", "WCSP (in review)", "WCSP (in review)", ""),
    ksu     ("wcsir", "WCSP (in review)", "WCSP (in review)", ""),
    ksufab  ("wcsir", "WCSP (in review)", "WCSP (in review)", ""),
    wcs     ("wcs", "WCSP", "WCSP", "http://apps.kew.org/wcsp"),
    iplants ("iplants", "iPlants", "iPlants", "http://www.iplants.org"),
    iopi    ("iopi", "IOPI", "IOPI", "http://www1.biologie.uni-hamburg.de/b-online/ibc99/iopi/iopihome.html"),

    rjp     ("rjp", "RJP", "RJP", ""), // TODO RJP is who?

    ifern   ("ipni", "IPNI", "IPNI", "https://www.ipni.org"),
    ipni    ("ipni", "IPNI", "IPNI", "https://www.ipni.org"),

    wcsir   ("", "WCSP (in review)", "WCSP (in review)", ""); // Used for statistics output, not a real source.

    private final String urlCode;
    private final String abbr;
    private final String full;
    private final String url;

    /**
     * Source database enumeration
     * @param urlcode abbreviation for URLs
     * @param abbr abbreviated text for output for reading
     * @param full full text for output for reading
     */
    Source(String urlCode, String abbr, String full, String url) {
        this.urlCode = urlCode;
        this.abbr = abbr;
        this.full = full;
        this.url = url;
    }

    public String getUrlCode() {
        return urlCode;
    }

    public String getAbbr() {
        return abbr;
    }

    public String getFull() {
        return full;
    }

    public String getUrl() {
        return url;
    }

    public String toString() {
        return this.urlCode;
    }
}
