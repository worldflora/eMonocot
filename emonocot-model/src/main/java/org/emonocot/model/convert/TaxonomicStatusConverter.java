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
package org.emonocot.model.convert;

//import org.gbif.ecat.voc.TaxonomicStatus;
import org.emonocot.model.constants.TaxonomicStatus;
import org.springframework.core.convert.converter.Converter;

public class TaxonomicStatusConverter implements Converter<String, TaxonomicStatus> {

	@Override
	public TaxonomicStatus convert(String source) {
		if(source == null || source.isEmpty()) {
			return null;
		}
		else
		{
			switch(source.toLowerCase()) {
				case "heterotypicSynonym":
					source = "heterotypic_Synonym";
					break;
				case "HeterotypicSynonym":
					source = "heterotypic_Synonym";
					break;
				case "Heterotypicsynonym":
					source = "heterotypic_Synonym";
					break;
				case "heterotypicsynonym":
					source = "heterotypic_Synonym";
					break;
				case "homotypicSynonym":
					source = "homotypic_Synonym";
					break;
				case "HomotypicSynonym":
					source = "homotypic_Synonym";
					break;
				case "Homotypicsynonym":
					source = "homotypic_Synonym";
					break;
				case "homotypicsynonym":
					source = "homotypic_Synonym";
					break;
			}
			return TaxonomicStatus.fromString(source);
		}

		}
	}




