<%@ page language="java" import="java.io.*,java.util.*,patients.*,java.sql.*,java.text.*,org.apache.log4j.*,java.text.SimpleDateFormat" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml">

<%-- HTML headers --%>
<jsp:include page="/page/index_head.jsp" />

<body onload="startTime(15,0);">
    <div id="site_body">

<%-- Security includes here --%>
<jsp:include page="/page/check_credentials.jsp" />
<%--<jsp:include page="/page/check_input.jsp" />--%>

<jsp:useBean id='connect' class='connection.ConnectBean'  scope='session'/>
<jsp:useBean id='brainit_connect' class='patients.Connect'  scope='session'/>

<%
    
ServletContext context = getServletContext() ;  

//Initialise the logger
String log4jConfigFile = context.getInitParameter("log4j_property_file");
Logger logger = Logger.getLogger("rootLogger");
logger.setLevel(Level.DEBUG);
PropertyConfigurator.configure(log4jConfigFile);

logger.debug("--- START OF PHYSIOCHART OUTPUT ---");

//== SET THE VARIOUS HANDLES USED BY THIS ENGINE ==
logger.debug("Setting handles...");
//This is a handle to either use the process model comparison object or the VxV approach
boolean processModelRep = true;
//Generated representative treatment data (versus the real Brain-IT annotations)
boolean dummy = false;
//Remove the default state for each patient
boolean removeDefault = request.getParameter("remove_default").equals("true");
//Switch between individual and group charts (will later turn this into a multi-page bit of the website)
boolean individualCharts = false;
//This a handle to control if the regression file is output
boolean generateRegressionFile = false;
//This is a handle to show the overall event/treatment assocation figures
boolean showEventTreatmentAssoc = false;
//These handles compile the data for showing on the aggregate display
boolean compileScatterPlots = true;
boolean aggregateEusig = true;
boolean aggregateAge = false;
boolean aggregateMassLesion = false;
boolean aggregateWeightings = false;
//== END OF HANDLE SETTING ==

//== INITIALISE ALL THE VARIABLES REQUIRED ==
logger.debug("Initialising variables...");

//Initialise the class instances required
SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
GraphUtils graphUtil = new GraphUtils();
GuidelineCalc gCalc = new GuidelineCalc();
patients.ListPatients patientList = new patients.ListPatients();  
Vector<String> patientIds = new Vector<String>();
String filepath = "/home/ubuntu/regression/";

//Set the dataset ID variable
String datasetId = request.getParameter("dataset");

//String switch to indicate which line of the IQR table will be rendered in a boxplot
String iqrRefLine = request.getParameter("iqr_ref_line");

//Define the variables for the database connections (local)
String driverName = context.getInitParameter("driver_name");
String serverName = context.getInitParameter("server_name");
String port = context.getInitParameter("port");
String username = context.getInitParameter("username");
String password = context.getInitParameter("password");

//Retrieve the local Brain-IT connection string (Brain-IT for contextual information, treatment_profile for main analysis)
Connection brainItConn = brainit_connect.connect("brainit", driverName, serverName, port, username, password);
Connection conn = connect.getConnection();

//Set the dataset name now the database connection has been set
String datasetStr = patientList.getDatasetName(conn, datasetId);

//Set the time-window size (in minutes and milliseconds)
String timeWindowMinsStr = request.getParameter("time_window");
if(timeWindowMinsStr == null || timeWindowMinsStr.equals("")){
    timeWindowMinsStr = "15";
}
int TIME_WINDOW_MINS = Integer.parseInt(timeWindowMinsStr);
long TIME_WINDOW_MILLIS = (TIME_WINDOW_MINS * 60000);
graphUtil.setTimeWindow(TIME_WINDOW_MINS);

//Initialise the remaining variables
String pid = request.getParameter("pid");   
String guideline = request.getParameter("guideline");
String eusigParamName = "";
String seriesSelector = "";
if(guideline.equalsIgnoreCase("1")){
    seriesSelector = "BPs";
    eusigParamName = "eusig_id_bps";
}else if(guideline.equalsIgnoreCase("8")){
    seriesSelector = "ICPm";
    eusigParamName = "eusig_id_icp";
}else if(guideline.equalsIgnoreCase("9")){
    seriesSelector = "CPP";
    eusigParamName = "eusig_id_cpp";
}
String holddown = request.getParameter("holddown");
if(holddown == null){
    holddown = "";
}
String eusigId = request.getParameter(eusigParamName);
if(eusigId == null){
    eusigId = "";
}
String eusigThreshold = patientList.getEusigThreshold(conn, eusigId);

String eventId = request.getParameter("event_id");
if(eventId == null){
    eventId = "";
}



String algorithm = ""; //This is currently unused, but may need to factor in later

//== END OF VARIABLE INITIALISATION ==

//== RENDER VARIABLES IN LOGGER FOR CONFIRMATION ==
logger.debug("=== PATIENT DETAILS ===");
logger.debug("Patient ID: " + pid);
logger.debug("Guideline: " + guideline);
logger.debug("Time window (mins): " + TIME_WINDOW_MINS);
logger.debug("=== CHART DETAILS ===");
logger.debug("Physiological data series: " + seriesSelector);
logger.debug("Data series threshold definition (eusig_id): " + eusigId);
logger.debug("Event hold-down definition (holddown): " + holddown);
logger.debug("Event ID shown on chart (event_id): " + eventId);
logger.debug("======");
//== END OF VARIABLE CONFIRMATION ==

//== POPULATE THE PATIENT ID VECTOR AND DECLARE SIZE VARIABLE (patientNum) ==
if(individualCharts){
    patientIds.add(pid);
}else{
    patientIds = patientList.getPatientIds(datasetId, conn, individualCharts);
}
int patientNum = patientIds.size();
logger.debug("patientNum: " + patientNum);
//== POPULATION OF PATIENT ID VECTOR ENDS ==

//== INITIALISE VARIABLES DEPENDENT ON PATIENT ID SIZE ==
String[] patientTotalOutputTables = new String[patientNum];
String[] patientIQRTables = new String[patientNum];
String[] patientIQRData = new String[patientNum];
gCalc.initPatientTables(patientNum);
//== VARIABLE INITIALISATION ENDS ==

//== POPULATE THE EVENT AND TREATMENT VECTORS ==
Vector<Event> events = new Vector<Event>();
Vector<Treatment> treatments = new Vector<Treatment>();
Vector<GuidelineDistance> guidelineDistances = new Vector<GuidelineDistance>();

events = patientList.getEvents(pid, holddown, seriesSelector, eusigId, guideline, brainItConn, conn);
logger.debug("events.size(): " + events.size());
treatments = patientList.getTreatments(pid, conn, dummy);
logger.debug("treatments.size(): " + treatments.size());
guidelineDistances = patientList.calculateGuidelineComparison(seriesSelector, treatments, events, TIME_WINDOW_MILLIS, algorithm,guideline,processModelRep,pid, brainItConn);
logger.debug("guidelineDistances.size(): " + guidelineDistances.size());
//== EVENT AND TREATMENT VECTOR POPULATION ENDS

//== RUN CALCULATIONS FOR CHART RENDERING OF SELECTED PID/EVENT ==
logger.debug("==== CHART RENDERING HERE ====");
int eventIdInt = 0;
try{
    eventIdInt = Integer.parseInt(eventId);
}catch(Exception e){
    logger.debug("NumberFormatException: " + e.getMessage());
}
String startPointTime = df.format(events.get(eventIdInt).getStart()); //This is the first time-point of the selected event
String guidelineReasonJsArrayStr = graphUtil.getGuidelineReasonJsArray(patientList,guidelineDistances);
String seriesStr = graphUtil.getOverallSeriesStr(pid,datasetId,eventId,seriesSelector,events,treatments,guidelineDistances,startPointTime,TIME_WINDOW_MILLIS);

long thisGuidelineStart = guidelineDistances.get(eventIdInt).getStart().getTime();
logger.debug("==== CHART RENDERING ENDS ====");
//== CHART RENDERING CALCULATIONS END ==


//== INITIALISE THE VARIABLES FOR SUBSEQUENT CHART RENDERING (NOT MAIN) ==
double thisPatientMeanCombined = 0.0;
double thisPatientMeanCombined2 = 0.0;
double thisPatientMaxCombined2 = 0.0;

Vector<String> timesWithinEvents = new Vector<String>();
Vector<String> patientMeanCombineds = new Vector<String>();
Vector<String> patientMeanCombineds2 = new Vector<String>();
Vector<String> patientMaxCombineds2 = new Vector<String>();

Vector<String> thisPatientLevels = new Vector<String>();
Vector<String> thisPatientContribs = new Vector<String>();

Vector<String> patientsTreatmentAssocs = new Vector<String>();

Vector<Double> patientDefaultTimes = new Vector<Double>();
Vector<Double> patientNonDefaultTimes = new Vector<Double>();

Vector<Vector> aggregateOutputs = new Vector<Vector>();
String aggregateOutputStr = "";

int totalEventNum = 0;
//== VARIABLE INITIALISATION FOR SUBSEQUENT CHARTS ENDS ==

//== FIND THE INDEX IN THE VECTOR LIST FOR THIS PID (Re-use later) ==
boolean pidFound = false;
int pidCount = 0;
int pidIndex = -1;
while(!pidFound && pidCount < patientIds.size()){        
    String thisPid = patientIds.get(pidCount);
    if(thisPid.equalsIgnoreCase(pid)){
        pidFound = true;
    }else{
        pidCount++;
    }
}
logger.debug("pidFound: " + pidFound);

if(pidFound){
    pidIndex = pidCount;
}
//== INDEX RETRIEVAL ENDS ==

//== GET THE CONTENT FOR THE OUTPUT AND IQR TABLES (this used to be multiple but is now indCharts only) ==
if(pidIndex != -1){

    patientTotalOutputTables[pidIndex] = "<table border='1' width='50%'>";
    patientTotalOutputTables[pidIndex] += "<tr><th>Total duration (mins)</th><th>Non-adherence (%)</th><th>Contributing reasons (%)</th></tr>";
    
    patientIQRTables[pidIndex] = "<table border='1' width='50%'>";
    patientIQRTables[pidIndex] += "<tr><th>Measure</th><th>Min</th><th>Q1</th><th>Mean</th><th>Median</th><th>Q3</th><th>Max</th></tr>";
        
    Vector<Event> thisPatientEvents = patientList.getEvents(pid, holddown, seriesSelector, eusigId, guideline, brainItConn, conn);
    int thisEventNum = thisPatientEvents.size();    
    logger.debug("thisEventNum: " + thisEventNum);
    totalEventNum += thisEventNum;
    
    int thisPatientTimeWithinEvents = 0;
    for(int j=0; j<thisPatientEvents.size(); j++){
        Event eventIn = thisPatientEvents.get(j);
        int eventSize = eventIn.getValues().size();
        thisPatientTimeWithinEvents += eventSize;
    }
    timesWithinEvents.add("" + thisPatientTimeWithinEvents);
    
    Vector<Treatment> thisPatientTreatments = patientList.getTreatments(pid, conn, dummy);
    logger.debug("thisPatientTreatments.size(): " + thisPatientTreatments.size());
    Vector<GuidelineDistance> thisGuidelineDistances = patientList.calculateGuidelineComparison(seriesSelector, thisPatientTreatments, thisPatientEvents, TIME_WINDOW_MILLIS, algorithm,guideline,processModelRep,pid, brainItConn);
    logger.debug("thisGuidelineDistanceSets.size(): " + thisGuidelineDistances.size());
    
    gCalc.calculateEventAdherence(thisPatientEvents,thisGuidelineDistances,patientList, pid,thisPatientTreatments, pidIndex, removeDefault);
    
    //Add the individual levels
    thisPatientLevels = gCalc.getPatientLevels(pidIndex);
    thisPatientContribs = gCalc.getPatientContribs(pidIndex);
    
    if(gCalc.getPatientLevels(pidIndex).size() > 1){
        patientsTreatmentAssocs.add(pid);
    }
    
    patientTotalOutputTables[pidIndex] += gCalc.getPatientTotalOutputTable(pidIndex);
    patientIQRTables[pidIndex] += gCalc.getPatientIQRTable(pidIndex);
    patientIQRData[pidIndex] = gCalc.getPatientIQRData(pidIndex,iqrRefLine);
        
    thisPatientMeanCombined = gCalc.getPatientMeanCombined(pidIndex);
    thisPatientMeanCombined = Math.round(thisPatientMeanCombined*100.0)/100.0; //Round to 2 decimal places
    patientMeanCombineds.add("" + thisPatientMeanCombined);    
    logger.debug("thisPatientMeanCombined: " + thisPatientMeanCombined);
    thisPatientMeanCombined2 = gCalc.getPatientMeanCombined2(pidIndex);
    thisPatientMaxCombined2 = gCalc.getPatientMaxCombined2(pidIndex);
    patientMaxCombineds2.add("" + thisPatientMaxCombined2);
    logger.debug("thisPatientMaxCombined2: " + thisPatientMaxCombined2);
    thisPatientMeanCombined2 = Math.round(thisPatientMeanCombined2*100.0)/100.0; //Round to 2 decimal places
    patientMeanCombineds2.add("" + thisPatientMeanCombined2);
    logger.debug("thisPatientMeanCombined2: " + thisPatientMeanCombined2);
    
    logger.debug("==========");
    
    patientTotalOutputTables[pidIndex] += "</table>";
    patientIQRTables[pidIndex] += "</table>";
    
    patientDefaultTimes.add(gCalc.getDefaultTime(pidIndex));
    patientNonDefaultTimes.add(gCalc.getNonDefaultTime(pidIndex));    
    
}
//== CONTENT RETRIEVAL FOR OUTPUT AND IQR TABLES END ==

//== CALCULATE THE OUTPUT FOR THE COMBINATION CHART ==
String thisPatientMeanCombinedDisp = patientMeanCombineds.get(pidIndex);
//This is a capping fudge - should really scale it along a range as with the other one
double meanCombined = Double.parseDouble(thisPatientMeanCombinedDisp);
if(meanCombined > 1.0){
    meanCombined = (double) 1.0;
}
String thisPatientMeanCombined2Disp = patientMeanCombineds2.get(pidIndex);    
String thisPatientMaxCombined2Disp = patientMaxCombineds2.get(pidIndex);

double meanCombined2 = Double.parseDouble(thisPatientMeanCombined2Disp);
double maxCombined2 = Double.parseDouble(thisPatientMaxCombined2Disp);
double scaledCombined2 = meanCombined2 / maxCombined2;
double tickCombined2 = maxCombined2 / 2.0;

maxCombined2 = Math.round(maxCombined2*100.0)/100.0; //Round to 2 decimal places
scaledCombined2 = Math.round(scaledCombined2*100.0)/100.0; //Round to 2 decimal places
tickCombined2 = Math.round(tickCombined2*100.0)/100.0; //Round to 2 decimal places
//== CALCULATION OF COMBINATION CHART OUTPUT ENDS ==

//== COMPILE THE CATEGORIES AND DATA FOR THE STACKED CONTRIBUTION CHARTS ==
String categoryStr = "[";
String stackedSeriesStr = "[";
if(thisPatientContribs.size() > 0){
    
    int maxTokenNum = patientList.getMaxTokenNum(thisPatientContribs);
    int maxTokenLine = patientList.getMaxTokenLine(thisPatientContribs);
    
    StringTokenizer st = new StringTokenizer(thisPatientContribs.get(maxTokenLine),",");
    String[] catStrs = patientList.getCatStrs(maxTokenNum,st);    
    categoryStr += patientList.getCategoryStr(thisPatientLevels);        
    String[] inputStrs = patientList.getInputStrs(maxTokenNum,thisPatientContribs,catStrs);
    stackedSeriesStr += patientList.getStackedSeriesStr(maxTokenNum,catStrs,inputStrs);
}
categoryStr += "]";
stackedSeriesStr += "]";
//== CATEGORY/DATA COMPILATION ENDS ==

//== COUNT UP AND SUMMARISE ALL THE EVENT AND TREATMENT INFORMATION ==
Vector<String> treatmentEventAssocCount = patientList.getTreatmentWithEventCount();
logger.debug("Number of events with associated treatments for patient " + pid + ": " + treatmentEventAssocCount.size());
logger.debug("Total number of events for patient " + pid + ": " + totalEventNum);
logger.debug("========");

//== CREATE LOGISTIC REGRESSION CSV FILES ==
if(!individualCharts){
    logger.debug("Running aggregate operations on dataset...");
    
    if(generateRegressionFile){
    
        logger.debug("Compiling instances for regression against GOSe");    
        Vector<Double>[] allPatientDurationSet = new Vector[patientNum];
        Vector<Double>[] allPatientDistanceSet = new Vector[patientNum];
    
        //Divide into split files (manually input numbers here)
        int lowerIndex = 226;
        int upperIndex = patientNum;
        int splitRun = 10;
    
        for(int i=lowerIndex; i<upperIndex; i++){

            String thisPid = patientIds.get(i);
            logger.debug("Compiling aggregate entry for patient (" + i + "): " + thisPid);
        
            //Run the event, treatment and guideline distance calculation for each patient
            Vector<Event> thisPatientEvents = patientList.getEvents(thisPid, holddown, seriesSelector, eusigId, guideline, brainItConn, conn);
            int thisEventNum = thisPatientEvents.size();    
            logger.debug("thisEventNum (" + thisPid + "): " + thisEventNum);        
            Vector<Treatment> thisPatientTreatments = patientList.getTreatments(thisPid, conn, dummy);
            logger.debug("thisPatientTreatments.size() (" + thisPid + "): " + thisPatientTreatments.size());
            Vector<GuidelineDistance> thisGuidelineDistances = patientList.calculateGuidelineComparison(seriesSelector, thisPatientTreatments, thisPatientEvents, TIME_WINDOW_MILLIS, algorithm,guideline,processModelRep,thisPid, brainItConn);
            logger.debug("thisGuidelineDistanceSets.size() (" + thisPid + "): " + thisGuidelineDistances.size());
    
            //Calculate the overall adherence
            gCalc.calculateEventAdherence(thisPatientEvents,thisGuidelineDistances,patientList,thisPid,thisPatientTreatments, i, removeDefault);
        
            //Retrieve the sets of durations and distances
            Vector<Double> thisDurationSet = gCalc.getOverallGuidelineIndDurations(i);
            Vector<Double> thisDistanceSet = gCalc.getOverallGuidelineIndDistances(i);
            allPatientDurationSet[i] = thisDurationSet;
            allPatientDistanceSet[i] = thisDistanceSet;
                
            logger.debug("------");
        }    
        logger.debug("Writing file for logistic regression...");    
        patientList.writeRegressionFiles(filepath,lowerIndex,upperIndex,splitRun,patientNum,patientIds,brainItConn,allPatientDurationSet,allPatientDistanceSet);
    
    }else if(showEventTreatmentAssoc){
        
        logger.debug("Output of total event/treatment association numbers...");            
        
        //Divide into groups of 50
        int lowerIndex = 50;
        int upperIndex = 96;
        
        int allPatientTotalEventNum = 0;        
        for(int i=lowerIndex; i<upperIndex; i++){

            String thisPid = patientIds.get(i);
            logger.debug("Compiling aggregate entry for patient (" + i + "): " + thisPid);
        
            //Run the event, treatment and guideline distance calculation for each patient
            Vector<Event> thisPatientEvents = patientList.getEvents(thisPid, holddown, seriesSelector, eusigId, guideline, brainItConn, conn);
            int thisEventNum = thisPatientEvents.size();    
            logger.debug("thisEventNum (" + thisPid + "): " + thisEventNum);        
            allPatientTotalEventNum += thisEventNum;
            Vector<Treatment> thisPatientTreatments = patientList.getTreatments(thisPid, conn, dummy);
            logger.debug("thisPatientTreatments.size() (" + thisPid + "): " + thisPatientTreatments.size());
            
            Vector<GuidelineDistance> thisGuidelineDistances = patientList.calculateGuidelineComparison(seriesSelector, thisPatientTreatments, thisPatientEvents, TIME_WINDOW_MILLIS, algorithm,guideline,processModelRep,thisPid, brainItConn);
            logger.debug("thisGuidelineDistanceSets.size() (" + thisPid + "): " + thisGuidelineDistances.size());
    
            //Calculate the overall adherence
            gCalc.calculateEventAdherence(thisPatientEvents,thisGuidelineDistances,patientList,thisPid,thisPatientTreatments, i, removeDefault);
            
            //Retrieve the number of patient with event/treatment associations here
            if(gCalc.getPatientLevels(i).size() > 1){
                patientsTreatmentAssocs.add(pid);
            }
            
            logger.debug("------");
        }    
        
        logger.debug("Number of patients with associated treatments: " + patientsTreatmentAssocs.size());
        treatmentEventAssocCount = patientList.getTreatmentWithEventCount();
        logger.debug("Number of events with associated treatments (all patients) - with time window of " + TIME_WINDOW_MINS + " mins: " + treatmentEventAssocCount.size());
        logger.debug("Total number of events (all patients): " + allPatientTotalEventNum);
        
    }else if(compileScatterPlots){
        
        logger.debug("Compiling information for aggregate outputs to display (box-plots and scatter-plots)...");                    
        
        if(aggregateEusig){
            logger.debug("Varying the eusigIds (x10 definitions)...");        
            for(int j=0; j < 10; j++){
            
                String thisEusigId = "" + (j + 1);                       
                Vector<String> thisThresholdOutput = new Vector<String>();
                thisThresholdOutput.add(thisEusigId);
                for(int i=0; i<patientNum; i++){
                //for(int i=0; i<3; i++){
                    
                    String thisPid = patientIds.get(i);
                    logger.debug("EUSIG definition #" + thisEusigId + " on patient " + thisPid);
        
                    //Run the event, treatment and guideline distance calculation for each patient
                    Vector<Event> thisPatientEvents = patientList.getEvents(thisPid, holddown, seriesSelector, thisEusigId, guideline, brainItConn, conn);
                    int thisEventNum = thisPatientEvents.size();    
                    logger.debug("thisEventNum (" + thisPid + "): " + thisEventNum);        
                    Vector<Treatment> thisPatientTreatments = patientList.getTreatments(thisPid, conn, dummy);
                    logger.debug("thisPatientTreatments.size() (" + thisPid + "): " + thisPatientTreatments.size());
            
                    Vector<GuidelineDistance> thisGuidelineDistances = patientList.calculateGuidelineComparison(seriesSelector, thisPatientTreatments, thisPatientEvents, TIME_WINDOW_MILLIS, algorithm,guideline,processModelRep,thisPid, brainItConn);
                    logger.debug("thisGuidelineDistanceSets.size() (" + thisPid + "): " + thisGuidelineDistances.size());
    
                    //Calculate the overall adherence
                    gCalc.calculateEventAdherence(thisPatientEvents,thisGuidelineDistances,patientList,thisPid,thisPatientTreatments, i, removeDefault);
                
                    //Retrieve the mean duration/distance for this patient and add to overall vector
                    //Double thisMeanDuration = gCalc.getPatientMeanDuration(i);
                    Double thisMeanDistance = gCalc.getPatientMeanDistance(i);;
                
                    //thisThresholdOutput.add("" + thisMeanDuration);
                    thisThresholdOutput.add("" + thisMeanDistance);
            
                    logger.debug("------");
                }
                logger.debug("======");
                aggregateOutputs.add(thisThresholdOutput);                
            }
            
            //Now we have a VxV of mean distances for each threshold definition
            //Find the IQR of each line, and pass these as a whole to the new group page
            Vector<Double> allMeanDistances = new Vector<Double>();
            //Remove the identifier at the front and make the new vector Doubles only
            for(int i = 0; i < aggregateOutputs.size(); i++){
                Vector<String> thresholdLineIn = aggregateOutputs.get(i);
                int lineSize = thresholdLineIn.size();
                for(int j = 1; j < lineSize; j++){
                    String valueInStr = thresholdLineIn.get(j);
                    try{
                        Double valueInDouble = Double.parseDouble(valueInStr);
                        allMeanDistances.add(valueInDouble);
                    }catch(Exception e){
                        logger.debug("Number parsing error...");
                    }
                }
            
                Collections.sort(allMeanDistances);
                double max = patientList.calcMax(allMeanDistances);
                max = Math.round(max*100.0)/100.0; //Round to 2 decimal places
                double min = patientList.calcMin(allMeanDistances);
                min = Math.round(min*100.0)/100.0; //Round to 2 decimal places
                double median = patientList.calcMedian(allMeanDistances);
                median = Math.round(median*100.0)/100.0; //Round to 2 decimal places
                double q1 = patientList.calcQ1(allMeanDistances);
                q1 = Math.round(q1*100.0)/100.0; //Round to 2 decimal places
                double q3 = patientList.calcQ3(allMeanDistances);
                q3 = Math.round(q3*100.0)/100.0; //Round to 2 decimal places
            
                String thisThresholdIQRStr = "[" + min + "," + q1 + "," + median + "," + q3 + "," + max + "]";
                aggregateOutputStr += "" + thisThresholdIQRStr + "|";
                
            }
            
            //Print the IQR data to the logfile
            logger.debug("aggregateOutputStr: " + aggregateOutputStr);
        
        }else if(aggregateWeightings){
            
            logger.debug("Varying the score weightings (x9 definitions)...");        
            String[] weightingLabels = {"wskipn = 0.75","wskipn = 0.5","wskipn = 0.25",
                                        "wskipe = 0.6","wskipe = 0.3","wskipe = 0.9",
                                        "type = 0.5","type = 0.25","type = 0.75"};
            for(int j=0; j < 9; j++){
            
                Vector<String> thisWeightingOutput = new Vector<String>();
                thisWeightingOutput.add(weightingLabels[j]);
                algorithm = weightingLabels[j];
                //for(int i=0; i<patientNum; i++){
                for(int i=0; i<3; i++){
                    
                    String thisPid = patientIds.get(i);
                    logger.debug("Compiling aggregate entry for patient (" + i + "): " + thisPid);
        
                    //Run the event, treatment and guideline distance calculation for each patient
                    Vector<Event> thisPatientEvents = patientList.getEvents(thisPid, holddown, seriesSelector, eusigId, guideline, brainItConn, conn);
                    int thisEventNum = thisPatientEvents.size();    
                    logger.debug("thisEventNum (" + thisPid + "): " + thisEventNum);        
                    Vector<Treatment> thisPatientTreatments = patientList.getTreatments(thisPid, conn, dummy);
                    logger.debug("thisPatientTreatments.size() (" + thisPid + "): " + thisPatientTreatments.size());
            
                    Vector<GuidelineDistance> thisGuidelineDistances = patientList.calculateGuidelineComparison(seriesSelector, thisPatientTreatments, thisPatientEvents, TIME_WINDOW_MILLIS, algorithm, guideline,processModelRep,thisPid, brainItConn);
                    logger.debug("thisGuidelineDistanceSets.size() (" + thisPid + "): " + thisGuidelineDistances.size());
    
                    //Calculate the overall adherence
                    gCalc.calculateEventAdherence(thisPatientEvents,thisGuidelineDistances,patientList,thisPid,thisPatientTreatments, i, removeDefault);
                
                    //Retrieve the mean duration/distance for this patient and add to overall vector
                    //Double thisMeanDuration = gCalc.getPatientMeanDuration(i);
                    Double thisMeanDistance = gCalc.getPatientMeanDistance(i);;
                
                    //thisThresholdOutput.add("" + thisMeanDuration);
                    thisWeightingOutput.add("" + thisMeanDistance);
            
                    logger.debug("------");
                }
                aggregateOutputs.add(thisWeightingOutput);                
            }
            
            //Now we have a VxV of mean distances for each threshold definition
            //Find the IQR of each line, and pass these as a whole to the new group page
            Vector<Double> allMeanDistances = new Vector<Double>();
            //Remove the identifier at the front and make the new vector Doubles only
            for(int i = 0; i < aggregateOutputs.size(); i++){
                Vector<String> thresholdLineIn = aggregateOutputs.get(i);
                int lineSize = thresholdLineIn.size();
                for(int j = 1; j < lineSize; j++){
                    String valueInStr = thresholdLineIn.get(j);
                    try{
                        Double valueInDouble = Double.parseDouble(valueInStr);
                        allMeanDistances.add(valueInDouble);
                    }catch(Exception e){
                        logger.debug("Number parsing error...");
                    }
                }
            
                Collections.sort(allMeanDistances);
                double max = patientList.calcMax(allMeanDistances);
                double min = patientList.calcMin(allMeanDistances);
                double mean = patientList.calcMean(allMeanDistances);
                double median = patientList.calcMedian(allMeanDistances);
                double q1 = patientList.calcQ1(allMeanDistances);
                double q3 = patientList.calcQ3(allMeanDistances);
            
                String thisThresholdIQRStr = "[" + min + "," + q1 + "," + median + "," + q3 + "," + max + "]";
                aggregateOutputStr += "" + thisThresholdIQRStr + "|";
                
            }
        }else if(aggregateAge){
                        
            logger.debug("Varying the ages...");        
            Vector<Vector> ages = patientList.getAges(brainItConn);
            //These are binned into 10 age-range categories
            int ageCatNum = 10;
            Vector<String>[] thisAgeOutput = new Vector[ageCatNum];
            for(int i=0; i<patientNum; i++){
                    
                String thisPid = patientIds.get(i);
                boolean patientFound = false;
                int patientCount = 0;
                while(!patientFound && patientCount < ages.size()){
                    String patientIn = (String) ages.get(patientCount).get(0);
                    if(patientIn.equals(thisPid)){
                        patientFound = true;
                    }else{
                        patientCount++;
                    }
                }
                String ageIn = (String) ages.get(patientCount).get(1);
                logger.debug("Compiling aggregate entry for patient (" + i + "): " + thisPid);
        
                //Run the event, treatment and guideline distance calculation for each patient
                Vector<Event> thisPatientEvents = patientList.getEvents(thisPid, holddown, seriesSelector, eusigId, guideline, brainItConn, conn);
                int thisEventNum = thisPatientEvents.size();    
                logger.debug("thisEventNum (" + thisPid + "): " + thisEventNum);        
                Vector<Treatment> thisPatientTreatments = patientList.getTreatments(thisPid, conn, dummy);
                logger.debug("thisPatientTreatments.size() (" + thisPid + "): " + thisPatientTreatments.size());
            
                Vector<GuidelineDistance> thisGuidelineDistances = patientList.calculateGuidelineComparison(seriesSelector, thisPatientTreatments, thisPatientEvents, TIME_WINDOW_MILLIS, algorithm,guideline,processModelRep,thisPid, brainItConn);
                logger.debug("thisGuidelineDistanceSets.size() (" + thisPid + "): " + thisGuidelineDistances.size());
    
                //Calculate the overall adherence
                gCalc.calculateEventAdherence(thisPatientEvents,thisGuidelineDistances,patientList,thisPid,thisPatientTreatments, i, removeDefault);
                
                //Retrieve the mean duration/distance for this patient and add to overall vector
                //Double thisMeanDuration = gCalc.getPatientMeanDuration(i);
                Double thisMeanDistance = gCalc.getPatientMeanDistance(i);;
                
                //thisAgeOutput.add("" + thisMeanDuration);
                int ageIndex = -1;
                try{
                    Double ageDouble = Double.parseDouble(ageIn);
                    if(ageDouble > 0 && ageDouble <= 10){
                        ageIndex = 0;                        
                    }else if(ageDouble > 10 && ageDouble <= 20){
                        ageIndex = 1;                        
                    }else if(ageDouble > 20 && ageDouble <= 30){
                        ageIndex = 2;                        
                    }else if(ageDouble > 30 && ageDouble <= 40){
                        ageIndex = 3;                        
                    }else if(ageDouble > 40 && ageDouble <= 50){
                        ageIndex = 4;                        
                    }else if(ageDouble > 50 && ageDouble <= 60){
                        ageIndex = 5;                        
                    }else if(ageDouble > 60 && ageDouble <= 70){
                        ageIndex = 6;                        
                    }else if(ageDouble > 70 && ageDouble <= 80){
                        ageIndex = 7;                        
                    }else if(ageDouble > 80 && ageDouble <= 90){
                        ageIndex = 8;                        
                    }else if(ageDouble > 90 && ageDouble <= 100){
                        ageIndex = 9;                        
                    }
                }catch(Exception e){
                    logger.debug("Number parsing error...");
                }
                thisAgeOutput[ageIndex].add("" + thisMeanDistance);
                    
                logger.debug("------");
            }
            for(int i = 0; i < ageCatNum; i++){
                aggregateOutputs.add(thisAgeOutput[i]);
            }
            
            //Now we have a VxV of mean distances for each threshold definition
            //Find the IQR of each line, and pass these as a whole to the new group page
            Vector<Double> allMeanDistances = new Vector<Double>();
            //Remove the identifier at the front and make the new vector Doubles only
            for(int i = 0; i < aggregateOutputs.size(); i++){
                Vector<String> thresholdLineIn = aggregateOutputs.get(i);
                int lineSize = thresholdLineIn.size();
                for(int j = 1; j < lineSize; j++){
                    String valueInStr = thresholdLineIn.get(j);
                    try{
                        Double valueInDouble = Double.parseDouble(valueInStr);
                        allMeanDistances.add(valueInDouble);
                    }catch(Exception e){
                        logger.debug("Number parsing error...");
                    }
                }
            
                Collections.sort(allMeanDistances);
                double max = patientList.calcMax(allMeanDistances);
                double min = patientList.calcMin(allMeanDistances);
                double mean = patientList.calcMean(allMeanDistances);
                double median = patientList.calcMedian(allMeanDistances);
                double q1 = patientList.calcQ1(allMeanDistances);
                double q3 = patientList.calcQ3(allMeanDistances);
            
                String thisThresholdIQRStr = "[" + min + "," + q1 + "," + median + "," + q3 + "," + max + "]";
                aggregateOutputStr += "" + thisThresholdIQRStr + "|";
                
            }
            
        }else if(aggregateMassLesion){
                        
            logger.debug("Varying the presence of mass lesion injuries...");        
            Vector<Vector> massLesions = patientList.getMassLesions(brainItConn);
            Vector<String> thisMLPresent = new Vector<String>();
            Vector<String> thisMLNone = new Vector<String>();
            for(int i=0; i<patientNum; i++){                    
                
                String thisPid = patientIds.get(i);
                boolean patientFound = false;
                int patientCount = 0;
                while(!patientFound && patientCount < massLesions.size()){
                    String patientIn = (String) massLesions.get(patientCount).get(0);
                    if(patientIn.equals(thisPid)){
                        patientFound = true;
                    }else{
                        patientCount++;
                    }
                }
                String mlIn = (String) massLesions.get(patientCount).get(1);
                logger.debug("Compiling aggregate entry for patient (" + i + "): " + thisPid);
        
                //Run the event, treatment and guideline distance calculation for each patient
                Vector<Event> thisPatientEvents = patientList.getEvents(thisPid, holddown, seriesSelector, eusigId, guideline, brainItConn, conn);
                int thisEventNum = thisPatientEvents.size();    
                logger.debug("thisEventNum (" + thisPid + "): " + thisEventNum);        
                Vector<Treatment> thisPatientTreatments = patientList.getTreatments(thisPid, conn, dummy);
                logger.debug("thisPatientTreatments.size() (" + thisPid + "): " + thisPatientTreatments.size());
            
                Vector<GuidelineDistance> thisGuidelineDistances = patientList.calculateGuidelineComparison(seriesSelector, thisPatientTreatments, thisPatientEvents, TIME_WINDOW_MILLIS, algorithm,guideline,processModelRep,thisPid, brainItConn);
                logger.debug("thisGuidelineDistanceSets.size() (" + thisPid + "): " + thisGuidelineDistances.size());
    
                //Calculate the overall adherence
                gCalc.calculateEventAdherence(thisPatientEvents,thisGuidelineDistances,patientList,thisPid,thisPatientTreatments, i, removeDefault);
                
                //Retrieve the mean duration/distance for this patient and add to overall vector
                //Double thisMeanDuration = gCalc.getPatientMeanDuration(i);
                Double thisMeanDistance = gCalc.getPatientMeanDistance(i);;
                
                //thisMLOutput.add("" + thisMeanDuration);
                if(mlIn.equals("Present")){
                    thisMLPresent.add("" + thisMeanDistance);
                }else{
                    thisMLNone.add("" + thisMeanDistance);
                }
                
                logger.debug("------");
            }            
            aggregateOutputs.add(thisMLPresent);
            aggregateOutputs.add(thisMLNone);
            
            //Now we have a VxV of mean distances for each threshold definition
            //Find the IQR of each line, and pass these as a whole to the new group page
            Vector<Double> allMeanDistances = new Vector<Double>();
            //Remove the identifier at the front and make the new vector Doubles only
            for(int i = 0; i < aggregateOutputs.size(); i++){
                Vector<String> thresholdLineIn = aggregateOutputs.get(i);
                int lineSize = thresholdLineIn.size();
                for(int j = 0; j < lineSize; j++){
                    String valueInStr = thresholdLineIn.get(j);
                    try{
                        Double valueInDouble = Double.parseDouble(valueInStr);
                        allMeanDistances.add(valueInDouble);
                    }catch(Exception e){
                        logger.debug("Number parsing error...");
                    }
                }
            
                Collections.sort(allMeanDistances);
                double max = patientList.calcMax(allMeanDistances);
                double min = patientList.calcMin(allMeanDistances);
                double mean = patientList.calcMean(allMeanDistances);
                double median = patientList.calcMedian(allMeanDistances);
                double q1 = patientList.calcQ1(allMeanDistances);
                double q3 = patientList.calcQ3(allMeanDistances);
            
                String thisThresholdIQRStr = "[" + min + "," + q1 + "," + median + "," + q3 + "," + max + "]";
                aggregateOutputStr += "" + thisThresholdIQRStr + "|";                
            }
            
        }        
    }
}
%>


<%
if(individualCharts){            

String catBoxPlotStr = "";
for(int i=0; i<patientNum; i++){
    catBoxPlotStr += "'" + (i+1) + "'";
    if(i != patientNum-1){
        catBoxPlotStr += ",";
    }
}

String patientIQRDataStr = "";
for(int i=0; i<patientNum; i++){
    patientIQRDataStr += "" + patientIQRData[i] + "";
    if(i != patientNum-1){
        patientIQRDataStr += ",";
    }
}

%>

<jsp:include page="ind_charts_header.jsp">
    <jsp:param name="pid" value="<%=pid%>"/>
    <jsp:param name="eventId" value="<%=eventId%>"/>
    <jsp:param name="seriesSelector" value="<%=seriesSelector%>"/>    
    <jsp:param name="seriesStr" value="<%=seriesStr%>"/>
    <jsp:param name="stackedSeriesStr" value="<%=stackedSeriesStr%>"/>
    <jsp:param name="categoryStr" value="<%=categoryStr%>"/>    
    <jsp:param name="thisGuidelineStart" value="<%=thisGuidelineStart%>"/>
    <jsp:param name="meanCombined" value="<%=meanCombined%>"/>
    <jsp:param name="meanCombined2" value="<%=meanCombined2%>"/>
    <jsp:param name="maxCombined2" value="<%=maxCombined2%>"/>
    <jsp:param name="tickCombined2" value="<%=tickCombined2%>"/>
    <jsp:param name="guidelineReasonJsArrayStr" value="<%=guidelineReasonJsArrayStr%>"/>    
    <jsp:param name="catBoxPlotStr" value="<%=catBoxPlotStr%>"/>
    <jsp:param name="patientIQRDataStr" value="<%=patientIQRDataStr%>"/>
    <jsp:param name="iqrRefLine" value="<%=iqrRefLine%>"/>
</jsp:include>

<%
//End of individualCharts clause
}else{
%>

<jsp:include page="group_charts_header.jsp">
    <jsp:param name="aggregateOutputStr" value="<%=aggregateOutputStr%>"/>
</jsp:include>


<%
//End of groupCharts clause
}
%>

<%-- Title banner --%> 
<jsp:include page="/page/page_nav.jsp" >
    <jsp:param name="embedding_page" value="physiochart"/>
    <jsp:param name="embedding_dataset" value="<%=datasetId%>"/>
    <jsp:param name="embedding_pid" value="<%=pid%>"/>
</jsp:include>

<%--<script src="https://code.highcharts.com/highcharts.js"></script>--%>

    </head>
    <body>
        
<%        
if(individualCharts){        
%>        

<%    
Vector<String> totalStayTimes = patientList.getTotalStayTimes(brainItConn,patientIds);   
    
    if(pidIndex != -1){
        
        String thisTotalStayTime = "";
        String thisTimeWithinEvents = "";
        Double thisDefaultTime = 0.0;
        Double thisNonDefaultTime = 0.0;
        String nonDefaultAsPercentStr = "Not available";
        
        if(!datasetId.equals("5")){
        
            thisTotalStayTime = totalStayTimes.get(pidIndex);    
            if(thisTotalStayTime == null){
                thisTotalStayTime = "Not available";
            }
            thisTimeWithinEvents = timesWithinEvents.get(pidIndex);
            if(thisTimeWithinEvents == null){
                thisTimeWithinEvents = "Not available";
            }
            thisDefaultTime = patientDefaultTimes.get(pidIndex);
            thisNonDefaultTime = patientNonDefaultTimes.get(pidIndex);
            Double thisTimeWithinEventsDouble = 0.0;
            nonDefaultAsPercentStr = "Not available";
        
            try{
                thisTimeWithinEventsDouble = Double.parseDouble(thisTimeWithinEvents);
                Double nonDefaultAsPercent = ((thisNonDefaultTime / thisTimeWithinEventsDouble) * 100.0);
                nonDefaultAsPercent = Math.round(nonDefaultAsPercent*100.0)/100.0;
                nonDefaultAsPercentStr = "" + nonDefaultAsPercent;
            }catch(Exception e){
                logger.info("Time within events not available: " + e.getMessage());
            }
        }
        
        String patientOutputTable = patientTotalOutputTables[pidIndex];
        String patientIQRTable = patientIQRTables[pidIndex];

logger.debug("pid: " + pid);
logger.debug("datasetStr: " + datasetStr);
logger.debug("seriesSelector: " + seriesSelector);
logger.debug("timeWindow: " + TIME_WINDOW_MINS);
logger.debug("eusigId: " + eusigThreshold);
logger.debug("holddown: " + holddown);
logger.debug("meanCombined: " + meanCombined);
logger.debug("meanCombined2: " + meanCombined2);
logger.debug("removeDefault: " + removeDefault);
logger.debug("patientOutputTable: " + patientOutputTable);
logger.debug("patientIQRTable: " + patientIQRTable);
%>

<jsp:include page="ind_charts_body.jsp">
    <jsp:param name="pid" value="<%=pid%>"/>
    <jsp:param name="dataset" value="<%=datasetStr%>"/>
    <jsp:param name="seriesSelector" value="<%=seriesSelector%>"/>    
    <jsp:param name="timeWindow" value="<%=TIME_WINDOW_MINS%>"/>
    <jsp:param name="eusigId" value="<%=eusigThreshold%>"/>
    <jsp:param name="holddown" value="<%=holddown%>"/>
    
    <jsp:param name="meanCombined" value="<%=meanCombined%>"/>
    <jsp:param name="meanCombined2" value="<%=meanCombined2%>"/> 
    
    <jsp:param name="removeDefault" value="<%=removeDefault%>"/>
    <jsp:param name="patientOutputTable" value="<%=patientOutputTable%>"/>
    <jsp:param name="patientIQRTable" value="<%=patientIQRTable%>"/>
    
    <jsp:param name="thisTotalStayTime" value="<%=thisTotalStayTime%>"/>
    <jsp:param name="thisTimeWithinEvents" value="<%=thisTimeWithinEvents%>"/>
    <jsp:param name="thisDefaultTime" value="<%=thisDefaultTime%>"/>
    <jsp:param name="thisNonDefaultTime" value="<%=thisNonDefaultTime%>"/>
    <jsp:param name="nonDefaultAsPercent" value="<%=nonDefaultAsPercentStr%>"/>
    
</jsp:include>

<%
    }
    
//End of indCharts clause
}else{
    
%>

<jsp:include page="group_charts_body.jsp">
    <jsp:param name="aggregateOutputStr" value="<%=aggregateOutputStr%>"/>    
</jsp:include>

<%
}//End of individualCharts=false (groupCharts) clause
%>

</p>

<jsp:include page="/page/page_foot.jsp" />
</div>

</body>
</html>
