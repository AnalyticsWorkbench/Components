/**
 * A Time Point/ Time Span
 */
GRAVIS3D.Model.TimeInfo = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// VALUE
	var value = ""; // timeinformation i.e. 2001
	this.getValue = function() {
		return value;
	};
	this.setValue = function( string ) {
		if ( typeof string != "string" && string != undefined ) {
			string = JSON.stringify( string );
			value = string;
		}
	};

	// ORDER
	var order = 0; // Number (Order of appearance in a timechain)
	this.getOrder = function() {
		return order;
	};
	this.setOrder = function( number ) {
		if ( typeof number == "number" ) {
			order = number;
		} else {
			throw new Error( "Model - TimeInfo: setOrder() requires a number as param." );
		}
	};

	if ( !params && !params.value ) throw new Error( "Model - TimeInfo: REQUIRED PARAM: value." );
	this.setValue( params.value );

	if ( params.id ) this.setId( params.id );
	if ( params.name ) this.setName( params.name );
	if ( params.description ) this.setDescription( params.description );
	if ( params.order ) this.setOrder( params.order );

};

/**
 * A Graph Snapshot
 */
GRAVIS3D.Model.GraphSnapshot = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// TIME
	var _time = null; // TimePoint
	this.getTime = function() {
		return _time;
	};
	this.setTime = function( timeInfo ) {
		if ( timeInfo instanceof GRAVIS3D.Model.TimeInfo ) {
			_time = timeInfo;
		} else {
			throw new Error( "Model - GraphSnapshot: setTime()-param has to be of the type TimePoint" );
		}
	};

	// GRAPH 
	var _graph = null; // Graph (reference)
	this.getGraph = function() {
		return _graph;
	};
	this.setGraph = function( graph ) {
		if ( graph instanceof GRAVIS3D.Model.Graph ) {
			_graph = graph;
		} else {
			throw new Error( "Model - GraphSnapshot: graphId must be a string." );
		}
	};

	if ( !params || !params.graph ) throw new Error( "Model - GraphSnapshot: REQUIRED PARAMS: graph, time." );
	this.setGraph( params.graph );
	this.setTime( params.time );

	if ( params.id ) this.setId( params.id );
	if ( params.name ) this.setName( params.name );
	if ( params.description ) this.setDescription( params.description );

};

/**
 * A Dynamic Graph
 *
 * @param params TODO
 */
GRAVIS3D.Model.DynamicGraph = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// SNAPSHOTS
	var snapshots = {}; // { snapshotId: Snapshot }
	this.getSnapshot = function( snapshotId ) {
		if ( snapshots[snapshotId] ) {
			return snapshots[snapshotId];
		} else {
			return null;
		}
	};
	this.getSnapshots = function() {
		return snapshots;
	};
	this.addSnapshot = function( snapshot ) {
		if ( snapshot instanceof GRAVIS3D.Model.GraphSnapshot ) {
			snapshots[snapshot.getId()] = snapshot;
			return snapshot.getId();
		} else {
			throw new Error( "DynamicGraph: No proper Snapshot added." );
		}
	};
	this.deleleteSnapshot = function( snapshot ) {
		if ( snapshots[snapshotId] ) {
			delete snapshots[snapshotId];
		}
	};
	
	this.getNodes = function(ignoreFilter){
		var result = {};
		var graphs = this.getGraphs();
		for(id in graphs){
			
//			var attrs = graphs[graphId].getAttributes("node");// XXX +EDGES
//			var nodes = graphs[graphId].getNodes(ignoreFilter);
//			
//			for ( nodeId in nodes) {
//				if (result[nodeId] == undefined ){
//					result[nodeId] = new GRAVIS3D.Model.Node();
//				}
//				for (attrId in attrs){
//					var attrType = attrs[attrId].getType();
//					var attrVal = nodes[nodeId].getAttributeValue(attrId);
//					if (attrType == "number"){
//						val = ( parseFloat(attrVal) / attrs[attrId].getRange().getMax() );	
//						if( result[nodeId].getAttributeValue(attrId) ) val = val + result[nodeId].getAttributeValue(attrId) ;
//						result[nodeId].setAttributeValue(attrId, val);	
//					}else{
//						result[nodeId].setAttributeValue(attrId, attrVal);					
//					}
//				}
//			}
//			
			
			var nodes = graphs[id].getNodes(ignoreFilter);
			for (_id in nodes){
				result[_id] = nodes[_id];
			}
		}
		return result;
		
	};
	this.getEdges = function(ignoreFilter){
		var result = {};
		var graphs = this.getGraphs();
		for(id in graphs){
			var edges = graphs[id].getEdges(ignoreFilter);
			for (_id in edges){
				result[_id] = edges[_id];
			}
		}
		return result;
	};

//	this.calculateAverages = function() {
//		var sg = this.getSupergraph();
//		var nodes = sg.getNodes();
//		var edges = sg.getEdges();
//		var attributes = sg.getAttributes();
//		// attribute averages
//		for ( attribute in attributes ) {
//			if ( attributes[attribute].getType() == "number" ) {
//				var sum = 0;
//				var count = Object.keys( snapshots ).length;
//				if ( attributes[attribute].isFor() == "node" ) {
//					// sum up all attribute values
//					for ( snapshot in snapshots ) {
//						if ( snapshots[snapshot].getGraph().getNodeById( id ) != null ) {
//							sum += snapshots[snapshot].getGraph().getNodeById( id ).getAttributeValue( attribute );
//						}
//					}
//					// apply value to nodes
//					for ( snapshot in snapshots ) {
//						if ( snapshots[snapshot].getGraph().getNodeById( id ) != null ) {
//							snapshots[snapshot].getGraph().getNodeById( id ).setData( "avg_" + attribute,
//									( sum / count ) );
//						}
//					}
//				} else {
//					// sum up all attribute values
//					for ( snapshot in snapshots ) {
//						if ( snapshots[snapshot].getGraph().getEdgeById( id ) != null ) {
//							sum += snapshots[snapshot].getGraph().getEdgeById( id ).getAttributeValue( attribute );
//						}
//					}
//					// apply value to edges
//					for ( snapshot in snapshots ) {
//						if ( snapshots[snapshot].getGraph().getEdgeById( id ) != null ) {
//							snapshots[snapshot].getGraph().getEdgeById( id ).setData( "avg_" + attribute,
//									( sum / count ) );
//						}
//					}
//				}
//			}
//		}
//		// node weight averages
//		for ( id in nodes ) {
//			var sum = 0;
//			var count = Object.keys( snapshots ).length;
//			// sum up all weights
//			for ( snapshot in snapshots ) {
//				if ( snapshots[snapshot].getGraph().getNodeById( id ) != null ) {
//					sum += snapshots[snapshot].getGraph().getNodeById( id ).getWeight();
//				}
//			}
//			// apply value to nodes
//			for ( snapshot in snapshots ) {
//				if ( snapshots[snapshot].getGraph().getNodeById( id ) != null ) {
//					snapshots[snapshot].getGraph().getNodeById( id ).setData( "avg_weight", ( sum / count ) );
//				}
//			}
//		}
//		// edge weight averages
//		for ( id in edges ) {
//			var sum = 0;
//			var count = Object.keys( snapshots ).length;
//			// sum up all weights
//			for ( snapshot in snapshots ) {
//				if ( snapshots[snapshot].getGraph().getEdgeById( id ) != null ) {
//					sum += snapshots[snapshot].getGraph().getEdgeById( id ).getWeight();
//				}
//			}
//			// apply value to nodes
//			for ( snapshot in snapshots ) {
//				if ( snapshots[snapshot].getGraph().getEdgeById( id ) != null ) {
//					snapshots[snapshot].getGraph().getEdgeById( id ).setData( "avg_weight", ( sum / count ) );
//				}
//			}
//		}
//
//	};

	this.getSupergraph = function() {
		return GRAVIS3D.Model.Tools.calculateSupergraph( this.getOrderedGraphs() );
	};

	/**
	 * @returns an ordered array of GRAVIS3D.Model.GraphSnapshot by timeappearance / order
	 */
	this.getOrderedSnapshots = function() {
		var result = [];
		var orders = [];
		for ( id in snapshots ) {
			orders.push(snapshots[id].getTime().getOrder());
		}
		orders = orders.sort(function(a, b){return a-b;});
		for ( var i = 0; i < orders.length; i++ ) {
			for ( id in snapshots ) {
				if (snapshots[id].getTime().getOrder() == orders[i]){
					result.push( snapshots[id]);
					break;
				}
			}
		}
		return result;
	};
	this.getGraphs = function() {
		var result = {};
		for ( id in snapshots ) {
			var s = snapshots[id];
			result[s.getGraph().getId()] = s.getGraph();
		}
		return result;
	};
	this.getGraphsAsArray = function() {
		var result = [];
		for ( id in snapshots ) {
			var s = snapshots[id];
			result.push( s.getGraph() );
		}
		return result;
	};
	this.getOrderedGraphs = function() {
		var result = [];
		var snaps = this.getOrderedSnapshots();
		for ( var i = 0; i < snaps.length; i++ ) {
			result.push( snaps[i].getGraph() );
		}
		return result;
	};
	
	// FILTER 

	var nodeFilter = null;
	var edgeFilter = null;
	/**
	 * returns the acutal filter
	 * 
	 * @method getFilter
	 * @params {"node" | "edge"} context 
	 * @returns {false | {@link GRAVIS3D.Filtering.Filter} }
	 */
	this.getFilter = function( context ) {
		if ( context == "edge" ) return edgeFilter;
		else if ( context == "node" ) return nodeFilter;
		else throw new Error( "Model: DynamicGraph " + this.getId()
				+ ": #getFilter: param must be node / edge... (param: " + JSON.stringify( context ) + ")" );
	};
	/**
	 * returns the actual node filter / null if no one exists
	 * (alias for @this#getFilter)
	 * 
	 * @method getNodeFilter
	 * @returns {null | {@link GRAVIS3D.Filtering.Filter} }
	 */
	this.getNodeFilter = function() {
		return this.getFilter( "node" );
	};
	/**
	 * returns the actual edge filter / null if no one exists
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
	 * @param filter
	 */
	this.setFilter = function( filter ) {
		if ( filter instanceof GRAVIS3D.Filtering.Filter ) {
			if ( filter.isFor() == "edge" ) {
				edgeFilter = filter;
				var graphs = this.getGraphsAsArray();
				for ( var i = 0; i < graphs.length; i++ ) {
					graphs[i].setFilter( filter );
				}
			} else nodeFilter = filter;
		} else throw new Error( "Model: DynamicGraph " + this.getId()
				+ ": #setFilter: param must be a Filtering - Filter... (param: " + JSON.stringify( filter ) + ")" );
	};
	/**
	 * sets the node filter (alias for @this#setFilter)
	 * 
	 * @method setNodeFilter 
	 * @param {@link GAVIS3D.Filtering.Filter} filter
	 */
	this.setNodeFilter = function( filter ) {
		this.setFilter( filter );
	};
	/**
	 * sets the edge filter (alias for @this#setFilter)
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
		if ( context == "edge" ) edgeFilter = null;
		else if ( context == "node" ) nodeFilter = null;
		else {
			throw new Error( "Model: DynamicGraph " + this.getId()
					+ ": #removeFilter: param must be node / edge... (param: " + JSON.stringify( context ) + ")" );
			return;
		}
		var graphs = this.getGraphsAsArray();
		for ( var i = 0; i < graphs.length; i++ ) {
			graphs[i].removeFilter( context );
		}
	};



	// ATTRIBUTES
	this.getNodeAttributes = function() {
		return this.getAttributes( "node" );
	};
	this.getNodeAttributesAsArray = function() {
		var array = [];
		var attrs = this.getAttributes( "node" );
		for ( id in attrs ) {
			array.push( attrs[id] );
		}
		return array;
	};
	this.getEdgeAttributes = function() {
		return this.getAttributes( "edge" );
	};
	this.getEdgeAttributesAsArray = function() {
		var array = [];
		var attrs = this.getAttributes( "edge" );
		for ( id in attrs ) {
			array.push( attrs[id] );
		}
		return array;
	};
	this.getAttributeById = function( attributeId, context ) {
		var attributes = null;
		if ( context == "node" ) {
			attributes = this.getAttributes( "node" );
		} else if ( context == "edge" ) {
			attributes = this.getAttributes( "edge" );
		}
		if ( attributes != null && attributes[attributeId] ) {
			return attributes[attributeId];
		} else {
			return null;
		}
	};
	this.getNodeAttributeById = function( attributeId ) {
		return this.getAttributeById( attributeId, "node" );
	};
	this.getEdgeAttributeById = function( attributeId ) {
		return this.getAttributeById( attributeId, "edge" );
	};
	this.getAttributes = function( context ) {
		var result = {};
		var gs = this.getOrderedGraphs();
		for ( var i = 0; i < gs.length; i++ ) {
			var g = gs[i];

			// get node / edge attributes
			var attributes = g.getAttributes(context);
			// combine the attributes from every snapshot
			for ( id in attributes ) {
				var attr = attributes[id];
				if ( !result[id]  ) {
					result[id] = new GRAVIS3D.Model.Attribute( {
						id : id,
						type : attr.getType(),
						isFor : attr.isFor(),
						name : attr.getName(),
					} );
				}
				var valuesToAdd = attr.getPossibleValues();
				for ( val in valuesToAdd ) {
					var count = valuesToAdd[val];
					if (attr.getType()=="number") val = parseFloat(val);
					result[attr.getId()].addPossibleValue( val, count );
				}
			}
		}
		return result;
	};
	this.getAttributesAsArray = function( context ) {
		var attributes = this.getAttributes(context);
		var array = [];
		for ( id in attributes ) {
			array.push( attributes[id] );
		}
		return array;
	};
	this.addAttribute = function( attribute ) {
		var gs = this.getOrderedGraphs();
		for ( var i = 0; i < gs.length; i++ ) {
			gs[i].addAttribute( attribute );
		}
	};
	this.removeAttribute = function( attributeId, context ) {
		var gs = this.getOrderedGraphs();
		for ( var i = 0; i < gs.length; i++ ) {
			gs[i].removeAttribute( attributeId, context );
		}
	};
	this.removeNodeAttribute = function( attributeId ) {
		var gs = this.getOrderedGraphs();
		for ( var i = 0; i < gs.length; i++ ) {
			gs[i].removeNodeAttribute( attributeId );
		}
	};
	this.removeEdgeAttribute = function( attributeId ) {
		var gs = this.getOrderedGraphs();
		for ( var i = 0; i < gs.length; i++ ) {
			gs[i].removeEdgeAttribute( attributeId );
		}
	};


	if ( params && params.id ) this.setId( params.id );
	if ( params && params.name ) this.setName( params.name );
	if ( params && params.description ) this.setDescription( params.description );
	if ( params && params.snapshots ) snapshots = params.snapshots;
	if ( params && params.graphs ) {
		for ( var i = 0; i < graphs.length; i++ ) {
			var time = new GRAVIS3D.Model.TimeInfo( {
				order : i,
				value : new Date().toDateString()
			} );
			this.addGraphSnapshot( new GRAVIS3D.Model.GraphSnapshot( {
				graph : graphs[i],
				time : time
			} ) );
		}
	}

};
