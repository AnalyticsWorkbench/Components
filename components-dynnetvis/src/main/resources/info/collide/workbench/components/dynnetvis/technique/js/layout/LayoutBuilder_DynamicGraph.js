/**
 * @author Henrik Detjen
 */

/**
 * Holds all default values concerning the layouters for a dynamic graph
 */
GRAVIS3D.Layout.Defaults.dynamicGraphLayoutBuilder = {
		supergraph : {
			// ...
		}
};

/**
 * @nameSpace
 * @nameSpace
 */
GRAVIS3D.Layout.DynamicGraphLayoutBuilder = {};

/**
 * 
 * @param { graph: {@link: GRAVIS3D.Model.DynamicGraph}, subLayouter: {@link GRAVIS3D.Layout.GraphLayoutBuilder} } params
 * @returns {graphId: {GRAVIS3D.GraphLayout.Layout}}
 */
GRAVIS3D.Layout.DynamicGraphLayoutBuilder.Supergraph = function( params ) {

//	if ( !params || !( params.graph instanceof GRAVIS3D.Model.DynamicGraph ) ) throw new Error(
//			"TimeSlice Layouter: params.graph must be a GRAVIS3D.Model.DynamicGraph..." );
	var dynGraph = params.graph;

	// get graphs
	var graphs = dynGraph.getOrderedGraphs();
	var result = {};

	// calculate supergraph
	var supergraph = GRAVIS3D.Model.Tools.calculateSupergraph( graphs );
	var layout;

	if ( params.subLayouter ) {
		layout = params.subLayouter( {
			graph : supergraph
		} );
	} else {
		layout = GRAVIS3D.Layout.Defaults.graphLayoutBuilder.basic( {
			graph : supergraph
		} );
	}

	// use on supergraph-layout for all graphs in dynamic graph
	for ( var i = 0; i < graphs.length; i++ ) {
		result[graphs[i].getId()] = layout;
	}

	return result;


};
