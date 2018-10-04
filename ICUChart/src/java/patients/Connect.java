/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package patients;

import java.sql.*;

/**
 *
 * @author astell
 */
public class Connect {
    
    private Connection conn = null;
    
    public Connect(){        
    }
    
    public Connection connect(String dbName, String driverName, String serverName, String port, String username, String password){
                
        String connectionURL = "jdbc:mysql://" + serverName + ":" + port + "/" + dbName;
        try {
            Class.forName(driverName).newInstance();
            conn = DriverManager.getConnection(connectionURL,username,password);
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return conn;        
    }
    
    public Connection getConnection(){
        return conn;
    }    
}
