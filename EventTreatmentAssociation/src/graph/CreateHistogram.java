/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

/**
 *
 * @author astell
 */
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import java.util.TreeMap;

public class CreateHistogram {

    private Logger logger;

    public CreateHistogram(Logger _logger) {
        logger = _logger;
    }

    public CreateHistogram() {
    }

    public void create(String filepath, TreeMap totalEventNums) {

        Set totalEventSet = totalEventNums.keySet();
        Iterator totalEventIter = totalEventSet.iterator();
        int binNum = totalEventSet.size();
        logger.info("binNum: " + binNum);
        double[] value = new double[binNum];
        int catIndex = 0;
        while (totalEventIter.hasNext()) {
            String totalEventName = (String) totalEventIter.next();
            Integer totalEventNum = (Integer) totalEventNums.get(totalEventName);

            value[catIndex] = (double) totalEventNum.intValue(); //WRONG USE OF catIndex..?
            catIndex++;
        }
        
        for(int i=0; i < binNum; i++){
            logger.info("value[" + i + "]: " + value[i]);
        }
        logger.info("------");

        int number = binNum;
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.RELATIVE_FREQUENCY);
        dataset.addSeries("Histogram", value, number);
        String plotTitle = "Histogram";
        String xaxis = "Parameter definition";
        String yaxis = "Total number of events";
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean show = false;
        boolean toolTips = false;
        boolean urls = false;
        JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxis, yaxis,
                dataset, orientation, show, toolTips, urls);
        int width = 1000;
        int height = 600;
        String filename = filepath + "histogram.png";
        try {
            ChartUtilities.saveChartAsPNG(new File(filename), chart, width, height);
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
        }
    }
}
