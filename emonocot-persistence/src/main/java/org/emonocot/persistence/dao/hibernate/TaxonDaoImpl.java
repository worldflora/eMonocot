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
package org.emonocot.persistence.dao.hibernate;

import org.emonocot.model.Taxon;
import org.emonocot.model.constants.TaxonomicStatus;
import org.emonocot.model.hibernate.Fetch;
import org.emonocot.pager.DefaultPageImpl;
import org.emonocot.pager.Page;
import org.emonocot.persistence.dao.TaxonDao;
import org.gbif.ecat.voc.Rank;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;

//import org.gbif.ecat.voc.TaxonomicStatus;

//import org.gbif.ecat.voc.TaxonomicStatus;

/**
 * @author ben
 */
@Repository
public class TaxonDaoImpl extends DaoImpl<Taxon> implements TaxonDao {

    /**
     *
     */
    private static Logger logger = LoggerFactory.getLogger(TaxonDaoImpl.class);

    private static Map<String, Fetch[]> FETCH_PROFILES;

    static {
        FETCH_PROFILES = new HashMap<String, Fetch[]>();
        FETCH_PROFILES.put("taxon-with-children", new Fetch[]{new Fetch(
                "childNameUsages", FetchMode.SELECT)});
        FETCH_PROFILES.put("classification-tree", new Fetch[]{
                new Fetch("childNameUsages", FetchMode.SELECT),
                new Fetch("keys", FetchMode.SELECT)});
        FETCH_PROFILES.put("taxon-with-annotations", new Fetch[]{new Fetch(
                "annotations", FetchMode.SELECT)});
        FETCH_PROFILES.put("taxon-with-related", new Fetch[]{
                new Fetch("parentNameUsage", FetchMode.JOIN),
                new Fetch("acceptedNameUsage", FetchMode.JOIN),
                new Fetch("childNameUsages", FetchMode.SELECT),
                new Fetch("synonymNameUsages", FetchMode.SELECT),
                new Fetch("annotations", FetchMode.SELECT)});
        FETCH_PROFILES.put("taxon-page", new Fetch[]{
                new Fetch("parentNameUsage", FetchMode.JOIN),
                new Fetch("parentNameUsage.authority", FetchMode.SELECT),
                new Fetch("higherClassification", FetchMode.SELECT),
                new Fetch("higherClassification.authority", FetchMode.SELECT),
                new Fetch("acceptedNameUsage", FetchMode.JOIN),
                new Fetch("acceptedNameUsage.authority", FetchMode.SELECT),
                new Fetch("childNameUsages", FetchMode.SELECT),
                new Fetch("childNameUsages.authority", FetchMode.SELECT),
                new Fetch("synonymNameUsages", FetchMode.SELECT),
                new Fetch("synonymNameUsages.authority", FetchMode.SELECT),
                new Fetch("distribution", FetchMode.SELECT),
                new Fetch("distribution.authority", FetchMode.SELECT),
                new Fetch("distribution.references", FetchMode.SELECT),
                new Fetch("distribution.references.authority", FetchMode.SELECT),
                new Fetch("descriptions", FetchMode.SELECT),
                new Fetch("descriptions.authority", FetchMode.SELECT),
                new Fetch("descriptions.references", FetchMode.SELECT),
                new Fetch("descriptions.references.authority", FetchMode.SELECT),
                new Fetch("images", FetchMode.SELECT),
                new Fetch("images.authority", FetchMode.SELECT),
                new Fetch("namePublishedIn", FetchMode.JOIN),
                new Fetch("namePublishedIn.authority", FetchMode.SELECT),
                new Fetch("references", FetchMode.SELECT),
                new Fetch("references.authority", FetchMode.SELECT),
                new Fetch("authority", FetchMode.JOIN),
                new Fetch("identifiers", FetchMode.SELECT),
                new Fetch("identifiers.authority", FetchMode.SELECT),
                new Fetch("vernacularNames", FetchMode.SELECT),
                new Fetch("vernacularNames.authority", FetchMode.SELECT),
                new Fetch("measurementsOrFacts", FetchMode.SELECT),
                new Fetch("measurementsOrFacts.authority", FetchMode.SELECT),
                new Fetch("trees", FetchMode.SELECT),
                new Fetch("trees.authority", FetchMode.SELECT),
                new Fetch("phylogenies", FetchMode.SELECT),
                new Fetch("phylogenies.authority", FetchMode.SELECT),
                new Fetch("uri", FetchMode.SELECT),
                new Fetch("typesAndSpecimens", FetchMode.SELECT),
                new Fetch("typesAndSpecimens.authority", FetchMode.SELECT),
                new Fetch("taxonExternalLinks", FetchMode.SELECT),
                new Fetch("comments", FetchMode.SELECT)});
        /* */
        FETCH_PROFILES.put("object-page", FETCH_PROFILES.get("taxon-page"));
        /* */
        FETCH_PROFILES.put("taxon-with-image", new Fetch[]{new Fetch("image",
                FetchMode.SELECT)});
        FETCH_PROFILES.put("taxon-with-content", new Fetch[]{
                new Fetch("descriptions", FetchMode.SELECT),
                new Fetch("sources", FetchMode.SELECT)});
        FETCH_PROFILES.put("taxon-ws", new Fetch[]{
                new Fetch("parentNameUsage", FetchMode.JOIN),
                new Fetch("acceptedNameUsage", FetchMode.JOIN),
                new Fetch("childNameUsages", FetchMode.SELECT),
                new Fetch("synonymNameUsages", FetchMode.SELECT),
                /**
                 *  ISSUE http://build.e-monocot.org/bugzilla/show_bug.cgi?id=180
                 *
                new Fetch("distribution", FetchMode.SELECT),*/
                new Fetch("namePublishedIn", FetchMode.JOIN)});
    }

    /**
     * The rank held by the the root(s) of the taxonomic classification.
     */
    private Rank rootRank;

    /**
     * @param rank Set the root rank
     */
    public final void setRootRank(final String rank) {
        this.rootRank = Rank.valueOf(rank);
    }

    /**
     *
     */
    public TaxonDaoImpl() {
        super(Taxon.class);
    }

    @Override
    protected final Fetch[] getProfile(final String profile) {
        return TaxonDaoImpl.FETCH_PROFILES.get(profile);
    }

    /**
     * @param t     Set the taxon
     * @param fetch Set the fetch profile
     */
    @Override
    public final void enableProfilePostQuery(final Taxon t, final String fetch) {
        if (fetch != null && t != null) {
            for (Fetch f : getProfile(fetch)) {
                if (f.getMode().equals(FetchMode.SELECT)) {
                    String association = f.getAssociation();
                    if (association.indexOf(".") == -1) {
                        initializeProperty(t, f.getAssociation());
                    } else {
                        List<String> associations = Arrays.asList(association
                                .split("\\."));
                        initializeProperties(t, associations);
                    }
                }
            }
        }
    }

    /**
     * Returns the child taxa of a given taxon.
     *
     * @param identifier set the identifier
     * @param pageSize   The maximum number of results to return
     * @param pageNumber The offset (in pageSize chunks, 0-based) from the beginning of
     *                   the recordset
     * @param fetch      Set the fetch profile
     * @return a Page from the resultset
     */
    public final List<Taxon> loadChildren(final String identifier,
                                          final Integer pageSize, final Integer pageNumber, final String fetch) {
        Criteria criteria = getSession().createCriteria(Taxon.class);
        if (identifier == null) {
            // return the root taxa
////			criteria.add(Restrictions.isNull("parentNameUsage"));
            criteria.add(Restrictions.isNotNull("scientificName"));
////		criteria.add(Restrictions.eq("taxonomicStatus", TaxonomicStatus.Accepted));
            criteria.add(Restrictions.isNotNull("parentNameUsage"));
            criteria.add(Restrictions.ne("taxonomicStatus", TaxonomicStatus.Synonym));
            if (rootRank != null) {
                criteria.add(Restrictions.eq("taxonRank", rootRank));
            }
        } else {
            criteria.createAlias("parentNameUsage", "p");
            criteria.add(Restrictions.eq("p.identifier", identifier));
        }

        if (pageSize != null) {
            criteria.setMaxResults(pageSize);
            if (pageNumber != null) {
                criteria.setFirstResult(pageSize * pageNumber);
            }
        }
        criteria.addOrder(Order.asc("scientificName"));
        enableProfilePreQuery(criteria, fetch);
        List<Taxon> results = (List<Taxon>) criteria.list();

        for (Taxon t : results) {
            enableProfilePostQuery(t, fetch);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see org.emonocot.persistence.dao.SearchableDao#searchByExample(org.emonocot.model.Base, boolean, boolean)
     */
    @Override
    public Page<Taxon> searchByExample(Taxon example, boolean ignoreCase,
                                       boolean useLike) {
        Example criterion = Example.create(example);
        if (ignoreCase) {
            criterion.ignoreCase();
        }
        if (useLike) {
            criterion.enableLike();
        }
        Session session = getSession();
        Criteria criteria = session.createCriteria(Taxon.class);
        criteria.add(criterion);

        if (example.getNamePublishedIn() != null) {
            Example criterion2 = Example.create(example.getNamePublishedIn());
            if (ignoreCase) {
                criterion2.ignoreCase();
            }
            if (useLike) {
                criterion2.enableLike();
            }
            criteria.createCriteria("namePublishedIn").add(criterion2);
        }

        if (example.getNameAccordingTo() != null) {
            Example criterion3 = Example.create(example.getNameAccordingTo());
            if (ignoreCase) {
                criterion3.ignoreCase();
            }
            if (useLike) {
                criterion3.enableLike();
            }
            criteria.createCriteria("nameAccordingTo").add(criterion3);
        }

        List<Taxon> results = (List<Taxon>) criteria.list();
        logger.debug("List of taxa:" + results.size());
        Page<Taxon> page = new DefaultPageImpl<Taxon>(results.size(), null, null, results, null);
        return page;
    }

/*    @Override
    public List<Taxon> findByScientificNameID(final String scientificNameID) {
        Criteria criteria = getSession()
                .createCriteria(type)
                .add(Restrictions.eq("scientificNameID", scientificNameID));
        return (Taxon) criteria.uniqueResult();
    }*/

    @Override
    public Page<Taxon> findByScientificNameID(final String scientificNameID) {
        String ipniId = scientificNameID;
        if (!scientificNameID.contains("urn:lsid:ipni.org:names:")) {
            ipniId = "urn:lsid:ipni.org:names:"+scientificNameID;
        }

        Criteria criteria = getSession()
                .createCriteria(type)
                .add(Restrictions.eq("scientificNameID", ipniId));
        List<Taxon> results = (List<Taxon>) criteria.list();
        logger.debug("List of taxa:" + results.size());
        Page<Taxon> page = new DefaultPageImpl<Taxon>(results.size(), null, null, results, null);
        return page;
    }

    @Override
    public Taxon findByTplID(String tplID) {
        int tplIdLength = tplID.length();
        if (tplIdLength == 1) {
            tplID = "/" + tplID + "/";//majorGroup
        }

        Taxon taxonId = new Taxon();

        String hql = "select t.taxon from TaxonExternalLinks t where t.tplID like :tplID";
        Query query = getSession().createQuery(hql);
        query.setParameter("tplID", "%" + tplID);
        List list = query.list();
        Iterator itr = list.iterator();

        while (itr.hasNext()) {
            taxonId = (Taxon) itr.next();
        }
        return taxonId;
    }


    @Override
    public Taxon findByTplID(String majorGroup, String tplFamily, String tplGenus) {
        Taxon taxonId = new Taxon();
        String tplID = "/" + majorGroup + "/" + tplFamily + "/" + tplGenus + "/";
        String hql = "select t.taxon from TaxonExternalLinks t where t.tplID like :tplID";
        Query query = getSession().createQuery(hql);
        query.setParameter("tplID", "%" + tplID);
        List list = query.list();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            taxonId = (Taxon) itr.next();
        }
        return taxonId;
    }

    @Override
    public Taxon findByTplID(String majorGroup, String tplFamily) {
        Taxon taxonId = new Taxon();
        String tplID = "/" + majorGroup + "/" + tplFamily + "/";
        String hql = "select t.taxon from TaxonExternalLinks t where t.tplID like :tplID";
        Query query = getSession().createQuery(hql);
        query.setParameter("tplID", "%" + tplID);

        List list = query.list();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            taxonId = (Taxon) itr.next();
        }
        return taxonId;
    }







/*    @Override
    public Taxon findByTplID(String tplID) {
        Taxon taxonId = new Taxon();
        String hql = "select t.taxon from TaxonExternalLinks t where t.tplID=:tplID";
        Query query = getSession().createQuery(hql);
        query.setParameter("tplID", tplID);
        List list = query.list();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            taxonId = (Taxon) itr.next();
        }
        return taxonId;
    }*/
}
