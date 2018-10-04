<%@ page language="java" import="java.util.*,java.sql.*,java.text.Format,java.text.SimpleDateFormat,org.apache.log4j.*,org.apache.commons.dbcp2.BasicDataSource" pageEncoding="ISO-8859-1"%>

<jsp:useBean id='user' class='user.UserBean'  scope='session'/>
<jsp:useBean id='connect' class='connection.ConnectBean'  scope='session'/>

<%
    ServletContext context = this.getServletContext();
    //Logging configuration
    String log4jConfigFile = context.getInitParameter("log4j_property_file");
    Logger logger = Logger.getLogger("rootLogger");
    logger.setLevel(Level.DEBUG);
    PropertyConfigurator.configure(log4jConfigFile);

    String username = user.getUsername();
    
    String domain = user.getDomain();

    //Clear the UserBean
    user.setUsername("");
    user.setForename("");
    user.setSurname("");
    user.setRole("");
    user.setCountry("");
    user.setCenter("");
    user.setSessionLogin("");
    user.setSearchFilter("");
    user.setDomain("");

    //Destroy the current connections and data-sources
    Connection connection = connect.getConnection();
    
    if(connection != null){
        connection.close();
    }
    
    logger.debug(" === User '" + username + "' logged out through sessionexpired.jsp === ");
    
    session.invalidate();

%>

<!-- Forward back to login page -->
<jsp:forward page="/index.jsp?sessionexpired=1"/>
