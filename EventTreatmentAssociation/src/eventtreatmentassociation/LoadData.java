/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 *
 * @author astell
 */
public class LoadData {

    private Logger logger;

    public LoadData(Logger _logger) {
        logger = _logger;
    }

    public Vector<String> getPatientIDs(String filepath) {

        Vector<String> patientIDs = new Vector<String>();
        File folder = new File(filepath);
        File[] listOfFiles = folder.listFiles();
        int fileNum = listOfFiles.length;

        //For each one ending with "_treatment_summary.txt", grab the patientID - add to a vector of IDs
        for (int i = 0; i < fileNum; i++) {
            File fileIn = listOfFiles[i].getAbsoluteFile();
            String fileInName = fileIn.getName();

            String patientID = "";
            if (fileInName.endsWith("_treatment_summary.txt")) {
                int underscoreIndex = fileInName.indexOf("_");
                patientID = fileInName.substring(0, underscoreIndex);
            }
            if (!patientID.equals("")) {
                patientIDs.add(patientID);
            }
        }
        return patientIDs;
    }

    public Vector<Treatment> getTreatments(String filepath, String patientID) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        String filename = filepath + patientID + "_treatment_summary.txt";

        FileReader fr = null;
        BufferedReader br = null;

        Vector<Treatment> treatments = new Vector<Treatment>();
        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);

            boolean treatmentPhase = false;
            while (br.ready()) {
                String lineIn = br.readLine();

                if (!treatmentPhase) {
                    treatmentPhase = lineIn.equals("====");
                } else {
                    //Do all the treatment processing stuff

                    int hyphenIndex = lineIn.indexOf(" - ");
                    String lineInLeft = lineIn.substring(0, hyphenIndex);
                    lineInLeft = lineInLeft.trim();

                    StringTokenizer stLeft = new StringTokenizer(lineInLeft);
                    stLeft.nextToken(); //This is the number
                    String timestampStr = stLeft.nextToken() + " " + stLeft.nextToken();

                    String lineInRight = lineIn.substring(hyphenIndex + 1, lineIn.length());
                    lineInRight = lineInRight.trim();
                    StringTokenizer stRight = new StringTokenizer(lineInRight, ",");

                    String[] treatmentValues = new String[3];
                    String value = "";
                    String desc = "";
                    String target = "";
                    int tokenCount = 0;
                    while (stRight.hasMoreTokens()) {
                        treatmentValues[tokenCount] = stRight.nextToken();
                        tokenCount++;
                    }
                    value = treatmentValues[0];
                    desc = treatmentValues[1];
                    target = treatmentValues[2];

                    if (value == null) {
                        value = "";
                    }
                    if (desc == null) {
                        desc = "";
                    }
                    if (target == null) {
                        target = "";
                    }
                    Date timestamp = df.parse(timestampStr);
                    Treatment treatment = new Treatment(value, desc, target, timestamp);
                    treatments.add(treatment);
                }
            }
        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
        return treatments;
    }

    public Vector<Event> getEvents(String parameterFeed, String patientID, String holdDownFeed) {

        SimpleDateFormat df2 = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
        FileReader fr = null;
        BufferedReader br = null;

        Vector<Event> events = new Vector<Event>();

        String filepathEvents = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\EventDetection\\output\\";
        String filenameEvents = filepathEvents + patientID + "_" + parameterFeed + "_holddown" + holdDownFeed + "_events.csv";

        //For now, we want start, end, values and feed        
        int eventCount = 0;
        Event event = null;
        try {
            fr = new FileReader(filenameEvents);
            br = new BufferedReader(fr);

            int eventLineCount = 0;
            while (br.ready()) {
                String lineIn = br.readLine();

                if (eventLineCount == 1) {
                    event = new Event(eventCount, parameterFeed);
                } else if (eventLineCount == 2 || eventLineCount == 3) {
                    //This is the start or end values
                    int colonIndex = lineIn.indexOf(":");
                    String timestampStr = lineIn.substring(colonIndex + 1, lineIn.length());
                    
                    timestampStr = timestampStr.trim();
                    Date timestamp = df2.parse(timestampStr);
                    if (eventLineCount == 2) {                        
                        event.setStart(timestamp);
                    } else {                        
                        event.setEnd(timestamp);
                    }
                } else if (eventLineCount == 4) {
                    //This is the set of values for this event
                    StringTokenizer st = new StringTokenizer(lineIn, ",");
                    Vector<String> eventValues = new Vector<String>();
                    /*if(patientID.equals("5027262")){
                        logger.info("st.countTokens: " + st.countTokens());
                    }*/
                    //int timeCount = 1440; //This is an upper limit of 24hrs for a single event
                    int timeCount = 480; //This is an upper limit of 8hrs for a single event
                    //while (st.hasMoreTokens() && st.countTokens() < timeCount) {
                    while (st.hasMoreTokens()) {
                        String valueIn = st.nextToken();
                        eventValues.add(valueIn);
                    }
                    event.setValues(eventValues);

                    events.add(event);
                    eventCount++;
                }
                eventLineCount++;
                if (eventLineCount == 5) {
                    eventLineCount = 0;
                }
                
                /*if(patientID.equals("5027262")){
                    logger.info("eventLineCount: " + eventLineCount);
                }*/                
            }
            //logger.info("eventCount: " + eventCount);
        } catch (Exception e) {
            //logger.info("Error: " + e.getMessage());
            //System.out.println("Error: " + e.getMessage());
        }
        return events;
    }
}
