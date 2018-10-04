/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package patients;

/**
 *
 * @author astell
 */
import java.util.Vector;

public class GuidelineDistance {
    
    private int guidelineDistanceIndex;
    private java.util.Date start;
    private Vector<Vector> valueLists;
    private String parameterFeed;    
    
    public GuidelineDistance(int _guidelineDistanceIndex, String _parameterFeed){
        guidelineDistanceIndex = _guidelineDistanceIndex;
        parameterFeed = _parameterFeed;
    }
    
    public void setGuidelineDistanceIndex(int _guidelineDistanceIndex){
        guidelineDistanceIndex = _guidelineDistanceIndex;
    }
    
    public int getGuidelineDistanceIndex(){
        return guidelineDistanceIndex;
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
    
    public void setValueLists(Vector<Vector> _valueLists){
        valueLists = _valueLists;
    }
    
    public Vector<Vector> getValueLists(){
        return valueLists;
    }
    
}
