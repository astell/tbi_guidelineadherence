

<%
String pid = request.getParameter("pid");
String eventId = request.getParameter("eventId");
String seriesSelector = request.getParameter("seriesSelector");
String seriesStr = request.getParameter("seriesStr");
String stackedSeriesStr = request.getParameter("stackedSeriesStr");
String categoryStr = request.getParameter("categoryStr");
String thisGuidelineStart = request.getParameter("thisGuidelineStart");
String meanCombined = request.getParameter("meanCombined");
String meanCombined2 = request.getParameter("meanCombined2");
//String thisPatientMeanCombinedDisp = request.getParameter("thisPatientMeanCombinedDisp");
//String thisPatientMeanCombined2Disp = request.getParameter("thisPatientMeanCombined2Disp");
String maxCombined2 = request.getParameter("maxCombined2");
String tickCombined2 = request.getParameter("tickCombined2");
String guidelineReasonJsArrayStr = request.getParameter("guidelineReasonJsArrayStr");
String catBoxPlotStr = request.getParameter("catBoxPlotStr");
String patientIQRDataStr = request.getParameter("patientIQRDataStr");
String iqrRefLine = request.getParameter("iqrRefLine");

%>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>

<script src="https://code.highcharts.com/stock/highstock.js"></script>
<script src="https://code.highcharts.com/stock/modules/exporting.js"></script>
<script src="https://code.highcharts.com/modules/exporting.js"></script>

<script src="https://code.highcharts.com/modules/data.js"></script>

<script src="https://code.highcharts.com/highcharts-more.js"></script>

<!-- Additional files for the Highslide popup effect -->
<script src="https://www.highcharts.com/media/com_demo/js/highslide-full.min.js"></script>
<script src="https://www.highcharts.com/media/com_demo/js/highslide.config.js" charset="utf-8"></script>
<link rel="stylesheet" type="text/css" href="https://www.highcharts.com/media/com_demo/css/highslide.css" />

<script>
var guidelineValueArray = new Array();
<%=guidelineReasonJsArrayStr%>


function getGuidelineReason(timeParam){
    
    //window.alert("timeParam: " + timeParam);
    //window.alert("thisGuidelineStart: " + <%=thisGuidelineStart%>);
    
    var timeDiff = timeParam - <%=thisGuidelineStart%>;
    //window.alert("timeDiff: " + timeDiff);
    
    var timeDiffMins = (timeDiff / 60000);
    //window.alert("timeDiffMins: " + timeDiffMins);
    
    var guidelineValIndex = timeDiffMins;
    //window.alert("guidelineValIndex: " + guidelineValIndex);
    var guidelineReason = guidelineValueArray[guidelineValIndex];
    //window.alert("guidelineReason: " + guidelineReason);
    
    //return '<li>' + guidelineReason + '</li>';
    return '' + guidelineReason + '';
}


$(function () {
    $('#container').highcharts({
        chart: {
            type: 'line',
            zoomType: 'x'
        },
        title: {
            text: 'Physiological output for patient <%=pid%>, event <%=eventId%>'
        },
        xAxis: {
            type: 'datetime', //ensures that xAxis is treated as datetime values            
            dateTimeLabelFormats : {
                day: '%H:%M'
            }
            //tickInterval: 3600 * 1000
        },
        yAxis: [{
                labels: {
                    format: '{value} mmHg'
                },
                title: {
                    text: 'Pressure (<%=seriesSelector%>)'
                }
            },{ // Secondary yAxis
                labels: {
                    format: '{value} %'            
                },                
                title: {
                    text: 'Distance from guideline (non-adherence)'
                },
                opposite: true
            }
        ],
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle'
        },
        plotOptions: {
            series: {  
                point: {
                    events: {
                        click: function (e) {                            
                            if(this.series.name === 'Distance from guideline (non-adherence)'){
                                hs.htmlExpand(null, {
                                    pageOrigin: {
                                        x: e.pageX || e.clientX,
                                        y: e.pageY || e.clientY
                                    },
                                    headingText: this.series.name,
                                    maincontentText: 'Time: ' + Highcharts.dateFormat('%e %b %Y - %H:%M', this.x) + ' <br/> Value: ' + this.y + ' % <br/> Reasons: <ul>' + getGuidelineReason(this.x) + '</ul>',
                                    width: 200
                                });
                            }
                        }
                    }
                }
            }
        },
        <%=seriesStr%>
});

});

$(function () {

var data = [            
    {x:<%=meanCombined2%>,y:<%=meanCombined%>}
    <%--{x:<%=meanCombined2%>,y:<%=thisPatientMeanCombinedDisp%>},
    {x:<%=meanCombined2%>,y:<%=meanCombined%>}--%>
    ];

var chart = new Highcharts.Chart({
    
    chart: {
        renderTo: 'container_combo',
        defaultSeriesType:'scatter',
        borderWidth:1,
        borderColor:'#ccc',
        marginLeft:125,
        marginRight:50,
        backgroundColor:'#eee',
        plotBackgroundColor:'#fff'
    },
    credits:{enabled:false},
    title:{
        text:'Severity chart - patient <%=pid%>'
    },
    legend:{
        enabled:false                                
    },
    tooltip: {
        formatter: function() {
            return '<b>A, B</b><br/>' + this.y +', '+ this.x;
        }
    },
    plotOptions: {
        series: {
            shadow:false
        }
    },
    xAxis:{
        title:{
            text:'B (duration * distance)'
        },
        //min:-100,
        //max:100,
        //tickInterval:100,
        min:0,
        max: <%=maxCombined2%>,
        tickInterval: <%=tickCombined2%>,
        tickLength:0,
        minorTickLength:0,
        gridLineWidth:1,
        showLastLabel:true,
        showFirstLabel:false,
        lineColor:'#ccc',
        lineWidth:1                
    },
    yAxis:{
        title:{
            text:'A <br/>(duration / distance)',
            rotation:0,
            margin:25
        },
        //min:-100,
        //max:100,
        //tickInterval:100,
        min:0,
        max:1,
        tickInterval:1,
        tickLength:3,
        minorTickLength:0,
        lineColor:'#ccc',
        lineWidth:1        
    },
    series: [{
        color:'#185aa9',
        data: data
    }]
}, function(chart) { // on complete
     
        var width = chart.plotBox.width / 2.0;
        var height = chart.plotBox.height / 2.0 + 1;
            
        chart.renderer.rect(chart.plotBox.x,                      
                            chart.plotBox.y, width, height, 1)
            .attr({
                fill: 'lightblue',
                zIndex: 0
            })
            .add();
    
     chart.renderer.rect(chart.plotBox.x + width,                      
                            chart.plotBox.y, width, height, 1)
            .attr({
                fill: 'pink',
                zIndex: 0
            })
            .add();
    
    chart.renderer.rect(chart.plotBox.x,                      
                            chart.plotBox.y + height, width, height, 1)
            .attr({
                fill: 'lightgreen',
                zIndex: 0
            })
            .add();
    
    chart.renderer.rect(chart.plotBox.x + width,                      
                            chart.plotBox.y + height, width, height, 1)
            .attr({
                fill: 'lightblue',
                zIndex: 0
            })
            .add();
        
});
});
</script>

<script>
$(function () {
    
var chart_stacked = new Highcharts.Chart({
    
    chart: {
        renderTo: 'container_stacked',
        type: 'bar'
    },
    title: {
        text: 'Contributing reasons of non-adherence (%) - patient <%=pid%>'
    },
    xAxis: {
        categories: <%=categoryStr%>
    },
    yAxis: {
        min: 0,
        max: 100,
        title: {
            text: 'Adherence (%)'
        }
    },
    legend: {
        reversed: true
    },
    plotOptions: {
        series: {
            stacking: 'normal'
        }
    },
    series: <%=stackedSeriesStr%>
});    
});
</script>

<script>
$(function () {

    var chart_boxplot = Highcharts.chart({

    chart: {
        renderTo: 'container_boxplot',
        type: 'boxplot'
    },

    title: {
        text: 'Representative distribution of <%=iqrRefLine%>'
    },

    legend: {
        enabled: false
    },

    xAxis: {
        categories: [<%=catBoxPlotStr%>],
        title: {
            //text: 'Patients'
            text: '<%=iqrRefLine%>'
        }
    },

    yAxis: {
        title: {
            text: 'Values'
        }/*,
        plotLines: [{
            value: 932,
            color: 'red',
            width: 1,
            label: {
                text: 'Theoretical mean: 932',
                align: 'center',
                style: {
                    color: 'gray'
                }
            }
        }]*/
    },

    series: [{
        name: 'Values',
        data: [
                        <%=patientIQRDataStr%>

        ],
        tooltip: {
            headerFormat: '<em><%=iqrRefLine%></em><br/>'
        }
    }/*, {
        name: 'Outlier',
        color: Highcharts.getOptions().colors[0],
        type: 'scatter',
        data: [ // x, y positions where 0 is the first category
            [0, 644],
            [4, 718],
            [4, 951],
            [4, 969]
        ],
        marker: {
            fillColor: 'white',
            lineWidth: 1,
            lineColor: Highcharts.getOptions().colors[0]
        },
        tooltip: {
            pointFormat: 'Observation: {point.y}'
        }
    }*/]

});    
});
</script>
