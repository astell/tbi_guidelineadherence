<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml">

<%-- HTML headers --%>
<jsp:include page="/page/index_head.jsp" />

<body onload="startTime(15,0);">
    <div id="site_body">

<%-- Security includes here --%>
<jsp:include page="/page/check_credentials.jsp" />

<%-- Title banner --%>
<jsp:include page="/page/page_nav.jsp" />

<%-- Functional Java classes --%>
<jsp:useBean id='user' class='user.UserBean'  scope='session'/>

	<h3>
		An error has occurred, please go back to <a href="./home.jsp">ICU Chart Home</a> and try again.
		If the problem persists please contact the system administrator
		<a href="mailto:a.stell.1@research.gla.ac.uk">Anthony Stell</a>.
	</h3>
		
<jsp:include page="/page/page_foot.jsp" />
</div>

</body>
</html>

