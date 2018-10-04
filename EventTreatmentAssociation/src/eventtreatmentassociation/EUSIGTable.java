/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

/**
 *
 * @author astell
 */
import java.util.Vector;

public class EUSIGTable {
    
    private Vector<EUSIGParameter> eusigParameters;
    
    public EUSIGTable(Vector<EUSIGParameter> _eusigParameters){
        eusigParameters = _eusigParameters;
    }
    
    public void setEUSIGParameters(Vector<EUSIGParameter> _eusigParameters){
        eusigParameters = _eusigParameters;
    }
    
    public Vector<EUSIGParameter> getEUSIGParameters(){
        return eusigParameters;
    }
    
    
}
