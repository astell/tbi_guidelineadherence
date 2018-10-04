/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patients;

import java.util.*;
import java.text.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author astell
 */
public class GraphUtils {

    private static final Logger logger = Logger.getLogger(GraphUtils.class);
    private long TIME_WINDOW_MILLIS;
    private SimpleDateFormat df;

    public GraphUtils() {
        df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    public void setTimeWindow(int timeWindowIn) {
        TIME_WINDOW_MILLIS = (timeWindowIn * 60000);
    }

    public String graphSeries(String pid, String eventId, String seriesSelector, Vector<Event> events, String startPointTime) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        java.util.Date startPointDate = null;
        try {
            startPointDate = df.parse(startPointTime);
        } catch (Exception e) {
            logger.debug("Parsing error: " + e.getMessage());
        }

        ListPatients patientList = new ListPatients();

        String seriesStr = "series: [";
        seriesStr += "{name: '" + seriesSelector + "',id: '" + seriesSelector + "',";

        seriesStr += "data: [";
        int eventIdInt = Integer.parseInt(eventId);
        
        boolean eventFound = false;
        int eventCount = 0;
        Event eventIn = null;
        while(!eventFound && eventCount < events.size()){
            eventIn = events.get(eventCount);
            if(eventIn.getEventIndex() == eventIdInt){
                eventFound = true;
            }else{
                eventCount++;
            }
        }
        
        if(eventIn != null){
            Vector<String> eventValues = eventIn.getValues();
            
            for (int k = 0; k < eventValues.size(); k++) {
                String readingTimestamp = df.format(startPointDate.getTime() + (k * 60000));
                readingTimestamp = patientList.getTimestamp(readingTimestamp);

                seriesStr += "[" + readingTimestamp + "," + eventValues.get(k) + "]";
                if (k != eventValues.size() - 1) {
                    seriesStr += ",";
                }
            }
        }
        seriesStr += "]}";

        return seriesStr;

    }

    public String graphGuideline(String pid, String seriesSelector, Vector<GuidelineDistance> guidelines, String startPointTime, int eventIndex) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        java.util.Date startPointDate = null;
        try {
            startPointDate = df.parse(startPointTime);
        } catch (Exception e) {
            logger.debug("Parsing error: " + e.getMessage());
        }

        ListPatients patientList = new ListPatients();

        String seriesStr = "";
        seriesStr += "{name: 'Distance from guideline (non-adherence)',id: 'Distance from guideline (non-adherence)',yAxis: 1,color: '#AA0000',marker: {enabled: true},";

        seriesStr += "data: [";
        //for (int j = 0; j < guidelines.size(); j++) {
        //for (int j = 0; j < 1; j++) {
            GuidelineDistance guidelineIn = guidelines.get(eventIndex);
            Vector<Vector> guidelineValues = guidelineIn.getValueLists();

            for (int k = 0; k < guidelineValues.size(); k++) {
                Vector<Vector> guidelineValueIn = guidelineValues.get(k);
                int guidelineAnswerNum = guidelineValueIn.size();
                double overallDistanceNum = 0;

                //Work out the overall number here
                for (int m = 0; m < guidelineAnswerNum; m++) {
                    String numberIn = (String) guidelineValueIn.get(m).get(0);
                    //logger.debug("numberIn (graphGuideline): " + numberIn);
                    double numberInDouble = 0;
                    try {
                        numberInDouble = Double.parseDouble(numberIn);
                    } catch (NumberFormatException nfe) {
                        logger.debug("NumberFormatException (float conversion): " + nfe.getMessage());
                    }
                    overallDistanceNum += numberInDouble;
                }

                //logger.debug("overallDistanceNum: " + overallDistanceNum);
                //logger.debug("guidelineAnswerNum: " + guidelineAnswerNum);
                
                //Scale the output here (percentage of number of answers assessed)
                double overallDistanceNumScaled = overallDistanceNum / (double) guidelineAnswerNum;
                overallDistanceNumScaled *= 100.0;
                overallDistanceNumScaled = Math.round(overallDistanceNumScaled*100.0)/100.0; //Round to 2 decimal places
                //logger.debug("overallDistanceNumScaled: " + overallDistanceNumScaled);

                String readingTimestamp = df.format(startPointDate.getTime() + (k * 60000));
                readingTimestamp = patientList.getTimestamp(readingTimestamp);

                //seriesStr += "[" + readingTimestamp + "," + guidelineValueIn.get(0) + "]";
                seriesStr += "[" + readingTimestamp + "," + overallDistanceNumScaled + "]";
                if (k != guidelineValues.size() - 1) {
                    seriesStr += ",";
                }
                //logger.debug("----");
            }
        //}
        seriesStr += "]}";

        //logger.debug("seriesStr (guideline): " + seriesStr);
        return seriesStr;

    }

    public String graphFlags(String pid, String datasetId, String seriesSelector, Vector<Treatment> treatments, long timeWindowMillis, String startPointTime) {

        ListPatients patientList = new ListPatients();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        boolean treatmentPresent = false;

        java.util.Date startPointDate = null;
        try {
            startPointDate = df.parse(startPointTime);
        } catch (Exception e) {
            logger.debug("Parsing error: " + e.getMessage());
        }        
        
        String seriesStr = "{type: 'flags',name: 'Treatments',";
        seriesStr += "data: [";

        long timeWindowStart = startPointDate.getTime();
        long timeWindowEnd = timeWindowStart + timeWindowMillis;
        
        //For graph rendering purposes - 09/07/17
        /*int treatmentNumToShow = 3;
        if (treatments.size() < 3) {
            treatmentNumToShow = treatments.size();
        }*/

        for (int i = 0; i < treatments.size(); i++) {
            //for (int i = 0; i < 3; i++) {
            Treatment treatmentIn = treatments.get(i);
            java.util.Date treatmentTime = treatmentIn.getTimestamp();            
            if(treatmentTime.getTime() > timeWindowStart 
                    && treatmentTime.getTime() <= timeWindowEnd){
                treatmentPresent = true;
                String timestampIn = df.format(treatmentTime);
                timestampIn = patientList.getTimestamp(timestampIn);
                String drugNameIn = treatmentIn.getValue();
                if(datasetId.equals("5")){
                    drugNameIn = treatmentIn.getDescription();
                }
                String fluidMarker = "" + drugNameIn + " ";
                seriesStr += "{x: " + timestampIn + ",title: '" + fluidMarker + "'},";
            }
        }

        if (seriesStr.length() > 1) {
            seriesStr = seriesStr.substring(0, seriesStr.length() - 1);
        }
        seriesStr += "]";
        seriesStr += ",onSeries: '" + seriesSelector + "',shape: 'squarepin'}";

        if(treatmentPresent){
            return ", " + seriesStr;
        }else{
            return "";
        }
    }

    public String getOverallSeriesStr(String pid, String datasetId, String eventId, String seriesSelector, Vector<Event> events, Vector<Treatment> treatments, Vector<GuidelineDistance> guidelineDistances, String startPointTime, long timeWindowMillis) {

        String seriesStr = "";
        if (guidelineDistances.size() > 0) {
            
            int eventIndex = 0;
            try{
                eventIndex = Integer.parseInt(eventId);
            }catch(Exception e){
                logger.debug("eventIndex unparseable (graphGuideline)...");
            }
        
            //Feed all of this into the series/guideline generator on GraphUtils
            long guidelineStartLong = events.get(eventIndex).getStart().getTime() + TIME_WINDOW_MILLIS;
            String guidelineStartPointTime = df.format(new java.util.Date(guidelineStartLong));

            seriesStr = this.graphSeries(pid, eventId, seriesSelector, events, startPointTime);
            seriesStr += ", ";
            seriesStr += this.graphGuideline(pid, seriesSelector, guidelineDistances, guidelineStartPointTime, eventIndex);
            //seriesStr += ", ";
            seriesStr += this.graphFlags(pid, datasetId, seriesSelector, treatments, timeWindowMillis, startPointTime);
            seriesStr += "]";
        }
        return seriesStr;

    }

    public String getGuidelineReasonJsArray(ListPatients patientList, Vector<GuidelineDistance> guidelineDistances) {

        String guidelineReasonJsArrayStr = "";
        if (guidelineDistances.size() > 0) {
            GuidelineDistance thisGuideline = guidelineDistances.get(0);
            long thisGuidelineStart = thisGuideline.getStart().getTime();
            logger.debug("Guideline start (chart): " + thisGuidelineStart);
            Vector<Vector> thisGuidelineValues = thisGuideline.getValueLists(); //0 = value (%), 1 = reason
            int thisGuidelineSize = thisGuidelineValues.size();
            logger.debug("Guideline size (chart): " + thisGuidelineSize);
            for (int i = 0; i < thisGuidelineSize; i++) {
                Vector<Vector> thisGuidelineValuesIn = thisGuidelineValues.get(i);
                //logger.debug("thisGuidelineValuesIn: " + thisGuidelineValuesIn);        
                String thisGuidelineReasonsOut = "";
                for (int j = 0; j < thisGuidelineValuesIn.size(); j++) {
                    String numberIn = (String) thisGuidelineValuesIn.get(j).get(0);
                    float numberInFloat = 0;
                    try {
                        numberInFloat = Float.parseFloat(numberIn);
                    } catch (NumberFormatException nfe) {
                        logger.debug("NumberFormatException (float conversion): " + nfe.getMessage());
                    }
                    if (numberInFloat > 0) {
                        String reasonIn = (String) thisGuidelineValuesIn.get(j).get(1);
                        
                        //ADDING IN AN EXTRA NUMBER TO THE TEXT TO MAKE THE CONTRIBUTION CLEARER:
                        //Will divide the weighting by the reason number and express as percentage
                        Double numberAsPercent = (numberInFloat / thisGuidelineValuesIn.size()) * 100.0;
                        numberAsPercent = Math.round(numberAsPercent*100.0)/100.0; //Round to 2 decimal places
                        
                        thisGuidelineReasonsOut += "<li>" + reasonIn + " (" + numberAsPercent + " %)</li>";
                    }
                }
                if (thisGuidelineReasonsOut.equals("")) {
                    guidelineReasonJsArrayStr += "guidelineValueArray.push('None - fully compliant');";
                } else {
                    guidelineReasonJsArrayStr += "guidelineValueArray.push('" + thisGuidelineReasonsOut + "');";
                }
                //logger.debug("----");
            }
        }
        return guidelineReasonJsArrayStr;
    }

    public String chartMinuteByMinuteTable(ListPatients patientList, Vector<GuidelineDistance> guidelineDistances, String startPointTime) {

        String minuteByMinuteTableHtml = "";
        
        GuidelineDistance guidelineIn = guidelineDistances.get(0);
        Vector<Vector> guidelineValues = guidelineIn.getValueLists();
        for (int k = 0; k < guidelineValues.size(); k++) {
            Vector<Vector> guidelineValueIn = guidelineValues.get(k);
            int guidelineAnswerNum = guidelineValueIn.size();
            int overallDistanceNum = 0;

            //Work out the overall number here
            for (int m = 0; m < guidelineAnswerNum; m++) {
                String numberIn = (String) guidelineValueIn.get(m).get(0);
                float numberInFloat = 0;
                try {
                    numberInFloat = Float.parseFloat(numberIn);
                } catch (NumberFormatException nfe) {
                    logger.debug("NumberFormatException (float conversion): " + nfe.getMessage());
                }
                overallDistanceNum += numberInFloat;
            }

            //Scale the output here (percentage of number of answers assessed)
            float overallDistanceNumScaled = overallDistanceNum / guidelineAnswerNum;

            java.util.Date startPointDate = null;
            try {
                startPointDate = df.parse(startPointTime);
            } catch (Exception e) {
                logger.debug("Parsing error: " + e.getMessage());
            }

            String readingTimestamp = df.format(startPointDate.getTime() + (k * TIME_WINDOW_MILLIS));
            readingTimestamp = patientList.getTimestamp(readingTimestamp);

            minuteByMinuteTableHtml += "<tr><td>" + readingTimestamp + "</td><td>" + overallDistanceNumScaled + "</td></tr>";
        }
        minuteByMinuteTableHtml += "</table>";
        return minuteByMinuteTableHtml;
    }

}
