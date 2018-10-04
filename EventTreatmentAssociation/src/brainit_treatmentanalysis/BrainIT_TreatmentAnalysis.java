/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brainit_treatmentanalysis;

import java.sql.Connection;
import java.util.Vector;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author astell
 */
public class BrainIT_TreatmentAnalysis {

    private static final Logger logger = Logger.getLogger(BrainIT_TreatmentAnalysis.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Set up logger        
        logger.setLevel(Level.INFO);
        //PropertyConfigurator.configure("C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\ICUChart\\Treatment Profiles\\EventDetection\\config\\log4j_eventdetection.properties");
        PropertyConfigurator.configure("/home/ubuntu/log4j/log4j_eventdetection.properties");

        logger.info("---- BRAINIT_TREATMENTANALYSIS BEGINS ----");
        
        boolean icca = false;
        boolean mimic = true;
        if(icca){
            logger.info("Processing ICCA data: " + icca);
        }else if(mimic){
            logger.info("Processing MIMIC data: " + mimic);
        }

        //Set up connection to database
        String dbName = "brainit";
        String dbNameOut = "treatment_profiles_backup";
        BrainITDriver bitd = new BrainITDriver(logger);
        Connection conn = bitd.getConnection(dbName);

        //Get list of patient IDs
        int PATIENT_NUM_TO_RUN = 262;
        Vector<String> patientIDs = bitd.getPatientList(conn,"" + PATIENT_NUM_TO_RUN,icca,mimic);
        
        int patientNum = patientIDs.size();
        logger.info("Number of patients (total): " + patientNum);
        
        //Put the patient IDs into the new database
        Connection newConn = bitd.getConnection(dbNameOut);
        //bitd.addPatients(patientIDs, newConn,icca,mimic);            

        //For each patient:
        for (int i = 0; i < patientNum; i++) {        
        //for (int i = 0; i < 1; i++) {        
        
            logger.info("--- Patient #" + (i + 1) + " ---");

            String patientID = patientIDs.get(i);     
            logger.info("patientID: " + patientID);
            
            Vector treatments = bitd.getTreatmentSummary(patientID, conn, newConn, icca, mimic);
            
            /*String[] physioParameters = {"ICPm","HRT","TC","BPm","SaO2",
            "BPs","BPd","CPP","RR","SaO2pls"};
            //Vector<Physiological> physios = bitd.getPhysioTrace(physioParameters, patientID, conn);
            TreeMap physios = bitd.getPhysioTrace(physioParameters, patientID, conn);*/
            
        }
    }
}
