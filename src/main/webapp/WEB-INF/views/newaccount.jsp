<%@ include file="/WEB-INF/views/commons/header.jspf" %>
<%@ include file="/WEB-INF/views/commons/navbar.jspf" %>


<%---- Form to add and update an account ----%>
<div class="container" id="newoffer">
		<h3>Fill in this form to create new account:</h3>
		<sf:form method="post" action="${pageContext.request.contextPath}/docreate" commandName="account">
			<fieldset class="form-group">
				<sf:label path="name">Owner's name: </sf:label> 
				<sf:input path="name" type="text" class="form-control" placeholder="Enter owner's name..."/>
				<sf:errors path="name" cssClass="error"></sf:errors></br>
				<sf:label path="number" style="margin-top: 10px;">Account's number: </sf:label>
				<sf:input path="number" type="text" class="form-control" placeholder="Enter account's number..."/>
				<sf:errors path="number" cssClass="error"></sf:errors>
			</fieldset>
			<button type="submit" class="btn btn-success">Create</button>
			<a class="btn btn-warning" href="${pageContext.request.contextPath}/accounts">Cancel</a>
		</sf:form>
	</div>

<%@ include file="/WEB-INF/views/commons/footer.jspf" %>