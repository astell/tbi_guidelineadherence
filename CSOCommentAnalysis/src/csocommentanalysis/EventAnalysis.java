package csocommentanalysis;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
import java.io.*;
import java.util.Vector;
import java.util.TreeMap;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author astell
 */
public class EventAnalysis {

    public EventAnalysis() {

    }

    public void iccaCombo() {

        //Read the live event annotations in
        String filepath = "C:\\Users\\astell\\Documents\\PhD\\PhD\\Data\\CSO\\CSOCommentAnalysis\\";
        String filename = filepath + "CSO_patient_events_out.txt";

        Vector<Vector> liveEventsIn = new Vector<Vector>();
        Vector<Vector> nonLiveEventsIn = new Vector<Vector>();
        boolean nonLiveEvents = false;
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            while (br.ready()) {
                Vector<String> eventIn = new Vector<String>();
                String lineIn = br.readLine();
                if (lineIn.contains("NON-LIVE")) {
                    nonLiveEvents = true;
                    
                }else{
                    
                    StringTokenizer st = new StringTokenizer(lineIn, ",");
                    int tokenCount = 0;
                    String pid = "";
                    String timestamp = "";
                    while (st.hasMoreTokens()) {
                        String tokenIn = st.nextToken();
                        tokenIn = tokenIn.trim();
                        if (tokenCount == 1) {
                            pid = tokenIn;
                        } else if (tokenCount == 2) {
                            timestamp = tokenIn;
                        }
                        tokenCount++;
                    }
                    eventIn.add(pid);
                    eventIn.add(timestamp);
                    pid = pid.trim();
                    timestamp = timestamp.trim();
                    if (!pid.equals("") && !timestamp.equals("") && timestamp.contains("/")) {
                        if (nonLiveEvents) {
                            nonLiveEventsIn.add(eventIn);
                        } else {
                            liveEventsIn.add(eventIn);
                        }
                    }
                }
            }

            //For each one, search through the corresponding digest files and print out information within +/- 10 mins of the timestamp
            int liveEventNum = liveEventsIn.size();
            for (int i = 0; i < liveEventNum; i++) {
            //for (int i = 0; i < 5; i++) {
                Vector<String> liveEventIn = liveEventsIn.get(i);
                System.out.println("liveEventIn: " + liveEventIn);
                String pidIn = liveEventIn.get(0);
                String timestampIn = liveEventIn.get(1);
                this.printIccaData(pidIn, timestampIn, false, null);
            }

            //For each one, search through the corresponding digest files and print out information within +/- 10 mins of the timestamp
            int nonLiveEventNum = nonLiveEventsIn.size();
            for (int i = 0; i < nonLiveEventNum; i++) {
            //for (int i = 0; i < 5; i++) {
                Vector<String> nonLiveEventIn = nonLiveEventsIn.get(i);
                System.out.println("nonLiveEventIn: " + nonLiveEventIn);
                String pidIn = nonLiveEventIn.get(0);
                String timestampIn = nonLiveEventIn.get(1);
                this.printIccaData(pidIn, timestampIn, true, null);
            }

        } catch (Exception e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    private String getTimestampLowerBound(String timestamp, int LOWER_BOUND_MINS) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        java.util.Date timestampInDate = sdf.parse(timestamp);
        long lowerBoundLong = timestampInDate.getTime() - (LOWER_BOUND_MINS * 60000);
        java.util.Date lowerBoundDate = new java.util.Date(lowerBoundLong);
        return sdf.format(lowerBoundDate);
    }

    private String getTimestampUpperBound(String timestamp, int UPPER_BOUND_MINS) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        java.util.Date timestampInDate = sdf.parse(timestamp);
        long upperBoundLong = timestampInDate.getTime() + (UPPER_BOUND_MINS * 60000);
        java.util.Date upperBoundDate = new java.util.Date(upperBoundLong);
        return sdf.format(upperBoundDate);
    }

    private boolean getTokenWithinTimeWindow(String timestamp, String timestampLowerBound, String timestampUpperBound) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        java.util.Date timestampInDate = sdf.parse(timestamp);
        java.util.Date timestampLowerDate = sdf.parse(timestampLowerBound);
        java.util.Date timestampUpperDate = sdf.parse(timestampUpperBound);
        return (timestampInDate.getTime() > timestampLowerDate.getTime()) && (timestampInDate.getTime() < timestampUpperDate.getTime());
    }

    private void printIccaData(String pid, String timestamp, boolean nonLiveEvents, BufferedWriter bw) throws Exception {
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        java.util.Date timestampDate = null;
        try{
            timestampDate = sdf.parse(timestamp);
        }catch(Exception e){
            return;
        }
                
        //int LOWER_BOUND_MINS = 2;
        //int UPPER_BOUND_MINS = 10;

        //String timestampLowerBound = this.getTimestampLowerBound(timestamp, LOWER_BOUND_MINS);
        //String timestampUpperBound = this.getTimestampUpperBound(timestamp, UPPER_BOUND_MINS);

        //System.out.println("timestamp: " + timestamp);
        //System.out.println("timestampLowerBound: " + timestampLowerBound);
        //System.out.println("timestampUpperBound: " + timestampUpperBound);
        String filepath = "E:\\PhD Data - 291116\\RAW_CSO_WAVE_DATA\\";
        String foldername = filepath + "zero_drop_avg_output_run2\\";

        File folderName = new File(foldername);
        if (folderName.exists() && folderName.isDirectory()) {
            File[] files = folderName.listFiles();
            int fileNum = files.length;            
            int fileCount = 0;
            while (fileCount < fileNum) {
            //while (fileCount < 1) {

                String waveFile = files[fileCount].getName();
                if (waveFile.contains(pid)) {
                    
                    FileReader fr = new FileReader(foldername + waveFile);
                    BufferedReader br = new BufferedReader(fr);

                    
                    //int lineCount = 0;
                    //boolean inTimestamp = false;
                    boolean entryFound = false;
                    while (br.ready() && !entryFound) {
                        String lineIn = br.readLine();
                        String timestampToken = "";
                        timestampToken = lineIn.substring(0, lineIn.indexOf(" = "));
                        
                        //Convert timestampToken to date and compare
                        java.util.Date timestampTokenDate = null;
                        long timestampDiff = -1;
                        try{
                            timestampTokenDate = sdf.parse(timestampToken);                        
                            timestampDiff = timestampTokenDate.getTime() - timestampDate.getTime();
                        }catch(Exception e){
                            
                        }
                        if(timestampDiff > 0){
                            entryFound = true;
                            long timestampDiffMins = timestampDiff / 60000;                                                        
                            System.out.println("" + timestampToken + " --> " + timestampDiffMins);
                            bw.write("" + timestampToken + " --> " + timestampDiffMins + "\r\n");
                        }
                        
                        /*if (nonLiveEvents) {
                            StringTokenizer st = new StringTokenizer(lineIn, ",");
                            int tokenNum = st.countTokens();
                            if (tokenNum > 5) {
                                timestampToken = st.nextToken();
                                for (int j = 0; j < 3; j++) {
                                    st.nextToken();
                                }
                                String abpMean = st.nextToken();
                                lineIn = timestampToken + " = " + abpMean + "\r\n";
                            }
                        } else {
                            timestampToken = lineIn.substring(0, lineIn.indexOf(" = "));
                        }*/
                        //boolean tokenWithinTimeWindow = this.getTokenWithinTimeWindow(timestampToken, timestampLowerBound, timestampUpperBound);
                        //if(timestampToken.contains(timestamp)){
                        /*if (tokenWithinTimeWindow) {
                            if (lineCount == 0) {
                                System.out.println("==== Start of window (" + timestamp + ") ====");
                                inTimestamp = true;
                            }
                            System.out.println(lineIn);
                            entryFound = true;
                            lineCount++;
                        } else {
                            if (inTimestamp) {
                                System.out.println("==== End of window (" + timestamp + ") ====");
                                inTimestamp = false;
                            }
                        }*/
                    }
                }
                fileCount++;
                System.out.println("======");
            }
        }
    }

    public void waveFormAnalysis() {

        String filepath = "E:\\PhD Data - 291116\\";
        String waveFoldername = filepath + "RAW_CSO_WAVE_DATA\\";

        int LINE_LIMIT = 450000; //1s = 1000, 10s = 10000, 1min = 60000
        System.out.println("LINE_LIMIT: " + LINE_LIMIT);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {

            File folderName = new File(waveFoldername);
            if (folderName.exists() && folderName.isDirectory()) {
                File[] files = folderName.listFiles();
                int fileNum = files.length;
                for (int k = 0; k < fileNum; k++) {
                //for (int k = 0; k < 2; k++) {

                    String waveFile = files[k].getName();
                    //String waveFile = "CSO_108.csv";
                    System.out.println("waveFile: " + waveFile);

                    if (!files[k].isDirectory()) {

                        FileReader fr = new FileReader(waveFoldername + waveFile);
                        BufferedReader br = new BufferedReader(fr);

                        String waveFilename = waveFile.substring(0, waveFile.length() - 4);
                        System.out.println("waveFilename: " + waveFilename);

                        String waveAbpFilenameOut = waveFoldername + "Condensed_output_run2\\" + waveFilename + ".txt";

                        FileWriter fw = new FileWriter(waveAbpFilenameOut, true);
                        BufferedWriter bw = new BufferedWriter(fw);

                        String headerIn = br.readLine();
                        StringTokenizer st = new StringTokenizer(headerIn, ",");
                        int abpIndex = -1;
                        int tokenCount = 0;
                        while (st.hasMoreTokens()) {
                            String tokenIn = st.nextToken();
                            if (tokenIn.contains("ABP-ABP(mmHg)")) {
                                abpIndex = tokenCount;
                            }
                            tokenCount++;
                        }
                        //System.out.println("abpIndex: " + abpIndex);

                        String lineIn = "";
                        int lineCount = 0;
                        Vector<Double> abpValues = new Vector<Double>();
                        String lastTimestamp = "";
                        //while (br.ready() && lineCount < LINE_LIMIT) {
                        //for(int m=0; m<6; m++){    
                        while (br.ready()) {
                            String timestamp = "";
                            //String timestampLong = "";
                            lineIn = br.readLine();

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
                            st = new StringTokenizer(lineOut, ",");
                            int tokenNum = st.countTokens();
                            //System.out.println("lineOut(" + tokenNum + "," + abpIndex + "): " + lineOut);

                            String tokenIn = "";
                            for (int i = 0; i <= abpIndex; i++) {
                                tokenIn = st.nextToken();
                                if (i == 0) {
                                    timestamp = sdf.format(Long.parseLong(tokenIn));
                                    //timestampLong = tokenIn;
                                    //System.out.println("timestamp (" + lineCount + "): " + timestamp);
                                    //timestamp = tokenIn.trim();
                                    //timestamp = timestamp.substring(0,timestamp.length()-3);
                                }
                            }
                            if (timestamp.equals(lastTimestamp)) {
                                tokenIn = tokenIn.trim();
                                //System.out.println(tokenIn);
                                if (!tokenIn.equals("")) {
                                    abpValues.add(Double.parseDouble(tokenIn));
                                }
                            } else {
                                //Calculate the average from the vector already compiled
                                double totalValues = 0.0;
                                for (int i = 0; i < abpValues.size(); i++) {
                                    totalValues += abpValues.get(i).doubleValue();
                                }
                                double avgValues = totalValues / ((double) abpValues.size());
                                avgValues = Math.round(avgValues * 100.0) / 100.0;
                                //System.out.println("" + timestamp + " (" + timestampLong + ") = " + avgValues);
                                bw.write("" + timestamp + " = " + avgValues + "\r\n");
                                /*if(timestamp.equals("31/10/2014 02:59:07") || timestamp.equals("31/10/2014 02:59:08") || timestamp.equals("31/10/2014 02:45:20")){
                                    bw.write("================" + "\r\n");
                                    bw.write("abpValues.size(): " + abpValues.size() + "\r\n");
                                    for (int i = 0; i < abpValues.size(); i++) {
                                        bw.write(abpValues.get(i).doubleValue() + "\r\n");
                                    }
                                    bw.write("================" + "\r\n");
                                }*/
                                //Reset the vector
                                abpValues = new Vector<Double>();
                            }
                            lastTimestamp = timestamp;

                            lineCount++;
                        }
                        //System.out.println("lineCount: " + lineCount);

                        bw.close();
                        fw.close();

                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public void detectZeroDrops() {
        
        Utility util = new Utility();

        String filepath = "E:\\PhD Data - 291116\\";
        String waveFoldername = filepath + "RAW_CSO_WAVE_DATA\\";

        int LINE_LIMIT = 60000; //10s = 10000, 1min = 60000
        System.out.println("LINE_LIMIT: " + LINE_LIMIT);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {

            File folderName = new File(waveFoldername);
            if (folderName.exists() && folderName.isDirectory()) {
                /*File[] files = folderName.listFiles();
                int fileNum = files.length;
                for (int k = 0; k < fileNum; k++) {*/
                
                
                
                    //String waveFile = files[k].getName();
                    String waveFile = "CSO_0482.csv";
                    System.out.println("waveFile: " + waveFile);

                    //if (!files[k].isDirectory()) {

                        FileReader fr = new FileReader(waveFoldername + waveFile);
                        BufferedReader br = new BufferedReader(fr);

                        String waveFilename = waveFile.substring(0, waveFile.length() - 4);
                        System.out.println("waveFilename: " + waveFilename);

                        String waveAbpFilenameOut = waveFoldername + "zero_drop_avg_output_run2\\" + waveFilename + ".txt";

                        FileWriter fw = new FileWriter(waveAbpFilenameOut, true);
                        BufferedWriter bw = new BufferedWriter(fw);

                        String headerIn = br.readLine();
                        System.out.println("headerIn: " + headerIn);
                        StringTokenizer st = new StringTokenizer(headerIn, ",");
                        int abpIndex = -1;
                        int tokenCount = 0;
                        while (st.hasMoreTokens()) {
                            String tokenIn = st.nextToken();
                            if (tokenIn.contains("ABP.mean")) {
                                abpIndex = tokenCount;
                            }
                            tokenCount++;
                        }
                        System.out.println("abpIndex: " + abpIndex);

                        String lineIn = "";
                        int lineCount = 0;
                        Vector<Double> abpValues = new Vector<Double>();
                        String lastTimestamp = "";
                        //while (br.ready() && lineCount < LINE_LIMIT) {
                        while (br.ready()) {
                            String timestamp = "";
                            lineIn = br.readLine();

                            //Process empty commas to contain blanks
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
                            st = new StringTokenizer(lineOut, ",");
                            int tokenNum = st.countTokens();
                            //System.out.println("lineOut(" + tokenNum + "," + abpIndex + "): " + lineOut);

                            String tokenIn = "";
                            for (int i = 0; i <= abpIndex; i++) {
                                tokenIn = st.nextToken();
                                if (i == 0) {
                                    //timestamp = sdf.format(Long.parseLong(tokenIn));
                                    timestamp = tokenIn.trim();
                                    //timestamp = util.rearrangeDate(timestamp);
                                    timestamp = timestamp.substring(0,timestamp.length()-3);
                                }
                            }
                            
                            /*String abpToken = tokenIn.trim();
                            try{
                                Double abpDouble = Double.parseDouble(abpToken);                                
                                if(abpDouble.equals(0.0)){
                                    bw.write("" + timestamp + " = " + abpDouble + "\r\n");
                                }
                            }catch(Exception e){
                                //System.out.println("Number format exception: " + e.getMessage());
                            }*/
                            
                            if (timestamp.equals(lastTimestamp)) {
                                tokenIn = tokenIn.trim();
                                //System.out.println(tokenIn);
                                if (!tokenIn.equals("")) {
                                    abpValues.add(Double.parseDouble(tokenIn));
                                }
                            } else {
                                //Calculate the average from the vector already compiled
                                double totalValues = 0.0;
                                for (int i = 0; i < abpValues.size(); i++) {
                                    totalValues += abpValues.get(i).doubleValue();
                                }
                                double avgValues = totalValues / ((double) abpValues.size());
                                avgValues = Math.round(avgValues * 100.0) / 100.0;
                                //System.out.println("" + timestamp + " = " + avgValues);
                                if(avgValues < 10.0){
                                    bw.write("" + timestamp + " = " + avgValues + "\r\n");
                                }

                                //Reset the vector
                                abpValues = new Vector<Double>();
                            }
                            lastTimestamp = timestamp;
                            /*tokenIn = tokenIn.trim();
                            double tokenDouble = -1.0;
                            try{
                                tokenDouble = Double.parseDouble(tokenIn);
                            }catch(Exception e){
                                
                            }                            
                            if(tokenDouble < 10.0){
                                bw.write("" + timestamp + " = " + tokenDouble + "\r\n");
                            }*/

                            lineCount++;
                        }
                        System.out.println("lineCount: " + lineCount);
                        System.out.println("=======");

                        bw.close();
                        fw.close();

                    //}
                }
            //}
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public void compareEventsAndZeroDrops(){
        
        //Read the live event annotations in
        String filepath = "C:\\Users\\astell\\Documents\\PhD\\PhD\\Data\\CSO\\CSOCommentAnalysis\\";
        String filename = filepath + "CSO_patient_events_out.txt";

        Vector<Vector> liveEventsIn = new Vector<Vector>();
        Vector<Vector> nonLiveEventsIn = new Vector<Vector>();
        boolean nonLiveEvents = false;
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            while (br.ready()) {
                Vector<String> eventIn = new Vector<String>();
                String lineIn = br.readLine();
                if (lineIn.contains("NON-LIVE")) {
                    nonLiveEvents = true;
                    
                }else{
                    
                    StringTokenizer st = new StringTokenizer(lineIn, ",");
                    int tokenCount = 0;
                    String pid = "";
                    String timestamp = "";
                    while (st.hasMoreTokens()) {
                        String tokenIn = st.nextToken();
                        tokenIn = tokenIn.trim();
                        if (tokenCount == 1) {
                            pid = tokenIn;
                        } else if (tokenCount == 2) {
                            timestamp = tokenIn;
                        }
                        tokenCount++;
                    }
                    eventIn.add(pid);
                    eventIn.add(timestamp);
                    pid = pid.trim();
                    timestamp = timestamp.trim();
                    if (!pid.equals("") && !timestamp.equals("") && timestamp.contains("/")) {
                        if (nonLiveEvents) {
                            nonLiveEventsIn.add(eventIn);
                        } else {
                            liveEventsIn.add(eventIn);
                        }
                    }
                }
            }
            
            String waveAbpFilenameOut = filepath + "zero_drop_distances_run2.txt";
            FileWriter fw = new FileWriter(waveAbpFilenameOut, true);
            BufferedWriter bw = new BufferedWriter(fw);
            
            //For each one, search through the corresponding digest files and print out information within +/- 10 mins of the timestamp
            int liveEventNum = liveEventsIn.size();
            for (int i = 0; i < liveEventNum; i++) {
            //for (int i = 0; i < 1; i++) {
                Vector<String> liveEventIn = liveEventsIn.get(i);
                System.out.println("liveEventIn: " + liveEventIn);
                bw.write("liveEventIn: " + liveEventIn + "\r\n");
                String pidIn = liveEventIn.get(0);
                String timestampIn = liveEventIn.get(1);
                this.printIccaData(pidIn, timestampIn, false, bw);
            }

            //For each one, search through the corresponding digest files and print out information within +/- 10 mins of the timestamp
            int nonLiveEventNum = nonLiveEventsIn.size();
            for (int i = 0; i < nonLiveEventNum; i++) {
            //for (int i = 0; i < 1; i++) {
                Vector<String> nonLiveEventIn = nonLiveEventsIn.get(i);
                System.out.println("nonLiveEventIn: " + nonLiveEventIn);
                bw.write("nonLiveEventIn: " + nonLiveEventIn + "\r\n");
                String pidIn = nonLiveEventIn.get(0);
                String timestampIn = nonLiveEventIn.get(1);
                this.printIccaData(pidIn, timestampIn, true, bw);
            }
            
            bw.close();
            fw.close();

        } catch (Exception e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    public void analyse() {

        //Open up the LIVE commentary file
        String filepath = "C:\\Users\\astell\\Documents\\PhD\\PhD\\Data\\CSO\\CSOCommentAnalysis\\";
        String liveFoldername = filepath + "Live_comment_files\\";
        String nonLiveFilename = filepath + "NON-LIVE_ICCA_TESTSET.xls";
        String filenameOut = filepath + "CSO_patient_events_out.txt";

        Utility util = new Utility();
        Vector<Vector> patientNonLiveEvents = new Vector<Vector>();
        Vector<Vector> patientLiveEvents = new Vector<Vector>();

        try {
            DataFormatter dataFormatter = new DataFormatter();

            //Need to work through the folder of Excel files
            //Format is: date in first column, time in second, type in fourth
            File folderName = new File(liveFoldername);
            if (folderName.exists() && folderName.isDirectory()) {
                File[] files = folderName.listFiles();
                int fileNum = files.length;
                for (int i = 0; i < fileNum; i++) {

                    String pid = files[i].getName();
                    pid = pid.substring(0, 8);
                    Workbook workbookLive = WorkbookFactory.create(files[i]);
                    int sheetLiveNum = workbookLive.getNumberOfSheets();

                    int sheetIndex = 0;
                    if (sheetLiveNum > 1) {
                        sheetIndex = 1;
                    }
                    Sheet sheetLive = workbookLive.getSheetAt(sheetIndex);

                    int rowCount = 0;
                    for (Row row : sheetLive) {
                        boolean recordEvent = util.getRecordLiveEvent(i, rowCount);
                        int cellCount = 0;
                        if (recordEvent) {
                            Vector<String> eventDetail = new Vector<String>();
                            eventDetail.add(pid);
                            String lineDateTime = "";
                            for (Cell cell : row) {
                                String cellValue = dataFormatter.formatCellValue(cell);
                                if (cellCount == 3) {
                                    eventDetail.add(cellValue);
                                } else if (cellCount == 0) {
                                    lineDateTime = cellValue;
                                } else if (cellCount == 1) {
                                    cellValue = util.reformatTime(cellValue);
                                    lineDateTime += " " + cellValue;
                                    eventDetail.add(lineDateTime);
                                }
                                cellCount++;
                            }
                            patientLiveEvents.add(eventDetail);
                        }
                        rowCount++;
                    }
                }
            } else {
                System.out.println("Error in folder");
            }

            Workbook workbookNonLive = WorkbookFactory.create(new File(nonLiveFilename));
            Sheet sheetNonLive = workbookNonLive.getSheetAt(0);

            int rowCount = 0;
            for (Row row : sheetNonLive) {

                boolean recordEvent = util.getRecordNonLiveEvent(rowCount);
                int cellCount = 0;
                if (recordEvent) {
                    Vector<String> eventDetail = new Vector<String>();
                    for (Cell cell : row) {
                        String cellValue = dataFormatter.formatCellValue(cell);
                        if (cellCount == 1 || cellCount == 3 || cellCount == 4) {
                            if (cellCount == 3) {
                                cellValue = util.reformatDate(cellValue);
                            }
                            eventDetail.add(cellValue);
                        }
                        cellCount++;
                    }
                    patientNonLiveEvents.add(eventDetail);
                }
                rowCount++;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("LIVE patientEvents.size(): " + patientLiveEvents.size());
        for (int i = 0; i < patientLiveEvents.size(); i++) {
            System.out.println("patientLiveEvents(" + i + "): " + patientLiveEvents.get(i));
        }
        System.out.println("====");

        System.out.println("NON-LIVE patientEvents.size(): " + patientNonLiveEvents.size());
        for (int i = 0; i < patientNonLiveEvents.size(); i++) {
            System.out.println("patientNonLiveEvents(" + i + "): " + patientNonLiveEvents.get(i));
        }

        try {
            FileWriter fw = new FileWriter(filenameOut, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("===== LIVE EVENTS =====\r\n");
            for (int i = 0; i < patientLiveEvents.size(); i++) {
                Vector<String> patientLiveEventIn = patientLiveEvents.get(i);
                bw.write(i + ",");
                for (int j = 0; j < patientLiveEventIn.size(); j++) {
                    bw.write(patientLiveEventIn.get(j) + ",");
                }
                bw.write("\r\n");
            }
            bw.write("===== NON-LIVE EVENTS =====\r\n");
            for (int i = 0; i < patientNonLiveEvents.size(); i++) {
                Vector<String> patientNonLiveEventIn = patientNonLiveEvents.get(i);
                bw.write(i + ",");
                for (int j = 0; j < patientNonLiveEventIn.size(); j++) {
                    bw.write(patientNonLiveEventIn.get(j) + ",");
                }
                bw.write("\r\n");
            }
            bw.write("=====\r\n");
            bw.close();
            fw.close();
        } catch (Exception e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
}
