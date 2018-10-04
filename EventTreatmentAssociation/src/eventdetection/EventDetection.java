/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventdetection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author astell
 */
public class EventDetection {

    private static final Logger logger = Logger.getLogger(EventDetection.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Set up logger        
        logger.setLevel(Level.INFO);
        //PropertyConfigurator.configure("C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\ICUChart\\Treatment Profiles\\EventDetection\\config\\log4j_eventdetection.properties");
        PropertyConfigurator.configure("/home/ubuntu/log4j/log4j_eventdetection.properties");

        logger.info("---- EVENT DETECTION BEGINS ----");

        boolean icca = false;
        boolean mimic = true;
        if(icca){
            logger.info("Processing ICCA data: " + icca);
        }else if(mimic){
            logger.info("Processing MIMIC III data: " + mimic);
        }
        
        
        String dbName = "brainit";
        String dbNameOut = "treatment_profiles_backup";
        BrainITDriver bitd = new BrainITDriver(logger);
        Connection conn = bitd.getConnection(dbName);

        //Get list of patient IDs
        int PATIENT_NUM_TO_RUN = 262;
        Vector<String> patientIDs = bitd.getPatientList(conn,"" + PATIENT_NUM_TO_RUN,icca,mimic);
        
        int patientNum = patientIDs.size();
        //int patientNum = 2;
        logger.info("Number of patients (total): " + patientNum);
        
        //Put the patient IDs into the new database        
        Connection newConn = bitd.getConnection(dbNameOut);
        //bitd.addPatients(patientIDs, newConn); - already done this from the treatment summary

        //Compile the EUSIG table of events (from definitions in new db)
        EUSIGTable eusigTable = bitd.getEUSIGTable(newConn);
        Vector<EUSIGParameter> eusigParameters = eusigTable.getEUSIGParameters();
        int eusigParamNum = eusigParameters.size();

        logger.info("eusigParamNum: " + eusigParamNum);
        
        int totalEventNum = 0;
        //For each patient:
        for (int i = 0; i < patientNum; i++) {
        //for (int i = 0; i < 1; i++) {
            
            logger.info("--- Patient #" + (i + 1) + " ---");
            String patientID = patientIDs.get(i);
            //Read in the physiological data file (args of patientID and parameter etc) and compile into a vector object
            logger.info("Patient ID: " + patientID);

            //String[] physioParameters = {"ICPm","HRT","TC","BPm","SaO2","BPs","BPd","CPP","RR","SaO2pls"};
            //Vector<Physiological> physios = bitd.getPhysioTrace(physioParameters, patientID, conn);
            //TreeMap physios = bitd.getPhysioTrace(physioParameters, patientID, conn);
            
            //Now need a conversion function to get the TreeMap "physios" into "physioLines" (replacing the commented out spreadsheet-read below)
                        
            //Vector<Vector> physioLines = new Vector<Vector>();
            Vector<Vector> physioLines = bitd.getPhysioLines(patientID,conn,icca,mimic);
            logger.info("physioLines.size(): " + physioLines.size());
            
            /*for(int j=0; j<physioLines.size(); j++){
                logger.info("physioLines(" + j + "): " + physioLines.get(j));
            }*/
            
            
            //logger.info("physioLines.get(0): " + physioLines.get(0));
            
            //String headerLine = bitd.getHeaderLine(patientID);
            //A wee hack here (17/04/17)
            String headerLine = "Timestamp,ICPm,HRT,TC,BPm,SaO2,BPs,BPd,CPP,RR,SaO2pls";
            if(icca){
                //headerLine = "time,HR,RespRate,ABP.dia,ABP.mean,ABP.sys,ICP.dia,ICP.mean,ICP.sys,PlethRate,ETCO2";
                headerLine = "time,HR,RespRate,ABP.mean,ABP.sys,ABP.dia,ICP.mean,ICP.sys,ICP.dia,ETCO2,CVP,PlethRate"; //Latest ICCA output
            }
            
            if(mimic){
                headerLine = "Timestamp,ICPm,CPP";
            }
            
            //String headerLine = "chartTime,HRT,RR,BPs,BPd,BPm,SaO2,ICP,TEMP,CVP,NIBPs,NIBPd,NIBPm";
            
            //Run an averaging tool here (more than one line per minute)
            if(icca){
                physioLines = bitd.averageInputs(physioLines);                                
            }
            
            
            //Tokenize the header line and pick out the relevant index numbers
            int[] paramIndexes = new int[eusigParamNum];
            for (int j = 0; j < eusigParamNum; j++) {
                EUSIGParameter eusigParamIn = eusigParameters.get(j);
                String paramToView = eusigParamIn.getParameter();
                String headerIn = paramToView;
                /*if(paramToView.equalsIgnoreCase("ICPm")){
                    headerIn = "ICP.mean";
                }*/
                if(paramToView.equalsIgnoreCase("BPs")){
                    headerIn = "ABP.sys";
                }
                StringTokenizer stHeader = new StringTokenizer(headerLine, ",");
                int tokenCount = 0;
                while (stHeader.hasMoreTokens()) {
                    if (headerIn.equals(stHeader.nextToken().trim())) {
                        paramIndexes[j] = tokenCount;
                    }
                    tokenCount++;
                }
            }

            logger.info("physioLines size: " + physioLines.size());
            //if(physioLines.size() < 10000){
            
            //Run this for each feed
            for (int j = 0; j < eusigParamNum; j++) {
            //for (int j = 0; j < 1; j++) {

                EUSIGParameter eusigParamIn = eusigParameters.get(j);
                String eusigID = eusigParamIn.getName();
                String paramToView = eusigParamIn.getParameter();
                String condition = eusigParamIn.getName();
                String units = eusigParamIn.getUnit();
                String threshold = "" + eusigParamIn.getGradeThreshold();
                logger.info("Name - " + condition);
                logger.info("Parameter - " + paramToView);
                
                boolean comparatorGreater = eusigParamIn.getComparatorGreater();
                logger.info("-----");

                //Set the hold-down definition here
                int[] holdDownDefinition = new int[4];
                if(mimic){
                    holdDownDefinition = new int[1];
                    holdDownDefinition[0] = 1;
                }else{
                    holdDownDefinition[0] = 5;
                    holdDownDefinition[0] = 10;
                    holdDownDefinition[0] = 15;
                    holdDownDefinition[0] = 20;
                }
                int holdDownDefNum = holdDownDefinition.length;

                for (int m = 0; m < holdDownDefNum; m++) {

                    double officialThreshold = (double) eusigParamIn.getGradeThreshold();
                    String conditionStr = "";
                    conditionStr = "Threshold value: ";
                    if (comparatorGreater) {
                        conditionStr += ">";
                    } else {
                        conditionStr += "<";
                    }
                    conditionStr += " " + officialThreshold + " " + units;
                    logger.info(conditionStr);
                    logger.info("Hold-down value: " + holdDownDefinition[m]);
                    logger.info("paramIndexes[" + j + "]: " + paramIndexes[j]);

                    EventCheck ec = new EventCheck(physioLines, logger);
                    Vector<Event> events = ec.getEvents(officialThreshold, paramToView, paramIndexes[j], comparatorGreater, holdDownDefinition[m],mimic);
                    logger.info("No. of events: " + events.size());
                    totalEventNum += events.size();

                    //Print event information to text files
                    //Values and timestamps printed in CSV format for each event
                    if (events.size() != 0) {                        
                        //ec.printEventsToSysOut(events);                        
                        ec.commitEvents(newConn,events,patientID,holdDownDefinition[m],eusigID);
                    }
                    logger.info("-----");
                }
                logger.info("=====");
                logger.info("Total event number: " + totalEventNum);
            }
        }
        logger.info("------- EVENT DETECTION ENDS -------");
    }
}
