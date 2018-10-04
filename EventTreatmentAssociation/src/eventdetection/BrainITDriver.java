/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventdetection;

import brainit_treatmentanalysis.Physiological;
import brainit_treatmentanalysis.Utility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import java.util.Vector;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 *
 * @author astell
 */
public class BrainITDriver {

    SimpleDateFormat df = null;
    Logger logger = null;

    public BrainITDriver(Logger _logger) {
        logger = _logger;
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    public void addPatients(Vector<String> patientIDs, Connection conn) {

        logger.info("into addPatients...");

        try {
            int patientNum = patientIDs.size();
            //int patientNum = 2;
            //String sql = "";            
            for (int i = 0; i < patientNum; i++) {
                String sql = "INSERT INTO patient VALUES(?,?);";

                logger.info("sql: " + sql);
                PreparedStatement ps = conn.prepareStatement(sql);

                String patientIDIn = patientIDs.get(i);
                int patientIDint = -1;
                patientIDint = Integer.parseInt(patientIDIn);

                /*int updateIndex1 = ((2 * i) + 1);
                int updateIndex2 = ((2 * i) + 2);
                logger.info("updateIndex1: " + updateIndex1);
                logger.info("updateIndex2: " + updateIndex2);*/
                logger.info("patientIDint: " + patientIDint);

                ps.setInt(1, patientIDint);
                ps.setInt(2, 1);

                int update = ps.executeUpdate();
                ps.close();
            }
            //int update = ps.executeUpdate();            
        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
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
                    if(fileNameIn.contains(".csv")){
                        int csvIndex = fileNameIn.indexOf(".csv");
                        String patientID = fileNameIn.substring(0,csvIndex);                        
                        if(!patientID.equalsIgnoreCase("vent") && !patientID.equalsIgnoreCase("meds")){                        
                            patients.add(patientID);
                        }
                    }
                    fileCount++;
                }
            }
        }
        return patients;
    }

    /* Commenting out this original table definition (raised ICP and lowered CPP only) */
 /*public EUSIGTable compileEUSIGTable(){
        
        EUSIGParameter icp = new EUSIGParameter("Raised ICP","ICPm","mmHg",20,30,40,true);
        //EUSIGParameter hypo = new EUSIGParameter("Hypotension","BPs","mmHg",90,70,50,false); //NOTE: should use BPm as well
        //EUSIGParameter hyper = new EUSIGParameter("Hypertension","BPs","mmHg",160,190,220,true); //NOTE: should use BPm as well
        EUSIGParameter cpp = new EUSIGParameter("Lowered CPP","CPP","mmHg",60,50,40,false);
        //EUSIGParameter hypox = new EUSIGParameter("Hypoxemia","SaO2","%",90,85,80,false);
        //EUSIGParameter pyrex = new EUSIGParameter("Pyrexia","TC","C",38,39,40,true); //NOTE: has a hold-down of 60 mins
        //EUSIGParameter tachy = new EUSIGParameter("Tachycardia","HRT","bpm",120,135,150,true);
        //EUSIGParameter brady = new EUSIGParameter("Bradycardia","HRT","bpm",50,40,30,false);
        
        Vector<EUSIGParameter> eusigParams = new Vector<EUSIGParameter>();
        eusigParams.add(icp);
        //eusigParams.add(hypo);
        //eusigParams.add(hyper);
        eusigParams.add(cpp);
        //eusigParams.add(hypox);
        //eusigParams.add(pyrex);
        //eusigParams.add(tachy);
        //eusigParams.add(brady);
        
        EUSIGTable eusig = new EUSIGTable(eusigParams);
        
        return eusig;
    }*/
    public EUSIGTable compileEUSIGTable() {

        //New definition changes require five definitions of grade 1 ICP threshold (ignores the grade2 and grade3 definitions (hence the -1))
        EUSIGParameter icp1 = new EUSIGParameter("Raised ICP #1", "ICPm", "mmHg", 10, true);
        EUSIGParameter icp2 = new EUSIGParameter("Raised ICP #2", "ICPm", "mmHg", 15, true);
        EUSIGParameter icp3 = new EUSIGParameter("Raised ICP #3", "ICPm", "mmHg", 20, true);
        EUSIGParameter icp4 = new EUSIGParameter("Raised ICP #4", "ICPm", "mmHg", 25, true);
        EUSIGParameter icp5 = new EUSIGParameter("Raised ICP #5", "ICPm", "mmHg", 30, true);

        EUSIGParameter cpp1 = new EUSIGParameter("Lowered CPP #1", "CPP", "mmHg", 50, false);
        EUSIGParameter cpp2 = new EUSIGParameter("Lowered CPP #2", "CPP", "mmHg", 60, false);
        EUSIGParameter cpp3 = new EUSIGParameter("Lowered CPP #3", "CPP", "mmHg", 70, false);

        Vector<EUSIGParameter> eusigParams = new Vector<EUSIGParameter>();
        eusigParams.add(icp1);
        eusigParams.add(icp2);
        eusigParams.add(icp3);
        eusigParams.add(icp4);
        eusigParams.add(icp5);

        eusigParams.add(cpp1);
        eusigParams.add(cpp2);
        eusigParams.add(cpp3);

        EUSIGTable eusig = new EUSIGTable(eusigParams);

        return eusig;
    }

    public EUSIGTable getEUSIGTable(Connection conn) {

        Vector<EUSIGParameter> eusigParams = new Vector<EUSIGParameter>();
        try {
            String sql = "SELECT * FROM eusig_defn WHERE series='ICPm' OR series='CPP';";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            int colNum = rsmd.getColumnCount();

            while (rs.next()) {
                Vector<String> eusigParamIn = new Vector<String>();
                for (int i = 0; i < colNum; i++) {
                    String eusigParamValueIn = rs.getString(i + 1);
                    eusigParamIn.add(eusigParamValueIn);
                }
                EUSIGParameter ep = new EUSIGParameter(eusigParamIn.get(0), eusigParamIn.get(1), eusigParamIn.get(3), Float.parseFloat(eusigParamIn.get(2)), Boolean.parseBoolean(eusigParamIn.get(4)));
                eusigParams.add(ep);
            }

        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }

        //New definition changes require five definitions of grade 1 ICP threshold (ignores the grade2 and grade3 definitions (hence the -1))
        /*EUSIGParameter icp1 = new EUSIGParameter("Raised ICP #1","ICPm","mmHg",10,true);
        EUSIGParameter icp2 = new EUSIGParameter("Raised ICP #2","ICPm","mmHg",15,true);
        EUSIGParameter icp3 = new EUSIGParameter("Raised ICP #3","ICPm","mmHg",20,true);
        EUSIGParameter icp4 = new EUSIGParameter("Raised ICP #4","ICPm","mmHg",25,true);
        EUSIGParameter icp5 = new EUSIGParameter("Raised ICP #5","ICPm","mmHg",30,true);
        
        EUSIGParameter cpp1 = new EUSIGParameter("Lowered CPP #1","CPP","mmHg",50,false);
        EUSIGParameter cpp2 = new EUSIGParameter("Lowered CPP #2","CPP","mmHg",60,false);
        EUSIGParameter cpp3 = new EUSIGParameter("Lowered CPP #3","CPP","mmHg",70,false);
                
        eusigParams.add(icp1);
        eusigParams.add(icp2);
        eusigParams.add(icp3);
        eusigParams.add(icp4);
        eusigParams.add(icp5);
        
        eusigParams.add(cpp1);
        eusigParams.add(cpp2);
        eusigParams.add(cpp3);*/
        EUSIGTable eusig = new EUSIGTable(eusigParams);

        return eusig;
    }

    public Vector<Vector> getPhysioLines(String patientID, Connection conn, boolean icca, boolean mimic) {

        Vector<Vector> physioLines = new Vector<Vector>();
        
        if(icca){

        //Open the spreadsheet file for reading (using the patientID)
        FileReader fr = null;
        BufferedReader br = null;
        //String filepath = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\ICCA Platform\\";
        String filepath = "/home/ubuntu/icca_data/";
        String filename = filepath + patientID + ".csv";

        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);

            //Line 1 is the physiological header values (use the paramsToView array to select the column number)
            br.readLine();

            while (br.ready()) {
                Vector<String> lineValues = new Vector<String>();
                String lineIn = br.readLine();

                //Ensure the empty commas are separated by blanks (which will then be added to the Vector<String>)
                StringBuffer sb = new StringBuffer("");
                char lastCharIn = ' ';
                for (int j = 0; j < lineIn.length(); j++) {
                    char charIn = lineIn.charAt(j);
                    if (lastCharIn == ',' && charIn == ',') {
                        sb.append(" " + charIn);
                    } else {
                        sb.append(charIn);
                    }
                    lastCharIn = charIn;
                    if (j == lineIn.length() - 1) {
                        sb.append(' ');
                    }
                }
                String lineOut = sb.toString();
                StringTokenizer st = new StringTokenizer(lineOut, ",");

                while (st.hasMoreTokens()) {
                    lineValues.add(st.nextToken());
                }
                physioLines.add(lineValues);
            }
        } catch (IOException ioe) {
            logger.info("I/O error: " + ioe.getMessage());
        }
        
        }else{
            
            if(mimic){
                
                String foldername = "/home/ubuntu/mimic_data/subject_physio_output_v1/";
                
                //For this patient, go through the two files (ICP and CPP) and compile the physio lines
                String[] icdCodes = {"226","92"};                
                HashMap timestamps = new HashMap();
                for(int i=0; i<icdCodes.length;i++){
                    
                    String filename = foldername + "" + patientID + "_" + icdCodes[i] + ".txt";
                    try{
                        FileReader fr = new FileReader(filename);
                        BufferedReader br = new BufferedReader(fr);
                        
                        while(br.ready()){
                            String lineIn = br.readLine();
                            lineIn = lineIn.trim();
                            int spacerIndex = lineIn.indexOf(" - ");
                            String timestamp = lineIn.substring(0,spacerIndex);
                            String value = lineIn.substring(spacerIndex+3,lineIn.length());
                            
                            String lineValues = new String();
                            if(timestamps.containsKey(timestamp)){
                                lineValues = (String) timestamps.get(timestamp);
                                lineValues += "|" + value;
                            }else{
                                lineValues = value;
                            }                            
                            timestamps.put(timestamp, lineValues);
                        }

                        br.close();
                        fr.close();
                        
                    }catch(Exception e){
                        logger.info("I/O error: " + e.getMessage());
                    }
                    
                }
                
                Set timestampSet = timestamps.keySet();
                Iterator timestampIter = timestampSet.iterator();
                while(timestampIter.hasNext()){
                    String timestampKey = (String) timestampIter.next();
                    String lineValuesStr = (String) timestamps.get(timestampKey);
                    int pipeIndex = lineValuesStr.indexOf("|");
                    String value1 = "";
                    String value2 = "";
                    if(pipeIndex != -1){
                        value1 = lineValuesStr.substring(0,pipeIndex);
                        value2 = lineValuesStr.substring(pipeIndex+1,lineValuesStr.length());
                    }else{
                        if(lineValuesStr.length() > 1){
                            value1 = lineValuesStr;
                        }
                    }
                    Vector<String> lineValues = new Vector<String>();
                    lineValues.add(timestampKey);
                    lineValues.add(value1);
                    lineValues.add(value2);
                    physioLines.add(lineValues);
                }
                
            }else{
            
                try {
                    String sql = "SELECT Time_Stamp,ICPm,HRT,TC,BPm,SaO2,BPs,BPd,CPP,RR,SaO2pls FROM physiological WHERE Patient_Id=?;";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1,patientID);
                    ResultSet rs = ps.executeQuery();
                
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int colNum = rsmd.getColumnCount();

                    while (rs.next()) {                    
                        Vector<String> lineValues = new Vector<String>();
                        for(int i=0; i<colNum; i++){
                            String valueIn = rs.getString(i+1);
                            if(valueIn == null){
                                valueIn = "";
                            }
                            lineValues.add(valueIn);
                        }
                        physioLines.add(lineValues);
                    }
                } catch (Exception e) {
                    logger.info("Error: " + e.getMessage());
                }
            }
        }
        return physioLines;
    }

    public Vector<Vector> averageInputs(Vector<Vector> physioLines) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        Vector<Vector> physioLinesOut = new Vector<Vector>();

        int lineNum = physioLines.size();
        //int lineNum = 100;
        
        Vector<String> lineOut = new Vector<String>();
        Vector<Vector> valuesOut = new Vector<Vector>();
        for (int i = 0; i < lineNum; i++) {

            //Read the line in
            Vector<String> lineIn = physioLines.get(i);

            //Get the timestamp element
            java.util.Date timestampDate = null;
            try {
                String timestampStrIn = lineIn.get(0);
                timestampStrIn = timestampStrIn.trim();
                if(timestampStrIn.contains("\"")){
                    timestampStrIn = timestampStrIn.substring(1,timestampStrIn.length()-1);
                }
                if(timestampStrIn.contains("/")){
                    int dividerIndex = timestampStrIn.indexOf("/");
                    int dividerIndex2 = timestampStrIn.lastIndexOf("/");
                    int spaceIndex = timestampStrIn.indexOf(" ");
                    
                    String dayIn = timestampStrIn.substring(0,dividerIndex);
                    String monthIn = timestampStrIn.substring(dividerIndex+1,dividerIndex2);
                    String yearIn = timestampStrIn.substring(dividerIndex2+1,spaceIndex);
                    String timeIn = timestampStrIn.substring(spaceIndex+1,timestampStrIn.length());
                    timestampStrIn = yearIn + "-" + monthIn + "-" + dayIn + " " + timeIn;
                }
                if(timestampStrIn.contains("T")){
                    timestampStrIn = timestampStrIn.replace('T', ' ');
                    timestampStrIn = timestampStrIn.substring(0,timestampStrIn.length()-5);
                }
                
                //timestampDate = df.parse(lineIn.get(0));
                /*if(timestampStrIn.contains("14:38:00")){                
                    logger.info("timestampStrIn: " + timestampStrIn);
                }*/
                timestampDate = df.parse(timestampStrIn);
                /*if(timestampStrIn.contains("14:38:00")){                
                    logger.info("timestampDate: " + timestampDate);
                }*/
            } catch (Exception e) {
                logger.info("Parse error: " + e.getMessage());
            }
            String timestamp = sdf.format(timestampDate) + ":00";
            //logger.info(timestamp);
            if (i == 0) {
                lineOut.add(timestamp);
                Vector<String> thisValuesOut = new Vector<String>();
                for (int j = 1; j < lineIn.size(); j++) { //Start from element 1 to skip the timestamp
                    //lineOut.add("");
                    thisValuesOut.add(lineIn.get(j));
                }
                valuesOut.add(thisValuesOut);
            } else {

                if (!lineOut.contains(timestamp)) {

                    //logger.info("line (averaging clause)" + i);
                    //Then we're onto a new line - so need to average and complete the old one first
                    Vector<String> avgValuesOut = new Vector<String>();
                    Vector<String> valuesOutColCheck = valuesOut.get(0);
                    int colNum = valuesOutColCheck.size();
                    for(int k = 0; k < colNum; k++){
                        double valueTotal = 0.0;
                        for (int j = 0; j < valuesOut.size(); j++) {
                            Vector<String> thisValuesOut = valuesOut.get(j);
                            try {
                                valueTotal += (double) Double.parseDouble(thisValuesOut.get(k));
                            } catch (Exception e) {
                                valueTotal += 0.0;
                            }
                        }
                        double avgValueOut = valueTotal / (double) valuesOut.size();
                        avgValuesOut.add("" + avgValueOut);                        
                        /*if(k == 5){
                            logger.info("valueTotal: " + valueTotal);
                            logger.info("valuesOut.size(): " + valuesOut.size());
                            logger.info("avgValueOut: " + avgValueOut);
                        }*/
                        
                    }
                    //logger.info("avgValuesOut: " + avgValuesOut);

                    lineOut.addAll(avgValuesOut);
                    
                    /*if(lineOut.contains("2017-02-22 08:28:00")){
                        logger.info("lineOut (col 6): " + lineOut.get(6));
                    }*/
                    physioLinesOut.add(lineOut);

                    //Then re-initialise for the next set
                    lineOut = new Vector<String>();
                    valuesOut = new Vector<Vector>();
                    lineOut.add(timestamp);
                } else {
                    //logger.info("line (adding clause)" + i);
                    Vector<String> thisValuesOut = new Vector<String>();
                    for (int j = 1; j < lineIn.size(); j++) { //Start from element 1 to skip the timestamp
                        thisValuesOut.add(lineIn.get(j));
                    }
                    valuesOut.add(thisValuesOut);
                }
            }
        }
        return physioLinesOut;
    }

    public String getHeaderLine(String patientID) {

        String headerLine = "";

        //Open the spreadsheet file for reading (using the patientID)
        FileReader fr = null;
        BufferedReader br = null;
        //String filepath = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\BrainIT_TreatmentAnalysis\\output\\";
        String filepath = "/home/ubuntu/brainit_treatmentanalysis/";
        String filename = filepath + "" + patientID + "_physio_summary.csv";

        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);

            //Line 3 will always be the physiological header values (use the paramsToView array to select the column number)
            br.readLine();
            br.readLine();
            headerLine = br.readLine();
            //logger.info("headerLine: " + headerLine);

        } catch (IOException ioe) {
            logger.info("I/O error: " + ioe.getMessage());
        }
        return headerLine;
    }

    public TreeMap getPhysioTrace(String[] parameters, String patientID, Connection conn) {

        //Utility utilities = new Utility();
        int paramNum = parameters.length;
        TreeMap physioTrace = new TreeMap();

        //String outputFileName = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\BrainIT_TreatmentAnalysis\\output\\" + patientID + "_physio_summary.csv";
        try {
            //FileWriter fw = new FileWriter(outputFileName, true);
            //BufferedWriter bw = new BufferedWriter(fw);

            String sql = "SELECT ";
            for (int i = 0; i < paramNum; i++) {
                sql += "" + parameters[i] + ",";
            }
            sql += "Time_Stamp FROM physiological ";
            sql += "WHERE physiological.Patient_Id = '" + patientID + "';";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int recordCount = 0;
            while (rs.next()) {
                String[] valuesIn = new String[paramNum];

                for (int i = 0; i < paramNum; i++) {
                    String valueIn = rs.getString(i + 1);
                    if (valueIn == null) {
                        valueIn = "-1.0";
                    }
                    valuesIn[i] = valueIn;
                }

                String[] types = parameters;
                String timestampIn = rs.getString(paramNum + 1);

                java.util.Date timestampDate = df.parse(timestampIn);
                Physiological physioIn = new Physiological(valuesIn, types, timestampDate);

                physioTrace.put(timestampDate, physioIn);
                recordCount++;
            }
            logger.info("Physiological output duration: " + recordCount + " minutes");
            //bw.write("Physiological output duration: " + recordCount + " minutes\n");
            //bw.write("\n");

            /*Set timestampSet = physioTrace.keySet();
            Iterator timestampIter = timestampSet.iterator();
            int paramCount = 0;
            while(timestampIter.hasNext()){
                java.util.Date timestamp = (java.util.Date) timestampIter.next();
                Physiological physioIn = (Physiological) physioTrace.get(timestamp);
                
                if(paramCount == 0){
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
                
                paramCount++;
            }
            
            bw.close();
            fw.close();*/
        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
        return physioTrace;
    }

}
