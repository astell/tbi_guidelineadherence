<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>

<%
String incorrectLogin = request.getParameter("incorrectlogin");
String sessionExpired = request.getParameter("sessionexpired");
String loggedOut = request.getParameter("logout");

boolean showFailMessage = false;
String failMessage = "Login incorrect - please try again.";

boolean showExpiredMessage = false;
boolean showLoggedOutMessage = false;
if(incorrectLogin != null){        
    showFailMessage = true;    
}else if(sessionExpired != null){    
    showExpiredMessage = true;
}else if(loggedOut != null){
    showLoggedOutMessage = true;
}

%>

<%--<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>--%>

<%--<p>--%>
<%--<div align="center">--%>
    
    <%
    if(showFailMessage){
    %>
    <strong><%=failMessage%></strong>
    <%
    }else if(showExpiredMessage){
    %>
    <strong>Session expired - please login again.</strong>
    <%
    }else if(showLoggedOutMessage){
    %>
    <strong>Thank you - you have successfully logged out.</strong>
    <%
    }else{
    %>
    <%
    }
    %>
    
    <%--<br/>
    <br/>--%>

    <div id="login_table">
        <table class="table table-striped">
        <tr><th>Username</th><td><input type="text" name="uname" size="30"/></td></tr>
        <tr><th>Password</th><td><input type="password" name="pword" size="30"/></td></tr>    
        <!--<tr><td colspan="2"><div align="center"><input type="button" value="Login"/></div></td></tr>-->
    </table>
    </div>
<%--</div>--%>
<%--</p>--%>
