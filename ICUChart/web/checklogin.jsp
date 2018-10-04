<%@ page language="java" import="java.util.*,java.sql.*,org.apache.log4j.*" pageEncoding="ISO-8859-1"%>

<jsp:useBean id='user' class='user.UserBean'  scope='session'/>
<jsp:useBean id='usercheck' class='security.UserCheck'  scope='session'/>
<jsp:useBean id='connect' class='connection.ConnectBean' scope='session'/>

<%
ServletContext context = getServletContext() ;  
//Logging configuration
String log4jConfigFile = context.getInitParameter("log4j_property_file");
Logger logger = Logger.getLogger("rootLogger");
logger.setLevel(Level.DEBUG);
PropertyConfigurator.configure(log4jConfigFile);

String versionParam = context.getInitParameter("version");
String dbName = "";
String contextStr = "";
String dbParamStr = "";
String contextParamStr ="";
/*if(versionParam.equals("test")){
    dbParamStr = "security_db_name_test";
    contextParamStr = "context_test";
}else{*/
dbParamStr = "db_name_prod";
contextParamStr = "context_prod";    
//}

dbName = context.getInitParameter(dbParamStr);
contextStr = context.getInitParameter(contextParamStr);
//When switching to production for real, then blank this
contextStr = "";

String host = context.getInitParameter("server_name");
String dbUsername = context.getInitParameter("username");
String dbPassword = context.getInitParameter("password");

String emailUsername = request.getParameter("uname");
String password = request.getParameter("pword");

logger.debug("emailUsername is: " + emailUsername);

Connection secConn = connect.getSecConnection();
//Connection conn = connect.getConnection();

/**
 * responseFlag:
 * 
 * 0 = user present, account active, membership up-to-date (OK login)
 * 1 = user not present (i.e. credentials are wrong)
 * 2 = user present, account not active
 * 3 = user present, account active, membership out-of-date
 */
int responseFlag = 1; //Default is the no-credentials option
if(emailUsername != null && password != null){
    responseFlag = usercheck.checkUserDetails(emailUsername,password,dbName,host,dbUsername,dbPassword,secConn);
}

logger.debug("responseFlag after checkUserDetails: " + responseFlag);

String redirectStr = "";

//ADDING WHILST TESTING INFRASTRUCTURE ON ENSAT-HT
//contextStr = "";

if(responseFlag==0){    
    user = usercheck.setUserDetails(emailUsername,password,dbName,session,user,host,dbUsername,dbPassword,secConn);      
    redirectStr = contextStr + "/home.jsp";        
}else{
    String responseFlagStr = "" + responseFlag;
    redirectStr = contextStr + "/index.jsp?incorrectlogin=" + responseFlagStr + "";
}
response.sendRedirect(redirectStr);
%>