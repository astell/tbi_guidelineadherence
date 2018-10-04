<%@ page language="java" import="java.io.*,java.util.*,patients.*" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml">

<jsp:include page="/page/index_head.jsp" />

<body>
    <div id="site_body">
<jsp:include page="/page/index_nav.jsp" />
        
        <p>Welcome to the ICU-Chart TBI guideline adherence tool</p>
        
        <p>This application allows the investigation of a cohort of brain-injured patients to see different metrics of adherence to the official guidelines</p>
        
        <p>[User guide]</p>
        
        <p>The guidelines referenced are the <a target='_blank' href='http://www.braintrauma.org'>Brain Trauma Foundation</a> (BTF) guidelines for Traumatic Brain Injury</p>
        
        <p>
            <a target='_blank' href='http://www.anthonystell.com'>Anthony Stell &copy; 2018</a>
        </p>
        
<jsp:include page="/page/index_login_modal.jsp" />
        
<jsp:include page="/page/page_foot.jsp" />
</div>

</body>
</html>
