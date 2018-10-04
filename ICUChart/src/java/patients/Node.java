/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patients;

/**
 *
 * @author astell
 */
public class Node {
    
    private String label, value;
    
    public Node(String _label, String _value){
        this.label = _label;
        this.value = _value;
    }
    
    public String getLabel(){
        return label;
    }

    public String getValue(){
        return value;
    }    
}
