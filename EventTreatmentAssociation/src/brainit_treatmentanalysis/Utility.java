/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brainit_treatmentanalysis;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;

/**
 *
 * @author anthony
 */
public class Utility {

    public void Utility() {
    }

    public long compareTimepoints(String _timepoint1, String _timepoint2) {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date1 = null;
        Date date2 = null;

        //Comparing strings of the format: "2003-09-25T22:35:00"^^<http://www.w3.org/2001/XMLSchema#dateTime>

        String timepoint1 = _timepoint1;
        String timepoint2 = _timepoint2;

        //Drop the literal qualifier at the end
        timepoint1 = timepoint1.substring(0, timepoint1.indexOf("^^"));
        timepoint2 = timepoint2.substring(0, timepoint2.indexOf("^^"));

        //System.out.println("timepoint1: " + timepoint1);
        //System.out.println("timepoint2: " + timepoint2);

        //Remove the quotation marks
        timepoint1 = timepoint1.substring(1, timepoint1.length() - 1);
        timepoint2 = timepoint2.substring(1, timepoint2.length() - 1);

        //System.out.println("timepoint1: " + timepoint1);
        //System.out.println("timepoint2: " + timepoint2);

        //Format is now: 2003-09-25T22:35:00
        //Break down to compare both sides of this
        String day1 = timepoint1.substring(0, timepoint1.indexOf("T"));
        String time1 = timepoint1.substring(timepoint1.indexOf("T") + 1, timepoint1.length());

        String day2 = timepoint2.substring(0, timepoint2.indexOf("T"));
        String time2 = timepoint2.substring(timepoint2.indexOf("T") + 1, timepoint2.length());

        String dateStr1 = day1 + " " + time1;
        String dateStr2 = day2 + " " + time2;

        //System.out.println("dateStr1: " + dateStr1);
        //System.out.println("dateStr2: " + dateStr2);


        try {
            date1 = (Date) formatter.parse(dateStr1);
            date2 = (Date) formatter.parse(dateStr2);
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
        }

        //Convert the dates to longs
        long dateLong1 = date1.getTime();
        long dateLong2 = date2.getTime();

        long dateDifference = dateLong1 - dateLong2;

        //Divide by 60000 to get this value in minutes
        dateDifference = dateDifference / 60000;
        return dateDifference;
    }

    public long convertTimepoint(String timepointIn) {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date1 = null;

        //Drop the literal qualifier at the end
        timepointIn = timepointIn.substring(timepointIn.indexOf("^^"), timepointIn.length());

        //Remove the quotation marks
        timepointIn = timepointIn.substring(1, timepointIn.length() - 1);

        //Format is now: 2003-09-25T22:35:00
        //Break down to compare both sides of this
        String day1 = timepointIn.substring(0, timepointIn.indexOf("T"));
        String time1 = timepointIn.substring(timepointIn.indexOf("T"), timepointIn.length());

        String dateStr1 = day1 + " " + time1;

        try {
            date1 = (Date) formatter.parse(dateStr1);
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
        }

        return date1.getTime();
    }

    public String convertTimepoint(long timepointIn) {
        Date date1 = new Date(timepointIn);
        return date1.toString();
    }

    public String setTimeWindow(String eventStart, String eventEnd, float timeWindow) throws Exception {

        SimpleDateFormat df = df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        //Add the time window (in seconds -> convert to milliseconds) to before and after the official event
        java.util.Date eventStartDate = df.parse(eventStart);
        java.util.Date eventEndDate = df.parse(eventEnd);

        //Get the times in milliseconds (long format)
        long eventStartLong = eventStartDate.getTime();
        long eventEndLong = eventEndDate.getTime();

        //Convert window to milliseconds
        long timeWindowLong = ((long) timeWindow * 1000);

        //Subtract window from start, add window to end
        long eventStartLongOut = eventStartLong - timeWindowLong;
        long eventEndLongOut = eventEndLong + timeWindowLong;

        eventStartDate = new java.util.Date(eventStartLongOut);
        eventEndDate = new java.util.Date(eventEndLongOut);

        String timeWindowOut = eventStartDate.toString() + "||" + eventEndDate.toString();
        return timeWindowOut;
    }
    
    public String convertBackToLiterals(String timestampIn){
        
        //Converting [dayOfWeek month date hh:mm:ss timezone year]
        //Into [year-month-dayThh:mm:ss]                
        StringTokenizer st = new StringTokenizer(timestampIn);
        String time = "";
        String year = "";
        String month = "";
        String day = "";
        while(st.hasMoreTokens()){
            st.nextToken();
            month = st.nextToken();
            day = st.nextToken();
            time = st.nextToken();
            st.nextToken();
            year = st.nextToken();
        }
        
        if(month.equals("Jan")){
            month = "01";
        }else if(month.equals("Feb")){
            month = "02";
        }else if(month.equals("Mar")){
            month = "03";
        }else if(month.equals("Apr")){
            month = "04";
        }else if(month.equals("May")){
            month = "05";
        }else if(month.equals("Jun")){
            month = "06";
        }else if(month.equals("Jul")){
            month = "07";
        }else if(month.equals("Aug")){
            month = "08";
        }else if(month.equals("Sep")){
            month = "09";
        }else if(month.equals("Oct")){
            month = "10";
        }else if(month.equals("Nov")){
            month = "11";
        }else if(month.equals("Dec")){
            month = "12";
        }
        
        String timestampOut = "" + year + "-" + month + "-" + day + "T" + time;
        return timestampOut;
    }

    public String formatLiterals(String literalValueIn) {
        int circIndex = literalValueIn.indexOf("^^");
        String valueOut = literalValueIn.substring(0, circIndex);
        return valueOut;
    }

    public String formatTimestamp(String timestampIn) {
        return timestampIn.replace("T", " ");
    }
    
    public String formatTimestampInverse(String timestampIn) {
        return timestampIn.replace(" ", "T");
    }
}
