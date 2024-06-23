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

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.SolrServerException;
import org.emonocot.api.PhylogeneticTreeService;
import org.emonocot.api.ResourceService;
import org.emonocot.api.SearchableObjectService;
import org.emonocot.api.UserService;
import org.emonocot.api.job.DarwinCorePropertyMap;
import org.emonocot.api.job.JobExecutionException;
import org.emonocot.api.job.JobExecutionInfo;
import org.emonocot.model.PhylogeneticTree;
import org.emonocot.model.SearchableObject;
import org.emonocot.model.auth.Permission;
import org.emonocot.model.auth.User;
import org.emonocot.model.constants.ResourceType;
import org.emonocot.model.registry.Resource;
import org.emonocot.pager.Page;
import org.emonocot.portal.format.annotation.FacetRequestFormat;
import org.emonocot.service.DownloadService;
import org.forester.io.parsers.PhylogenyParser;
import org.forester.io.parsers.phyloxml.PhyloXmlParser;
import org.forester.io.writers.PhylogenyWriter;
import org.forester.io.writers.PhylogenyWriter.FORMAT;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyNode.NH_CONVERSION_SUPPORT_VALUE_STYLE;
import org.gbif.dwc.terms.DwcTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author ben
 *
 */
@Controller
@RequestMapping(value = "/download")
public class DownloadController {

	private static Logger queryLog = LoggerFactory.getLogger("query");

	private static Logger logger = LoggerFactory.getLogger(DownloadController.class);

	private SearchableObjectService searchableObjectService;

	private ResourceService resourceService;

	private UserService userService;

	private JobExplorer jobExplorer;

	private MessageSource messageSource;

	private DownloadService downloadService;

	private PhylogeneticTreeService phylogeneticTreeService;

	@Autowired
	public void setSearchableObjectService(SearchableObjectService searchableObjectService) {
		this.searchableObjectService = searchableObjectService;
	}

	@Autowired
	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Autowired
	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Autowired
	public void setDownloadService(DownloadService downloadService) {
		this.downloadService = downloadService;
	}

	@Autowired
	public void setPhylogeneticTreeService(PhylogeneticTreeService phylogeneticTreeService) {
		this.phylogeneticTreeService = phylogeneticTreeService;
	}
	/**
	 *
	 * @param query
	 *            Set the query
	 * @param limit
	 *            Limit the number of returned results
	 * @param start
	 *            Set the offset
	 * @param facets
	 *            The facets to set
	 * @param view Set the view
	 * @param model Set the model
	 *
	 * @return a model and view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String search(
			@RequestParam(value = "query", required = false) String query,
			@RequestParam(value = "facet", required = false) @FacetRequestFormat List<FacetRequest> facets,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "x1", required = false) Double x1,
			@RequestParam(value = "y1", required = false) Double y1,
			@RequestParam(value = "x2", required = false) Double x2,
			@RequestParam(value = "y2", required = false) Double y2,
			Principal principal,
			Model model) throws SolrServerException {

		User requestingUser = userService.load(principal.getName());

		Map<String, String> selectedFacets = null;
		if (facets != null && !facets.isEmpty()) {
			selectedFacets = new HashMap<String, String>();
			for (FacetRequest facetRequest : facets) {
				selectedFacets.put(facetRequest.getFacet(),
						facetRequest.getSelected());
			}
		}

		String spatial = null;
		DecimalFormat decimalFormat = new DecimalFormat("###0.0");
		if (x1 != null && y1 != null && x2 != null && y2 != null && (x1 != 0.0 && y1 != 0.0 && x2 != 0.0 && x2 != 0.0 && y2 != 0.0)) {
			model.addAttribute("x1",x1);
			model.addAttribute("y1",y1);
			model.addAttribute("x2",x2);
			model.addAttribute("y2",y2);
			spatial = "{!join to=taxon.distribution_ss from=location.tdwg_code_s}geo:\"Intersects(" + decimalFormat.format(x1) + " " + decimalFormat.format(y1) + " " + decimalFormat.format(x2) + " " + decimalFormat.format(y2) + ")\"";
		}

		//Run the search
		Page<? extends SearchableObject> result = searchableObjectService.search(query, spatial, 10, 0, null, null, selectedFacets, sort, null);

		result.setSort(sort);
		result.putParam("query", query);
		model.addAttribute("taxonTerms", DarwinCorePropertyMap.getConceptTerms(DwcTerm.Taxon));
		model.addAttribute("taxonMap", DarwinCorePropertyMap.getPropertyMap(DwcTerm.Taxon));
		model.addAttribute("result", result);
		if(result.getSize() > downloadService.getDownloadLimit() && !requestingUser.getAuthorities().contains(Permission.PERMISSION_ADMINISTRATE)) {
			String[] codes = new String[] { "download.truncated" };
			Object[] args = new Object[] { result.getSize(), downloadService.getDownloadLimit() };
			DefaultMessageSourceResolvable message = new DefaultMessageSourceResolvable(
					codes, args);
			model.addAttribute("warn",message);
		}

		return "download/download";
	}

	@RequestMapping(method = RequestMethod.POST, produces = "text/html", params = "download")
	public String download(
			@RequestParam(value = "query", required = false) String query,
			@RequestParam(value = "facet", required = false) @FacetRequestFormat List<FacetRequest> facets,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "x1", required = false) Double x1,
			@RequestParam(value = "y1", required = false) Double y1,
			@RequestParam(value = "x2", required = false) Double x2,
			@RequestParam(value = "y2", required = false) Double y2,
			@RequestParam(value = "purpose", required = false) String purpose,
			Model model,
			HttpServletRequest request,
			@RequestParam(value="downloadFormat", required = true) String downloadFormat,
			@RequestParam(value = "archiveOptions", required = false) List<String> archiveOptions,
			RedirectAttributes redirectAttributes,
			Principal principal) throws SolrServerException {

		User user = userService.load(principal.getName());

		Map<String, String> selectedFacets = null;
		if (facets != null && !facets.isEmpty()) {
			selectedFacets = new HashMap<String, String>();
			for (FacetRequest facetRequest : facets) {
				selectedFacets.put(facetRequest.getFacet(),
						facetRequest.getSelected());
			}
		}

		String spatial = null;
		DecimalFormat decimalFormat = new DecimalFormat("###0.0");
		if (x1 != null && y1 != null && x2 != null && y2 != null && (x1 != 0.0 && y1 != 0.0 && x2 != 0.0 && x2 != 0.0 && y2 != 0.0)) {
			spatial = "{!join to=taxon.distribution_ss from=location.tdwg_code_s}geo:\"Intersects(" + decimalFormat.format(x1) + " " + decimalFormat.format(y1) + " " + decimalFormat.format(x2) + " " + decimalFormat.format(y2) + ")\"";
		}

		if(archiveOptions == null) {
			archiveOptions = new ArrayList<String>();
		}
		Page<? extends SearchableObject> result = searchableObjectService.search(query, spatial, 10, 0, null, null, selectedFacets, sort, null);

		Resource resource = new Resource();
		resource.setTitle("download" + Long.toString(System.currentTimeMillis()));

		//Save the 'resource'
		try {
			StringBuffer downloadFileName = new StringBuffer(UUID.randomUUID().toString()); // Download file - either the file or the directory
			resource.setResourceType(ResourceType.DOWNLOAD);
			resource.setStartTime(null);
			resource.setDuration(null);
			resource.setExitCode(null);
			resource.setExitDescription(null);
			resource.setJobId(null);
			resource.setStatus(BatchStatus.UNKNOWN);
			resource.setRecordsRead(0);
			resource.setReadSkip(0);
			resource.setProcessSkip(0);
			resource.setWriteSkip(0);
			resource.setWritten(0);
			switch (downloadFormat) {
			case "taxon":
				downloadFileName.append(".txt");
				break;
			case "alphabeticalChecklist":
			case "hierarchicalChecklist":
				downloadFileName.append(".pdf");
				break;
			default://archive
				downloadFileName.append(".zip");
				break;
			}
			resource.getParameters().put("download.file", downloadFileName.toString());
			resource.getParameters().put("download.query", query);
			resourceService.save(resource);

			//Launch the job
			downloadService.requestDownload(query, selectedFacets, sort, spatial, result.getSize(),
					purpose, downloadFormat, archiveOptions, resource, user);

			String[] codes = new String[] { "download.submitted" };
			Object[] args = new Object[] {};
			DefaultMessageSourceResolvable message = new DefaultMessageSourceResolvable(
					codes, args);
			redirectAttributes.addFlashAttribute("info", message);
			queryLog.info("Download: \'{}\', format: {}, purpose: {},"
					+ "facet: [{}], {} results", new Object[] {query,
							downloadFormat, purpose, selectedFacets, result.getSize() });
		} catch (JobExecutionException e) {
			String[] codes = new String[] { "download.failed" };
			Object[] args = new Object[] { e.getMessage() };
			DefaultMessageSourceResolvable message = new DefaultMessageSourceResolvable(
					codes, args);
			redirectAttributes.addFlashAttribute("error", message);
		}
		return "redirect:/download/progress?downloadId=" + resource.getId();
	}

	@RequestMapping(value = "/progress", method = RequestMethod.GET, produces = "text/html")
	public String getProgress(@RequestParam("downloadId") Long downloadId, Model model, Locale locale) {
		Resource resource = resourceService.load(downloadId);
		model.addAttribute("resource", resource);

		String downloadFileName = resource.getParameters().get("download.file");
		if(resource.getBaseUrl() == null) {
			resource.setExitDescription(messageSource.getMessage("download.being.prepared", new Object[] {}, locale));
		} else {
			resource.setExitDescription(messageSource.getMessage("download.will.be.available", new Object[] {messageSource.getMessage("harvester.url", new Object[] {}, locale), downloadFileName}, locale));
		}

		return "download/progress";
	}

	@RequestMapping(value = "/progress", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public JobExecutionInfo getProgress(@RequestParam("downloadId") Long downloadId, Locale locale) throws Exception {
		JobExecutionInfo jobExecutionInfo = new JobExecutionInfo();
		Resource resource = resourceService.load(downloadId);

		JobExecution jobExecution = jobExplorer.getJobExecution(resource.getJobId());
		if(jobExecution != null) {
			Float recordsRead = 0f;
			jobExecutionInfo.setStatus(jobExecution.getStatus());
			if(jobExecution.getExitStatus() != null) {
				ExitStatus exitStatus = jobExecution.getExitStatus();
				jobExecutionInfo.setExitCode(exitStatus.getExitCode());
				jobExecutionInfo.setExitDescription(exitStatus.getExitDescription());


				Integer readSkip = 0;
				Integer processSkip = 0;
				Integer writeSkip = 0;
				Integer written = 0;
				for(StepExecution stepExecution : jobExecution.getStepExecutions()) {
					recordsRead += stepExecution.getReadCount();
					readSkip += stepExecution.getReadSkipCount();
					processSkip += stepExecution.getProcessSkipCount();
					writeSkip += stepExecution.getWriteSkipCount();
					written += stepExecution.getWriteCount();
				}
				jobExecutionInfo.setRecordsRead(recordsRead.intValue());
				jobExecutionInfo.setReadSkip(readSkip);
				jobExecutionInfo.setProcessSkip(processSkip);
				jobExecutionInfo.setWriteSkip(writeSkip);
				jobExecutionInfo.setWritten(written);
			}
			Float total = Float.parseFloat(jobExecution.getJobInstance().getJobParameters().getString("job.total.reads"));

			jobExecutionInfo.setProgress(Math.round((recordsRead/ total) * 100f));
			if(resource.getBaseUrl() == null) {
				jobExecutionInfo.setExitDescription(messageSource.getMessage("download.being.prepared", new Object[] {}, locale));
			} else {
				String downloadFileName = resource.getParameters().get("download.file");
				if(jobExecutionInfo.getExitCode() == null) {
					jobExecutionInfo.setExitDescription(messageSource.getMessage("download.will.be.available", new Object[] {resource.getBaseUrl(), downloadFileName}, locale));
				} else {
					if(jobExecutionInfo.getExitCode().equals("COMPLETED")) {
						jobExecutionInfo.setExitDescription(messageSource.getMessage("download.is.available", new Object[] {messageSource.getMessage("harvester.url", new Object[] {}, locale), downloadFileName}, locale));
					} else if(jobExecutionInfo.getExitCode().equals("FAILED")) {
						jobExecutionInfo.setExitDescription(messageSource.getMessage("download.failed", new Object[] {}, locale));
					} else {
						jobExecutionInfo.setExitDescription(messageSource.getMessage("download.will.be.available", new Object[] {messageSource.getMessage("harvester.url", new Object[] {}, locale), downloadFileName}, locale));
					}
				}
			}

		}
		return jobExecutionInfo;
	}

	@RequestMapping(value = "/{downloadId}", method = RequestMethod.GET, produces = "text/html", params = {"!form"})
	public String show(@PathVariable Long downloadId, Model uiModel) {
		Resource resource = resourceService.load(downloadId,"job-with-source");
//		Resource resource1 = resourceService.load(downloadId);

		uiModel.addAttribute("resource", resource);
		uiModel.addAttribute("query", resource.getParameters().get("download.query"));
		return "download/show";
	}

	@RequestMapping(value = "/phylo", params = {"!format"}, method = RequestMethod.GET, produces = {"text/html", "*/*"})
	public String getDownloadPage(@RequestParam(value = "id", required = true) Long id, Model model) {
		PhylogeneticTree tree = phylogeneticTreeService.load(id);
		model.addAttribute(tree);
		model.addAttribute("formats", Arrays.asList(FORMAT.values()));
		return "phylo/download";
	}

	@RequestMapping(value = "/phylo", params = {"format!=PHYLO_XML"}, method = RequestMethod.POST, produces = "text/plain")
	public @ResponseBody String getDownload(@RequestParam(value = "id", required = true) Long id,
			@RequestParam(value = "format", required = false, defaultValue = "NH") FORMAT format,
			@RequestParam(value = "purpose", required = false) String purpose,
			Model model) throws Exception {
		PhylogeneticTree tree = phylogeneticTreeService.load(id);
		String output = null;
		PhylogenyParser parser = new PhyloXmlParser();
		parser.setSource(new ByteArrayInputStream(tree.getPhylogeny().getBytes()));
		Phylogeny phylogeny = parser.parse()[0];
		PhylogenyWriter phylogenyWriter = PhylogenyWriter.createPhylogenyWriter();
		StringBuffer stringBuffer = null;
		switch (format) {
		case NEXUS:
			stringBuffer = phylogenyWriter.toNexus(phylogeny, NH_CONVERSION_SUPPORT_VALUE_STYLE.IN_SQUARE_BRACKETS);
			output = stringBuffer.toString();
			break;
		case NHX:
			stringBuffer = phylogenyWriter.toNewHampshireX(phylogeny);
			output = stringBuffer.toString();
			break;
		case NH:
		default:
			stringBuffer = phylogenyWriter.toNewHampshire(phylogeny, false, tree.getHasBranchLengths());
			output = stringBuffer.toString();
			break;
		}
		queryLog.info("DownloadPhylogeny: \'{}\', format: {}, purpose: {}", new Object[] {id, format, purpose});
		return output;
	}

	@RequestMapping(value = "/phylo", params = {"format=PHYLO_XML"}, method = RequestMethod.POST, produces = "application/xml")
	public @ResponseBody String getDownloadPhyloXml(@RequestParam(value = "id", required = true) Long id,
			@RequestParam(value = "format", required = false, defaultValue = "NH") FORMAT format,
			@RequestParam(value = "purpose", required = false) String purpose,
			Model model) throws Exception {
		PhylogeneticTree tree = phylogeneticTreeService.load(id);
		queryLog.info("DownloadPhylogeny: \'{}\', format: {}, purpose: {}", new Object[] {id, format, purpose});
		return tree.getPhylogeny();
	}

}
