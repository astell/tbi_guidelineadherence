/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import org.jfree.chart.*;
import org.jfree.data.category.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.*;
import org.jfree.data.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.ChartUtilities;
import org.jfree.data.general.PieDataset;

import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import java.util.TreeMap;
import java.util.Vector;

/**
 *
 * @author astell
 */
public class CreatePieChart {

    private Logger logger;

    public CreatePieChart(Logger _logger) {
        logger = _logger;
    }

    public void createTreatmentChart(String filepath, TreeMap currentListIn, TreeMap currentNumIn, String chartTitle) {

        Set totalAssocEventSet = currentNumIn.keySet();
        Iterator totalAssocEventIter = totalAssocEventSet.iterator();
        int fileIndex = 0;
        while (totalAssocEventIter.hasNext()) {

            //Get total number of treatments for each definition
            int totalTreatmentChartNum = 0;            
            String totalAssocEventName = (String) totalAssocEventIter.next();
            TreeMap totalAssocTreatmentList = (TreeMap) currentListIn.get(totalAssocEventName);
            if (totalAssocTreatmentList != null && (totalAssocTreatmentList.size() != 0)) {
                Set totalAssocTreatmentListSet = totalAssocTreatmentList.keySet();
                Iterator totalAssocTreatmentListIter = totalAssocTreatmentListSet.iterator();

                //Re-assign the iterator and use it again for the full distribution output
                totalAssocTreatmentListIter = totalAssocTreatmentListSet.iterator();
                while (totalAssocTreatmentListIter.hasNext()) {
                    String treatmentName = (String) totalAssocTreatmentListIter.next();
                    Integer treatmentNum = (Integer) totalAssocTreatmentList.get(treatmentName);
                    totalTreatmentChartNum += treatmentNum;
                }
            }                
            
            //Write one chart per definition
            DefaultPieDataset dataset = new DefaultPieDataset();            
            if (totalAssocTreatmentList != null && (totalAssocTreatmentList.size() != 0)) {
                Set totalAssocTreatmentListSet = totalAssocTreatmentList.keySet();
                Iterator totalAssocTreatmentListIter = totalAssocTreatmentListSet.iterator();

                //Re-assign the iterator and use it again for the full distribution output
                totalAssocTreatmentListIter = totalAssocTreatmentListSet.iterator();
                while (totalAssocTreatmentListIter.hasNext()) {
                    String treatmentName = (String) totalAssocTreatmentListIter.next();
                    Integer treatmentNum = (Integer) totalAssocTreatmentList.get(treatmentName);
                    String treatmentPercent = this.writePercentage(treatmentNum, new Integer(totalTreatmentChartNum));

                    String treatmentLabel = treatmentName + " (" + treatmentPercent + ")";
                    dataset.setValue(treatmentLabel,new Double(treatmentNum));
                }
            }
            
            JFreeChart chart = ChartFactory.createPieChart(chartTitle, dataset, true, true, true);
            int width = 1000;
            int height = 600;
            String filename = filepath + "treatment_pie_chart_" + totalAssocEventName + ".png";
            try {
                ChartUtilities.saveChartAsPNG(new File(filename), chart, width, height);
            } catch (Exception e) {
                logger.info("Exception: " + e.getMessage());
            }                       
            fileIndex++;
        }
    }
    
    private float calcPercentage(float numerator, float denominator) {
        return (numerator / denominator) * 100;
    }

    private String writePercentage(Integer numerator, Integer denominator) {
        float numeratorFloat = (float) numerator;
        float denominatorFloat = (float) denominator;
        float percent = this.calcPercentage(numeratorFloat, denominatorFloat);

        //Round to one decimal point
        String percentOutStr = String.format("%.1f", percent);

        return percentOutStr + "%";
    }
}
