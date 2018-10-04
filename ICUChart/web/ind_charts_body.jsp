

<%
String dataset = request.getParameter("dataset");
String pid = request.getParameter("pid");
String seriesSelector = request.getParameter("seriesSelector");
String timeWindow = request.getParameter("timeWindow");
String eusigId = request.getParameter("eusigId");
String holddown = request.getParameter("holddown");

String thisPatientMeanCombinedDisp = request.getParameter("meanCombined");
String thisPatientMeanCombined2Disp = request.getParameter("meanCombined2");

String patientOutputTable = request.getParameter("patientOutputTable");
String patientIQRTable = request.getParameter("patientIQRTable");
boolean removeDefault = request.getParameter("removeDefault").equalsIgnoreCase("true");

/*String thisTotalStayTime = request.getParameter("thisTotalStayTime");
String thisTimeWithinEvents = request.getParameter("thisTimeWithinEvents");
String thisDefaultTime = request.getParameter("thisDefaultTime");
String thisNonDefaultTime = request.getParameter("thisNonDefaultTime");
String nonDefaultAsPercent = request.getParameter("nonDefaultAsPercent");*/

%>

<h2>Guideline adherence information</h2>

<h2 id="section_header">Summary criteria</h2>
<ul>
    <li>Dataset: <strong><%=dataset%></strong></li>
    <li>Patient ID: <strong><%=pid%></strong></li>    
    <li>Physiological series: <strong><%=seriesSelector%></strong></li>
    <li>Threshold (mmHg): <strong><%=eusigId%></strong></li>
    <li>Hold-down (mins): <strong><%=holddown%></strong></li>
    <li>Time window (mins): <strong><%=timeWindow%></strong></li>
</ul>

<p>Please click on each section header below to view details</p>

<hr/>

<h2 id="section_header"><a href="#physiodata" data-toggle="collapse">Physiological data and adherence</a></h2>

<p>This chart shows the physiological output for the selected event and the corresponding minute-by-minute guideline adherence.</p>
<p>The adherence measurement begins after one time-window length has passed since the beginning of the event.</p>
<p>To view the reasons contributing to a particular level of adherence, click on the individual data point on the adherence series (red line).</p>

<div id="physiodata" class="collapse">
<div id="container" style="min-width: 310px; max-width: 1600px; height: 800px; margin: 0 auto"></div>
</div>

<hr/>

<h2 id="section_header"><a href="#instances" data-toggle="collapse">Instances of non-adherence</a></h2>

<p>This table shows the different levels of non-adherence to the chosen guideline during the entire patient stay (aggregated per level).</p>
<p>Each line in the table shows the duration, level, and reasons contributing to that level.</p>
<p><strong>Note:</strong> this table shows non-adherence levels for the whole stay, not just the event selected above.</p>
        
    <div id="instances" class="collapse">
    <%=patientOutputTable%>
    <br/>
    
    <p>The following items also show overall durations for this patient:</p>
    
    <%--<strong>Total time of patient stay (mins):</strong> <%=thisTotalStayTime%>
    <br/>
    <strong>Total time of patient requiring clinical action after an event (mins):</strong> <%=thisTimeWithinEvents%>
    <br/>--%>
<%
    if(!removeDefault){    
%>

    <br/>
    <p>An overall "default" duration is present in every evaluation of guideline adherence. 
        This "default" state is believed to be the duration of time a patient requires clinical action but has yet to receive it.</p>

    <%--<strong>Total duration of &quot;default&quot; non-adherence state (mins):</strong> <%=thisDefaultTime%>
    <br/>
    <strong>Total time spent in non-adherence, which is not in the &quot;default&quot; state (mins):</strong> <%=thisNonDefaultTime%>
    <br/>
    <strong>Expressed as a percentage of all the time spent requiring clinical action:</strong> <%=nonDefaultAsPercent%> &percnt;
    <br/>--%>
<%
}   
%>
    </div>
    
<hr/>

<h2 id="section_header"><a href="#reasons" data-toggle="collapse">Contributing reasons for non-adherence</a></h2>

<p>This chart visually compares the different contributions of each reason to the overall non-adherence levels</p>

<div id="reasons" class="collapse">
    <div id="container_stacked" style="min-width: 310px; max-width: 800px; height: 400px; margin: 0 auto"></div>
</div>

    
<hr/>
    
<h2 id="section_header"><a href="#iqr" data-toggle="collapse">Interquartile range of each factor</a></h2>

<p>This table and box-plot shows the statistical spread (representation) of the duration, degree and two combinatorial metrics of all the instances</p>

<%
if(removeDefault){    
%>
<p><strong>Note:</strong> the "default" non-adherence instance has been removed from this calculation.</p>
<%
}   
%>

<div id="iqr" class="collapse">    
<%=patientIQRTable%>

<br/>
<br/>

<div id="container_boxplot" style="height: 400px; margin: auto; min-width: 310px; max-width: 600px"></div>
</div>

<hr/>

<h2 id="section_header"><a href="#combochart" data-toggle="collapse">Severity chart</a></h2>
        
<p>This chart indicates the severity of the guideline non-adherence based on a combination of the degree and duration of the non-adherence</p>
<ul>
    <li>The bottom left quadrant (green) indicates low severity</li>
    <li>The top right quadrant (red) indicates high severity</li>
    <li>The remaining quadrants (blue) indicate mid-range severity</li>
</ul>

<p>Each instance of non-adherence has a duration and a distance associated with it. For each instance, the metrics A and B are calculated.
    Presented on this chart is the mean of all the instances of A and B for this patient, as a summary measure of severity.</p>

<div id="combochart" class="collapse">
<p><strong>A (duration / non-adherence):</strong> <%=thisPatientMeanCombinedDisp%></p>
<p><strong>B (duration * non-adherence):</strong> <%=thisPatientMeanCombined2Disp%></p>

<div id="container_combo" style="min-width: 310px; max-width: 600px; height: 600px; margin: 0 auto"></div>
</div>
