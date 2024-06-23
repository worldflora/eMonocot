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
package org.emonocot.job.iucn;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.emonocot.api.match.Match;
import org.emonocot.api.match.taxon.TaxonMatcher;
import org.emonocot.harvest.common.AbstractRecordAnnotator;
import org.emonocot.model.Annotation;
import org.emonocot.model.MeasurementOrFact;
import org.emonocot.model.Taxon;
import org.emonocot.model.constants.AnnotationCode;
import org.emonocot.model.constants.AnnotationType;
import org.emonocot.model.constants.RecordType;
import org.gbif.dwc.terms.IucnTerm;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/**
 * @author ben
 */
public class Processor extends AbstractRecordAnnotator implements ItemProcessor<Map<String,Object>, MeasurementOrFact> {

	private static final Object SCIENTIFIC_NAME_FIELD = "scientific_name";

	private static final Object MODIFIED_YEAR_FIELD = "modified_year";

	private static final Object CATEGORY_FIELD = "category";

	private static final Object SPECIES_ID_FIELD = "species_id";

	private static final Object TAXONID_ID_FIELD = "taxonid";

	public static String GENUS_FIELD = "genus";

	public static String ALTERNATE_GENUS_FIELD = "genus_name";

	public static String SPECIFIC_EPITHET_FIELD = "species";

	public static String AUTHORITY_FIELD = "authority";

	public static String INFRASPECIFIC_EPITHET_FIELD = "infra_name";

	public static String INFRASPECIFIC_AUTHORITY_FIELD = "infra_authority";

	public static String INFRASPECIFIC_RANK_FIELD = "infra_rank";

	public static String CRITERIA_FIELD = "criteria";

	public static String TAXONOMIC_AUTHORITY_FIELD = "taxonomic_authority";

	private String accessRights;

	private String license;

	private String rights;

	private String rightsHolder;

	private String bibliographicCitation;

	private String iucnWebsiteUri = "http://www.iucnredlist.org/details/${identifier}/0";

	private NameParser nameParser;

	public void setIucnWebsiteUri(String iucnWebsiteUri) {
		if(iucnWebsiteUri != null) {
			this.iucnWebsiteUri = iucnWebsiteUri;
		}
	}

	/**
	 * @param accessRights the accessRights to set
	 */
	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}

	/**
	 * @param license the license to set
	 */
	public void setLicense(String license) {
		this.license = license;
	}

	/**
	 * @param rights the rights to set
	 */
	public void setRights(String rights) {
		this.rights = rights;
	}

	/**
	 * @param bibliographicCitation the bibliographicCitation to set
	 */
	public void setBibliographicCitation(String bibliographicCitation) {
		this.bibliographicCitation = bibliographicCitation;
	}

	/**
	 * @param rightsHolder the rightsHolder to set
	 */
	public void setRightsHolder(String rightsHolder) {
		this.rightsHolder = rightsHolder;
	}

	private Logger logger = LoggerFactory.getLogger(Processor.class);

	private TaxonMatcher taxonMatcher;

	public void setTaxonMatcher(TaxonMatcher taxonMatcher) {
		this.taxonMatcher = taxonMatcher;
	}

	public void setNameParser(NameParser nameParser) {
		this.nameParser = nameParser;
	}

	public final MeasurementOrFact process(final Map<String,Object> map) throws Exception {
		Taxon taxon = doMatchTaxon(map);
		if(taxon != null) {
			MeasurementOrFact measurementOrFact = new MeasurementOrFact();
			StringBuffer remarks = new StringBuffer();
			if(map.get(Processor.CRITERIA_FIELD) != null) {
				remarks.append("Criteria: " + map.get(Processor.CRITERIA_FIELD) + ". ");
			}
			if(map.get(Processor.MODIFIED_YEAR_FIELD) != null) {
				remarks.append("Modified Year: " + map.get(Processor.MODIFIED_YEAR_FIELD) + ". ");
			}
			measurementOrFact.setMeasurementRemarks(remarks.toString().trim());
			measurementOrFact.setMeasurementValue((String)map.get(Processor.CATEGORY_FIELD));
			measurementOrFact.setMeasurementType(IucnTerm.threatStatus);
			measurementOrFact.setAccessRights(accessRights);
			measurementOrFact.setRights(rights);
			measurementOrFact.setRightsHolder(rightsHolder);
			measurementOrFact.setLicense(license);
			measurementOrFact.setBibliographicCitation(bibliographicCitation);
			if(map.get(Processor.SPECIES_ID_FIELD) != null) {
				Integer speciesId = (Integer)map.get(Processor.SPECIES_ID_FIELD);
				measurementOrFact.setSource(iucnWebsiteUri.replace("${identifier}",speciesId.toString()));
			}else if(map.get(Processor.TAXONID_ID_FIELD) != null){
				Integer speciesId = (Integer)map.get(Processor.TAXONID_ID_FIELD);
				measurementOrFact.setSource(iucnWebsiteUri.replace("${identifier}",speciesId.toString()));
			}
			measurementOrFact.setTaxon(taxon);
			return measurementOrFact;
		}
		return null;
	}

	private boolean nullSafeContains(Map<String,Object> map, String key) {
		return map.containsKey(key) && map.get(key) != null && !(((String) map.get(key)).isEmpty());
	}

	private Taxon doMatchTaxon(Map<String, Object> map) {
		ParsedName<String> parsedName = null;
		if(map.get(Processor.SCIENTIFIC_NAME_FIELD) != null) {
			logger.info("IUCN processor name map.get(Processor.SCIENTIFIC_NAME_FIELD): "+map.get(Processor.SCIENTIFIC_NAME_FIELD));
			try {
				parsedName = nameParser.parse(StringEscapeUtils.unescapeXml((String)map.get(Processor.SCIENTIFIC_NAME_FIELD)));
				logger.info("IUCN processor name parsedName: "+parsedName);
			} catch (UnparsableException e) {
				logger.error("Unable to parse scientific_name");
			}
		}

		StringBuffer nameBuffer = new StringBuffer();
		String scientificNameAuthorship = "";

		if(nullSafeContains(map,Processor.GENUS_FIELD)) {
			logger.info("IUCN processor - nullSafeContains(map,Processor.GENUS_FIELD)");
			String genus = ((String)map.get(Processor.GENUS_FIELD)).trim();
			logger.info("IUCN processor genus: "+genus);
			nameBuffer.append(genus);
		}else if (nullSafeContains(map, Processor.ALTERNATE_GENUS_FIELD)) {
			logger.info("IUCN processor - nullSafeContains(map,Processor.ALTERNATE_GENUS_FIELD)");
			String genus = ((String)map.get(Processor.ALTERNATE_GENUS_FIELD)).trim();
			logger.info("IUCN processor genus: "+genus);
			nameBuffer.append(genus);
		}
		if(nullSafeContains(map,Processor.SPECIFIC_EPITHET_FIELD)) {
			logger.info("IUCN processor - nullSafeContains(map,Processor.SPECIFIC_EPITHET_FIELD)");
			String species = ((String)map.get(Processor.SPECIFIC_EPITHET_FIELD)).trim();
			logger.info("IUCN processor species: "+species);
			nameBuffer.append(" ").append(species);
		}
		else if((parsedName.specificEpithet != null) && !parsedName.specificEpithet.isEmpty())
		{
			logger.info("IUCN processor - specificEpithet not null and not empty");
            String species = parsedName.specificEpithet.toString().trim();
			logger.info("IUCN processor species: "+species);
			nameBuffer.append(" ").append(species);
		}
		if(!nullSafeContains(map,Processor.INFRASPECIFIC_EPITHET_FIELD)) {
			logger.info("IUCN processor - !nullSafeContains(map,Processor.INFRASPECIFIC_EPITHET_FIELD)");
			if(parsedName == null || parsedName.getInfraSpecificEpithet() == null) {
				logger.info("IUCN processor - parsedName null or getInfraSpecificEpithet null");
				// Assume species, and use the "authority" field
				if(nullSafeContains(map,Processor.AUTHORITY_FIELD)) {
					logger.info("IUCN processor - nullSafeContains(map,Processor.AUTHORITY_FIELD)");
					String authority = StringEscapeUtils.unescapeXml(((String)map.get("authority")).trim());
					logger.info("IUCN processor authority: "+authority);
					nameBuffer.append(" ").append(authority);
				}
			} else {
				// The parsed json fields do not contain information about the
				// infraspecies, but the scientific_name field does contain this
				// information
				logger.info("IUCN processor parsedName: "+parsedName);
				logger.info("IUCN processor parsedName.getInfraSpecificEpithet(): "+parsedName.getInfraSpecificEpithet());
				if(parsedName.getRankMarker() != null) {
					logger.info("IUCN processor - getRankMarker= "+ parsedName.getRankMarker());
					String infraspecificRank = parsedName.getRankMarker().trim();
					logger.info("IUCN processor infraspecificRank: "+infraspecificRank);
					nameBuffer.append(" ").append(infraspecificRank);
				}
				logger.info("IUCN processor getRankMarker is null");
				String infraspecificEpithet = parsedName.getInfraSpecificEpithet().trim();
				logger.info("IUCN processor infraspecificEpithet: "+infraspecificEpithet);
				nameBuffer.append(" ").append(infraspecificEpithet);
				logger.info("IUCN processor nameBuffer: "+nameBuffer);
				if(parsedName.getAuthorship() != null) {
					logger.info("IUCN processor - getAuthorship : "+parsedName.getAuthorship());
					String infraspecificAuthority = parsedName.getAuthorship().trim();
					logger.info("IUCN processor infraspecificAuthority: "+infraspecificAuthority);
					nameBuffer.append(" ").append(infraspecificAuthority);
				}
			}
		} else {
			logger.info("IUCN processor nullSafeContains(map,Processor.INFRASPECIFIC_EPITHET_FIELD)");
			// Assume infraspecies, try to use the infra_rank, infra_name and  "infra_authority" fields
			if(nullSafeContains(map,Processor.INFRASPECIFIC_RANK_FIELD)) {
				logger.info("IUCN processor - map.get(Processor.INFRASPECIFIC_RANK_FIELD) : " + map.get(Processor.INFRASPECIFIC_RANK_FIELD));
				String infraspecificRank = ((String)map.get(Processor.INFRASPECIFIC_RANK_FIELD)).trim();
				logger.info("IUCN processor infraspecificRank: "+infraspecificRank);
				nameBuffer.append(" ").append(infraspecificRank);
			}

			if(nullSafeContains(map,Processor.INFRASPECIFIC_EPITHET_FIELD)) {
				logger.info("IUCN processor - map.get(Processor.INFRASPECIFIC_EPITHET_FIELD) : "+ map.get(Processor.INFRASPECIFIC_EPITHET_FIELD));
				String infraspecificEpithet = ((String)map.get(Processor.INFRASPECIFIC_EPITHET_FIELD)).trim();
				logger.info("IUCN processor infraspecificEpithet: "+infraspecificEpithet);
				nameBuffer.append(" ").append(infraspecificEpithet);
			}

			if(nullSafeContains(map,Processor.INFRASPECIFIC_AUTHORITY_FIELD)) {
				logger.info("IUCN processor - map.get(Processor.INFRASPECIFIC_AUTHORITY_FIELD) : "+ map.get(Processor.INFRASPECIFIC_AUTHORITY_FIELD));
				String infraspecificAuthority = StringEscapeUtils.unescapeXml(((String)map.get(Processor.INFRASPECIFIC_AUTHORITY_FIELD)).trim());
				logger.info("IUCN processor infraspecificAuthority: "+infraspecificAuthority);
				nameBuffer.append(" ").append(infraspecificAuthority);
			}
		}

		if(nullSafeContains(map,Processor.TAXONOMIC_AUTHORITY_FIELD)) {
			logger.info("IUCN processor - nullSafeContains(map,Processor.TAXONOMIC_AUTHORITY_FIELD)");
			scientificNameAuthorship = ((String)map.get(Processor.TAXONOMIC_AUTHORITY_FIELD)).trim();
			logger.info("IUCN processor scientificNameAuthorship: "+scientificNameAuthorship);
			nameBuffer.append(" ").append(scientificNameAuthorship);
		}

		logger.info("IUCN processor nameBuffer===: "+nameBuffer);
		String name = nameBuffer.toString();
		logger.info("IUCN processor name===: "+name);
		List<Match<Taxon>> results;
		try {
			results = taxonMatcher.match(name, scientificNameAuthorship);

			if(results.size() == 1) {
				logger.info("IUCN processor name: results size 1");
				return results.get(0).getInternal();
			} else if(results.size() > 1) {
				logger.info(name + " multiple matches");

				for(Match<Taxon> result : results) {
					logger.info("IUCN processor name: result.getInternal().getScientificName()" + result.getInternal().getScientificName());
					logger.info("IUCN processor name: result.getInternal().getInternal()" + result.getInternal().getIdentifier());

				}


				Annotation annotation = new Annotation();
				annotation.setJobId(stepExecution.getJobExecutionId());
				annotation.setAnnotatedObj(null);
				annotation.setRecordType(RecordType.MeasurementOrFact);
				annotation.setCode(AnnotationCode.BadRecord);
				annotation.setType(AnnotationType.Error);
				annotation.setValue("Species Id: " + (Integer)map.get("species_id"));
				annotation.setText(results.size() + " matches found for taxonomic name " + name);
				super.annotate(annotation);
				return null;
			} else {
				logger.info(name + " no matches");
				Annotation annotation = new Annotation();
				annotation.setJobId(stepExecution.getJobExecutionId());
				annotation.setAnnotatedObj(null);
				annotation.setRecordType(RecordType.MeasurementOrFact);
				annotation.setCode(AnnotationCode.Absent);
				annotation.setType(AnnotationType.Error);
				annotation.setValue("Species Id: " + (Integer)map.get("species_id"));
				annotation.setText("No matches found for taxonomic name " + name);
				super.annotate(annotation);
				return null;
			}
		} catch (UnparsableException e) {
			logger.info(name + " is unparseable");
			Annotation annotation = new Annotation();
			annotation.setJobId(stepExecution.getJobExecutionId());
			annotation.setAnnotatedObj(null);
			annotation.setRecordType(RecordType.MeasurementOrFact);
			annotation.setCode(AnnotationCode.Absent);
			annotation.setType(AnnotationType.Error);
			annotation.setValue("Species Id: " + (Integer)map.get("species_id"));
			annotation.setText("Taxonomic name " + name + " cannot be parsed");
			return null;
		}

	}

}
