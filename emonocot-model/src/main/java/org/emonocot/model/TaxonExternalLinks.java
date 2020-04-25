package org.emonocot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.emonocot.model.marshall.json.TaxonDeserializer;
import org.emonocot.model.marshall.json.TaxonSerializer;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.CascadeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "taxon_externallinks")
public class TaxonExternalLinks {

    private Logger logger = LoggerFactory.getLogger(Taxon.class);

    private Long id;

    private String localID;

    private String tplID;

    private String majorGroup;

    private String infraSpecificRank;

    private String genusHybridMarker;

    private String ccrStatus;

    private String originalID;

    private Taxon taxon;

    public void setId(Long newId) {
        this.id = newId;
    }

    @Id
    @GeneratedValue(generator = "table-hilo", strategy = GenerationType.TABLE)
    public Long getId() {
        return id;
    }

    public String getLocalID() {
        return localID;
    }

    public void setLocalID(String localID) {
        this.localID = localID;
    }

    public String getTplID() {
        return tplID;
    }

    public void setTplID(String tplID) {
        this.tplID = tplID;
    }

    public String getMajorGroup() {
        return majorGroup;
    }

    public void setMajorGroup(String majorGroup) {
        this.majorGroup = majorGroup;
    }

    public String getInfraSpecificRank() {
        return infraSpecificRank;
    }

    public void setInfraSpecificRank(String infraSpecificRank) {
        this.infraSpecificRank = infraSpecificRank;
    }

    public String getGenusHybridMarker() {
        return genusHybridMarker;
    }

    public void setGenusHybridMarker(String genusHybridMarker) {
        this.genusHybridMarker = genusHybridMarker;
    }

    public String getCcrStatus() {
        return ccrStatus;
    }

    public void setCcrStatus(String ccrStatus) {
        this.ccrStatus = ccrStatus;
    }

    public String getOriginalID() {
        return originalID;
    }

    public void setOriginalID(String originalID) {
        this.originalID = originalID;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE  })
    @JoinColumn(name = "taxon_id", referencedColumnName = "id")
    @JsonSerialize(using = TaxonSerializer.class)
    public Taxon getTaxon() {
        return taxon;
    }

    @JsonDeserialize(using = TaxonDeserializer.class)
   public void setTaxon(Taxon taxon) {
       this.taxon = taxon;
   }

}
