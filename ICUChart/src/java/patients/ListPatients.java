package patients;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.lang.Math;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author astell
 */
public class ListPatients {

    private static final Logger logger = Logger.getLogger(ListPatients.class);
    private Vector<String> treatmentWithEventCount, treatmentWithEventWithinWindowCount;

    public ListPatients() {
        treatmentWithEventCount = new Vector<String>();
        treatmentWithEventWithinWindowCount = new Vector<String>();
    }

    public Vector<String> getTreatmentWithEventCount() {
        return treatmentWithEventCount;
    }

    public void setTreatmentWithEventCount(Vector<String> _treatmentWithEventCount) {
        treatmentWithEventCount = _treatmentWithEventCount;
    }

    public Vector<String> getTreatmentWithEventWithinWindowCount() {
        return treatmentWithEventWithinWindowCount;
    }

    public void setTreatmentWithEventWithinWindowCount(Vector<String> _treatmentWithEventWithinWindowCount) {
        treatmentWithEventWithinWindowCount = _treatmentWithEventWithinWindowCount;
    }

    public Vector<Vector> getPatientOutput(Connection sourceConn, int lowerIndex, int upperIndex, int splitRun, int patientNum, Vector<String> patientIds, Vector<Double>[] overallGuidelineIndDurations, Vector<Double>[] overallGuidelineIndDistances) {

        Vector<Vector> patientDetails = new Vector<Vector>();

        Vector<Vector> gosDetails = this.getGosScores(sourceConn, patientIds);

        //int patientNum = patientIds.size();
        int gosSize = gosDetails.size();
        
        logger.debug("patientNum (getPatientOutput): " + patientNum);        
        //for (int i = 0; i < patientNum; i++) {
        for (int i = lowerIndex; i < upperIndex; i++) {

            Vector<String> patientDetail = new Vector<String>();

            String patientId = patientIds.get(i);
            patientDetail.add(patientId);

            logger.debug("patientId (getPatientOutput): " + patientId);
            Integer gosScore = new Integer(-1);
            boolean patientFound = false;
            int patientCount = 0;
            while (!patientFound && patientCount < gosSize) {
                Vector<String> gosDetailIn = gosDetails.get(patientCount);
                String idIn = gosDetailIn.get(0);
                if (idIn.equalsIgnoreCase(patientId)) {
                    gosScore = Integer.parseInt(gosDetailIn.get(2));
                    patientFound = true;
                } else {
                    patientCount++;
                }
            }
            logger.debug("gosScore (getPatientOutput): " + gosScore);
            patientDetail.add("" + gosScore);

            Vector<Double> thisPatientDurations = overallGuidelineIndDurations[i];
            int durationSize = thisPatientDurations.size();
            double durationSum = 0.0;
            for (int j = 0; j < durationSize; j++) {
                durationSum += thisPatientDurations.get(j);
            }
            double avgDuration = durationSum / durationSize;
            avgDuration = Math.round(avgDuration * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + avgDuration);

            Vector<Double> thisPatientDistances = overallGuidelineIndDistances[i];
            int distanceSize = thisPatientDistances.size();
            double distanceSum = 0.0;
            for (int j = 0; j < distanceSize; j++) {
                distanceSum += thisPatientDistances.get(j);
            }
            double avgDistance = distanceSum / distanceSize;
            avgDistance = Math.round(avgDistance * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + avgDistance);

            /*double logAvgDuration = Math.log(avgDuration);
            logAvgDuration = Math.round(logAvgDuration * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + logAvgDuration);

            double logAvgDistance = Math.log(avgDistance);
            logAvgDistance = Math.round(logAvgDistance * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + logAvgDistance);

            double logSum = logAvgDuration + logAvgDistance;
            logSum = Math.round(logSum * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + logSum);*/

            patientDetails.add(patientDetail);
        }
        return patientDetails;
    }

    public Vector<Vector> getPatientOutput(Connection sourceConn, Vector<String> patientIds, Vector<Double>[] overallGuidelineIndDurations, Vector<Double>[] overallGuidelineIndDistances, boolean adjustedModel) {

        Vector<Vector> patientDetails = new Vector<Vector>();

        Vector<Vector> gosDetails = this.getGosScores(sourceConn, patientIds);
        Vector<Vector> otherPredictors = this.getOtherPredictors(sourceConn, patientIds);

        int patientNum = patientIds.size();
        int gosSize = gosDetails.size();
        int opSize = otherPredictors.size();
        for (int i = 0; i < patientNum; i++) {

            Vector<String> patientDetail = new Vector<String>();

            String patientId = patientIds.get(i);
            patientDetail.add(patientId);

            logger.debug("patientId (createCSVfile): " + patientId);
            Integer gosScore = new Integer(-1);
            boolean patientFound = false;
            int patientCount = 0;
            while (!patientFound && patientCount < gosSize) {
                Vector<String> gosDetailIn = gosDetails.get(patientCount);
                String idIn = gosDetailIn.get(0);
                if (idIn.equalsIgnoreCase(patientId)) {
                    gosScore = Integer.parseInt(gosDetailIn.get(2));
                    patientFound = true;
                } else {
                    patientCount++;
                }
            }
            patientDetail.add("" + gosScore);

            patientFound = false;
            patientCount = 0;
            while (!patientFound && patientCount < opSize) {
                Vector<String> otherPredictorIn = otherPredictors.get(patientCount);
                String idIn = otherPredictorIn.get(0);
                if (idIn.equalsIgnoreCase(patientId)) {
                    patientFound = true;
                    for (int j = 0; j < 6; j++) {
                        patientDetail.add(otherPredictorIn.get(j + 1));
                    }
                } else {
                    patientCount++;
                }
            }

            Vector<Double> thisPatientDurations = overallGuidelineIndDurations[i];
            int durationSize = thisPatientDurations.size();
            double durationSum = 0.0;
            for (int j = 0; j < durationSize; j++) {
                durationSum += thisPatientDurations.get(j);
            }
            double avgDuration = durationSum / durationSize;
            avgDuration = Math.round(avgDuration * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + avgDuration);

            Vector<Double> thisPatientDistances = overallGuidelineIndDistances[i];
            int distanceSize = thisPatientDistances.size();
            double distanceSum = 0.0;
            for (int j = 0; j < distanceSize; j++) {
                distanceSum += thisPatientDistances.get(j);
            }
            double avgDistance = distanceSum / distanceSize;
            avgDistance = Math.round(avgDistance * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + avgDistance);

            double logAvgDuration = Math.log(avgDuration);
            logAvgDuration = Math.round(logAvgDuration * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + logAvgDuration);

            double logAvgDistance = Math.log(avgDistance);
            logAvgDistance = Math.round(logAvgDistance * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + logAvgDistance);

            double logSum = logAvgDuration + logAvgDistance;
            logSum = Math.round(logSum * 100.0) / 100.0; //Round to 2 decimal places
            patientDetail.add("" + logSum);

            patientDetails.add(patientDetail);
        }
        return patientDetails;
    }

    public Vector<Vector> getGosScores(Connection sourceConn, Vector<String> patientIds) {

        Vector<Vector> gosScores = new Vector<Vector>();

        String sql = "SELECT Patient_Id,GOSe_Months_Post_injury,GOSe_code FROM demographic ORDER BY Patient_Id;";
        try {
            PreparedStatement ps = sourceConn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String idIn = rs.getString(1);
                if (idIn == null) {
                    idIn = "";
                }
                String gosMonthsIn = rs.getString(2);
                if (gosMonthsIn == null) {
                    gosMonthsIn = "-1";
                }
                String gosCodeIn = rs.getString(3);
                if (gosCodeIn == null) {
                    gosCodeIn = "1";
                }
                //Categorising GOSe code: Bad = 1, Good = 0
                if (gosCodeIn.equals("1")
                        || gosCodeIn.equals("2")
                        || gosCodeIn.equals("3")
                        || gosCodeIn.equals("4")) {
                    gosCodeIn = "1";
                } else {
                    gosCodeIn = "0";
                }
                Vector<String> gosDetailsIn = new Vector<String>();
                if(patientIds.contains(idIn)){
                    gosDetailsIn.add(idIn);
                    gosDetailsIn.add(gosMonthsIn);
                    gosDetailsIn.add(gosCodeIn);

                    gosScores.add(gosDetailsIn);
                }
            }
            rs.close();
        } catch (Exception e) {
            logger.debug("Error (getGosScores): " + e.getMessage());
        }
        return gosScores;
    }

    public Vector<Vector> getOtherPredictors(Connection sourceConn, Vector<String> patientIds) {

        Vector<Vector> otherPredictors = new Vector<Vector>();

        //String sql = "SELECT Patient_Id,Age,PNSH_GCS_Motor,NSH_Adm_GCS_Motor,PNSH_Left_Pupil_Reaction,PNSH_Left_Pupil_Size,PNSH_Right_Pupil_Reaction,PNSH_Right_Pupil_Size,NSH_Adm_Left_Pupil_Reaction,NSH_Adm_Left_Pupil_Size,NSH_Adm_Right_Pupil_Reaction,NSH_Adm_Right_Pupil_Size,Injury_Facial FROM demographic ORDER BY Patient_Id;";        
        String sql = "SELECT Patient_Id,Age,NSH_Adm_GCS_Motor,NSH_Adm_Left_Pupil_Reaction,NSH_Adm_Left_Pupil_Size,NSH_Adm_Right_Pupil_Reaction,NSH_Adm_Right_Pupil_Size,Injury_Facial FROM demographic ORDER BY Patient_Id;";
        try {
            PreparedStatement ps = sourceConn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String idIn = rs.getString(1);
                if (idIn == null) {
                    idIn = "";
                }

                Vector<String> predictorsIn = new Vector<String>();
                predictorsIn.add(idIn);

                for (int i = 0; i < 6; i++) {
                    String predictorIn = rs.getString(i + 2);
                    if (predictorIn == null) {
                        predictorIn = "-1";
                    }
                    predictorsIn.add(predictorIn);
                }
                otherPredictors.add(predictorsIn);
            }
            rs.close();
        } catch (Exception e) {
            logger.debug("Error (getOtherPredictors): " + e.getMessage());
        }
        return otherPredictors;
    }

    public String getDatasetName(Connection conn, String datasetId) {

        String datasetName = "";
        String sql = "SELECT dataset_name FROM dataset WHERE dataset_id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, datasetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String valueIn = rs.getString(1);
                if (valueIn == null) {
                    valueIn = "";
                }
                datasetName = valueIn;
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return datasetName;
    }

    /*public Vector<String> getPatientList(String fileFolder) {

     Vector<String> patients = new Vector<String>();
     File folderName = new File(fileFolder);

     boolean errorInFolder = false;
     if (folderName.exists() && folderName.isDirectory()) {
     File[] files = folderName.listFiles();
     int fileNum = files.length;
     String[] filenames = new String[fileNum];

     for (int i = 0; i < fileNum; i++) {
     filenames[i] = files[i].getName();
                
     //String reducedFilename = filenames[i].substring(filenames[i].indexOf("_")+1,filenames[i].lastIndexOf("_"));
     //patients.add(reducedFilename);
     patients.add(filenames[i]);
     }
     } else {
     errorInFolder = true;
     }
     return patients;
     }*/
    public String getTimestamp(String timestampIn) {

        //yyyy-MM-dd hh:mm:ss
        //Convert the datetime to Date.UTC                        
        String yearStr = timestampIn.substring(0, timestampIn.indexOf("-"));
        String monthStr = timestampIn.substring(timestampIn.indexOf("-") + 1, timestampIn.lastIndexOf("-"));

        //Subtract 1 for month, because apparently that's what you do... :-/
        int monthInt = Integer.parseInt(monthStr);
        monthInt = monthInt - 1;

        String dayStr = timestampIn.substring(timestampIn.lastIndexOf("-") + 1, timestampIn.indexOf(" "));
        String hourStr = timestampIn.substring(timestampIn.indexOf(" ") + 1, timestampIn.indexOf(":"));
        String minStr = timestampIn.substring(timestampIn.indexOf(":") + 1, timestampIn.lastIndexOf(":"));
        String timestampOut = "Date.UTC(" + yearStr + "," + monthInt + "," + dayStr + "," + hourStr + "," + minStr + ",0)";

        /*String dayStr = timestampIn.substring(0,timestampIn.indexOf("/"));
         String monthStr = timestampIn.substring(timestampIn.indexOf("/")+1,timestampIn.lastIndexOf("/"));
         String yearStr = timestampIn.substring(timestampIn.lastIndexOf("/")+1,timestampIn.indexOf(" "));
         String hourStr = timestampIn.substring(timestampIn.indexOf(" ")+1,timestampIn.indexOf(":"));
         String minStr = timestampIn.substring(timestampIn.indexOf(":")+1,timestampIn.length());
         String timestampOut = "Date.UTC(" + yearStr + "," + monthStr + "," + dayStr + "," + hourStr + "," + minStr + ",0)";*/
        return timestampOut;
    }

    public Vector<Vector> getParamOutput(String filenameIn, String samplingSize) {

        //The comment block below is the equivalent reading of the physio information from the Brain-IT analysis
        /*
         //Open the spreadsheet file for reading (using the patientID)
         FileReader fr = null;
         BufferedReader br = null;
         String filepath = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\BrainIT_TreatmentAnalysis\\output\\";
         String filename = filepath + "" + patientID + "_physio_summary.csv";

         try {
         fr = new FileReader(filename);
         br = new BufferedReader(fr);

         //Line 3 will always be the physiological header values (use the paramsToView array to select the column number)
         br.readLine();
         br.readLine();
         headerLine = br.readLine();
         //logger.info("headerLine: " + headerLine);

         while (br.ready()) {
         Vector<String> lineValues = new Vector<String>();
         String lineIn = br.readLine();
         StringTokenizer st = new StringTokenizer(lineIn, ",");

         //Assume that there are no blanks in the files (generated by my own programs anyway to ensure this)
         while (st.hasMoreTokens()) {
         lineValues.add(st.nextToken());
         }
         physioLines.add(lineValues);
         }
         } catch (IOException ioe) {
         logger.info("I/O error: " + ioe.getMessage());
         }*/
        int samplingSizeInt = Integer.parseInt(samplingSize);

        System.out.println("samplingSizeInt: " + samplingSizeInt);

        Vector<Vector> paramOutputs = new Vector<Vector>();
        try {
            FileReader fr = new FileReader(filenameIn);
            BufferedReader br = new BufferedReader(fr);

            int lineCount = 0;
            while (br.ready() && lineCount < samplingSizeInt) {
                String lineIn = br.readLine();
                System.out.println("lineIn: " + lineIn);
                StringTokenizer st = new StringTokenizer(lineIn, ",");
                Vector<String> lineVector = new Vector<String>();

                while (st.hasMoreTokens()) {
                    String tokenIn = st.nextToken();
                    if (tokenIn == null) {
                        tokenIn = "";
                    }
                    lineVector.add(tokenIn);
                }
                paramOutputs.add(lineVector);
                lineCount++;
            }
        } catch (Exception e) {
            System.out.println("I/O error: " + e.getMessage());
        }
        return paramOutputs;
    }

    public int getSeriesIndex(String seriesSelector) {

        int seriesIndex = -1;
        if (seriesSelector.equalsIgnoreCase("HRT")) {
            seriesIndex = 2;
        } else if (seriesSelector.equalsIgnoreCase("RR")) {
            seriesIndex = 3;
        } else if (seriesSelector.equalsIgnoreCase("BPs")) {
            seriesIndex = 4;
        } else if (seriesSelector.equalsIgnoreCase("BPd")) {
            seriesIndex = 5;
        } else if (seriesSelector.equalsIgnoreCase("BPm")) {
            seriesIndex = 6;
        } else if (seriesSelector.equalsIgnoreCase("SaO2")) {
            seriesIndex = 7;
        } else if (seriesSelector.equalsIgnoreCase("ICP")) {
            seriesIndex = 8;
        } else if (seriesSelector.equalsIgnoreCase("TEMP")) {
            seriesIndex = 9;
        } else if (seriesSelector.equalsIgnoreCase("CVP")) {
            seriesIndex = 10;
        } else if (seriesSelector.equalsIgnoreCase("NIBPs")) {
            seriesIndex = 11;
        } else if (seriesSelector.equalsIgnoreCase("NIBPd")) {
            seriesIndex = 12;
        } else if (seriesSelector.equalsIgnoreCase("NIBPm")) {
            seriesIndex = 13;
        }
        return seriesIndex;
    }

    public Vector<GuidelineDistance> calculateGuidelineComparison(String seriesSelector, Vector<Treatment> treatments, Vector<Event> events, long timeWindow, String algorithm, String guidelineNumStr, boolean processModelRep, String pid, Connection brainItConn) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //For each event, run a calculation of adherence 

        //treatments can tell us the pressor administration from previously        
        //treatments can also tell us about the water balance?        
        //seriesSelector/seriesIndex give us the values of a particular line (BP, HRT, etc) and that's what we're measuring already below (sort of)
        Vector<GuidelineDistance> guidelineDistances = new Vector<GuidelineDistance>();

        logger.debug("events.size() (within calculateGuidelineComparison): " + events.size());
        for (int i = 0; i < events.size(); i++) {

            //Set up the event time parameters
            Event eventIn = events.get(i);
            Vector<String> eventValues = eventIn.getValues();

            java.util.Date eventStart = eventIn.getStart();
            int eventIndex = eventIn.getEventIndex();
            String eventParameter = eventIn.getParameterFeed();

            //Set up the guideline time parameters (includes time window)
            long guidelineStartLong = eventStart.getTime() + timeWindow;
            java.util.Date guidelineStart = new java.util.Date(guidelineStartLong);
            int guidelineIndex = eventIndex;
            String guidelineParameter = eventParameter;
            
            //Calculate the ideal guideline answers
            Vector<Vector> idealGuidelineAnswers = new Vector<Vector>();
            Vector<EventProcessModel> idealGuidelinePMs = new Vector<EventProcessModel>();
            if (!processModelRep) {
                idealGuidelineAnswers = this.calculateIdealGuidelineAnswers(eventValues, eventStart, guidelineStart, treatments, guidelineNumStr,pid,brainItConn);
            } else {
                idealGuidelinePMs = this.calculateIdealGuidelinePMs(eventValues, eventStart, guidelineStart, treatments, guidelineNumStr, pid, i,brainItConn);
            }

            //Calculate the actual guideline answers
            Vector<Vector> actualGuidelineAnswers = new Vector<Vector>();
            Vector<EventProcessModel> actualGuidelinePMs = new Vector<EventProcessModel>();
            if (!processModelRep) {
                actualGuidelineAnswers = this.calculateActualGuidelineAnswers(eventValues, eventStart, guidelineStart, treatments, guidelineNumStr);
            } else {
                actualGuidelinePMs = this.calculateActualGuidelinePMs(eventValues, eventStart, guidelineStart, treatments, guidelineNumStr, pid, i, brainItConn);
            }

            //Calculate the distance between these two sets of answers
            Vector<Vector> guidelineDistanceValueLists = new Vector<Vector>();
            if (!processModelRep) {
                guidelineDistanceValueLists = this.calculateGuidelineDistances(actualGuidelineAnswers, idealGuidelineAnswers, algorithm);
            } else {
                //logger.debug("Event number: " + i);
                guidelineDistanceValueLists = this.calculateGuidelineDistances(actualGuidelinePMs, idealGuidelinePMs, algorithm, true, eventStart, timeWindow, pid, eventIndex);
            }

            //Check what information is stored here
            //this.printValuesOutput(i, eventValues, idealGuidelinePMs, actualGuidelinePMs, guidelineDistanceValueLists);
            //And add them into this GuidelineDistance object
            GuidelineDistance guidelineDistance = new GuidelineDistance(guidelineIndex, guidelineParameter);
            guidelineDistance.setStart(guidelineStart);
            guidelineDistance.setValueLists(guidelineDistanceValueLists);
            guidelineDistances.add(guidelineDistance);
            //logger.debug("=====");
        }
        return guidelineDistances;
    }

    private void printValuesOutput(int i, Vector<String> eventValues, Vector<EventProcessModel> idealGuidelinePMs, Vector<EventProcessModel> actualGuidelinePMs, Vector<Vector> guidelineDistanceValueLists) {

        logger.debug("event size (" + i + "): " + eventValues.size());
        /*for(int j=0; j<eventValues.size(); j++){
                logger.debug("event (" + i + ") value (" + j + "): " + eventValues.get(j));
            }*/
        logger.debug("=====");
        logger.debug("idealGuidelinePMs.size(): " + idealGuidelinePMs.size());
        /*for(int j=0; j<idealGuidelineAnswers.size(); j++){
                logger.debug("event (" + i + ") ideal guideline answer (" + j + "): " + idealGuidelineAnswers.get(j));
            }*/
        logger.debug("=====");
        logger.debug("actualGuidelinePMs.size(): " + actualGuidelinePMs.size());
        /*for(int j=0; j<actualGuidelineAnswers.size(); j++){
                logger.debug("event (" + i + ") actual guideline answer (" + j + "): " + actualGuidelineAnswers.get(j));
            }*/
        logger.debug("=====");
        logger.debug("guidelineDistanceValueLists.size(): " + guidelineDistanceValueLists.size());
        /*for(int j=0; j<guidelineDistanceValueLists.size(); j++){
                logger.debug("event (" + i + ") guideline distance (" + j + "): " + guidelineDistanceValueLists.get(j));
            }*/
        logger.debug("=====");
    }

    private Vector<Vector> calcIndividualDistance(Vector<String> timepointActual, Vector<String> timepointIdeal, String algorithm) {

        //logger.debug("Into calcIndividualDistance...");
        Vector<Vector> indDistances = new Vector<Vector>();
        int answerNum = timepointActual.size();
        if (algorithm.equals("direct")) {
            for (int i = 0; i < answerNum; i++) {
                String actualAnswerIn = timepointActual.get(i);
                String idealAnswerIn = timepointIdeal.get(i);
                Vector<String> indDistance = new Vector<String>();
                if (i == 0) {
                    //Treatment administered
                    String distanceNumber = "0";
                    String reason = "Treatment administered";
                    if (actualAnswerIn.equalsIgnoreCase(idealAnswerIn)) {
                        distanceNumber = "0";
                    } else {
                        /*if (showNoTreatmentDeviation) {
                            distanceNumber = "0";
                        } else {*/
                        distanceNumber = "100";
                        //}
                    }
                    indDistance.add(distanceNumber);
                    indDistance.add(reason);
                } else if (i == 1) {
                    //Treatment type
                    String distanceNumber = "0";
                    String reason = "Treatment type";
                    if (actualAnswerIn.equalsIgnoreCase(idealAnswerIn)) {
                        distanceNumber = "0";
                    } else if ((actualAnswerIn.equalsIgnoreCase("water") && idealAnswerIn.equalsIgnoreCase("pressors"))
                            || (actualAnswerIn.equalsIgnoreCase("water") && idealAnswerIn.equalsIgnoreCase("other"))
                            || (actualAnswerIn.equalsIgnoreCase("pressors") && idealAnswerIn.equalsIgnoreCase("water"))
                            || (actualAnswerIn.equalsIgnoreCase("pressors") && idealAnswerIn.equalsIgnoreCase("other"))
                            || (actualAnswerIn.equalsIgnoreCase("other") && idealAnswerIn.equalsIgnoreCase("water"))
                            || (actualAnswerIn.equalsIgnoreCase("other") && idealAnswerIn.equalsIgnoreCase("pressors"))) {
                        distanceNumber = "33";
                    } else {
                        distanceNumber = "100";
                    }
                    indDistance.add(distanceNumber);
                    indDistance.add(reason);
                } else if (i == 2) {
                    //Treatment within time window
                    String distanceNumber = "0";
                    String reason = "Treatment within time window";
                    if (actualAnswerIn.equalsIgnoreCase(idealAnswerIn)) {
                        distanceNumber = "0";
                    } else {
                        distanceNumber = "100";
                    }
                    indDistance.add(distanceNumber);
                    indDistance.add(reason);
                } else if (i == 3) {
                    //Treatment is part of repeat pattern
                    String distanceNumber = "0";
                    String reason = "Treatment is part of repeat pattern";
                    if (actualAnswerIn.equalsIgnoreCase(idealAnswerIn)) {
                        distanceNumber = "0";
                    } else {
                        distanceNumber = "100";
                    }
                    indDistance.add(distanceNumber);
                    indDistance.add(reason);
                }
                indDistances.add(indDistance);
            }
        } else if (algorithm.equals("weighted")) {
            for (int i = 0; i < answerNum; i++) {
                String actualAnswerIn = timepointActual.get(i);
                String idealAnswerIn = timepointIdeal.get(i);
                Vector<String> indDistance = new Vector<String>();
                if (i == 0) {
                    //Treatment administered
                    String distanceNumber = "0";
                    String reason = "Treatment administered";
                    double weighting = 1.0;
                    if (actualAnswerIn.equalsIgnoreCase(idealAnswerIn)) {
                        distanceNumber = "0";
                    } else {
                        distanceNumber = "100";
                    }

                    //Weight (for importance) the distance of this answer between ideal and actual
                    int distanceNumberInt = Integer.parseInt(distanceNumber);
                    double weightedDistanceNumber = distanceNumberInt * weighting;
                    //weightedDistanceNumber = Math.round - or something, how do I round the text to 2 d.p. again?
                    distanceNumber = "" + weightedDistanceNumber;

                    indDistance.add(distanceNumber);
                    indDistance.add(reason);
                } else if (i == 1) {

                    logger.debug("Into clause i == 1...");
                    logger.debug("actualAnswerIn: " + actualAnswerIn);
                    logger.debug("idealAnswerIn: " + idealAnswerIn);
                    logger.debug("----");

                    //Treatment type
                    String distanceNumber = "0";
                    String reason = "Treatment type";
                    double weighting = 0.5;
                    if (actualAnswerIn.equalsIgnoreCase(idealAnswerIn)) {
                        distanceNumber = "0";
                    } else if ((actualAnswerIn.equalsIgnoreCase("water") && idealAnswerIn.equalsIgnoreCase("pressors"))
                            || (actualAnswerIn.equalsIgnoreCase("water") && idealAnswerIn.equalsIgnoreCase("other"))
                            || (actualAnswerIn.equalsIgnoreCase("pressors") && idealAnswerIn.equalsIgnoreCase("water"))
                            || (actualAnswerIn.equalsIgnoreCase("pressors") && idealAnswerIn.equalsIgnoreCase("other"))
                            || (actualAnswerIn.equalsIgnoreCase("other") && idealAnswerIn.equalsIgnoreCase("water"))
                            || (actualAnswerIn.equalsIgnoreCase("other") && idealAnswerIn.equalsIgnoreCase("pressors"))) {
                        distanceNumber = "33";
                    } else {
                        distanceNumber = "100";
                    }

                    //Weight (for importance) the distance of this answer between ideal and actual
                    int distanceNumberInt = Integer.parseInt(distanceNumber);
                    double weightedDistanceNumber = distanceNumberInt * weighting;
                    //weightedDistanceNumber = Math.round - or something, how do I round the text to 2 d.p. again?
                    distanceNumber = "" + weightedDistanceNumber;

                    indDistance.add(distanceNumber);
                    indDistance.add(reason);
                } else if (i == 2) {
                    //Treatment within time window
                    String distanceNumber = "0";
                    String reason = "Treatment within time window";
                    double weighting = 1.0;
                    if (actualAnswerIn.equalsIgnoreCase(idealAnswerIn)) {
                        distanceNumber = "0";
                    } else {
                        distanceNumber = "100";
                    }

                    //Weight (for importance) the distance of this answer between ideal and actual
                    int distanceNumberInt = Integer.parseInt(distanceNumber);
                    double weightedDistanceNumber = distanceNumberInt * weighting;
                    //weightedDistanceNumber = Math.round - or something, how do I round the text to 2 d.p. again?
                    distanceNumber = "" + weightedDistanceNumber;

                    indDistance.add(distanceNumber);
                    indDistance.add(reason);
                } else if (i == 3) {
                    //Treatment is part of repeat pattern
                    String distanceNumber = "0";
                    String reason = "Treatment is part of repeat pattern";
                    double weighting = 0.25;
                    if (actualAnswerIn.equalsIgnoreCase(idealAnswerIn)) {
                        distanceNumber = "0";
                    } else {
                        distanceNumber = "100";
                    }

                    //Weight (for importance) the distance of this answer between ideal and actual
                    int distanceNumberInt = Integer.parseInt(distanceNumber);
                    double weightedDistanceNumber = distanceNumberInt * weighting;
                    //weightedDistanceNumber = Math.round - or something, how do I round the text to 2 d.p. again?
                    distanceNumber = "" + weightedDistanceNumber;

                    indDistance.add(distanceNumber);
                    indDistance.add(reason);
                }
                indDistances.add(indDistance);
            }
        } else if (algorithm.equals("a_star")) {

        } else if (algorithm.equals("greedy")) {

        }
        return indDistances;
    }

    private boolean comparePMStructure(EventProcessModel epm1, EventProcessModel epm2) {

        boolean epmStructureMatch = true;

        Vector<Node> epm1nodes = epm1.getNodes();
        Vector<Edge> epm1edges = epm1.getEdges();

        Vector<Node> epm2nodes = epm2.getNodes();
        Vector<Edge> epm2edges = epm2.getEdges();

        //Check the count of nodes and edges - if there's a mis-match then structure is different
        if (epm1nodes.size() != epm2nodes.size()
                || epm1edges.size() != epm2edges.size()) {
            epmStructureMatch = false;

            //Run a print check of the nodes present
            /*int elemCount = 0;
            while(elemCount < epm1nodes.size()){
                Node nodeIn1 = epm1nodes.get(elemCount);
                String nodeIn1Label = nodeIn1.getLabel();
                logger.debug("nodeIn1Label (non-matched): " + nodeIn1Label);
                elemCount++;
            }
            elemCount = 0;
            while(elemCount < epm2nodes.size()){
                Node nodeIn2 = epm2nodes.get(elemCount);
                String nodeIn2Label = nodeIn2.getLabel();
                logger.debug("nodeIn2Label (non-matched): " + nodeIn2Label);
                elemCount++;
            }*/
        }

        //Check the node labels match - the order should be retained between all of them
        if (epmStructureMatch) {
            int elemCount = 0;
            while (elemCount < epm1nodes.size() && epmStructureMatch) {
                Node nodeIn1 = epm1nodes.get(elemCount);
                String nodeIn1Label = nodeIn1.getLabel();
                //logger.debug("nodeIn1Label: " + nodeIn1Label);
                
                Node nodeIn2 = epm2nodes.get(elemCount);
                String nodeIn2Label = nodeIn2.getLabel();
                //logger.debug("nodeIn2Label: " + nodeIn2Label);

                if (!nodeIn1Label.equalsIgnoreCase(nodeIn2Label)) {
                    epmStructureMatch = false;
                } else {
                    elemCount++;
                }
            }
        }

        return epmStructureMatch;
    }

    private String convertMonth(String monthIn) {
        String monthOut = "";
        if (monthIn.equalsIgnoreCase("Jan")) {
            monthOut = "01";
        } else if (monthIn.equalsIgnoreCase("Feb")) {
            monthOut = "02";
        } else if (monthIn.equalsIgnoreCase("Mar")) {
            monthOut = "03";
        } else if (monthIn.equalsIgnoreCase("Apr")) {
            monthOut = "04";
        } else if (monthIn.equalsIgnoreCase("May")) {
            monthOut = "05";
        } else if (monthIn.equalsIgnoreCase("Jun")) {
            monthOut = "06";
        } else if (monthIn.equalsIgnoreCase("Jul")) {
            monthOut = "07";
        } else if (monthIn.equalsIgnoreCase("Aug")) {
            monthOut = "08";
        } else if (monthIn.equalsIgnoreCase("Sep")) {
            monthOut = "09";
        } else if (monthIn.equalsIgnoreCase("Oct")) {
            monthOut = "10";
        } else if (monthIn.equalsIgnoreCase("Nov")) {
            monthOut = "11";
        } else if (monthIn.equalsIgnoreCase("Dec")) {
            monthOut = "12";
        }
        return monthOut;
    }

    private Vector<String> getStringEditScore(String label, String value1, String value2, String algorithm, java.util.Date eventStart, long TIME_WINDOW, String pid, int eventIndex) {

        Vector<String> indDistance = new Vector<String>();

        double typeWeight = 0.5;
        if(algorithm.equals("type = 0.5")){
            typeWeight = 0.5;
        }else if(algorithm.equals("type = 0.25")){
            typeWeight = 0.25;
        }else if(algorithm.equals("type = 0.75")){
            typeWeight = 0.75;
        }else{
            typeWeight = 0.5;
        }
        
        //String fsubnStr = ""; //fsubn = "average distance of substituted nodes" [NEED TO CHECK]
        double stringEditScore = 0.0;
        double simScore = 0.0; //This is the similarity
        if (label.equalsIgnoreCase("Dose")) {
            if(value1.equalsIgnoreCase(value2)){ //This case should only ever by "Under" (i.e. the guideline shouldn't recommend an overdose!)
                simScore = 1.0;
            }else if(value1.contains("Over") || value2.contains("Over")){
                
                //logger.debug("Into the 'dose too high' clause...");
                
                //Find the number of doses over
                int hyphenIndex1 = value1.indexOf("-");
                int hyphenIndex2 = value2.indexOf("-");
                String multiplierStr = "1";
                if(hyphenIndex1 != -1){
                    multiplierStr = value1.substring(hyphenIndex1+1,value1.length()).trim();
                }
                if(hyphenIndex2 != -1){
                    multiplierStr = value2.substring(hyphenIndex2+1,value1.length()).trim();
                }
                int multiplier = Integer.parseInt(multiplierStr);
                simScore = 0.25 / multiplier;                
            }
            //logger.debug("simScore (" + label + "): " + simScore);
        } else if (label.equalsIgnoreCase("Type")) {
            
            /*if(eventIndex == 1){
                logger.info("value1: " + value1);
                logger.info("value2: " + value2);
            }*/
            
            simScore = 0.0; //Default simScore is 0 (lowest similarity possible)
            if(value2.equalsIgnoreCase("Any")){ //Any treatment is good
                simScore = 1.0;
            }else if(value2.equalsIgnoreCase("Neither")){ //Neither Osmotic or Steroids should be applied
                if(value1.equalsIgnoreCase("Osmotic_Therapy")
                        || value1.equalsIgnoreCase("Inotropes")){
                    simScore = typeWeight;
                }
            }else if(value2.equalsIgnoreCase("!Osmotic")){ //Osmotic should not be applied
                if(value1.equalsIgnoreCase("Osmotic_Therapy")){
                    simScore = typeWeight;
                }else{
                    simScore = 1.0;
                }
            }else if(value2.equalsIgnoreCase("!Inotropes")){ //Steroid should not be applied
                if(value1.equalsIgnoreCase("Inotropes")){
                    simScore = typeWeight;
                }else{
                    simScore = 1.0;
                }
            }else if(value1.equalsIgnoreCase("None")){ //No mass lesion is present, therefore treatment perhaps not merited
                simScore = typeWeight;
            }else if(value1.equalsIgnoreCase("Present")){ //Mass lesion is present, treatment required
                simScore = 1.0;
            }
            
            //NOTE: the rest of this case statement is because both value1 and value2 can interchangeably be ideal and actual            
            else if(value2.equalsIgnoreCase("Any")){ //Any treatment is good
                simScore = 1.0;
            }else if(value2.equalsIgnoreCase("Neither")){ //Neither Osmotic or Steroids should be applied
                if(value1.equalsIgnoreCase("Osmotic_Therapy")
                        || value1.equalsIgnoreCase("Inotropes")){
                    simScore = typeWeight;
                }
            }else if(value2.equalsIgnoreCase("!Osmotic")){ //Osmotic should not be applied
                if(value1.equalsIgnoreCase("Osmotic_Therapy")){
                    simScore = typeWeight;
                }else{
                    simScore = 1.0;
                }
            }else if(value2.equalsIgnoreCase("!Inotropes")){ //Steroid should not be applied
                if(value1.equalsIgnoreCase("Inotropes")){
                    simScore = typeWeight;
                }else{
                    simScore = 1.0;
                }
            }else if(value2.equalsIgnoreCase("None")){ //No mass lesion is present, therefore treatment perhaps not merited
                simScore = typeWeight;
            }else if(value2.equalsIgnoreCase("Present")){ //Mass lesion is present, treatment required
                simScore = 1.0;
            }
            //logger.debug("simScore (" + label + "): " + simScore);
        } else if (label.equalsIgnoreCase("Time to treatment")) {

            //logger.debug("Into the 'time to treatment' clause...");
            
            //The time passed in is the time of treatment
            //=> Need a relative time in minutes since event start (+offset, which is the answer from the ideal guideline)
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            long timeToTreatMin = 0;
            try {
                //token 2 = day number
                //token 1 = month string (convert to number)
                //token 5 = year
                //token 3(sub 1) = hour
                //token 3(sub 2) = minute
                StringTokenizer st = new StringTokenizer(value1);
                int tokenCount = 0;
                String monthStr = "";
                String dayStr = "";
                String hourStr = "";
                String minStr = "";
                String yearStr = "";
                while (st.hasMoreTokens()) {
                    String tokenIn = st.nextToken();
                    if (tokenCount == 1) {
                        monthStr = tokenIn;
                        monthStr = this.convertMonth(monthStr);
                    } else if (tokenCount == 2) {
                        dayStr = tokenIn;
                    } else if (tokenCount == 3) {
                        String timeStr = tokenIn;
                        int colonChar = timeStr.indexOf(":");
                        int colonChar2 = timeStr.lastIndexOf(":");
                        hourStr = timeStr.substring(0, colonChar);
                        minStr = timeStr.substring(colonChar + 1, colonChar2);
                    } else if (tokenCount == 5) {
                        yearStr = tokenIn;
                    }
                    tokenCount++;
                }
                value1 = dayStr + "/" + monthStr + "/" + yearStr + " " + hourStr + ":" + minStr;
                java.util.Date value1date = df.parse(value1);

                long timeToTreatMillis = value1date.getTime() - eventStart.getTime();
                
                //Subtracting the offset test here (assumes value2 is ideal output)
                if(value2.equalsIgnoreCase("0.5")){
                    long offsetMillis = (long) Double.parseDouble(value2) * 60000;
                    timeToTreatMillis -= offsetMillis;
                }
                
                long timeToTreatSec = timeToTreatMillis / 1000;
                timeToTreatMin = timeToTreatSec / 60;
                if (timeToTreatMin < 0) {
                    timeToTreatMin *= -1;
                }
                value1 = "" + timeToTreatMin;
            } catch (Exception pe) {
                //logger.debug("Error (parsing date 1): " + pe.getMessage());
            }

            try {
                java.util.Date value2date = df.parse(value2);
                long timeToTreatMillis = value2date.getTime() - eventStart.getTime();
                
                //Subtracting the offset test here (assumes value1 is ideal output)
                if(value1.equalsIgnoreCase("0.5")){
                    long offsetMillis = (long) Double.parseDouble(value1) * 60000;
                    timeToTreatMillis -= offsetMillis;
                }
                
                long timeToTreatSec = timeToTreatMillis / 1000;
                timeToTreatMin = timeToTreatSec / 60;
                if (timeToTreatMin < 0) {
                    timeToTreatMin *= -1;
                }
                value2 = "" + timeToTreatMin;
            } catch (Exception pe) {
                //logger.debug("Error (parsing date 2): " + pe.getMessage());                
            }

            try {
                double valueDouble1 = Double.parseDouble(value1);
                //logger.debug("valueDouble1: " + valueDouble1);
                double valueDouble2 = Double.parseDouble(value2);
                //logger.debug("valueDouble2: " + valueDouble2);
                double timeAbs = valueDouble1 - valueDouble2;
                if (timeAbs < 0.0) {
                    timeAbs *= -1;
                }
                //logger.debug("timeAbs: " + timeAbs);
                double timeRel = timeAbs / (TIME_WINDOW / 60000);
                //logger.debug("timeRel: " + timeRel);
                //NOTE: if timeRel goes over 1.0, then cap it here for normalisation (it's effectively a missed treatment)
                //ALSO NOTE: I'm not sure why the above isn't captured earlier (treatment shouldn't be associated outwith the time window)
                if (timeRel > 1.0) {
                    timeRel = 1.0;
                } else {
                    //ADD THE TREATMENT/EVENT ASSOCIATION COUNT (within window) HERE
                    //String thisTreatmentWithEventWithinWindowCount = pid + " - " + eventIndex + " - " + timeAbs + " - " + eventStart;
                    String thisTreatmentWithEventWithinWindowCount = pid + " - " + eventIndex;
                    if (!treatmentWithEventWithinWindowCount.contains(thisTreatmentWithEventWithinWindowCount)) {
                        treatmentWithEventWithinWindowCount.add(thisTreatmentWithEventWithinWindowCount);
                    }
                }
                //NOTE: this is 1.0 - timeRel, because the smaller the diff, the more similar are the two times
                double scoreDouble = 1.0 - (timeRel);
                simScore = scoreDouble;
                //logger.debug("simScore: " + simScore);
            } catch (Exception e) {
                //logger.debug("Number error (getStringEditScore): " + e.getMessage());
            }
            //logger.debug("----");
            //logger.info("simScore (" + label + "): " + simScore);
            //logger.info("----");
        } else {
            simScore = 1.0;
        }
        stringEditScore = 1.0 - simScore; //Distance = 1.0 - Similarity
        /*if(eventIndex == 1){
            logger.info("stringEditScore (" + label + "): " + stringEditScore);
            logger.info("----");
        }*/
        
        
        stringEditScore = Math.round(stringEditScore * 100.0) / 100.0; //Round to 2 decimal places

        String stringEditScoreStr = "" + stringEditScore;
        indDistance.add(stringEditScoreStr);
        indDistance.add(label);
        //indDistance.add(label + " (" + fsubnStr + ")");
        //logger.debug("getStringEditScore: " + indDistance);        

        return indDistance;
    }

    private Vector<String> getGraphEditScore(String label, String value1, String value2, String algorithm, java.util.Date eventStart, long TIME_WINDOW, String pid, int eventIndex) {

        Vector<String> indDistance = new Vector<String>();
        //If the corresponding node cannot be found, then need to work out the substitutions/insertions required to match

        //If the node is nature or time_taken, then 1 node + 1 edge are missing
        //Cost = add one node, add one edge, substitute one value in
        //If the node is type, then 3 nodes + 3 edges are missing, 3 substituted values
        //Cost = add three nodes, add three edges, substitute three values in   
        
        double fskipn = 0; //fskipn = average node insertions/deletions required
        double wskipn = 0.0; //wskipn = weighting of node insertions/deletions
        double fskipe = 0; //fskipe = average edge insertions/deletions required
        double wskipe = 0.0; //wskipe = weighting of edge insertions/deletions
        double fsubn = 0.0; //fsubn = average number of substitutions - this is a double as the calculations may be floating point numbers)
        double wsubn = 0.0; //wsubn = weighting of node substitutions - setting as 0.5 so that final value is always normalised lower than 1        

        String scoreStr = "";
        if (label.equalsIgnoreCase("Dose")
                || label.equalsIgnoreCase("Time to treatment")) {
            fskipn = 0.2; //Fraction of all nodes => 1/5
            fskipe = 0.2; //Fraction of all nodes => 1/5
            
            if(algorithm.equals("wskipn = 0.75")){
                wskipn = 0.83; //0.75 + 0.08
            }else if(algorithm.equals("wskipn = 0.5")){
                wskipn = 0.58; //0.5 + 0.08
            }else if(algorithm.equals("wskipn = 0.25")){
                wskipn = 0.33; //0.25 + 0.08
            }else{
                wskipn = 0.83; //This is arrived at by base cost = 0.75, additional cost (peripheral node) = 0.08 (1/3 weighting of central node)
            }
            
            if(algorithm.equals("wskipe = 0.6")){
                wskipn = 0.6;
            }else if(algorithm.equals("wskipe = 0.3")){
                wskipn = 0.3;
            }else if(algorithm.equals("wskipe = 0.9")){
                wskipn = 0.9;
            }else{
                wskipe = 0.6;
            }            
            wsubn = 1.0;
            
            String fsubnStr = this.getStringEditScore(label, value1, value2, algorithm, eventStart, TIME_WINDOW, pid, eventIndex).get(0);
            fsubn = Double.parseDouble(fsubnStr);
            
        } else if (label.equalsIgnoreCase("Type")) {
            fskipn = 0.6; //Fraction of all nodes => 3/5
            fskipe = 0.6; //Fraction of all nodes => 3/5
            
            if(algorithm.equals("wskipn = 0.75")){
                wskipn = 0.99; //0.75 + 0.24
            }else if(algorithm.equals("wskipn = 0.5")){
                wskipn = 0.74; //0.5 + 0.24
            }else if(algorithm.equals("wskipn = 0.25")){
                wskipn = 0.49; //0.25 + 0.24
            }else{
                wskipn = 0.99; //This is arrived at by base cost = 0.75, additional cost (central node) = 0.24
            }
            
            if(algorithm.equals("wskipe = 0.6")){
                wskipn = 0.6;
            }else if(algorithm.equals("wskipe = 0.3")){
                wskipn = 0.3;
            }else if(algorithm.equals("wskipe = 0.9")){
                wskipn = 0.9;
            }else{
                wskipe = 0.6;
            }
            wsubn = 1.0;
                        
            fsubn = 1.0; //Putting the direct distance in here (as the string edit distance is difficult to calculate without overriding the main function) It will also always be 1.0
        }
        double score = (((fskipn * wskipn) + (fskipe * wskipe) + (fsubn * wsubn)) / (wskipn + wskipe + wsubn));        
        //logger.info("score (" + label + "): " + score);
        score = Math.round(score * 100.0) / 100.0; //Round to 2 decimal places
        //logger.info("rounded score (" + label + "): " + score);
        double normalisedScore = score / 5.0; //Normalise using the five factors here           
        scoreStr = "" + normalisedScore;
        //scoreStr = "" + score;
        //logger.info("normalisedScore (" + label + "): " + normalisedScore);
        //logger.info("----");
        
        indDistance.add(scoreStr);
        indDistance.add(label);
        
        return indDistance;
    }

    private Vector<Vector> calculateStringEditDistance(EventProcessModel epm1, EventProcessModel epm2, String algorithm, java.util.Date eventStart, long TIME_WINDOW, String pid, int eventIndex) {

        Vector<Vector> indDistances = new Vector<Vector>();

        //Go through all the nodes in each process model and calculate string-edit distance between them
        //Compile list of reasons + scores associated with each reason
        Vector<Node> nodes1 = epm1.getNodes();
        Vector<Node> nodes2 = epm2.getNodes();
        int nodeNum = nodes1.size();
        for (int i = 0; i < nodeNum; i++) {
            Node nodeIn1 = nodes1.get(i);
            Node nodeIn2 = nodes2.get(i);

            String label1 = nodeIn1.getLabel();
            String label2 = nodeIn2.getLabel();
            if (label1.equalsIgnoreCase(label2)) {
                String value1 = nodeIn1.getValue();
                String value2 = nodeIn2.getValue();
                //if(!value1.equalsIgnoreCase(value2)){
                Vector<String> indDistance = this.getStringEditScore(label1, value1, value2, algorithm, eventStart, TIME_WINDOW, pid, eventIndex);
                indDistances.add(indDistance);
                //}
            }
        }
        return indDistances;
    }

    private Vector<Vector> calculateGraphEditDistance(EventProcessModel epm1, EventProcessModel epm2, String algorithm, java.util.Date eventStart, long TIME_WINDOW, String pid, int eventIndex) {

        Vector<Vector> indDistances = new Vector<Vector>();

        //Work out the steps required to change one process model into the other
        //Difference in nodes
        Vector<Node> nodes1 = epm1.getNodes();
        Vector<Node> nodes2 = epm2.getNodes();
        int node1num = nodes1.size();
        int node2num = nodes2.size();
        
        boolean nodeSet1IsMax = (node1num > node2num);
        int nodeNumMax = node2num;
        if (nodeSet1IsMax) {
            nodeNumMax = node1num;
        }
        
        Vector<String> indDistance = new Vector<String>();
        double overallDistance = 0.0;
        for (int i = 0; i < nodeNumMax; i++) {
            Node nodeIn = null;
            if (nodeSet1IsMax) {
                nodeIn = nodes1.get(i);
            } else {
                nodeIn = nodes2.get(i);
            }

            String labelIn = nodeIn.getLabel();
            
            //If the nodes match then the string-edit distance is calculated
            Node nodeMatch = null;
            if (nodeSet1IsMax) {
                nodeMatch = epm2.getNode(labelIn);
            } else {
                nodeMatch = epm1.getNode(labelIn);
            }
            String valueIn = nodeIn.getValue();
            String valueMatch = nodeMatch.getValue();
                
            Vector<String> indDistanceGraphEdit = new Vector<String>();
            if((labelIn.equalsIgnoreCase("Type")
                    || labelIn.equalsIgnoreCase("Time to treatment")
                    || labelIn.equalsIgnoreCase("Dose"))
                        
                    && valueMatch.equals("")){
                indDistanceGraphEdit = this.getGraphEditScore(labelIn, valueIn, valueMatch, algorithm, eventStart, TIME_WINDOW, pid, eventIndex);
            }else{
                indDistanceGraphEdit.add("0.0"); //In this clause the distance = 0
                indDistanceGraphEdit.add(labelIn);
            }
                
            String scoreStr = indDistanceGraphEdit.get(0);
            //String scoreStr = "1.0";
            overallDistance += Double.parseDouble(scoreStr);                
        }
        indDistance.add("" + overallDistance);
        indDistance.add("Treatment missing");
        indDistances.add(indDistance);        
        
        return indDistances;
    }

    private Vector<Vector> calcIndividualDistance(EventProcessModel epmActual, EventProcessModel epmIdeal, String algorithm, java.util.Date eventStart, long TIME_WINDOW, String pid, int eventIndex) {

        //logger.debug("Into calcIndividualDistance...");
        Vector<Vector> indDistances = new Vector<Vector>();
        //Compare if the two process models are the same structure
        boolean pmSameStructure = this.comparePMStructure(epmActual, epmIdeal);
        //logger.debug("EPM is same structure: " + pmSameStructure);        

        if (pmSameStructure) {
            //This is the string-edit calculation
            /*if(eventIndex == 0){
                logger.info("Into clause for same structured EPM...");
            }*/
            indDistances = this.calculateStringEditDistance(epmActual, epmIdeal, algorithm, eventStart, TIME_WINDOW, pid, eventIndex);
        } else {
            /*if(eventIndex == 0){
                logger.info("Into clause for different structured EPM...");
            }*/            
            //This is the graph-edit calculation
            indDistances = this.calculateGraphEditDistance(epmActual, epmIdeal, algorithm, eventStart, TIME_WINDOW, pid, eventIndex);
        }
        return indDistances;

    }

    private Vector<Vector> calculateGuidelineDistances(Vector<Vector> actualAnswers, Vector<Vector> idealAnswers, String algorithm) {

        int timepointNum = actualAnswers.size();
        Vector<Vector> distances = new Vector<Vector>();
        for (int i = 0; i < timepointNum; i++) {
            Vector<String> timepointActual = actualAnswers.get(i);
            Vector<String> timepointIdeal = idealAnswers.get(i);
            Vector<Vector> timepointDistance = this.calcIndividualDistance(timepointActual, timepointIdeal, algorithm);
            distances.add(timepointDistance);
        }

        return distances;
    }

    private Vector<Vector> calculateGuidelineDistances(Vector<EventProcessModel> actualAnswers, Vector<EventProcessModel> idealAnswers, String algorithm, boolean processModelRep, java.util.Date eventStart, long TIME_WINDOW, String pid, int eventIndex) {

        int timepointNum = actualAnswers.size();
        Vector<Vector> distances = new Vector<Vector>();
        for (int i = 0; i < timepointNum; i++) {
            EventProcessModel actualEpmIn = actualAnswers.get(i);
            EventProcessModel idealEpmIn = idealAnswers.get(i);
            Vector<Vector> timepointDistance = this.calcIndividualDistance(actualEpmIn, idealEpmIn, algorithm, eventStart, TIME_WINDOW, pid, eventIndex);
            distances.add(timepointDistance);
        }

        return distances;
    }

    private String calcContextAnswer(String parameterInput, String guidelineNumStr, java.util.Date eventStart, Vector<Treatment> treatments, Treatment treatmentIn) {

        //Note that if treatmentIn is null, then this is checking the context for the ideal answer        
        String contextAnswerOut = "";

        //Check the treatment list / physio readings for the patient
        if (guidelineNumStr.equals("1") || guidelineNumStr.equals("2")) {
            if (parameterInput.equals("mannitol_bp")) {
                /**
                 * Check if neurological deterioration exists, if it does -
                 * Other, if not - Mannitol Check current neurological GCS E/M/V
                 * (Neurological in Brain-IT) values against those when the
                 * patient was admitted (Demog in Brain-IT) if lower then
                 * deterioration = "yes" (=> no treatment), else "no" (=>
                 * treatment)
                 *
                 */
            }
        } else if (guidelineNumStr.equals("8")) {
            if (parameterInput.equals("treatment_indication_icp")) {
                /**
                 * Check if clinical signs or brain ct indicate treatment, if
                 * they do - Y, if not - N Check first and worst CT (Demog in
                 * Brain-IT) to see if treatment is indicated
                 *
                 */
            }
        } else if (guidelineNumStr.equals("9")) {
            if (parameterInput.equals("pressors_water_cpp")) {

                /**
                 * To check high pressor load: - look through treatment list, if
                 * more than two instances of pressors admin (steroid?), load is
                 * considered high
                 *
                 * To check water balance: - look through treatment list, if
                 * more than two instances of water (osmotic?), water is
                 * considered high
                 *
                 */
                boolean highWater = false;
                int waterInstanceCount = 0;
                boolean highPressors = false;
                int pressorInstanceCount = 0;

                for (int i = 0; i < treatments.size(); i++) {
                    String valueIn = treatments.get(i).getValue();
                    if (valueIn.contains("Osmotic")) {
                        waterInstanceCount++;
                    } else if (valueIn.contains("Inotropes")) {
                        pressorInstanceCount++;
                    }
                }
                highWater = (waterInstanceCount > 1);
                highPressors = (pressorInstanceCount > 1);

                if (highWater && !highPressors) {
                    contextAnswerOut = "Pressors";
                } else if (!highWater && highPressors) {
                    contextAnswerOut = "Water";
                } else if (highWater && highPressors) {
                    contextAnswerOut = "No treatment";
                }
            }
        }

        if (parameterInput.equals("repeat_treatment")) {
            /**
             * Check if treatmentIn is the same as a previous treatment for this
             * patient (look through treatment list)
             *
             */
            contextAnswerOut = "N";
            for (int i = 0; i < treatments.size(); i++) {
                String valueIn = treatments.get(i).getValue();
                if (treatmentIn.getValue().contains(valueIn)) {
                    contextAnswerOut = "Y";
                }
            }
        }
        return contextAnswerOut;
    }

    private String pressorFluidCheck(Vector<Treatment> treatments, int eventIndex) {

        String contextAnswerOut = "";
        /**
         * To check high pressor load: - look through treatment list, if more
         * than two instances of pressors admin (steroid?), load is considered
         * high
         *
         * To check water balance: - look through treatment list, if more than
         * two instances of water (osmotic?), water is considered high
         *
         */
        
        boolean highWater = false;
        int waterInstanceCount = 0;
        boolean highPressors = false;
        int pressorInstanceCount = 0;
        
        /*if(eventIndex == 1){
            logger.info("treatments.size(): " + treatments.size());
        }*/

        for (int i = 0; i < treatments.size(); i++) {
            String valueIn = treatments.get(i).getValue();
            if (valueIn.contains("Osmotic")) {
                waterInstanceCount++;
            } else if (valueIn.contains("Inotropes")) {
                pressorInstanceCount++;
            }
        }
        highWater = (waterInstanceCount > 1);
        highPressors = (pressorInstanceCount > 1);
        
        /*if(eventIndex == 1){
            logger.info("highWater: " + highWater);
            logger.info("highPressors: " + highPressors);
        }*/


        if (highWater && !highPressors) {
            contextAnswerOut = "!Osmotic";
        } else if (!highWater && highPressors) {
            contextAnswerOut = "!Inotropes";
        } else if (highWater && highPressors) {
            contextAnswerOut = "Neither";
        } else{
            contextAnswerOut = "Any";
        }
        
        /*if(eventIndex == 1){
            logger.info("contextAnswerOut: " + contextAnswerOut);
            logger.info("------");
        }*/
        
        return contextAnswerOut;
    }
    
    
    public Vector<Vector> getMassLesions(Connection brainItConn){
        
        Vector<Vector> massLesions = new Vector<Vector>();
        String sql = "SELECT Patient_Id,NSH_First_CT_TCDB_Class,NSH_Worst_CT_TCDB_Class FROM demographic;";
        try{
            PreparedStatement ps = brainItConn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Vector<String> massLesionIn = new Vector<String>();
                boolean massLesionPresent = false;        
                String pid = rs.getString(1);
                String firstTcdb = rs.getString(2);
                String worstTcdb = rs.getString(3);
                if(firstTcdb == null || firstTcdb.equals("")){
                    firstTcdb = "Unknown";
                }
                if(worstTcdb == null || worstTcdb.equals("")){
                    worstTcdb = "Unknown";
                }
                massLesionPresent = !firstTcdb.equalsIgnoreCase("Unknown") || !worstTcdb.equalsIgnoreCase("Unknown");
                massLesionIn.add(pid);                
                if(massLesionPresent){
                    massLesionIn.add("Present");
                }else{
                    massLesionIn.add("None");
                }
                massLesions.add(massLesionIn);
            }
        }catch(Exception e){
            logger.debug("I/O error (massLesionCTCheck): " + e.getMessage());
        }
        return massLesions;
    }
    
    
    private String massLesionCTCheck(String pid, Connection brainItConn){
        
        boolean massLesionPresent = false;        
        String sql = "SELECT NSH_First_CT_TCDB_Class,NSH_Worst_CT_TCDB_Class FROM demographic WHERE Patient_Id=?;";
        try{
            PreparedStatement ps = brainItConn.prepareStatement(sql);
            ps.setString(1,pid);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String firstTcdb = rs.getString(1);
                String worstTcdb = rs.getString(2);
                if(firstTcdb == null || firstTcdb.equals("")){
                    firstTcdb = "Unknown";
                }
                if(worstTcdb == null || worstTcdb.equals("")){
                    worstTcdb = "Unknown";
                }
                massLesionPresent = !firstTcdb.equalsIgnoreCase("Unknown") || !worstTcdb.equalsIgnoreCase("Unknown");
            }
            
        }catch(Exception e){
            logger.debug("I/O error (massLesionCTCheck): " + e.getMessage());
        }
        
        if(massLesionPresent){
            return "Present";
        }else{
            return "None";
        }
    }
    
    private int getTreatmentTypeMax(String treatmentTypeIn){

        //Use the Brain-IT category listing here (eventually)
        
        int maxAllowed = 2;
        return maxAllowed;
    }
    
    private String checkDose(Vector<Treatment> treatmentsIn, boolean ideal, int eventIndex){
        
        String contextAnswer = "";
        if(ideal){
            contextAnswer = "Under"; 
        }else{
            
            /*if(eventIndex == 0){
                for(int i = 0; i< treatmentsIn.size(); i++){
                    logger.debug("Treatment " + i);
                    Treatment treatmentIn = treatmentsIn.get(i);
                    logger.debug("Time: " + treatmentIn.getTimestamp());
                    logger.debug("Value: " + treatmentIn.getValue());
                    logger.debug("-----");
                }
            }*/
            
            
            boolean treatmentFound = treatmentsIn.size() > 0;
            TreeMap<String,Integer> treatmentTypes = new TreeMap<String,Integer>();
            for(int i = 0; i< treatmentsIn.size(); i++){
                Treatment treatmentIn = treatmentsIn.get(i);
                String type = treatmentIn.getValue();
                if(!treatmentTypes.containsKey(type)){
                    treatmentTypes.put(type,1);
                }else{
                    int typeNum = treatmentTypes.get(type);
                    typeNum++;
                    treatmentTypes.put(type,typeNum);
                }
            }
            
            if(!treatmentFound){
                contextAnswer = "Under";
            }else{
                
                //Go through all the entries to check if a particular type has gone over the allowed max
                Set typeSet = treatmentTypes.keySet();
                Iterator typeIter = typeSet.iterator();
                while(typeIter.hasNext()){
                    String type = (String) typeIter.next();
                    int typeNum = ((Integer) treatmentTypes.get(type)).intValue();
                    if(typeNum > this.getTreatmentTypeMax(type)){
                        //contextAnswer += "Over (" + type + ") - " + typeNum;
                        contextAnswer = "Over (" + type + ") - " + typeNum;
                    }
                }
                
                if(contextAnswer.equals("")){
                    contextAnswer = "Under";
                }
            }
        }
        /*if(eventIndex == 0){
            logger.debug("contextAnswer (checkDose): " + contextAnswer);
            logger.debug("======");
        }*/
        return contextAnswer;
    }

    private String calcContextAnswer(String parameterInput, String guidelineNumStr, Vector<Treatment> treatmentsIn, boolean ideal, String pid, Connection brainItConn, int eventIndex) {

        String contextAnswerOut = "";
        if (parameterInput.equalsIgnoreCase("min_time_to_treatment")) {
            contextAnswerOut = "0.5"; //0.5 mins (=30s) is the lower bound of the time window, everything is calculated relative to this
        } else if (parameterInput.equalsIgnoreCase("type")) {
            if (guidelineNumStr.equalsIgnoreCase("8")) {
                //Mass lesions/CT check
                contextAnswerOut = this.massLesionCTCheck(pid, brainItConn);                                
            } else if (guidelineNumStr.equalsIgnoreCase("9")) {
                //Pressors/fluids check: if only pressors high, recommend fluid, if only fluid high, recommend pressors, both high then none
                contextAnswerOut = this.pressorFluidCheck(treatmentsIn,eventIndex);                
            } else{
                contextAnswerOut = "Any";
            }
        } else if (parameterInput.equalsIgnoreCase("dose")) {
            contextAnswerOut = this.checkDose(treatmentsIn, ideal, eventIndex);
            /*if(!ideal){
                logger.debug("contextAnswerOut: " + contextAnswerOut);
            }*/
        }
        return contextAnswerOut;
    }

    public Vector<Vector> calculateIdealGuidelineAnswers(Vector<String> eventValues, java.util.Date eventStart, java.util.Date guidelineStart, Vector<Treatment> treatments, String guidelineNumStr, String pid, Connection brainItConn) {

        Vector<Vector> guidelineAnswers = new Vector<Vector>();

        //Build ideal guideline for each event from context
        for (int j = 0; j < eventValues.size(); j++) {

            Vector<String> guidelineValueIn = new Vector<String>();

            guidelineValueIn.add("Y"); //Treatment administered
            guidelineValueIn.add(this.calcContextAnswer("pressors_water_cpp", guidelineNumStr, treatments,true,pid,brainItConn,0)); //Need to search previous water/pressor balance to assign this (CALL-OUT TO OTHER FUNCTION)
            guidelineValueIn.add("Y"); //Within time window
            guidelineValueIn.add(this.calcContextAnswer("repeat_treatment", guidelineNumStr, treatments,true,pid,brainItConn,0)); //Repeat treatment
            guidelineAnswers.add(guidelineValueIn);
        }

        return guidelineAnswers;
    }

    public Vector<EventProcessModel> calculateIdealGuidelinePMs(Vector<String> eventValues, java.util.Date eventStart, java.util.Date guidelineStart, Vector<Treatment> treatments, String guidelineNumStr, String pid, int eventIndex, Connection brainItConn) {

        Vector<EventProcessModel> guidelinePMs = new Vector<EventProcessModel>();

        //Build ideal guideline for each event from context
        for (int j = 0; j < eventValues.size(); j++) {
            
            long eventReadingTimestamp = eventStart.getTime() + (j * 60000); //Converted to mins
            long guidelineReadingTimestamp = guidelineStart.getTime() + (j * 60000); //Converted to mins
            
            boolean treatmentFound = false;
            Vector<Treatment> treatmentsIn = new Vector<Treatment>();
            for(int k = 0; k < treatments.size(); k++){
                Treatment treatmentIn = treatments.get(k);
                long treatmentTimeIn = treatmentIn.getTimestamp().getTime();
                if ((treatmentTimeIn > eventReadingTimestamp) && (treatmentTimeIn < guidelineReadingTimestamp)) {
                    treatmentsIn.add(treatmentIn);
                }
            }
            treatmentFound = treatmentsIn.size() > 0;

            String type = "";
            if(guidelineNumStr.equals("9")){
                type = this.calcContextAnswer("type", guidelineNumStr, treatmentsIn,true,pid,brainItConn,eventIndex);
            }else{
                type = this.calcContextAnswer("type", guidelineNumStr, treatments,true,pid,brainItConn,eventIndex);
            }
            
            String timeTaken = this.calcContextAnswer("min_time_to_treatment", guidelineNumStr, treatments,true,pid,brainItConn,eventIndex);
            String nature = this.calcContextAnswer("dose", guidelineNumStr, treatments,true,pid,brainItConn,eventIndex);

            EventProcessModel epm = new EventProcessModel(type, timeTaken, nature, pid, eventIndex);
            guidelinePMs.add(epm);
        }

        return guidelinePMs;
    }

    public Vector<Vector> calculateActualGuidelineAnswers(Vector<String> eventValues, java.util.Date eventStart, java.util.Date guidelineStart, Vector<Treatment> treatments, String guidelineNumStr) {

        Vector<Vector> guidelineAnswers = new Vector<Vector>();

        //Check the treatments - has there been one since the event start?
        for (int j = 0; j < eventValues.size(); j++) {

            Vector<String> guidelineValueIn = new Vector<String>();

            long eventReadingTimestamp = eventStart.getTime() + (j * 60000);
            long guidelineReadingTimestamp = guidelineStart.getTime() + (j * 60000);

            boolean treatmentFound = false;
            int treatmentCount = 0;
            Treatment treatmentIn = null;
            while (!treatmentFound && treatmentCount < treatments.size()) {
                treatmentIn = treatments.get(treatmentCount);
                long treatmentTimeIn = treatmentIn.getTimestamp().getTime();
                if ((treatmentTimeIn > eventReadingTimestamp) && (treatmentTimeIn < guidelineReadingTimestamp)) {
                    treatmentFound = true;
                } else {
                    treatmentCount++;
                }
            }
            if (treatmentFound) {
                guidelineValueIn.add("Y"); //Treatment administered
                //guidelineValueIn.add("100");
            } else {
                guidelineValueIn.add("N");
                //guidelineValueIn.add("0");
            }
            if (treatmentFound) {
                guidelineValueIn.add(treatmentIn.getValue()); //Retrieve the treatment type from above (CALL-OUT TO OTHER FUNCTION)
            } else {
                guidelineValueIn.add("Water");
            }

            if (treatmentFound) {
                guidelineValueIn.add("Y"); //Time-point is within treatment time window
            } else {
                guidelineValueIn.add("N"); //CHECK LOGIC OF THIS
            }
            if (treatmentFound) {
                guidelineValueIn.add(this.calcContextAnswer("repeat_treatment", guidelineNumStr, eventStart, treatments, treatmentIn)); //Retrieve the "repeat" status from other treatment list (CALL-OUT TO OTHER FUNCTION)
            } else {
                guidelineValueIn.add("N"); //CHECK LOGIC OF THIS
            }
            guidelineAnswers.add(guidelineValueIn);
        }

        return guidelineAnswers;
    }
    
    private Treatment getEarliestTreatment(Vector<Treatment> treatmentsIn, int eventIndex){
        
        int treatmentNum = treatmentsIn.size();
        Treatment earliestTreatment = null;
        if(treatmentNum > 0){
            earliestTreatment = treatmentsIn.get(0);
        }
        for(int i=0; i<treatmentNum; i++){            
            Treatment treatmentIn = treatmentsIn.get(i);
            
            String treatmentType = treatmentIn.getValue();
            /*if(eventIndex == 0){
                logger.debug("treatmentType (within getEarliestTreatment): " + treatmentType);
            }*/
            
            java.util.Date treatmentInTime = treatmentIn.getTimestamp();
            java.util.Date earliestTreatmentTime = earliestTreatment.getTimestamp();
            if(treatmentInTime.getTime() < earliestTreatmentTime.getTime()){
                earliestTreatment = treatmentIn;
            }
        }
        return earliestTreatment;
    }

    public Vector<EventProcessModel> calculateActualGuidelinePMs(Vector<String> eventValues, java.util.Date eventStart, java.util.Date guidelineStart, Vector<Treatment> treatments, String guidelineNumStr, String pid, int eventIndex, Connection brainItConn) {

        Vector<EventProcessModel> guidelinePMs = new Vector<EventProcessModel>();

        //Check the treatments - has there been one since the event start?
        String nature = ""; //Nature and Type only gets calculated for the first encountering of the treatment so declaring outside loop here
        String type = "";
        /*if(eventIndex == 0){
            logger.debug("eventValues.size() (within calculateActualGuidelinePMs): " + eventValues.size());
        }*/
        for (int j = 0; j < eventValues.size(); j++) {

            long eventReadingTimestamp = eventStart.getTime() + (j * 60000); //Converted to mins
            long guidelineReadingTimestamp = guidelineStart.getTime() + (j * 60000); //Converted to mins
            
            boolean treatmentFound = false;
            Vector<Treatment> treatmentsIn = new Vector<Treatment>();
            for(int k = 0; k < treatments.size(); k++){
                Treatment treatmentIn = treatments.get(k);
                long treatmentTimeIn = treatmentIn.getTimestamp().getTime();
                if ((treatmentTimeIn > eventReadingTimestamp) && (treatmentTimeIn < guidelineReadingTimestamp)) {
                    treatmentsIn.add(treatmentIn);
                }
            }
            treatmentFound = treatmentsIn.size() > 0;
            
            EventProcessModel epm = null;            
            if (treatmentFound) {               
                
                //For the epm created in this particular minute, choose:
                    //- the timeTaken to the first treatment encountered (chronologically)
                    //- the type of the first treatment encountered (chronologically) (not sure if this is justified)
                    //- the overall dosage (nature) of all treatments within the window (of the same type)
                    
                    //The list is not necessarily ordered chronologically, so need to pull out the earliest one
                Treatment earliestTreatment = this.getEarliestTreatment(treatmentsIn,eventIndex);
                    
                String timeTaken = earliestTreatment.getTimestamp().toString();                                
                nature = this.calcContextAnswer("dose", guidelineNumStr, treatmentsIn, false,pid,brainItConn,eventIndex);
                /*if(eventIndex == 0){
                    logger.debug("nature: " + nature);
                }*/
                if(guidelineNumStr.equals("9") || guidelineNumStr.equals("1")){
                    type = earliestTreatment.getValue();
                }else{
                    type = this.calcContextAnswer("type", guidelineNumStr, treatmentsIn, false,pid,brainItConn,eventIndex);
                }
                                
                String thisTreatmentWithEventCount = pid + " " + eventIndex;
                if (!treatmentWithEventCount.contains(thisTreatmentWithEventCount)) {
                    if(eventIndex == 0){
                        nature = this.calcContextAnswer("dose", guidelineNumStr, treatmentsIn, false,pid,brainItConn,eventIndex);
                    }
                    treatmentWithEventCount.add(thisTreatmentWithEventCount);                    
                }
                
                epm = new EventProcessModel(type, timeTaken, nature, pid, eventIndex);

            } else {
                epm = new EventProcessModel("", "", "", pid, eventIndex);
            }
            
            
            
            
            guidelinePMs.add(epm);
        }

        return guidelinePMs;
    }

    public String getGuidelineValue(String timestampIn, Vector<Vector> guidelineParams) {

        String guidelineValueOut = "";

        boolean found = false;
        int paramValues = guidelineParams.size();

        int paramCount = 0;
        while (!found && paramCount < paramValues) {
            Vector<String> guidelineParamIn = guidelineParams.get(paramCount);
            if (timestampIn.equalsIgnoreCase(guidelineParamIn.get(0))) {
                found = true;
                guidelineValueOut = guidelineParamIn.get(1);
            } else {
                paramCount++;
            }
        }
        return guidelineValueOut;
    }

    public String getPatientList(String datasetId, Connection conn) {

        String listStr = "<select class='form-control' name='pid'><option value=''>[Select...]</option>";
        String sql = "SELECT patient_id FROM patient WHERE dataset_id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, datasetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String valueIn = rs.getString(1);
                listStr += "<option value='" + valueIn + "'>" + valueIn + "</option>";
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        listStr += "</select>";
        return listStr;
    }
    
    private boolean patientIsException(String pidIn){
        return pidIn.equals("16138374")
                || pidIn.equals("38350537")
                || pidIn.equals("48351515")
                || pidIn.equals("38350505")
                || pidIn.equals("84884925")
                || pidIn.equals("4816151")
                || pidIn.equals("51572724")
                || pidIn.equals("5248383")
                || pidIn.equals("83805040")
                || pidIn.equals("5061515")
                || pidIn.equals("15127262")
                || pidIn.equals("37248483")
                || pidIn.equals("50462651")
                || pidIn.equals("62684848")
                || pidIn.equals("84885067")
                || pidIn.equals("84885073")
                ;
    }
    

    public Vector<String> getPatientIds(String datasetId, Connection conn, boolean individualCharts) {

        Vector<String> pids = new Vector<String>();
        String sql = "SELECT patient_id FROM patient WHERE dataset_id=?;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, datasetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String valueIn = rs.getString(1);                
                if(!this.patientIsException(valueIn)
                        || individualCharts){
                    pids.add(valueIn);
                }
            }
            rs.close();
        } catch (Exception e) {
            logger.debug("Error: " + e.getMessage());
        }
        return pids;
    }

    public String getGuidelineList(Connection conn) {

        String listStr = "<select class='form-control' name='guideline' onchange='select_eusig(this.value);'><option value=''>[Select...]</option>";
        String sql = "SELECT guideline_desc, guideline_id FROM guideline;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String guidelineIn = rs.getString(1);
                String guidelineIdIn = rs.getString(2);
                listStr += "<option value='" + guidelineIdIn + "'>" + guidelineIdIn + " - " + guidelineIn + "</option>";
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        listStr += "</select>";
        return listStr;
    }

    public String getGuidelineSeriesList(Connection conn) {

        String listStr = "<select name='guideline_series'><option value=''>[Select...]</option>";
        String sql = "SELECT series_name FROM series;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String seriesIn = rs.getString(1);
                listStr += "<option value='" + seriesIn + "'>" + seriesIn + "</option>";
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        listStr += "</select>";
        return listStr;
    }

    public String getHolddownList(Connection conn) {

        //String listStr = "<select class='form-control' name='holddown' onchange='getEventIdList(this,\"" + pid + "\");'><option value=''>[Select...]</option>";
        String listStr = "<select class='form-control' name='holddown'><option value=''>[Select...]</option>";
        String sql = "SELECT DISTINCT holddown FROM event ORDER BY holddown;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String holddownIn = rs.getString(1);
                listStr += "<option value='" + holddownIn + "'>" + holddownIn + "</option>";
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        listStr += "</select>";
        return listStr;
    }

    public String getTimeWindow(String pid) {

        String listStr = "<select class='form-control' name='time_window' onchange='getEventIdList(\"" + pid + "\");'><option value=''>[Select...]</option>";
        listStr += "<option>15</option>";
        listStr += "<option>30</option>";
        listStr += "<option>45</option>";
        listStr += "<option>60</option>";

        listStr += "</select>";
        return listStr;
    }

    public String getAlgorithmList() {

        String listStr = "<select name='algorithm'><option value=''>[Select...]</option>";
        listStr += "<option value='direct'>Direct</option>";
        listStr += "<option value='weighted'>Weighted</option>";
        listStr += "<option value='a_star'>A-star</option>";
        listStr += "<option value='greedy'>Greedy</option>";

        listStr += "</select>";
        return listStr;
    }

    public String getEusigEventList(Connection conn) {

        String listStr = "<select name='eusig_id'><option value=''>[Select...]</option>";
        String sql = "SELECT * FROM eusig_defn ORDER BY eusig_id;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String eusigIdIn = rs.getString(1);
                String seriesIn = rs.getString(2);
                String thresholdIn = rs.getString(3);
                listStr += "<option value='" + eusigIdIn + "'>" + seriesIn + " (" + thresholdIn + ")</option>";
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        listStr += "</select>";
        return listStr;
    }

    public String getEventList(String pid, Connection conn, String eusigId, String holddown) {

        String listStr = "<select class='form-control' name='event_id'><option value=''>[Select...]</option>";
        String sql = "SELECT DISTINCT event_id FROM event WHERE patient_id=? AND eusig_id=? AND holddown=? ORDER BY event_id;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pid);
            ps.setString(2, eusigId);
            ps.setString(3, holddown);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String eventIdIn = rs.getString(1);
                listStr += "<option value='" + eventIdIn + "'>" + eventIdIn + "</option>";
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        listStr += "</select>";
        return listStr;
    }
    
    public Vector<Vector> getAges(Connection brainItConn) {
        
        Vector<Vector> patientAges = new Vector<Vector>();
        try{
            String sql = "SELECT Patient_Id,age FROM demographic;";
            PreparedStatement ps = brainItConn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Vector<String> patientAgeIn = new Vector<String>();
                String pid = rs.getString(1);
                String age = rs.getString(2);
                patientAgeIn.add(pid);
                patientAgeIn.add(age);
                
                patientAges.add(patientAgeIn);
            }
        }catch(Exception e){
            logger.debug("I/O error (getAges): " + e.getMessage());
        }
        return patientAges;
    }

    private boolean checkAgeHigh(String pid, Connection brainItConn) {
        
        boolean ageHigh = false;
        try{
            String sql = "SELECT age FROM demographic WHERE Patient_Id=?;";
            PreparedStatement ps = brainItConn.prepareStatement(sql);
            ps.setString(1, pid);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String age = rs.getString(1);
                double ageDouble = Double.parseDouble(age);
                ageHigh = ageDouble >= 70.0;                
            }
        }catch(Exception e){
            logger.debug("I/O error: " + e.getMessage());
        }
        return ageHigh;
    }

    private boolean checkAutoregIntact(double prX, double[] autoregBounds, String pid, Connection conn) {
        return false;
    }

    private String getCppOpt(double prX, String pid, Connection conn) {
        
        //Plot the prx against the cpp for this 5 min window
        
        
        
        return "";
    }
    
    private double[] checkAutoreg(double prX, String pid, Connection conn){
        return null;
    }
    
    private double getPRx(String pid, Connection conn){
        
        //Get 5 min window of ABP/ICP for this patient
        Vector<Double> abpReadings = new Vector<Double>();
        Vector<Double> icpReadings = new Vector<Double>();
        Vector<Double> cppReadings = new Vector<Double>();
        String sql = "SELECT BPm,ICPm,CPP FROM physiological WHERE Patient_Id=? LIMIT 5;";
        try{
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,pid);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String abpIn = rs.getString(1);
                String icpIn = rs.getString(2);
                String cppIn = rs.getString(3);
                if(abpIn == null){
                    abpIn = "";
                }
                if(icpIn == null){
                    icpIn = "";
                }
                if(cppIn == null){
                    cppIn = "";
                }                
                double abpDoubleIn = Double.parseDouble(abpIn);
                double icpDoubleIn = Double.parseDouble(icpIn);
                double cppDoubleIn = Double.parseDouble(cppIn);
                abpReadings.add(abpDoubleIn);
                icpReadings.add(icpDoubleIn);
                cppReadings.add(cppDoubleIn);
            }            
        }catch(Exception e){
            logger.debug("I/O error (getPRx): " + e.getMessage());
        }        
        
        //Translate to arrays for calculation
        double[] abpArray = new double[abpReadings.size()];
        double[] icpArray = new double[icpReadings.size()];
        double[] cppArray = new double[cppReadings.size()];
        for(int i=0; i<abpReadings.size(); i++){
            abpArray[i] = abpReadings.get(i);
        }
        for(int i=0; i<icpReadings.size(); i++){
            icpArray[i] = icpReadings.get(i);
        }
        for(int i=0; i<cppReadings.size(); i++){
            cppArray[i] = cppReadings.get(i);
        }
        
        //Pearson co-efficient of ABP vs ICP over 5 minute window for this patient
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double prx = pc.correlation(abpArray, icpArray);
        return prx;
    }

    private String checkContext(String eusigId, String guideline, String pid, Connection conn) {

        String eusigIdMod = "";

        /**
         * Using the guideline we may modify the eusig_defn called - if
         * guideline = 1, and age is high, then use eusig_defn=10 (instead of 9)
         * - if guideline = 9, and autoreg=true, then find cppopt and use
         * eusig_defn
         */
        if (guideline.equalsIgnoreCase("1")) {

            boolean ageHigh = this.checkAgeHigh(pid, conn);
            if (ageHigh) {
                eusigIdMod = "10";
            } else {
                eusigIdMod = eusigId;
            }
        } else if (guideline.equalsIgnoreCase("9")) {

            eusigIdMod = eusigId;
            /*double prX = this.getPRx(pid,conn);
            String cppOptEusig = this.getCppOpt(prX,pid,conn);
            double[] autoregBounds = this.checkAutoreg(prX,pid,conn);
            boolean autoregIntact = this.checkAutoregIntact(prX,autoregBounds,pid, conn);
            if (autoregIntact) {                
                eusigIdMod = cppOptEusig;
            } else {
                eusigIdMod = eusigId;
            }*/
        } else {
            eusigIdMod = eusigId;
        }
        return eusigIdMod;
    }

    public Vector<Event> getEvents(String pid, String holddown, String series, String eusigId, String guideline, Connection brainItConn, Connection conn) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        /**
         * Using the guideline we may modify the eusig_defn called - if
         * guideline = 1, and age is high, then use eusig_defn=10 (instead of 9)
         * - if guideline = 9, and autoreg=true, then find cppopt and use
         * eusig_defn
         */
        eusigId = this.checkContext(eusigId, guideline, pid, brainItConn);        
        //logger.debug("pid: " + pid + ", eusigId: " + eusigId);

        //Gives you threshold associated with that EUSIG definition
        String sqlEusig = "SELECT threshold FROM eusig_defn WHERE eusig_id=?;";
        String threshold = "";
        try {
            PreparedStatement ps = conn.prepareStatement(sqlEusig);
            ps.setString(1, eusigId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                threshold = rs.getString(1);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            logger.debug("Error (getEvents #1): " + e.getMessage());
        }

        //Gives you the number of events in the data with these characteristics (for this patient)
        Vector<Event> events = new Vector<Event>();
        String sql = "SELECT * FROM event WHERE event.patient_id=? AND event.eusig_id=? AND event.holddown=?;"; //Gives you index, start and end
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pid);
            ps.setString(2, eusigId);
            ps.setString(3, holddown);
            ResultSet rs = ps.executeQuery();

            int holddownInt = Integer.parseInt(holddown);
            long holddownLong = (long) holddownInt;

            float thresholdFloat = Float.parseFloat(threshold);

            while (rs.next()) {
                int eventId = rs.getInt(1);
                String start = rs.getString(5);
                String end = rs.getString(6);

                Event event = new Event(eventId, series);
                event.setEventHolddown(holddownLong);
                event.setThresholdValue(thresholdFloat);
                java.util.Date startDate = df.parse(start);
                java.util.Date endDate = df.parse(end);
                event.setStart(startDate);
                event.setEnd(endDate);

                events.add(event);
            }
        } catch (Exception e) {
            logger.debug("Error (getEvents #2): " + e.getMessage());
        }

        //Gives you the actual readings associated with each event
        for (int i = 0; i < events.size(); i++) {

            Event eventIn = events.get(i);
            int eventIndex = eventIn.getEventIndex();
            String sqlReading = "SELECT reading FROM event_reading WHERE eusig_id=? AND holddown=? AND patient_id=? AND event_id=?;";

            try {
                PreparedStatement ps = conn.prepareStatement(sqlReading);
                ps.setString(1, eusigId);
                ps.setString(2, holddown);
                ps.setString(3, pid);
                ps.setInt(4, eventIndex);
                ResultSet rs = ps.executeQuery();
                Vector<String> readings = new Vector<String>();
                while (rs.next()) {
                    String readingIn = rs.getString(1);
                    if (readingIn == null) {
                        readingIn = "";
                    }
                    readings.add(readingIn);
                }
                eventIn.setValues(readings);
            } catch (Exception e) {
                logger.debug("Error (getEvents #3): " + e.getMessage());
            }
            events.set(i, eventIn);
        }

        return events;
    }

    /*public Vector<Treatment> getTreatments(String pid, Connection conn) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        Vector<Treatment> treatments = new Vector<Treatment>();
        //String sql = "SELECT * FROM treatment WHERE treatment.patient_id=?;";
        String sql = "SELECT * FROM treatment WHERE treatment.patient_id=? LIMIT 2;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pid);
            ResultSet rs = ps.executeQuery();

            int treatmentCount = 0;
            while (rs.next()) {
                String timestamp = rs.getString(3);
                
                String value = rs.getString(4);
                String desc = rs.getString(5);
                String target = rs.getString(6);
                
                //Doing this to check the graph rendering
                if (treatmentCount == 0) {
                    timestamp = "2004-05-28 02:09:00";
                } else if (treatmentCount == 1) {
                    timestamp = "2004-05-28 02:28:00";
                } else if (treatmentCount == 2) {
                    timestamp = "2004-05-28 02:40:00";
                }
                /*if (treatmentCount == 0) {
                    timestamp = "2004-05-28 02:01:00";
                    value = "Steroid";
                } else if (treatmentCount == 1) {
                    timestamp = "2004-05-28 02:36:00";
                    value = "Steroid";
                }
                
                
                java.util.Date timestampDate = df.parse(timestamp);
                Treatment treatment = new Treatment(value, desc, target, timestampDate);

                /*System.out.println("======");
                 System.out.println("timestamp: " + timestamp);
                 System.out.println("desc: " + desc);
                 System.out.println("value: " + value);
                 System.out.println("target: " + target);
                 System.out.println("======");
                if (targetedTreatments) {
                    if (target.contains("icp")) {
                        treatments.add(treatment);
                    }
                } else {
                    treatments.add(treatment);
                }
                treatmentCount++;
            }
        } catch (Exception e) {
            System.out.println("Error (getTreatments): " + e.getMessage());
        }
        return treatments;
    }*/
    public Vector<Treatment> getTreatments(String pid, Connection conn, boolean dummy) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        Vector<Treatment> treatments = new Vector<Treatment>();
        //String sql = "SELECT * FROM treatment WHERE treatment.patient_id=?;";
        String tablename = "treatment";
        if (dummy) {
            tablename = "treatment_dummy";
        }
        String sql = "SELECT * FROM " + tablename + " WHERE " + tablename + ".patient_id=?;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pid);
            ResultSet rs = ps.executeQuery();

            int treatmentCount = 0;
            while (rs.next()) {
                String timestamp = rs.getString(3);

                String value = rs.getString(4);
                /*if(treatmentCount % 2 == 0){
                    value = "Osmotic";
                }else{
                    value = "Steroid";
                }*/
                String desc = rs.getString(5);
                String target = rs.getString(6);

                //Doing this to check the graph rendering
                if (treatmentCount == 0) {
                    timestamp = "2004-05-29 09:02:00";
                    value = "Steroid_Therapy";
                }/* else if (treatmentCount == 1) {
                    timestamp = "2004-05-28 05:24:00";
                    value = "Inotropes";
                } else if (treatmentCount == 2) {
                    timestamp = "2004-05-28 05:26:00";
                    value = "Inotropes";
                }*/
                /*if (treatmentCount == 0) {
                    timestamp = "2004-05-28 02:01:00";
                    value = "Steroid";
                } else if (treatmentCount == 1) {
                    timestamp = "2004-05-28 02:36:00";
                    value = "Steroid";
                }*/
                java.util.Date timestampDate = df.parse(timestamp);
                Treatment treatment = new Treatment(value, desc, target, timestampDate);

                /*System.out.println("======");
                 System.out.println("timestamp: " + timestamp);
                 System.out.println("desc: " + desc);
                 System.out.println("value: " + value);
                 System.out.println("target: " + target);
                 System.out.println("======");*/
 /*if (targetedTreatments) {
                    if (target.contains("icp")) {
                        treatments.add(treatment);
                    }
                } else {*/
                treatments.add(treatment);
                //}
                treatmentCount++;
            }
        } catch (Exception e) {
            System.out.println("Error (getTreatments): " + e.getMessage());
        }
        return treatments;
    }

    public String mapReasonToNegative(String reasonIn) {

        String reasonOut = "";
        if (reasonIn.equals("Treatment administered")) {
            reasonOut = "Treatment not administered";
        } else if (reasonIn.equals("Treatment within time window")) {
            reasonOut = "Treatment not administered within time window";
        } else if (reasonIn.equals("Treatment is part of repeat pattern")) {
            reasonOut = "Treatment should be part of repeat pattern";
        } else if (reasonIn.equals("Treatment type")) {
            reasonOut = "Treatment type contraindicates in patient context";
        }
        return reasonOut;
    }

    public double calcMin(Vector<Double> distances) {

        int size = distances.size();
        double min = 100;
        for (int i = 0; i < size; i++) {
            double distanceIn = distances.get(i).doubleValue();
            if (distanceIn < min) {
                min = distanceIn;
            }
        }
        //logger.debug("min: " + min);
        return min;
    }

    public double calcMax(Vector<Double> distances) {

        int size = distances.size();
        double max = 0;
        for (int i = 0; i < size; i++) {
            double distanceIn = distances.get(i).doubleValue();
            if (distanceIn > max) {
                max = distanceIn;
            }
        }
        //logger.debug("max: " + max);
        return max;
    }

    public double calcMean(Vector<Double> distances) {

        double mean = 0.0;
        int size = distances.size();
        double totalValuesIn = 0.0;
        for (int i = 0; i < size; i++) {
            double valueIn = distances.get(i);
            totalValuesIn += valueIn;
        }
        mean = (double) totalValuesIn / (double) size;
        //logger.debug("mean: " + mean);
        return mean;
    }

    public double calcMedian(Vector<Double> distances) {

        double median = 0.0;
        int size = distances.size();
        if (size == 0) {
            median = 0.0;
        } else if (size == 1) {
            median = (double) distances.get(0);
        } else {
            double medianRank = (size + 1) / 2.0;

            //If median_rank is an integer, return value at that rank
            if (medianRank % 2 == 0) {
                int medianRankInt = (int) medianRank;
                median = (double) distances.get(medianRankInt - 1);
            } else {
                //If not, then average the two closest
                int lowerRank = (int) medianRank;
                int upperRank = lowerRank + 1;

                //Note that the vector index counts from 0 (not 1)
                double lowerVal = distances.get(lowerRank - 1);
                double upperVal = distances.get(upperRank - 1);

                median = (double) (lowerVal + upperVal) / 2.0;
            }
        }
        //logger.debug("median: " + median);
        return median;
    }

    public double calcQ1(Vector<Double> distances) {

        double q1 = 0.0;
        int size = distances.size();

        if (size == 0) {
            q1 = 0.0;
        } else if (size == 1 || size == 2) {
            q1 = (double) distances.get(0);
        } else {
            double medianRank = (size + 1) / 2.0;

            //Split the set in two
            int medianRankFloor = (int) medianRank;
            double quartileRank = (medianRankFloor + 1) / 2.0;

            int lowerRank = (int) quartileRank;
            int upperRank = lowerRank + 1;

            double lowerVal = distances.get(lowerRank - 1);
            double upperVal = distances.get(upperRank - 1);

            q1 = (double) (lowerVal + upperVal) / 2.0;
        }
        //logger.debug("q1: " + q1);
        return q1;
    }

    public double calcQ3(Vector<Double> distances) {

        double q3 = 0.0;
        int size = distances.size();
        if (size == 0) {
            q3 = 0.0;
        } else if (size == 1) {
            q3 = (double) distances.get(0);
        } else if (size == 2) {
            q3 = (double) distances.get(1);
        } else {

            double medianRank = (size + 1) / 2.0;

            //Split the set in two (and take second half)
            int medianRankFloor = (int) medianRank;
            int offset = medianRankFloor;
            double quartileRank = ((size - offset) + 1) / 2.0;

            int lowerRank = (int) quartileRank + offset;
            if (medianRank % 2 == 0) {
                lowerRank = lowerRank - 1;
            }
            int upperRank = lowerRank + 1;

            double lowerVal = distances.get(lowerRank - 1);
            double upperVal = distances.get(upperRank - 1);

            q3 = (double) (lowerVal + upperVal) / 2.0;
        }
        //logger.debug("q3: " + q3);
        return q3;
    }

    public Vector<String> getTotalStayTimes(Connection sourceConn, Vector<String> patientIds) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Vector<String> totalStayTimes = new Vector<String>();

        String sql = "SELECT Patient_Id,NSH_Time_of_Arrival,NSH_Time_of_Discharge FROM demographic WHERE Patient_id=? ORDER BY Patient_Id;";
        try {
            PreparedStatement ps = sourceConn.prepareStatement(sql);
            ps.setString(1,patientIds.get(0));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String idIn = rs.getString(1);
                if (idIn == null) {
                    idIn = "";
                }
                String arrivalTime = rs.getString(2);
                if (arrivalTime == null) {
                    arrivalTime = "";
                }
                String dischargeTime = rs.getString(3);
                if (dischargeTime == null) {
                    dischargeTime = "";
                }

                try {
                    java.util.Date arrivalTimeDate = df.parse(arrivalTime);
                    java.util.Date dischargeTimeDate = df.parse(dischargeTime);
                    long totalStayTimeMillis = dischargeTimeDate.getTime() - arrivalTimeDate.getTime();
                    long totalStayTimeMins = totalStayTimeMillis / 60000;

                    totalStayTimes.add("" + totalStayTimeMins);
                    logger.debug("-----");
                    logger.debug("idIn: " + idIn);
                    logger.debug("arrivalTime: " + arrivalTime);
                    logger.debug("dischargeTime: " + dischargeTime);
                    logger.debug("totalStayTimeMins: " + totalStayTimeMins);
                    logger.debug("-----");
                    
                } catch (ParseException pe) {
                    /*logger.debug("Parsing exception: " + pe.getMessage());
                    if(arrivalTime.equals("")){
                        logger.debug("arrivalTime (" + idIn + "): " + arrivalTime);
                    }else{
                        logger.debug("dischargeTime (" + idIn + "): " + dischargeTime);
                    }*/
                    totalStayTimes.add("Not available");
                }
            }
            rs.close();
        } catch (Exception e) {
            logger.debug("Error (totalStayTimes): " + e.getMessage());
        }
        return totalStayTimes;
    }

    /*public String getTimeWithinEvents(Connection sourceConn, String patientId, String eusigId, String holddown){
        
        logger.debug("patientId: " + patientId);
        logger.debug("eusigId: " + eusigId);
        logger.debug("holddown: " + holddown);
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");        
        //SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        long timeWithinEventsMins = -1;
        
        String sql = "SELECT start_time,end_time FROM event WHERE eusig_id=? AND holddown=? AND patient_id=? ORDER BY start_time,end_time;";        
        try {
            PreparedStatement ps = sourceConn.prepareStatement(sql);
            ps.setString(1,eusigId);
            ps.setString(2,holddown);
            ps.setString(3,patientId);
            ResultSet rs = ps.executeQuery();
            int recordCount = 0;
            java.util.Date startTime = null;
            java.util.Date endTime = null;            
            
            while (rs.next()) {
                if(recordCount == 0){
                    String startTimeIn = rs.getString(1);
                    if(startTimeIn == null){
                        startTimeIn = "";
                    }
                    startTime = df.parse(startTimeIn);                    
                }
                String endTimeIn = rs.getString(2);
                if (endTimeIn == null) {
                    endTimeIn = "";
                }                
                if(recordCount == 0){
                    endTime = df.parse(endTimeIn);
                }else{
                    //If this endTimeIn is later than the current endTime, then replace it with this one
                    if(endTime.compareTo(df.parse(endTimeIn)) == -1){
                        endTime = df.parse(endTimeIn);
                    }
                }
                recordCount++;
            }
            
            logger.debug("start time (after selection): " + startTime);
            logger.debug("end time (after selection): " + endTime);
            
            long timeWithinEventsMillis = endTime.getTime() - startTime.getTime();
            timeWithinEventsMins = timeWithinEventsMillis / 60000;                

            rs.close();
        } catch (Exception e) {
            logger.debug("Error (getTimeWithinEvents): " + e.getMessage());
        }
        return "" + timeWithinEventsMins;
    }*/
    
    public Vector<String> getListFromRun(Vector<String> patientIds, int patient_run){
        
        int lowerBound = 0;
        int upperBound = 25;
        
        if(patient_run == 1){
            lowerBound = 0;
            upperBound = 25;
        }else if(patient_run == 2){
            lowerBound = 25;
            upperBound = 50;
        }else if(patient_run == 3){
            lowerBound = 50;
            upperBound = 75;
        }else if(patient_run == 4){
            lowerBound = 75;
            upperBound = 100;
        }else if(patient_run == 5){
            lowerBound = 100;
            upperBound = 125;
        }else if(patient_run == 6){
            lowerBound = 125;
            upperBound = 150;
        }else if(patient_run == 7){
            lowerBound = 175;
            upperBound = 200;
        }else if(patient_run == 8){
            lowerBound = 200;
            upperBound = 225;
        }else if(patient_run == 9){
            lowerBound = 225;
            upperBound = 250;
        }else if(patient_run == 10){
            lowerBound = 250;
            upperBound = 262;
        }
        
        Vector<String> patientIdsOut = new Vector<String>();
        int totalPatientNum = patientIds.size();
        for(int i=lowerBound; i<upperBound; i++){            
            if(i < totalPatientNum){
                patientIdsOut.add(patientIds.get(i));
            }
        }
        return patientIdsOut;
    }
    
    public int getMaxTokenLine(Vector<String> thisPatientContribs){
        
        int maxTokenNum = 0;
        int maxTokenLine = 0;
        for(int i=0; i<thisPatientContribs.size(); i++){
            StringTokenizer st = new StringTokenizer(thisPatientContribs.get(i),",");
            int tokenNum = st.countTokens();
            if(tokenNum > maxTokenNum){
                maxTokenNum = tokenNum;
                maxTokenLine = i;
            }
        }
        return maxTokenLine;
    }
    
    public int getMaxTokenNum(Vector<String> thisPatientContribs){
        
        int maxTokenNum = 0;
        for(int i=0; i<thisPatientContribs.size(); i++){
            StringTokenizer st = new StringTokenizer(thisPatientContribs.get(i),",");
            int tokenNum = st.countTokens();
            if(tokenNum > maxTokenNum){
                maxTokenNum = tokenNum;                
            }
        }
        return maxTokenNum;
    }
    
    public void writeTreatmentInstances(String filepath, Vector<String> treatmentEventAssocCount, Vector<String> treatmentEventAssocWithinWindowCount){
        
        logger.debug("--- ASSOCIATED EVENT TREATMENT LIST START ---");
        for(int i=0; i<treatmentEventAssocCount.size(); i++){
            logger.debug(treatmentEventAssocCount.get(i));
        }
        logger.debug("--- ASSOCIATED EVENT TREATMENT LIST END ---");

        String filenameTreatmentEventAssoc = filepath + "treatment_event_assoc.txt";
        try{
            FileWriter fwTreatmentEventAssoc = new FileWriter(filenameTreatmentEventAssoc,true);
            BufferedWriter bwTreatmentEventAssoc = new BufferedWriter(fwTreatmentEventAssoc);
            for(int i=0; i<treatmentEventAssocCount.size(); i++){
                bwTreatmentEventAssoc.write(treatmentEventAssocCount.get(i) + "\r\n");
            }
            bwTreatmentEventAssoc.close();
            fwTreatmentEventAssoc.close();
        
        }catch(Exception e){
            logger.debug("I/O error: " + e.getMessage());
        }

        logger.debug("======");
        logger.debug("Number of events with associated treatments within window: " + treatmentEventAssocWithinWindowCount.size());

        String filenameTreatmentEventWithinWindowAssoc = filepath + "treatment_event_assoc_within_window.txt";
        try{
            FileWriter fwTreatmentEventWithinWindowAssoc = new FileWriter(filenameTreatmentEventWithinWindowAssoc,true);
            BufferedWriter bwTreatmentEventWithinWindowAssoc = new BufferedWriter(fwTreatmentEventWithinWindowAssoc);
            for(int i=0; i<treatmentEventAssocWithinWindowCount.size(); i++){
                bwTreatmentEventWithinWindowAssoc.write(treatmentEventAssocWithinWindowCount.get(i) + "\r\n");
            }
            bwTreatmentEventWithinWindowAssoc.close();
            fwTreatmentEventWithinWindowAssoc.close();

        }catch(Exception e){
            logger.debug("I/O error: " + e.getMessage());
        }

        
    }
    
    public void writeRegressionFiles(String filepath, int lowerIndex, int upperIndex, int splitRun, int patientNum, Vector<String> patientIds, Connection brainItConn, Vector<Double>[] allPatientDurationSet, Vector<Double>[] allPatientDistanceSet){
        
        Utilities util = new Utilities();
        
        /**
            * Patient ID
            * GOS Score - 0 = Good, 1 = Bad
            * Duration
            * Distance
        */
        String filename = filepath + "brainit_" + splitRun + ".csv";
        Vector<Vector> patientOutput = new Vector<Vector>();
        patientOutput = this.getPatientOutput(brainItConn,lowerIndex,upperIndex,splitRun,patientNum,patientIds,allPatientDurationSet,allPatientDistanceSet);
        util.createCSVfile(filename,patientOutput);

    }
    
    public String[] getCatStrs(int maxTokenNum, StringTokenizer st){
        
        String[] catStrs = new String[maxTokenNum];
        for(int i=0; i<maxTokenNum; i++){
            String tokenIn = st.nextToken().trim();
            int bracketIndex = tokenIn.indexOf("(");
            catStrs[i] = tokenIn.substring(0,bracketIndex).trim();
        }
        return catStrs;
    }
    
    public String getCategoryStr(Vector<String> thisPatientLevels){
        
        String categoryStr = "";
        for(int i=0; i<thisPatientLevels.size(); i++){
            String patientLevelIn = thisPatientLevels.get(i);
            categoryStr += "'" + patientLevelIn + "'";
            if(i != thisPatientLevels.size()-1){
                categoryStr += ",";
            }
        }
        return categoryStr;
    }
    
    public String getStackedSeriesStr(int maxTokenNum, String[] catStrs, String[] inputStrs){
        
        String stackedSeriesStr = "";
        for(int i=0; i<maxTokenNum; i++){
            stackedSeriesStr += "{ name: '" + catStrs[i] + "',";
            stackedSeriesStr += "  data: " + inputStrs[i] + " }";
            if(i != maxTokenNum-1){
                stackedSeriesStr += ",";
            }
        }
        return stackedSeriesStr;
    }
    
    public String[] getInputStrs(int maxTokenNum, Vector<String> thisPatientContribs, String[] catStrs){
        
        String[] inputStrs = new String[maxTokenNum];
        for(int i=0; i<maxTokenNum; i++){
            inputStrs[i] = "[";
        }    
    
        for(int i=0; i<thisPatientContribs.size(); i++){
            boolean[] inputAdded = new boolean[maxTokenNum];
            for(int j=0; j<maxTokenNum; j++){
                inputAdded[j] = false;
            }
    
            String patientContribIn = thisPatientContribs.get(i);
            StringTokenizer stIn = new StringTokenizer(patientContribIn,",");
            int thisTokenNumIn = stIn.countTokens();
        
            for(int j=0; j<thisTokenNumIn; j++){
                String tokenIn = stIn.nextToken();
            
                int openIndex = tokenIn.indexOf("(");
                int closeIndex = tokenIn.indexOf(")");
                String catIn = tokenIn.substring(0,openIndex);
                catIn = catIn.trim();
                String numIn = tokenIn.substring(openIndex+1,closeIndex);
                if(catIn.equals(catStrs[j])){
                    inputStrs[j] += numIn + ",";
                    inputAdded[j] = true;
                }else{
                    //Find the index of the inputStr where this input should go
                    boolean catFound = false;
                    int catCount = 0;
                    while(!catFound && catCount < maxTokenNum){
                        if(catIn.equals(catStrs[catCount])){
                            catFound = true;
                            inputStrs[catCount] += numIn + ",";
                            inputAdded[catCount] = true;
                        }else{
                            catCount++;
                        }
                    }
                }
            }    
            //Find the inputStr that is missing in this round
            if(thisTokenNumIn < maxTokenNum){
                for(int j=0; j<maxTokenNum; j++){
                    if(!inputAdded[j]){
                        inputStrs[j] += "0,";
                    }
                }
            }
        }

        for(int i=0; i<maxTokenNum; i++){
            inputStrs[i] = inputStrs[i].substring(0,inputStrs[i].length()-1);
            inputStrs[i] += "]";
        }
        return inputStrs;        
    }
    
    
    public String getEusigThreshold(Connection conn, String eusigId) {

        String threshold = "";
        String sql = "SELECT threshold FROM eusig_defn WHERE eusig_id=?;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,eusigId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                String thresholdIn = rs.getString(1);
                if (thresholdIn == null) {
                    thresholdIn = "";
                }            
                threshold = thresholdIn;
            }
            rs.close();
        } catch (Exception e) {
            logger.debug("Error (getEusigThreshold): " + e.getMessage());
        }
        return threshold;
    }
    
    public String getDatasetLabel(Connection conn, String datasetId) {

        String datasetName = "";
        String sql = "SELECT dataset_name FROM dataset WHERE dataset_id=?;";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,datasetId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                String datasetNameIn = rs.getString(1);
                if (datasetNameIn == null) {
                    datasetNameIn = "";
                }            
                datasetName = datasetNameIn;
            }
            rs.close();
        } catch (Exception e) {
            logger.debug("Error (getDatasetLabel): " + e.getMessage());
        }
        return datasetName;
    }
    
}
