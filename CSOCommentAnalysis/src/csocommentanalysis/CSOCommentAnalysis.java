package csocommentanalysis;

/**
 *
 * @author astell
 */
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.Vector;

public class CSOCommentAnalysis {

    public static void main(String[] args) {

        //Read in the LIVE and NON-LIVE commentaries, list the relevant ABP=0 events, and write to output txt file
        EventAnalysis ea = new EventAnalysis();
        //ea.analyse(); //Output is CSO_patient_events_out.txt in C:\
        
        //Read in the corresponding wave-form files - process into digest files
        //ea.waveFormAnalysis(); //Output is Condensed_output_run2/ in E:\
                
        //Print out the timestamps of the individual zero-drops (from raw data, not averaged)
        ea.detectZeroDrops(); //Output is zero_drop_avg_output_run2/ in E:\
        
        //Compare the event list with the zero drops and calculate the average distances
        //ea.compareEventsAndZeroDrops(); //Output is zero_drop_distances_run2.txt in C:\
        
    }
    
    //Combine the information about the selected LIVE and NON-LIVE events against the ICCA output
    //ea.iccaCombo();
        
}
