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
package org.emonocot.job.dwc.reference;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.emonocot.api.ReferenceService;
import org.emonocot.api.TaxonService;
import org.emonocot.job.dwc.exception.RequiredFieldException;
import org.emonocot.job.dwc.read.NonOwnedProcessor;
import org.emonocot.model.Reference;
import org.emonocot.model.Taxon;
import org.emonocot.model.constants.RecordType;
import org.emonocot.model.registry.Organisation;
import org.emonocot.persistence.dao.hibernate.ReferenceDaoImpl;
import org.emonocot.service.impl.TaxonServiceImpl;
import org.gbif.ecat.voc.Rank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 *
 * @author ben
 *
 */
public class Processor extends NonOwnedProcessor<Reference, ReferenceService> {

	private Logger logger = LoggerFactory.getLogger(Processor.class);

//	private TaxonService taxonService = new TaxonServiceImpl();
//
//	@Autowired
//	public void setTaxonService(TaxonService taxonService) {
//		this.taxonService = taxonService;
//	}

	@Autowired
	public void setReferenceService(ReferenceService service) {
		super.service = service;
	}

	@Override
	protected void doUpdate(Reference persisted, Reference t) {
		persisted.setBibliographicCitation(t.getBibliographicCitation());
		persisted.setCreator(t.getCreator());
		persisted.setDate(t.getDate());
		persisted.setDescription(t.getDescription());
		persisted.setLanguage(t.getLanguage());
		persisted.setSource(t.getSource());
		persisted.setSubject(t.getSubject());
		persisted.setTaxonRemarks(t.getTaxonRemarks());
		persisted.setTitle(t.getTitle());
		persisted.setType(t.getType());
		persisted.setUri(t.getUri());
		updateAuthority(t, persisted);
		//persisted.setAuthority(updateReferenceAuthority(t));
	}

//	private Organisation updateAuthority(Reference ref) {
//		//Taxon t = new Taxon();
//		Organisation org = new Organisation();
//		org.setId(66136L);
//		//t.setAuthority(org);
//		logger.info("Processing updateAuthority: " + org.getId());
//		return org;
//	}
//
	private void updateAuthority(Reference ref, Reference persisted) {

		//ReferenceDaoImpl impl = new ReferenceDaoImpl();
		Set<Taxon> taxa = ref.getTaxa();
		logger.info(" taxa.size(): " + taxa.size());
		if (taxa.size() > 0) {
		List<Taxon> list = new ArrayList<>(taxa);
			Taxon t = list.get(0);
		logger.info("list.get(0). Taxon: " + t);
		String family = t.getFamily();
		Organisation authority = t.getAuthority();
			logger.info("list.get(0). family: " + family);
			logger.info("list.get(0). authority: " + authority);

		//Taxon t = impl.loadTaxon(list.get(0).getIdentifier());
			//Taxon t = taxonService.load(list.get(0).getIdentifier());
			//Taxon t = service.load(list.get(0).getIdentifier());

		logger.info("updateAuthority- TaxonId: " + t.getIdentifier() + "Family: " + t.getFamily());
		Map<String, Rank> hashMap = new HashMap<>();
		//hashMap.putAll(Rank.RANK_MARKER_MAP_SUPRAGENERIC);
		//hashMap.putAll(Rank.RANK_MARKER_MAP_INFRAGENERIC);
			hashMap.putAll(Rank.RANK_MARKER_MAP);
		hashMap.remove("fam");
		Organisation org = getSource();
		//if((StringUtils.isNotBlank(family)) &&
			//	(!family.equalsIgnoreCase(org.getIdentifier()) && (hashMap.containsValue(t.getTaxonRank()))))
		if((t.getFamily() != null && t.getFamily() != "") && (t.getFamily() != org.getIdentifier()))
			{
			logger.info("Source and family not equal:  " + t.getFamily());
			org = getSource(t.getFamily());
		}
		logger.info("persisted setAuthority======");
		persisted.setAuthority(org);
		}
		//return org;
	}

	public Organisation updateReferenceAuthority(Reference r) {
		logger.info("Entered updateReferenceAuthority----");
		Organisation org = getSource();
		if (r.getIdentifier().toLowerCase().contains("database") || r.getIdentifier().toLowerCase().contains("person")
				|| r.getIdentifier().toLowerCase().contains("literature") || r.getIdentifier().toLowerCase().contains("specimen")
		) {
			Set<Taxon> taxa = r.getTaxa();
			logger.info(" taxa.size()--: " + taxa.size());
			if (taxa.size() > 0) {
				List<Taxon> list = new ArrayList<>(taxa);
				Taxon t = list.get(0);
				logger.info("list.get(0). Taxon--: " + t);
				String family = t.getFamily();
				if (!family.equalsIgnoreCase(org.getIdentifier())) {
					logger.info("updateReferenceAuthority --- Source and family not equal. Source: " + r.getAuthority().getIdentifier() + "and family: " + family);
					org = getSource(family);
				}
			}
		}
		return org;
	}

	@Override
	protected void doPersist(Reference t) {

	}

	@Override
	protected RecordType getRecordType() {
		return RecordType.Reference;
	}

	@Override
	protected void bind(Reference t) {
		if (t.getIdentifier() != null) {
			boundObjects.put(t.getIdentifier(), t);
		}
		if (t.getBibliographicCitation() != null) {
			boundObjects.put(t.getBibliographicCitation(), t);
		}
	}

	@Override
	protected Reference retrieveBound(Reference t) {
		if (t.getIdentifier() != null) {
			return service.find(t.getIdentifier());
		} else if (t.getBibliographicCitation() != null) {
			return service.findByBibliographicCitation(t.getBibliographicCitation());
		}
		return null;
	}

	@Override
	protected Reference lookupBound(Reference t) {
		if (t.getIdentifier() != null) {
			return boundObjects.get(t.getIdentifier());
		} else if (t.getBibliographicCitation() != null) {
			return boundObjects.get(t.getBibliographicCitation());
		}
		return null;
	}

	@Override
	protected void doValidate(Reference t) throws Exception {
		if (t.getBibliographicCitation() == null) {
			throw new RequiredFieldException(t + " has no bibliographicCitation set", RecordType.Reference, getStepExecution().getReadCount());
		}
	}

	@Override
	protected boolean doFilter(Reference t) {
		return false;
	}
}
