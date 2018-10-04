/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csocommentanalysis;

/**
 *
 * @author astell
 */
public class Utility {
    
    public Utility(){
        
    }
    
    public boolean getRecordLiveEvent(int fileNum, int rowNum){
        
        boolean recordLiveEvent = false;
        if(fileNum == 0){
            recordLiveEvent = (
                    rowNum == 25
                    || rowNum == 32
                    || rowNum == 44
                    || rowNum == 55
                    || rowNum == 104
                    || rowNum == 127                  
                    );
        }else if(fileNum == 1){
            recordLiveEvent = (
                    rowNum == 10
                    || rowNum == 20
                    || rowNum == 50
                    || rowNum == 88
                    || rowNum == 98
                    || rowNum == 102
                    || rowNum == 117
                    );
        }else if(fileNum == 3){
            recordLiveEvent = (
                    rowNum == 99
                    || rowNum == 101
                    );
        }else if(fileNum == 4){
            recordLiveEvent = (
                    rowNum == 5
                    || rowNum == 27
                    || rowNum == 31
                    );
        }else if(fileNum == 5){
            recordLiveEvent = (
                    rowNum == 7
                    || rowNum == 11
                    || rowNum == 46
                    || rowNum == 62
                    || rowNum == 72
                    );
        }else if(fileNum == 6){
            recordLiveEvent = (
                    rowNum == 19
                    || rowNum == 21
                    || rowNum == 23
                    || rowNum == 27
                    || rowNum == 34
                    );
        }else if(fileNum == 7){
            recordLiveEvent = (
                    rowNum == 11
                    || rowNum == 28
                    || rowNum == 53
                    );
        }else if(fileNum == 8){
            recordLiveEvent = (
                    rowNum == 5
                    || rowNum == 13
                    || rowNum == 16
                    || rowNum == 21
                    );
        }else if(fileNum == 9){
            recordLiveEvent = (
                    rowNum == 7
                    || rowNum == 14
                    || rowNum == 23
                    || rowNum == 36
                    || rowNum == 40
                    || rowNum == 49
                    || rowNum == 61                    
                    );
        }else if(fileNum == 10){
            recordLiveEvent = (
                    rowNum == 7
                    || rowNum == 9
                    || rowNum == 13
                    || rowNum == 17
                    || rowNum == 18
                    );
        }else if(fileNum == 11){
            recordLiveEvent = (
                    rowNum == 10
                    || rowNum == 16
                    || rowNum == 27
                    || rowNum == 28
                    );
        }
        
        return recordLiveEvent;
    }
    
    public boolean getRecordNonLiveEvent(int rowNum){
                
        return rowNum == 4
                || rowNum == 5
                || rowNum == 10
                || rowNum == 13
                || rowNum == 14
                || rowNum == 19
                || rowNum == 20
                || rowNum == 23
                || rowNum == 24
                || rowNum == 25
                || rowNum == 26
                || rowNum == 27
                || rowNum == 28
                || rowNum == 29
                || rowNum == 30
                || rowNum == 33
                || rowNum == 34
                || rowNum == 35
                || rowNum == 40
                || rowNum == 41
                || rowNum == 42
                || rowNum == 45
                || rowNum == 173
                || rowNum == 174
                || rowNum == 178
                || rowNum == 180
                || rowNum == 183;
    }
    
    public String reformatDate(String dateIn){
        
        String dateStr = dateIn.substring(0,dateIn.indexOf(" "));
        String timeStr = dateIn.substring(dateIn.indexOf(" ")+1,dateIn.length());
                
        dateStr = dateStr.trim();
        int slashIndex = dateStr.indexOf("/");
        int slashIndex2 = dateStr.lastIndexOf("/");
        String monthStr = dateStr.substring(0,slashIndex);
        String dayStr = dateStr.substring(slashIndex+1,slashIndex2);
        String yearStr = dateStr.substring(slashIndex2+1,slashIndex2+3);
        
        String dateOut = dayStr + "/" + monthStr + "/20" + yearStr + " " + timeStr;
        return dateOut;
    }
    
    public String rearrangeDate(String dateIn){
        
        String dateStr = dateIn.substring(0,dateIn.indexOf(" "));
        String timeStr = dateIn.substring(dateIn.indexOf(" ")+1,dateIn.length());
                
        dateStr = dateStr.trim();
        int slashIndex = dateStr.indexOf("-");
        int slashIndex2 = dateStr.lastIndexOf("-");
        String yearStr = dateStr.substring(0,slashIndex);
        String monthStr = dateStr.substring(slashIndex+1,slashIndex2);
        String dayStr = dateStr.substring(slashIndex2+1,slashIndex2+3);
        
        String dateOut = dayStr + "/" + monthStr + "/" + yearStr + " " + timeStr;
        return dateOut;
    }
    
    public String reformatTime(String timeIn){        
        String timeOut = "";
        if(timeIn.lastIndexOf(":") != -1){
            timeOut = timeIn.substring(0,timeIn.lastIndexOf(":"));
        }
        return timeOut;
    }    
}
