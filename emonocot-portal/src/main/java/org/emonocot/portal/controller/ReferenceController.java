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

import org.emonocot.api.ReferenceService;
import org.emonocot.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author ben
 *
 */
@Controller
@RequestMapping("/reference")
public class ReferenceController extends
GenericController<Reference, ReferenceService> {

	/**
	 *
	 */
	public ReferenceController() {
		super("reference", Reference.class);
	}

	/**
	 *
	 * @param referenceService
	 *            Set the reference service
	 */
	@Autowired
	public final void setReferenceService(
			final ReferenceService referenceService) {
		super.setService(referenceService);
	}
}
