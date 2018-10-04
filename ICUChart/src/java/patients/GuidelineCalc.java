/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patients;

import org.apache.log4j.Logger;
import java.util.*;
import java.io.BufferedWriter;

/**
 *
 * @author astell
 */
public class GuidelineCalc {

    private static final Logger logger = Logger.getLogger(GuidelineCalc.class);
    //private Vector<Double> guidelineAllDurations, guidelineAllDistances;
    private Vector<Double>[] guidelineTotalDurations, guidelineTotalDistances;
    private double percentCompliance, percentNonCompliance, totalPercentCompliance, totalPercentNonCompliance;
    private int totalContribCount;
    private String pressureEventTableHtml1, pressureEventTableHtml2, patientStayTableHtml;
    private String[] patientTotalOutputTable, patientIQRTable;
    private boolean[] patientWithTreatmentStmts;
    private double[] patientMeanCombined, patientMeanCombined2, patientMaxCombined2, patientMeanDuration, patientMeanDistance;
    private Vector<String>[] patientLevels, patientContribs;
    
    private Vector<Double>[] overallGuidelineIndDurations, overallGuidelineIndDistances, overallGuidelineCombineds, overallGuidelineCombineds2;        
    
    private HashMap<Double,Double>[] eventGuidelineDeviations;
    private Vector<Vector> totalGuidelineDeviations;
        
    private TreeMap<Double,Vector<String>> nonComplianceReasons;
    
    private double[] defaultTime,nonDefaultTime;
    

    public GuidelineCalc() {
        guidelineTotalDistances = new Vector[0];
        //guidelineAllDistances = new Vector<Double>();
        guidelineTotalDurations = new Vector[0];
        //guidelineAllDurations = new Vector<Double>();
        percentCompliance = 0;
        percentNonCompliance = 0;
        totalPercentCompliance = 0;
        totalPercentNonCompliance = 0;
        totalContribCount = 0;
        pressureEventTableHtml1 = "";
        pressureEventTableHtml2 = "";
        patientStayTableHtml = "";
        patientTotalOutputTable = new String[0];
        patientIQRTable = new String[0];
        totalGuidelineDeviations = new Vector<Vector>();
        
        nonComplianceReasons = new TreeMap<Double,Vector<String>>();
        
        overallGuidelineIndDurations = new Vector[0];
        overallGuidelineIndDistances = new Vector[0];
        overallGuidelineCombineds = new Vector[0];
        overallGuidelineCombineds2 = new Vector[0];
        
    }

    public Vector<Double> getGuidelineTotalDistances(int index) {
        return guidelineTotalDistances[index];
    }

    public Vector[] getGuidelineTotalDistances() {
        return guidelineTotalDistances;
    }

    public void initGuidelineTotalDistances(int size) {
        guidelineTotalDistances = new Vector[size];
        eventGuidelineDeviations = new HashMap[size];
        percentCompliance = 0.0;
        percentNonCompliance = 0.0;
        nonComplianceReasons = new TreeMap<Double,Vector<String>>();
    }
    
    public void clearEventMetrics() {
        guidelineTotalDistances = new Vector[0];
        percentCompliance = 0.0;
        percentNonCompliance = 0.0;
        nonComplianceReasons = new TreeMap<Double,Vector<String>>();
    }

    /*public Vector<Double> getGuidelineAllDistances() {
        return guidelineAllDistances;
    }*/

    public Vector<Double> getGuidelineTotalDurations(int index) {
        return guidelineTotalDurations[index];
    }

    public Vector[] getGuidelineTotalDurations() {
        return guidelineTotalDurations;
    }

    public void initGuidelineTotalDurations(int size) {
        guidelineTotalDurations = new Vector[size];
    }

    /*public Vector<Double> getGuidelineAllDurations() {
        return guidelineAllDurations;
    }*/

    public double getPercentCompliance() {
        return percentCompliance;
    }

    public double getPercentNonCompliance() {
        return percentNonCompliance;
    }

    public double getTotalPercentCompliance() {
        return totalPercentCompliance;
    }

    public double getTotalPercentNonCompliance() {
        return totalPercentNonCompliance;
    }

    public int getTotalContribCount() {
        return totalContribCount;
    }

    public String getPressureEventTableHtml1() {
        return pressureEventTableHtml1;
    }
    
    public String getPressureEventTableHtml2() {
        return pressureEventTableHtml2;
    }

    public String getPatientStayTableHtml() {
        return patientStayTableHtml;
    }
    
    public void initPatientTables(int patientNum){
        patientTotalOutputTable = new String[patientNum];
        patientIQRTable = new String[patientNum];
        patientWithTreatmentStmts = new boolean[patientNum];
        patientMeanDuration = new double[patientNum];
        patientMeanDistance = new double[patientNum];
        patientMeanCombined = new double[patientNum];
        patientMeanCombined2 = new double[patientNum];
        patientMaxCombined2 = new double[patientNum];
        
        overallGuidelineIndDurations = new Vector[patientNum];
        overallGuidelineIndDistances = new Vector[patientNum];
        overallGuidelineCombineds = new Vector[patientNum];
        overallGuidelineCombineds2 = new Vector[patientNum];
        
        patientLevels = new Vector[patientNum];
        patientContribs = new Vector[patientNum];
        
        defaultTime = new double[patientNum];
        nonDefaultTime = new double[patientNum];
    }
    
    public double getDefaultTime(int index){
        return defaultTime[index];
    }
    
    public double getNonDefaultTime(int index){
        return nonDefaultTime[index];
    }
    
    public String getPatientTotalOutputTable(int index){
        return patientTotalOutputTable[index];
    }
    
    public String getPatientIQRTable(int index){
        return patientIQRTable[index];
    }
    
    public String getPatientIQRData(int index, String iqrRefLine){
        
        String thisPatientIQRData = "";
        String thisPatientIQRTable = patientIQRTable[index];
        
        int rowTagIndex = thisPatientIQRTable.indexOf("<tr>");
        int rowTagEndIndex = thisPatientIQRTable.indexOf("</tr>");
        int lineNum = 4;
        String[] linesIn = new String[lineNum];
        String remainderStr = thisPatientIQRTable;
        for(int i=0; i<lineNum; i++){
            linesIn[i] = remainderStr.substring(rowTagIndex,rowTagEndIndex+5);
            //logger.debug("linesIn[" + i + "]: " + linesIn[i]);
            remainderStr = remainderStr.substring(rowTagEndIndex+5,remainderStr.length());
            rowTagIndex = remainderStr.indexOf("<tr>");
            rowTagEndIndex = remainderStr.indexOf("</tr>");        
        }
        if(iqrRefLine.equals("Non-adherence")){
            thisPatientIQRData = linesIn[0];
        }else if(iqrRefLine.equals("Duration")){
            thisPatientIQRData = linesIn[1];
        }else if(iqrRefLine.equals("A")){
            thisPatientIQRData = linesIn[2];
        }else if(iqrRefLine.equals("B")){
            thisPatientIQRData = linesIn[3];
        }
        
        //Process away the td tags
        int cellTagIndex = thisPatientIQRData.indexOf("<td>");
        int cellTagEndIndex = thisPatientIQRData.indexOf("</td>");
        int cellNum = 7;
        String[] cellsIn = new String[cellNum];
        String remainderCellStr = thisPatientIQRData;
        for(int i=0; i<cellNum; i++){
            cellsIn[i] = remainderCellStr.substring(cellTagIndex,cellTagEndIndex+5);
            //logger.debug("cellsIn[" + i + "]: " + cellsIn[i]);
            remainderCellStr = remainderCellStr.substring(cellTagEndIndex+5,remainderCellStr.length());
            cellTagIndex = remainderCellStr.indexOf("<td>");
            cellTagEndIndex = remainderCellStr.indexOf("</td>");
        }
        thisPatientIQRData = "[";
        for(int i=1; i<cellNum; i++){
            String taggedCell = cellsIn[i];
            int cellTag = taggedCell.indexOf("<td>");
            int cellEndTag = taggedCell.indexOf("</td>");
            String untaggedCell = taggedCell.substring(cellTag+4,cellEndTag);            
            if(untaggedCell.equals("?")){ //WTAF?
                untaggedCell = "0";
            }
            
            if(i != 3){ //This drops the mean value from the IQR graph
                thisPatientIQRData += "" + untaggedCell;
                if(i != cellNum-1){
                    thisPatientIQRData += ",";
                }   
            }
        }
        thisPatientIQRData += "]";        
        return thisPatientIQRData;
    }
    
    public boolean getPatientWithTreatmentStmts(int index){
        return patientWithTreatmentStmts[index];
    }
    
    public double getPatientMeanDuration(int index){
        return patientMeanDuration[index];
    }
    
    public double getPatientMeanDistance(int index){
        return patientMeanDistance[index];
    }

    public double getPatientMeanCombined(int index){
        return patientMeanCombined[index];
    }
    
    public double getPatientMeanCombined2(int index){
        return patientMeanCombined2[index];
    }
    
    public double getPatientMaxCombined2(int index){
        return patientMaxCombined2[index];
    }
    
    public Vector<String> getPatientLevels(int index){
        return patientLevels[index];
    }
    
    public Vector<String> getPatientContribs(int index){
        return patientContribs[index];
    }

    
    public void calculate(int guidelineSize, Vector<Vector> eventGuidelineDistancesIn, int eventIndex) {

        Vector<Double> thisGuidelineTotalDistances = new Vector<Double>();
        Vector<Double> uniqueDistanceNumbers = new Vector<Double>();
        HashMap<Double,Double> uniqueDistanceNumberCount = new HashMap<Double,Double>();
        //logger.debug("guidelineSize (calculate): " + guidelineSize);
        for (int i = 0; i < guidelineSize; i++) {
            Vector<Vector> guidelineNumberSetIn = eventGuidelineDistancesIn.get(i);
            int reasonNumberSize = guidelineNumberSetIn.size();
            //logger.debug("reasonNumberSize: " + reasonNumberSize);
            double distanceTotal = 0;
            Vector<String> thisNonComplianceReasons = new Vector<String>();
            for (int j = 0; j < reasonNumberSize; j++) {
                //logger.debug("guidelineNumberSetIn.get(" + j + "): " + guidelineNumberSetIn.get(j));
                String numberIn = (String) guidelineNumberSetIn.get(j).get(0); //THIS IS THE NUMBER (WEIGHT)
                double numberInDouble = Double.parseDouble(numberIn);                
                distanceTotal += numberInDouble;
                if (numberInDouble != 0) {
                    String reasonIn = (String) guidelineNumberSetIn.get(j).get(1); //THIS IS THE TEXT REASON
                    
                    //ADDING IN AN EXTRA NUMBER TO THE TEXT TO MAKE THE CONTRIBUTION CLEARER:
                    //Will divide the weighting by the reason number and express as percentage
                    double numberAsPercentDouble = (numberInDouble / reasonNumberSize) * 100.0;
                    numberAsPercentDouble = Math.round(numberAsPercentDouble*100.0)/100.0;
                    String numberAsPercent = "" + numberAsPercentDouble;                    
                    /*if(!reasonIn.equals("Treatment missing") && !reasonIn.equals("Time to treatment")){
                        logger.debug("reasonIn: " + reasonIn);
                        logger.debug("---");
                    }*/
                    thisNonComplianceReasons.add(reasonIn + " (" + numberAsPercent + ")");
                }                        
            }
            double distanceTotalScaled = distanceTotal / (double) reasonNumberSize;
            distanceTotalScaled *= 100.0;
            
            //Add this to vector of total distances
            Double distanceTotalScaledDouble = new Double(distanceTotalScaled);            
            //Format the distance to 2 d.p.
            distanceTotalScaledDouble = Math.round(distanceTotalScaledDouble*100.0)/100.0;
            thisGuidelineTotalDistances.add(distanceTotalScaledDouble);
            
            //Add to the count of unique distances if not there            
            if(!uniqueDistanceNumbers.contains(distanceTotalScaledDouble)){                
                uniqueDistanceNumbers.add(distanceTotalScaledDouble);
                uniqueDistanceNumberCount.put(distanceTotalScaledDouble,1.0);
                nonComplianceReasons.put(distanceTotalScaledDouble,thisNonComplianceReasons);
            }else{                
                Double thisCountIn = uniqueDistanceNumberCount.get(distanceTotalScaledDouble);                
                Double thisCountOut = thisCountIn + 1.0;                
                uniqueDistanceNumberCount.put(distanceTotalScaledDouble,thisCountOut);
            }
        }
        //logger.debug("thisGuidelineTotalDistances.size(): " + thisGuidelineTotalDistances.size());
        //logger.debug("eventIndex: " + eventIndex);
        
        guidelineTotalDistances[eventIndex] = thisGuidelineTotalDistances;
        eventGuidelineDeviations[eventIndex] = uniqueDistanceNumberCount;
    }

    public void calculateEventAdherence(Vector<Event> events, Vector<GuidelineDistance> guidelineDistances, ListPatients patientList, String pid, Vector<Treatment> treatments, int patientIndex, /*BufferedWriter bw,*/ boolean removeDefault) throws Exception{

        this.initGuidelineTotalDistances(events.size());        

        Vector<Double> guidelineAllDistances = new Vector<Double>();        
        Vector<Double> uniqueDistances = new Vector<Double>();
        TreeMap<Double,Integer> uniqueDistanceCounts = new TreeMap<Double,Integer>();        
        
        Vector<Vector> guidelineAllDurations = new Vector<Vector>();
        Vector<Double> lastLevel = new Vector<Double>();
        Double levelCount = 0.0;
        Double lastDistanceIn = -1.0;            
        for (int k = 0; k < events.size(); k++) {
        
            String eventIdIn = "" + k;
            int eventIdInt = Integer.parseInt(eventIdIn);
            Vector<Vector> eventGuidelineDistancesIn = guidelineDistances.get(eventIdInt).getValueLists();
            int guidelineSize = eventGuidelineDistancesIn.size();
            
            //Calculate the guideline distances, the compliance counts and the non-compliance values
            this.calculate(guidelineSize, eventGuidelineDistancesIn, k);

            //Add up all the distances and associated numbers (and add to total running count of distances)
            Vector<Double> guidelineTotalDistanceThisEvent = guidelineTotalDistances[k];
            for(int m = 0; m < guidelineTotalDistanceThisEvent.size(); m++){
                Double distanceIn = guidelineTotalDistanceThisEvent.get(m);                    
                if(!uniqueDistances.contains(distanceIn)){                    
                    uniqueDistances.add(distanceIn);
                    uniqueDistanceCounts.put(distanceIn,1);
                }else{
                    Integer distanceInCount = uniqueDistanceCounts.get(distanceIn);
                    distanceInCount++;
                    uniqueDistanceCounts.put(distanceIn,distanceInCount);
                }                
                guidelineAllDistances.add(distanceIn);                
                
                //Add to the number of unique "levels" here (for the guidelineAllDurations)
                if(!distanceIn.equals(lastDistanceIn) && !levelCount.equals(0.0)){                    
                    //Add the last distance and the final count to lastLevel, then add to the durations
                    lastLevel.add(lastDistanceIn);
                    lastLevel.add(levelCount);
                    guidelineAllDurations.add(lastLevel);
                    
                    //Reset the counters
                    lastLevel = new Vector<Double>();
                    levelCount = 1.0;         
                    lastDistanceIn = distanceIn;
                }else{
                    //Increment the counter
                    levelCount = levelCount + 1.0;                    
                    lastDistanceIn = distanceIn;
                    if(m == guidelineTotalDistanceThisEvent.size()-1){
                        lastLevel.add(lastDistanceIn);
                        lastLevel.add(levelCount);
                        guidelineAllDurations.add(lastLevel);                    
                        
                        //Reset the counters
                        lastLevel = new Vector<Double>();
                        levelCount = 0.0;
                        lastDistanceIn = distanceIn;                    
                    }
                }
            }            
        }
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        
        //Now display this information on the total results for each patient
        String thisPatientTableOutput = "";        
        Set uniqueDistancesSet = uniqueDistanceCounts.keySet();
        Iterator uniqueDistancesIter = uniqueDistancesSet.iterator();
        Vector<String> thisLevelSet = new Vector<String>();
        Vector<String> thisContribSet = new Vector<String>();
        
        //Set the max level so that the default state (max) can be removed later
        Double maxValue = new Double(-1);
        while (uniqueDistancesIter.hasNext()) {
            Double distanceValue = (Double) uniqueDistancesIter.next();            
            if(distanceValue > maxValue){
                maxValue = distanceValue;
            }
        }
        
        //Reset the iterator to run it again
        uniqueDistancesIter = uniqueDistancesSet.iterator();
        while (uniqueDistancesIter.hasNext()) {
            Double distanceValue = (Double) uniqueDistancesIter.next();
            Integer durationIn = (Integer) uniqueDistanceCounts.get(distanceValue);            
            
            Vector<String> thisNonComplianceReasons = nonComplianceReasons.get(distanceValue);
            String thisNonComplianceReasonsStr = "";
            for(int m = 0; m<thisNonComplianceReasons.size(); m++){
                thisNonComplianceReasonsStr += "" + thisNonComplianceReasons.get(m);
                if(m != thisNonComplianceReasons.size()-1){
                    thisNonComplianceReasonsStr += ", ";
                }
            }

            if(removeDefault){
                if(!distanceValue.equals(maxValue)){
                    thisPatientTableOutput += "<tr><td>" + df.format(durationIn) + "</td><td>" + df.format(distanceValue) + "</td><td>" + thisNonComplianceReasonsStr + "</td></tr>";            
                    thisLevelSet.add("" + df.format(distanceValue));
                    thisContribSet.add(thisNonComplianceReasonsStr);
                }
            }else{
                thisPatientTableOutput += "<tr><td>" + df.format(durationIn) + "</td><td>" + df.format(distanceValue) + "</td><td>" + thisNonComplianceReasonsStr + "</td></tr>";            
                thisLevelSet.add("" + df.format(distanceValue));
                thisContribSet.add(thisNonComplianceReasonsStr);
            }
        }
        thisPatientTableOutput += "";        
        patientTotalOutputTable[patientIndex] = thisPatientTableOutput;
        
        patientLevels[patientIndex] = thisLevelSet;
        patientContribs[patientIndex] = thisContribSet;
            
        //Modify the distance instances if the default is to be removed
        Vector<Double> guidelineAllDistancesDisp = new Vector<Double>();
        if(removeDefault){
            for(int i=0; i<guidelineAllDistances.size(); i++){
                Double distanceIn = guidelineAllDistances.get(i);
                if(!distanceIn.equals(maxValue)){
                    guidelineAllDistancesDisp.add(distanceIn);
                }
            }
        }else{
            for(int i=0; i<guidelineAllDistances.size(); i++){
                Double distanceIn = guidelineAllDistances.get(i);
                guidelineAllDistancesDisp.add(distanceIn);
            }
        }
        
        //Calculate the IQR spread and add to table output
        
        Collections.sort(guidelineAllDistancesDisp);
        //logger.debug("calculating IQR...");
        double max = patientList.calcMax(guidelineAllDistancesDisp);
        double min = patientList.calcMin(guidelineAllDistancesDisp);
        double mean = patientList.calcMean(guidelineAllDistancesDisp);
        double median = patientList.calcMedian(guidelineAllDistancesDisp);
        double q1 = patientList.calcQ1(guidelineAllDistancesDisp);
        double q3 = patientList.calcQ3(guidelineAllDistancesDisp);
        
        if(mean != median){
            patientWithTreatmentStmts[patientIndex] = true;
        }else{
            patientWithTreatmentStmts[patientIndex] = false;
        }
        
        //logger.debug("=====");
        patientIQRTable[patientIndex] = "<tr><td>Non-adherence (%)</td><td>" + df.format(min) + "</td><td>" + df.format(q1) + "</td><td>" + df.format(mean) + "</td><td>" + df.format(median) + "</td><td>" + df.format(q3) + "</td><td>" + df.format(max) + "</td></tr>";
        //patientIQRData[patientIndex] = "[" + df.format(min) + "," + df.format(q1) + "," + df.format(mean) + "," + df.format(median) + "," + df.format(q3) + "," + df.format(max) + "],";
        
        //logger.debug("guidelineAllDurations.size(): " + guidelineAllDurations.size());        
        //logger.debug("=====");
        
        //Modify the duration instances if the default is to be removed
        Vector<Vector> guidelineAllDurationsDisp = new Vector<Vector>();
        if(removeDefault){
            for(int i=0; i<guidelineAllDurations.size(); i++){
                Vector<Double> durationIn = guidelineAllDurations.get(i);
                if(!durationIn.get(0).equals(maxValue)){
                    guidelineAllDurationsDisp.add(durationIn);
                }
            }
        }else{
            for(int i=0; i<guidelineAllDurations.size(); i++){
                Vector<Double> durationIn = guidelineAllDurations.get(i);
                guidelineAllDurationsDisp.add(durationIn);
                if(!durationIn.get(0).equals(maxValue)){
                    nonDefaultTime[patientIndex] += durationIn.get(1);
                }else{
                    defaultTime[patientIndex] += durationIn.get(1);
                }
            }
        }
        
        Vector<Double> guidelineIndDurations = new Vector<Double>();
        Vector<Double> guidelineIndDistances = new Vector<Double>();
        Vector<Double> guidelineCombineds = new Vector<Double>();
        Vector<Double> guidelineCombineds2 = new Vector<Double>();
        for(int k = 0; k<guidelineAllDurationsDisp.size(); k++){
            Double distanceIn = (Double)guidelineAllDurationsDisp.get(k).get(0);
            Double durationIn = (Double)guidelineAllDurationsDisp.get(k).get(1);            
            Double combinedIn = durationIn / distanceIn;
            Double combinedIn2 = durationIn * distanceIn;
            
            guidelineIndDurations.add(durationIn);
            guidelineIndDistances.add(distanceIn);
            guidelineCombineds.add(combinedIn);
            guidelineCombineds2.add(combinedIn2);
        }
        
        //logger.debug("guidelineIndDistances (" + patientIndex + "): " + guidelineIndDistances);
        //logger.debug("guidelineIndDurations (" + patientIndex + "): " + guidelineIndDurations);
        /*bw.write("" + pid + "\r\n");        
        //logger.debug("guidelineIndDistances.size(): " + guidelineIndDistances.size());
        bw.write("Distances: " + guidelineIndDistances + "\r\n");        
        //logger.debug("guidelineIndDurations.size(): " + guidelineIndDurations.size());
        bw.write("Durations: " + guidelineIndDurations + "\r\n");
        /*logger.debug("guidelineCombineds: " + guidelineCombineds);
        logger.debug("guidelineCombineds2: " + guidelineCombineds2);
        bw.write("====" + "\r\n");*/
        
        overallGuidelineIndDistances[patientIndex] = guidelineIndDistances;
        overallGuidelineIndDurations[patientIndex] = guidelineIndDurations;
        overallGuidelineCombineds[patientIndex] = guidelineCombineds;
        overallGuidelineCombineds2[patientIndex] = guidelineCombineds2;
        
        Collections.sort(guidelineIndDurations);
        max = patientList.calcMax(guidelineIndDurations);
        min = patientList.calcMin(guidelineIndDurations);
        mean = patientList.calcMean(guidelineIndDurations);
        median = patientList.calcMedian(guidelineIndDurations);
        q1 = patientList.calcQ1(guidelineIndDurations);
        q3 = patientList.calcQ3(guidelineIndDurations);
        patientMeanDuration[patientIndex] = mean;
        patientIQRTable[patientIndex] += "<tr><td>Duration (mins)</td><td>" + min + "</td><td>" + q1 + "</td><td>" + df.format(mean) + "</td><td>" + median + "</td><td>" + q3 + "</td><td>" + max + "</td></tr>";
        //patientIQRData[patientIndex] += "[" + min + "," + q1 + "," + df.format(mean) + "," + median + "," + q3 + "," + max + "],";
        
        mean = patientList.calcMean(guidelineIndDistances);
        patientMeanDistance[patientIndex] = mean;
        
        Collections.sort(guidelineCombineds);
        max = patientList.calcMax(guidelineCombineds);
        min = patientList.calcMin(guidelineCombineds);
        mean = patientList.calcMean(guidelineCombineds);
        patientMeanCombined[patientIndex] = mean;
        //logger.debug("mean (combined): " + mean);
        
        median = patientList.calcMedian(guidelineCombineds);
        q1 = patientList.calcQ1(guidelineCombineds);
        q3 = patientList.calcQ3(guidelineCombineds);
        patientIQRTable[patientIndex] += "<tr><td>Duration / Non-adherence</td><td>" + df.format(min) + "</td><td>" + df.format(q1) + "</td><td>" + df.format(mean) + "</td><td>" + df.format(median) + "</td><td>" + df.format(q3) + "</td><td>" + df.format(max) + "</td></tr>";
        //patientIQRData[patientIndex] += "[" + df.format(min) + "," + df.format(q1) + "," + df.format(mean) + "," + df.format(median) + "," + df.format(q3) + "," + df.format(max) + "],";
        
        Collections.sort(guidelineCombineds2);
        max = patientList.calcMax(guidelineCombineds2);
        min = patientList.calcMin(guidelineCombineds2);
        mean = patientList.calcMean(guidelineCombineds2);
        patientMeanCombined2[patientIndex] = mean;
        patientMaxCombined2[patientIndex] = max;
        //logger.debug("mean (combined2): " + mean);
                
        median = patientList.calcMedian(guidelineCombineds2);
        q1 = patientList.calcQ1(guidelineCombineds2);
        q3 = patientList.calcQ3(guidelineCombineds2);
        patientIQRTable[patientIndex] += "<tr><td>Duration * Non-adherence</td><td>" + df.format(min) + "</td><td>" + df.format(q1) + "</td><td>" + df.format(mean) + "</td><td>" + df.format(median) + "</td><td>" + df.format(q3) + "</td><td>" + df.format(max) + "</td></tr>";
        //patientIQRData[patientIndex] += "[" + df.format(min) + "," + df.format(q1) + "," + df.format(mean) + "," + df.format(median) + "," + df.format(q3) + "," + df.format(max) + "]";
        
        //Clear the guidelineTotalDistances here
        this.clearEventMetrics();
        
    }
    
    public Vector<Double> getOverallGuidelineIndDistances(int index){
        return overallGuidelineIndDistances[index];
    }
    
    public Vector<Double> getOverallGuidelineIndDurations(int index){
        return overallGuidelineIndDurations[index];
    }
    
    public Vector<Double> getOverallGuidelineCombineds(int index){
        return overallGuidelineCombineds[index];
    }
    
    public Vector<Double> getOverallGuidelineCombineds2(int index){
        return overallGuidelineCombineds2[index];
    }
    
    
    

}
