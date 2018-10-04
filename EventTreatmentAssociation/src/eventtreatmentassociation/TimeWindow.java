/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

/**
 *
 * @author astell
 */
import java.util.Date;
import java.util.Vector;

public class TimeWindow {
    
    private Date start,end,midTime;
    final private boolean TIME_WINDOW_SYMMETRIC;
    private Vector<Event> associatedEvents;
    private Vector<Treatment> associatedTreatments;
    
    public TimeWindow(Date _start, Date _end, Date _midTime, boolean symmetric){
        TIME_WINDOW_SYMMETRIC = symmetric;
        start = _start;
        end = _end;
        midTime = _midTime;
    }
    
    public Date getStart(){
        return start;
    }
    
    public void setStart(Date _start){
        start = _start;
    }
    
    public Date getEnd(){
        return end;
    }
    
    public void setEnd(Date _end){
        end = _end;
    }
    
    public Date getMidTime(){
        return midTime;
    }
    
    public void setMidTime(Date _midTime){
        midTime = _midTime;
    }
    
    public Vector<Event> getAssociatedEvents(){
        return associatedEvents;
    }
    
    public void setAssociatedEvents(Vector<Event> _associatedEvents){
        associatedEvents = _associatedEvents;
    }
    
    public Vector<Treatment> getAssociatedTreatments(){
        return associatedTreatments;
    }
    
    public void setAssociatedTreatments(Vector<Treatment> _associatedTreatments){
        associatedTreatments = _associatedTreatments;
    }
}
