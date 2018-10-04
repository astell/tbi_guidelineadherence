<%@ page language="java" import="java.util.*,java.sql.*,java.text.Format,java.text.SimpleDateFormat,org.apache.log4j.*" pageEncoding="ISO-8859-1"%>

<%-- Functional Java classes --%>
<jsp:useBean id='user' class='user.UserBean'  scope='session'/>
<jsp:useBean id='connect' class='connection.ConnectBean'  scope='session'/>


<%
String forename = user.getForename();
boolean isSuperUser = user.getIsSuperUser();
/*String searchFilter = request.getParameter("search_filter");
if(searchFilter == null){
    searchFilter = "";
}*/

String username = user.getUsername();

//Logging configuration
ServletContext context = getServletContext() ;  
String log4jConfigFile = context.getInitParameter("log4j_property_file");
Logger logger = Logger.getLogger("rootLogger");
logger.setLevel(Level.DEBUG);
PropertyConfigurator.configure(log4jConfigFile);

logger.debug("username (page_nav.jsp) - " + username);

//Get the calling class here and populate the activeStr as appropriate
String activeStr = "active";
String activeStr1 = "";
String activeStr2 = "";
String activeStr3 = "";
String activeStr4 = "";

String embeddingPage = "";
embeddingPage = request.getParameter("embedding_page");
if(embeddingPage == null){
    embeddingPage = "";
}

if(embeddingPage.equals("home")){
    activeStr1 = activeStr;
}else if(embeddingPage.equals("patients")){
    activeStr2 = activeStr;
}else if(embeddingPage.equals("guidelines")){
    activeStr3 = activeStr;
}else if(embeddingPage.equals("physiochart")){
    activeStr4 = activeStr;
}

logger.debug("embeddingPage: " + embeddingPage);

patients.ListPatients patientList = new patients.ListPatients();  
String embeddingDataset = "";
String embeddingDatasetNum = "";
if(embeddingPage.equals("patients") || embeddingPage.equals("guidelines") || embeddingPage.equals("physiochart")){
    embeddingDatasetNum = request.getParameter("embedding_dataset");
    if(!embeddingDatasetNum.equals("0")){
        Connection conn = connect.getConnection();    
        embeddingDataset = patientList.getDatasetName(conn,embeddingDatasetNum);    
    }
}

String embeddingPid = "";
if(embeddingPage.equals("guidelines") || embeddingPage.equals("physiochart")){
    embeddingPid = request.getParameter("embedding_pid");    
    if(embeddingPid == null){
        embeddingPid = "";
    }
}

logger.debug("embeddingPid: " + embeddingPid);
logger.debug("embeddingDatasetNum: " + embeddingDatasetNum);
logger.debug("embeddingDataset: " + embeddingDataset);

%>		

<table id="title_banner">
<tr>
    <td><div align="left"><img src="./page/icuchart.png" alt="ICU Chart" height="50%" width="50%"/></div></td>
    <td>
        
            <ul>
                <li class="account_mgmt">Welcome,
                <%
                if(isSuperUser){
                %>
                <strong>Anthony (logged in as <%=forename%>)</strong>
                <%
                }else{
                %>
                <b><%=forename%></b>
                <%
                }
                %>
                </li>
                
                <li class="account_mgmt"><div id="session_clock"></div></li>
            
                <%--<li class="account_mgmt_link"><a href="./jsp/admin/account.jsp">Manage account</a></li>--%>
                <li class="account_mgmt_link"><a href="./logout.jsp">Sign Out</a></li>                
            </ul>
        
    </td>

</tr>
</table>	


<table id="nav_banner">
    <tr>
        <td id="nav_banner_left">    
            
			<ul class="nav nav-pills">
				<li class="nav_item <%=activeStr1%>">
					<a class="nav-link" href="./home.jsp">Home</a>
				</li>
                                <%
                                if(embeddingPage.equalsIgnoreCase("patients")){                                    
                                %>
                                <li class="nav_item <%=activeStr2%>">
					<a class="nav-link" href="./patient_list.jsp?dataset=<%=embeddingDatasetNum%>"><%=embeddingDataset%></a>
				</li>		                                
                                <%
                                }else if(embeddingPage.equalsIgnoreCase("guidelines") || embeddingPage.equalsIgnoreCase("physiochart")){
logger.debug("Into second activeStr clause...");
                                %>
                                <li class="nav_item <%=activeStr2%>">
					<a class="nav-link" href="./patient_list.jsp?dataset=<%=embeddingDatasetNum%>"><%=embeddingDataset%></a>
				</li>		                                                                
                                
                                <li class="nav_item <%=activeStr3%>">                                        
                                    <a class="nav-link" href="./guideline_list.jsp?dataset=<%=embeddingDatasetNum%>&pid=<%=embeddingPid%>">[<%=embeddingPid%>]</strong></a>
				</li>                                                                        
                                <%
                                }
                                %>                                
                                <%--<li class="nav_item <%=activeStr4%>">
					<a class="nav-link" href="./physiochart.jsp">Adherence Output</a>
				</li>--%>
                                    </ul>			
        </td>
    </tr>    
</table>
		
        

