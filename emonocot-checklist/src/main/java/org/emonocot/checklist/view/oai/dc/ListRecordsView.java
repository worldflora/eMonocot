package org.emonocot.checklist.view.oai.dc;

import org.emonocot.checklist.model.IdentifiableEntity;
import org.openarchives.pmh.Metadata;
import org.openarchives.pmh.OaiDc;

/**
 *
 * @author ben
 *
 */
public class ListRecordsView extends
    org.emonocot.checklist.view.oai.ListRecordsView {

    @Override
    public final void constructMetadata(final Metadata metadata,
            final IdentifiableEntity identifiableEntity) {
        OaiDc dc = (OaiDc) getMapper().map(identifiableEntity, OaiDc.class);
        metadata.setAny(dc);
    }

}