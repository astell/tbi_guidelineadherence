/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

import java.io.BufferedReader;
import java.io.FileReader;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author astell
 */
public class CentreInfo {

    private Logger logger;

    public CentreInfo(Logger _logger) {
        logger = _logger;
    }

    public TreeMap compilePatientCentreList(Vector<String> patientIDs) {

        //Use the patient ID as the key
        TreeMap patientCentres = new TreeMap();

        int idNum = patientIDs.size();

        for (int i = 0; i < idNum; i++) {
            String patientID = patientIDs.get(i);
            //Read through all the demographic information files

            //Now write the treatment breakdown to the file
            FileReader fr = null;
            BufferedReader br = null;

            String filepath = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\BrainIT_Demographics\\output\\";
            String filename = filepath + patientID + "_demog.txt";

            try {

                fr = new FileReader(filename);
                br = new BufferedReader(fr);

                //Read the first two lines
                br.readLine(); //Line contains the patientID
                String centreLine = br.readLine(); //Line contains the centre ID

                String centreID = "";
                int colonIndex = centreLine.indexOf(":");
                if (colonIndex != -1) {
                    centreID = centreLine.substring(colonIndex + 1, centreLine.length()).trim();
                }
                patientCentres.put(patientID, (String) centreID);

                br.close();
                fr.close();
            } catch (Exception e) {
                logger.info("I/O error: " + e.getMessage());
            }
        }

        return patientCentres;
    }
    
    public Vector<String> getCentreIDs(TreeMap patientCentres){
        
        Vector<String> centreIDs = new Vector<String>();
        
        Set patientCentreSet = patientCentres.keySet(); //patient ID set
        Iterator patientCentreIter = patientCentreSet.iterator();
        while (patientCentreIter.hasNext()) {
            String patientID = (String) patientCentreIter.next();
            String centreID = "" + (String) patientCentres.get(patientID);
            if(centreID == null){
                centreID = "";
            }
            centreID = centreID.trim();            
            if(!centreIDs.contains(centreID)){
                centreIDs.add(centreID);
            }            
        }
        return centreIDs;        
    }
}
