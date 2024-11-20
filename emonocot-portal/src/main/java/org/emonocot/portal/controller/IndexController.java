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
package org.emonocot.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.emonocot.api.CommentService;
import org.emonocot.api.SearchableObjectService;
import org.emonocot.model.Comment;
import org.emonocot.model.SearchableObject;
import org.emonocot.pager.Page;
import org.emonocot.service.SolrQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class IndexController {

	private CommentService commentService;

	private SearchableObjectService searchableObjectService;

	private SolrQueryService solrQueryService;

	@Autowired
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	@Autowired
	public void setSearchableObjectService(SearchableObjectService searchableObjectService) {
		this.searchableObjectService = searchableObjectService;
	}
	@Autowired
	public void setSolrQueryService(SolrQueryService solrQueryService) {
		this.solrQueryService = solrQueryService;
	}


	@RequestMapping(method = RequestMethod.GET,produces = "text/html")
	public String index(Model uiModel) throws SolrServerException {
		// Cope with solr unavailability
		try {
			Map<String, String> selectedFacets = new HashMap<String, String>();
			selectedFacets.put("base.class_s", "org.emonocot.model.Comment");
			selectedFacets.put("comment.status_t", "SENT");
			Page<Comment> comments = commentService.search(null, null, 5, 0, null, null, selectedFacets, "comment.created_dt_desc", "aboutData");
			uiModel.addAttribute("comments", comments);
			List<String> responseFacets = new ArrayList<String>();
			responseFacets.add("base.class_s");
			Page<SearchableObject> stats = searchableObjectService.search("", null, 1, 0, responseFacets.toArray(new String[1]), null, null, null, null);
			uiModel.addAttribute("stats", stats);

			String query = "taxon.descriptions_not_empty_b:true";
			Long descriptionsCount = solrQueryService.getResultsCount(query);
			uiModel.addAttribute("descriptionsCount", descriptionsCount);

			String distributionQuery = "taxon.distribution_not_empty_b:true";
			Long distributionCount = solrQueryService.getResultsCount(distributionQuery);
			uiModel.addAttribute("distributionCount", distributionCount);

			String referencesQuery = "taxon.references_not_empty_b:true";
			Long referencesCount = solrQueryService.getResultsCount(referencesQuery);
			uiModel.addAttribute("referencesCount", referencesCount);

			String queryString = "taxon.taxon_rank_s:SPECIES";
			String acceptedSpeciesQuery = "taxon.taxonomic_status_s:Accepted";
			Long acceptedSpeciesCount = solrQueryService.getResultsCount(queryString, acceptedSpeciesQuery);
			uiModel.addAttribute("acceptedSpeciesCount", acceptedSpeciesCount);

			String contentNamesQueryString = "(taxon.descriptions_not_empty_b:true OR taxon.distribution_not_empty_b:true OR taxon.images_not_empty_b:true OR taxon.references_not_empty_b:true)";
			//String contentFilterQuery = "(taxon.descriptions_not_empty_b%3Atrue%20OR%20taxon.distribution_not_empty_b%3Atrue%20OR%20taxon.images_not_empty_b%3Atrue%20OR%20taxon.references_not_empty_b%3Atrue)";//condition
			//String contentFilterQuery = "(taxon.descriptions_not_empty_b:true OR taxon.distribution_not_empty_b:true OR taxon.images_not_empty_b:true OR taxon.references_not_empty_b:true)";//condition
			Long contentNamesCount = solrQueryService.getResultsCount(contentNamesQueryString);
			uiModel.addAttribute("contentNamesCount", contentNamesCount);


		} catch (SolrServerException sse) {

		}

		return "index";
	}

}
