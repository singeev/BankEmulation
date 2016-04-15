<%@ include file="/WEB-INF/views/commons/header.jspf" %>
<%@ include file="/WEB-INF/views/commons/navbar.jspf" %>

<%----- Accounts table -----%>
	<div class="container">
		<h3>Accounts list:</h3>
		<table class="table table-striped">
			<thead>
				<th>ID</th>
				<th>Owner's name</th>
				<th>Account's number</th>
				<th>Balance, USD</th>
				<th>Edit Details / Delete account</th>
			</thead>
			<tbody>
				<c:forEach items="${accounts}" var="account">
					<tr>
						<td>${account.id}</td>
						<td>${account.name}</td>
						<td>${account.number}</td>
						<td>${account.balance}</td>
						<td>
							<a type="button" class="btn btn-primary" href="${pageContext.request.contextPath}/update-account?id=${account.id}"><span class="glyphicon glyphicon-pencil"></span> Edit</a>
							<a type="button" class="btn btn-danger" href="${pageContext.request.contextPath}/delete-account?id=${account.id}"><span class="glyphicon glyphicon-trash"></span> Delete</a>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>

<%----- Add new account button -----%>
		<div>
			<a class="btn btn-success" href="${pageContext.request.contextPath}/account">Add new</a>
		</div>
	</div>

<%@ include file="/WEB-INF/views/commons/footer.jspf" %>