
var height = "600";
var margin =  {bottom:110, left:80, right: 40};
var propertyX = "label";
var titleX = "";
var titleY = "";

function redraw() {
    
    var oCustomControls = d3.select("#customControls");
    oCustomControls.remove();
    
    d3.select("#title").text("Grouped/Stacked Bar Chart - "+ title);
     
    // Remove the old visualization
    d3.select("svg").remove();
 
    // Defines the height.
    d3.select("#content").append("svg")
        .attr("height", height)
        ;
    
    // NV3D
    nv.addGraph(function() {
        
        var chart = nv.models.multiBarChart();

        chart.xAxis
            .axisLabel(titleX)
//            .tickFormat(function(d, i) { return d; })
            ;

        chart.yAxis
            .axisLabel(titleY)
            .tickFormat(d3.format(',.1f'));

        // Add range of colours    
        chart.color(d3.scale.category10().range());
        
        // Custom tootip
        chart.tooltipContent(function(key, y, e, graph) { 
            return key + ': ' + '<BR>' + y + ' -- ' + e ;
        });
        
        chart.margin(margin);
//        chart.stacked(true);

        d3.select('#content svg')
            .datum(getVisualizationData())
        .transition().duration(500).call(chart);

        var xTicks = d3.select('.nv-x.nv-axis > g').selectAll('g');
        xTicks
            .selectAll('text')
            .attr('transform', function(d,i,j) { return 'translate (10, 0) rotate(70 0,0)' }) 
            .style("text-anchor", "start")
            .style("font-size", getSizeLabelX(data) + "px")
        ;
        
        // Show all values of X measure.
        chart.reduceXTicks(false); 
        d3.select(".nvd3.nv-wrap.nv-axis")
            .selectAll("g")
            .selectAll("text")
                .style("opacity", 1);
                
        // Initially show the grid        
        d3.select(".nv-x.nv-axis")
            .selectAll("line")
                .style("opacity", 1);
               
        nv.utils.windowResize(chart.update);
        
        return chart;
    });
}



function getVisualizationData() {
    
    // Array with values that contains the data Visualization.
    var dataMeasures = [];
    for (i in this.measures)
    {
        var dataMeasure = data.map(function(d) {
                var valueY = d[measures[i].property]
               
                if (typeof valueY === "string"){
                    if (measures[i].type == "double" ){
                        valueY = parseFloat(valueY);
                    }
                    else if (measures[i].type == "integer" ){
                        valueY = parseInt(valueY);
                    }
                }
                return { x: d[propertyX], y: valueY}});
        
        dataMeasures.push(dataMeasure);
    }
   
    // Array with values that contains the title of measure and the data Visualization.
    var arrayReturn = [];
    for (i in this.measures)
    {  
        var rObject = new Object();
        rObject.values = dataMeasures[i];
        rObject.key = measures[i].title;
       
        arrayReturn.push(rObject);
    }
   
    return arrayReturn;
}
