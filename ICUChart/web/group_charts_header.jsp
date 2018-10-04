

<%
String aggregateOutputStr = request.getParameter("aggregateOutputStr");
String catBoxPlotStr = "'ICP (10 mmHg)','ICP (15 mmHg)','ICP (20 mmHg)','ICP (25 mmHg)','ICP (30 mmHg)','CPP (50 mmHg)','CPP (60 mmHg)','CPP (70 mmHg)','BPs (100 mmHg)','BPs (110 mmHg)'";
String iqrRefLine = "Thresholds";

java.util.StringTokenizer st = new java.util.StringTokenizer(aggregateOutputStr,"|");

int thresholdLineNum = st.countTokens();
String[] thresholdCats = new String[thresholdLineNum];
for(int i=0; i < thresholdLineNum; i++){
    thresholdCats[i] = st.nextToken();
}
%>

<%--<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<script src="https://code.highcharts.com/highcharts.js"></script>
<script src="https://code.highcharts.com/highcharts-more.js"></script>
<script src="https://code.highcharts.com/modules/exporting.js"></script>--%>

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
            text: 'Distances'
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
                        <%=thresholdCats[0]%>,
                        <%=thresholdCats[1]%>,
                        <%=thresholdCats[2]%>,
                        <%=thresholdCats[3]%>,
                        <%=thresholdCats[4]%>,
                        <%=thresholdCats[5]%>,
                        <%=thresholdCats[6]%>,
                        <%=thresholdCats[7]%>,
                        <%=thresholdCats[8]%>,
                        <%=thresholdCats[9]%>

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
