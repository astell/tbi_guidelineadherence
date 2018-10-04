/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package connection;

/**
 *
 * @author anthony
 */
import javax.servlet.ServletContext;
import java.sql.*;
import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//import utility.Database;

public class ConnectBean {

    private static final Logger logger = Logger.getLogger(ConnectBean.class);
    private Connection conn, secConn, paramConn, ccConn;
    private DataSource ds, secds, paramds, ccds;

    public ConnectBean() {        
    }
    
    public void setConnection(ServletContext context) {

        String dbParamName = "";
        dbParamName = "db_name_prod";
        String dbName = context.getInitParameter(dbParamName);
        String driverName = context.getInitParameter("driver_name");
        String serverName = context.getInitParameter("server_name");
        String port = context.getInitParameter("port");
        String username = context.getInitParameter("username");
        String password = context.getInitParameter("password");
        
        String connectionURL = "jdbc:mysql://" + serverName + ":" + port + "/" + dbName;
        logger.debug("connectionURL: " + connectionURL);
        try {
            Class.forName(driverName).newInstance();
            conn = DriverManager.getConnection(connectionURL,username,password);
            
            //DataSource ds = Database.connectMain();
            //conn = ds.getConnection();
        } catch (Exception e) {
            logger.debug("Database connection error: " + e.getMessage());
        }
    }
    
    public void setConnections(ServletContext context, Connection _conn, Connection _secConn, Connection _paramConn, Connection _ccConn) {

        String versionParam = context.getInitParameter("version");
        String dbNameStr = "";
        String dbSecNameStr = "";
        String dbParamNameStr = "";
        String dbCcNameStr = "";
        if (versionParam.equals("test")) {
            dbNameStr = "db_name_test";
            dbSecNameStr = "security_db_name_test";
            dbParamNameStr = "parameter_db_name";
            dbCcNameStr = "center_callout_db_name";
        } else {
            dbNameStr = "db_name_prod";
            dbSecNameStr = "security_db_name_prod";
            dbParamNameStr = "parameter_db_name";
            dbCcNameStr = "center_callout_db_name";
        }
        //Assign the database names
        String dbName = context.getInitParameter(dbNameStr);
        String dbSecName = context.getInitParameter(dbSecNameStr);
        String dbParamName = context.getInitParameter(dbParamNameStr);
        String dbCcName = context.getInitParameter(dbCcNameStr);
        
        //Assign the context names
        String driverName = context.getInitParameter("driver_name");
        String serverName = context.getInitParameter("server_name");
        String port = context.getInitParameter("port");
        String ensatServerName = context.getInitParameter("ensat_server_name");
        String ensatPort = context.getInitParameter("ensat_port");        
        String username = context.getInitParameter("username");
        String password = context.getInitParameter("password");
        String ensatUsername = context.getInitParameter("ensat_username");
        String ensatPassword = context.getInitParameter("ensat_password");


        //Set up the different connection strings
        String connectionURL = "adbc:mysql://" + serverName + ":" + port + "/" + dbName;
        String connectionSecURL = "adbc:mysql://" + ensatServerName + ":" + ensatPort + "/" + dbSecName;
        String connectionParamURL = "adbc:mysql://" + serverName + ":" + port + "/" + dbParamName;
        String connectionCcURL = "adbc:mysql://" + ensatServerName + ":" + ensatPort + "/" + dbCcName;        
        
        //Translate into connection objects
        try {
            /*Class.forName(driverName).newInstance();
            conn = DriverManager.getConnection(connectionURL,username,password);            
            secConn = DriverManager.getConnection(connectionSecURL,username,password);            
            paramConn = DriverManager.getConnection(connectionParamURL,username,password);            
            ccConn = DriverManager.getConnection(connectionCcURL,username,password);*/
            
            logger.debug("connectionURL: " + connectionURL);
            logger.debug("connectionSecURL: " + connectionSecURL);
            logger.debug("connectionParamURL: " + connectionParamURL);
            logger.debug("connectionCcURL: " + connectionCcURL);
            
            
            //ds = Database.connectMain();
            //ds = this.getDataSource(connectionURL, driverName, username, password);
            logger.debug("ds created...");
            //secds = Database.connectSecurity();
            //secds = this.getDataSource(connectionSecURL, driverName, ensatUsername, ensatPassword);
            logger.debug("secds created...");
            //paramds = Database.connectParameter();
            //paramds = this.getDataSource(connectionParamURL, driverName, username, password);
            logger.debug("paramds created...");
            //ccds = Database.connectCenterCallout();
            //ccds = this.getDataSource(connectionCcURL, driverName, ensatUsername, ensatPassword);
            logger.debug("ccds created...");
            
            if(_conn == null){
                conn = ds.getConnection();
                logger.debug("Main database connection assigned from DataSource pool...");
            }else{
                conn = _conn;
            }
            
            if(_secConn == null){
                secConn = secds.getConnection();
                logger.debug("Security database connection assigned from DataSource pool...");
            }else{
                secConn = _secConn;
            }
            
            if(_paramConn == null){
                paramConn = paramds.getConnection();
                logger.debug("Parameter database connection assigned from DataSource pool...");
            }else{
                paramConn = _paramConn;
            }
            
            if(_ccConn == null){
                ccConn = ccds.getConnection();
                logger.debug("Center callout database connection assigned from DataSource pool...");
            }else{
                ccConn = _ccConn;
            }
            
            
            
        } catch (Exception e) {
            logger.debug("Database connection error: " + e.getMessage());
        }
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public Connection getSecConnection() {
        return secConn;
    }
    
    public Connection getParamConnection() {
        return paramConn;
    }
    
    public Connection getCcConnection() {
        return ccConn;
    }
    
    public DataSource getDs() {
        return ds;
    }
    
    public DataSource getSecDs() {
        return secds;
    }
    
    public DataSource getParamDs() {
        return paramds;
    }
    
    public DataSource getCcDs() {
        return ccds;
    }
}
