package org.emonocot.portal.view.bibliography;

import org.emonocot.model.Reference;
import org.emonocot.model.Taxon;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

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
     * @return a sorted list of references
     */
    List<Reference> getReferences();
}
