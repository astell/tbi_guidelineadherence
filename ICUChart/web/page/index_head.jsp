<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>

<%
String path = request.getContextPath();
//String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String basePath = "//"+request.getServerName()+"/"; //removing the port specification (due to haproxy setup)
%>


<head> 
    <!-- Bootstrap meta-tags -->
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" /> 
<title>ICU Chart</title> 
<base href="<%=basePath%>" />

<!-- Enforcing the session time-out (15 mins in seconds) -->
<script type="text/javascript">
    var timeoutsec = 900;
    var expirytag = "<meta http-equiv=\"refresh\" content=\"" + timeoutsec + "; url=/sessionexpired.jsp\">";
    document.write(expirytag);    
</script>


<!-- Bootstrap -->
<link href="./css/bootstrap.min.css" rel="stylesheet">

<style type="text/css">
        #site_body 
        {
            width: 95%;
            margin: 0 auto;
            background-color: white;
        }
        #login_table
        {
            /*width: 50%;*/
            margin: 0 auto;            
        }
        
        #summary_table
        {            
            width: 75%;       
            border: thin solid #000000;            
        }
        
        #title_banner 
        {
            width: 100%;
            /*padding-top: 10px; 
            padding-right: 10px; 
            padding-bottom: 100px; 
            padding-left: 10px;*/
            padding: 100px;
            border-bottom: thin solid #000000;
            background: url('images/heading.png');
            background-size: 100%;
        }
        
        #title_banner td
        {
            padding: 25px;                  
        }

        #title_header
        {            
            text-align: center;
            font-size: 18px;
        }
        
        #title_highlights
        {
            color: #2aabd2;
        }
        
        #nav_banner 
        {            
            width: 100%;
            padding-top: 10px; 
            padding-right: 10px; 
            padding-bottom: 10px; 
            padding-left: 10px;
            border-bottom: thin solid #000000;
        }
        
        #nav_banner_left
        {
            width: 100%;            
            padding-top: 10px; 
            padding-right: 10px; 
            padding-bottom: 10px; 
            padding-left: 10px;            
        }
        
        .nav_item
        {            
            font-weight: bold;                                    
        }
        
        .nav_text
        {            
            font-weight: normal;   
            font-size: 12px;
        }
        
        /*.nav-pills > li
        {            
            vertical-align: middle;
            display: inline-block;
        }
        
        .nav-pills > li.active > a, .nav-pills > li.active > a:hover {
            background-color: #eeeeee;
            color: #429e41;
        }
        
        .nav-pills > li.active > a, .nav-pills > li.active > a:focus {
            background-color: #eeeeee;
            color: #429e41;
        }
        
        
        .nav-pills > li.active > a, .nav-pills > li > a:hover {
            background-color: #eeeeee;
            color: #429e41;
        }
        
        .nav-pills > li.active > a, .nav-pills > li > a:focus {
            background-color: #eeeeee;
            color: #429e41;
        }*/
        
        
        
        
        #nav_mgmt
        {            
            float: right;
        }
        
        #nav_banner_right
        {
            width: 50%;           
            right: 0px;
            padding-top: 10px; 
            padding-right: 10px; 
            padding-bottom: 10px; 
            padding-left: 10px;            
        }
        
        #home_canvas
        {
            width: 100%;
        }
        
        .account_mgmt
        {
            list-style-type: none;
            text-align: left;
        }
        
        .account_mgmt_link
        {
            list-style-type: none;
            font-weight: bold;
        }
        
        #parameter_input
        {
            width: 75%;
            text-align: center;
        }
        
        #chart_div
        {
            width: 550px;
            height: 300px;            
            border-bottom: thin solid #000000;
            border-top: thin solid #000000;
            border-right: thin solid #000000;
            border-left: thin solid #000000;            
        }
        
        #chart_div_pi
        {
            width: 550px;
            height: 300px;
            border-bottom: thin solid #000000;
            border-top: thin solid #000000;
            border-right: thin solid #000000;
            border-left: thin solid #000000;
        }
        
        #edit_details_table
        {
            width: 50%;            
            /*border-bottom: thin solid #000000;
            border-top: thin solid #000000;
            border-right: thin solid #000000;
            border-left: thin solid #000000;*/
        }
        
        .inner_account_mgmt_table td
        {
            padding: 5px;                  
        }
        
        .domain_filter_table td
        {
            padding: 5px;            
        }
        
        .warning
        {
            color: red;            
        }
        
        .dropdown-menu li{
            list-style-type: none;
        }
        
        #hidden_params{
            padding-top: 10px; 
            padding-right: 10px; 
            padding-bottom: 10px; 
            padding-left: 0px;            
        }
        
        #patient_detail{
            padding-top: 10px;
            width: 95%;
            margin: 0 auto;
        }
        
        #patient_header{
            padding-top: 10px;
            width: 100%;
            margin: 0 auto;
        }
        
        #patient_id{
            font-size: 20px; 
            font-weight: bold;             
        }

        #retro_prosp{
            font-size: 12px; 
            font-weight: bold;             
        }
        
        #patient_subheader, #edit_form_title, #edit_main_title{
            font-size: 14px; 
            font-weight: bold;             
        }
        
        #section_header{
            font-size: 18px; 
            font-weight: bold;             
        }
        
        h4#section_header{
            font-size: 14px; 
            /*font-weight: normal;*/
            display:inline-block;
            margin-right:20px;
            padding-bottom: 10px;
        }
        
        .more-info{
            display:inline-block;            
        }
        
        .col-md-6 {
            padding-right: 22px;
            padding-left: 22px;
        }
        
        a, h1, h2{
            color: #8abfa5;
        }
        
        a:hover, a:focus{
            color: black;
        }
        
        .btn-primary, .btn-primary:visited{
            background-color: #8abfa5;
            border-color: #8abfa5;
        }
        
        .btn-primary:hover,.btn-primary:focus,.btn-primary:active,.btn-primary:active:focus{
            background-color: #44c083;
            border-color: #44c083;
        }
        
        .form-control{
            width: 25%;
        }
        
        
        .hide{
            display: none;
        }

        .show{
            display: block;
        }
        
        .explanatory_text{
            width: 45%;
        }
        
        .sub-divider-line{
            width: 35%;
            display:inline-block;
        }

        
</style>


<%--<link href="./css/ensat.css" rel="stylesheet" type="text/css" />--%>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="./js/bootstrap.min.js"></script>

<%--<!-- Imported directly from ENSAT -->
<link rel="stylesheet" href="./css/jquery-ui.css" />
<script src="./scripts/jquery-1.12.1.js"></script>
<script src="./scripts/jquery-ui.js"></script>--%>
  
<!-- NOTE: the onSelect:function(){} bit is a patch for IE and the JQuery datepicker -->  
<%--<script>
  $(function() {      
      $('input').filter('.datepicker').datepicker({
          dateFormat: "dd-mm-yy", onSelect:function(){} 
      });             
  });
</script>--%>

<!-- Control the login modal box here -->
<%
String loggedOut = request.getParameter("logout");
String incorrectLogin = request.getParameter("incorrectlogin");
String sessionExpired = request.getParameter("sessionexpired");
if(loggedOut != null || incorrectLogin != null || sessionExpired != null){
%>
<script type="text/javascript">
    //window.alert("The login page has loaded with one of the messages and the function has changed...");
    $(window).load(function(){
        $('#login_modal').modal(options);
        //$('#login_modal').show();
    });
    
    var options = {
        "show" : "true"
    };
</script>
<%
}
%>

<%--<script type="text/javascript">
$(document).on("click", ".edit_main", function () {    
     var tablenameId = $(this).data('tablename');    
     var tables = ["Identification","Inclusion"];
     for(var i=0; i<tables.length; i=i+1){
         var tableIn = tables[i];         
         if(tablenameId===tableIn){             
             $(".modal-body #modal_" + tables[i]).show();
             $(".modal-body #table_trigger_" + tables[i]).val("1");
             $(".modal-body #edit_main_title").html(tablenameId);
         }else{             
             $(".modal-body #modal_" + tables[i]).hide();
         }
     }         
});
</script>--%>

<!-- End of JQuery headers -->


<!-- Custom JavaScript functions -->
<script src="./js/icuchart.js"></script>

<%--
<!-- Timeline CSS and JS -->
<script type="text/javascript" src="./scripts/timeline.js"></script>                
<link href="./css/timeline.css" media="all" rel="stylesheet" type="text/css" >
<script type="text/javascript" src="https://www.google.com/jsapi"></script>--%>

<script type="text/javascript">
$(document).on("click", ".more-info", function () {    
    
    var defnStr = $(this).data('defn');     
    var defnSections = ['eusig','holddown','time_window','event_id','adherence_aspect','remove_default'];
    var defnLabels = ['EUSIG definition','Hold-down','Time window','Event ID','Adherence aspect','Remove \'default\''];
    var defnLabelDisp = "";
    
    for(var i=0; i<defnSections.length; i=i+1){
        $(".modal-body #modal_" + defnSections[i]).hide();
        if(defnSections[i]===defnStr){
            defnLabelDisp = defnLabels[i];
        }
    }
    
    $(".modal-body #more_info_header").html(defnLabelDisp);
    $(".modal-body #modal_" + defnStr).show();
     
});
</script>


</head>

