<%@ page language="java" 
	import="java.util.*,java.text.Format,java.text.SimpleDateFormat"
	pageEncoding="ISO-8859-1"%>

<div id="page-foot">
	<hr />

	<div class="page-foot-left">
		<%
			Format formatter = new SimpleDateFormat(
					"EEEE, dd MMM yyyy HH:mm:ss Z");
			Date date = new Date(session.getLastAccessedTime());
			String s = formatter.format(date);
		%>
		<%=s%>
	</div>
        <br/>        

</div>


