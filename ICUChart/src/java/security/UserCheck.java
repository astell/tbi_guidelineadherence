/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

/**
 *
 * @author astell
 */
import java.sql.*;
import java.text.Format;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpSession;

import user.UserBean;

import java.sql.PreparedStatement;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.sql.DataSource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
//import utility.Database;

import org.apache.commons.dbcp2.BasicDataSource;

public class UserCheck {
    
    private static final Logger logger = Logger.getLogger(UserCheck.class);

    public UserCheck() {
        logger.debug("Running user check (login request)...");        
    }
    
    private BasicDataSource connect(String dbName, String host, String username, String password) throws Exception {

        String connectionURL = "jdbc:mysql://" + host + ":3306/" + dbName;
        
        logger.debug("connectionURL: " + connectionURL);
        
        //Class.forName("com.mysql.adbc.Driver").newInstance();
        //Connection connection = DriverManager.getConnection(connectionURL, username, password);
        
            BasicDataSource ds = new BasicDataSource();            
            ds.setDriverClassName("com.mysql.jdbc.Driver");            
            ds.setUsername(username);            
            ds.setPassword(password);            
            ds.setUrl(connectionURL);
        
            //return ds.getConnection();
            return ds;
        //return connection;
    }
    
    /*private DataSource connectEnsat(String dbName, String host, String username, String password) throws Exception {

        return Database.connectSecurity();
        //String connectionURL = "adbc:mysql://localhost:3307/" + dbName; //CONNECTION BACK TO ENSAT DATA MACHINE

        //username = "tunnel-2";
        //password = "a2yX4sp";
        //Class.forName("com.mysql.adbc.Driver").newInstance();
        //Connection connection = DriverManager.getConnection(connectionURL, username, password);
        
            //BasicDataSource ds = new BasicDataSource();            
            //ds.setDriverClassName("com.mysql.adbc.Driver");            
            //ds.setUsername(username);            
            //ds.setPassword(password);            
            //ds.setUrl(connectionURL);
        
            //return ds.getConnection();
            //return ds;
        //return connection;
    }*/

    public int checkUserDetails(String emailUsername, String password, String dbName, String host, String dbUsername, String dbPassword, Connection secConn) {

        /**
        * responseFlag:
        * 
        * 0 = user present, account active, membership up-to-date (OK login)
        * 1 = user not present (i.e. credentials are wrong)
        * 2 = user present, account not active
        * 3 = user present, account active, membership out-of-date
        */
        int responseFlag = 1;
        ResultSet rs = null;
        try {
            
            DataSource secds = null;
            if(secConn == null){
                //secds = this.connectEnsat(dbName, host, dbUsername, dbPassword);
                secds = this.connect(dbName, host, dbUsername, dbPassword);
                secConn = secds.getConnection();
            }
            //String sql = "SELECT user_id, username, password, password_sha2, salt FROM User WHERE username=?;";            
            //String sql = "SELECT user_id, username, password, password_sha2, salt, User.active, Membership.active_status FROM User,Membership WHERE User.user_id=Membership.user_id AND User.username=?;";
            //String sql = "SELECT User.user_id, username, password, password_sha2, salt, User.active, Membership.active_status FROM User,Membership WHERE User.user_id=Membership.user_id AND User.email_address=?;";
            String sql = "SELECT user_id, username, password, password_sha2, salt, active, domain FROM User WHERE User.email_address=?;";
            
            PreparedStatement ps = secConn.prepareStatement(sql);
            ps.setString(1,emailUsername);            
            rs = ps.executeQuery();
            
            //Test if the username is listed
            int userCount = 0;
            boolean accountActive = false;
            String domain = "";            
            if(rs != null){
                if(rs.next()){
                    
                    accountActive = rs.getString(6).equals("yes");
                    domain = rs.getString(7);
                    if(domain == null){
                        domain = "";
                    }
                
                //Test the SHA2 entry
                String sha2entry = rs.getString(4);
                if(sha2entry == null){
                    sha2entry = "";
                }
                
                
                if(!sha2entry.equals("")){
                    //Hash the password (+salt) with SHA2 algorithm
                    String salt = rs.getString(5);
                    String hashInput = salt + password;
                    logger.debug("Login credentials checked against SHA-256 (" + emailUsername + ")...");
                    String hashedInput = this.toSHA256(hashInput.getBytes("UTF-8"));
                    
                    if(hashedInput.equals(sha2entry)){
                        
                        //Need to check the domain credentials here
                        /*if(domain.equals("muppet")){
                            logger.debug("Domain is 'muppet'...");
                            userCount++; //THIS IS THE AUTHENTICATION SUCCESS STEP                            
                        }else if(domain.equals("ensat")){
                            logger.debug("Domain is 'ensat'...");
                            //Check for "muppet" role in SP_Role_User_Assignment
                            String userId = rs.getString(1);
                            if(userId == null){
                                userId = "";
                            }                            
                            boolean foundRole = this.checkForRole(userId, "muppet", secConn);
                            if(foundRole){
                                logger.debug("User has 'muppet' role entry...");
                                userCount++; //THIS IS THE AUTHENTICATION SUCCESS STEP
                            }else{
                                logger.debug("User does not have 'muppet' role entry...");
                            }
                        }else{
                            logger.debug("Domain is not recognised for MUPPET...");
                        }*/
                        userCount++; //THIS IS THE AUTHENTICATION SUCCESS STEP
                    }                    
                    
                }else{
                    
                    //Match on SHA1 entry
                    String hashedPassword = this.toSHA1(password.getBytes("UTF-8"));                    
                    String sha1entry = rs.getString(3);
                    if(hashedPassword.equals(sha1entry)){
                        
                        //Need to check the domain credentials here
                        /*if(domain.equals("muppet")){
                            logger.debug("Domain is 'muppet'...");
                            userCount++; //THIS IS THE AUTHENTICATION SUCCESS STEP                            
                        }else if(domain.equals("ensat")){
                            logger.debug("Domain is 'ensat'...");
                            //Check for "muppet" role in SP_Role_User_Assignment
                            String userId = rs.getString(1);
                            if(userId == null){
                                userId = "";
                            }                            
                            boolean foundRole = this.checkForRole(userId, "muppet", secConn);
                            if(foundRole){
                                logger.debug("User has 'muppet' role entry...");
                                userCount++; //THIS IS THE AUTHENTICATION SUCCESS STEP
                            }else{
                                logger.debug("User does not have 'muppet' role entry...");
                            }
                        }else{
                            logger.debug("Domain is not recognised for MUPPET...");
                        }*/
                        
                        //Now set the new password in SHA2, as well as the newly-generated salt value, and blank the sha1 entry
                        String salt = this.generateSaltValue();                    
                        String hashInput = salt + password;
                        logger.debug("Login credentials checked against SHA-256 (new assignment from SHA-1)...");
                        String hashedInput = this.toSHA256(hashInput.getBytes("UTF-8"));                    
                    
                        //String updateSha2Sql = "UPDATE User SET password_sha2=?,salt=?,password='' WHERE username=?;";
                        String updateSha2Sql = "UPDATE User SET password_sha2=?,salt=?,password='' WHERE email_address=?;";
                        PreparedStatement sha2ps = secConn.prepareStatement(updateSha2Sql);
                        sha2ps.setString(1,hashedInput);
                        sha2ps.setString(2,salt);
                        sha2ps.setString(3,emailUsername);
                        int updateResult = sha2ps.executeUpdate();
                    }
                }
                }else{
                    userCount = 0;
                }
            }else{
                userCount = 0;
            }            
            rs.close();
            
            //If they've made it this far and userCount is 1, then the presence and credentials are good
            if(userCount == 1){                
                //Now check the active flag
                if(!accountActive){
                    responseFlag = 2; //Account is deactivated
                }else{
                    //Account is active
                    responseFlag = 0; //All good, can login
                }
            }else{
                responseFlag = 1; //Credentials are incorrect
            }
            
            //Finally close the connection/data source
            if(secConn != null){
                secConn.close();
            }
            
        } catch (Exception e) {
            logger.debug("(" + emailUsername + ") - Error (checkUserDetails - using password): " + e.getMessage());
        }
        
        //Log any unsuccessful login attempts
        if(responseFlag != 0){
            logger.debug("Login request unsuccessful (username='" + emailUsername + "')");        
            if(responseFlag == 1){
                logger.debug("Credentials incorrect / membership entry not present (username='" + emailUsername + "')");
            }else if(responseFlag == 2){
                logger.debug("Account is deactivated (username='" + emailUsername + "')");
            }else if(responseFlag == 1){
                logger.debug("Membership has lapsed (username='" + emailUsername + "')");
            }
        }
        
        
        
        return responseFlag;
    }
    
    private boolean checkForRole(String userId, String roleName, Connection secConn) throws Exception{
        
        String sqlRoleCheck = "SELECT sp_role_userdomain FROM SP_Role_User_Assignment,SP_Role WHERE SP_Role.sp_role_id=SP_Role_User_Assignment.sp_role_id AND sp_user_id=?;";
        PreparedStatement psRoleCheck = secConn.prepareStatement(sqlRoleCheck);
        psRoleCheck.setString(1,userId);
        ResultSet rsRoleCheck = psRoleCheck.executeQuery();
        boolean foundRole = false;
        while(rsRoleCheck.next() && !foundRole){
            String roleIn = rsRoleCheck.getString(1);
            if(roleIn == null){
                roleIn = "";
            }
            if(roleIn.equals(roleName)){
                foundRole = true;
            }
        }
        return foundRole;
    }
    
    
    public int checkUserDetails(String username, String dbName, String host, String dbUsername, String dbPassword, Connection secConn) {

        int userCount = 0;
        ResultSet rs = null;

        try {
            
            DataSource secds = null;
            if(secConn == null){
                //secds = this.connectEnsat(dbName, host, dbUsername, dbPassword);
                //secds = this.connect(dbName, host, dbUsername, dbPassword);
                secConn = secds.getConnection();
            }
            //String sql = "SELECT user_id, username, forename, surname, role, country, center FROM User WHERE username=?;";
            String sql = "SELECT user_id, username, forename, surname, role, country, center FROM User WHERE email_address=?;";
            
            PreparedStatement ps = secConn.prepareStatement(sql);
            ps.setString(1,username);                        
            rs = ps.executeQuery();
            
            if (rs != null) {
                while (rs.next()) {
                    userCount++;
                }
            }
            rs.close();        
            //secConn.close();
            //secds.close();
        } catch (Exception e) {
            logger.debug("(" + username + ") - Error (checkUserDetails - no password): " + e.getMessage());
        }
        
        //Log any unsuccessful login attempts
        if(userCount != 1){
            logger.debug("Login request unsuccessful (username='" + username + "')");        
        }
        
        return userCount;
    }

    public UserBean setUserDetails(String emailUsername, String password, String dbName, HttpSession session, UserBean user, String host, String dbUsername, String dbPassword, Connection secConn) {

        String username = "";
        String forename = "";
        String surname = "";
        String role = "";
        String country = "";
        String center = "";
        String emailAddress = "";
        String domain = "";

        ResultSet rs = null;
        try {
            //Statement statement = this.connect(dbName);
            DataSource ds = null;
            if(secConn == null){
                //ds = this.connectEnsat(dbName, host, dbUsername, dbPassword);
                ds = this.connect(dbName, host, dbUsername, dbPassword);
                secConn = ds.getConnection();
            }String sql = "SELECT user_id, username, password, password_sha2, salt, forename, surname, role, country, center, email_address, domain FROM User WHERE email_address=?;";
            
            PreparedStatement ps = secConn.prepareStatement(sql);
            ps.setString(1,emailUsername);            
            rs = ps.executeQuery();

            if (rs.next()) {
                String saltIn = rs.getString(5);                            
                String inputToCheck = saltIn + password;
                String hashedInput = this.toSHA256(inputToCheck.getBytes("UTF-8"));
            
                String sha2entry = rs.getString(4);                
                if(hashedInput.equals(sha2entry)){
                    username = rs.getString(2);
                    forename = rs.getString(6);
                    surname = rs.getString(7);
                    role = rs.getString(8);
                    country = rs.getString(9);
                    center = rs.getString(10);
                    emailAddress = rs.getString(11);
                    domain = rs.getString(12);
                }
            }
            rs.close();
            secConn.close();   
        } catch (Exception e) {
            logger.debug("(" + emailUsername + ") - Error (setUserDetails): " + e.getMessage());
        }

        //Now set these details in the user bean and return it to the JSP page
        //Set the UserBean
        user.setUsername(emailAddress);
        user.setForename(forename);
        user.setSurname(surname);
        user.setRole(role);
        user.setCountry(country);
        user.setCenter(center);
        user.setDomain(domain);
        user.setIsSuperUser(false);

        //Get the current time and set as session time
        Format formatter = new SimpleDateFormat("HH:mm");
        java.util.Date date = new java.util.Date(session.getLastAccessedTime());
        String currentTime = formatter.format(date);
        user.setSessionLogin(currentTime);
        
        //Log the successful login
        logger.debug("=== User '" + emailUsername + "' successfully logged in at " + currentTime + " ===");        
        return user;
    }
    
    public String toSHA1(byte[] convertme) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        }catch(Exception e) {
            e.printStackTrace();
        } 
        
        //return new String(md.digest(convertme));
        String convertedStr = byteArrayToHexString(md.digest(convertme));
        logger.debug("Login credentials checked against SHA-1...");
        return convertedStr;
    }
    
    public String toSHA256(byte[] convertme) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        }catch(Exception e) {
            e.printStackTrace();
        } 
        
        //return new String(md.digest(convertme));
        String convertedStr = byteArrayToHexString(md.digest(convertme));        
        return convertedStr;
    }
    
    public String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
    return result;
    }
    
    
    private String generateSaltValue(){
        
        int SALT_BYTE_SIZE = 24;
        String saltStr = "";
        byte[] salt = null;
        try{
            
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            salt = new byte[SALT_BYTE_SIZE];
            random.nextBytes(salt);
        
        }catch(Exception e){
            logger.debug("Error (generateSaltValue): " + e.getMessage());
        }

        saltStr = salt.toString();        
        return saltStr;
    }

}
