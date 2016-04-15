<%@ include file="/WEB-INF/views/commons/header.jspf" %>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/error.css">
<link rel="stylesheet" href="webjars/bootstrap/3.3.6/css/bootstrap.min.css"	>
</head>
<body>
<!-- Navbar from JSPF -->
<%@ include file="/WEB-INF/views/commons/navbar.jspf" %>
<!--  end of navbar from JSPF -->

<div class="container">
    <div class="row">
        <div class="col-md-12">
            <div class="error-template">
                <h1>
                    Oops!</h1>
                <h2>
                    404 Not Found</h2>
                <div class="error-details">
                    Sorry, an error has occured, Requested page not found!
                </div>
                <div class="error-actions">
                    <a href="${pageContext.request.contextPath}/main/webapp" class="btn btn-primary btn-lg"><span class="glyphicon glyphicon-home"></span>
                        Take Me Home </a><a href="${pageContext.request.contextPath}/main/webapp" class="btn btn-default btn-lg"><span class="glyphicon glyphicon-envelope"></span> Contact Support </a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="webjars/jquery/1.9.1/jquery.min.js"></script>
	<script src="webjars/bootstrap/3.3.6/js/bootstrap.min.js"></script>
</body>
</html>