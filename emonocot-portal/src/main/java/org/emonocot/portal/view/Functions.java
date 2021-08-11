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
package org.emonocot.portal.view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ELException;

import org.apache.commons.lang.StringEscapeUtils;
import org.emonocot.api.job.EmonocotTerm;
import org.emonocot.api.job.WCSPTerm;
import org.emonocot.model.BaseData;
import org.emonocot.model.Description;
import org.emonocot.model.Distribution;
import org.emonocot.model.Identifier;
import org.emonocot.model.Image;
import org.emonocot.model.MeasurementOrFact;
import org.emonocot.model.Reference;
import org.emonocot.model.Taxon;
import org.emonocot.model.TypeAndSpecimen;
import org.emonocot.model.VernacularName;
import org.emonocot.model.compare.AlphabeticalTaxonComparator;
import org.emonocot.model.compare.DistributionComparator;
import org.emonocot.model.compare.LocationComparator;
import org.emonocot.model.compare.ReferenceBasedDescriptionComparator;
import org.emonocot.model.constants.*;
import org.emonocot.model.convert.ClassToStringConverter;
import org.emonocot.model.convert.PermissionToStringConverter;
import org.emonocot.model.registry.Organisation;
import org.emonocot.pager.FacetName;
import org.emonocot.pager.Page;
import org.emonocot.portal.view.bibliography.Bibliography;
import org.emonocot.portal.view.bibliography.SimpleBibliographyImpl;
import org.emonocot.portal.view.bibliography.TaxonomicStatusReference;
import org.emonocot.portal.view.bibliography.TaxonomicStatusReferenceImpl;
import org.emonocot.portal.view.provenance.ProvenanceHolder;
import org.emonocot.portal.view.provenance.ProvenanceManager;
import org.emonocot.portal.view.provenance.ProvenanceManagerImpl;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.IucnTerm;
import org.gbif.ecat.voc.EstablishmentMeans;
import org.gbif.ecat.voc.OccurrenceStatus;
import org.gbif.ecat.voc.Rank;
//import org.gbif.ecat.voc.TaxonomicStatus;
import org.hibernate.proxy.HibernateProxy;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.batch.core.BatchStatus;
import org.springframework.core.convert.support.DefaultConversionService;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *
 * @author ben
 *
 */
public class Functions {

	private static DateTimeFormatter timeOnlyFormatter = DateTimeFormat
			.forPattern("HH:mm:ss");

	private static DefaultConversionService conversionService = new DefaultConversionService();

	private static double MAX_DEGREES_LATITUDE = 180.0;

	private static Color[] baseColors = { new Color(255, 210, 54),
			new Color(213, 12, 84), new Color(111, 148, 73),
			new Color(0, 116, 204), new Color(97, 102, 104) };

	static {
		conversionService.addConverter(new PermissionToStringConverter());
		conversionService.addConverter(new ClassToStringConverter());
	}

	private Functions() {
	}

	public static String escape(final String string) {
		return StringEscapeUtils.escapeXml(string);
	}

	public static Boolean isChildFacet(String parent, String facet) {
		try {
			FacetName parentFacetName = FacetName.fromString(parent);
			FacetName facetName = FacetName.fromString(facet);
			if(parentFacetName.getChild() == null || !parentFacetName.getChild().equals(facetName)) {
				return Boolean.FALSE;
			} else {
				return Boolean.TRUE;
			}
		} catch(IllegalArgumentException iae) {
			return Boolean.FALSE;
		}
	}

	/**
	 *
	 * @param string
	 *            Set the string to strip xml from
	 * @return an stripped string
	 */
	public static String stripXml(String string) {
		return string.replaceAll("\\<.*?>", "");
	}

	public static String printTimeOnly(DateTime dateTime) {
		if (dateTime == null) {
			return null;
		} else {
			return timeOnlyFormatter.print(dateTime.minusHours(1));
		}
	}

	/**
	 * @param status
	 *            Set the status
	 * @return true if the job is startable
	 */
	public static Boolean isStartable(BatchStatus status) {
		if (status == null) {
			return Boolean.TRUE;
		} else {
			switch (status) {
			case STARTED:
			case STARTING:
			case STOPPING:
			case UNKNOWN:
				return Boolean.FALSE;
			case COMPLETED:
			case FAILED:
			case STOPPED:
			default:
				return Boolean.TRUE;
			}
		}
	}

	public static Map<String, Map<String, String>> phylocolors(
			Collection<Taxon> taxa) {
		Map<String, Map<String, String>> phylocolors = new HashMap<String, Map<String, String>>();

		Set<String> orders = new HashSet<String>();
		Set<String> families = new HashSet<String>();
		Set<String> genera = new HashSet<String>();

		for (Taxon t : taxa) {
			if (t.getOrder() != null && !t.getOrder().isEmpty()) {
				orders.add(t.getOrder());
			}
			if (t.getFamily() != null && !t.getFamily().isEmpty()) {
				families.add(t.getFamily());
			}
			if (t.getGenus() != null && !t.getGenus().isEmpty()) {
				genera.add(t.getGenus());
			}
		}

		phylocolors.put("order", getColorMap(orders));
		phylocolors.put("family", getColorMap(families));
		phylocolors.put("genus", getColorMap(genera));

		return phylocolors;
	}

	private static Map<String, String> getColorMap(Set<String> categories) {
		int numberOfCategories = categories.size();
		float increment = 0.5f / (numberOfCategories / 5);
		Map<String, String> colorMap = new HashMap<String, String>();

		int i = 0;
		for (String category : categories) {
			Color baseColor = baseColors[i % 5];
			int offset = i / 5;
			if (offset > 0) {
				float hsbVals[] = Color.RGBtoHSB(baseColor.getRed(),
						baseColor.getGreen(), baseColor.getBlue(), null);
				Color highlight = Color.getHSBColor(hsbVals[0], hsbVals[1],
						offset * increment * (1f + hsbVals[2]));
				colorMap.put(category,
						String.format("#%06X", (0xFFFFFF & highlight.getRGB())));
			} else {
				colorMap.put(category,
						String.format("#%06X", (0xFFFFFF & baseColor.getRGB())));
			}
			i++;
		}

		return colorMap;
	}

	public static String evaluate(String expressionString,
								  PageContext pageContext) throws ELException {
		return (String) pageContext.getExpressionEvaluator().evaluate(
				expressionString, String.class,
				pageContext.getVariableResolver(), null);
	}

	static PrettyTime prettyTime = new PrettyTime();

	public static String prettyTime(DateTime dateTime) {
		if (dateTime == null) {
			return null;
		} else {
			return prettyTime.format(dateTime.toDate());
		}
	}

	/**
	 *
	 * @param rank
	 *            Set the rank
	 * @return the abbreviated rank
	 */
	public static String abbreviateRank(Rank rank) {
		if (rank == null) {
			return null;
		} else {
			switch (rank) {
				case VARIETY:
					return "var";
				case InfraspecificName:
					return "infrasp";
				case SUBSPECIES:
					return "ssp";
				case SPECIES:
					return "sp";
				case GENUS:
					return "gen";
				case Subtribe:
					return "subtrib";
				case Tribe:
					return "trib";
				case Subfamily:
					return "subfam";
				case FAMILY:
					return "fam";
				default:
					return Rank.toAbbreviation(rank);

			}
		}
	}

	/**
	 *
	 * @param rank
	 *            Set the rank
	 * @return the formatted rank
	 */
	public static String formatRank(Rank rank) {
		if (rank == null) {
			return null;
		} else {
			String r = rank.name();
			return r.substring(0, 1).toUpperCase()
					+ r.substring(1).toLowerCase();
		}
	}

	/**
	 * @param rank
	 *            Set the rank
	 * @return true if the rank is infraspecific
	 */
	public static Boolean isInfraspecific(Rank rank) {
		if (rank == null) {
			return Boolean.FALSE;
		} else {
			if (rank.compareTo(Rank.SPECIES) > 0) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}
	}

	/**
	 * @param taxon
	 *            Set the taxon
	 * @return the protolog link of the taxon if it exists
	 */
	public static Identifier getProtologLink(Taxon taxon) {
		for (Identifier identifier : taxon.getIdentifiers()) {
			if (identifier.getSubject() != null
					&& identifier.getSubject().equals("Protolog")) {
				return identifier;
			}
		}
		return null;
	}

	/**
	 * @param preferred
	 *            Set the preferred
	 * @return the preferred name of the vernacularNames
	 */
	public static VernacularName getPreferred(Taxon taxon) {
		for (VernacularName vernacular : taxon.getVernacularNames()) {
			if (vernacular.getPreferredName() == true) {
				return vernacular;
			}
		}
		for (VernacularName vernacular : taxon.getVernacularNames()) {
			return vernacular;
		}
		return null;
	}

	public static Boolean isPreferred(Taxon taxon) {
		boolean isTrue = false;
		for (VernacularName vernacular : taxon.getVernacularNames()) {
			if (vernacular.getPreferredName() == true) {
				isTrue = true;
			}
		}
		return isTrue;
	}

	public static int getCount(Taxon taxon) {
		int count = 0;
		int truecount = 0;
		int falsecount = 0;
		for (VernacularName vernacular : taxon.getVernacularNames()) {
			if (vernacular.getPreferredName() == true) {
				truecount = 1;
			}
			if (vernacular.getPreferredName() == false) {
				falsecount = 1;
			}
		}
		count = truecount + falsecount;
		return count;
	}

	public static int getHomotypicSynonymsCount(Taxon taxon) {
		int count = 0;
		for (Taxon t : taxon.getSynonymNameUsages()) {
			if (t.getTaxonomicStatus().equals(TaxonomicStatus.Homotypic_Synonym)) {
				count = 1;
			}
		}
		return count;
	}

	public static int getHeterotypicSynonymsCount(Taxon taxon) {
		int count = 0;
		for (Taxon t : taxon.getSynonymNameUsages()) {
			if (t.getTaxonomicStatus().equals(TaxonomicStatus.Heterotypic_Synonym)) {
				count = 1;
			}
		}
		return count;
	}

	public static int getSynonymsCount(Taxon taxon) {
		int count = 0;
		for (Taxon t : taxon.getSynonymNameUsages()) {
			//if (t.getTaxonomicStatus().equals(TaxonomicStatus.Synonym) || t.getTaxonomicStatus().equals(TaxonomicStatus.DeterminationSynonym) || t.getTaxonomicStatus().equals(TaxonomicStatus.Synonym)) {
			switch (t.getTaxonomicStatus()) {
				case Synonym:
				case DeterminationSynonym:
				case IntermediateRankSynonym:
				case Proparte_Synonym:
					count = 1;
			}
		}
		return count;
	}
	/**
	 * @param taxon
	 *            Set the taxon
	 * @return true if the taxon is a synonym
	 */
	public static Boolean isSynonym(Taxon taxon) {
		if (taxon.getTaxonomicStatus() == null) {
			return false;
		} else {
			switch (taxon.getTaxonomicStatus()) {
				case Synonym:
//			 case Heterotypic_Synonym:
//			 case Homotypic_Synonym:
				case DeterminationSynonym:
				case IntermediateRankSynonym:
				case Proparte_Synonym:
					return true;
				case Heterotypic_Synonym:
				case Homotypic_Synonym:
				case Accepted:
				case Doubtful:
				case Unchecked:
				case Misapplied:
				default:
					return false;
			}
		}
	}

	public static Boolean isHeterotypicSynonym(Taxon taxon) {
		if (taxon.getTaxonomicStatus() == null) {
			return false;
		} else {
			switch (taxon.getTaxonomicStatus()) {
				case Heterotypic_Synonym:
//				case heterotypicSynonym:
					return true;
				case Homotypic_Synonym:
				case DeterminationSynonym:
				case IntermediateRankSynonym:
				case Proparte_Synonym:
				case Accepted:
				case Doubtful:
				case Unchecked:
				case Misapplied:
				default:
					return false;
			}
		}
	}

	public static Boolean isHomotypicSynonym(Taxon taxon) {
		if (taxon.getTaxonomicStatus() == null) {
			return false;
		} else {
			return taxon.getTaxonomicStatus().equals(TaxonomicStatus.Homotypic_Synonym);
		}
	}

	/**
	 * @param taxon
	 *            Set the taxon
	 * @return true if the taxon is accepted
	 */
	public static Boolean isAccepted(Taxon taxon) {
		if (taxon.getTaxonomicStatus() == null) {
			return false;
		} else {
			return taxon.getTaxonomicStatus().equals(TaxonomicStatus.Accepted);
		}
	}

	/**
	 *
	 * @return the list of features
	 */
	public static DescriptionType[] features() {
		return DescriptionType.values();
	}

	/**
	 * @param region
	 *            Set the region
	 * @return the country code or null if the distribution is at regional level
	 *         or above
	 */
	public static String country(Location region) {
		if (region == null || region.getLevel().equals(2)
				|| region.getLevel().equals(0)) {
			return null;
		} else {
			return region.getCode();
		}
	}

	/**
	 * @param region
	 *            Set the region
	 * @return the region code or null if the distribution is at continent level
	 */
	public static String region(Location region) {
		if (region == null || region.getLevel().equals(0)) {
			return null;
		} else if (region.getLevel().equals(1)) {
			return region.getCode();
		} else {
			return region.getParent().getCode();
		}
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @param feature
	 *            Set the feature
	 * @return a Content object, or null
	 */
	public static List<Description> content(Taxon taxon, DescriptionType feature) {
		List<Description> descriptions = new ArrayList<Description>();
		for (Description d : taxon.getDescriptions()) {
			if (d.getType().equals(feature)) {
				descriptions.add(d);
			}
		}
		Collections.sort(descriptions,new ReferenceBasedDescriptionComparator());
		return descriptions;
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return the list of regions we have distribution records for
	 */
	public static List<Location> regions(Collection<Distribution> distribution) {
		List<Location> regions = new ArrayList<Location>();
		for (Distribution d : distribution) {
			regions.add(d.getLocation());
		}
		LocationComparator comparator = new LocationComparator();
		Collections.sort(regions, comparator);
		return regions;
	}

	/**
	 *
	 * @param taxa
	 *            Set the taxa to sort
	 * @return the list of taxa sorted alphabetically
	 */
	public static List<Taxon> sort(Collection<Taxon> taxa) {
		Comparator<Taxon> comparator = new AlphabeticalTaxonComparator();
		List<Taxon> list = new ArrayList<Taxon>(taxa);
		Collections.sort(list, comparator);
		return list;
	}

	public static String duration(Duration duration) {
		if (duration == null) {
			return null;
		} else {
			return PeriodFormat.getDefault().print(new Period(duration));
		}
	}

	/**
	 * Returns a string which can be passed to the EDIT Map REST Service to
	 * produce a distribution map.
	 *
	 * http://dev.e-taxonomy.eu/trac/wiki/MapRestServiceApi
	 *
	 * TODO only assumes the status "present" at the moment. TODO assumes that
	 * level 3 is the finest level of detail
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return a string which can be passed to the map rest service
	 */
	public static String map(Taxon taxon) {
		StringBuffer stringBuffer = new StringBuffer();

		List<Location> continents = new ArrayList<Location>();
		List<Location> regions = new ArrayList<Location>();
		List<Location> countries = new ArrayList<Location>();

		for (Distribution distribution : taxon.getDistribution()) {
			if (distribution.getLocation().getLevel().equals(0)) {
				continents.add(distribution.getLocation());
			} else if (distribution.getLocation().getLevel().equals(1)) {
				regions.add(distribution.getLocation());
			} else {
				countries.add(distribution.getLocation());
			}
		}
		boolean hasLevel1 = !continents.isEmpty();
		boolean hasLevel2 = !regions.isEmpty();
		boolean hasLevel3 = !countries.isEmpty();

		if (hasLevel1) {
			stringBuffer.append("tdwg1:");
			appendAreas(stringBuffer, Status.present, continents);

			if (hasLevel2 || hasLevel3) {
				stringBuffer.append("||");
			}
		}
		if (hasLevel2) {
			stringBuffer.append("tdwg2:");
			appendAreas(stringBuffer, Status.present, regions);
			if (hasLevel3) {
				stringBuffer.append("||");
			}
		}
		if (hasLevel3) {
			stringBuffer.append("tdwg3:");
			appendAreas(stringBuffer, Status.present, countries);
		}

		return stringBuffer.toString();
	}

	/**
	 *
	 * @param stringBuffer
	 *            a stringbuffer containing a partially constructed url fragment
	 * @param status
	 *            the status of the taxon in the following regions
	 * @param areas
	 *            a list of regions
	 */
	private static void appendAreas(StringBuffer stringBuffer, Status status,
									List<? extends Location> areas) {
		stringBuffer.append(status.name() + ":");
		stringBuffer.append(areas.get(0).getCode());

		for (int i = 1; i < areas.size(); i++) {
			stringBuffer.append("," + areas.get(i).getCode());
		}
	}

	public static List<String> sortItems() {
		List<String> sortItems = new ArrayList<String>();
		sortItems.add("searchable.label_sort_asc");
		sortItems.add("base.created_dt_desc");
		sortItems.add("_asc");
		return sortItems;
	}

	public static List<String> annotationSortItems() {
		List<String> sortItems = new ArrayList<String>();
		sortItems.add("annotation.type_s_asc");
		sortItems.add("annotation.code_s_asc");
		sortItems.add("annotation.record_type_s_asc");
		sortItems.add("base.created_dt_desc");
		sortItems.add("_asc");
		return sortItems;
	}

	public static List<String> organisationItems() {
		List<String> sortItems = new ArrayList<String>();
		sortItems.add("searchable.label_sort_asc");
		sortItems.add("_asc");
		return sortItems;
	}

	public static List<String> commentItems() {
		List<String> sortItems = new ArrayList<String>();
		sortItems.add("comment.created_dt_desc");
		sortItems.add("_asc");
		return sortItems;
	}

	public static List<String> resourceSortItems() {
		List<String> sortItems = new ArrayList<String>();
		sortItems.add("resource.last_harvested_dt_desc");
		sortItems.add("resource.last_harvested_dt_asc");
		sortItems.add("resource.duration_l_desc");
		sortItems.add("resource.duration_l_asc");
		sortItems.add("resource.records_read_l_asc");
		sortItems.add("resource.records_read_l_desc");
		sortItems.add("base.created_dt_desc");
		sortItems.add("_asc");
		return sortItems;
	}

	private static Pattern pattern = Pattern
			.compile("\\[(.*?) TO (.*?)\\+(\\d)(\\w+)\\]");

	private static DateTimeFormatter solrDateTimeFormatter = DateTimeFormat
			.forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");

	public static String formatDateRange(String dateRange) {

		Matcher matcher = pattern.matcher(dateRange);

		if (matcher.matches()) {
			String beginningString = matcher.group(1);
			String endString = matcher.group(2);

			DateTime beginning = solrDateTimeFormatter
					.parseDateTime(beginningString);
			DateTime end = solrDateTimeFormatter.parseDateTime(endString);
			Integer gap = Integer.parseInt(matcher.group(3));
			String increment = matcher.group(4);
			DateTimeFormatter dateTimeFormatter = null;
			switch (increment) {
				case "DAY":
					end = end.plusDays(gap);
					dateTimeFormatter = DateTimeFormat.shortDate();
					break;
				case "WEEK":
					end = end.plusWeeks(gap);
					dateTimeFormatter = DateTimeFormat.shortDate();
					break;
				case "MONTH":
					end = end.plusMonths(gap);
					dateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM");
					break;
				case "YEAR":
					end = end.plusYears(gap);
					dateTimeFormatter = DateTimeFormat.forPattern("yyyy");
					break;
			}

			return dateTimeFormatter.print(beginning) + " - "
					+ dateTimeFormatter.print(end);

		} else {
			return dateRange;
		}

	}

	/**
	 *
	 * @param object
	 *            the object, which may be a proxy or may not
	 * @return the object
	 */
	public static Object deproxy(Object object) {
		if (object instanceof HibernateProxy) {
			return ((HibernateProxy) object).getHibernateLazyInitializer()
					.getImplementation();
		} else {
			return object;
		}
	}

	/**
	 *
	 * @param start
	 *            Set the start date
	 * @param end
	 *            Set the end date
	 * @return a formatted period
	 */
	public static String formatPeriod(Date start, Date end) {
		DateTime startDate = new DateTime(start);
		DateTime endDate = new DateTime(end);

		Period period = new Interval(startDate, endDate).toPeriod();
		PeriodFormatter formatter = new PeriodFormatterBuilder()
				.minimumPrintedDigits(2).appendHours().appendSeparator(":")
				.appendMinutes().appendSeparator(":").appendSeconds()
				.toFormatter();
		return formatter.print(period);
	}

	/**
	 *
	 * @param date
	 *            Set the date
	 * @return a formatted date
	 */
	public static String formatDate(Date date) {
		DateTime dateTime = new DateTime(date);
		return DateTimeFormat.forStyle("SS").print(dateTime);
	}

	/**
	 *
	 * @param list
	 *            Set the list
	 * @param index
	 *            Set the index
	 * @return a sublist starting at index
	 */
	public static List<Taxon> sublist(List<Taxon> list, Integer index) {
		return list.subList(index, list.size());
	}

	/**
	 *
	 * @param string
	 *            The string to split
	 * @param pattern
	 *            The pattern to split upon
	 * @return an array of substrings
	 */
	public static List<String> split(String string, String pattern) {
		return Arrays.asList(string.split(pattern));
	}

	/**
	 *
	 * @param object
	 *            the object to convert
	 * @return a string
	 */
	public static String convert(Object object) {
		return conversionService.convert(object, String.class);
	}

	/**
	 * To cope with the fact that we can't use the reserved word 'class' in JSP
	 * Expression language and Tomcat 7 complains.
	 *
	 * @param object
	 *            the object to convert
	 * @return the fully qualified class name
	 */
	public static String convertClazz(Object object) {
		return conversionService.convert(object.getClass(), String.class);
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return the bibliography
	 */
	public static Bibliography bibliography(Taxon taxon) {
		Bibliography bibliography = new SimpleBibliographyImpl();
		bibliography.setReferences(taxon);
		return bibliography;
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return the nameAccordingTo
	 */
	public static TaxonomicStatusReference taxonomicStatusReference(Taxon taxon) {
		TaxonomicStatusReference tsr = new TaxonomicStatusReferenceImpl();
		tsr.setReferences(taxon);
		return tsr;
	}

	/**
	 *
	 * @param bibliography
	 *            Set the bibliography
	 * @param reference
	 *            Set the reference
	 * @return the citation key
	 */
	public static String citekey(Bibliography bibliography, Reference reference) {
		return bibliography.getKey(reference);
	}

	/**
	 *
	 * @param bibliography
	 *            Set the bibliography
	 * @param reference
	 *            Set the reference
	 * @return the citation key
	 */
	public static String taxonomicStatusRefcitekey(TaxonomicStatusReference bibliography, Reference reference) {
		return bibliography.getKey(reference);
	}

	public static SortedSet<String> citekeys(Bibliography bibliography,
			 Collection<Distribution> distributions) {
		return bibliography.getKeys(distributions);
	}

	public static Reference getref(Bibliography bibliography, String key) {
		return bibliography.getReference(key);
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return the provenance
	 */
	public static ProvenanceManager provenance(BaseData data) {
		ProvenanceManager provenance = new ProvenanceManagerImpl();
		provenance.setProvenance(data);
		return provenance;
	}

	/**
	 *
	 * @param provenance
	 *            Set the provenance
	 * @param data
	 *            Set the data
	 * @return the provenance key
	 */
	public static String provenancekey(ProvenanceManager provenance,
									   BaseData data) {
		return provenance.getKey(data);
	}

	public static String initialtokey(String initial){
//		 return initial.substring(initial.length() - 1).toUpperCase();
		return initial.substring(1,2).toUpperCase();
	}

	/**
	 *
	 * @param provenance
	 *            Set the provenance
	 * @param data
	 *            Set the data
	 * @return the provenance initial
	 */
	 /*public static String provenanceinitial(ProvenanceManager provenance,
			 BaseData data) {
		 String key = provenance.getKey(data).toUpperCase();
		 String orgname = data.getAuthority().getTitle();
		 String initials = "";
		 for (int i = 0, n = orgname.length(); i < n; i++){
			 if (Character.isUpperCase(orgname.charAt(i))){
				 initials += orgname.charAt(i);
			 }
		 }

		 initials += "." + key;
		 return initials;
	 }*/


	public static String provenanceinitial(ProvenanceManager provenance,
										   BaseData data) {
		String key = provenance.getKey(data).toUpperCase();
		String orgname = data.getAuthority().getTitle();
		String provenanceName = "";

//		 provenanceName = orgname + "." + key;
		provenanceName = "[" +key +"]" + "." + orgname;
		return provenanceName;
	}

	public static String majorGroupName(String majorGroup) {
		String mgName = "";
		switch (majorGroup) {
			case "A":
				mgName = "Angiosperms";
				break;
			case "B":
				mgName = "Bryophytes";
				break;
			case "G":
				mgName = "Gymnosperms";
				break;
			case "P":
				mgName = "Pteridophytes";
				break;
		}
		return mgName;
	}

	public static String getAncestorsWFOId(Taxon t, String name) {
		String wfoId = "";

		if (t.getHigherClassification() != null) {
			for (Taxon higherClassification : t.getHigherClassification()) {
				if( higherClassification.getScientificName()!= null)
				{
					if(higherClassification.getScientificName().equalsIgnoreCase(name))
					{
						wfoId = higherClassification.getIdentifier();
					}
				}
			}
		}
		return wfoId;
	}


	public static List<String> getSource(Taxon t) {
//		Source s = t.getSource().toString();
		List<String> list = new ArrayList<String>();
		if(t.getSource() == null || t.getSource().isEmpty()) {
			return null;
		}
		else
		{
			switch(t.getSource()) {
				case "gcc":
//					s = Source.gcc;
					list.add(Source.gcc.getFull());
					list.add(Source.gcc.getUrl());
					break;
				case "ildis":
//					s = Source.ildis;
					list.add(Source.ildis.getFull());
					list.add(Source.ildis.getUrl());
					break;
				case "tro":
//					s = Source.tro;
					list.add(Source.tro.getFull());
					list.add(Source.tro.getUrl());
					break;
				case "cmp":
//					s = Source.cmp;
					list.add(Source.cmp.getFull());
//					list.add(Source.cmp.getUrl());
					break;
				case "ksu":
//					s = Source.ksu;
					list.add(Source.ksu.getFull());
//					list.add(Source.ksu.getUrl());
					break;
				case "ksufab":
//					s = Source.ksufab;
					list.add(Source.ksufab.getFull());
//					list.add(Source.ksufab.getUrl());
					break;
				case "wcs":
//					s = Source.wcs;
					list.add(Source.wcs.getFull());
					list.add(Source.wcs.getUrl());
					break;
				case "iplants":
//					s = Source.iplants;
					list.add(Source.iplants.getFull());
					list.add(Source.iplants.getUrl());
					break;
				case "iopi":
//					s = Source.iopi;
					list.add(Source.iopi.getFull());
					list.add(Source.iopi.getUrl());
					break;
				case "rjp":
//					s = Source.rjp;
					list.add(Source.rjp.getFull());
//					list.add(Source.rjp.getUrl());
					break;
				case "ifern":
//					s = Source.ifern;
					list.add(Source.ifern.getFull());
					list.add(Source.ifern.getUrl());
					break;
				case "ipni":
//					s = Source.ipni;
					list.add(Source.ipni.getFull());
					list.add(Source.ipni.getUrl());
					break;
				case "wcsir":
//					s = Source.wcsir;
					list.add(Source.wcsir.getFull());
					list.add(Source.wcsir.getUrl());
					break;
					default:
						list.add(t.getSource());

			}
		}
		return list;
	}


	public static String getFullSource(String source) {
		String s = source;
		if(source == null || source.isEmpty()) {
			return null;
		}
		else
		{
			switch(source) {
				case "gcc":
					s = "TICA";
					break;
				case "ildis":
					s = "ILDIS";
					break;
				case "tro":
					s ="Tropicos";
					break;
				case "cmp":
					s = "WCSP (in review)";
					break;
				case "ksu":
					s = "WCSP (in review)";
					break;
				case "ksufab":
					s = "WCSP (in review)";
					break;
				case "wcs":
					s = "WCSP";
					break;
				case "iplants":
					s = "iPlants";
					break;
				case "iopi":
					s = "IOPI";
					break;
				case "rjp":
					s ="RJP";
					break;
				case "ifern":
					s = "IPNI";
					break;
				case "ipni":
					s = "IPNI";
					break;
				case "wcsir":
					s = "WCSP (in review)";
					break;
					default:
						s = source;
			}
		}
		return s;
	}

	private void getAncestors(Taxon t, List<Taxon> ancestors) {
		if ((t.getParentNameUsage() != null) ) {

			getAncestors(t.getParentNameUsage(), ancestors);
		}
		ancestors.add(t);
	}

	public static String getAuthor(String name) {

		String author = name;
		StringBuilder authorsb = new StringBuilder();

		authorsb.append(name);
		authorsb=authorsb.reverse();
		int dotPos=authorsb.indexOf(".");
			if (dotPos == 0) {
			authorsb=authorsb.replace(0,1,"");
			authorsb=authorsb.reverse();
			author=authorsb.toString();
		}
		return author;
	}

//	public static String provenancecitation(ProvenanceManager provenance,
//										   BaseData data) {
//		String key = provenance.getKey(data).toUpperCase();
//		String orgname = data.getAuthority().getTitle();
//		String provenanceName = "";
//
//		provenanceName = "Provided by: [" +key + "]" +"." + orgname;
//		return provenanceName;
//	}

	public static String provenancename(ProvenanceManager provenance,
										BaseData data) {
		String orgname = data.getAuthority().getTitle();
		String provenanceName = "";

		provenanceName = orgname;
		return provenanceName;
	}

	public static SortedSet<String> provenanceinitials(ProvenanceManager provenance, Collection<BaseData> data)
	{
		SortedSet<String> initials = new TreeSet<String>();
		for(BaseData baseData : data) {
			initials.add(provenanceinitial(provenance, baseData));
		}
		return initials;
	}



	public static SortedSet<String> provenancekeys(
			ProvenanceManager provenance, Collection<BaseData> data) {
		return provenance.getKeys(data);
	}

	public static SortedSet<Organisation> provenancesources(
			ProvenanceManager provenance) {
		return provenance.getSources();
	}

	public static SortedSet<ProvenanceHolder> provenancedata(
			ProvenanceManager provenance, Organisation organisation) {
		return provenance.getProvenanceData(organisation);
	}

	/**
	 *
	 * @param pager
	 *            Set the pager
	 * @param facet
	 *            Set the facet name
	 * @return true, if the facet is selected
	 */
	public static Boolean isFacetSelected(Page pager, String facet) {
		return pager.isFacetSelected(facet);
	}

	public static String escapeHtmlIdentifier(String identifier) {
		return identifier.replaceAll("\\.", "").replaceAll("_", "");
	}

	public static Set<Organisation> sources(Taxon taxon) {
		Set<Organisation> sources = new HashSet<Organisation>();
		sources.add(taxon.getAuthority());
		for (Description d : taxon.getDescriptions()) {
			sources.add(d.getAuthority());
		}

		for (Distribution d : taxon.getDistribution()) {
			sources.add(d.getAuthority());
		}

		for (Image i : taxon.getImages()) {
			sources.add(i.getAuthority());
		}

		for (Reference r : taxon.getReferences()) {
			sources.add(r.getAuthority());
		}

		for (Identifier i : taxon.getIdentifiers()) {
			sources.add(i.getAuthority());
		}

		for (TypeAndSpecimen s : taxon.getTypesAndSpecimens()) {
			sources.add(s.getAuthority());
		}

		for (MeasurementOrFact f : taxon.getMeasurementsOrFacts()) {
			sources.add(f.getAuthority());
		}

		for (VernacularName n : taxon.getVernacularNames()) {
			sources.add(n.getAuthority());
		}

		return sources;
	}

	private static OccurrenceStatus toPresentAbsent(OccurrenceStatus o) {
		if (o == null) {
			return OccurrenceStatus.Present;
		} else {
			switch (o) {
				case Absent:
				case Excluded:
					return OccurrenceStatus.Absent;
				case Present:
				case Common:
				case Rare:
				case Irregular:
				case Doubtful:
				default:
					return OccurrenceStatus.Present;
			}
		}
	}

	private static EstablishmentMeans toNativeIntroduced(EstablishmentMeans e) {
		if (e == null) {
			return EstablishmentMeans.Native;
		} else {
			switch (e) {
				case Introduced:
				case Invasive:
				case Managed:
				case Naturalised:
					return EstablishmentMeans.Introduced;
				case Uncertain:
				case Native:
				default:
					return EstablishmentMeans.Native;
			}
		}
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return the bounding box
	 */
	public static String boundingBox(Taxon taxon,
									 OccurrenceStatus occurrenceStatus) {
		List<Geometry> list = new ArrayList<Geometry>();
		for (Distribution d : taxon.getDistribution()) {
			if (toPresentAbsent(d.getOccurrenceStatus()).equals(
					occurrenceStatus)) {
				list.add(d.getLocation().getEnvelope());
			}
		}
		GeometryCollection geometryCollection = new GeometryCollection(
				list.toArray(new Geometry[list.size()]), new GeometryFactory());

		Coordinate[] envelope = geometryCollection.getEnvelope()
				.getCoordinates();
		StringBuffer boundingBox = new StringBuffer();
		boundingBox.append(Math.round(envelope[0].x));
		boundingBox.append(",");
		boundingBox.append(Math.round(envelope[0].y));
		boundingBox.append(",");
		boundingBox.append(Math.round(envelope[2].x));
		boundingBox.append(",");
		boundingBox.append(Math.round(envelope[2].y));
		return boundingBox.toString();
	}

	public static Boolean hasDistribution(Taxon taxon,
										  OccurrenceStatus occurrenceStatus,
										  EstablishmentMeans establishmentMeans) {
		for (Distribution d : taxon.getDistribution()) {
			if (toPresentAbsent(d.getOccurrenceStatus()).equals(occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(establishmentMeans)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public static Collection<Distribution> getDistribution(Taxon taxon,
														   OccurrenceStatus occurrenceStatus,
														   EstablishmentMeans establishmentMeans) {
		SortedSet<Distribution> distribution = new TreeSet<Distribution>(new DistributionComparator());
		for (Distribution d : taxon.getDistribution()) {
			if (toPresentAbsent(d.getOccurrenceStatus()).equals(occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(establishmentMeans)) {
				distribution.add(d);
			}
		}
		return distribution;
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return true if the taxon has level 1 features, false otherwise
	 */
	public static Boolean hasLevel1Features(Taxon taxon,
			OccurrenceStatus occurrenceStatus,
			EstablishmentMeans establishmentMeans) {
		for (Distribution d : taxon.getDistribution()) {
			if (d.getLocation().getLevel().equals(0)
					&& toPresentAbsent(d.getOccurrenceStatus()).equals(
					occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(
					establishmentMeans)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return the level 1 feature identifiers (FIDs)
	 */
	public static String getLevel1Features(Taxon taxon,
		OccurrenceStatus occurrenceStatus,
		EstablishmentMeans establishmentMeans) {
		boolean first = true;
		StringBuffer features = new StringBuffer();
		for (Distribution d : taxon.getDistribution()) {
			if (d.getLocation().getLevel().equals(0)
					&& toPresentAbsent(d.getOccurrenceStatus()).equals(
					occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(
					establishmentMeans)) {
				if (!first) {
					features.append(",");
				}
				features.append(d.getLocation().getFeatureId());
				first = false;
			}
		}
		return features.toString();
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return true if the taxon has level 2 features, false otherwise
	 */
	public static Boolean hasLevel2Features(Taxon taxon,
											OccurrenceStatus occurrenceStatus,
											EstablishmentMeans establishmentMeans) {
		for (Distribution d : taxon.getDistribution()) {
			if (d.getLocation().getLevel().equals(1)
					&& toPresentAbsent(d.getOccurrenceStatus()).equals(
					occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(
					establishmentMeans)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return the level 2 feature identifiers (FIDs)
	 */
	public static String getLevel2Features(Taxon taxon,
										   OccurrenceStatus occurrenceStatus,
										   EstablishmentMeans establishmentMeans) {
		boolean first = true;
		StringBuffer features = new StringBuffer();
		for (Distribution d : taxon.getDistribution()) {
			if (d.getLocation().getLevel().equals(1)
					&& toPresentAbsent(d.getOccurrenceStatus()).equals(
					occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(
					establishmentMeans)) {
				if (!first) {
					features.append(",");
				}
				features.append(d.getLocation().getFeatureId());
				first = false;
			}
		}
		return features.toString();
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return true if the taxon has level 3 features, false otherwise
	 */
	public static Boolean hasLevel3Features(Taxon taxon,
											OccurrenceStatus occurrenceStatus,
											EstablishmentMeans establishmentMeans) {
		for (Distribution d : taxon.getDistribution()) {
			if (d.getLocation().getLevel().equals(2)
					&& toPresentAbsent(d.getOccurrenceStatus()).equals(
					occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(
					establishmentMeans)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public static Boolean hasLevel4Features(Taxon taxon,
											OccurrenceStatus occurrenceStatus,
											EstablishmentMeans establishmentMeans) {
		for (Distribution d : taxon.getDistribution()) {
			if (d.getLocation().getLevel().equals(3)
					&& toPresentAbsent(d.getOccurrenceStatus()).equals(
					occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(
					establishmentMeans)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @return the level 3 feature identifiers (FIDs)
	 */
	public static String getLevel3Features(Taxon taxon,
										   OccurrenceStatus occurrenceStatus,
										   EstablishmentMeans establishmentMeans) {
		boolean first = true;
		StringBuffer features = new StringBuffer();
		for (Distribution d : taxon.getDistribution()) {
			if (d.getLocation().getLevel().equals(2)
					&& toPresentAbsent(d.getOccurrenceStatus()).equals(
					occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(
					establishmentMeans)) {
				if (!first) {
					features.append(",");
				}
				features.append(d.getLocation().getFeatureId());
				first = false;
			}
		}
		return features.toString();
	}

	public static String getLevel4Features(Taxon taxon,
										   OccurrenceStatus occurrenceStatus,
										   EstablishmentMeans establishmentMeans) {
		boolean first = true;
		StringBuffer features = new StringBuffer();
		for (Distribution d : taxon.getDistribution()) {
			if (d.getLocation().getLevel().equals(3)
					&& toPresentAbsent(d.getOccurrenceStatus()).equals(
					occurrenceStatus)
					&& toNativeIntroduced(d.getEstablishmentMeans()).equals(
					establishmentMeans)) {
				if (!first) {
					features.append(",");
				}
				features.append(d.getLocation().getFeatureId());
				first = false;
			}
		}
		return features.toString();
	}

	/**
	 *
	 * @return the list of measurements or facts
	 */
	public static Term[] measurements() {
		return new Term[] { WCSPTerm.Habitat, WCSPTerm.Lifeform,
				IucnTerm.threatStatus
//				  , EmonocotTerm.SRLI
		};
	}

	/**
	 *
	 * @param taxon
	 *            Set the taxon
	 * @param measurement
	 *            Set the measurement
	 * @return a Content object, or null
	 */
	public static Set<MeasurementOrFact> facts(Taxon taxon,
											   Term measurements) {
		Set<MeasurementOrFact> facts = new HashSet<MeasurementOrFact>();

		for (MeasurementOrFact m : taxon.getMeasurementsOrFacts()) {
			if (m.getMeasurementType().equals(measurements)) {
				facts.add(m);
			}
		}

		return facts;
	}

	public static String modifyToDisplay(String uri) {
		int txtLen=uri.length();
		int lastIndex;
		String finaltext=null;
		while (txtLen > 40 && uri.lastIndexOf('/', 40)>1) {
			lastIndex = uri.lastIndexOf('/', 40);
			String lasttext1=uri.substring(0,(lastIndex+1));
			String lasttext2=uri.substring(lastIndex+1,uri.length());
			txtLen=lasttext2.length();
			uri=lasttext2;
			if(finaltext!=null){finaltext=finaltext+lasttext1+" ";}else{finaltext=lasttext1+" ";}
		}
		if(finaltext!=null){finaltext=finaltext+uri;}else{finaltext=uri;}
		return finaltext;
	};
	
	public static int getRandom(int upperbound) {
		Random rand = new Random();
		return rand.nextInt(upperbound);
	};
}
