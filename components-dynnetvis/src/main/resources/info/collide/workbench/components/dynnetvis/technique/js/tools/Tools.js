/**
 * @author Henrik Detjen
 */

//////////////////////////////////////////////////
// useful tools + functions can be defined here //
//////////////////////////////////////////////////
GRAVIS3D.Model.Tools = {};

/**
 * @param {@link GRAVIS3D.Model.Graph[]} graphs 
 * @return {@link GRAVIS3D.Model.Graph}
 */
GRAVIS3D.Model.Tools.calculateSupergraph = function( graphs ) {
	
	var sg = new GRAVIS3D.Model.Graph();
	var l = graphs.length;
	
	for ( var i = 0; i < l; i++ ) {
		
		var attributes = graphs[i].getAttributes();
		for ( id in attributes ) {
			sg.addAttribute( attributes[id] );
		}
		
		var nodes = graphs[i].getNodes();
		for ( id in nodes ) {
			var sgNodes = sg.getNodes();
			if ( sgNodes[id] == undefined ) {
				sg.addNode( nodes[id] );
				sgNodes[id].setData( "timeCount", 1 );
			}
			sgNodes[id].setWeight( sgNodes[id].getWeight() + nodes[id].getWeight() );
			sgNodes[id].setData( "timeCount", sgNodes[id].getData( "timeCount" ) + 1 );
		}
		
		var edges = graphs[i].getEdges();
		for ( id in edges ) {
			var sgEdges = sg.getEdges();
			if ( sgEdges[id] == undefined ) {
				sg.addEdge( edges[id] );
				sgEdges[id].setData( "timeCount", 1 );
			}
			sgEdges[id].setWeight( sgEdges[id].getWeight() + edges[id].getWeight() );
			sgEdges[id].setData( "timeCount", sgEdges[id].getData( "timeCount" ) + 1 );
		}
		
	}
	
	var sgNodes = sg.getNodes();
	for (id in sgNodes){
		sgNodes[id].setWeight( sgNodes[id].getWeight() / sgNodes[id].getData("timeCount") );
	}
	var sgEdges = sg.getEdges();
	for (id in sgEdges){
		sgEdges[id].setWeight( sgEdges[id].getWeight() / sgEdges[id].getData("timeCount") );
	}
	
	return sg;
};

GRAVIS3D.Model.Tools.createRandomGraph = function( params ) {
	var p = {};
	if ( params.id ) p.id = params.id;
	var graph = new GRAVIS3D.Model.Graph( p );
	for ( var i = 0; i < params.numberOfNodes; i++ ) {
		var nweight = 1;
		if ( params.weights == true ) nweight = Math.random();
		var node = new GRAVIS3D.Model.Node( {
			name : i.toString(),
			weight : nweight
		} );
		if ( params.runningIds ) node.setId( i.toString() );
		var nodes = graph.getNodes();
		for ( var j in nodes ) {
			if ( Math.random() <= params.connectionPossibility ) {
				var target = nodes[j];
				var name = "from " + node.getName() + " to " + target.getName();
				var eweight = 1;
				if ( params.weights == true ) eweight = Math.random();
				var edge = new GRAVIS3D.Model.Edge( {
					source : node,
					target : target,
					name : name,
					weight : eweight
				} );
				graph.addEdge( edge );
			}
		}
		graph.addNode( node );
	}
	return graph;
};

GRAVIS3D.Model.Tools.createScaleFreeGraph = function( numberOfNodes, connectionCoefficient ) {

};
