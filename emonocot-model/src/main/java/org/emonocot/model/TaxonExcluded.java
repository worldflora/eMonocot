package org.emonocot.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.emonocot.model.marshall.json.TaxonDeserializer;
import org.emonocot.model.marshall.json.TaxonSerializer;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;


@Entity
@Table(name = "taxon_excluded")
public class TaxonExcluded {

    private Logger logger = LoggerFactory.getLogger(Taxon.class);

    private Long id;

    private String identifier;

    private String reason;

    private String scientificName;

    private String scientificNameAuthorship;

   // private Taxon taxon;

    public void setId(Long newId) {
        this.id = newId;
    }

    @Id
    @GeneratedValue(generator = "table-hilo", strategy = GenerationType.TABLE)
    public Long getId() {
        return id;
    }

//    @OneToOne(fetch = FetchType.LAZY)
//    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE  })
//    @JoinColumn(name = "taxon_id", referencedColumnName = "id")
//    @JsonSerialize(using = TaxonSerializer.class)
//    public Taxon getTaxon() {
//        return taxon;
//    }
//
//    @JsonDeserialize(using = TaxonDeserializer.class)
//   public void setTaxon(Taxon taxon) {
//       this.taxon = taxon;
//   }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getScientificNameAuthorship() {
        return scientificNameAuthorship;
    }

    public void setScientificNameAuthorship(String scientificNameAuthorship) {
        this.scientificNameAuthorship = scientificNameAuthorship;
    }

}
