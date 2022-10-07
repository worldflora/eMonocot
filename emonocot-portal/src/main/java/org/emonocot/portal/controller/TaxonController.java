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

import org.emonocot.api.TaxonService;
import org.emonocot.api.UserService;
import org.emonocot.model.Taxon;
import org.emonocot.model.TaxonExcluded;
import org.emonocot.model.auth.Permission;
import org.emonocot.model.auth.User;
import org.gbif.ecat.voc.EstablishmentMeans;
import org.gbif.ecat.voc.OccurrenceStatus;
import org.hibernate.UnresolvableObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.Serializable;
import java.security.Principal;

/**
 *
 * @author ben
 *
 */
@Controller
@RequestMapping("/taxon")
public class TaxonController extends GenericController<Taxon, TaxonService> {

	private static Logger queryLog = LoggerFactory.getLogger("query");
	private UserService userService;

	/**
	 *
	 */
	public TaxonController() {
		super("taxon", Taxon.class);
	}

	/**
	 *
	 * @param taxonService
	 *            Set the taxon service
	 */
	@Autowired
	public void setTaxonService(TaxonService taxonService) {
		super.setService(taxonService);
	}


	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * @param identifier
	 *            Set the identifier of the taxon
	 * @param model Set the model
	 * @return A model and view containing a taxon
	 */
	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {"text/html", "*/*"})
	public String show(@PathVariable String identifier, Model model, RedirectAttributes redirectAttributes) {
		Taxon t = getService().load(identifier, "object-page");
		if (t == null) {
			TaxonExcluded taxonExcluded = getService().loadExcludedName(identifier);

			String excluded1 = "Identifier " + taxonExcluded.getIdentifier() + " was excluded.";
			String excluded = "Identifier " + taxonExcluded.getIdentifier() + " for " + taxonExcluded.getScientificName() + " " + taxonExcluded.getScientificNameAuthorship() + " was excluded from WFO. ";
			String excluded_reason = "Excluded reason:" + taxonExcluded.getReason() + ".";

			if (taxonExcluded != null && taxonExcluded.getIdentifier() != null) {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if (authentication.getPrincipal() instanceof String) {
					throw new UnresolvableObjectException(excluded1, "");
				} else {
					User user = (User) authentication.getPrincipal();
					User requestingUser = userService.load(user.getUsername());
					if (requestingUser.getAuthorities().contains(Permission.PERMISSION_ADMINISTRATE)) {
						throw new UnresolvableObjectException(excluded+"<br>"+excluded_reason, "");
					} else {
						throw new UnresolvableObjectException(excluded1, "");
					}
				}
			} else {
				throw new UnresolvableObjectException(identifier, "Object could not be resolved");
			}
		}

		model.addAttribute("taxon", t);
		model.addAttribute("present", OccurrenceStatus.Present);
		model.addAttribute("present", OccurrenceStatus.Present);
		model.addAttribute("absent", OccurrenceStatus.Absent);
		model.addAttribute("nativ", EstablishmentMeans.Native); // native is a keyword in java so we can't use it as a JSP variable, at least in tomcat
		model.addAttribute("introduced", EstablishmentMeans.Introduced);
		queryLog.info("Taxon: \'{}\'", new Object[]{identifier});
		return "taxon/show";
	}

	/**
	 * Many users visit a taxon page and then navigate to the document above, then bounce
	 */
	@RequestMapping(method = RequestMethod.GET, produces = {"text/html", "*/*"})
	public String list(Model model) {
		return "redirect:/search?facet=base.class_s%3aorg.emonocot.model.Taxon";
	}
}
