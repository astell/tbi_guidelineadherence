/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brainit_treatmentanalysis;

/**
 *
 * @author astell
 */
import java.util.Vector;
import org.apache.log4j.Logger;

public class SortTreatments {

    private Treatment[] treatmentsOut;
    Logger logger = null;
    
    public SortTreatments(Logger _logger){
        logger = _logger;        
    }
    
    public Vector<Treatment> sort(Vector<Treatment> treatments){
        
        int treatmentNum = treatments.size();
        treatmentsOut = new Treatment[treatmentNum];        
        
        //Show the unsorted treatment vector
        //logger.info("Unsorted treatment vector\n====\n");
        for(int i=0; i<treatmentNum; i++){
            treatmentsOut[i] = treatments.get(i);
            //logger.info("" + treatments.get(i).getTimestamp() + ": " + treatments.get(i).getDescription() + "," + treatments.get(i).getTarget() + "," + treatments.get(i).getValue() + "\n");
        }        
        //logger.info("====\n");
        
        this.sortTreatmentOutput(treatmentsOut, 0, treatmentNum);
        
        //Show the unsorted treatment vector
        //logger.info("Sorted treatment vector\n====\n");
        Vector<Treatment> sortedTreatments = new Vector<Treatment>();
        for(int i=0; i<treatmentNum; i++){            
            //logger.info("" + treatmentsOut[i].getTimestamp() + ": " + treatmentsOut[i].getDescription() + "," + treatmentsOut[i].getTarget() + "," + treatmentsOut[i].getValue() + "\n");
            sortedTreatments.add(treatmentsOut[i]);            
        }        
        //logger.info("====\n");
        
        return sortedTreatments;
    }
    
    public void sortTreatmentOutput(Treatment array[],int low, int n) {

        int lo = low;
        int hi = n-1;
        if (lo >= n) {
            return;
        }
        Treatment mid = array[(lo + hi) / 2];
        while (lo < hi) {
            while (lo < hi && (array[lo].compareTo(mid) == -1)) {
                lo++;
            }
            while (lo < hi && (array[hi].compareTo(mid) == 1)) {
                hi--;
            }
            if (lo < hi) {
                Treatment T = array[lo];
                array[lo] = array[hi];
                array[hi] = T;
            }
            lo++;
        }
        if (hi < lo) {
            int T = hi;
            hi = lo;
            lo = T;
        }
        sortTreatmentOutput(array, low, lo);
        sortTreatmentOutput(array, lo == low ? lo + 1 : lo, n);
    }
    
    
}
