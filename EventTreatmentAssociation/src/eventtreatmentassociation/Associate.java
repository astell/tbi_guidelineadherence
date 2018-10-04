/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

import java.util.Date;
import java.util.Vector;
//import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 *
 * @author astell
 */
public class Associate {

    private Logger logger;
    private AssociationData associationData;
    private boolean FIRST_TREATMENT_ONLY;

    public Associate(Logger _logger, AssociationData ad, boolean _firstTreatmentOnly) {
        logger = _logger;
        associationData = ad;
        FIRST_TREATMENT_ONLY = _firstTreatmentOnly;
    }

    public AssociationData getAssociationData() {
        return associationData;
    }

    public void associate(Vector<Treatment> treatments, Vector<Event> events, String patientID, String condition, String _holdDownFeed, String _windowDefn, int windowDefnIndex) {

        int assocEventsTreatments = this.checkEventsAgainstTreatments(treatments, events, patientID, condition, _holdDownFeed, _windowDefn);
        String holdDownLabel = "";
        if (_holdDownFeed.equals("5")) {
            holdDownLabel = "05";
        } else {
            holdDownLabel = _holdDownFeed;
        }
        associationData.setParamCount(condition + "_" + holdDownLabel, assocEventsTreatments, windowDefnIndex);
    }

    private Vector<TimeWindow> setupTimeWindows(Vector<Event> events, String windowDefn) {

        Vector<TimeWindow> timeWindows = new Vector<TimeWindow>();

        final boolean TIME_WINDOW_SYMMETRIC = true;

        //Get the minute definition of the window size from the string
        int windowSizeMins = Integer.parseInt(windowDefn);
        long startToEvent = 60000 * windowSizeMins;

        long eventToEnd = 0;
        if (TIME_WINDOW_SYMMETRIC) {
            eventToEnd = startToEvent;
        }

        //For each Treatment object, get the timestamp and define the associated time-window object
        //For each time-window, count the number of event starts/ends within the time window
        //Always measure it from the treatmentTime axis    
        int eventNum = events.size();
        for (int i = 0; i < eventNum; i++) {
            Event eventIn = events.get(i);

            //NOTE: ONLY FOCUSING ON EVENT STARTS AND ("TREATMENT START + ANY")
            Date eventStartTime = eventIn.getStart();
            //Date eventEndTime = eventIn.getEnd();

            Date start = new Date(eventStartTime.getTime() - startToEvent);
            Date end = new Date(eventStartTime.getTime() + eventToEnd);

            TimeWindow timeWindow = new TimeWindow(start, end, eventStartTime, TIME_WINDOW_SYMMETRIC);
            timeWindows.add(timeWindow);
        }

        return timeWindows;
    }

    private int checkEventsAgainstTreatments(Vector<Treatment> treatments, Vector<Event> events, String patientID, String condition, String holdDownFeed, String windowDefn) {

        int assocCount = 0;

        Vector<TimeWindow> timeWindows = this.setupTimeWindows(events, windowDefn);
        int treatmentNum = treatments.size();
        int eventNum = events.size();

        int EVENT_NUM_TO_CHECK = eventNum;
        if (FIRST_TREATMENT_ONLY) {
            if (eventNum > 0) {
                EVENT_NUM_TO_CHECK = 1;
            } else {
                EVENT_NUM_TO_CHECK = 0;
            }
        }

        //Adding a measure of how many double-associations are being thrown away
        int eventsWithMoreThanOne = 0;
        for (int i = 0; i < EVENT_NUM_TO_CHECK; i++) {

            Event eventIn = events.get(i);

            //This boolean flag controls any double-counting going on
            //Switches to true if a treatment gets associated with this event
            //And this prevents any further addition of associated treatments
            boolean hasAssociatedTreatment = false;
            boolean hasMoreThanOneAssociation = false;
            Date eventStart = eventIn.getStart();

            Vector<Treatment> associatedTreatments = new Vector<Treatment>();
            TimeWindow timeWindowIn = timeWindows.get(i);

            Date timeWindowEnd = timeWindowIn.getEnd();
            Date timeWindowEvent = timeWindowIn.getMidTime();

            for (int j = 0; j < treatmentNum; j++) {

                Treatment treatmentIn = treatments.get(j);
                Date treatmentTime = treatmentIn.getTimestamp();

                String treatmentDesc = treatmentIn.getDescription();
                String treatmentTarget = treatmentIn.getTarget();
                String treatmentValue = treatmentIn.getValue();

                if (treatmentTarget == null) {
                    treatmentTarget = "";
                }
                if (treatmentDesc == null) {
                    treatmentDesc = "";
                }
                if (treatmentValue == null) {
                    treatmentValue = "";
                }
                treatmentTarget = treatmentTarget.trim();
                treatmentDesc = treatmentDesc.trim();
                treatmentValue = treatmentValue.trim();

                if (treatmentTarget.toLowerCase().contains("icp")
                        || treatmentTarget.toLowerCase().contains("cpp")
                        || treatmentTarget.toLowerCase().contains("hypotension")
                        || treatmentDesc.toLowerCase().contains("icp")
                        || treatmentDesc.toLowerCase().contains("cpp")
                        || treatmentDesc.toLowerCase().contains("hypotension")) {

                    //THIS COMPARISON CHECKS TO SEE IF ANY TREATMENT OCCURS BETWEEN THE EVENT START AND THE TIME-WINDOW END (30 MINS AFTER)
                    if (((treatmentTime.compareTo(timeWindowEnd) == -1) && (treatmentTime.compareTo(timeWindowEvent) == 1))) {

                        /*
                         * boolean isTreatmentStart =
                         * treatmentTarget.toLowerCase().contains("start") || treatmentDesc.toLowerCase().contains("start");
                         */
                        boolean isTreatmentEnd = treatmentTarget.toLowerCase().contains("end")
                                || treatmentDesc.toLowerCase().contains("end");

                        //NOTE: run this the other way - as we are checking treatment start, include everything except those tagged "End"
                        if (!isTreatmentEnd) {

                            if (!hasAssociatedTreatment) {

                                associatedTreatments.add(treatmentIn);
                                hasAssociatedTreatment = true;

                                //Add the treatment type to the relevant count (COMPLICATED)
                                String treatmentType = treatmentValue.substring(2, treatmentValue.length());
                                treatmentType = treatmentType.trim();

                                int windowDefnIndex = this.getWindowDefnIndex(windowDefn);
                                String holdDownLabel = "";
                                if (holdDownFeed.equals("5")) {
                                    holdDownLabel = "05";
                                } else {
                                    holdDownLabel = holdDownFeed;
                                }
                                String parameterDefn = condition + "_" + holdDownLabel;

                                associationData.setTreatmentList(treatmentType, parameterDefn, windowDefnIndex);

                                //Add the time to the treatment at this point
                                long timeToTreatment = treatmentTime.getTime() - eventStart.getTime();
                                Date timeToTreatmentDate = new Date(timeToTreatment);
                                associationData.setTimeList(timeToTreatmentDate, parameterDefn, windowDefnIndex);

                            }else{
                                hasMoreThanOneAssociation = true;
                            }
                        }
                    }
                }
            }
            //Add the treatment listing to the time-window object for this event
            timeWindowIn.setAssociatedTreatments(associatedTreatments);

            //And increment the counter for number of events that have associated treatments
            if (associatedTreatments.size() != 0) {
                assocCount++;
            }
            
            if(hasMoreThanOneAssociation){
                eventsWithMoreThanOne++;
            }            
        }        
        if(EVENT_NUM_TO_CHECK > 0){
            logger.info("Events with more than one association: " + (eventsWithMoreThanOne/EVENT_NUM_TO_CHECK)*100);
        }

        //logger.info("Number of events WITH associated treatments: " + assocCount);
        //logger.info("=====");

        return assocCount;
    }

    private int getWindowDefnIndex(String windowDefn) {
        int windowIndex = 0;
        if (windowDefn.equals("30")) {
            windowIndex = 0;
        } else if (windowDefn.equals("60")) {
            windowIndex = 1;
        } else if (windowDefn.equals("90")) {
            windowIndex = 2;
        } else if (windowDefn.equals("120")) {
            windowIndex = 3;
        }
        return windowIndex;
    }
}
