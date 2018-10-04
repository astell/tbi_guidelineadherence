/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author astell
 */
public class ChartIO {
    
    public ChartIO(){
        
    }
    
    public void saveChartToFile(PhysioCharts demo, String patientID){
        String filepath = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\CSO\\snapshots\\";
        String filename = filepath + "" + patientID + ".jpg";
         try {            
            BufferedImage image = new BufferedImage(demo.getWidth(), demo.getHeight(),BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = image.createGraphics();
            demo.paint(graphics2D);            
            ImageIO.write(image, "jpg", new File(filename));
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
}
