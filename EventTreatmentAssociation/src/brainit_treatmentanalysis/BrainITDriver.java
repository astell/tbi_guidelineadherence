/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brainit_treatmentanalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.PreparedStatement;

import java.util.Vector;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

/**
 *
 * @author astell
 */
public class BrainITDriver {

    SimpleDateFormat df,dfIcca = null;
    Logger logger = null;

    public BrainITDriver(Logger _logger) {
        logger = _logger;
        //df = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        //df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //dfIcca = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        dfIcca = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public Connection getConnection(String dbName) {

        /*Connection conn = null;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
            String filename = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\BrainIT_2011\\" + dbName;
            String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
            database += filename.trim() + ";DriverID=22;READONLY=true}";
            conn = DriverManager.getConnection(database, "", "");
            logger.info("Successful connection to BrainIT database...");
        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
        return conn;*/
        Connection conn = null;
        String connectionURL = "jdbc:mysql://localhost:3306/" + dbName;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(connectionURL, "root", "ps4Xy2a");
        } catch (Exception e) {
            logger.debug("Database connection error: " + e.getMessage());
        }
        return conn;
    }

    public Vector<String> getPatientList(Connection conn, String patientNum, boolean icca, boolean mimic) {

        Vector<String> patients = new Vector<String>();

        if (!icca) {
            
            if(!mimic){
                        
            try {
                //String sql = "SELECT Patient_Id FROM Demographic;";
                String sql = "SELECT patient_id FROM demographic LIMIT " + patientNum + ";";

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    patients.add("" + rs.getString(1));
                }

            } catch (Exception e) {
                logger.info("Error: " + e.getMessage());
            }
            
            }else{
                
                String foldername = "/home/ubuntu/mimic_data/subject_physio_output_v1/";
                
                File folderName = new File(foldername);
                if (folderName.exists() && folderName.isDirectory()) {
                    File[] files = folderName.listFiles();
                    int fileNum = files.length;
                    int fileCount = 0;
                    while (fileCount < fileNum) {

                        String fileNameIn = files[fileCount].getName();
                        int underscoreIndex = fileNameIn.indexOf("_");
                        String patientID = fileNameIn.substring(0,underscoreIndex);
                        if(!patients.contains(patientID)){
                            patients.add(patientID);
                        }
                        fileCount++;
                    }
                }
            }
        } else {

            String foldername = "/home/ubuntu/icca_data/";

            File folderName = new File(foldername);
            if (folderName.exists() && folderName.isDirectory()) {
                File[] files = folderName.listFiles();
                int fileNum = files.length;
                int fileCount = 0;
                while (fileCount < fileNum) {

                    String fileNameIn = files[fileCount].getName();
                    if (fileNameIn.contains(".csv")) {
                        int csvIndex = fileNameIn.indexOf(".csv");
                        String patientID = fileNameIn.substring(0, csvIndex);
                        if(!patientID.equalsIgnoreCase("vent") && !patientID.equalsIgnoreCase("meds")){
                            patients.add(patientID);
                        }
                    }
                    fileCount++;
                }
            }
        }
        return patients;

        /*Vector<String> patients = new Vector<String>();
        try {
            //String sql = "SELECT Patient_Id FROM Demographic;";
            String sql = "SELECT Patient_Id FROM demographic;";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String patientIn = rs.getString(1);
                if(!patients.contains(patientIn)){
                    patients.add(patientIn);
                }                
            }

        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
        
        return patients;*/
    }

    public void addPatients(Vector<String> patientIDs, Connection conn,boolean icca,boolean mimic) {

        String datasetId = "";
        if(icca){
            datasetId = "4";
        }else if(mimic){
            datasetId = "5";
        }else{
            datasetId = "1";
        }
        
        try {
            int patientNum = patientIDs.size();
            String sql = "";
            /*for(int i=0; i<2; i++){
                sql += "INSERT INTO Patient VALUES(?,3);";
            }*/
            for (int i = 0; i < patientNum; i++) {
                sql = "INSERT INTO patient VALUES(?,?);";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, patientIDs.get(i));
                ps.setString(2, datasetId);
                int update = ps.executeUpdate();
            }
        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
    }

    public TreeMap getPhysioTrace(String[] parameters, String patientID, Connection conn) {

        Utility utilities = new Utility();
        int paramNum = parameters.length;
        //Vector<Physiological> physioTrace = new Vector<Physiological>();
        TreeMap physioTrace = new TreeMap();

        String outputFileName = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\BrainIT_TreatmentAnalysis\\output\\" + patientID + "_physio_summary.csv";

        try {
            FileWriter fw = new FileWriter(outputFileName, true);
            BufferedWriter bw = new BufferedWriter(fw);

            String sql = "SELECT ";
            //sql += "CPP,BPm,";
            for (int i = 0; i < paramNum; i++) {
                sql += "" + parameters[i] + ",";
            }
            sql += "Time_Stamp FROM Physiological ";
            sql += "WHERE Physiological.Patient_Id = '" + patientID + "';";

            //sql = "SELECT Physiological.Time_Stamp, Physiological.HRT, Physiological.RR, Physiological.BPs, Physiological.BPd,      Physiological.BPm, Physiological.ICPm, Physiological.CPP,      Physiological.TC, Physiological.SaO2 "; //sql += "FROM Physiological      WHERE (((Physiological.Patient_Id)=\"15127262\"));";
            //logger.info("sql: " + sql);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int recordCount = 0;
            while (rs.next()) {
                String[] valuesIn = new String[paramNum];

                //logger.info("tempValueIn: " + rs.getString(1));
                for (int i = 0; i < paramNum; i++) {
                    String valueIn = rs.getString(i + 1);
                    if (valueIn == null) {
                        valueIn = "-1.0";
                    }
                    valuesIn[i] = valueIn;
                }
                //String tempValueIn = rs.getString(3);
                //logger.info("tempValueIn: " + tempValueIn);
                String[] types = parameters;
                String timestampIn = rs.getString(paramNum + 1);
                //String timestampIn = ""; //System.out.println("valueIn: " + valueIn);
                //System.out.println("timestampIn: " + timestampIn);

                java.util.Date timestampDate = df.parse(timestampIn);
                Physiological physioIn = new Physiological(valuesIn, types, timestampDate);

                //physioTrace.add(physioIn);
                physioTrace.put(timestampDate, physioIn);
                recordCount++;
            }
            logger.info("Physiological output duration: " + recordCount + " minutes");
            bw.write("Physiological output duration: " + recordCount + " minutes\n");
            bw.write("\n");

            /*int physioNum = physioTrace.size();
            for(int i=0; i<physioNum; i++){
                Physiological physioIn = physioTrace.get(i);
                if(i == 0){
                    String[] params = physioIn.getParameters();
                    bw.write("Timestamp, ");
                    for(int j=0; j<params.length; j++){
                        if(j != params.length-1){
                            bw.write(params[j] + ", ");
                        }else{
                            bw.write(params[j] + "\n");
                        }
                    }                    
                }                
                
                String timestampIn = physioIn.getTimestamp().toString();
                String timestampPrint = utilities.convertBackToLiterals(timestampIn);
                timestampPrint = utilities.formatTimestamp(timestampPrint);
                
                String[] values = physioIn.getValues();                
                
                bw.write("" + timestampPrint + ", ");
                for(int j=0; j<values.length; j++){
                    if(j != values.length-1){
                        bw.write("" + values[j] + ", ");
                    }else{
                        bw.write("" + values[j]);
                    }
                }
                bw.write("\n");                
            }*/
            Set timestampSet = physioTrace.keySet();
            Iterator timestampIter = timestampSet.iterator();
            int paramCount = 0;
            while (timestampIter.hasNext()) {
                java.util.Date timestamp = (java.util.Date) timestampIter.next();
                Physiological physioIn = (Physiological) physioTrace.get(timestamp);

                if (paramCount == 0) {
                    String[] params = physioIn.getParameters();
                    bw.write("Timestamp, ");
                    for (int j = 0; j < params.length; j++) {
                        if (j != params.length - 1) {
                            bw.write(params[j] + ", ");
                        } else {
                            bw.write(params[j] + "\n");
                        }
                    }
                }

                String timestampIn = physioIn.getTimestamp().toString();
                String timestampPrint = utilities.convertBackToLiterals(timestampIn);
                timestampPrint = utilities.formatTimestamp(timestampPrint);

                String[] values = physioIn.getValues();

                bw.write("" + timestampPrint + ", ");
                for (int j = 0; j < values.length; j++) {
                    if (j != values.length - 1) {
                        bw.write("" + values[j] + ", ");
                    } else {
                        bw.write("" + values[j]);
                    }
                }
                bw.write("\n");

                paramCount++;
            }

            bw.close();
            fw.close();
        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
        return physioTrace;
    }

    public Vector getTreatmentSummary(String patientID, Connection conn, Connection newConn, boolean icca, boolean mimic) {

        Utility utilities = new Utility();
        Vector<Treatment> treatments = new Vector<Treatment>();

        String foldername = "/home/ubuntu/icca_data/";
        String mimicFoldername = "/home/ubuntu/mimic_data/";
        
        try {

            int overallTreatmentNum = 0;
            if (!icca) {
                
                if(!mimic){

                //Define the array of treatment types here
                String[] treatmentTypes = this.getTreatmentTypes();
                int treatmentTypeNum = treatmentTypes.length;

                for (int i = 0; i < treatmentTypeNum; i++) {

                    String treatmentParam = "" + treatmentTypes[i];
                    //logger.info("treatmentParam: " + treatmentParam);
                    treatmentParam = "`" + treatmentParam + "`";
                    String sql = "SELECT target_therapies.Time_Stamp, ";
                    if (!treatmentParam.equals("Other_Therapy")) {
                        sql += "target_therapies." + treatmentParam + ", ";
                        sql += "target_therapies." + treatmentParam + "_Target, ";
                        sql += "target_therapies." + treatmentParam + "_Target_other ";
                    } else {
                        sql += "target_therapies." + treatmentParam + ", ";
                        sql += "target_therapies." + treatmentParam + "_Name, ";
                        sql += "target_therapies." + treatmentParam + "_Target ";
                    }
                    sql += "FROM target_therapies WHERE Patient_Id='" + patientID + "' ";
                    sql += " AND target_therapies." + treatmentParam + " NOT LIKE '';";

                    logger.info("sql (getTreatmentSummary): " + sql);

                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);

                    int recordCount = 0;
                    while (rs.next()) {

                        String valueIn = treatmentTypes[i];

                        String timestampIn = rs.getString(1);
                        java.util.Date timestampDate = df.parse(timestampIn);
                        String timestampPrint = utilities.convertBackToLiterals(timestampDate.toString());
                        timestampPrint = utilities.formatTimestamp(timestampPrint);

                        String targetIn = rs.getString(3);
                        if (targetIn == null) {
                            targetIn = "";
                        }
                        String descOtherIn = rs.getString(4);
                        if (descOtherIn == null) {
                            descOtherIn = "";
                        }
                        String descIn = rs.getString(2) + " " + descOtherIn;
                        descIn = descIn.trim();

                        Treatment treatmentIn = new Treatment(valueIn, descIn, targetIn, timestampDate);
                        treatments.add(treatmentIn);

                        recordCount++;
                    }
                    logger.info("" + treatmentParam + ": " + recordCount);
                    overallTreatmentNum += recordCount;

                    stmt.close();
                }
                
                }else{
                    
                    //Read in the codes
                    Vector<Vector> icdCodes = new Vector<Vector>();
                    
                    String icdCodeFilename = mimicFoldername + "Item_IDs_TBI.txt";
                    FileReader fr = new FileReader(icdCodeFilename);
                    BufferedReader br = new BufferedReader(fr);
                    
                    while(br.ready()){
                        String lineIn = br.readLine();
                        int hyphenIndex = lineIn.indexOf(" - ");
                        int lastHyphenIndex = lineIn.lastIndexOf(" - ");
                        
                        if(hyphenIndex != -1 && lastHyphenIndex != -1 && (hyphenIndex != lastHyphenIndex)){
                            Vector<String> thisCode = new Vector<String>();
                            String codeNum = lineIn.substring(0,hyphenIndex);
                            String codeDesc = lineIn.substring(hyphenIndex+3,lastHyphenIndex);
                            thisCode.add(codeNum);
                            thisCode.add(codeDesc);
                            icdCodes.add(thisCode);
                        }
                    }
                    
                    /*for(int i=0; i<icdCodes.size(); i++){
                        Vector<String> thisCodeIn = icdCodes.get(i);
                        logger.info("thisCodeIn: " + thisCodeIn);
                    }*/
                    
                    
                    //For each code, check if there's a file in the other folder, then add the details (one line per treatment)
                    String mimicTreatmentFolder = mimicFoldername + "subject_physio_treatment_output/";
                    for(int i=0; i<icdCodes.size(); i++){
                        Vector<String> thisCodeIn = icdCodes.get(i);
                        String filenameToTest = mimicTreatmentFolder + patientID + "_" + thisCodeIn.get(0) + ".txt";                        
                        try{
                            FileReader frTest = new FileReader(filenameToTest);
                            BufferedReader brTest = new BufferedReader(frTest);
                            
                            while(brTest.ready()){
                                String lineIn = brTest.readLine();
                                lineIn = lineIn.trim();
                                int hyphenIndex = lineIn.indexOf(" - ");
                                
                                String timestamp = "";
                                String type = "";                    
                                String desc = "";
                                
                                if(hyphenIndex != -1){
                                    
                                    timestamp = lineIn.substring(0,hyphenIndex);
                                    type = lineIn.substring(hyphenIndex+3,lineIn.length());
                                    desc = thisCodeIn.get(1);
                                
                                    /*logger.info("timestamp: " + timestamp);
                                    logger.info("type: " + type);
                                    logger.info("desc: " + desc);*/
                        
                                    java.util.Date timestampDate = dfIcca.parse(timestamp);
                                    String valueIn = type;
                                    String descIn = desc;
                                    String targetIn = "";
                        
                                    Treatment treatmentIn = new Treatment(valueIn, descIn, targetIn, timestampDate);
                                    treatments.add(treatmentIn);
                                    overallTreatmentNum++;
                                }
                            }
                        }catch(Exception e){
                            //logger.info("I/O error: " + e.getMessage());
                        }
                    }
                }
            }else{                
                //String pidEncounterId = this.getEncounterId(patientID);
                
                String[] treatmentFileNames = {"vent","meds"};
                
                for(int i=0; i<treatmentFileNames.length; i++){
                    
                    String treatmentFilename = foldername + treatmentFileNames[i] + ".csv";
                    FileReader fr = new FileReader(treatmentFilename);
                    BufferedReader br = new BufferedReader(fr);
                    
                    br.readLine(); //Skip the header line
                    while(br.ready()){
                        String lineIn = br.readLine();
                        StringTokenizer st = new StringTokenizer(lineIn,",");
                        int tokenNum = st.countTokens();
                        boolean treatmentMatch = false;
                        String timestamp = "";
                        String type = "";                    
                        String desc = "";
                        if(tokenNum > 1){
                            String tokenIn = st.nextToken();
                            treatmentMatch = patientID.equalsIgnoreCase(tokenIn.trim());
                        }
                        
                        if(treatmentMatch){
                            
                            if(treatmentFileNames[i].equals("meds")){
                            
                                int tokenCount = 0;
                                while(st.hasMoreTokens()){
                                    String tokenIn = st.nextToken();
                                    if(tokenCount == 0){
                                        timestamp = tokenIn.trim();
                                    }else if(tokenCount == 1){
                                        type = tokenIn.trim();
                                    }
                                    tokenCount++;
                                }                            
                            }else{
                                int tokenCount = 0;
                                while(st.hasMoreTokens()){
                                    String tokenIn = st.nextToken();
                                    if(tokenCount == 0){
                                        timestamp = tokenIn.trim();
                                    }else if(tokenCount == 3){
                                        desc = tokenIn.trim();
                                    }
                                    tokenCount++;
                                }
                                type = "Ventilation";
                            }
                        
                            logger.info("timestamp: " + timestamp);
                            logger.info("type: " + type);
                            logger.info("desc: " + desc);
                        
                            java.util.Date timestampDate = dfIcca.parse(timestamp);
                            String valueIn = type;
                            String descIn = desc;
                            String targetIn = "";
                        
                            Treatment treatmentIn = new Treatment(valueIn, descIn, targetIn, timestampDate);
                            treatments.add(treatmentIn);
                            overallTreatmentNum++;
                        }                        
                    }
                }
            }
            logger.info("----");
            logger.info("Total treatment number for " + patientID + ": " + overallTreatmentNum);

            //SORT THE TREATMENTS VECTOR CHRONOLOGICALLY
            SortTreatments sortTreat = new SortTreatments(logger);
            treatments = sortTreat.sort(treatments);

            //Now write the treatment details to the summary file
            int treatmentNum = treatments.size();
            for (int i = 0; i < treatmentNum; i++) {
                Treatment treatment = treatments.get(i);
                String timestampIn = treatment.getTimestamp().toString();
                if (timestampIn == null) {
                    timestampIn = "";
                }
                String timestampPrint = utilities.convertBackToLiterals(timestampIn);
                timestampPrint = utilities.formatTimestamp(timestampPrint);

                String valueIn = treatment.getValue();
                if (valueIn == null) {
                    valueIn = "";
                }
                String descIn = treatment.getDescription();
                if (descIn == null) {
                    descIn = "";
                }
                String targetIn = treatment.getTarget();
                if (targetIn == null) {
                    targetIn = "";
                }

                //MODIFY THIS BIT TO OUTPUT TO THE NEW DATABASE
                String updateSql = "INSERT INTO treatment VALUES(?,?,?,?,?,?);";
                PreparedStatement ps = newConn.prepareStatement(updateSql);
                ps.setInt(1, i);
                ps.setString(2, patientID);
                ps.setString(3, timestampPrint);
                ps.setString(4, valueIn);
                ps.setString(5, descIn);
                ps.setString(6, targetIn);

                int update = ps.executeUpdate();
            }
        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
        return treatments;
    }
    
    private String getEncounterId(String pid){
        
        String encounterId = "";
        if(pid.equals("P02_lo")){
            encounterId = "3120";
        }else if(pid.equals("P09_lo")){
            encounterId = "1007";
        }
        return encounterId;        
    }

    private String[] getTreatmentTypes() {
        String[] treatmentTypes = {
            "Ventilation",
            "Sedation",
            "Analgesia",
            "Paralysis",
            "Volume_Expansion",
            "Inotropes",
            "Anti-Hypertensives",
            "Anti-Pyretics",
            "Hypothermia",
            "Steroid_Therapy",
            "Cerebral_Vasoconstriction",
            "Osmotic_Therapy",
            "CSF_Drainage",
            "Head_Elevation",
            "Barbiturate_Therapy",
            "Other_Therapy"
        };
        return treatmentTypes;
    }
}
