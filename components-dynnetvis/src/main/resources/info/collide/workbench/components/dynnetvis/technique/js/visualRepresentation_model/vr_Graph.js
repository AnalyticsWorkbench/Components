/**
 * @author Henrik Detjen
 */


/**
 * @namespace holds all classes used for basic visualization 
 * @nameSpace
 */
GRAVIS3D.VisualRepresentation = {};
/**
 * holds the default values
 * 
 * @public
 * @constant
 */
GRAVIS3D.VisualRepresentation.Defaults = {
	/**
	 * everything related to rendering defaults
	 */
	rendering : {
		/**
		 * scales the quality - i.e. used polygon count for node forms
		 * @global 
		 * @type {Number}
		 * default setting = 1
		 */
		quality : 0.5
	}
};

GRAVIS3D.VisualRepresentation.Defaults.graph = {
	/**
	 * TODO
	 */
	basicLayouter : GRAVIS3D.Layout.GraphLayoutBuilder.ForceDirected2D
};

/**
 * @summary
 * A Visual Representation of a Graph
 * 
 * @since 1.0
 * 
 * @constructor Graph
 * @param { <br>
 * 		&emsp;represents: {@link GRAVIS3D.Model.Graph}, <br>
 * 		&emsp;nodeRepresentations: {@link GRAVIS3D.VisualRepresentation.Node[]}, <br>
 * 		&emsp;edgeRepresentations: {@link GRAVIS3D.VisualRepresentation.Edge[]} <br>
 * 		&emsp;layout: {@link GRAVIS3D.Layout.GraphLayout}, <br>
 * 		&emsp;mappings: {@link GRAVIS3D.Mapping.AttributeMapping[]}, <br>
 * 		&emsp;nodeFilter: {@link GRAVIS3D.Filtering.Filter}, <br>
 * 		&emsp;edgeFilter: {@link GRAVIS3D.Filtering.Filter}, <br>
 * } params
 *
 * @example
 * TODO
 * 
 */
GRAVIS3D.VisualRepresentation.Graph = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// NODE REPRESENTATIONS
	var nodeRepresentations = {}; // { nodeRepresentationId: VisualRepresentation.Node }
	var nodeRepFor = {}; // { nodeModelId: nodeRepresentationId } - improves speed
	/**
	 * returns all node representations
	 * 
	 * @method getNodeRepresentations
	 * @return {{@link GRAVIS3D.VisualRepresentation.Node#getId}: {@link GRAVIS3D.VisualRepresentation.Node}}
	 */
	this.getNodeRepresentations = function() {
		return nodeRepresentations;
	};
	/**
	 * @see {@link GRAVIS3D.VisualRepresentation.Graph#getNodeRepresentations}
	 */
	this.getNodeRepresentationsAsArray = function() {
		var array = new Array();
		for ( id in nodeRepresentations ) {
			array.push( nodeRepresentations[id] );
		}
		return array;
	};
	/**
	 * gets a {@link GRAVIS3D.VisualRepresentation.Node} for a given id of a {@link GRAVIS3D.Model.Node#getId}
	 * 
	 * @method getNodeRepresentationByModelId
	 * @param {String} nodeModelId
	 * @return {@link GRAVIS3D.VisualRepresentation.Node}
	 */
	this.getNodeRepresentationByNodeModelId = function( nodeModelId ) {
		if ( nodeRepresentations[nodeRepFor[nodeModelId]] ) return nodeRepresentations[nodeRepFor[nodeModelId]];
		else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #getNodeRepresentationByNodeModelId: Node Model Id not found... " + JSON.stringify( nodeModelId )
				+ ". Listed: " + Object.keys( nodeRepFor ).toString() );
	};
	/**
	 * adds a node representation
	 * 
	 * @method addNodeRepresentation
	 * @param {@link GRAVIS3D.VisualRepresentation.Node} visualRepresentation_node
	 * @return {String} {@link GRAVIS3D.VisualRepresentation.Node#getId}
	 */
	this.addNodeRepresentation = function( visualRepresentation_node ) {
		if ( visualRepresentation_node instanceof GRAVIS3D.VisualRepresentation.Node ) {
			nodeRepresentations[visualRepresentation_node.getId()] = visualRepresentation_node;
			if ( visualRepresentation_node.getRepresentedModel() != null ) {
				nodeRepFor[visualRepresentation_node.getRepresentedModel().getId()] = visualRepresentation_node.getId();
			}
			return visualRepresentation_node.getId();
		} else {
			throw new Error( "Visual Representation: Graph " + this.getId()
					+ ": #addNodeRep: No proper Node Representation added... "
					+ JSON.stringify( visualRepresentation_node ) );
		}
	};
	/**
	 * adds an array of node representations (overwrite old ones)
	 * 
	 * @method setNodeRepresentations
	 * @param {@link GRAVIS3D.VisualRepresentation.Node[]} nodeRepresentationArray
	 */
	this.setNodeRepresentations = function( nodeRepresentationArray ) {
		if ( Object.prototype.toString.call( nodeRepresentationArray ) === '[object Array]' ) {
			nodeRepresentations = {};
			for ( var i = 0; i < nodeRepresentationArray.length; i++ ) {
				this.addNodeRepresentation( nodeRepresentationArray[i] );
			}
		} else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #setNodeRepresentations: No proper Node Representations added - must be an array... "
				+ JSON.stringify( nodeRepresentationArray ) );
	};
	/**
	 * adds a node representation directly from a {@link GRAVIS3D.Model.Node}
	 * 
	 * @method addNodeRepresentationByNodeModel
	 * @param {@link GRAVIS3D.Model.Node} node
	 * @return {String} {@link GRAVIS3D.VisualRepresentation.Node#getId}
	 */
	this.addNodeRepresentationByNodeModel = function( node ) {
		if ( node instanceof GRAVIS3D.Model.Node ) {
			var nodeRep = new GRAVIS3D.VisualRepresentation.Node( {
				represents : node
			} );
			return this.addNodeRepresentation( nodeRep );
		} else {
			throw new Error( "Visual Representation: Graph " + this.getId()
					+ ": #addRepByModel: No proper Node Model added... " + JSON.stringify( node ) );
		}
	};
	/**
	 * remove a node representation for a given {@link GRAVIS3D.Model.Node#getId}
	 * 
	 * @method removeNodeRepresentationByNodeModelId
	 * @param {String} nodeModelId ({@link GRAVIS3D.VisualRepresentation.Node#getId)
	 */
	this.removeNodeRepresentationByNodeModelId = function( nodeModelId ) {
		if ( nodeRepresentations[nodeRepFor[nodeModelId]] ) delete nodeRepresentations[nodeRepFor[nodeModelId]];
		else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #removeNodeRepresentationByNodeModelId: Node Model Id not found... "
				+ JSON.stringify( visualRepresentation_node ) + ". Listed: " + Object.keys( nodeRepFor ).toString() );
	};

	// EDGE REPRESENTATIONS
	var edgeRepresentations = {}; // { edgeRepresentationId: VisualRepresentation.Edge }
	var edgeRepFor = {}; // { edgeModelId: edgeRepresentationId } - improves speed
	/**
	 * get all edge representations
	 * 
	 * @method getEdgeRepresentations
	 * @return { {@link GRAVIS3D.VisualRepresentation.Edge#getId} : {@link GRAVIS3D.VisualRepresentation.Edge} }
	 */
	this.getEdgeRepresentations = function() {
		return edgeRepresentations;
	};
	/**
	 * @see {@link GRAVIS3D.VisualRepresentation.Graph#getEdgeRepresentations}
	 */
	this.getEdgeRepresentationsAsArray = function() {
		var array = new Array();
		for ( id in edgeRepresentations ) {
			array.push( edgeRepresentations[id] );
		}
		return array;
	};
	/**
	 * gets a {@link GRAVIS3D.VisualRepresentation.Edge} for a given id of a {@link GRAVIS3D.Model.Edge#getId}
	 * 
	 * @method getEdgeRepresentationByEdgeModelId
	 * @param {String} edgeModelId
	 * @return {@link GRAVIS3D.VisualRepresentation.Edge}
	 */
	this.getEdgeRepresentationByEdgeModelId = function( edgeModelId ) {
		if ( edgeRepresentations[edgeRepFor[edgeModelId]] ) return edgeRepresentations[edgeRepFor[edgeModelId]];
		else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #getEdgeRepresentationByEdgeModelId: edge Model Id not found... "
				+ JSON.stringify( visualRepresentation_edge ) + ". Listed: " + Object.keys( edgeRepFor ).toString() );
	};
	/**
	 * add an {@link GRAVIS3D.VisualRepresentation.Edge} 
	 * 
	 * @method addEdgeRepresentation
	 * @param {@link GRAVIS3D.VisualRepresentation.Edge} visualRepresentation_edge
	 * @return {@link GRAVIS3D.VisualRepresentation.Edge#getId}
	 */
	this.addEdgeRepresentation = function( visualRepresentation_edge ) {
		if ( visualRepresentation_edge instanceof GRAVIS3D.VisualRepresentation.Edge ) {
			edgeRepresentations[visualRepresentation_edge.getId()] = visualRepresentation_edge;
			if ( visualRepresentation_edge.getRepresentedModel() != null ) {
				edgeRepFor[visualRepresentation_edge.getRepresentedModel().getId()] = visualRepresentation_edge.getId();
			}
			return visualRepresentation_edge.getId();
		} else {
			throw new Error( "Visual Representation: Graph " + this.getId()
					+ ": #addEdgeRepresentation: No proper Edge Representation added... "
					+ JSON.stringify( visualRepresentation_edge ) );
		}
	};
	/**
	 * adds an array of edge representations (overwrite old ones)
	 * 
	 * @method setEdgeRepresentations
	 * @param {@link GRAVIS3D.VisualRepresentation.Edge[]} edgeRepresentationArray
	 */
	this.setEdgeRepresentations = function( edgeRepresentationArray ) {
		if ( Object.prototype.toString.call( edgeRepresentationArray ) === '[object Array]' ) {
			edgeRepresentations = {};
			for ( var i = 0; i < edgeRepresentationArray.length; i++ ) {
				this.addEdgeRepresentation( edgeRepresentationArray[i] );
			}
		} else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #setEdgeRepresentations: No proper Node Representations added - must be an array... "
				+ JSON.stringify( edgeRepresentationArray ) );
	};
	/**
	 * adds a node representation directly from a {@link GRAVIS3D.Model.Edge}
	 * 
	 * @method addEdgeRepresentationByEdgeModel
	 * @param {@link GRAVIS3D.Model.Edge} edge
	 * @return {String} {@link GRAVIS3D.VisualRepresentation.Edge#getId}
	 */
	this.addEdgeRepresentationByEdgeModel = function( edge ) {
		if ( edge instanceof GRAVIS3D.Model.Edge ) {
			var edgeRep = new GRAVIS3D.VisualRepresentation.Edge( {
				source : this.getNodeRepresentationByNodeModelId( edge.getSource().getId() ),
				target : this.getNodeRepresentationByNodeModelId( edge.getTarget().getId() ),
				represents : edge
			} );
			return this.addEdgeRepresentation( edgeRep );
		} else {
			throw new Error( "Visual Representation: Graph " + this.getId()
					+ ": #addEdgeRepresentationByEdgeModel: No proper Edge Model added... " + JSON.stringify( edge ) );
		}
	};
	/**
	 * removes an edge representation by passing its corresponding model id {@link GRAVIS3D.Model.Edge#getId} 
	 * 
	 * @method removeEdgeRepresentationFor
	 * @param {String} edgeModelId
	 */
	this.removeEdgeRepresentationByEdgeModelId = function( edgeModelId ) {
		if ( edgeRepresentations[edgeRepFor[edgeModelId]] ) delete edgeRepresentations[edgeRepFor[edgeModelId]];
		else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #removeEdgeRepresentationByEdgeModelId: - edge Model Id not found... "
				+ JSON.stringify( edgeModelId ) + ". Listed: " + Object.keys( edgeRepFor ).toString() );
	};

	// MODEL GRAPH
	var represents = null;
	/**
	 * gets the corresponding graph model (i.e. for quick access to meta data in the views)
	 *
	 *@method getRepresentedGraphModel
	 *@return {@link GRAVIS3D.Model.Graph}
	 */
	this.getRepresentedGraphModel = function() {
		return represents;
	};
	/**
	 * set the corresponding graph model (for quick access to meta data in the views)
	 *
	 *@method setRepresentedModel
	 *@param {@link GRAVIS3D.Model.GRAVIS3D.Model.Graph}
	 */
	this.setRepresentedModel = function( graphModel ) {
		if ( graphModel instanceof GRAVIS3D.Model.Graph ) {
			represents = graphModel;
			this.init( graphModel );
		} else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #setRepresentedGraphModel: param must be a Model - Graph... " + JSON.stringify( graphModel ) );
	};
	/**
	 * init @this with a given {@link GRAVIS3D.Model.Graph}
	 * 
	 * @private
	 * @method init
	 * @param {@link GRAVIS3D.Model.Graph} graph
	 */
	this.init = function( graph ) {
		if ( graph instanceof GRAVIS3D.Model.Graph ) {
			var nodes = graph.getNodes();
			var edges = graph.getEdges();
			nodeRepresentations = {};
			for ( id in nodes ) {
				this.addNodeRepresentationByNodeModel( nodes[id] );
			}
			edgeRepresentations = {};
			for ( id in edges ) {
				this.addEdgeRepresentationByEdgeModel( edges[id] );
			}
		} else {
			throw new Error( "Visual Representation: Graph " + this.getId()
					+ ": ~init: No proper Graph for initalization... " + JSON.stringify( graph ) );
		}
	};

	// FILTER
	var nodeFilter = null;
	var edgeFilter = null;
	/**
	 * returns the acutal filter
	 * 
	 * @method getFilter
	 * @param {"node" | "edge"} context 
	 * @returns {null | {@link GRAVIS3D.Filtering.Filter} }
	 */
	this.getFilter = function( context ) {
		if ( context == "edge" ) return edgeFilter;
		else if ( context == "node" ) return nodeFilter;
		else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #getFilter: param must be node / edge... (param: " + JSON.stringify( context ) + ")" );
	};
	/**
	 * returns the actual node filter / false if no one exists
	 * 
	 * @method getNodeFilter
	 * @returns {null | {@link GRAVIS3D.Filtering.Filter} }
	 */
	this.getNodeFilter = function() {
		return this.getFilter( "node" );
	};
	/**
	 * returns the actual edge filter / false if no one exists
	 * 
	 * @method getEdgeFilter
	 * @returns {null | {@link GRAVIS3D.Filtering.Filter} }
	 */
	this.getEdgeFilter = function() {
		return this.getFilter( "edge" );
	};
	/**
	 * sets and applies a filter to @this
	 * depending on filter.isFor() it is set as node or edgefilter
	 * 
	 * @method setFilter
	 * @param {@link GAVIS3D.Filtering.Filter} filter
	 */
	this.setFilter = function( filter ) {
		if ( filter instanceof GRAVIS3D.Filtering.Filter ) {
			filter.applyToVisualRepresentation( this );
			if ( filter.isFor() == "edge" ) edgeFilter = filter;
			else nodeFilter = filter;
		} else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #setFilter: param must be a Filtering - Filter... (param: " + JSON.stringify( filter ) + ")" );
	};
	/**
	 * sets the node filter
	 * 
	 * @method setNodeFilter
	 * @param {@link GAVIS3D.Filtering.Filter} filter
	 */
	this.setNodeFilter = function( filter ) {
		this.setFilter( filter );
	};
	/**
	 * sets the edge filter
	 * 
	 * @method setEdgeFilter
	 * @param {@link GAVIS3D.Filtering.Filter} filter
	 */
	this.setEdgeFilter = function( filter ) {
		this.setFilter( filter );
	};
	/**
	 * removes the nodeFilter / edgeFilter
	 * 
	 * @method removeFilter 
	 * @param {"node" | "edge"} context 
	 */
	this.removeFilter = function( context ) {
		if ( context == "edge" ) {
			if ( edgeFilter != null ) {
				edgeFilter.removeFromVisualRepresentation( this );
				edgeFilter = null;
			}
		} else if ( context == "node" ) {
			if ( nodeFilter != null ) {
				nodeFilter.removeFromVisualRepresentation( this );
				nodeFilter = null;
			}
		} else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #removeFilter: param must be node / edge... (param: " + JSON.stringify( context ) + ")" );

	};
	/**
	 * @see @this#removeFilter
	 */
	this.removeNodeFilter = function() {
		this.removeFilter( "node" );
	};
	/**
	 * @see @this#removeFilter
	 */
	this.removeEdgeFilter = function() {
		this.removeFilter( "edge" );
	};


	// MAPPINGS
	var mappings = {};
	/**
	 * returns a list of actually used mapping
	 * 
	 * @method getMappings
	 * @returns {mappingId: Mapping}  - {@link GRAVIS3D.Mapping.AttributeMapping}
	 */
	this.getMappings = function() {
		return mappings;
	};
	/**
	 * returns a Mapping with given id 
	 * 
	 * @method getMappingById
	 * @param {String} mappingId - {@link GRAVIS3D.Mapping.AttributeMapping#getId}
	 */
	this.getMappingById = function( mappingId ) {
		if ( mappings[mappingId] ) return mappings[mappingId];
		else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #getMappingById: no such mapping found... (param: " + JSON.stringify( mappingId ) + ") Listed: "
				+ Object.keys( mappings ).toString() );
	};
	/**
	 * adds and applies a {@link GRAVIS3D.Mapping.AttributeMapping} to @this
	 * 
	 * @method addMapping
	 * @param {@link GRAVIS3D.Mapping.AttributeMapping} attributeMapping
	 */
	this.addMapping = function( attributeMapping ) {
		if ( attributeMapping instanceof GRAVIS3D.Mapping.AttributeMapping ) {
			mappings[attributeMapping.getId()] = attributeMapping;
			attributeMapping.applyToVisualRepresentation( this );
		} else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #addMapping: param must be a Mapping - AttributeMapping... (param: "
				+ JSON.stringify( attributeMapping ) + ")" );
	};
	/**
	 * removes a mapping from @this
	 * 
	 * @method removeMapping
	 * @param {String} mappingId - {@link GRAVIS3D.Mapping.AttributeMapping#getId}
	 */
	this.removeMapping = function( mappingId ) {
		if ( mappings[mappingId] ) {
			var context = mappings[mappingId].isFor();
			this.resetVisualVariable( context, mappings[mappingId].getVisualVariable() );
			//			mappings[mappingId].removeFromVisualRepresentation( this ); //"cleanes" the visual variable (set back to defaults)
			delete mappings[mappingId];
		} else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #removeMapping: no such mapping found... (param: " + JSON.stringify( mappingId ) + ") Listed: "
				+ Object.keys( mappings ).toString() );
	};
	/**
	 * removes multiple mappings from @this
	 * 
	 * @method removeMapping
	 * @param {String[]} mappingIds - {@link GRAVIS3D.Mapping.AttributeMapping#getId}
	 */
	this.removeMappings = function( mappingIds ) {
		var nodeVisVarsToClean = [];
		var edgeVisVarsToClean = [];
		for ( var i = 0; i < mappingIds.length; i++ ) {
			if ( mappings[mappingIds[i]] ) {
				var context =mappings[mappingIds[i]].isFor();
				var visVar = mappings[mappingIds[i]].getVisualVariable();
				if ( context == "node" ) {
					if ( nodeVisVarsToClean.indexOf( visVar ) == -1 ) nodeVisVarsToClean.push( visVar );
				}
				if ( context == "edge" ) {
					if ( edgeVisVarsToClean.indexOf( visVar ) == -1 ) edgeVisVarsToClean.push( visVar );
				}
				delete mappings[mappingIds[i]];
			} else throw new Error( "Visual Representation: Graph " + this.getId()
					+ ": #removeMapping: no such mapping found... (param: " + JSON.stringify( mappings[mappingIds[i]] )
					+ ") Listed: " + Object.keys( mappings ).toString() );
		}
		for ( var i = 0; i < nodeVisVarsToClean.length; i++ ) {
			this.resetVisualVariable( "node", nodeVisVarsToClean[i] );
		}
		for ( var i = 0; i < edgeVisVarsToClean.length; i++ ) {
			this.resetVisualVariable( "edge", edgeVisVarsToClean[i] );
		}
	};

	// LAYOUT
	var _layout = null; // layout
	/**
	 * returns the used layout
	 * this is automatically set by <code>setLayout()<code>
	 * 
	 * @method getLayout
	 * @returns {@link GRAVIS3D.Layout.GraphLayout}
	 */
	this.getLayout = function() {
		return _layout;
	};
	/**
	 * Set all positions for node/edge representations with a {@link GRAVIS3D.Layout.GraphLayout} 
	 * 
	 * @method setLayout
	 * @param {@link GRAVIS3D.Layout.GraphLayout} layout
	 */
	this.setLayout = function( layout ) {
		if ( layout instanceof GRAVIS3D.Layout.GraphLayout ) {
			_layout = layout;
			layout.applyToVisualRepresentation( this );
		} else {
			throw new Error( "Visual Representation - Graph " + this.getId() + ": The Layout seems to be wrong... "
					+ JSON.stringify( layout ) );
		}
	};
	/**
	 * @method buildLayout
	 * @param {@link GRAVIS3D.Layout.GraphLayoutBuilder} subLayoutBuilder
	 */
	this.buildLayout = function( layoutBuilder ) {

		if ( layoutBuilder == null || layoutBuilder == undefined ) layoutBuilder = GRAVIS3D.VisualRepresentation.Defaults.graph.basicLayouter;
		var layout = layoutBuilder( {
			graph : this.getRepresentedGraphModel()
		} );
		this.setLayout( layout );

	};

	// CLEANER
	/**
	 * reset all values for a visual variable in a certain context to default
	 * 
	 * @method resetVisualVariable
	 * @param {"label"|"node"|"edge"} context
	 * @param {String} visualVariable i.e. "brightness", "size", ... (not neccessary for label reset)
	 * @example 
	 * <code>
	 * <pre>
	 * // resets the opacity for all nodes 
	 * visualRepresentation.resetVisualVariable( "node", "opacity" );
	 * 
	 * // resets all labels to node.getName()
	 * visualRepresentation.resetVisualVariable( "label" );
	 * 
	 * // resets the size of all labels
	 * visualRepresentation.resetVisualVariable( "node", "labelSize" );
	 * </pre>
	 * </code>
	 */
	this.resetVisualVariable = function( context, visualVariable ) {
		if ( context == null || visualVariable == null ) throw new Error( "Visual Representation: Graph "
				+ this.getId() + ": #resetVisualVariable: cannot clean " + JSON.stringify( visualVariable )
				+ " in context" + JSON.stringify( context ) + "... " );

		var set = null;
		if ( context == "node" ) set = this.getNodeRepresentations();
		if ( context == "edge" ) set = this.getEdgeRepresentations();
		var defaultVal = GRAVIS3D.VisualRepresentation.Defaults[context][visualVariable];
		for ( id in set ) {
			set[id].set( visualVariable, defaultVal );
		}
	};

	if ( params && params.represents ) this.setRepresentedModel( params.represents );
	if ( params && params.nodeRepresentations ) this.setNodeRepresentations( params.nodeRepresentations );
	if ( params && params.layout ) this.setLayout( params.layout );
	else {
		if ( this.getRepresentedGraphModel() != null ) {
			//			this.buildLayout();
		}
	}
	if ( params && params.layoutBuilder ) {
		this.buildLayout( params.layoutBuilder );
	}

	if ( params && params.edgeRepresentations ) this.setEdgeRepresentations( params.edgeRepresentations );
	if ( params && params.mappings ) {
		for ( var i = 0; i < params.mappings.length; i++ ) {
			this.addMapping( params.mappings[i] );
		}
	}
	if ( params && params.nodeFilter ) this.setFilter( params.nodeFilter );
	if ( params && params.edgeFilter ) this.setFilter( params.edgeFilter );

};
