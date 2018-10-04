<%@ page language="java" import="java.util.*,java.sql.*,java.text.Format,java.text.SimpleDateFormat" pageEncoding="ISO-8859-1"%>

<jsp:useBean id='connect' class='connection.ConnectBean' scope='session'/>

<%
Connection conn = connect.getConnection();    
    
String eusigId = request.getParameter("eusig_id");
if(eusigId == null){
    eusigId = "";
}

String pid = request.getParameter("pid");
if(pid == null){
    pid = "";
}

String holddown = request.getParameter("holddown");
if(holddown == null){
    holddown = "";
}

String listStr = "<select class='form-control' name='event_id'><option value=''>[Select...]</option>";

String sql = "SELECT DISTINCT event_id FROM event WHERE patient_id=? AND eusig_id=? AND holddown=? ORDER BY event_id;";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, pid);
ps.setString(2, eusigId);
ps.setString(3, holddown);
ResultSet rs = ps.executeQuery();

while (rs.next()) {
    String eventIdIn = rs.getString(1);
    listStr += "<option value='" + eventIdIn + "'>" + eventIdIn + "</option>";
}
listStr += "</select>";
rs.close();
%>

<%=listStr%>