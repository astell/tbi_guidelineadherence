<%@ page language="java" import="java.io.*,java.util.*,patients.*,java.sql.*,org.apache.log4j.*" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml">

<%-- HTML headers --%>
<jsp:include page="/page/index_head.jsp" />

<body onload="startTime(15,0);">
    <div id="site_body">

<%-- Security includes here --%>
<jsp:include page="/page/check_credentials.jsp" />
<jsp:include page="/page/check_input.jsp" />

<jsp:useBean id='connect' class='connection.ConnectBean' scope='session'/>
       
<%
ServletContext context = getServletContext();
//Logging configuration
String log4jConfigFile = context.getInitParameter("log4j_property_file");
Logger logger = Logger.getLogger("rootLogger");
logger.setLevel(Level.DEBUG);
PropertyConfigurator.configure(log4jConfigFile);

String dataset = request.getParameter("dataset");   
if(dataset == null){
    dataset = "0";
}    

patients.ListPatients patientList = new patients.ListPatients();  
String datasetName = "Not selected";
String patientListStr = "";
if(!dataset.equals("0")){
    Connection conn = connect.getConnection();
    logger.debug("Into dataset clause...");
    datasetName = patientList.getDatasetName(conn,dataset);
    patientListStr = patientList.getPatientList(dataset,conn);
}

%>

<%-- Title banner --%> 
<jsp:include page="/page/page_nav.jsp" >
    <jsp:param name="embedding_page" value="patients"/>
    <jsp:param name="embedding_dataset" value="<%=dataset%>"/>
</jsp:include>

    </head>
    <body>
        <h2 id="section_header">Patient list - <%=datasetName%></h2>
        
        <p>The following drop-down list shows the unique identifiers for each patient in this cohort.
            Please select an identifier to view physiological, treatment and guideline adherence information about that patient.</p>
        
        <%--<p class='explanatory_text'>Please select which patient from this dataset you would like to measure the guideline adherence of</p>--%>
        
        <p>
        <form method="POST" action="guideline_list.jsp">
            <%= patientListStr %>
            <input type="hidden" name="dataset" value="<%=dataset%>"/>
            
            <br/>
            <button type="submit" class="btn btn-primary">Select patient</button>
        </form>
        </p>
        
<jsp:include page="/page/page_foot.jsp" />
</div>

</body>
</html>
