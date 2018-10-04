/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventdetection;

/**
 *
 * @author astell
 */
import java.util.Vector;

public class Event {
    
    private int eventIndex;
    private java.util.Date start, end;
    private long eventHolddown, clearHolddown;
    private float thresholdValue;
    private Vector<String> values;
    private String parameterFeed;
    private EventTrigger trigger;
    
    public Event(int _eventIndex, String _parameterFeed){
        eventIndex = _eventIndex;
        parameterFeed = _parameterFeed;
    }
    
    public void setEventIndex(int _eventIndex){
        eventIndex = _eventIndex;
    }
    
    public int getEventIndex(){
        return eventIndex;
    }
    
    public void setParameterFeed(String _parameterFeed){
        parameterFeed = _parameterFeed;
    }
    
    public String getParameterFeed(){
        return parameterFeed;
    }
    
    public void setStart(java.util.Date _start){
        start = _start;
    }
    
    public java.util.Date getStart(){
        return start;
    }
    
    public void setEnd(java.util.Date _end){
        end = _end;
    }
    
    public java.util.Date getEnd(){
        return end;
    }
    
    public void setEventHolddown(long _eventHolddown){
        eventHolddown = _eventHolddown;
    }
    
    public long getEventHolddown(){
        return eventHolddown;
    }
        
    public void setClearHolddown(long _clearHolddown){
        clearHolddown = _clearHolddown;
    }
    
    public long getClearHolddown(){
        return clearHolddown;
    }    
    
    public void setThresholdValue(float _thresholdValue){
        thresholdValue = _thresholdValue;
    }
    
    public float getThresholdValue(){
        return thresholdValue;
    }    
    
    public void setValues(Vector<String> _values){
        values = _values;
    }
    
    public Vector<String> getValues(){
        return values;
    }
    
}
