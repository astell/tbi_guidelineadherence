/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brainit_treatmentanalysis;

/**
 *
 * @author astell
 */
public class Physiological {
    
    private String[] values;
    private String[] parameters;
    private java.util.Date timestamp;
    
    public Physiological(String[] _values, String[] _parameters, java.util.Date _timestamp){
        values = _values;
        parameters = _parameters;
        timestamp = _timestamp;
    }
    
    public String[] getValues(){
        return values;
    }
    
    public void setValues(String[] _values){
        values = _values;
    }
    
    public String[] getParameters(){
        return parameters;
    }
    
    public void setParameters(String[] _parameters){
        parameters = _parameters;
    }
    
    public String getValue(int index){
        return values[index];
    }
    
    public void setValue(String _value, int index){
        values[index] = _value;
    }

    public String getParameter(int index){
        return parameters[index];
    }
    
    public void setParameter(String _parameter, int index){
        parameters[index] = _parameter;
    }
    
    public java.util.Date getTimestamp(){
        return timestamp;
    }
    
    public void setTimestamp(java.util.Date _timestamp){
        timestamp = _timestamp;
    }
    
    
}
