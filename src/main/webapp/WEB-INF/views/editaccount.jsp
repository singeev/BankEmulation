<%@ include file="/WEB-INF/views/commons/header.jspf" %>
<%@ include file="/WEB-INF/views/commons/navbar.jspf" %>


<%---- Form to add and update an account ----%>
<div class="container" id="newoffer">
		<h3>Edit account's details:</h3>
		<sf:form method="post" action="${pageContext.request.contextPath}/update-account" commandName="account">
			<fieldset class="form-group">
				<sf:label path="name">Owner's name:</sf:label><sf:errors path="name" cssClass="error"></sf:errors> 
				<sf:input path="name" type="text" class="form-control" placeholder="Enter owner's name..."/>
				<sf:label path="number" style="margin-top: 10px;">Account's number</sf:label><sf:errors path="number" cssClass="error"></sf:errors> 
				<sf:input path="number" type="text" class="form-control" placeholder="Enter account's number..."/>
				<sf:input type="hidden" path="id" class="form-control" value="${account.id}" />
			</fieldset>
			<button type="submit" class="btn btn-success">Save</button>
			<a class="btn btn-warning" href="${pageContext.request.contextPath}/accounts">Cancel</a>
		</sf:form>
	</div>

<%@ include file="/WEB-INF/views/commons/footer.jspf" %>