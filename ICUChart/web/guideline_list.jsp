<%@ page language="java" import="java.io.*,java.util.*,java.sql.*,patients.*,org.apache.log4j.*" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml">

<%-- HTML headers --%>
<jsp:include page="/page/index_head.jsp" />

<body onload="startTime(15,0);">
    <div id="site_body">

<%-- Security includes here --%>
<jsp:include page="/page/check_credentials.jsp" />
<%--<jsp:include page="/page/check_input.jsp" />--%>

<jsp:useBean id='connect' class='connection.ConnectBean' scope='session'/>
        
<%
ServletContext context = getServletContext();
//Logging configuration
String log4jConfigFile = context.getInitParameter("log4j_property_file");
Logger logger = Logger.getLogger("rootLogger");
logger.setLevel(Level.DEBUG);
PropertyConfigurator.configure(log4jConfigFile);

String pid = request.getParameter("pid");   
if(pid == null){
    pid = "0";
}    

String dataset = request.getParameter("dataset");   
if(dataset == null){
    dataset = "0";
}    

logger.debug("Into guideline page...");

patients.ListPatients patientList = new patients.ListPatients();  
String guidelineListStr = "";
String guidelineSeriesListStr = "";
String holddownListStr = "";
String eusigEventListStr = "";
String timeWindowStr = "";
String thisPatientEventListStr = "";
if(!pid.equals("0") && !dataset.equals("0")){    
    Connection conn = connect.getConnection();
    guidelineListStr = patientList.getGuidelineList(conn);
    guidelineSeriesListStr = patientList.getGuidelineSeriesList(conn);
    holddownListStr = patientList.getHolddownList(conn);
    eusigEventListStr = patientList.getEusigEventList(conn);
    timeWindowStr = patientList.getTimeWindow(pid);
    //thisPatientEventListStr = patientList.getEventList(pid, conn);    
}

%>

<%-- Title banner --%> 
<jsp:include page="/page/page_nav.jsp" >
    <jsp:param name="embedding_page" value="guidelines"/>
    <jsp:param name="embedding_dataset" value="<%=dataset%>"/>
    <jsp:param name="embedding_pid" value="<%=pid%>"/>
</jsp:include>

    </head>
    <body>
        
        <p>
        <form method="POST" action="physiochart.jsp" id="guideline_inputs">
                        
            <h2 id="section_header">Guideline</h2>
            
            <p>The list below shows the three BTF threshold-monitoring guidelines (BPs, ICP, CPP).</p>
            <p>Please select one to view adherence information for patient '<%=pid%>'.</p>
            
            <%= guidelineListStr %>

            <hr/>
            
            <h2 id="section_header">Physiological/treatment data definitions</h2>
            
            <p>Guideline adherence is measured by characterising physiological pressure events according to the Edinburgh University Secondary Insult Grade (EUSIG),
            and associating clinical management interventions with those events, by overlaying a time window of reaction after an event.</p>
                        
            <p>To view these characteristics, please select a set of definitions - crossing threshold value, hold-down value and time-window length.
            To view the definition of each option, click on the "More info" button next to the option header.</p>
            
            <h4 id="section_header">Data series threshold definition (mmHg)</h4>
            <button type="button" class="btn btn-info btn-sm more-info" data-toggle="modal" data-target="#more_info_modal" data-defn="eusig">More info</button>
            
            <div id='eusig' class='show'>No guideline selected</div>
                        
            <div id='eusig_icp' class='hide'>
            <select name='eusig_id_icp' class='form-control'>
                <option value=''>[Select...]</option>
                <option value='1'>ICPm (10)</option>
                <option value='2'>ICPm (15)</option>
                <option value='3'>ICPm (20)</option>
                <option value='4'>ICPm (25)</option>
                <option value='5'>ICPm (30)</option>
            </select>
            </div>
                
            <div id='eusig_cpp' class='hide'>
            <select name='eusig_id_cpp' class='form-control'>
                <option value=''>[Select...]</option>
                <option value='6'>CPP (50)</option>
                <option value='7'>CPP (60)</option>
                <option value='8'>CPP (70)</option>
            </select>
            </div>
            
            <div id='eusig_bps' class='hide'>
            <select name='eusig_id_bps' class='form-control'>
                <option value=''>[Select...]</option>
                <option value='9'>BPs (100)</option>
                <option value='10'>BPs (110)</option>
            </select>
            </div>
            <br/>
            
            <hr class="sub-divider-line"/><br/>
            
            <h4 id="section_header">Event hold-down definition (mins)</h4>
            
            <button type="button" class="btn btn-info btn-sm more-info" data-toggle="modal" data-target="#more_info_modal" data-defn="holddown">More info</button>
            <%= holddownListStr %>
            <br/>
            
            <hr class="sub-divider-line"/><br/>
            
            <h4 id="section_header">Time window (mins)</h4>
            
            <button type="button" class="btn btn-info btn-sm more-info" data-toggle="modal" data-target="#more_info_modal" data-defn="time_window">More info</button>
            <%= timeWindowStr %>
            
            <hr/>
            
            <h2 id="section_header">Display options</h2>
            
            <p>Please select from the following three display options in order to control which aspect of adherence you wish to view</p>
            
            <p>Click on the "More information" button next to option to read the definition.</p>
            
            <h4 id="section_header">Individual <span id='series_defn_disp'></span> event ID for patient '<%=pid%>' to display on adherence chart</h4>
            
            <button type="button" class="btn btn-info btn-sm more-info" data-toggle="modal" data-target="#more_info_modal" data-defn="event_id">More info</button>
            
            <div id="event_id_list"></div>
            
            <br/>
            <hr class="sub-divider-line"/><br/>
            
            <h4 id="section_header">Adherence aspect to display as box-plot</h4>
            
            <button type="button" class="btn btn-info btn-sm more-info" data-toggle="modal" data-target="#more_info_modal" data-defn="adherence_aspect">More info</button>
            <select name='iqr_ref_line' class='form-control'>
                <option value=''>[Select...]</option>
                <option value='Non-adherence'>Non-adherence</option>
                <option value='Duration'>Duration</option>
                <option value='A'>Duration / Non-adherence</option>
                <option value='B'>Duration * Non-adherence</option>
            </select>
            <br/>
            <hr class="sub-divider-line"/><br/>
            
            <h4 id="section_header">Remove 'default' output from box-plot display</h4>
            
            <button type="button" class="btn btn-info btn-sm more-info" data-toggle="modal" data-target="#more_info_modal" data-defn="remove_default">More info</button>
            <select name='remove_default' class='form-control'>
                <option value=''>[Select...]</option>
                <option value='true'>Yes</option>
                <option value='false'>No</option>
            </select>
            
            <hr/>
            
            <input type="hidden" name="pid" value="<%=pid%>"/>
            <input type="hidden" name="dataset" value="<%=dataset%>"/>
            
            <br/>
            <button type="submit" class="btn btn-primary">Select options</button>
        </form>
        </p>
            
            
<jsp:include page="/page/more_info_modal.jsp" />
            
<jsp:include page="/page/page_foot.jsp" />
</div>

</body>
</html>
