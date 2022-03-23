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

import java.util.*;

import org.apache.commons.beanutils.PropertyUtils;
import org.emonocot.model.Base;
import org.emonocot.model.NonOwned;
import org.emonocot.model.Taxon;
import org.emonocot.model.TaxonExcluded;
import org.emonocot.model.hibernate.Fetch;
import org.emonocot.persistence.dao.Dao;
import org.hibernate.*;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author ben
 *
 * @param <T>
 *            the type of object managed by this dao
 */
public abstract class DaoImpl<T extends Base> extends HibernateDaoSupport
implements Dao<T> {

	/**
	 *
	 * @param profile
	 *            Set the name of the profile
	 * @return a list of Fetch instances
	 */
	protected abstract Fetch[] getProfile(String profile);

	/**
	 *
	 * @param criteria
	 *            Set a Criteria instance
	 * @param fetch
	 *            Set the name of the fetch profile
	 * @return true if the criteria have been set, false otherwise
	 */
	protected boolean enableProfilePreQuery(final Criteria criteria,
			final String fetch) {
		boolean setCriteria = false;
		if (fetch != null) {
			for (Fetch f : getProfile(fetch)) {
				if (f.getMode().equals(FetchMode.JOIN)) {
					criteria.setFetchMode(f.getAssociation(), f.getMode());
					setCriteria = true;
				}
			}
		}
		return setCriteria;
	}

	/**
	 *
	 * @param t
	 *            Set a the fetched object
	 * @param fetch
	 *            Set the name of the fetch profile
	 */
	protected void enableProfilePostQuery(final T t, final String fetch) {
		if (fetch != null && t != null) {
			Fetch[] fetchDefs = getProfile(fetch);
			if(fetchDefs == null || fetchDefs.length < 1) {
				return;
			}
			for (Fetch f : fetchDefs) {
				if (f.getMode().equals(FetchMode.SELECT)) {
					String association = f.getAssociation();
					if (association.indexOf(".") == -1) {
						initializeProperty(t, f.getAssociation());
					} else {
						List<String> associations = Arrays.asList(association.split("\\."));
						initializeProperties(t, associations);
					}
				}
			}
		}
	}

	/**
	 *
	 * @param o the object being initialized
	 * @param associations a list of associations being initialized
	 */
	protected void initializeProperties(final Object o, final List<String> associations) {
		List<String> assocs = new ArrayList<String>(associations);
		String association = assocs.remove(0);
		Object associatedObject = initializeProperty(o, association);
		if (associatedObject == null) {
			return;
		} else if (Collection.class.isAssignableFrom(associatedObject.getClass())) {
			for (Object obj : (Collection) associatedObject) {
				if (!assocs.isEmpty()) {
					initializeProperties(obj, assocs);
				}
			}
		} else if (Map.class.isAssignableFrom(associatedObject.getClass())) {
			for (Object obj : ((Map) associatedObject).values()) {
				if (!assocs.isEmpty()) {
					initializeProperties(obj, assocs);
				}
			}
		} else {
			if (!assocs.isEmpty()) {
				initializeProperties(associatedObject, assocs);
			}
		}
	}

	/**
	 *
	 * @param o Set the object
	 * @param association Set the association to initialize
	 */
	protected Object initializeProperty(final Object o, final String association) {
		Object object;
		try {
			object = PropertyUtils.getProperty(o, association);
		} catch (Exception e) {
			logger.debug("Cannot get proxy " + association + " for class " + o.getClass());
			return null;
		}
		if (object == null) {
			return null;
		} else if (object instanceof HibernateProxy) {
			((HibernateProxy) object).getHibernateLazyInitializer().initialize();
			LazyInitializer lazyInitializer = ((HibernateProxy) object)
					.getHibernateLazyInitializer();
			return lazyInitializer.getImplementation();
		} else if (object instanceof PersistentCollection) {
			((PersistentCollection) object).forceInitialization();
			return object;
		} else {
			return object;
		}
	}

	/**
	 *
	 */
	protected Class<T> type;

	/**
	 *
	 * @param newType
	 *            Set the type of object handled by this DAO
	 */
	public DaoImpl(final Class<T> newType) {
		this.type = newType;
	}

	/**
	 *
	 * @param sessionFactory
	 *            Set the session factory
	 */
	@Autowired
	public final void setHibernateSessionFactory(
			final SessionFactory sessionFactory) {
		this.setSessionFactory(sessionFactory);
	}

	/**
	 * @param identifier
	 *            Set the identifier
	 */
	public final void deleteById(final Long id) {
		T t = (T)getSession().load(type, id);
		getSession().delete(t);
	}

	/**
	 * @param identifier
	 *            Set the identifier
	 */
	public final void delete(final String identifier) {
		T t = load(identifier);
		getSession().delete(t);
	}

	/**
	 * @param id the primary key
	 * @return the loaded object
	 */
	public final T load(final Long id) {
		return load(id, null);
	}

	/**
	 * @param identifier
	 *            set the identifier
	 * @return the loaded object
	 */
	public final T load(final String identifier) {
		return load(identifier, null);
	}

	/**
	 * @param id
	 *            Set the primary key
	 * @return the object, or null if the object cannot be found
	 */
	public final T find(final Long id) {
		return find(id, null);
	}

	/**
	 * @param identifier
	 *            Set the identifier
	 * @return the object, or null if the object cannot be found
	 */
	public final T find(final String identifier) {
		return find(identifier, null);
	}

	/**
	 * @param id
	 *            Set the id
	 * @param fetch
	 *            Set the fetch profile (can be null)
	 * @return the loaded object
	 */
	public T load(final Long id, final String fetch) {

		Criteria criteria = getSession().createCriteria(type).add(
				Restrictions.idEq(id));

		enableProfilePreQuery(criteria, fetch);

		T t = (T) criteria.uniqueResult();

		if (t == null) {
			throw new HibernateObjectRetrievalFailureException(
					new UnresolvableObjectException(id,
							"Object could not be resolved"));
		}
		enableProfilePostQuery(t, fetch);
		return t;
	}

	/**
	 * @param identifier
	 *            Set the identifier
	 * @param fetch
	 *            Set the fetch profile (can be null)
	 * @return the loaded object
	 */
	public T load(final String identifier, final String fetch) {

		Criteria criteria = getSession().createCriteria(type).add(
				Restrictions.eq("identifier", identifier));

		enableProfilePreQuery(criteria, fetch);

		T t = (T) criteria.uniqueResult();

/*		if (t == null) {
			throw new HibernateObjectRetrievalFailureException(
					new UnresolvableObjectException(identifier,
							"Object could not be resolved"));
		}*/
		enableProfilePostQuery(t, fetch);
		return t;
	}

	public TaxonExcluded loadExcludedName(final String identifier) {
		//String scientificName = null;

		TaxonExcluded taxonExcluded = new TaxonExcluded();
		//search in excluded names list
		if (identifier!= null) {
			String hql = "select t from TaxonExcluded t where t.identifier = :identifier";
			Query query = getSession().createQuery(hql);
			query.setParameter("identifier", identifier);

			List<TaxonExcluded> list = (List<TaxonExcluded>)query.list();
			Iterator itr = list.iterator();

			while (itr.hasNext()) {
				taxonExcluded = (TaxonExcluded)itr.next();
			}
		}
		return taxonExcluded;
	}


	/**
	 * @param identifier
	 *            Set the identifer
	 * @param fetch
	 *            Set the fetch profile
	 * @return the object or null if it cannot be found
	 */
	public T find(final String identifier, final String fetch) {
		Criteria criteria = getSession().createCriteria(type).add(
				Restrictions.eq("identifier", identifier));
		enableProfilePreQuery(criteria, fetch);
		T t = (T) criteria.uniqueResult();
		enableProfilePostQuery(t, fetch);

		return t;
	}

	/**
	 * @param id
	 *            Set the id
	 * @param fetch
	 *            Set the fetch profile
	 * @return the object or null if it cannot be found
	 */
	public T find(final Long id, final String fetch) {
		Criteria criteria = getSession().createCriteria(type).add(
				Restrictions.idEq(id));
		enableProfilePreQuery(criteria, fetch);
		T t = (T) criteria.uniqueResult();
		enableProfilePostQuery(t, fetch);

		return t;
	}

	/**
	 *
	 * @param t
	 *            The object to save.
	 * @return the saved object
	 */
	public final T save(final T t) {
		getSession().save(t);
		return t;
	}

	/**
	 *
	 * @param t
	 *            The object to save.
	 */
	public final void saveOrUpdate(final T t) {
		getSession().saveOrUpdate(t);
	}

	/**
	 *
	 * @param t
	 *            The object to update.
	 */
	public final void update(final T t) {
		getSession().update(t);
	}

	/**
	 *
	 * @param t
	 *            The object to merge.
	 * @return the merged object
	 */
	public final T merge(final T t) {
		return (T) getSession().merge(t);
	}

	/**
	 * @return the total number of objects
	 */
	public final Long count() {
		Criteria criteria = getSession().createCriteria(type);
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult();
	}

	/**
	 * @param page Set the offset (in size chunks, 0-based), optional
	 * @param size Set the page size
	 * @param fetch Set the fetch profile to which relations are fetched
	 * @return A list of results
	 */
	public final List<T> list(final Integer page, final Integer size, final String fetch) {
		Criteria criteria = getSession().createCriteria(type);

		if (size != null) {
			criteria.setMaxResults(size);
			if (page != null) {
				criteria.setFirstResult(page * size);
			}
		}
		return (List<T>) criteria.list();
	}
}
