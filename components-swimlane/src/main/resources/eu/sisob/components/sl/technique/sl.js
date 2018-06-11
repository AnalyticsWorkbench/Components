function loadCustomControls(){

   
    if(d3.select("#xaxisinfo")==""){
        d3.select("#customcontrols").append("p")
            .attr("id","xaxisinfo")
            .text("X Axis Size")
    }

    if(d3.select("#xinput")==""){
        d3.select("#customcontrols").append("input")
            .attr("id","xinput")
            .attr("type","number")
            .attr("size","4")
            .attr("value","10");
    }

    if(d3.select("#yaxisinfo")==""){
        d3.select("#customcontrols").append("p")
            .attr("id","yaxisinfo")
            .text("Y Axis Size")
    }

    if(d3.select("#yinput")==""){
        d3.select("#customcontrols").append("input")
            .attr("id","yinput")
            .attr("type","number")
            .attr("size","4")
            .attr("value","10");
    }
    if(d3.select("#messageinfo")==""){
        d3.select("#customcontrols").append("p")
            .attr("id","messageinfo")
            .text("Message Size")
    }

    if(d3.select("#messageinput")==""){
        d3.select("#customcontrols").append("input")
            .attr("id","messageinput")
            .attr("type","number")
            .attr("size","4")
            .attr("value","10");
    }

    if(d3.select("#nodeinfo")==""){
        d3.select("#customcontrols").append("p")
            .attr("id","nodeinfo")
            .text("Node Size")
    }

    if(d3.select("#nodeinput")==""){
        d3.select("#customcontrols").append("input")
            .attr("id","nodeinput")
            .attr("type","number")
            .attr("size","4")
            .attr("value","300");
    }


    if(d3.select("#updatebutton")==""){
        d3.select("#customcontrols")
            .append("p")
            .append("button")
            .attr("id","updatebutton")
            .text("Update Graph")
            .on("click",function(){
                loadData(datalinks[currentTime].toString());
            });
    }

}

function extractKeyValues(data,filterParameter){
    var values = new Array();
    var nest = d3.nest()
        .key(function(d) { return d[filterParameter]; })
        .entries(data);

    nest.forEach(function(element){
        values.push(element.key)
    })
    return values;
}

function redraw(){

    // Set visualization title
    d3.select("#title").text("Swim Lane Layout - "+ title );
    
    function panzoom_redraw() {
        svg.attr("transform", "translate(" + d3.event.translate + ")"  + " scale(" + d3.event.scale + ")");
    }
  

    if(swimLaneYAxis.length>1 && swimLaneXAxis.length>1){     

            // create svg element
            var svg = d3.select("#content").append("svg")
                .attr("id","canvas")
                .attr("viewBox", "0 0 " + width + " " + height )
                .attr("pointer-events", "all")
                .attr("preserveAspectRatio", "xMidYMid meet")
                .call(d3.behavior.zoom().on("zoom", panzoom_redraw))
                .append('svg:g');

            svg.append('svg:rect')
                .attr('width', width)
                .attr('height', height)
                .attr('fill', 'white');

            if(directed=='true'){
                  var marker = svg.append('svg:defs').selectAll("marker")
                     .data(["0","1","2","3","4","5","6","7","8","9","10"])                    
                     .enter().append('svg:marker')
                    .attr('id', 'endMarker')
                    .attr('viewBox', '0 -5 10 10')
                    .attr('refX', 17)
                    .attr('refY', 0)
                    .attr('markerWidth', 10)
                    .attr('markerHeight', 10)
                    .attr('orient', 'auto')
                  .append('svg:path')
                    .attr('d', 'M0,-5L10,0L0,5')                    
            }

        var swimLaneYAxisHeight = height-(height *.10);
        var swimLaneXAxisWidth = width-(width *.02);

        var swimLaneYAxisMargin = 220;
        var swimLaneXAxisMargin = swimLaneYAxisHeight+10;

        var yDelta = (swimLaneYAxisHeight)/swimLaneYAxis.length;
        var xDelta = (swimLaneXAxisWidth-(swimLaneYAxisMargin))/swimLaneXAxis.length;


        //insert the y axis vertical separation
        var verticalSeparation = svg.selectAll("line.hline")
            .data(["0"]).enter().append("svg:line")
            .attr("id",function(d,i){return "hline"+i})
            .attr("class","hline")
            .attr("stroke-width", 1)
            .attr("stroke", "black")
            .attr("x1", swimLaneYAxisMargin)
            .attr("x2", swimLaneYAxisMargin)
            .attr("y1", 0)
            .attr("y2", swimLaneYAxisHeight)
            .transition()
            .attr("transform",function(d,i){return "translate("+(i)*xDelta+")"});

        //insert x avis horizontal separation
        var horizontalSeparation = svg.selectAll("line.vline")
            .data("0").enter().append("svg:line")
            .attr("id",function(d,i){return "vline"+i})
            .attr("class","vline")
            .attr("stroke-width", 1)
            .attr("stroke", "black")
            .attr("x1", swimLaneYAxisMargin)
            .attr("y1", swimLaneYAxisHeight)
            .attr("x2", swimLaneXAxisWidth)
            .attr("y2", swimLaneYAxisHeight)
            .transition()
            .attr("transform",function(d,i){return "translate("+0+","+ (-(i)*yDelta)+")"})


        //insert the y axis elements
        var yAxisLabel = svg.selectAll("text.ylabel")
            .data(swimLaneYAxis).enter().append("svg:text")
            .attr("id", function(d){return "ylabel"+swimLaneYAxis.indexOf(d)})
            .text(function(d){return d})
            .on("mouseover",function(d){
                    d3.select(this)
                        .transition()
                        .attr("font-size", defaultYLabelSize*3)
                        .attr("fill", colorScale(d))

                    d3.selectAll("path.node").each(function(node){
                        if(node.yvalue == d){
                            d3.select("#node"+ node.id)
                                .transition()
                                .attr("d", d3.svg.symbol()
                                    .size(function(d) {
                                        if( d[nodeMeasureExplorationContext] != null && d[nodeMeasureExplorationContext]!=undefined){
                                            var measure = parseFloat(d[nodeMeasureExplorationContext])
                                            var radius = nodeRadiusScale(measure);
                                            return radius;
                                        }
                                        else return defaultNodeScale*4;
                                    })
                                    .type(function(d) { return d3.svg.symbolTypes[d.type] ;
                                    }))
                                
                            d3.select("#xlabel"+swimLaneXAxis.indexOf(node.xvalue))
                                .transition()
                                .attr("font-size", defaultXLabelSize*3)
                                .attr("fill", "#1f77b4")
                        }
                    })
                })
                .on("mouseout",function(){
                    d3.select(this)
                        .transition()
                        .attr("font-size", defaultYLabelSize)
                        .attr("fill", "black")


                    d3.selectAll("path.node")
                        .transition()
                        .attr("d", d3.svg.symbol()
                        .size(function(d) {
                            if( d[nodeMeasureExplorationContext] != null && d[nodeMeasureExplorationContext]!=undefined){
                                var measure = parseFloat(d[nodeMeasureExplorationContext])
                                var radius = nodeRadiusScale(measure);
                                return radius;
                            }
                            else return defaultNodeScale;
                        })
                        .type(function(d) { return d3.svg.symbolTypes[d.type] ;
                        }))


                d3.selectAll(".xlabel")
                        .transition()
                        .attr("font-size", defaultXLabelSize)
                        .attr("fill", "black")

                })
            .attr("class","ylabel")
            .attr("text-anchor", "end")
            .attr("fill", "black")
            .attr("font-size",defaultYLabelSize)
            .attr("x", function() {return swimLaneYAxisMargin - (swimLaneYAxisMargin * 0.02)})
            .attr("y", function() {return 0})
            .transition()
            .duration(1000)
            .attr("transform",function(d,i){return "translate("+0 +","+ ((yDelta*(i)) + (yDelta/2))+")"} )


        //insert the x axis elements
        var xAxisLabel = svg.selectAll("text.xlabel")
            .data(swimLaneXAxis).enter().append("svg:text")
            .attr("id", function(d){return "xlabel"+swimLaneXAxis.indexOf(d)})
            .text(function(d){ return d; })
            .on("mouseover",function(d){
                    d3.select(this)
                        .transition()
                        .attr("font-size", defaultXLabelSize*3)
                        .attr("fill", "#1f77b4")

                    d3.selectAll("path.node").each(function(node){
                        if(node.xvalue == d){
                            d3.select("#node"+ node.id)
                                .transition()
                                .attr("d", d3.svg.symbol()
                                    .size(function(d) {
                                        if( d[nodeMeasureExplorationContext] != null && d[nodeMeasureExplorationContext]!=undefined){
                                            var measure = parseFloat(d[nodeMeasureExplorationContext])
                                            var radius = nodeRadiusScale(measure);
                                            return radius;
                                        }
                                        else return defaultNodeScale*4;
                                    })
                                    .type(function(d) { return d3.svg.symbolTypes[d.type] ;
                                    }))

                            d3.select("#ylabel"+swimLaneYAxis.indexOf(node.yvalue))
                                .transition()
                                .attr("font-size", defaultYLabelSize*3)
                                .attr("fill", colorScale(node.yvalue))
                        }
                    })
                })
                .on("mouseout",function(){
                    d3.select(this)
                        .transition()
                        .attr("font-size", defaultXLabelSize)
                        .attr("fill", "black")

                    d3.selectAll("path.node")
                        .transition()
                        .attr("d", d3.svg.symbol()
                            .size(function(d) {
                                if( d[nodeMeasureExplorationContext] != null && d[nodeMeasureExplorationContext]!=undefined){
                                    var measure = parseFloat(d[nodeMeasureExplorationContext])
                                    var radius = nodeRadiusScale(measure);
                                    return radius;
                                }
                                else return defaultNodeScale;
                            })
                            .type(function(d) { return d3.svg.symbolTypes[d.type] ;
                            }))

                    d3.selectAll(".ylabel")
                        .transition()
                        .attr("font-size", defaultYLabelSize)
                        .attr("fill", "black")
                })
            .attr("y", swimLaneXAxisMargin)
            .attr("x", swimLaneYAxisMargin)
            .attr("class","xlabel")
            .attr("text-anchor", "end")
            .attr("fill", "black")
            .attr("font-size",defaultXLabelSize)
            .attr("transform",function(){return "rotate(270,"+d3.select(this).attr("x")+","+d3.select(this).attr("y")+")"} )
            .transition()
            .duration(1000)
            .attr("transform",function(d,i){ return "translate("+ (( (xDelta*(i)))+(xDelta/2)) +","+ 0 +")" + "rotate(270,"+d3.select(this).attr("x")+","+d3.select(this).attr("y")+")"})

        // draw the edges
           var edge = svg.selectAll("line.edge")
                .data(edgeset).enter().append("svg:line")
                .attr("id", function(d) { return "edge"+ d.id;} )
                .attr("class","edge")
                .attr("x1", function(d){
                    var i = swimLaneXAxis.indexOf(d.source.xvalue);
                    var position = xDelta*i;
                    var finalPosition = position + (xDelta/2) +swimLaneYAxisMargin;
                    return finalPosition;
                })
                .attr("y1", function(d){
                    var i = swimLaneYAxis.indexOf(d.source.yvalue);
                    var position = yDelta*i;
                    var finalPosition = position + (yDelta/2);
                    return finalPosition;
                })
                 .attr("x2", function(d){
                    var i = swimLaneXAxis.indexOf(d.target.xvalue);
                    var position = xDelta*i;
                    var finalPosition = position + (xDelta/2)  +swimLaneYAxisMargin;
                    return finalPosition;

                })
                 .attr("y2", function(d){
                    var i = swimLaneYAxis.indexOf(d.target.yvalue);
                    var position = yDelta*i;
                    var finalPosition = position + (yDelta/2);
                    return finalPosition;
                })
                .attr("opacity", 0)
                .attr("stroke", function(d){
                    if(d.mp!=undefined && d.mp.toString().toLowerCase()=="true")
                    return "red";
                    else return "black";
                })
                 .attr("stroke-width", function(d){
                	if(d.mp!=undefined && d.mp.toString().toLowerCase()=="true")
                        return 2;
                    else return 1;
                })
                .attr("marker-end",function(){
                    if(directed)
                        return "url(#endMarker)";
                })


        var node = svg.selectAll("path.node")
            .data(nodeset).enter().append("svg:path")
            .attr("d", d3.svg.symbol()
                .size(function(d) {
                    if( d[nodeMeasureExplorationContext] != null && d[nodeMeasureExplorationContext]!=undefined){
                        var measure = parseFloat(d[nodeMeasureExplorationContext])
                        var radius = nodeRadiusScale(measure);
                        return radius;
                    }
                    else return defaultNodeScale;
                })
                .type(function(d) { return d3.svg.symbolTypes[d.type] ;
                }))
            .attr("id", function (d) { return "node"+d.id; } )
            .attr("class", "node")
            .on("click",function(d){
                   showNodeRelations(d);
                })
                .on("mouseover",function(d){
                    var opacity = d3.select(this).attr("opacity");

                    var x  = (xDelta * swimLaneXAxis.indexOf(d.xvalue)) + (xDelta/2) +swimLaneYAxisMargin;
                    var y  = (yDelta * swimLaneYAxis.indexOf(d.yvalue)) + (yDelta/2);

                    if(opacity==1){
                        d3.select("#ylabel"+swimLaneYAxis.indexOf(d.yvalue))
                            .transition()
                            .attr("font-size", defaultYLabelSize*3)
                            .attr("fill", colorScale(d.yvalue))

                        d3.select("#xlabel"+swimLaneXAxis.indexOf(d.xvalue))
                            .transition()
                            .attr("font-size", defaultXLabelSize*3)
                            .attr("fill", "#1f77b4")
                    }
                    displayNodeInformation(d)
                    svg.selectAll("text.tooltip")
                        .data([d.text])
                        .enter().append("svg:text")
                        .attr("class","tooltip")
                        .attr("y", y-50)
                        .attr("x", x)
                        .text(function(d){
                        	if(d!=undefined)
                        	return "["+d+"]"}
                        )
                        .attr("font-size",defaultLabelSize*2);

                   })
                .on("mouseout",function(d){
                    removeNodeInfo(d);
                d3.selectAll(".tooltip").remove();
                    var opacity =d3.select(this).attr("opacity");
                    if(opacity==1){
                        d3.selectAll(".ylabel")
                            .transition()
                            .attr("font-size", defaultYLabelSize)
                            .attr("fill", "black")

                        d3.selectAll(".xlabel")
                            .transition()
                            .attr("font-size", defaultXLabelSize)
                            .attr("fill", "black")
                    }
                })
                .attr("stroke", "black")
                .attr("stroke-width", 2.0)
                .attr("opacity",1.0)
                .attr("fill", function(d){return colorScale(d.yvalue);
                	 })
                .attr("r", function(d){
                 if( d[nodeMeasureExplorationContext] != null && d[nodeMeasureExplorationContext]!=undefined){
                	 var measure = parseFloat(d[nodeMeasureExplorationContext])
                	 var radius = nodeRadiusScale(measure);
                    return radius;
                 }
                else return defaultNodeScale;
                })
                .transition()
                .duration(1000)
                .attr("transform",function(d){return "translate("+((xDelta * swimLaneXAxis.indexOf(d.xvalue))+(xDelta/2) +swimLaneYAxisMargin) +","+  ((yDelta * swimLaneYAxis.indexOf(d.yvalue)) + (yDelta/2)) +")"} );


        d3.selectAll(".edge")
        .transition()
        .duration(4000)
        .attr("opacity",function(d){
            if(d[edgeMeasureExplorationContext]!=null && d[edgeMeasureExplorationContext]!=undefined){
                var measure = parseFloat(d[edgeMeasureExplorationContext]);
                var alpha = edgeAlphaScale(measure);
                return alpha;
            }else {
                return defaultEdgeAlpha;
            }
        });
            
    } else {
        alert("Warinig: The network information does not contains the \"category\" tag for generating the swim lanes. ");
    }
}
