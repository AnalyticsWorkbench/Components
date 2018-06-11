/**
 * @author Henrik Detjen
 */

/**
 * every Graph Layouter has a {@link GRAIVS3D.Model.Graph} as an input und returns a {@link GRAVIS3D.Layout.GraphLayout}
 * 
 * @namespace GraphLayoutBuilder
 * @nameSpace GraphLayoutBuilder
 */
GRAVIS3D.Layout.GraphLayoutBuilder = {};


/**
 * Holds all default values concerning the layouters for a graph
 */
GRAVIS3D.Layout.Defaults.graphLayoutBuilder = {
	random : {
		distance : 1
	},
	forceDirected2d : {
		edgeDistance : 2,
		repulsion : 2,
		gravity : 0.9,
		distance_decay : 0.7
	}
};

GRAVIS3D.Layout.GraphLayoutBuilder.ForceDirected2D = function( params ) {

	if ( !params || !( params.graph instanceof GRAVIS3D.Model.Graph ) ) throw new Error(
			"FDG Layouter: params.graph must be a GRAVIS3D.Model.Graph... (Missing?)" );
	var graph = params.graph;

	var useNodeWeight = null;
	var useEdgeWeight = null;
	var edgeDistance = GRAVIS3D.Layout.Defaults.graphLayoutBuilder.forceDirected2d.edgeDistance;
	var repulsion = GRAVIS3D.Layout.Defaults.graphLayoutBuilder.forceDirected2d.repulsion;
	var gravity = GRAVIS3D.Layout.Defaults.graphLayoutBuilder.forceDirected2d.gravity;
	var distance_decay = GRAVIS3D.Layout.Defaults.graphLayoutBuilder.forceDirected2d.distance_decay;
	if ( params.useNodeWeight ) useNodeWeight = params.useNodeWeight;
	if ( params.useEdgeWeight ) useEdgeWeight = params.useEdgeWeight;
	if ( params.edgeDistance ) edgeDistance = params.edgeDistance;
	if ( params.repulsion ) repulsion = params.repulsion;
	if ( params.gravity ) gravity = params.gravity;
	if ( params.distance_decay ) distance_decay = params.distance_decay;

	var layout = new GRAVIS3D.Layout.GraphLayout();

	var nodes = graph.getNodes();
	var nodePosInArray = {};
	var count = 0;
	var d3nodes = [];
	var maxWeight = 1;//needed to normalize edge weights (must be 0-1 in d3)
	for ( id in nodes ) {
		var weight = nodes[id].getWeight();
		if ( useNodeWeight != null && nodes[id].getData( useNodeWeight ) ) weight = nodes[id].getData( useNodeWeight );
		d3nodes.push( {
			x : 0,
			y : 0,
			id : id,
			weight : weight
		} );
		nodePosInArray[id] = count;
		count++;
	}
	var edges = graph.getEdges();
	var d3edges = [];
	for ( id in edges ) {
		var weight = edges[id].getWeight();
		if ( useEdgeWeight != null && edges[id].getData( useEdgeWeight ) ) weight = edges[id].getData( useEdgeWeight );
		if ( weight > maxWeight ) weight = maxWeight;
		d3edges.push( {
			source : nodePosInArray[edges[id].getSource().getId()],
			target : nodePosInArray[edges[id].getTarget().getId()],
			weight : weight
		} );
	}
	var width = 1;
	var height = 1;
	var force = d3.layout.force().nodes( d3nodes ).links( d3edges ).linkDistance( edgeDistance ).linkStrength(
			function( link ) {
				return link.weight/maxWeight;
			} ).charge( function( node ) { // repulsion of a node (negative values cause attraction)
		return -1 * repulsion + -1 * node.weight;
	} ).size( [ width, height ] ).gravity( gravity ).start();

	var n = 100 + d3nodes.length * Math.pow( distance_decay, d3nodes.length );
	if ( n > 1000 ) n = 1000;
	for ( var i = 0; i < n; ++i )
		force.tick();
	force.stop();

	//assign positions
	for ( var i = 0; i < d3nodes.length; i++ ) {
		var v = new GRAVIS3D.Vector3D( ( d3nodes[i].x - ( width / 2 ) ), ( d3nodes[i].y - ( height / 2 ) ), 0 );
		layout.addPosition( d3nodes[i].id, v );
	}

	return layout;

};

GRAVIS3D.Layout.GraphLayoutBuilder.RandomLayouter2D = function( params ) {

	if ( !params || !( params.graph instanceof GRAVIS3D.Model.Graph ) ) throw new Error(
			"Random Layouter: graph must be a GRAVIS3D.Model.Graph..." );
	var graph = params.graph;

	var distance = 2;//GRAVIS3D.Layout.Defaults.builder.random.distance;
	if ( params.distance ) distance = params.distance;

	var layout = new GRAVIS3D.Layout.GraphLayout();

	var nodes = graph.getNodes();
	var length = Object.keys( nodes ).length;
	var ld = length * distance;
	var x_max = Math.sqrt( ld ) * 2;
	var y_max = Math.sqrt( ld ) * 2;
	var occupied = [];

	for ( id in nodes ) {
		var asd = [ -1, 1 ];
		var x = parseInt( Math.random() * x_max * asd[parseInt( Math.random() * 2 )] );
		var y = parseInt( Math.random() * y_max * asd[parseInt( Math.random() * 2 )] );
		var rect = new GRAVIS3D.Rectangle( new GRAVIS3D.Vector2D( x, y ), distance, distance );
		while ( !isValidPos( rect ) ) {
			x = parseInt( Math.random() * x_max * asd[parseInt( Math.random() * 2 )] );
			y = parseInt( Math.random() * y_max * asd[parseInt( Math.random() * 2 )] );
			rect.setStartPoint( new GRAVIS3D.Vector2D( x, y ) );
		}
		occupied.push( rect );
		layout.addPosition( id, new GRAVIS3D.Vector3D( ( x + ( distance / 2 ) ), ( y + ( distance / 2 ) ), 0 ) );
	}

	function isValidPos( rect ) {
		for ( var i = 0; i < occupied.length; i++ ) {
			if ( occupied[i].intersects( rect ) ) { return false; }
		}
		return true;
	}

	return layout;

};



