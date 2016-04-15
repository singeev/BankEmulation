<%@ include file="/WEB-INF/views/commons/header.jspf" %>
<%@ include file="/WEB-INF/views/commons/navbar.jspf" %>

<div class="container">
<!-- Add funds form -->
	<div class="panel panel-primary">
		<div class="panel-heading">Add funds</div>
		<div class="panel-body">
		<sf:form class="form-inline" role="form" method="post" action="${pageContext.request.contextPath}/addfunds" commandName="transaction">
			<fieldset class="form-group">
				<sf:label path="toid">Account's ID:</sf:label><sf:errors path="toid" cssClass="error"></sf:errors> 
				<sf:input path="toid" type="text" class="form-control" placeholder="Account's ID..."/>
				<sf:label path="summ" style="margin-top: 10px;">   Amount:</sf:label> 
				<sf:input path="summ" type="text" class="form-control" placeholder="Amount of money..."/>
			</fieldset>
			<button type="submit" class="btn btn-primary">Add funds</button>
		</sf:form>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg1}</h4></div>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg2}</h4></div>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg3}</h4></div>
		<sf:errors path="summ" cssClass="error"></sf:errors>
		</div>
	</div>
<!-- End of add funds form -->

<!-- Withdraw form -->
	<div class="panel panel-primary">
		<!-- Default panel contents -->
		<div class="panel-heading">Withdraw</div>
		<div class="panel-body">
		<sf:form class="form-inline" role="form" method="post" action="${pageContext.request.contextPath}/withdraw" commandName="transaction">
			<fieldset class="form-group">
				<sf:label path="fromid">Account's ID:</sf:label>
				<sf:input path="fromid" type="text" class="form-control" placeholder="Account's ID..."/>
				<sf:label path="summ" style="margin-top: 10px;">   Amount:</sf:label> 
				<sf:input path="summ" type="text" class="form-control" placeholder="Amount of money..."/>
			</fieldset>
			<button type="submit" class="btn btn-primary">Withdraw</button>
		</sf:form>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg4}</h4></div>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg5}</h4></div>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg6}</h4></div>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg7}</h4></div>
		<div><sf:errors path="fromid" cssClass="error"></sf:errors></div>
		<div><sf:errors path="summ" cssClass="error"></sf:errors></div>
		</div>
	</div>
<!-- End of withdraw form -->

<!-- Transfer form -->
	<div class="panel panel-primary">
		<!-- Default panel contents -->
		<div class="panel-heading">Transfer</div>
		<div class="panel-body">
		<sf:form class="form-inline" role="form" method="post" action="${pageContext.request.contextPath}/transfer" commandName="transaction">
			<fieldset class="form-group">
				<sf:label path="fromid">From Account ID:</sf:label> 
				<sf:input path="fromid" type="text" class="form-control" placeholder="From Account:"/>
				<sf:label path="toid">To Account ID:</sf:label> 
				<sf:input path="toid" type="text" class="form-control" placeholder="Account's ID..."/>
				<sf:label path="summ" style="margin-top: 10px;">Amount:</sf:label> 
				<sf:input path="summ" type="text" class="form-control" placeholder="Amount of money..."/>
			</fieldset>
			<button type="submit" class="btn btn-primary">Transfer</button>
		</sf:form>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg8}</h4></div>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg9}</h4></div>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg10}</h4></div>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg11}</h4></div>
		<div style="margin-top: 5px;margin-bottom: -15;"><h4 style="margin-bottom: 0px;color:#C93F1C;">${errMsg12}</h4></div>
		<sf:errors path="fromid" cssClass="error"></sf:errors>
		<sf:errors path="toid" cssClass="error"></sf:errors>
		<sf:errors path="summ" cssClass="error"></sf:errors>
		</div>
	</div>
<!-- End of transfer form -->
</div>

<%@ include file="/WEB-INF/views/commons/footer.jspf" %>
