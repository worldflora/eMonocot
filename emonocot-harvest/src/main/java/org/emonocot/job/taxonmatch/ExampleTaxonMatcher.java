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
package org.emonocot.job.taxonmatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.emonocot.api.TaxonService;
import org.emonocot.api.match.Match;
import org.emonocot.api.match.MatchStatus;
import org.emonocot.api.match.Matcher;
import org.emonocot.api.match.taxon.TaxonMatcher;
import org.emonocot.model.Taxon;
import org.emonocot.pager.Page;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jk00kg
 */
public class ExampleTaxonMatcher implements TaxonMatcher, Matcher<String, Taxon> {

	private Logger logger = LoggerFactory.getLogger(ExampleTaxonMatcher.class);

	@Autowired
	private TaxonService taxonService;

	@Autowired
	private NameParser nameParser;

	/**
	 * @param taxonService
	 *            the taxonService to set
	 */
	public void setTaxonService(TaxonService taxonService) {
		this.taxonService = taxonService;
	}

	public void setNameParser(NameParser nameParser) {
		this.nameParser = nameParser;
	}

	/*
	 * Returns one or more match of the same status
	 * @see
	 * org.emonocot.api.match.TaxonMatcher#match(org.gbif.ecat.model.ParsedName
	 * )
	 */
	public List<Match<Taxon>> match(ParsedName<String> parsed) {

		List<Match<Taxon>> matches = new ArrayList<Match<Taxon>>();
		Taxon emonocotTaxon = new Taxon();
		emonocotTaxon.setScientificName(parsed.buildName(true, true, false, false, false,
				false, true, false, false, false, false));
		if (parsed.getAuthorship() != null) {
			emonocotTaxon.setScientificNameAuthorship(parsed.getAuthorship());
		}

		logger.debug("Attempting to match " + emonocotTaxon.getScientificName());

		Page<Taxon> page = taxonService.searchByExample(emonocotTaxon, true, true);

		switch (page.getRecords().size()) {
		case 0:
			if(parsed.getBracketAuthorship() != null){
				parsed.setBracketAuthorship(null);
				matches = match(parsed);
			} else if (parsed.getAuthorship() != null) {
				parsed.setAuthorship(null);
				matches = match(parsed);
			}
			for (Match<Taxon> match : matches) {
				match.setStatus(MatchStatus.PARTIAL);
			}
			break;
		case 1:
			Match<Taxon> single = new Match<Taxon>();
			single.setInternal(page.getRecords().get(0));
			String internalName = (new NameParser().parseToCanonical(single.getInternal().getScientificName()));
			if (emonocotTaxon.getScientificName().equals(internalName)) {
				single.setStatus(MatchStatus.EXACT);
			} else {
				single.setStatus(MatchStatus.PARTIAL);
			}
			matches.add(single);
			break;
		default:
			Set<Match<Taxon>> exactMatches = new HashSet<Match<Taxon>>();
			for (Taxon taxon : page.getRecords()) {
				logger.debug(taxon.getScientificName() + " " + taxon.getIdentifier());
				Match<Taxon> m = new Match<Taxon>();
				m.setInternal(taxon);
				matches.add(m);
				String name = (new NameParser().parseToCanonical(taxon.getScientificName()));
				if (emonocotTaxon.getScientificName().equals(name)) {
					m.setStatus(MatchStatus.EXACT);
					exactMatches.add(m);
				} else {
					m.setStatus(MatchStatus.PARTIAL);
				}
			}
			switch (exactMatches.size()) {
			case 0:
				break;
			case 1:
				matches.retainAll(exactMatches);
				break;
			default:
				logger.debug(exactMatches.size() + " exact matches:");
				for (Match<Taxon> m : exactMatches) {
					logger.debug(m.getInternal().getScientificName() + " exact "
							+ m.getInternal().getIdentifier());
				}
				break;
			}
		}

		return matches;
	}

	@Override
	public List<Match<Taxon>> match(String name) throws UnparsableException {
		ParsedName<String> parsed = nameParser.parse(name);
		return match(parsed);
	}

	@Override
	public List<Match<Taxon>> getMatches(String input) {
		try {
			return match(input);
		} catch (UnparsableException e) {
			logger.error("Couldn't parse the string");
			return null;
		}
	}
}
