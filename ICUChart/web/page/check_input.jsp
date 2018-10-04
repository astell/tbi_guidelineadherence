<%@ page language="java" import="java.util.*,java.sql.*,java.text.Format,java.text.SimpleDateFormat" pageEncoding="ISO-8859-1"%>

<jsp:useBean id='user' class='user.UserBean'  scope='session'/>
<jsp:useBean id='utility' class='security.Utilities'  scope='session'/>

<%
ServletContext context = this.getServletContext();
    
String host = context.getInitParameter("server_name");
String username = context.getInitParameter("username");
String password = context.getInitParameter("password");

Enumeration inputs = request.getParameterNames();
boolean inputError = utility.checkInput(inputs,request, host, username, password);

if(inputError){
%>
<jsp:forward page="/exception/error.jsp"/>
<%
}
%>
    
    






