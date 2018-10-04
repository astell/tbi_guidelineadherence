/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

/**
 *
 * @author astell
 */
import org.apache.log4j.Logger;
import java.util.Vector;
import java.util.Date;
import java.util.TreeMap;

public class TimeData {
    
    private Logger logger;
    private TreeMap timeList;
    
    public TimeData(Logger _logger){
        logger = _logger;
        timeList = new TreeMap();
    }
    
    public void setTimeList(Date timeToTreatment, String defnName){                
        
        if(timeList.containsKey(defnName)){
            Vector<Date> timeListIn = (Vector<Date>) timeList.get(defnName);
            timeListIn.add(timeToTreatment);
            timeList.put(defnName, timeListIn);
        }else{
            Vector<Date> timeListIn = new Vector<Date>();
            timeListIn.add(timeToTreatment);
            timeList.put(defnName, timeListIn);        
        }
    }
    
    public TreeMap getTimeList(){
        return timeList;
    }    
    
}
