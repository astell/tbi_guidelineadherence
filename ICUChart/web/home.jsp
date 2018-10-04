<%@ page language="java" import="java.util.*,java.sql.*,org.apache.log4j.*" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml">

<%-- HTML headers --%>
<jsp:include page="/page/index_head.jsp" />

<body onload="startTime(15,0);">
    <div id="site_body">

<%-- Security includes here --%>
<%--<jsp:include page="/page/check_credentials.jsp" />--%>
<%--<jsp:include page="/page/check_input.jsp" />--%>

<%-- Title banner --%> 
<jsp:include page="/page/page_nav.jsp" >
    <jsp:param name="embedding_page" value="home"/>
</jsp:include>

<%-- Functional Java classes --%>
<jsp:useBean id='user' class='user.UserBean'  scope='session'/>
<jsp:useBean id='connect' class='connection.ConnectBean' scope='session'/>

<%
ServletContext context = getServletContext() ;  
//Logging configuration
String log4jConfigFile = context.getInitParameter("log4j_property_file");
Logger logger = Logger.getLogger("rootLogger");
logger.setLevel(Level.DEBUG);
PropertyConfigurator.configure(log4jConfigFile);

//This is the initial setting up of the connections that will be used from now on in the session
Connection conn = connect.getConnection();
/*Connection secConn = connect.getSecConnection();
Connection paramConn = connect.getParamConnection();
Connection ccConn = connect.getCcConnection();*/

//Guard this setting with the main connection (if not null, assume all are not null and vice versa)
if(conn == null){
    logger.debug("conn is null - setting connection");
    connect.setConnection(context);
}
conn = connect.getConnection();
/*secConn = connect.getSecConnection();
paramConn = connect.getParamConnection();
ccConn = connect.getCcConnection();*/

if(conn == null){
    logger.debug("conn is null...");
}

String username = user.getUsername();
logger.debug("('" + username + "') - home.jsp");

%>

<p>The following datasets provide physiological and treatment/intervention information for cohorts of patients that have been
    admitted to an intensive-care unit (ICU) for traumatic brain injury (TBI).
</p>

<p>This application allows you to view levels of guideline adherence - specifically the threshold-monitoring guidelines of systolic blood pressure (BPs),
    intra-cranial pressure (ICP) and cerebral perfusion pressure (CPP), as recommended by the <a target='_blank' href='http://www.braintrauma.org'>Brain Trauma Foundation</a> (BTF).
</p>

<p>Please click on a data-set to proceed:
        
        <ul>
            <li><a href='patient_list.jsp?dataset=1'>Brain-IT</a> - a cohort of TBI patients, collected from specialist 
            neurological ICUs across Europe from 2005 to 2008</li>
            <li><a href='patient_list.jsp?dataset=4'>ICCA</a> - a set of TBI patients, collected from the QEUH Glasgow neurological ICU in 2017</li>
            <li><a href='patient_list.jsp?dataset=5'>MIMIC III</a> - a set of TBI patients, collected from non-specialist ICUs across the United States from 2007 to 2013</li>            
        </ul>        
</p>
       

<jsp:include page="/page/page_foot.jsp" />
</div>

</body>
</html>
