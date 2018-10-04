/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author astell
 */
public class RunCharts {
    
    public static void main(final String[] args) {
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        //Put all these into a properties file

        //Set up parameter selection arguments here
        //String[] paramsToView = {"BPm","BPs","BPd","ICPm","CPP"};
        String[] paramsToView = {"ICPm"};
        //String[] paramsToView = {"TC"};
        //String[] paramsToView = {"SaO2","SaO2pls"};
        //String[] paramsToView = {"HRT"};
        //String[] paramsToView = {"RR"};

        //Pass in the patient ID here too
        String patientID = "38351484";

        //Pass in the times that you want to chart (need to have a default setting somewhere in the code
        String chartStartStr = "2003-09-25 06:00:00";
        String chartEndStr = "2003-09-25 09:00:00";
        /*String chartStartStr = "";
        String chartEndStr = "";*/
        
        Date chartStart = null;
        Date chartEnd = null;
        try{
            chartStart = df.parse(chartStartStr);
            chartEnd = df.parse(chartEndStr);
        }catch(Exception e){
            System.out.println("Parsing error: " + e.getMessage());
        }

        final PhysioCharts demo = new PhysioCharts("Physiological Output", paramsToView, patientID, chartStart, chartEnd);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
        
        //Now want a utility here to save the jframe to an image file
        ChartIO chartio = new ChartIO();
        chartio.saveChartToFile(demo,patientID);
        
    }
    
}
