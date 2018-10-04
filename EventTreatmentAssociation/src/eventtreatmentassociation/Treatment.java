/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

/**
 *
 * @author astell
 */
public class Treatment {
    
    private String value;
    private String description;
    private String target;
    private java.util.Date timestamp;
    
    public Treatment(String _value, String _description, String _target, java.util.Date _timestamp){
        value = _value;
        description = _description;
        target = _target;
        timestamp = _timestamp;
    }
    
    public String getValue(){
        return value;
    }
    
    public void setValue(String _value){
        value = _value;
    }

    public String getDescription(){
        return description;
    }
    
    public void setDescription(String _description){
        description = _description;
    }
    
    
    public String getTarget(){
        return target;
    }
    
    public void setTarget(String _target){
        target = _target;
    }
    
    public java.util.Date getTimestamp(){
        return timestamp;
    }
    
    public void setTimestamp(java.util.Date _timestamp){
        timestamp = _timestamp;
    }
    
    
}
