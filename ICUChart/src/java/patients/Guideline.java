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

public class Guideline {
    
    private int guidelineIndex;
    private java.util.Date start;
    private Vector<Vector> values;
    private String parameterFeed;    
    
    public Guideline(int _guidelineIndex, String _parameterFeed){
        guidelineIndex = _guidelineIndex;
        parameterFeed = _parameterFeed;
    }
    
    public void setGuidelineIndex(int _guidelineIndex){
        guidelineIndex = _guidelineIndex;
    }
    
    public int getGuidelineIndex(){
        return guidelineIndex;
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
    
    public void setValues(Vector<Vector> _values){
        values = _values;
    }
    
    public Vector<Vector> getValues(){
        return values;
    }
    
}
