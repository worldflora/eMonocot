package org.emonocot.model;

public class ClassificationFilter {
    private String rootRank;
    private String taxonmicStatus;

    public ClassificationFilter (String rootRank, String taxonmicStatus ) {
        this.rootRank = rootRank;
        this.taxonmicStatus = taxonmicStatus;
    }

    public String getRootRank() {
        return rootRank;
    }

    public void setRootRank(String rootRank) {
        this.rootRank = rootRank;
    }

    public String getTaxonmicStatus() {
        return taxonmicStatus;
    }

    public void setTaxonmicStatus(String taxonmicStatus) {
        this.taxonmicStatus = taxonmicStatus;
    }
}