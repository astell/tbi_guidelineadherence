/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import org.jfree.chart.*;
import org.jfree.data.category.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.*;
import org.jfree.data.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.ChartUtilities;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import java.util.TreeMap;
import java.util.Vector;

/**
 *
 * @author astell
 */
public class CreateLineGraph {

    private Logger logger;

    public CreateLineGraph(Logger _logger) {
        logger = _logger;
    }

    public void createTimeChart(String filepath, TreeMap timesIn, TreeMap totalEventNums, String chartTitle) {

        boolean printMean = false;
        if(chartTitle.equals("Mean times")){
            printMean = true;
        }
        
        Set totalEventSet = totalEventNums.keySet();
        Iterator totalEventIter = totalEventSet.iterator();

        //Split into two arrays (one for CPP, one for ICP)
        int cppCount = 0;
        int icpCount = 0;
        while (totalEventIter.hasNext()) {
            String totalEventName = (String) totalEventIter.next();            
            if (totalEventName.contains("Lowered CPP")) {
                cppCount++;
            } else {
                icpCount++;
            }
        }

        int[] cppValues = new int[cppCount];
        int[] icpValues = new int[icpCount];
        String[] cppEventNames = new String[cppCount];
        String[] icpEventNames = new String[icpCount];

        int cppIndex = 0;
        int icpIndex = 0;
        totalEventSet = totalEventNums.keySet();
        totalEventIter = totalEventSet.iterator();
        while (totalEventIter.hasNext()) {
            String totalEventName = (String) totalEventIter.next();
            
            //Now retrieve the associated time information
            //Now retrieve the mean and median times to treatment for this parameter definition                
            java.util.Vector<Date> theseTreatmentTimes = (java.util.Vector<Date>) timesIn.get(totalEventName);
            String meanTimeStr = "0";
            String medianTimeStr = "0";
            if (theseTreatmentTimes != null) {
                meanTimeStr = this.calculateMeanTime(theseTreatmentTimes);
                medianTimeStr = this.calculateMedianTime(theseTreatmentTimes);                
            }

            if (totalEventName.contains("Lowered CPP")) {
                if(printMean){
                    cppValues[cppIndex] = Integer.parseInt(meanTimeStr);
                }else{
                    cppValues[cppIndex] = Integer.parseInt(medianTimeStr);
                }
                cppEventNames[cppIndex] = totalEventName;
                cppIndex++;
            } else {
                if(printMean){
                    icpValues[icpIndex] = Integer.parseInt(meanTimeStr);
                }else{
                    icpValues[icpIndex] = Integer.parseInt(medianTimeStr);
                }
                icpEventNames[icpIndex] = totalEventName;
                icpIndex++;
            }
        }

        //Truncate the dataset label slightly
        for (int i = 0; i < cppCount; i++) {
            cppEventNames[i] = cppEventNames[i].substring(cppEventNames[i].indexOf("#"), cppEventNames[i].length());
        }
        for (int i = 0; i < icpCount; i++) {
            icpEventNames[i] = icpEventNames[i].substring(icpEventNames[i].indexOf("#"), icpEventNames[i].length());
        }

        DefaultCategoryDataset cppDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset icpDataset = new DefaultCategoryDataset();

        for (int i = 0; i < cppCount; i++) {
            cppDataset.setValue(cppValues[i], "Time (mins)", "" + cppEventNames[i]);
        }
        for (int i = 0; i < icpCount; i++) {
            icpDataset.setValue(icpValues[i], "Time (mins)", "" + icpEventNames[i]);
        }

        JFreeChart cppChart = ChartFactory.createLineChart("" + chartTitle + " (CPP)", "Parameter", "Time (mins)", cppDataset, PlotOrientation.VERTICAL, false, true, false);
        //.createBarChart("" + chartTitle + " (CPP)", "Parameter", "Time (mins)", cppDataset,PlotOrientation.VERTICAL, false, true, false);
        int width = 1000;
        int height = 600;
        String filename = filepath + "cpp_line_graph.png";
        try {
            ChartUtilities.saveChartAsPNG(new File(filename), cppChart, width, height);
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
        }

        JFreeChart icpChart = ChartFactory.createLineChart("" + chartTitle + " (ICP)", "Parameter", "Time (mins)", icpDataset, PlotOrientation.VERTICAL, false, true, false);
        width = 1300;
        height = 600;
        filename = filepath + "icp_line_graph.png";
        try {
            ChartUtilities.saveChartAsPNG(new File(filename), icpChart, width, height);
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
        }
    }
    
    private String calculateMeanTime(java.util.Vector<Date> timeToTreatments) {
        
        if (timeToTreatments.size() > 0) {
            //Find the mean time
            int treatmentNum = timeToTreatments.size();
            int totalTreatmentTime = 0;
            for (int i = 0; i < treatmentNum; i++) {
                int timeToTreatmentsMin = this.getTimeAsMins(timeToTreatments.get(i));
                totalTreatmentTime += timeToTreatmentsMin;
            }
            int meanTreatmentTime = totalTreatmentTime / treatmentNum;
            return "" + meanTreatmentTime;
        } else {
            return null;
        }
    }

    private String calculateMedianTime(java.util.Vector<Date> timeToTreatments) {

        timeToTreatments = this.orderTimes(timeToTreatments);        
        
        //And find the median time
        int treatmentNum = timeToTreatments.size();
        int index = -1;
        if (treatmentNum % 2 == 0) {
            //If even, take the value next below the middle
            index = (treatmentNum / 2) - 1;
        } else {
            index = (int) treatmentNum / 2;
        }
        //logger.info("MEDIAN: " + this.getTimeAsMins(timeToTreatments.get(index)));
        return "" + this.getTimeAsMins(timeToTreatments.get(index));

    }
    
    private int getTimeAsMins(Date dateIn) {
        long timeInMillis = dateIn.getTime();
        long timeInSecs = timeInMillis / 1000;
        long timeInMins = timeInSecs / 60;
        return (int) timeInMins;
    }
    
    private Vector<Date> orderTimes(Vector<Date> timesIn){
        
        int timeNum = timesIn.size();
        final Date[] times = new Date[timeNum];
        for(int i=0; i<timeNum; i++){
            times[i] = timesIn.get(i);
        }
        
        for (int i = 0; i < timeNum; i++) {            
            
            // Assume first element is min
            int minIndex = i;
            Date min = times[i];
            for (int j = i + 1; j < timeNum; j++) {                
                Date nextTimeIn = times[j];
                if (nextTimeIn.compareTo(min) == -1){
                    minIndex = j;
                    min = nextTimeIn;
                }
            }
            
            Date temp = times[i];
            times[i] = min;
            times[minIndex] = temp;                        
        }
        
        Vector<Date> timesOut = new Vector<Date>();
        for(int i=0; i<timeNum; i++){
            timesOut.add(times[i]);
        }
        
        return timesOut;
    }
}
