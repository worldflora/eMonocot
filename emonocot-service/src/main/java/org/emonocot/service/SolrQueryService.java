package org.emonocot.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yash on 12/1/17.
 * A class which can implement raw solr queries.
 * This is not designed with the previous considerations in mind and is done ad-hoc.
 */
@Service
public class SolrQueryService {

    private Logger logger = LoggerFactory.getLogger(SolrQueryService.class);

    private HttpSolrServer solrServer;

    @Autowired
    public void setSolrServer(HttpSolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public long getResultsCount(String queryString) {
        SolrQuery solrQuery = new SolrQuery(queryString);
        QueryResponse response = null;
        try {
            response = solrServer.query(solrQuery);
        } catch (SolrServerException e) {
            logger.error("Unable to process the Solr search query: " + queryString);
            logger.error(String.valueOf(e.getStackTrace()));
        }
        SolrDocumentList results = response.getResults();
        return results.getNumFound();
    }

    public long getResultsCount(String queryString, String filterQueryString) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(queryString);
        solrQuery.setFilterQueries(filterQueryString);
        QueryResponse response = null;
        try {
            response = solrServer.query(solrQuery);
        } catch (SolrServerException e) {
            logger.error("Unable to process the Solr search query: " + queryString);
            logger.error(String.valueOf(e.getStackTrace()));
        }
        SolrDocumentList results = response.getResults();
        return results.getNumFound();
    }
}
