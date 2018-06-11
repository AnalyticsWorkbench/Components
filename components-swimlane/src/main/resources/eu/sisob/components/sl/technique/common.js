// the network title
var title;
// the network description
var description;
// is the network directed
var directed;
// the node measures
var nodeMeasures;
//the edge measures
var edgeMeasures;
//the network data links
var datalinks;
// the node properties
var nodeproperties;
//the edge properties
var edgeproperties;
// the node set
var nodeset;
// the edge set
var edgeset;
//axis elements default size
var defaultXLabelSize;
var defaultYLabelSize;
var defaultLabelSize;

//time navegation variables
var timeappearances;
var currentTime;
var dataAppearanceIndex = 0;

//the network color scale
var colorScale=d3.scale.category10();

//node default radius
var defaultNodeScale = 300;
//edge size value
var defaultEdgeAlpha = 1.0

//the graphExplorationContext context
var graphExplorationContext= 0;
var trailExploration = false;

//node scaling variables
var nodeScaleKeySet;
var nodeRadiusScale;
var nodeMeasureExplorationContext;

//edge scaling variables
var edgeScaleKeySet;
var edgeAlphaScale;
var edgeMeasureExplorationContext;


//canvas width and height
var width = "3080";
var height = "2160";

// number of downloads
var downloads = 0;

//swimlane axis data
var swimLaneYAxis;
var swimLaneXAxis;

//---------------------------visualization technique start functions------------------
function start(){
    loadData("0.json");
}

function loadData(dataFile){

    d3.json(dataFile, function(json) {
        initialize(json.metadata,json.data);
    });
}
//---------------------------end of visualization techniques start functions

//---------------------------initialization methods----------------------------------

function initialize(metadata,data) {


    if(metadata!=null && metadata!= undefined && data!=null && data!=undefined){
        //clean the previous svg
        cleanSVG();

        // clean previous searches
        cleanSearch();

        this.title = (metadata.title!=undefined) ? metadata.title : undefined;
        this.description = (metadata.description !=undefined ) ? metadata.description : undefined;
        this.type = (metadata.type !=undefined ) ? metadata.type : undefined;
        this.directed = (metadata.directed !=undefined ) ? metadata.directed : undefined;
        this.datalinks = (metadata.datalinks !=undefined ) ? metadata.datalinks : undefined;
        this.nodeMeasures = (metadata.measures !=undefined ) ? extractMeasures(metadata.measures,"node") : undefined;
        this.edgeMeasures = (metadata.measures !=undefined ) ? extractMeasures(metadata.measures,"edge") : undefined;
        this.nodeproperties = (metadata.nodeproperties !=undefined ) ? extractNodeProperties(metadata.nodeproperties) : undefined;
        this.edgeproperties = (metadata.edgeproperties !=undefined ) ? extractEdgeProperties(metadata.edgeproperties) : undefined;
        this.nodeset = (data.nodes !=undefined ) ? data.nodes : undefined;
        this.edgeset = (data.edges !=undefined ) ? data.edges : undefined;

        // calculateTimeAppearances
        calculateNetworkTime();

        // translate the edges
        translateEdges();

        //update the user interface
        updateUI()

        //initialize search control
        initializeSearchControl();

        //init custom controls
        loadCustomControls();

        //update the measures
        updateNodeMeasureExplorationContext();
        updateEdgeMeasureExplorationContext();

        //updateAxisSize
        updateSwimLane();
        
        //create swim lane
        swimLaneYAxis = extractKeyValues(nodeset,"yvalue");
        swimLaneXAxis = extractKeyValues(nodeset,"xvalue");
        

        // draw the visualization
        redraw();


    }else {
        alert("Unable to read network data.")
    }

}

function translateEdges(){
    edgeset.forEach(
        function(edge) {
            edge.source= nodeset[locateNode(edge.source)]
            edge.target= nodeset[locateNode(edge.target)]
        }
    );
}

function locateNode(nodeID){
    for(i=0;i<nodeset.length;i++){
        if(nodeID==nodeset[i].id)
            return i;
    }
}

function updateUI(){
    // set the current time to the ui
    d3.select('#timeselector').attr("value", timeappearances[dataAppearanceIndex]) ;

    // set network description
    d3.select("#description").text("Network Description: "+ this.description );


    if(nodeMeasures!=undefined){
        if(d3.select("#nodemeasures").html().toString()==" "){
            //remove the previous node measures
            d3.select("#nodemeasures").selectAll("option").remove();
            // append the default node measure value
            d3.select('#nodemeasures').append("option")
                .attr("value","default")
                .text("None");
            //insert the measures
            nodeMeasures.forEach(function(measure){
                d3.select('#nodemeasures').append("option")
                    .attr("value",measure.property)
                    .text(measure.title);
            })
        }
    }

    if(edgeMeasures!=undefined){
        if(d3.select("#edgemeasures").html().toString()==" "){
            //remove the previous edge measures
            d3.select("#edgemeasures").selectAll("option").remove();
            // append the default edge measure value
            d3.select('#edgemeasures').append("option")
                .attr("value","default")
                .text("None");
            //insert the measures
            edgeMeasures.forEach(function(measure){
                d3.select('#edgemeasures').append("option")
                    .attr("value",measure.property)
                    .text(measure.title);
            })

        }
    }
}

function initializeSearchControl(){
    try{
        var controlSlot = d3.select("#searchcontrols");
        if(controlSlot != null && controlSlot.node() != null){
            removeAllChild(controlSlot)
            controlSlot.append("p").text("Search Value:");// label
            controlSlot.append("datalist")
                .attr("id", "idDataList")
                .selectAll("option")
                .data(nodeset.map(function (d){return d.label})).enter()   // options
                .append("option")
                .attr("value", function(d) {return d;} )
            ;

            controlSlot.append("input")
            .attr("id", "idTbSearch")
            .attr("type", "text")
            .on("keydown",function(){
                if(d3.event.keyCode==13){
                	search();	
                }                
                else if(d3.event.keyCode==27){
                	cleanSearch();	
                	 highlightNodeMeasure();
                     highlightEdgeMeasure();
                }                
               
            }).attr("list", "idDataList");       

            controlSlot.append("input")
                .attr("id", "idBtSearch")
                .attr("type", "button")
                .attr("value", "Search")
                .on("click", function() {search()})
            ;

            controlSlot.append("input")
                .attr("id", "idBtClean")
                .attr("type", "button")
                .attr("value", "Restore")
               .on("click", function() {
                    cleanSearch();
                    highlightNodeMeasure();
                    highlightEdgeMeasure();
                });            
            controlSlot.append("div")
                .attr("id", "idDivResultSearch")
                .style("height", "140px")
                .style("overflow", "auto")
                .style("border-style", "groove")
            ;
        }
    }
    catch(e){
        alert(e);
    }
}

// extracts the node properties defined in the metadata
function extractNodeProperties(nodeproperties){

}
// extract the edge properties defined in the metadata
function extractEdgeProperties(edgeproperties){

}
// extract the corresponding measures of a given class
function extractMeasures(measures,filterClass){
    var measuresKeySet = new Array();
    measures.forEach(function(measure){
        if(measure.class == filterClass){
            measuresKeySet.push(measure);
        }
    })
    return measuresKeySet;
}
//insert the node object into the corresponding edge source and edge target
function updateSwimLane(){
    this.defaultXLabelSize = d3.select("#xinput").property("value");
    this.defaultYLabelSize = d3.select("#yinput").property("value");
    this.defaultLabelSize =  d3.select("#messageinput").property("value");
    this.defaultNodeScale = d3.select("#nodeinput").property("value")
}

function updateEdgeMeasureExplorationContext(){
	  edgeMeasureExplorationContext = d3.select('#edgemeasures').property("value");
	  edgeScaleKeySet = new Array();
	  
	    edgeset.forEach(function(edge){
	        if( edge[edgeMeasureExplorationContext] != null && edge[edgeMeasureExplorationContext]!=undefined){
	            var edgeMeasure = parseFloat(edge[edgeMeasureExplorationContext]);
	            var index = edgeScaleKeySet.indexOf(edgeMeasure);
	            if(index == -1){
	                edgeScaleKeySet.push(edgeMeasure);
	            }
	        }
	    });


	    var min = d3.min(edgeScaleKeySet);
	    var max = d3.max(edgeScaleKeySet);
	    
	    if(min == max){
	    	edgeAlphaScale = d3.scale.linear().domain([min, max]).range([1,1]);
	    }else{
	    	edgeAlphaScale = d3.scale.linear().domain([min, max]).range([0,1]);	
	    }
	    
	    	    
}

function updateNodeMeasureExplorationContext(){

	 nodeMeasureExplorationContext = d3.select('#nodemeasures').property("value");
	 nodeScaleKeySet = new Array();	 

	 nodeset.forEach(function(node){
	      if(node[nodeMeasureExplorationContext]!= null && node[nodeMeasureExplorationContext]!=undefined){
	            var nodeMeasure = parseFloat(node[nodeMeasureExplorationContext]);
	            var index = nodeScaleKeySet.indexOf(nodeMeasure);
	            if(index==-1){
	                nodeScaleKeySet.push(nodeMeasure);
	            }

	        }
	    });	    
	 var min = d3.min(nodeScaleKeySet);
	 var max = d3.max(nodeScaleKeySet);	
	 
	 if(min == max){
		 nodeRadiusScale = d3.scale.linear().domain([min, max]).range([(defaultNodeScale*3),(defaultNodeScale*3)]);
	 }else{
		 nodeRadiusScale = d3.scale.linear().domain([min, max]).range([defaultNodeScale,(defaultNodeScale*3)]);
	 }
}

//---------------------------end of initialization methods----------------------------------


//---------------------------support functions--------------------------------------------

function downloadVisualization(){
    var html = d3.select("#canvas")
        .attr("title", "test2")
        .attr("version", 1.1)
        .attr("xmlns", "http://www.w3.org/2000/svg")
        .node().parentNode.innerHTML;

    d3.select("#download")
        .attr("href", 'data:application/octet-stream;base64,' + btoa(html))
        .attr("download", "screenshot"+downloads+".svg")

    downloads++;
}

function calculateNetworkTime(){
    this.timeappearances = new Array();
    datalinks.forEach(function(n){
        this.timeappearances.push(n.toString().substring(0,n.toString().indexOf(".")));

    });
    this.currentTime = timeappearances[dataAppearanceIndex];
}

function updateCurrentTimeByStep(step, onUpdateCallback) {
    var currentTimeIndex = timeappearances.indexOf(currentTime);
    currentTimeIndex += step;

    if ( currentTimeIndex > -1 && currentTimeIndex <  timeappearances.length ) {
        currentTime =  timeappearances[currentTimeIndex];
        dataAppearanceIndex = currentTimeIndex;
        onUpdateCallback();
        loadData(datalinks[currentTime].toString());
    }
}

function onCurrentTimeUpdate() {
    d3.select('#timeselector').attr("value", currentTime) ;

}

function removeAllChild(oObjectD3){
    // Remove all child
    while (oObjectD3.node().hasChildNodes()) {
        oObjectD3.node().removeChild(oObjectD3.node().firstChild);
    }
}

function switchToAlphaGraph(){
    d3.selectAll("path.node").attr("opacity",0.15);
    d3.selectAll("line.edge").attr("opacity",0.15);
    d3.selectAll("text.label").attr("opacity",0.15);
}

function switchToNonAlphaGraph(){
    d3.selectAll("path.node").attr("opacity",1.0);
    d3.selectAll("line.edge").attr("opacity",1.0);
    d3.selectAll("text.label").attr("opacity",1.0);
}

function changeGraphExplorationContext(){
    this.graphExplorationContext = d3.select("#gexplorercontext").property("value");
    this.trailExploration=false;
    if(graphExplorationContext==0){
        switchToNonAlphaGraph();
    }
}

function cleanSVG(){
    d3.select("svg").remove();
}
//---------------------------end support functions--------------------------------------------

//--------------------------search functions-------------------------------------------------


//highlights the neighbors of a node and returns the impacted elements
function highlightNeighbors(sourceNode){

  var impactedNeighbors = new Array();
  edgeset.forEach(function(edge){

      var neighbor = (sourceNode==edge.source) ? edge.target :(sourceNode==edge.target)? edge.source : undefined;
      if(neighbor!=undefined){
          var neighborOpacity = d3.select("#node"+neighbor.id).attr("opacity");
          if(neighborOpacity!=1){
              impactedNeighbors.push(neighbor)
          }
          d3.select("#node"+neighbor.id).attr("opacity",1.0);
          d3.select("#label"+neighbor.id).attr("opacity",1.0);
          if(edge.source == neighbor || edge.target==neighbor)
          d3.select("#edge"+edge.id).transition().attr("opacity",1.0);
      }

  })

  return impactedNeighbors;

}

//highlight the source node
function highlightSourceNode(sourceNode){
  d3.select("#node"+sourceNode.id).attr("opacity",1.0);
  d3.select("#label"+sourceNode.id).attr("opacity",1.0);
}

//executes the graph  exploration
function searchScope(sourceNodes, iteration){
  var impactedNodes = new Array();

  sourceNodes.forEach(function(sourcenode){
         highlightSourceNode(sourcenode);
         var selectedNodes = highlightNeighbors(sourcenode);
         selectedNodes.forEach(function(impactedNode){
             impactedNodes.push(impactedNode);
         })
  })

  iteration++;

  if(iteration<graphExplorationContext)
  searchScope(impactedNodes,iteration);

}
//displayes the current relations of a graph
function showNodeRelations(node){

  if(graphExplorationContext!=0){
      if(graphExplorationContext<4){
          switchToAlphaGraph();
          searchScope([node],0);
      }
      else if(graphExplorationContext==4){
          if(!trailExploration){
              switchToAlphaGraph();
              trailExploration = !trailExploration
          }
          searchScope([node],3);
      }
  }
}
function displayNodeInformation(node){
    //select the result div
    var divResult = d3.select("#idDivResultSearch");
    var measure = d3.select('#nodemeasures').property("value");

    //if you can select the div result and t is not empty
    if(divResult != null && divResult.node() != null){
        removeAllChild(divResult);
        var htmlContent ="Id: "+ node.id + " Label: " + node.label + " Time: " + node.timeappearance; "<br>";
        if(node[measure]!=null)
            htmlContent += " Measure:"+measure+ ": "+node[measure];
        divResult.append("p").html(htmlContent);

    }

    d3.select("#node"+ node.id)
        .transition()
        .duration(700)
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
}

function removeNodeInfo(node){
    var divResult = d3.select("#idDivResultSearch");
    var measure = d3.select('#nodemeasures').property("value");

    //if you can select the div result and t is not empty
    if(divResult != null && divResult.node() != null){
        removeAllChild(divResult);
    }

    d3.select("#node"+ node.id)
        .transition()
        .duration(700)
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
}

function search() {
// Search Data
    var keyword = d3.select("#idTbSearch").property("value").toString().trim();
    var results = nodeset.filter( function(n) { return n.label.toLowerCase().indexOf(keyword.toLowerCase()) != -1; } );
    switchToAlphaGraph();

    results.forEach(function(d){
        var r =d3.select("#node"+d.id).attr("r");
        d3.select("#node"+d.id)
            .attr("r",300)
            .transition()
            .attr("opacity",1.0)
            .transition()
            .attr("r",r)

        d3.select("#label"+ d.id)
            .transition()
            .attr("opacity",1.0);
    })
}

function cleanSearch(){
    switchToNonAlphaGraph();
    d3.select('#idTbSearch').property("value", "");
}

function highlightNodeMeasure(){
    updateNodeMeasureExplorationContext();
    d3.selectAll(".node")
        .transition()
        .duration(700)
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
            }));
}

function highlightEdgeMeasure(){
	 updateEdgeMeasureExplorationContext();

	    d3.selectAll(".edge")
	        .transition()
	        .duration(700)
	        .attr("opacity",function(d){
	            if(d[edgeMeasureExplorationContext]!=null && d[edgeMeasureExplorationContext]!=undefined){
	                var measure = parseFloat(d[edgeMeasureExplorationContext]);
	                var alpha = edgeAlphaScale(measure);
	                return alpha;
	            }else {
	                return defaultEdgeAlpha;
	            }
	        });
}

//--------------------------end of search functions-------------------------------------------------


