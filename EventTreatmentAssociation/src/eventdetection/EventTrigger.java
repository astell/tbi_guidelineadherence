/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventdetection;

/**
 *
 * @author astell
 */
public class EventTrigger {
    
    private float percentageChange;
    private boolean increase;
    
    public EventTrigger(float _percentageChange, boolean _increase){
        percentageChange = _percentageChange;
        increase = _increase;
    }
    
    public void setPercentageChange(float _percentageChange){
        percentageChange = _percentageChange;
    }
    
    public float getPercentageChange(){
        return percentageChange;
    }
        
    public void setIncrease(boolean _increase){
        increase = _increase;
    }
    
    public boolean getIncrease(){
        return increase;
    }
    
}
