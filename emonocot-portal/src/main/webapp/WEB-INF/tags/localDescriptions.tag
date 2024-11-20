<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
		  xmlns:c="http://java.sun.com/jsp/jstl/core"
		  xmlns:security="http://www.springframework.org/security/tags"
		  xmlns:spring="http://www.springframework.org/tags"
		  xmlns:em="http://e-monocot.org/portal/functions"
		  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
		  xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
		  xmlns:tags="urn:jsptagdir:/WEB-INF/tags" version="2.0">
	<jsp:directive.attribute name="taxon" type="org.emonocot.model.Taxon" required="true" />
	<c:set var="provenance" value="${em:provenance(taxon)}" />
	<c:set var="bibliography" value="${em:bibliography(taxon)}" />
	<section id="local" class="section-box">
		<div class="inner">
			<div class="box clearfix">
				<div>
					<span class="pull-left" style="color:white;margin-top: 10px;margin-left: 20px">Order descriptions by:</span>
				<ul class="nav nav-tabs" style="background-color:gray;color:white">
					<li class="active">
						<a href="#3" data-toggle="tab" style="color:white">type</a>
					</li>
					<li>
						<a href="#4" data-toggle="tab" style="color:white">source</a>
					</li>
				</ul>
				</div>
				<div class="tab-content">
					<div class="tab-pane active" id="3">
						<c:forEach var="feature" items="${em:features()}">
							<c:set var="descriptions" value="${em:localContent(taxon,feature)}"/>
							<c:if test="${not empty descriptions}">
								<div class="inner">
									<div id="${feature}">
										<spring:message code="${feature}"
														var="featureHeader"/>
										<h2>
											<span>${featureHeader}</span>
											<tags:feedbackLink
													selector="option:contains(${featureHeader})"
													dataName="${featureHeader}"/>
										</h2>
										<c:forEach var="description" items="${descriptions}">
											<c:if test="${description.preferredDescription == false }">
											<div class="description-with-citations">
												<p class="justified">${description.description}</p>
												<li style="list-style: none;  margin: 0px;  float: right; color: #888;">
													<c:set var="provenancekey"
														   value="${em:provenancekey(provenance, description)}"/>
													<c:set var="provenancename"
														   value="${em:provenancename1(provenance, description)}"/>
													<spring:message
															code="citation.provenance"
															arguments="${provenancekey}|${provenancekey}|${provenancekey}|${provenancename}"
															argumentSeparator="|"/>
												</li>
												<ul class="citations">
													<c:if test="${not empty description.references}">
														<spring:message
																code="citation.source"/>

														<c:forEach var="reference"
																   items="${description.references}">
															<li>
																<c:set var="citationKey"
																	   value="${em:citekey(bibliography, reference)}"/>
																<a href="#citation${citationKey}">${citationKey}</a>
															</li>
														</c:forEach>
														<spring:message code="]."/>&#160;
													</c:if>
												</ul>
											</div>
											</c:if>
										</c:forEach>
									</div>
								</div>
							</c:if>
						</c:forEach>
					</div>

					<div class="tab-pane" id="4">
						<c:if test="${not empty taxon.descriptions}">
							<div class="inner">
								<div class="box clearfix">
									<c:forEach var="feature" items="${em:descriptionSources(taxon)}">
										<c:set var="descriptions" value="${em:localContentBySource(taxon,feature)}"/>
										<c:if test="${not empty descriptions}">
											<div class="inner">
												<div id="${feature}">
													<details>
														<summary>
															<b style="display:inline-block;">
																<c:set var="provenancename" value="${em:provenancename(provenance, feature)}"/>
																<!--<a href="${provenancekey}">${provenancename}</a>-->
																<!--<br></br>-->
																<spring:message
																		code="description.provenance"
																		arguments="${provenancekey}|${provenancename}"
																		argumentSeparator="|"/>

															</b>

														</summary>
														<c:forEach var="description" items="${descriptions}">
															<c:if test="${description.preferredDescription == false }">
																<b style="display:inline-block;">
																	<spring:message code="${description.type}"/>
																	<span class="pull-right" style="margin-left: 100px;">
																<tags:feedbackLink selector="option:contains(${description.type})" dataName="${description.type}"/>
																</span>
																</b>
																<div class="description-with-citations">
																	<p class="justified">${description.description}</p>
																</div>
															</c:if>
														</c:forEach>
													</details>
												</div>
											</div>
										</c:if>
									</c:forEach>
								</div>
							</div>
						</c:if>
					</div>
				</div>
			</div>
		</div>
	</section>


</jsp:root>
