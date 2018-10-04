/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

/**
 *
 * @author anthony
 */
public class UserBean {

    private String username = "";
    private String country = "";
    private String center = "";
    private String role = "";
    private String surname = "";
    private String forename = "";
    private String sessionLogin = "";
    private boolean isSuperUser = false;
    private String searchFilter = "";
    private String domain = "";
    //private boolean sessionExpired;

    public UserBean(){

    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String _username){
        username = _username;
    }

    public String getCountry(){
        return country;
    }

    public void setCountry(String _country){
        country = _country;
    }

    public String getCenter(){
        return center;
    }

    public void setCenter(String _center){
        center = _center;
    }

    public String getRole(){
        return role;
    }

    public void setRole(String _role){
        role = _role;
    }
    
    public String getSurname(){
        return surname;
    }

    public void setSurname(String _surname){
        surname = _surname;
    }

    public String getForename(){
        return forename;
    }

    public void setForename(String _forename){
        forename = _forename;
    }

    public String getSessionLogin(){
        return sessionLogin;
    }

    public void setSessionLogin(String _sessionLogin){
        sessionLogin = _sessionLogin;
    }

    public boolean getIsSuperUser(){
        return isSuperUser;
    }

    public void setIsSuperUser(boolean _isSuperUser){
        isSuperUser = _isSuperUser;
    }

    public String getSearchFilter(){
        return searchFilter;
    }

    public void setSearchFilter(String _searchFilter){
        searchFilter = _searchFilter;
    }
    
    public String getDomain(){
        return domain;
    }

    public void setDomain(String _domain){
        domain = _domain;
    }
}
