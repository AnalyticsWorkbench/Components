GRAVIS3D.VisualRepresentation.Defaults.dynGraph = {
	/**
	 * TODO
	 */
	basicLayouter : GRAVIS3D.Layout.DynamicGraphLayoutBuilder.Supergraph
};


GRAVIS3D.VisualRepresentation.DynamicGraph = function( params ) {

	GRAVIS3D.InfoObject.call( this );

	// VISUAL REPRESENTATIONS

	// TODO comments, validation, errors
	var graphReps = {};
	this.getGraphRepresentations = function() {
		return graphReps;
	};
	this.getGraphRepresentationsArray = function() {
		var array = [];
		for ( id in graphReps ) {
			array.push( graphReps[id] );
		}
		return array;
	};
	this.getGraphRepresentationById = function( graphRepId ) {
		return graphReps[graphRepId];
	};
	this.getGraphRepresentationByGraphModelId = function( graphId ) {
		for ( id in graphReps ) {
			var g = graphReps[id].getRepresentedGraphModel();
			if ( g.getId() == graphId ) return g;
		}
	};
	this.addGraphRepresentation = function( graphRepresentation ) {
		graphReps[graphRepresentation.getId()] = graphRepresentation;
	};
	this.removeGraphRepresentation = function( graphRepId ) {
		delete graphReps[graphRepId];
	};

	// MODEL GRAPH

	var represents = null;
	/**
	 * gets the corresponding dynamic graph model (i.e. for quick access to meta data in the views)
	 *
	 *@method getRepresentedDynamicGraphModel
	 *@return {@link GRAVIS3D.Model.Graph}
	 */
	this.getRepresentedDynamicGraphModel = function() {
		return represents;
	};
	/**
	 * set the corresponding graph model (for quick access to meta data in the views) and initialization
	 *
	 *@method setRepresentedDynamicGraphModel
	 *@param {@link GRAVIS3D.Model.GRAVIS3D.Model.DynamicGraph}
	 */
	this.setRepresentedDynamicGraphModel = function( dynGraphModel ) {
		if ( dynGraphModel instanceof GRAVIS3D.Model.DynamicGraph ) {
			represents = dynGraphModel;
			graphReps = {};//reset acutal representations
			var graphs = represents.getOrderedGraphs();
			for ( var i = 0; i<graphs.length;i++ ) {
				var v = new GRAVIS3D.VisualRepresentation.Graph( {
					represents : graphs[i]
				} );
				this.addGraphRepresentation( v );
			}
		} else throw new Error( "Visual Representation: Graph " + this.getId()
				+ ": #setRepresentedGraphModel: param must be a Model - Graph... " + JSON.stringify( dynGraphModel ) );
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
	 * (alias for @this#getFilter) 
	 * 
	 * @method getNodeFilter
	 * @returns {null | {@link GRAVIS3D.Filtering.Filter} }
	 */
	this.getNodeFilter = function() {
		return this.getFilter( "node" );
	};
	/**
	 * returns the actual edge filter / false if no one exists
	 * (alias for @this#getFilter) 
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
			if ( filter.isFor() == "edge" ) edgeFilter = filter;
			else nodeFilter = filter;
			for ( id in graphReps ) {
				graphReps[id].setFilter( filter );
			}
		} else {
			throw new Error( "Visual Representation: DynamicGraph " + this.getId()
					+ ": #setFilter: param must be a Filtering - Filter... (param: " + JSON.stringify( filter ) + ")" );
			return;
		}
	};
	/**
	 * sets the node filter
	 * (alias for @this#setFilter)
	 * 
	 * @method setNodeFilter
	 * @param {@link GAVIS3D.Filtering.Filter} filter
	 */
	this.setNodeFilter = function( filter ) {
		this.setFilter( filter );
	};
	/**
	 * sets the edge filter
	 * (alias for @this#setFilter)
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
			for ( id in graphReps ) {
				graphReps[id].removeFilter( "edge" );
			}
			edgeFilter = null;
		} else if ( context == "node" ) {
			for ( id in graphReps ) {
				graphReps[id].removeFilter( "node" );
			}
			nodeFilter = null;
		} else {
			throw new Error( "Visual Representation: DynamicGraph " + this.getId()
					+ ": #removeFilter: param must be node / edge... (param: " + JSON.stringify( context ) + ")" );
		}
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
		else throw new Error( "Visual Representation: DynamicGraph " + this.getId()
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
			for ( id in graphReps ) {
				graphReps[id].addMapping( attributeMapping );
			}
		} else {
			throw new Error( "Visual Representation: DynamicGraph " + this.getId()
					+ ": #addMapping: param must be a Mapping - AttributeMapping... (param: "
					+ JSON.stringify( attributeMapping ) + ")" );
		}
	};
	/**
	 * removes a mapping from @this
	 * 
	 * @method removeMapping
	 * @param {String} mappingId - {@link GRAVIS3D.Mapping.AttributeMapping#getId}
	 */
	this.removeMapping = function( mappingId ) {
		if ( mappings[mappingId] ) {
			for ( id in graphReps ) {
				graphReps[id].removeMapping( mappingId );
			}
			delete mappings[mappingId];
		} else {
			throw new Error( "Visual Representation: DynamicGraph " + this.getId()
					+ ": #removeMapping: no such mapping found... (param: " + JSON.stringify( mappingId )
					+ ") Listed: " + Object.keys( mappings ).toString() );
			return;
		}
	};
	/**
	 * removes mappings from @this
	 * 
	 * @method removeMappings
	 * @param {String[]} mappingId - {@link GRAVIS3D.Mapping.AttributeMapping#getId}
	 */
	this.removeMappings = function( mappingIds ) {
		
		for (var i = 0; i < mappingIds.length; i++){
			delete mappings[mappingIds[i]];			
		}
		for ( id in graphReps ) {
			graphReps[id].removeMappings( mappingIds );
		}
		
	};

	// LAYOUT
	/**
	 * returns the merged Layout of all snapshots, where you can access i.e. the width over all snapshots
	 */
	this.getLayout = function(){
		var l = new GRAVIS3D.Layout.GraphLayout();
		for (id in graphReps){
			var gl = graphReps[id].getLayout();
			var positions = gl.getPositions();
			for (pos in positions){
				var p  = positions[pos];
				l.addPosition( pos, new GRAVIS3D.Vector3D( p.getX()	, p.getY(), p.getZ() ) );
			}
		}
		return l;
	};
	
	/**
	 * @method buildLayout
	 * @param {@link GRAVIS3D.Layout.DynamicGraphLayoutBuilder} superLayoutBuilder
	 */
	this.buildLayout = function( subLayoutBuilder, superLayoutBuilder ) {

		if ( superLayoutBuilder == null || superLayoutBuilder == undefined ) superLayoutBuilder = GRAVIS3D.VisualRepresentation.Defaults.dynGraph.basicLayouter;
		if ( subLayoutBuilder == null || subLayoutBuilder == undefined ) subLayoutBuilder = GRAVIS3D.VisualRepresentation.Defaults.graph.basicLayouter;

		var layouts = superLayoutBuilder( {
			subLayouter : subLayoutBuilder,
			graph : this.getRepresentedDynamicGraphModel()
		} );

		for ( graphId in layouts ) {
			var l = layouts[graphId];
			for ( id in graphReps ) {
				if ( graphReps[id].getRepresentedGraphModel().getId() == graphId ) {
					graphReps[id].setLayout( l );
				}
			}
		}

	};


	///////////////////////////////

	if ( params && params.represents ) {
		if ( params.represents instanceof GRAVIS3D.Model.DynamicGraph ) {
			this.setRepresentedDynamicGraphModel( params.represents );
		}
		if ( params.represents instanceof GRAVIS3D.Model.Graph ) {
			var dg = new GRAVIS3D.Model.DynamicGraph( {
				graphs : [ params.represents ]
			} );
			this.setRepresentedDynamicGraphModel( dg );
		}
	}
	if ( params && params.graphRepresentations ) {
		for ( var i = 0; i < params.graphRepresentation.length; i++ ) {
			this.addGraphRepresentation( params.graphRepresentations[i] );
		}
	}
	if ( params && params.layoutBuilder ) this.buildLayout( params.layoutBuilder );

	if ( params && params.mappings ) {
		for ( var i = 0; i < params.mappings.length; i++ ) {
			this.addMapping( params.mappings[i] );
		}
	}
	if ( params && params.nodeFilter ) this.setFilter( params.nodeFilter );
	if ( params && params.edgeFilter ) this.setFilter( params.edgeFilter );

};
