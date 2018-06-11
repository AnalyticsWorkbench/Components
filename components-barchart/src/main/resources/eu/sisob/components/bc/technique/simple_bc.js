
var propertyX = "label";

var currentMeasure;
var currentMeasureLabel;

function redraw() {
    
    d3.select("#title").text("Simple Bar Chart - "+ title);
    
    initializeMeasureControls();
    draw();
}

function draw() {

    // removes old chart if exists
    d3.select("svg").remove();

    // Viewport resolution is "virtual", the visualization scales (attr viewBox in svg main element)
    var width = "1200";
    var height = "600";

    // Set margins
    var margin = { top: 40, right: 20, bottom: 110, left: 40}

    // create svg element
    var svg = d3.select("#content").append("svg")
		.attr("viewBox", "0 0 " + width + " " + height )
                .attr("preserveAspectRatio", "xMidYMid meet")
	  	.append('svg:g')
                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
    // Chart size 
    var cheight = height - margin.top - margin.bottom;
    var cwidth  = width - margin.left - margin.right;

    // x and y scales
    var x = d3.scale.ordinal().rangeRoundBands([0, cwidth], .1);
    var y = d3.scale.linear().range([cheight, 0]);

    // x and y axis
    var xAxis = d3.svg.axis().scale(x).orient("bottom");
    var yAxis = d3.svg.axis().scale(y).orient("left");

    // enforce number datatype in data source
    data.forEach(function(d) {
        d[currentMeasure] = +d[currentMeasure];
    });

    // x domain of labels
    x.domain(data.map(function(d) { return d[propertyX]; }) );

    // y domain of measures
    y.domain([0, d3.max(data, function(d) { return d[currentMeasure]; })] );

    // xAxis svg element
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + cheight + ")")
        .call(xAxis);

    // yAxis svg element
    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis).append("text")
            .attr("transform", "rotate(0)").attr("y", 2)
            .attr("y", -26)
            .attr("dy", ".71em")
            .style("text-anchor", "start")
            .text(currentMeasureLabel);

    // bars elements calculated from data
    svg.selectAll(".bar")
        .data(data).enter()
        .append("rect")
        .attr("class", "bar")
        .attr("x", function(d) { return x(d[propertyX]); } )
        .attr("width", x.rangeBand())
        .attr("y", function(d) { return y(d[currentMeasure]); })
        .attr("height", function(d) { return cheight - y(d[currentMeasure]); } )
        .style("fill", "steelblue");

        
    // STYLE
    d3.select(".axis").selectAll('g').selectAll('text')
        .attr('transform', "rotate(50 6,10)") 
        .style("text-anchor", "start")
        .style("font-size", getSizeLabelX(data) + "px")
        ;
        
    d3.selectAll(".axis path")
        .style("fill", "none")
        .style("stroke", "#000")
        .style("shape-rendering", "crispEdges")
        ;   
    d3.selectAll(".axis line")
        .style("fill", "none")
        .style("stroke", "#000")
        .style("shape-rendering", "crispEdges")
        ;    
    
}



