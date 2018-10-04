/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package patients;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author astell
 */
public class Utilities {
    
    private static final Logger logger = Logger.getLogger(Utilities.class);
    private String filepath = "";
    private Vector<Vector> patientFluidParams = null;
    private Vector<Vector> patientCommentParams = null;
    private Vector<Vector> patientPressorParams = null;
    
    public Utilities(){
        filepath = "";
    }
    
    public String getFilepath(){
        return filepath;
    }
    
    public Vector<Vector> getPatientFluidParams(){
        return patientFluidParams;
    }
    
    public Vector<Vector> getPatientCommentParams(){
        return patientCommentParams;
    }
    
    public Vector<Vector> getPatientPressorParams(){
        return patientPressorParams;
    }
    
    public Vector<Vector> getTreatments(String samplingSize, ListPatients patientList){
        
        Vector<Vector> treatments = new Vector<Vector>();
        String fluidsFilename = filepath + "anthony_fluids2.csv";
        patientFluidParams = patientList.getParamOutput(fluidsFilename,samplingSize);
        String commentsFilename = filepath + "anthony_comments2.csv";
        patientCommentParams = patientList.getParamOutput(commentsFilename,samplingSize);
        String pressorsFilename = filepath + "anthony_pressors2.csv";
        patientPressorParams = patientList.getParamOutput(pressorsFilename,samplingSize);

        for(int i=0; i<patientFluidParams.size(); i++){
            Vector<String> paramIn = patientFluidParams.get(i);
            treatments.add(paramIn);        
        }
        for(int i=0; i<patientCommentParams.size(); i++){
            Vector<String> paramIn = patientCommentParams.get(i);
            treatments.add(paramIn);
        }
        for(int i=0; i<patientPressorParams.size(); i++){
            Vector<String> paramIn = patientPressorParams.get(i);
            treatments.add(paramIn);
        }
        return treatments;
    }
    
    public Vector<Event> getEvents(String parameterFeed, String patientID, String holdDownFeed){
        
        SimpleDateFormat df2 = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
        FileReader fr = null;
        BufferedReader br = null;

        Vector<Event> events = new Vector<Event>();

        String filepathEvents = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\EventDetection\\output\\";
        String filenameEvents = filepathEvents + patientID + "_" + parameterFeed + "_holddown" + holdDownFeed + "_events.csv";

        //For now, we want start, end, values and feed        
        int eventCount = 0;
        Event event = null;
        try {
            fr = new FileReader(filenameEvents);
            br = new BufferedReader(fr);

            int eventLineCount = 0;
            while (br.ready()) {
                String lineIn = br.readLine();

                if (eventLineCount == 1) {
                    event = new Event(eventCount, parameterFeed);
                } else if (eventLineCount == 2 || eventLineCount == 3) {
                    //This is the start or end values
                    int colonIndex = lineIn.indexOf(":");
                    String timestampStr = lineIn.substring(colonIndex + 1, lineIn.length());
                    
                    timestampStr = timestampStr.trim();
                    Date timestamp = df2.parse(timestampStr);
                    if (eventLineCount == 2) {                        
                        event.setStart(timestamp);
                    } else {                        
                        event.setEnd(timestamp);
                    }
                } else if (eventLineCount == 4) {
                    //This is the set of values for this event
                    StringTokenizer st = new StringTokenizer(lineIn, ",");
                    Vector<String> eventValues = new Vector<String>();
                    /*if(patientID.equals("5027262")){
                        logger.info("st.countTokens: " + st.countTokens());
                    }*/
                    //int timeCount = 1440; //This is an upper limit of 24hrs for a single event
                    int timeCount = 480; //This is an upper limit of 8hrs for a single event
                    //while (st.hasMoreTokens() && st.countTokens() < timeCount) {
                    while (st.hasMoreTokens()) {
                        String valueIn = st.nextToken();
                        eventValues.add(valueIn);
                    }
                    event.setValues(eventValues);

                    events.add(event);
                    eventCount++;
                }
                eventLineCount++;
                if (eventLineCount == 5) {
                    eventLineCount = 0;
                }
                
                /*if(patientID.equals("5027262")){
                    logger.info("eventLineCount: " + eventLineCount);
                }*/                
            }
            //logger.info("eventCount: " + eventCount);
        } catch (Exception e) {
            //logger.info("Error: " + e.getMessage());
            //System.out.println("Error: " + e.getMessage());
        }
        return events;
    }
    
    public Vector<Event> getEvents(double officialThreshold, String parameterFeed, int paramIndex, boolean comparatorGreater, int holdDownDefinition, Vector<Vector> patientParams) {

        int HOLD_DOWN_DEFINITION = holdDownDefinition;
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        Vector<Event> events = new Vector<Event>();

        boolean inEvent = false;
        int eventIndex = 0;
        boolean potentialEvent = false;
        boolean potentialClear = false;
        int eventHolddownCount = 0;
        int clearHolddownCount = 0;

        int physioSize = patientParams.size();
        System.out.println("physioSize: " + physioSize);
        //logger.info("Begin checking for events in time series...");
        
        java.util.Date lastTimestamp = new java.util.Date();
        Event event = new Event(0,parameterFeed);
        Vector<String> potentialValues = new Vector<String>();
        for (int i = 0; i < physioSize; i++) {
        //for (int i = 0; i < 1000; i++) {
        
            //Get the physiological line data
            Vector<String> physioLineIn = patientParams.get(i);

            //Read in timepoint and value
            java.util.Date thisTimestamp = null;
            try {
                thisTimestamp = df.parse(physioLineIn.get(0));                
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            double thisValue = 0;
            int physioLineIndex = paramIndex;
            String valueInStr = physioLineIn.get(physioLineIndex);
            valueInStr = valueInStr.trim();
            //Catches null feeds
            if (valueInStr.trim().equals("-1.0")) {
                valueInStr = "";
            }
            Double valueInDouble = null;
            try {
                double valueInD = Double.parseDouble(valueInStr);
                valueInDouble = new Double(valueInD);
            } catch (NumberFormatException nfe) {
                //Catches null feeds
                valueInDouble = new Double(Double.NaN);
            }
            thisValue = valueInDouble;                
            
            //Compare to last timepoint - if there is a gap, reset all the parameters 
            //(note we're not going into inferring gaps and the like)
            if (thisTimestamp.getTime() - lastTimestamp.getTime() > 60000) {
                System.out.println("RESET GAP...");
                inEvent = false;
                potentialEvent = false;
                potentialClear = false;
                eventHolddownCount = 0;
                clearHolddownCount = 0;
            }

            //Check if we're already mid-way through a real event
            if (inEvent) {

                //logger.info("EVENT IS IN PROGRESS...");
                //logger.info("thisValue: " + thisValue);
                //logger.info("thisTimestamp: " + thisTimestamp);
                    
                
                //If we're in an event, we need to check if we're still above the threshold
                //If we're not, then check if there's been a clear hold-down reached
                //If it has, then we note the end time and the clear hold-down (will always be 5 by definition)

                //If the value is less than or equal to the official threshold value
                boolean clearCondition = thisValue <= officialThreshold;
                if(!comparatorGreater){
                    clearCondition = thisValue > officialThreshold;
                }
                if (clearCondition) {
                    
                    //logger.info("POTENTIAL CLEAR...");
                    
                    //If potentialClear is false, set to true, reset counter and increment by 1
                    //If potentialClear is already true, increment clearHolddownCount by 1                    
                    if (!potentialClear) {
                        potentialClear = true;
                        clearHolddownCount = 0;
                    }
                    clearHolddownCount++;
                    //logger.info("clearHolddownCount: " + clearHolddownCount);

                    //If clearHolddownCount == 5
                    //Set inEvent = false, note end time (-5 minutes from this timestamp)
                    //set clearHolddown = 5 mins (expressed as long)
                    if (clearHolddownCount == HOLD_DOWN_DEFINITION) {
                        inEvent = false;
                        //logger.info("EVENT HAS ENDED...");
                        java.util.Date eventEnd = new java.util.Date(thisTimestamp.getTime() - (HOLD_DOWN_DEFINITION * 60000));
                        event.setEnd(eventEnd);
                        long clearHolddown = (HOLD_DOWN_DEFINITION * 60000);
                        event.setClearHolddown(clearHolddown);
                        events.add(event);
                        eventIndex++;
                    }
                }else{
                    potentialClear = false;
                    clearHolddownCount = 0;                    
                }
                //Whether the event has ended or not, still record the value that dips below the threshold                    
                Vector<String> theseValues = event.getValues();
                theseValues.add("" + thisValue);
                event.setValues(theseValues);
            } else {
                
                //logger.info("index: " + i);

                //If we're not in an event, we need to check if the threshold has been crossed
                //If it has, then we need to check whether this is the start of a potential or real event
                //If it's real, we want to note the start time and the event hold-down (will always be 5 by definition)

                //If the value is greater than the official threshold value
                boolean eventCondition = thisValue > officialThreshold;
                if(!comparatorGreater){
                    eventCondition = thisValue <= officialThreshold;
                }
                //if (thisValue > officialThreshold) {                
                if (eventCondition) {

                    //logger.info("POTENTIAL EVENT...");
                    //logger.info("thisValue: " + thisValue);
                    //logger.info("thisTimestamp: " + thisTimestamp);
                    
                    //If potentialEvent is false, set to true, reset counter and increment by 1
                    //If potentialEvent is already true, increment eventHolddownCount by 1                    
                    if (!potentialEvent) {
                        potentialEvent = true;
                        eventHolddownCount = 0;
                    }
                    eventHolddownCount++;
                    potentialValues.add("" + thisValue);
                    //logger.info("eventHolddownCount: " + eventHolddownCount);

                    //If eventHolddownCount == 5
                    //Set realEvent = true, note start time (-5 minutes from this timestamp)
                    //set eventHolddown = 5 mins (expressed as long)
                    if (eventHolddownCount == HOLD_DOWN_DEFINITION) {
                        inEvent = true;
                        //logger.info("EVENT HAS BEGUN...");
                        event = new Event(eventIndex,parameterFeed);
                        java.util.Date eventStart = new java.util.Date(thisTimestamp.getTime() - (HOLD_DOWN_DEFINITION * 60000));
                        event.setStart(eventStart);
                        long eventHolddown = (HOLD_DOWN_DEFINITION * 60000);
                        event.setEventHolddown(eventHolddown);

                        //Set this initial value and use it to create the value vector for this event
                        Vector<String> values = new Vector<String>();
                        values.addAll(potentialValues);                        
                        values.add("" + thisValue);
                        event.setValues(values);
                        
                        potentialEvent = false;
                        eventHolddownCount = 0;
                    }
                }else{
                    potentialEvent = false;
                    eventHolddownCount = 0;
                    potentialValues = new Vector<String>();
                }
            }
        }
        return events;
    }
    
    public void createCSVfile(String filename, Vector<Double>[] overallGuidelineIndDurations, Vector<Double>[] overallGuidelineIndDistances, Vector<Double>[] overallGuidelineCombineds, Vector<Double>[] overallGuidelineCombineds2, Vector<String> patientIds, Vector<Vector> gosScores){
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        try{
            
            FileWriter fw = new FileWriter(filename,true);
            BufferedWriter bw = new BufferedWriter(fw);
                
            //Header line
            String headerLine = "Patient ID, GOS, Avg duration, Avg distance, log(Avg duration), log(Avg distance), log(dur) + log(dist)";
            bw.write(headerLine + "\r\n");
            
            int overallInstanceCount = 0;
            int patientNum = patientIds.size();
            int gosSize = gosScores.size();
            for(int i=0; i<patientNum; i++){
            //for(int i=0; i<1; i++){
                String patientId = patientIds.get(i);
                logger.debug("patientId (createCSVfile): " + patientId);
                Integer gosScore = new Integer(-1);
                boolean patientFound = false;
                int patientCount = 0;
                while(!patientFound && patientCount < gosSize){
                    Vector<String> gosDetailIn = gosScores.get(patientCount);
                    String idIn = gosDetailIn.get(0);
                    if(idIn.equalsIgnoreCase(patientId)){
                        gosScore = Integer.parseInt(gosDetailIn.get(2));
                        patientFound = true;
                    }else{
                        patientCount++;                        
                    }
                }
                
                logger.debug("gosScore (createCSVfile): " + gosScore);
                Vector<Double> thisPatientDurations = overallGuidelineIndDurations[i];
                int thisPatientDurationSize = thisPatientDurations.size();                
                logger.debug("thisPatientDurationSize: " + thisPatientDurationSize);
                logger.debug("thisPatientDurations: " + thisPatientDurations);
                
                Vector<Double> thisPatientDistances = overallGuidelineIndDistances[i];
                Vector<Double> thisPatientCombineds = overallGuidelineCombineds[i];
                Vector<Double> thisPatientCombineds2 = overallGuidelineCombineds2[i];
                
                for(int j=0; j<thisPatientDurationSize; j++){
                    
                    String patientInfoOutLine = "" + overallInstanceCount + "," + patientId;
                    patientInfoOutLine += "," + gosScore + ""; 
                    patientInfoOutLine += "," + df.format(thisPatientCombineds.get(j)) + "," + df.format(thisPatientCombineds2.get(j));
                    patientInfoOutLine += "," + df.format(thisPatientDurations.get(j)) + "," + df.format(thisPatientDistances.get(j));
                    
                    logger.debug("patientInfoOutLine: " + patientInfoOutLine);
                    
                    bw.write(patientInfoOutLine + "\r\n");
                    overallInstanceCount++;
                }
            }
            bw.close();
            fw.close();
            
        }catch(Exception e){
            logger.debug("Error (createCSVfile): " + e.getMessage());
        }
    }
    
    public void createCSVfile(String filename, Vector<Vector> patientOutput, boolean adjustedModel){
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        try{
            
            FileWriter fw = new FileWriter(filename,true);
            BufferedWriter bw = new BufferedWriter(fw);
                
            //Header line
            //String headerLine = "Patient ID, GOS, Age,PNSH_GCS_Motor,NSH_Adm_GCS_Motor,PNSH_Left_Pupil_Reaction,PNSH_Left_Pupil_Size,PNSH_Right_Pupil_Reaction,PNSH_Right_Pupil_Size,NSH_Adm_Left_Pupil_Reaction,NSH_Adm_Left_Pupil_Size,NSH_Adm_Right_Pupil_Reaction,NSH_Adm_Right_Pupil_Size,Injury_Facial, ";
            //headerLine += "Avg duration, Avg distance, log(Avg duration), log(Avg distance), log(dur) + log(dist)";
            String headerLine = "Patient ID, GOS, Age,NSH_Adm_GCS_Motor,NSH_Adm_Left_Pupil_Reaction,NSH_Adm_Left_Pupil_Size,NSH_Adm_Right_Pupil_Reaction,NSH_Adm_Right_Pupil_Size,Injury_Facial, ";
            headerLine += "Avg duration, Avg distance, log(Avg duration), log(Avg distance), log(dur) + log(dist)";
            bw.write(headerLine + "\r\n");
            
            int patientNum = patientOutput.size();            
            for(int i=0; i<patientNum; i++){
            
                Vector<String> patientIn = patientOutput.get(i);
                logger.debug("patientIn (createCSVfile): " + patientIn);
                
                for(int j=0; j<patientIn.size(); j++){
                    bw.write(patientIn.get(j));
                    if(j != patientIn.size()-1){
                        bw.write(",");
                    }
                }
                bw.write("\r\n");
            }                
            bw.close();
            fw.close();            
        }catch(Exception e){
            logger.debug("Error (createCSVfile): " + e.getMessage());
        }
    }
    
    public void createCSVfile(String filename, Vector<Vector> patientOutput){
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        try{
            
            FileWriter fw = new FileWriter(filename,true);
            BufferedWriter bw = new BufferedWriter(fw);
                
            //Header line
            //String headerLine = "Patient ID, GOS, Avg duration, Avg distance, log(Avg duration), log(Avg distance), log(dur) + log(dist)";
            String headerLine = "Patient ID, GOS, Avg duration, Avg distance";
            bw.write(headerLine + "\r\n");
            
            int patientNum = patientOutput.size();            
            for(int i=0; i<patientNum; i++){
            
                Vector<String> patientIn = patientOutput.get(i);
                logger.debug("patientIn (createCSVfile): " + patientIn);
                
                for(int j=0; j<patientIn.size(); j++){
                    bw.write(patientIn.get(j));
                    if(j != patientIn.size()-1){
                        bw.write(",");
                    }
                }
                bw.write("\r\n");
            }                
            bw.close();
            fw.close();            
        }catch(Exception e){
            logger.debug("Error (createCSVfile): " + e.getMessage());
        }
    }

}


