/**
 * A Node
 */
GRAVIS3D.Model.Node = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// WEIGHT
	var weight = 1;
	this.getWeight = function() {
		return weight;
	};
	this.setWeight = function( number ) {
		if ( number >= 0 ) {
			weight = number;
		}
	};

	// ATTRIBUTES
	var attributeValues = {}; // { attributeId: aValue }
	this.getAttributeValues = function() {
		return attributeValues;
	};
	this.getAttributeValue = function( attributeId ) {
		if ( attributeValues[attributeId] ) {
			return attributeValues[attributeId];
		} else {
			return null;
		}
	};
	this.setAttributeValue = function( attributeId, value ) {
		attributeValues[attributeId] = value;
	};

	// GROUPS
	var groups = []; // i.e. ["biology", "engineering", "information science", ...]
	this.getGroupIds = function() {
		return groups;
	};
	this.addGroup = function( groupId ) {
		if ( typeof ( groupId ) == "string" ) {
			if ( groups.indexOf( groupId ) == -1 ) groups.push( groupId );
		} else {
			throw new Error( "Model - Node: addGroup() requires a string as param." );
		}
	};
	this.setGroups = function( groupId_Array ) {
		if ( groupId_Array && Object.prototype.toString.call( groupId_Array ) === '[object Array]' ) {
			for ( var i = 0; i < groupId_Array.length; i++ ) {
				this.addGroup( groupId_Array[i] );
			}
		} else {
			throw new Error( "Model - Node: setGroups() requires an array as param." );
		}
	};
	this.removeGroup = function( groupId ) {
		var position = groups.indexOf( groupId );
		if ( position >= 0 ) groups.splice( position, 1 );
	};

	if ( params && params.id ) this.setId( params.id );
	if ( params && params.name ) this.setName( params.name );
	if ( params && params.description ) this.setDescription( params.description );
	if ( params && params.weight ) this.setWeight( params.weight );
	if ( params && params.attribtues ) attributeValues = params.attributes;
	if ( params && params.groups ) this.setGroups( params.groups );

};

/**
 * An Edge
 */
GRAVIS3D.Model.Edge = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// SOURCE
	var _source = ""; // nodeId
	this.setSource = function( source ) {
		if ( source instanceof GRAVIS3D.Model.Node ) {
			_source = source;
		} else {
			throw new Error( "Edge: No proper Source for Edge '" + this.name + "' - " + this.getId() );
		}
	};
	this.getSource = function() {
		return _source;
	};

	// TARGET
	var _target = ""; // nodeId
	this.setTarget = function( target ) {
		if ( target instanceof GRAVIS3D.Model.Node ) {
			_target = target;
		} else {
			throw new Error( "Edge: No proper Target for Edge '" + this.name + "' - " + this.getId() );
		}
	};
	this.getTarget = function() {
		return _target;
	};

	// DIRECTION
	var directed = false; // if the edge is undirected or not
	this.ifDirected = function() {
		return directed;
	};
	this.setDirected = function( boolean ) {
		if ( typeof boolean == "boolean" ) directed = boolean;
	};


	// WEIGHT
	var weight = 1;
	this.getWeight = function() {
		return weight;
	};
	this.setWeight = function( number ) {
		if ( typeof number == "number" ) {
			weight = number;
		}
	};

	// ATTRIBUTES
	var attributeValues = {}; // { attributeId: aValue }
	this.getAttributeValues = function() {
		return attributeValues;
	};
	this.getAttributeValue = function( attributeId ) {
		if ( attributeValues[attributeId] != undefined ) {
			return attributeValues[attributeId];
		} else {
			return null;
		}
	};
	this.setAttributeValue = function( attributeId, value ) {
		attributeValues[attributeId] = value;
	};

	if ( !params || !params.source || !params.target ) throw new Error(
			"Model - Edge: REQUIRED PARAMS: source, target." );
	this.setSource( params.source );
	this.setTarget( params.target );

	if ( params.id ) this.setId( params.id );
	if ( params.name ) this.setName( params.name );
	if ( params.description ) this.setDescription( params.description );
	if ( params.weight ) this.setWeight( params.weight );
	if ( params.attributeValues ) attributeValues = params.attributeValues;
	if ( params.directed ) this.setDirected( params.directed );
	if ( params.undirected ) this.setDirected( !params.undirected );

};

/**
 * An Attribute
 */
GRAVIS3D.Model.possibleAttributeTypes = [ "string", "number", "cluster" ];
GRAVIS3D.Model.Attribute = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// NODE / EDGE
	var isFor = ""; // "node", "edge"
	this.isFor = function() {
		return isFor;
	};
	this.setFor = function( nodeOrEdge ) {
		if ( nodeOrEdge.toString().toLowerCase() == "node" || nodeOrEdge.toString().toLowerCase() == "edge" ) {
			isFor = nodeOrEdge;
		} else {
			throw new Error( "Model - Attribute: isFor-argument can only be 'node' or 'edge'. Passed: " + nodeOrEdge );
		}
	};

	// TYPE
	var _type = ""; // "number", "string", "cluster", ...
	this.getType = function() {
		return _type;
	};
	this.setType = function( type ) {
		if ( GRAVIS3D.Model.possibleAttributeTypes.indexOf( type.toString().toLowerCase() ) != -1 ) {
			_type = params.type;
		} else {
			throw new Error( "Model - Attribute: type-argument can only be "
					+ GRAVIS3D.Model.possibleAttributeTypes.toString() + ". Missing param?... Passed: " + type );
		}
	};

	// RANGE
	var _range = null; // Range for number attributes (to normalize values)
	this.getRange = function() {
		if ( _range == null ) return new GRAVIS3D.Range( 0, 0 );
		return _range;
	};
	this.setRange = function( range ) {
		if ( range instanceof GRAVIS3D.Range ) {
			_range = range;
		} else if ( range.min && range.max ) {
			_range = new GRAVIS3D.Range( range.min, range.max );
		} else {
			throw new Error( "Model - Attribute.setRange: argument must be a range. Passed: " + range );
		}
	};

	// VALUES 
	var possibleValues = {}; // { value: numberOfAppearance }
	this.getPossibleValues = function() {
		return possibleValues;
	};
	/**
	 * @return {value: count}[] 
	 */
	this.getPossibleValues_rankedAsArray = function() {
		var result = [];
		// for all vals
		for ( val in possibleValues ) {
			// value
			var count = possibleValues[val];
			if ( result.length == 0 ) {
				var o = new Object();
				o[val] = count;
				result.push( o );
			} else {
				// go though result, if actual value >= value, add actual value 
				for ( var i = 0; i < result.length; i++ ) {
					var val2 = Object.keys( result[i] )[0];
					var count2 = result[i][val2];
					if ( count <= count2 ) {
						var o = new Object();
						o[val] = count;
						result.splice( i, 0, o );
						break;
					}

				}
			}
		}
		return result.reverse();
	};
	/**
	 * make sure you pass correct values (i. e. number attribute - numbers...)
	 */
	this.addPossibleValue = function( value, count ) {
		if ( possibleValues[value] == undefined ) {
			if ( count != undefined && count != null ) possibleValues[value] = count;
			else possibleValues[value] = 1;
		} else {
			if ( count != undefined && count != null ) possibleValues[value] += count;
			else possibleValues[value] += 1;
		}
		//auto-adjust range with new value
		if ( this.getType() == "number" ) {
			if ( this.getRange() == null ) {
				this.setRange( new GRAVIS3D.Range( value, value ) );
			}
			if ( this.getRange().getMin() > value ) {
				this.setRange( new GRAVIS3D.Range( value, this.getRange().getMax() ) );
			}
			if ( this.getRange().getMax() < value ) {
				this.setRange( new GRAVIS3D.Range( this.getRange().getMin(), value ) );
			}
		}
	};

	if ( !params || !params.isFor || !params.type ) { throw new Error(
			"Model - Attribute: REQUIRED PARAMS: isFor, type." ); }
	this.setFor( params.isFor );
	this.setType( params.type );

	if ( params.id ) this.setId( params.id );
	if ( params.name ) this.setName( params.name );
	if ( params.description ) this.setDescription( params.description );
	if ( params.possibleValues ) possibleValues = params.possibleValues;
	if ( params.range ) this.setRange( params.range );


};


/**
 * A Graph
 */
GRAVIS3D.Model.Graph = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// NODES
	var nodes = {}; // { nodeId: Node }
	var nodesWithFilter = null; // a container for nodes after filtering..
	this.getNodes = function( ignoreFilter ) {
		if ( this.getNodeFilter() != null && ignoreFilter != true ) {
			if ( nodesWithFilter == null ) {
				var n = {};
				var filteredIds = this.getNodeFilter().getResult( this );
				for ( id in nodes ) {
					if ( filteredIds.indexOf( id ) == -1 ) {
						n[id] = nodes[id];
					}
				}
				nodesWithFilter = n;
			}
			return nodesWithFilter;
		} else return nodes;
	};
	this.getNodeById = function( nodeId ) {
		if ( nodes[nodeId] ) {
			return nodes[nodeId];
		} else {
			return null;
		}
	};
	this.addNode = function( node ) {
		if ( node instanceof GRAVIS3D.Model.Node ) {
			nodes[node.getId()] = node;
			return node.getId();
		} else {
			throw new Error( "Model - Graph: No proper Node added." );
		}
	};
	this.removeNode = function( nodeId ) {
		if ( nodes[nodeId] ) delete nodes[nodeId];
	};

	// EDGES
	var edges = {}; // { edgeId: Edge }
	var edgesWithFilter = null;
	this.getEdgeById = function( edgeId ) {
		if ( edges[edgeId] ) {
			return edges[edgeId];
		} else {
			return null;
		}
	};
	this.getEdges = function( ignoreFilter ) {
		// edge filter and possible node filter
		if ( this.getEdgeFilter() != null && ignoreFilter != true ) {
			if ( edgesWithFilter == null ) {
				var e = {};
				var filteredIds = this.getEdgeFilter().getResult( this );
				var filteredNodes = null;
				if ( this.getNodeFilter() != null ) {
					filteredNodes = this.getNodeFilter().getResult( this );
				}
				for ( id in edges ) {
					if ( filteredNodes != null ) {
						var sourceId = edges[id].getSource().getId();
						var targetId = edges[id].getTarget().getId();
						if ( filteredNodes.indexOf( sourceId ) == -1 && filteredNodes.indexOf( targetId ) == -1 ) {
							if ( filteredIds.indexOf( id ) == -1 ) e[id] = edges[id];
						}
					} else {
						if ( filteredIds.indexOf( id ) == -1 ) e[id] = edges[id];
					}
				}
				edgesWithFilter = e;
			}
			return edgesWithFilter;
		}
		//node filter only
		else if ( this.getNodeFilter() != null && ignoreFilter != true ) {
			if ( edgesWithFilter == null ) {

				var e = {};
				filteredNodes = this.getNodeFilter().getResult( this );
				for ( id in edges ) {
					var sourceId = edges[id].getSource().getId();
					var targetId = edges[id].getTarget().getId();
					if ( filteredNodes.indexOf( sourceId ) == -1 && filteredNodes.indexOf( targetId ) == -1 ) {
						e[id] = edges[id];
					}
				}
				edgesWithFilter = e;
			}
			return edgesWithFilter;
		}
		// no filter
		else return edges;
	};
	this.addEdge = function( edge ) {
		if ( edge instanceof GRAVIS3D.Model.Edge ) {
			edges[edge.getId()] = edge;
			return edge.getId();
		} else {
			throw new Error( "Model - Graph: No proper Edge added." );
		}
	};
	this.removeEdge = function( edgeId ) {
		if ( edges[edgeId] ) delete edges[edgeId];
	};

	// ATTRIBUTES
	var node_attributes = {}; // { attributeId: Attribute }
	var edge_attributes = {};
	this.getNodeAttributes = function() {
		return node_attributes;
	};
	this.getNodeAttributesAsArray = function() {
		var array = [];
		for ( id in node_attributes ) {
			array.push( node_attributes[id] );
		}
		return array;
	};
	this.getEdgeAttributes = function() {
		return edge_attributes;
	};
	this.getEdgeAttributesAsArray = function() {
		var array = [];
		for ( id in edge_attributes ) {
			array.push( edge_attributes[id] );
		}
		return array;
	};
	this.getAttributeById = function( attributeId, context ) {
		var attributes;
		if ( context == "node" ) {
			attributes = node_attributes;
		} else if ( context == "edge" ) {
			attributes = edge_attributes;
		} else {
			//TODO
		}
		if ( attributes[attributeId] ) {
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
		if ( context == "node" ) {
			return node_attributes;
		} else if ( context == "edge" ) {
			return edge_attributes;
		} else {
			//TODO
		}
	};
	this.getAttributesAsArray = function( context ) {
		var attributes;
		if ( context == "node" ) {
			attributes = node_attributes;
		} else if ( context == "edge" ) {
			attributes = edge_attributes;
		} else {
			//TODO
		}
		var array = [];
		for ( id in attributes ) {
			array.push( attributes[id] );
		}
		return array;
	};
	this.addNodeAttribute = function( attribute ) {
		node_attributes[attribute.getId()] = attribute;
	};
	this.addEdgeAttribute = function( attribute ) {
		edge_attributes[attribute.getId()] = attribute;
	};
	this.addAttribute = function( attribute ) {
		if ( attribute instanceof GRAVIS3D.Model.Attribute ) {
			var attributes;
			var context = attribute.isFor();
			if ( context == "node" ) {
				attributes = node_attributes;
			} else if ( context == "edge" ) {
				attributes = edge_attributes;
			}
			attributes[attribute.getId()] = attribute;
			return attribute.getId();
		} else {
			throw new Error( "Model - Graph: No proper Attribute added." );
		}
	};
	this.removeAttribute = function( attributeId, context ) {
		var attributes;
		var context = attribute.isFor();
		if ( context == "node" ) {
			attributes = node_attributes;
		} else if ( context == "edge" ) {
			attributes = edge_attributes;
		}
		if ( attributes[attributeId] ) delete attributes[attributeId];
	};
	this.removeNodeAttribute = function( attributeId ) {
		if ( node_attributes[attributeId] ) delete node_attributes[attributeId];
	};
	this.removeEdgeAttribute = function( attributeId ) {
		if ( edge_attributes[attributeId] ) delete edge_attributes[attributeId];
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
		else throw new Error( "Model: Graph " + this.getId() + ": #getFilter: param must be node / edge... (param: "
				+ JSON.stringify( context ) + ")" );
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
			edgesWithFilter = null;
			nodesWithFilter = null;
			if ( filter.isFor() == "edge" ) edgeFilter = filter;
			else nodeFilter = filter;
		} else throw new Error( "Model: Graph " + this.getId()
				+ ": #setFilter: param must be a Filtering - Filter... (param: " + JSON.stringify( filter ) + ")" );
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
		if ( context == "edge" ) edgeFilter = null;
		else if ( context == "node" ) nodeFilter = null;
		else throw new Error( "Model: Graph " + this.getId() + ": #removeFilter: param must be node / edge... (param: "
				+ JSON.stringify( context ) + ")" );
		edgesWithFilter = null;
		nodesWithFilter = null;
	};

	/////////////////////

	if ( params && params.id ) this.setId( params.id );
	if ( params && params.name ) this.setName( params.name );
	if ( params && params.description ) this.setDescription( params.description );
	if ( params && params.nodes ) nodes = params.nodes;
	if ( params && params.edges ) edges = params.edges;
	if ( params && params.attributes ) attributes = params.attributes;

};
