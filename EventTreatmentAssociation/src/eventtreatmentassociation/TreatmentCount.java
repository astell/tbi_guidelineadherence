/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

import org.apache.log4j.Logger;
import java.util.Vector;
import java.util.TreeMap;

import java.util.Set;
import java.util.Iterator;

/**
 *
 * @author astell
 */
public class TreatmentCount {
    
    private Logger logger;
    private TreeMap treatmentLists;
    
    public TreatmentCount(Logger _logger){
        logger = _logger;
        treatmentLists = new TreeMap();
    }
    
    public void setTreatmentLists(String treatmentName, String defnName){                
        
        /*if(defnName.equals("Lowered CPP #1_05")){            
            logger.info("treatmentName: " + treatmentName);
        }*/
        
        if(treatmentLists.containsKey(defnName)){
            //logger.info("defnName: " + defnName);
            //logger.info("DEFINITION " + defnName + " EXISTS");
            TreeMap treatmentListIn = (TreeMap) treatmentLists.get(defnName);
            if(treatmentListIn.containsKey(treatmentName)){
                //logger.info("DEFINITION " + defnName + " CONTAINS " + treatmentName);
                //logger.info("treatmentName 1: " + treatmentName);
                Integer treatmentCountIn = (Integer) treatmentListIn.get(treatmentName);
                //logger.info("DEFINITION " + defnName + " CONTAINS " + treatmentName + " WITH A COUNT OF " + treatmentCountIn);
                treatmentCountIn++;
                treatmentListIn.put(treatmentName, treatmentCountIn);
            }else{
                //logger.info("DEFINITION " + defnName + " DOES NOT CONTAIN " + treatmentName);
                //logger.info("treatmentName 2: " + treatmentName);
                treatmentListIn.put(treatmentName, new Integer(1));
            }
            treatmentLists.put(defnName, treatmentListIn);            
        }else{
            //logger.info("DEFINITION " + defnName + " DOES NOT EXIST");
            //logger.info("treatmentName 3: " + treatmentName);
            TreeMap treatmentList = new TreeMap();
            treatmentList.put(treatmentName, new Integer(1));
            treatmentLists.put(defnName, treatmentList);        
        }
    }
    
    public TreeMap getTreatmentLists(){
        return treatmentLists;
    }
    
    
    
    
}
