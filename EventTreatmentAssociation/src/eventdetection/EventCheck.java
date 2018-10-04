/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventdetection;

/**
 *
 * @author astell
 */
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.text.SimpleDateFormat;
import java.util.Vector;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.TimeZone;

public class EventCheck {

    Logger logger = null;
    private Vector<Vector> physioLines;

    public EventCheck(Vector<Vector> _physioLines, Logger _logger) {
        logger = _logger;
        physioLines = _physioLines;
    }

    public Vector<Event> getEvents(double officialThreshold, String parameterFeed, int paramIndex, boolean comparatorGreater, int holdDownDefinition, boolean mimic) {

        int HOLD_DOWN_DEFINITION = holdDownDefinition;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        //SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        //SimpleDateFormat df = new SimpleDateFormat("dd/MM/YYYY hh:mm");

        Vector<Event> events = new Vector<Event>();

        boolean inEvent = false;
        int eventIndex = 0;
        boolean potentialEvent = false;
        boolean potentialClear = false;
        int eventHolddownCount = 0;
        int clearHolddownCount = 0;

        int physioSize = physioLines.size();
        logger.info("physioSize: " + physioSize);
        //logger.info("paramIndex: " + paramIndex);
        //logger.info("Begin checking for events in time series...");

        java.util.Date lastTimestamp = new java.util.Date();
        Event event = new Event(0, parameterFeed);
        Vector<String> potentialValues = new Vector<String>();

        for (int i = 0; i < physioSize; i++) {
            //for (int i = 0; i < 10; i++) {

            //Get the physiological line data
            Vector<String> physioLineIn = physioLines.get(i);
            
            /*if (i == 0) {
                logger.info("physioLineIn: " + physioLineIn);
            }*/

            //Read in timepoint and value
            java.util.Date thisTimestamp = null;
            try {
                String timestampInStr = physioLineIn.get(0);
                //logger.info("timestampInStr: " + timestampInStr);
                if (timestampInStr.contains("T")) {
                    timestampInStr = timestampInStr.replace('T', ' ');
                    timestampInStr = timestampInStr.substring(0, timestampInStr.length() - 5);
                }
                //logger.info("timestampInStr: " + timestampInStr);
                thisTimestamp = df.parse(timestampInStr);
                
            } catch (Exception e) {
                logger.info("Error: " + e.getMessage());
            }

            double thisValue = 0;
            int physioLineIndex = paramIndex;
            String valueInStr = "0.0";
            if (physioLineIn.size() > physioLineIndex) {
                valueInStr = physioLineIn.get(physioLineIndex);
            }

            valueInStr = valueInStr.trim();
            //Catches null feeds
            /*if (valueInStr.trim().equals("-1.0")) {
                valueInStr = "";
            }*/
            Double valueInDouble = null;
            try {
                double valueInD = Double.parseDouble(valueInStr);
                valueInDouble = new Double(valueInD);
            } catch (NumberFormatException nfe) {
                //Catches null feeds
                valueInDouble = new Double(Double.NaN);
            }
            thisValue = valueInDouble;

            //logger.info("thisTimestamp: " + thisTimestamp);
            //logger.info("thisValue: " + thisValue);

            //MIMIC has it's own style of rendering events (as it's end-hour averaged)
            if (mimic) {

                boolean eventCondition = thisValue > officialThreshold;
                if (!comparatorGreater) {
                    eventCondition = thisValue <= officialThreshold;
                }
                if (eventCondition) {

                    event = new Event(eventIndex, parameterFeed);
                    java.util.Date eventStart = new java.util.Date(thisTimestamp.getTime());
                    df.format(eventStart);
                    event.setStart(eventStart);
                    long eventHolddown = (HOLD_DOWN_DEFINITION * 60000);
                    event.setEventHolddown(eventHolddown);

                    //Set this initial value and use it to create the value vector for this event
                    Vector<String> values = new Vector<String>();
                    values.add("" + thisValue);
                    event.setValues(values);

                    //Set the event end for a minute later (arbitrary but required)
                    java.util.Date eventEnd = new java.util.Date(thisTimestamp.getTime() + (HOLD_DOWN_DEFINITION * 60000));
                    df.format(eventEnd);
                    event.setEnd(eventEnd);
                    long clearHolddown = (HOLD_DOWN_DEFINITION * 60000);
                    event.setClearHolddown(clearHolddown);
                    events.add(event);
                    eventIndex++;
                }

            } else {

                //Compare to last timepoint - if there is a gap, reset all the parameters 
                //(note we're not going into inferring gaps and the like)
                if (thisTimestamp.getTime() - lastTimestamp.getTime() > 60000) {
                    logger.info("RESET GAP...");
                    inEvent = false;
                    potentialEvent = false;
                    potentialClear = false;
                    eventHolddownCount = 0;
                    clearHolddownCount = 0;
                }

                //Check if we're already mid-way through a real event
                if (inEvent) {

                    //logger.info("EVENT IS IN PROGRESS...");
                    //logger.info("thisValue: " + thisValue);
                    //logger.info("thisTimestamp: " + thisTimestamp);
                    //If we're in an event, we need to check if we're still above the threshold
                    //If we're not, then check if there's been a clear hold-down reached
                    //If it has, then we note the end time and the clear hold-down (will always be 5 by definition)
                    //If the value is less than or equal to the official threshold value
                    boolean clearCondition = thisValue <= officialThreshold;
                    if (!comparatorGreater) {
                        clearCondition = thisValue > officialThreshold;
                    }
                    if (clearCondition) {

                        //logger.info("POTENTIAL CLEAR...");
                        //If potentialClear is false, set to true, reset counter and increment by 1
                        //If potentialClear is already true, increment clearHolddownCount by 1                    
                        if (!potentialClear) {
                            potentialClear = true;
                            clearHolddownCount = 0;
                        }
                        clearHolddownCount++;
                        //logger.info("clearHolddownCount: " + clearHolddownCount);

                        //If clearHolddownCount == 5
                        //Set inEvent = false, note end time (-5 minutes from this timestamp)
                        //set clearHolddown = 5 mins (expressed as long)
                        if (clearHolddownCount == HOLD_DOWN_DEFINITION) {
                            inEvent = false;
                            //logger.info("EVENT HAS ENDED...");
                            java.util.Date eventEnd = new java.util.Date(thisTimestamp.getTime() - (HOLD_DOWN_DEFINITION * 60000));
                            df.format(eventEnd);
                            event.setEnd(eventEnd);
                            long clearHolddown = (HOLD_DOWN_DEFINITION * 60000);
                            event.setClearHolddown(clearHolddown);
                            events.add(event);
                            eventIndex++;
                        }
                    } else {
                        potentialClear = false;
                        clearHolddownCount = 0;
                    }
                    //Whether the event has ended or not, still record the value that dips below the threshold                    
                    Vector<String> theseValues = event.getValues();
                    theseValues.add("" + thisValue);
                    event.setValues(theseValues);
                } else {

                    //logger.info("index: " + i);
                    //If we're not in an event, we need to check if the threshold has been crossed
                    //If it has, then we need to check whether this is the start of a potential or real event
                    //If it's real, we want to note the start time and the event hold-down (will always be 5 by definition)
                    //If the value is greater than the official threshold value
                    boolean eventCondition = thisValue > officialThreshold;
                    if (!comparatorGreater) {
                        eventCondition = thisValue <= officialThreshold;
                    }
                    //if (thisValue > officialThreshold) {                
                    if (eventCondition) {

                        //logger.info("POTENTIAL EVENT...");
                        //logger.info("thisValue: " + thisValue);
                        //logger.info("thisTimestamp: " + thisTimestamp);
                        //If potentialEvent is false, set to true, reset counter and increment by 1
                        //If potentialEvent is already true, increment eventHolddownCount by 1                    
                        if (!potentialEvent) {
                            potentialEvent = true;
                            eventHolddownCount = 0;
                        }
                        eventHolddownCount++;
                        potentialValues.add("" + thisValue);
                        //logger.info("eventHolddownCount: " + eventHolddownCount);

                        //If eventHolddownCount == 5
                        //Set realEvent = true, note start time (-5 minutes from this timestamp)
                        //set eventHolddown = 5 mins (expressed as long)
                        if (eventHolddownCount == HOLD_DOWN_DEFINITION) {
                            inEvent = true;
                            //logger.info("EVENT HAS BEGUN...");
                            event = new Event(eventIndex, parameterFeed);
                            logger.info("thisTimestamp: " + thisTimestamp);
                            java.util.Date eventStart = new java.util.Date(thisTimestamp.getTime() - (HOLD_DOWN_DEFINITION * 60000));
                            df.format(eventStart);
                            logger.info("eventStart: " + eventStart);
                            event.setStart(eventStart);
                            long eventHolddown = (HOLD_DOWN_DEFINITION * 60000);
                            event.setEventHolddown(eventHolddown);

                            //Set this initial value and use it to create the value vector for this event
                            Vector<String> values = new Vector<String>();
                            values.addAll(potentialValues);
                            values.add("" + thisValue);
                            event.setValues(values);

                            potentialEvent = false;
                            eventHolddownCount = 0;
                        }
                    } else {
                        potentialEvent = false;
                        eventHolddownCount = 0;
                        potentialValues = new Vector<String>();
                    }
                }
            }
        }
        return events;
    }

    public void printEvents(Vector<Event> events, String filename) {

        try {

            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);

            int eventNum = events.size();
            for (int i = 0; i < eventNum; i++) {
                Event eventIn = events.get(i);
                int eventIndex = eventIn.getEventIndex();
                java.util.Date eventStart = eventIn.getStart();
                java.util.Date eventEnd = eventIn.getEnd();
                Vector<String> values = eventIn.getValues();

                bw.write("=====\n");
                bw.write("Event " + (eventIndex + 1) + "\n");
                bw.write("Event start: " + eventStart + "\n");
                bw.write("Event end: " + eventEnd + "\n");

                int valueNum = values.size();
                for (int j = 0; j < valueNum; j++) {
                    bw.write("" + values.get(j) + ",");
                }
                bw.write("\n");
            }

            bw.close();
            fw.close();

        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }

    }

    public void printEventsToSysOut(Vector<Event> events) {

        int eventNum = events.size();
        for (int i = 0; i < eventNum; i++) {
            Event eventIn = events.get(i);
            int eventIndex = eventIn.getEventIndex();
            
            java.util.Date eventStart = eventIn.getStart();
            java.util.Date eventEnd = eventIn.getEnd();
            Vector<String> values = eventIn.getValues();

            System.out.println("=====");
            System.out.println("Event " + (eventIndex + 1) + "");
            System.out.println("Event start: " + eventStart + "");
            System.out.println("Event end: " + eventEnd + "");

            /*int valueNum = values.size();
            for (int j = 0; j < valueNum; j++) {
                System.out.print("" + values.get(j) + ",");
            }*/
            System.out.println("");
        }

    }

    public void commitEvents(Connection conn, Vector<Event> events, String patientID, int holddown, String eusigID) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //SimpleDateFormat df = new SimpleDateFormat("dd/MM/YYYY hh:mm");
        try {

            int eventNum = events.size();
            for (int i = 0; i < eventNum; i++) {
                Event eventIn = events.get(i);
                int eventIndex = eventIn.getEventIndex();
                java.util.Date eventStart = eventIn.getStart();
                java.util.Date eventEnd = eventIn.getEnd();
                Vector<String> values = eventIn.getValues();

                String sql = "";
                sql = "INSERT INTO event VALUES(?,?,?,?,?,?);";

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, eventIndex);
                ps.setString(2, patientID);
                ps.setInt(3, Integer.parseInt(eusigID));
                ps.setString(4, "" + holddown);
                ps.setString(5, df.format(eventStart));
                ps.setString(6, df.format(eventEnd));

                logger.info("eventStart: " + df.format(eventStart));
                logger.info("eventEnd: " + df.format(eventEnd));

                int update = ps.executeUpdate();
                ps.close();

                //NEED TO SET Event_Reading TO HAVE AN AUTO INCREMENTING reading_id
                int valueNum = values.size();
                logger.info("Patient: " + patientID + " event " + i + " inserted, with size: " + valueNum);
                int readingIndex = 1;
                for (int j = 0; j < valueNum; j++) {
                    String readingSql = "";
                    readingSql = "INSERT INTO event_reading VALUES(?,?,?,?,?,?);";
                    PreparedStatement readingPs = conn.prepareStatement(readingSql);
                    readingPs.setInt(1, readingIndex);
                    readingPs.setString(2, patientID);
                    readingPs.setInt(3, eventIndex);
                    readingPs.setInt(4, Integer.parseInt(eusigID));
                    readingPs.setString(5, "" + holddown);
                    readingPs.setString(6, values.get(j));

                    int readingUpdate = readingPs.executeUpdate();
                    readingPs.close();
                    //logger.debug("event reading inserted...");
                    readingIndex++;
                }
            }
        } catch (Exception e) {
            logger.info("Error (commitEvents): " + e.getMessage());
        }
    }

    public void printSummaryEventData(Vector<Event> events) {

    }

}
