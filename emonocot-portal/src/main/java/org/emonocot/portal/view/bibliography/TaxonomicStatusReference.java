package org.emonocot.portal.view.bibliography;

//import org.emonocot.model.Distribution;
import org.emonocot.model.Reference;
import org.emonocot.model.Taxon;

import java.util.List;

public interface TaxonomicStatusReference {
    /**
     *
     * @param taxon Set the taxon
     */
    void setReferences(final Taxon taxon);

    /**
     *
     * @param refernce Set the reference
     * @return A string key which can be displayed in the taxon page as a
     *         citation reference to the reference
     */
    String getKey(Reference refernce);

    /**
     *
     * @param key the key to the reference
     * @return the reference
     */
    Reference getReference(String key);

    /**
     *
     * @param distributions Set the distributions
     * @return a sorted set of keys
     */
//    SortedSet<String> getKeys(Collection<Distribution> distributions);

    /**
     *
     * @return a sorted list of references
     */
    List<Reference> getReferences();
}
