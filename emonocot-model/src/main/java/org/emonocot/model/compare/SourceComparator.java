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
package org.emonocot.model.compare;

import org.apache.commons.collections.comparators.NullComparator;
import org.emonocot.model.Reference;
import org.emonocot.model.registry.Organisation;

import java.util.Comparator;

public class SourceComparator implements Comparator<Organisation> {

	private NullComparator nullSafeStringComparator;

	public SourceComparator() {
		Comparator<String> stringComparator = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}

		};
		nullSafeStringComparator = new NullComparator(stringComparator, false);
	}

	@Override
	public int compare(Organisation o1, Organisation o2) {
				int compareTitle = nullSafeStringComparator.compare(o1.getTitle(), o2.getTitle());
				if(compareTitle == 0) {
					return nullSafeStringComparator.compare(o1.getTitle(), o2.getTitle());
				} else {
					return compareTitle;
				}
	}

}
