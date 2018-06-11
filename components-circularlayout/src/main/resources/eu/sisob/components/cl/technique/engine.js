function VisualizationEngine(framework){

    var radius = 200;
    
    this.initialize = function(){
        initLayoutOptions();
    };

    this.start = function(callback){
    	draw();
    	if(callback != null){
    		callback.call();
    	}
    };
    
    function initLayoutOptions(){
    	    	
    	$('#customoptions').append('<h3>Circular Layout</h3>');
    	$('#customoptions').append('<hr />');
    	$('#customoptions').append('<input id="updatelayout1" value="Update"></input>');
    	$('#updatelayout1').button();
    	$('#updatelayout1').on("click", function(){
    		updateLayoutOptions();
    	});
    	$('#customoptions').append('<hr />');
    	$('#customoptions').append('<h3>Radius</h3>');
    	$('#customoptions').append('<input id="radius"></input>');
    	$('#radius').textinput();
    	$('#radius').val(radius);
    	$('#radius').keyup(function(e) {
			if(e.keyCode == 13) {updateLayoutOptions();}
    	});
    	
    
    };

    function updateLayoutOptions(){
        var input = $('#radius').val();
    	radius = parseInt(input);
    	framework.requestUpdate();
        draw();
    };

    function draw (){
        //request the canvas
        var canvas = framework.requestCanvas();

        //defines the drag event
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

        var drag = d3.behavior.drag()
            .origin(function(d) {return d;})
            .on('dragstart', dragstart)
            .on('drag', dragmove)
            .on('dragend', dragend);

        //the update layout function
        function updateLayout(source){
            d3.select('#'+framework.requestNodeClassNotation()+source.id).attr('transform', function(d) {
                return 'translate(' + d.x + ',' + d.y + ')';
            });

            d3.select('#'+framework.requestLabelClassNotation()+source.id).attr('transform', function(d) {
                return "translate("+ d.x+","+ (d.y-( framework.requestLabelSize() ))+")";
            });

            edgeset.each(function(edge){
                if(edge.source.id == source.id){
                    d3.select(this).attr("x1",function(){edge.source.x = source.x; edge.source.fixed=true; return source.x;})
                    d3.select(this).attr("y1",function(){edge.source.y = source.y; return source.y;})
                }else if(edge.target.id == source.id){
                    d3.select(this).attr("x2",function(){edge.target.x = source.x; return source.x;})
                    d3.select(this).attr("y2",function(){edge.target.y = source.y; edge.target.fixed=true;return source.y;})
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
            .attr("id", function (d) { return framework.requestEdgeClassNotation()+d.id; } )
            .attr("class", function(){return framework.requestEdgeClassNotation()})
            .attr('stroke', function(d){return framework.requestEdgeColor(d)})
            .attr('opacity',function(edge){return framework.requestEdgeAlpha(edge)})
            .attr('stroke-width', function(edge){return framework.requestEdgeSize(edge)})
            .attr("x1", function(d){
                if(d.source.fixed==undefined){
                    var angle = 2*Math.PI*(framework.requestNodeIndex(d.source.id))/framework.requestNodeSet().length;
                    d.source.x = (framework.requestCanvasWidth()/2)+(Math.sin(angle))*(radius);
                }
                return d.source.x;
            })
            .attr("x2", function(d){
                if(d.target.fixed==undefined){
                    var angle = 2*Math.PI*(framework.requestNodeIndex(d.target.id))/framework.requestNodeSet().length;
                    d.target.x =(framework.requestCanvasWidth()/2)+(Math.sin(angle))*(radius);
                }
                return d.target.x;
            })
            .attr("y1", function(d){
                if(d.source.fixed==undefined){
                    var angle = 2*Math.PI*(framework.requestNodeIndex(d.source.id))/framework.requestNodeSet().length;
                    d.source.y = (framework.requestCanvasHeight()/2) -(Math.cos(angle))*(radius);
                }
                return d.source.y;
            })
            .attr("y2", function(d){
                if(d.target.fixed==undefined){
                    var angle = 2*Math.PI*(framework.requestNodeIndex(d.target.id))/framework.requestNodeSet().length;
                    d.target.y = (framework.requestCanvasHeight()/2)-(Math.cos(angle))*(radius);
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
            .attr('text-anchor',function(d){
                var angle = 2*Math.PI*(framework.requestNodeIndex(d.id))/framework.requestNodeSet().length;
                if(angle*180/Math.PI>180){return 'end'}
            })
            .text(function(d) { return d.label; })
            .attr("transform", function(d){
                if(d.fixed==undefined){
                    var angle = 2*Math.PI*(framework.requestNodeIndex(d.id))/framework.requestNodeSet().length;
                    d.x = (framework.requestCanvasWidth()/2)+ (Math.sin(angle)* (radius));
                    d.y = (framework.requestCanvasHeight()/2)- (Math.cos(angle)* (radius));
                }
                return "translate("+ d.x+","+ (d.y-framework.requestLabelSize())+")";
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
                        var angle = 2*Math.PI*(framework.requestNodeIndex(d.id))/framework.requestNodeSet().length;
                        d.x = (framework.requestCanvasWidth()/2)+ (Math.sin(angle)* (radius));
                        d.y = (framework.requestCanvasHeight()/2)- (Math.cos(angle)* (radius));
                    }
                    return "translate("+ d.x+","+ d.y+")";
                    })
                .on('click',function(node){framework.requestScopeSearch(node)})
                .on('mouseover',function(node){framework.requestNodeInformation(node)})
                .on('mouseout',function(node){framework.requestNodeInformationRemoval(node)})
                .call(drag);

    };

}