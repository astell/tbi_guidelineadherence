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
import java.util.TreeMap;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;
import java.util.Date;

public class AssociationData {
    
    private Logger logger;
    private String patientID;
    private String centreID;
    private Vector<TimeData> timeDataList; //List of times to associated treatments
    private Vector<TreeMap> windowSizeCount;        
    private final int WINDOW_DEFN_NUMBER = 4;
    private Vector<Treatment> totalTreatments;
    private TreeMap eventLists;
    private Vector<TreatmentCount> treatmentLists; //List of treatment distributions
    
    public AssociationData(Logger _logger, String _patientID, String _centreID){
        logger = _logger;
        patientID = _patientID;
        centreID = _centreID;        
        windowSizeCount = new Vector<TreeMap>();
        timeDataList = new Vector<TimeData>();
        treatmentLists = new Vector<TreatmentCount>();
        for(int i=0; i< WINDOW_DEFN_NUMBER; i++){
            windowSizeCount.add(new TreeMap());            
            timeDataList.add(new TimeData(logger));
            treatmentLists.add(new TreatmentCount(logger));
        }        
        totalTreatments = new Vector<Treatment>();
        eventLists = new TreeMap();        
    }
    
    //paramName is the key; count is the value; defnIndex is the window definition index
    public void setParamCount(String paramName, int count, int defnIndex){
        windowSizeCount.get(defnIndex).put(paramName,count);
    }
    
    public void setTotalTreatments(Vector<Treatment> _totalTreatments){
        totalTreatments = _totalTreatments;
    }
    
    public void setEventList(Vector<Event> _events, String eventListKey){
        eventLists.put(eventListKey,_events);
    }
    
    public Vector<Treatment> getTotalTreatments(){
        return totalTreatments;
    }
    
    public TreeMap getEventLists(){
        return eventLists;
    }
    
    public TreeMap getWindowSizeCount(int defnIndex){
        return windowSizeCount.get(defnIndex);    
    }
    
    public void setTreatmentList(String treatmentName, String defnName, int outerIndex){        
        treatmentLists.get(outerIndex).setTreatmentLists(treatmentName, defnName);
    }
    
    public void setTimeList(Date timeToTreatment, String defnName, int outerIndex){        
        timeDataList.get(outerIndex).setTimeList(timeToTreatment, defnName);
    }
        
    public Vector<TreatmentCount> getTreatmentLists(){
        return treatmentLists;
    }
    
    public Vector<TimeData> getTimeList(){
        return timeDataList;
    }
    
    public void setCentreID(String _centreID){
        centreID = _centreID;
    }
    
    public String getCentreID(){
        return centreID;
    }
    
    public void setPatientID(String _patientID){
        patientID = _patientID;
    }
    
    public String getPatientID(){
        return patientID;
    }
    
    
}
