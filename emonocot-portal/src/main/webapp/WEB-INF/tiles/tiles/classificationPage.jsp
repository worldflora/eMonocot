<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:form="http://www.springframework.org/tags/form"
	xmlns:spring="http://www.springframework.org/tags" version="2.0">

	<div class="content">
		<div class="page-header">
			<h2>
				<spring:message code="classification" />
			</h2>
		</div>

		<div class="row">
			<div id="classification" class="jstree-default">tree</div>
			<c:url var="webserviceUrl" value="/taxonTree"/>			
			<script type="text/javascript">
				$(function() {
					$("#classification")
							.jstree(
									{
										"json_data" : {
											"ajax" : {
												"url" : function(n) {
													      return n.attr ? "${webserviceUrl}/" + n.attr("id") : "${webserviceUrl}";
												}
											}
										},							
										"plugins" : [ "json_data" ]
									});
				});
			</script>
		</div>
	</div>
</jsp:root>