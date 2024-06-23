/*
 * This is eMonocot, a global online biodiversity information resource.
 *
 * Copyright © 2011–2015 The Board of Trustees of the Royal Botanic Gardens, Kew and The University of Oxford
 *
 * eMonocot is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * eMonocot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * The complete text of the GNU Affero General Public License is in the source repository as the file
 * ‘COPYING’.  It is also available from <http://www.gnu.org/licenses/>.
 */
package org.emonocot.job.dwc.read;

import java.util.*;

import org.emonocot.api.Service;
import org.emonocot.job.dwc.exception.DarwinCoreProcessingException;
import org.emonocot.model.*;
import org.emonocot.model.constants.AnnotationCode;
import org.emonocot.model.constants.AnnotationType;
import org.emonocot.model.constants.RecordType;
import org.emonocot.model.registry.Organisation;
import org.gbif.ecat.voc.Rank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;

/**
 *
 * @author ben
 *
 */
public abstract class NonOwnedProcessor<T extends BaseData, SERVICE extends Service<T>> extends DarwinCoreProcessor<T> implements
ChunkListener {
	/**
	 *
	 */
	private Logger logger = LoggerFactory.getLogger(NonOwnedProcessor.class);

	/**
	 *
	 */
	protected Map<String, T> boundObjects = new HashMap<String, T>();

	/**
	 *
	 */
	protected SERVICE service;

	/**
	 * @param t an object
	 * @throws Exception if something goes wrong
	 * @return T an object
	 */
	public final T doProcess(final T t)
			throws Exception {
		logger.info("Validating " + t.getIdentifier());

		if(doFilter(t)) {
			return null;
		}

		Taxon taxon = null;
		if(!((NonOwned)t).getTaxa().isEmpty()) {
			taxon = super.getTaxonService().find(((NonOwned)t).getTaxa().iterator().next().getIdentifier());

			((NonOwned)t).getTaxa().clear();
			((NonOwned)t).getTaxa().add(taxon);
			super.checkTaxon(getRecordType(), t, ((NonOwned)t).getTaxa().iterator().next());
		}

		//TODO Simplify this lookup (abstract away whether it is retrieved from chuck of 'bound items' or DB)
		T bound = lookupBound(t);
		if (bound == null) {
			T persisted = null;
			if(t.getIdentifier() != null) {
				persisted = retrieveBound(t);
			} else {
				t.setIdentifier(UUID.randomUUID().toString());
			}

			if (persisted == null) {
				doPersist(t);
				validate(t);
				bind(t);
				logger.info("doProcess setAuthority=====");
				t.setAuthority(getSource());//////uncommented for 500 error
				//updateReferenceAuthority((Reference)t);////////uncomment for backbone harvest

				Annotation annotation = createAnnotation(t, getRecordType(), AnnotationCode.Create, AnnotationType.Info);
				t.getAnnotations().add(annotation);
				logger.info("Adding object " + t.getIdentifier());
				return t;
			} else {
				logger.info("doProcess persisted: " + persisted);
				logger.info("doProcess persisted.getAuthority() : " + persisted.getAuthority());
				////checkAuthority(getRecordType(), t, persisted.getAuthority());////////uncomment for backbone harvest
				// We've seen this object before, but not in this chunk
				if (skipUnmodified && ((persisted.getModified() != null && t.getModified() != null)
						&& !persisted.getModified().isBefore(t.getModified()))) {
					// Assume the object hasn't changed, but maybe this taxon
					// should be associated with it
					replaceAnnotation(persisted, AnnotationType.Info, AnnotationCode.Skipped);
					if(taxon != null) {
						if (((NonOwned)persisted).getTaxa().contains(taxon)) {
							// do nothing
						} else {
							// Add the taxon to the list of taxa
							bind(persisted);
							logger.info("Updating object " + t.getIdentifier());
							((NonOwned)persisted).getTaxa().add(taxon);
						}
					}
					return persisted;
				} else {
					// Assume that this is the first of several times this object
					// appears in the result set, and we'll use this version to
					// overwrite the existing object

					persisted.setAccessRights(t.getAccessRights());
					persisted.setCreated(t.getCreated());
					persisted.setLicense(t.getLicense());
					persisted.setModified(t.getModified());
					persisted.setRights(t.getRights());
					persisted.setRightsHolder(t.getRightsHolder());
					doUpdate(persisted, t);

					((NonOwned)persisted).getTaxa().clear();
					if(taxon != null) {
						((NonOwned)persisted).getTaxa().add(taxon);
					}
					validate(t);

					bind(persisted);
					replaceAnnotation(persisted, AnnotationType.Info, AnnotationCode.Update);
					logger.info("Overwriting object " + t.getIdentifier());
					return persisted;
				}
			}
		} else {
			// We've already seen this object within this chunk and we'll
			// update it with this taxon but that's it, assuming that it
			// isn't a more up to date version
			if(taxon != null) {
				if (((NonOwned)bound).getTaxa().contains(taxon)) {
					// do nothing
				} else {
					// Add the taxon to the list of taxa

					((NonOwned)bound).getTaxa().add(taxon);
				}
			}
			// We've already returned this object once
			logger.info("Skipping object " + t.getIdentifier());
			return null;
		}
	}


	protected abstract boolean doFilter(T t);

	protected abstract void doUpdate(T persisted, T t);

	protected abstract void doPersist(T t);

	protected abstract RecordType getRecordType();

	protected abstract void bind(T t);

	protected abstract T retrieveBound(T t);

	protected abstract T lookupBound(T t);

	protected abstract void doValidate(T t) throws Exception;

	public void afterChunk() {
		super.afterChunk();
		logger.info("After Chunk");
	}

	public void beforeChunk() {
		super.beforeChunk();
		logger.info("Before Chunk");
		boundObjects = new HashMap<String, T>();
	}

	private void updateReferenceAuthority( Reference ref) {
		Organisation org = getSource();

		if (ref != null && (ref.getIdentifier().toLowerCase().contains("database") || ref.getIdentifier().toLowerCase().contains("person")
				|| ref.getIdentifier().toLowerCase().contains("literature") || ref.getIdentifier().toLowerCase().contains("specimen")
		)) {
		Set<Taxon> taxa = ref.getTaxa();
		logger.info(" taxa.size()  : " + taxa.size());
		if (taxa.size() > 0) {
			List<Taxon> list = new ArrayList<>(taxa);
			Taxon t = list.get(0);
			logger.info("list.get(0). Taxon: " + t);
			String family = t.getFamily();
			Organisation authority = t.getAuthority();
			logger.info("list.get(0). family: " + family);
			logger.info("list.get(0). authority: " + authority);
			logger.info("Source and family not equal. TaxonId   : " + t.getIdentifier() + "Family: " + t.getFamily());
			Map<String, Rank> hashMap = new HashMap<>();
			//hashMap.putAll(Rank.RANK_MARKER_MAP_SUPRAGENERIC);
			//hashMap.putAll(Rank.RANK_MARKER_MAP_INFRAGENERIC);
			hashMap.putAll(Rank.RANK_MARKER_MAP);
			hashMap.remove("fam");
			if((t.getFamily() != null) && (t.getFamily() != org.getIdentifier()))
			{
				logger.info("Source and family not equal   :  " + t.getFamily());
				org = getSource(t.getFamily());
			}
			logger.info("persisted setAuthority   ======");
			ref.setAuthority(org);
		}
	}
	}
}
