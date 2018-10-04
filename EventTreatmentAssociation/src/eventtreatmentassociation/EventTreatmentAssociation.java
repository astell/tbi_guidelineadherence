/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

/**
 *
 * @author astell
 */
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;

import java.util.StringTokenizer;
import java.util.Date;
import java.util.Vector;
import java.util.TreeMap;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class EventTreatmentAssociation {

    private static final Logger logger = Logger.getLogger(EventTreatmentAssociation.class);
    private static boolean FIRST_TREATMENT_ONLY = false;
    private static int PATIENT_NUM = 0;    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Set up logger        
        logger.setLevel(Level.INFO);
        PropertyConfigurator.configure("C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\EventTreatmentAssociation\\config\\log4j_eventtreatmentassociation.properties");

        logger.info("---- EVENT TREATMENT ASSOCIATION BEGINS ----");

        //Create the helper class that loads in all the relevant information
        LoadData ld = new LoadData(logger);

        //RUN THIS PER PATIENT - BUT SOURCE THIS FROM THE FILENAMES NOW (RATHER THAN THE DATABASE)
        //Get the number of files from the filepath folder
        String filepath = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\BrainIT_TreatmentAnalysis\\output\\";
        Vector<String> patientIDs = ld.getPatientIDs(filepath);
        int patientNum = patientIDs.size();
        
        //===========================================
        
        //CONTROL THE PATIENT NUMBER FLAG HERE
        PATIENT_NUM = patientNum;        
        //PATIENT_NUM = 100;        
               
        //CONTROL THE TREATMENT NUMBER HERE
        FIRST_TREATMENT_ONLY = false;
        
        //===========================================
        
        //Now get a hashmap list of the centre IDs associated with all the patient IDs
        CentreInfo ci = new CentreInfo(logger);
        TreeMap patientCentres = ci.compilePatientCentreList(patientIDs);
        Vector<String> uniqueCentreIDs = ci.getCentreIDs(patientCentres);
                
        //Compile the EUSIG table of events
        BrainITDriver bitd = new BrainITDriver(logger);
        EUSIGTable eusigTable = bitd.compileEUSIGTable();
        Vector<EUSIGParameter> eusigParameters = eusigTable.getEUSIGParameters();
        int eusigParamNum = eusigParameters.size();
               
        //Run for each of the four hold-down definitions (mins)
        int[] holdDownDefinition = {5, 10, 15, 20};
        int holdDownDefNum = holdDownDefinition.length;
        
        //Run for each of the time-window definitions (mins)
        int[] windowDefinition = {30, 60, 90, 120};
        int windowDefNum = windowDefinition.length;
        
        //Initialise the list of association data objects
        Vector<AssociationData> ads = new Vector<AssociationData>();
        
        //For each patient:
        for (int i = 0; i < PATIENT_NUM; i++) {      
                        
            logger.info("--- Patient #" + (i + 1) + " ---");
            String patientID = patientIDs.get(i);
            logger.info("--- " + patientID + " ---");
            String centreID = (String) patientCentres.get(patientID);

            //Initialise the association objects
            AssociationData ad = new AssociationData(logger,patientID,centreID);            
            Associate assoc = new Associate(logger, ad, FIRST_TREATMENT_ONLY);
            
            //Get treatments
            Vector<Treatment> treatments = ld.getTreatments(filepath, patientID);
            int treatmentNumPrint = treatments.size();
            //logger.info("Number of treatments: " + treatmentNumPrint);
            ad.setTotalTreatments(treatments);
            
            //Run this for each feed
            for (int j = 0; j < eusigParamNum; j++) {

                EUSIGParameter eusigParamIn = eusigParameters.get(j);
                String paramToView = eusigParamIn.getParameter();
                String condition = eusigParamIn.getName();
                String units = eusigParamIn.getUnit();
                String threshold = "" + eusigParamIn.getGradeThreshold();
                //logger.info("Name - " + condition);
                //logger.info("Parameter - " + paramToView);

                boolean comparatorGreater = eusigParamIn.getComparatorGreater();
                
                double officialThreshold = (double) eusigParamIn.getGradeThreshold();
                String conditionStr = "";
                conditionStr = "Threshold value: ";
                if (comparatorGreater) {
                    conditionStr += ">";
                } else {
                    conditionStr += "<";
                }
                conditionStr += " " + officialThreshold + " " + units;
                //logger.info(conditionStr);

                //logger.info("-----");

                //Run for each hold-down definition
                for (int m = 0; m < holdDownDefNum; m++) {

                    //logger.info("Hold-down value: " + holdDownDefinition[m] + " mins");
                                        
                    //Get events
                    String parameterFeed = condition + "_" + threshold;
                    Vector<Event> events = ld.getEvents(parameterFeed, patientID,(""+holdDownDefinition[m]));
                    int eventNum = events.size();                    
                    //logger.info("Number of events: " + eventNum);
                    String holdDownLabel = "";
                    if(holdDownDefinition[m] == 5){
                        holdDownLabel = "05";
                    }else{
                        holdDownLabel = "" + holdDownDefinition[m];
                    }
                    String eventListLabel = condition + "_" + holdDownLabel;
                    ad.setEventList(events, eventListLabel);
                    
                    //Run for each window definition
                    for(int n=0; n < windowDefNum; n++){
                        //logger.info("Window definition #" + (n+1) + ": " + windowDefinition[n] + " mins");
                        //Associate these
                        assoc.associate(treatments, events, patientID, condition, (""+holdDownDefinition[m]), ("" + windowDefinition[n]), n);                                                                        
                    }
                    
                    //logger.info("-----");
                }
                //logger.info("=====");
            }
            //Now print out the association data to the logfile
            ad = assoc.getAssociationData();
            ads.add(ad);
        }        
        
        /*logger.info("Printing association information...");
        PrintAssociationData pad = new PrintAssociationData(logger, FIRST_TREATMENT_ONLY, uniqueCentreIDs);
        pad.printData(ads);*/

        logger.info("---- EVENT TREATMENT ASSOCIATION ENDS ----");
    }
}
