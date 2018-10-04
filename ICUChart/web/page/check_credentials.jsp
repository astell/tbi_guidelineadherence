<%@ page language="java" import="java.util.*,java.sql.*,java.text.Format,java.text.SimpleDateFormat" pageEncoding="ISO-8859-1"%>

<jsp:useBean id='user' class='user.UserBean'  scope='session'/>
<jsp:useBean id='usercheck' class='security.UserCheck'  scope='session'/>
<jsp:useBean id='connect' class='connection.ConnectBean' scope='session'/>

<%
ServletContext context = getServletContext() ;  
String dbName = "";
String dbParamStr = "";
dbParamStr = "db_name_prod";

dbName = context.getInitParameter(dbParamStr);

String host = context.getInitParameter("server_name");
String dbUsername = context.getInitParameter("username");
String dbPassword = context.getInitParameter("password");

String username = user.getUsername();

Connection secConn = connect.getConnection();

//Check user details here and redirect to login page if not there (i.e. just opened page without logging into a session)
int userCount = usercheck.checkUserDetails(username,dbName,host,dbUsername,dbPassword,secConn);

%>

<%
if(userCount!=1){    
%>
<jsp:forward page="/index.jsp"/>
<%
}
%>
