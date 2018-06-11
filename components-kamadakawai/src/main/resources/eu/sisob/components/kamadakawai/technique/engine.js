function VisualizationEngine(framework){

    this.initialize = function(){
    	initLayoutOptions();
    };

    this.start = function(callback){
    	draw();
    	if(callback!=null){
    		callback.call();
    	}
    };

    function initLayoutOptions(){
        var customoptions = $('#customoptions');
            customoptions.append('<h3>Kamada-Kawai</h3><hr /><p>No Options available.</p>');
    };

    function updateLayoutOptions(){
      
    };

    function redraw(){
        updateLayoutOptions();
        framework.requestUpdate();
        draw();
    };

    function draw (){
        //request the canvas
        var canvas = framework.requestCanvas();

        //defines the drag event
        //define the node drag event
        var drag = d3.behavior.drag()
            .origin(function(d) {return d;})
            .on('dragstart', dragstart)
            .on('drag', dragmove)
            .on('dragend', dragend);

        function dragstart(source) {
            d3.event.sourceEvent.stopPropagation();
            d3.select(this).classed('dragging', true);
        };

        function dragmove(source) {
            source.x += d3.event.dx;
            source.y += d3.event.dy;
            updateLayout(source);
        };

        function dragend(source) {
            d3.select(this).classed('dragging', true);
            source.fixed = true;
            updateLayout(source);
        };

        //the update layout function
        function updateLayout(source){

            d3.select('#'+framework.requestNodeClassNotation()+source.id).attr('transform', function(d) {
                return 'translate(' + d.x + ',' + d.y + ')';
            });

            d3.select('#'+framework.requestLabelClassNotation()+source.id).attr('transform', function(d) {
                return "translate("+ d.x+","+ (parseFloat(d.y)-framework.requestLabelSize())+")";
            });

            edgeset.each(function(edge){
                if(edge.source.id == source.id){
                    d3.select(this).attr("x1",function(){edge.source.x = source.x; edge.source.fixed=true; return source.x;})
                    d3.select(this).attr("y1",function(){edge.source.y = source.y; return source.y;})
                }else if(edge.target.id == source.id){
                    d3.select(this).attr("x2",function(){edge.target.x = source.x; return source.x;})
                    d3.select(this).attr("y2",function(){edge.target.y = source.y; edge.target.fixed=true; return source.y;})
                }
            });

        };

        canvas.append('svg:defs').selectAll("marker")
            .data(["arrow"])
            .enter().append('svg:marker')
            .attr('id', 'endMarker')
            .attr('viewBox', '0 -5 10 10')
            .attr('refX', 10)
            .attr('refY', 0)
            .attr('markerWidth', 8)
            .attr('markerHeight', 8)
            .attr('orient', 'auto')
            .append('svg:path')
            .attr('d', 'M0,-5L10,0L0,5')

        //define how to paint the edges
        var edgeset = canvas.selectAll(framework.requestEdgeNotation())
            .data(framework.requestEdgeSet())
            .enter().append('svg:line')
            .attr("id", function (d) {return framework.requestEdgeClassNotation()+d.id; } )
            .attr("class", function(){return framework.requestEdgeClassNotation()})
            .attr('stroke', function(d){return framework.requestEdgeColor(d)})
            .attr('opacity',function(edge){return framework.requestEdgeAlpha(edge)})
            .attr('stroke-width', function(edge){return framework.requestEdgeSize(edge)})
            .attr("x1", function(d){
                if(d.source.fixed==undefined){
                    d.source.x = parseFloat(d.source.coordinates[0]);
                }
                return d.source.x;
            })
            .attr("x2", function(d){
                if(d.target.fixed==undefined){
                    d.target.x = parseFloat(d.target.coordinates[0]);
                }
                return d.target.x;
            })
            .attr("y1", function(d){
                if(d.source.fixed==undefined){
                    d.source.y = parseFloat(d.source.coordinates[1]);
                }
                return d.source.y;
            })
            .attr("y2", function(d){
                if(d.target.fixed==undefined){
                    d.target.y = parseFloat(d.target.coordinates[1]);
                }
                return d.target.y;
            })
            .attr("marker-end", function() {
                if(framework.requestNetworkDirection()==true)
                    return "url(#endMarker)";
            });

        //define the labels
        var labels = canvas.selectAll(framework.requestLabelNotation())
            .data(framework.requestNodeSet())
            .enter().append('svg:text')
            .attr("id", function (d) { return framework.requestLabelClassNotation()+d.id; } )
            .attr("class",function(){return framework.requestLabelClassNotation()})
            .attr('opacity',function(label){return framework.requestLabelAlpha(label)})
            .attr('font-size',framework.requestLabelSize())
            .attr("text-anchor","middle")
            .text(function(d) { return d.label; })
            .attr("transform", function(d){
                if(d.fixed==undefined){
                    d.x = parseFloat(d.coordinates[0]);
                    d.y = parseFloat(d.coordinates[1]);
                }
                return "translate("+ d.x+","+ (parseFloat(d.y)-10)+")";
            });


        //define how to paint the nodes
        var nodeset = canvas.selectAll(framework.requestNodeNotation())
                .data(framework.requestNodeSet())
                .enter().append('svg:path')
                .attr("id", function (d) { return framework.requestNodeClassNotation()+d.id; } )
                .attr("class", function(){return framework.requestNodeClassNotation()})
                .attr('d', d3.svg.symbol()
                    .size(function(d) { return framework.requestNodeSize(d);})
                    .type(function(d) { return framework.requestNodeSymbol(d)}))
                .attr('fill',function(d){return framework.requestNodeColor(d);})
                .attr('opacity',function(node){return framework.requestNodeAlpha(node)})
                .attr('stroke', 'black')
                .attr('stroke-width', '0.5')
                .attr("transform", function(d){
                    if(d.fixed==undefined){
                        d.x = parseFloat(d.coordinates[0]);
                        d.y = parseFloat(d.coordinates[1]);
                    }
                    return "translate("+ d.x+","+ d.y+")";
                    })
                .on('click',function(node){framework.requestScopeSearch(node)})
                .on('mouseover',function(node){framework.requestNodeInformation(node)})
                .on('mouseout',function(node){framework.requestNodeInformationRemoval(node)})
                .call(drag);
    };

}