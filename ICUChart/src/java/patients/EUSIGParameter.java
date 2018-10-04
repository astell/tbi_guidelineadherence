/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package patients;

/**
 *
 * @author astell
 */
public class EUSIGParameter {
    
    private String name, parameter, unit;
    private float gradeThreshold;
    private boolean comparatorGreater;
    
    public EUSIGParameter(String _name, String _parameter, String _unit, float _gradeThreshold, boolean _comparatorGreater){
        name = _name;
        parameter = _parameter;
        unit = _unit;
        gradeThreshold = _gradeThreshold;
        comparatorGreater = _comparatorGreater;
    }
    
    public void setName(String _name){
        name = _name;
    }
    
    public String getName(){
        return name;
    }
    
    public void setParameter(String _parameter){
        parameter = _parameter;
    }
    
    public String getParameter(){
        return parameter;
    }
    
    public void setUnit(String _unit){
        unit = _unit;
    }
    
    public String getUnit(){
        return unit;
    }
    
    public void setGradeThreshold(float _gradeThreshold){
        gradeThreshold = _gradeThreshold;        
    }
    
    public float getGradeThreshold(){
        return gradeThreshold;
    }
    
    public boolean getComparatorGreater(){
        return comparatorGreater;
    }
    
    public void setComparatorGreater(boolean _comparatorGreater){
        comparatorGreater = _comparatorGreater;
    }
}
