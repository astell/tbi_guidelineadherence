/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eventtreatmentassociation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import java.util.Vector;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

/**
 *
 * @author astell
 */
public class BrainITDriver {

    SimpleDateFormat df = null;
    Logger logger = null;

    public BrainITDriver(Logger _logger) {
        logger = _logger;
        df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    public Connection getConnection(String dbName) {

        Connection conn = null;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
            String filename = "C:\\Documents and Settings\\astell\\My Documents\\PhD\\PhD\\Data\\BrainIT_2011\\" + dbName;
            String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
            database += filename.trim() + ";DriverID=22;READONLY=true}";
            conn = DriverManager.getConnection(database, "", "");
            logger.info("Successful connection to BrainIT database...");
        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
        return conn;
    }

    public Vector<String> getPatientList(Connection conn) {

        Vector<String> patients = new Vector<String>();
        try {
            String sql = "SELECT Patient_Id FROM Demographic;";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                patients.add("" + rs.getString(1));
            }

        } catch (Exception e) {
            logger.info("Error: " + e.getMessage());
        }
        return patients;
    }

    /* Commenting out this original table definition (raised ICP and lowered CPP only) */
    /*public EUSIGTable compileEUSIGTable(){
        
        EUSIGParameter icp = new EUSIGParameter("Raised ICP","ICPm","mmHg",20,30,40,true);
        //EUSIGParameter hypo = new EUSIGParameter("Hypotension","BPs","mmHg",90,70,50,false); //NOTE: should use BPm as well
        //EUSIGParameter hyper = new EUSIGParameter("Hypertension","BPs","mmHg",160,190,220,true); //NOTE: should use BPm as well
        EUSIGParameter cpp = new EUSIGParameter("Lowered CPP","CPP","mmHg",60,50,40,false);
        //EUSIGParameter hypox = new EUSIGParameter("Hypoxemia","SaO2","%",90,85,80,false);
        //EUSIGParameter pyrex = new EUSIGParameter("Pyrexia","TC","C",38,39,40,true); //NOTE: has a hold-down of 60 mins
        //EUSIGParameter tachy = new EUSIGParameter("Tachycardia","HRT","bpm",120,135,150,true);
        //EUSIGParameter brady = new EUSIGParameter("Bradycardia","HRT","bpm",50,40,30,false);
        
        Vector<EUSIGParameter> eusigParams = new Vector<EUSIGParameter>();
        eusigParams.add(icp);
        //eusigParams.add(hypo);
        //eusigParams.add(hyper);
        eusigParams.add(cpp);
        //eusigParams.add(hypox);
        //eusigParams.add(pyrex);
        //eusigParams.add(tachy);
        //eusigParams.add(brady);
        
        EUSIGTable eusig = new EUSIGTable(eusigParams);
        
        return eusig;
    }*/
    
public EUSIGTable compileEUSIGTable(){
        
        //New definition changes require five definitions of grade 1 ICP threshold (ignores the grade2 and grade3 definitions (hence the -1))
    
        EUSIGParameter icp1 = new EUSIGParameter("Raised ICP #1","ICPm","mmHg",10,true);
        EUSIGParameter icp2 = new EUSIGParameter("Raised ICP #2","ICPm","mmHg",15,true);
        EUSIGParameter icp3 = new EUSIGParameter("Raised ICP #3","ICPm","mmHg",20,true);
        EUSIGParameter icp4 = new EUSIGParameter("Raised ICP #4","ICPm","mmHg",25,true);
        EUSIGParameter icp5 = new EUSIGParameter("Raised ICP #5","ICPm","mmHg",30,true);
        
        EUSIGParameter cpp1 = new EUSIGParameter("Lowered CPP #1","CPP","mmHg",50,false);
        EUSIGParameter cpp2 = new EUSIGParameter("Lowered CPP #2","CPP","mmHg",60,false);
        EUSIGParameter cpp3 = new EUSIGParameter("Lowered CPP #3","CPP","mmHg",70,false);
        
        Vector<EUSIGParameter> eusigParams = new Vector<EUSIGParameter>();
        eusigParams.add(icp1);
        eusigParams.add(icp2);
        eusigParams.add(icp3);
        eusigParams.add(icp4);
        eusigParams.add(icp5);
        
        eusigParams.add(cpp1);
        eusigParams.add(cpp2);
        eusigParams.add(cpp3);
        
        EUSIGTable eusig = new EUSIGTable(eusigParams);
        
        return eusig;
    }

}
