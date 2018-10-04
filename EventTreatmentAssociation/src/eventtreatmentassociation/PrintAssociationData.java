/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

/**
 *
 * @author astell
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import java.util.Vector;
import java.util.Date;

import graph.*;

public class PrintAssociationData {

    private Logger logger;
    private final static int WINDOW_DEFN_NUM = 4;
    private boolean FIRST_TREATMENT_ONLY;
    private Vector<String> centreIDs;

    public PrintAssociationData(Logger _logger, boolean _firstTreatmentOnly, Vector<String> _centreIDs) {
        logger = _logger;
        FIRST_TREATMENT_ONLY = _firstTreatmentOnly;
        centreIDs = _centreIDs;
    }

    private void printOutput(Vector<AssociationData> ads, BufferedWriter bw, String centreID, String filepath) throws Exception {

        //TREATMENT NUMBER (ONLY ONE FOR ALL CASES => SIMPLE)
        //Pull out the treatment number per patient and add to total (lists stored per ad object)
        int adNum = ads.size();
        int totalTreatmentNum = 0;
        for (int i = 0; i < adNum; i++) {
            AssociationData ad = ads.get(i);
            int treatmentNum = ad.getTotalTreatments().size();
            totalTreatmentNum += treatmentNum;
        }
        bw.write("Total treatment number: " + totalTreatmentNum + "\n");

        bw.write("=====\n");

        bw.write("\n");
        bw.write("Parameter definition - (total event number)\n");
        //For each definition (32 x 4)
        //Pull out the total number of events and add to overall counts for definitions
        TreeMap totalEventNums = new TreeMap();
        for (int i = 0; i < adNum; i++) {
            AssociationData adIn = ads.get(i);
            TreeMap eventListsIn = adIn.getEventLists();

            Set eventListInSet = eventListsIn.keySet();
            Iterator eventListInIter = eventListInSet.iterator();
            while (eventListInIter.hasNext()) {
                String eventListName = (String) eventListInIter.next();
                Vector<Event> eventList = (Vector<Event>) eventListsIn.get(eventListName);
                int eventListSize = eventList.size();

                //Add this to the value that is currently there
                Integer currentNum = (Integer) totalEventNums.get(eventListName);
                if (currentNum == null) {
                    totalEventNums.put(eventListName, (Integer) eventListSize);
                } else {
                    totalEventNums.put(eventListName, (Integer) (eventListSize + currentNum));
                }
            }
        }

        //Print to file        
        Set totalEventSet = totalEventNums.keySet();
        Iterator totalEventIter = totalEventSet.iterator();
        while (totalEventIter.hasNext()) {
            String totalEventName = (String) totalEventIter.next();
            Integer totalEventNum = (Integer) totalEventNums.get(totalEventName);
            bw.write("" + totalEventName + " - (" + totalEventNum + ")\n");
        }

        if (centreID.equals("")) {
            //Graph and print as bar chart
            CreateBarChart cbc = new CreateBarChart(logger);
            cbc.createEventChart(filepath, totalEventNums, "Event number distribution");
        }

        bw.write("=====\n");

        this.printAssociationData(bw, ads, totalEventNums, centreID, filepath);
    }

    private void printAssociationData(BufferedWriter bw, Vector<AssociationData> adsTotal, TreeMap totalEventNums, String centreID, String filepath) throws Exception {

        //Work out whether the centre ID is specific here or not        
        int adTotalNum = adsTotal.size();
        Vector<AssociationData> ads = new Vector<AssociationData>();

        if (centreID.equals("")) {
            //If centreID is blank, process the whole list
            for (int p = 0; p < adTotalNum; p++) {
                ads.add(adsTotal.get(p));
            }
        } else {
            //Strip down the association data objects to only those belonging to the centre
            for (int p = 0; p < adTotalNum; p++) {
                AssociationData adIn = adsTotal.get(p);
                String centreIDIn = adIn.getCentreID();
                if (centreIDIn.equals(centreID)) {
                    ads.add(adIn);
                }
            }
        }
        int adNum = ads.size();

        //Now pull out the number of events associated with treatments
        Vector<TreeMap> totalAssocEventNums = new Vector<TreeMap>();
        Vector<TreeMap> totalAssocTreatmentLists = new Vector<TreeMap>();
        Vector<TreeMap> totalAssocTreatmentTimes = new Vector<TreeMap>();
        for (int j = 0; j < WINDOW_DEFN_NUM; j++) {
            totalAssocEventNums.add(new TreeMap());
            totalAssocTreatmentLists.add(new TreeMap());
            totalAssocTreatmentTimes.add(new TreeMap());
        }

        for (int i = 0; i < adNum; i++) {
            AssociationData ad = ads.get(i);

            Vector<TreatmentCount> treatmentCounts = ad.getTreatmentLists();
            Vector<TimeData> treatmentTimes = ad.getTimeList();

            for (int j = 0; j < WINDOW_DEFN_NUM; j++) {
                TreeMap eventLists = ad.getWindowSizeCount(j);
                TreeMap currentNumIn = (TreeMap) totalAssocEventNums.get(j);

                Set eventListSet = eventLists.keySet();
                Iterator eventListIter = eventListSet.iterator();
                while (eventListIter.hasNext()) {
                    String eventListName = (String) eventListIter.next();
                    Integer eventListNum = (Integer) eventLists.get(eventListName);
                    if (currentNumIn != null) {
                        Integer currentNum = (Integer) currentNumIn.get(eventListName);
                        if (currentNum == null) {
                            currentNumIn.put(eventListName, (Integer) eventListNum);
                        } else {
                            currentNumIn.put(eventListName, (Integer) (eventListNum + currentNum));
                        }
                    }
                }
                totalAssocEventNums.set(j, currentNumIn);

                //Now I'm associating the TreeMap of treatment lists with the appropriate key (defn of 32) in the TreeMap above
                TreatmentCount treatmentCount = treatmentCounts.get(j);
                TreeMap treatmentList = treatmentCount.getTreatmentLists();

                TreeMap currentListIn = (TreeMap) totalAssocTreatmentLists.get(j);

                Set treatmentListSet = treatmentList.keySet();
                Iterator treatmentListIter = treatmentListSet.iterator();
                while (treatmentListIter.hasNext()) {
                    String treatmentListName = (String) treatmentListIter.next();
                    TreeMap treatmentCountDefn = (TreeMap) treatmentList.get(treatmentListName);
                    currentListIn = this.integrateTreemaps(currentListIn, treatmentListName, treatmentCountDefn);
                }
                totalAssocTreatmentLists.set(j, currentListIn);

                //Now adding the time to treatment information to the overall lists
                TimeData timeListTd = treatmentTimes.get(j);
                TreeMap timeListIn = timeListTd.getTimeList();

                TreeMap currentTimeListIn = (TreeMap) totalAssocTreatmentTimes.get(j);

                Set timeListSet = timeListIn.keySet();
                Iterator timeListIter = timeListSet.iterator();
                while (timeListIter.hasNext()) {
                    String timeListName = (String) timeListIter.next();
                    Vector<Date> timeList = (Vector<Date>) timeListIn.get(timeListName);

                    if (currentTimeListIn == null) {
                        currentTimeListIn = new TreeMap();
                        currentTimeListIn.put(timeListName, timeList);
                    } else {
                        Vector<Date> listIn = (Vector<Date>) currentTimeListIn.get(timeListName);
                        if (listIn == null) {
                            listIn = new Vector<Date>();
                        }
                        if (timeList != null) {
                            for (int m = 0; m < timeList.size(); m++) {
                                listIn.add(timeList.get(m));
                            }
                        }
                        currentTimeListIn.put(timeListName, listIn);
                    }
                }
                totalAssocTreatmentTimes.set(j, currentTimeListIn);
            }
        }

        if (!centreID.equals("")) {
            bw.write("SPECIFIC BREAKDOWN OF TREATMENTS FOR CENTRE " + centreID + "\n\n");
        }

        //Print to file
        for (int j = 0; j < WINDOW_DEFN_NUM; j++) {

            TreeMap currentListIn = (TreeMap) totalAssocTreatmentLists.get(j);
            TreeMap currentNumIn = (TreeMap) totalAssocEventNums.get(j);
            TreeMap currentTimesIn = (TreeMap) totalAssocTreatmentTimes.get(j);

            bw.write("Window definition #" + (j + 1) + "\n");
            bw.write("\n");
            bw.write("Parameter definition - (number of events associated with treatments) - percentage of total event numbers ");
            bw.write("- mean time to treatment - median time to treatment - (top three treatments)\n");
            Set totalAssocEventSet = totalAssocEventNums.get(j).keySet();
            Iterator totalAssocEventIter = totalAssocEventSet.iterator();
            while (totalAssocEventIter.hasNext()) {
                String totalAssocEventName = (String) totalAssocEventIter.next();
                Integer totalAssocEventNum = (Integer) currentNumIn.get(totalAssocEventName);
                bw.write("" + totalAssocEventName + " - (" + totalAssocEventNum + ")");

                //Now retrieve the corresponding total number and express as a percentage
                Integer totalEventNum = (Integer) totalEventNums.get(totalAssocEventName);
                String percent = this.writePercentage(totalAssocEventNum, totalEventNum);
                bw.write(" - " + percent + "");

                //Now retrieve the mean and median times to treatment for this parameter definition                
                Vector<Date> theseTreatmentTimes = (Vector<Date>) currentTimesIn.get(totalAssocEventName);

                if (theseTreatmentTimes != null) {
                    String meanTimeStr = this.calculateMeanTime(theseTreatmentTimes);
                    String medianTimeStr = this.calculateMedianTime(theseTreatmentTimes);
                    bw.write(" - " + meanTimeStr + " mins - " + medianTimeStr + " mins");
                } else {
                    bw.write(" - 0 - 0");
                }

                //Now retrieve the associated treatment count list and render for each definition
                TreeMap totalAssocTreatmentList = (TreeMap) currentListIn.get(totalAssocEventName);
                if (totalAssocTreatmentList != null && (totalAssocTreatmentList.size() != 0)) {
                    Set totalAssocTreatmentListSet = totalAssocTreatmentList.keySet();
                    Iterator totalAssocTreatmentListIter = totalAssocTreatmentListSet.iterator();

                    //Order the iterator by number and print the top three treatments
                    this.printTopTreatments(totalAssocTreatmentListIter, totalAssocTreatmentList, bw);

                    //Re-assign the iterator and use it again for the full distribution output
                    totalAssocTreatmentListIter = totalAssocTreatmentListSet.iterator();
                    bw.write("---- TREATMENT SUMMARY DISTRIBUTION ----\n");
                    while (totalAssocTreatmentListIter.hasNext()) {
                        String treatmentName = (String) totalAssocTreatmentListIter.next();
                        Integer treatmentNum = (Integer) totalAssocTreatmentList.get(treatmentName);

                        //Print these alphabetically in the distribution summary
                        bw.write("" + treatmentName + ": " + treatmentNum + "\n");
                    }
                    bw.write("---- END TREATMENT SUMMARY DISTRIBUTION ----\n");
                }
            }
            bw.write("-----\n");
        }

        //Print charts for all of these
        for (int j = 0; j < WINDOW_DEFN_NUM; j++) {
            TreeMap currentNumIn = (TreeMap) totalAssocEventNums.get(j);
            String chartFilepath = filepath + "assocevents_window" + (j + 1) + "_";
            CreateBarChart cbc = new CreateBarChart(logger);
            cbc.createEventChart(chartFilepath, currentNumIn, "Association event numbers");
        }

        for (int j = 0; j < WINDOW_DEFN_NUM; j++) {
            TreeMap currentNumIn = (TreeMap) totalAssocEventNums.get(j);
            TreeMap currentTimesIn = (TreeMap) totalAssocTreatmentTimes.get(j);

            String chartFilepath = filepath + "meantime_window" + (j + 1) + "_";
            CreateLineGraph clg = new CreateLineGraph(logger);
            clg.createTimeChart(chartFilepath, currentTimesIn, currentNumIn, "Mean times");

            chartFilepath = filepath + "mediantime_window" + (j + 1) + "_";
            clg = new CreateLineGraph(logger);
            clg.createTimeChart(chartFilepath, currentTimesIn, currentNumIn, "Median times");
        }
        
        for(int j=0; j<WINDOW_DEFN_NUM; j++){               
            
            TreeMap currentListIn = (TreeMap) totalAssocTreatmentLists.get(j);
            TreeMap currentNumIn = (TreeMap) totalAssocEventNums.get(j);            
            
            String chartFilepath = filepath + "treatmentdist_window" + (j + 1) + "_";
            CreatePieChart cpc = new CreatePieChart(logger);
            cpc.createTreatmentChart(chartFilepath, currentListIn, currentNumIn, "Treatment distribution");            
        }

    }

    private TreeMap integrateTreemaps(TreeMap treemap1, String defnName, TreeMap treemap2) {

        //Retrieve the treemap that is associated with this defnName
        TreeMap treemapOut = (TreeMap) treemap1.get(defnName);
        if (treemapOut == null) {
            treemapOut = new TreeMap();
        }

        Set treemap2Set = treemap2.keySet();
        Iterator treemap2Iter = treemap2Set.iterator();
        while (treemap2Iter.hasNext()) {
            String treatmentName = (String) treemap2Iter.next();
            Integer treatmentNum = (Integer) treemap2.get(treatmentName);

            if (treemapOut.containsKey(treatmentName)) {
                Integer currentTreatmentNum = (Integer) treemapOut.get(treatmentName);
                treemapOut.put(treatmentName, currentTreatmentNum + treatmentNum);
            } else {
                treemapOut.put(treatmentName, treatmentNum);
            }
        }
        treemap1.put(defnName, treemapOut);
        return treemap1;
    }

    private float calcPercentage(float numerator, float denominator) {
        return (numerator / denominator) * 100;
    }

    private String writePercentage(Integer numerator, Integer denominator) {
        float numeratorFloat = (float) numerator;
        float denominatorFloat = (float) denominator;
        float percent = this.calcPercentage(numeratorFloat, denominatorFloat);

        //Round to one decimal point
        String percentOutStr = String.format("%.1f", percent);

        return percentOutStr + "%";
    }

    private String calculateMeanTime(Vector<Date> timeToTreatments) {

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

    private String calculateMedianTime(Vector<Date> timeToTreatments) {

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

    private Vector<Date> orderTimes(Vector<Date> timesIn) {

        int timeNum = timesIn.size();
        final Date[] times = new Date[timeNum];
        for (int i = 0; i < timeNum; i++) {
            times[i] = timesIn.get(i);
        }

        for (int i = 0; i < timeNum; i++) {

            // Assume first element is min
            int minIndex = i;
            Date min = times[i];
            for (int j = i + 1; j < timeNum; j++) {
                Date nextTimeIn = times[j];
                if (nextTimeIn.compareTo(min) == -1) {
                    minIndex = j;
                    min = nextTimeIn;
                }
            }

            Date temp = times[i];
            times[i] = min;
            times[minIndex] = temp;
        }

        Vector<Date> timesOut = new Vector<Date>();
        for (int i = 0; i < timeNum; i++) {
            timesOut.add(times[i]);
        }

        return timesOut;
    }

    public void printData(Vector<AssociationData> ads) {

        //Run the processing for a total output
        this.printDataPerCentre(ads, "");

        //Then run the processing for each centre
        int uniqueCentreNum = centreIDs.size();
        for (int k = 0; k < uniqueCentreNum; k++) {
            String centreID = centreIDs.get(k);
            this.printDataPerCentre(ads, centreID);
        }
    }

    public void printDataPerCentre(Vector<AssociationData> ads, String centreID) {

        FileWriter fw = null;
        BufferedWriter bw = null;

        String outputFilepath = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\EventTreatmentAssociation\\output\\";
        if (!centreID.equals("")) {
            outputFilepath += "centre_specific\\centre" + centreID + "_";
        }
        String outputFilename = outputFilepath + "event_treatment_association_output.txt";
        if (FIRST_TREATMENT_ONLY) {
            outputFilename = outputFilepath + "event_treatment_association_firsttreatment_output.txt";
        }

        try {
            fw = new FileWriter(outputFilename, true);
            bw = new BufferedWriter(fw);

            this.printOutput(ads, bw, centreID, outputFilepath);

            bw.close();
            fw.close();
        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
    }

    private void printTopTreatments(Iterator iter, TreeMap treatmentList, BufferedWriter bw) throws Exception {

        int TOP_TREATMENT_NUM = 3;
        
        //Declare two arrays to capture all the information
        int catSize = treatmentList.size();
        String[] treatmentNames = new String[catSize];
        int[] treatmentNums = new int[catSize];
        
        int catCount = 0;
        while (iter.hasNext()) {    
            treatmentNames[catCount] = (String) iter.next();
            treatmentNums[catCount] = ((Integer) treatmentList.get(treatmentNames[catCount])).intValue();
            catCount++;
        }

        treatmentNames = this.orderTreatments(treatmentNames, treatmentNums);
        int printNum = TOP_TREATMENT_NUM;        
        if(treatmentNames.length < TOP_TREATMENT_NUM){
            printNum = treatmentNames.length;
        }
        bw.write(" - (");
        for(int i=0; i<printNum; i++){
            bw.write(treatmentNames[i]);
            if(i != (printNum-1)){
                bw.write(", ");
            }
        }
        bw.write(")\n");                    
    }
    
    private String[] orderTreatments(String[] treatmentNames, int[] treatmentNums){
        
        int treatmentNum = treatmentNames.length;
        
        for (int i = 0; i < treatmentNum; i++) {

            // Assume first element is min
            int minIndex = i;
            int min = treatmentNums[i];
            String minStr = treatmentNames[i];
            for (int j = i + 1; j < treatmentNum; j++) {
                int nextNumIn = treatmentNums[j];
                String nextStrIn = treatmentNames[j];
                if (nextNumIn < min) {
                    minIndex = j;
                    min = nextNumIn;                 
                    minStr = nextStrIn;
                }
            }

            int temp = treatmentNums[i];
            String tempStr = treatmentNames[i];
            treatmentNums[i] = min;
            treatmentNames[i] = minStr;
            treatmentNums[minIndex] = temp;
            treatmentNames[minIndex] = tempStr;
        }
        
        //Flip the array into descending order
        String[] treatmentNamesDesc = new String[treatmentNum];
        for(int i=0; i < treatmentNum; i++){
            treatmentNamesDesc[i] = treatmentNames[(treatmentNum-1)-i];            
        }        
        
        return treatmentNamesDesc;
    }
}
