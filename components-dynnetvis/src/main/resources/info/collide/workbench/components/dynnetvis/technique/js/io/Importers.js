/**
 * @author Henrik Detjen
 */
GRAVIS3D.IO.Importers = {};
/**
 * @summary
 * SISOB-Loader (async): 
 * creates a @link GRAVIS3D.Model.Dynamic graph from given files
 * @since 1.0
 * @method SISOBLoader
 * @param {String[]} filePath
 * @param {Function} callback - define what to do after files are loaded.
 * @example
 * <code>
 * <pre>
 * var dynamicGraph = null;
 * var filepath1 = "url1";
 * var filepath2 = "url2";
 * GRAVIS3D.IO.SISOBDynamicGraphLoader(
 * 	[filepath1,filepath2],
 * 	function(graph){
 * 		dynamicGraph = graph;
 * 		//...do something with the graph. 
 * });
 * </pre>
 * </code>
 */
GRAVIS3D.IO.Importers.SISOB = function( files_Path, callback ) {

	var dynGraph = new GRAVIS3D.Model.DynamicGraph();
	dynGraph.setData( "files", files_Path );

	for ( var fileCounter = 0; fileCounter < files_Path.length; fileCounter++ ) {
		createSnapshot( files_Path[fileCounter], fileCounter );
	}

	var callCount = 0;
	var stats = {
			graphCount: 0,
			nodeCount: 0,
			edgeCount: 0
	};
	function ready() {
		if ( callCount == files_Path.length - 1 ) {
			console.log("----------------------------------------");
			console.log("GRAVIS3D.IO.Importers.SISOB - Objects read (total count)");
			console.log("Graphs: " + stats.graphCount);
			console.log("Nodes: " + stats.nodeCount);
			console.log("Edges: " + stats.edgeCount);
			console.log("----------------------------------------");
			callback( dynGraph );
		}
		callCount++;
	}
	
	function createSnapshot( filePath, order ) {

		d3.json( filePath, function( error, json ) {

			function errMsg(msg){
				new GRAVIS3D.GUI.PromptModal( {
					title: "Problems while reading a file...",
					$msg : $( '<div class="alert alert-warning" role="alert"><span class="glyphicon glyphicon-warning-sign"></span> File:<br />'+error.responseURL+'<br />'+msg+'</div>' ),
				} );			
			}

			if( error != null || json == null || json == undefined ){
				
				errMsg("Could not read file. (Empty or not accessable)");
				ready();
				
			}else{
				
				var data = json.data;
				var metadata = json.metadata;
				
				if ( metadata != null && metadata != undefined && data != null && data != undefined && data.nodes != undefined ) {
					
					var timeInfo = metadata.time;
					var time = new GRAVIS3D.Model.TimeInfo( {
						value : timeInfo,
						order : order
					} );
					
					var snapshot = new GRAVIS3D.Model.GraphSnapshot( {
						graph : createGraph( data, metadata ),
						time : time
					} );
					snapshot.setData( "file", filePath );
					
					dynGraph.addSnapshot( snapshot );
					
				}else{
					errMsg("Check your Graph definition and make sure to use the SISOB-Format.");
				}
				ready();
			}
			
		} );
	}

	function createGraph( data, metadata ) {

		stats.graphCount++;
		
		var d = data;
		var md = metadata;

		var graph = new GRAVIS3D.Model.Graph();

		// normalize the cluster type
		correctClusters( md, d );

		// retrieving the meta data....
		graph.setName( md.title );
		graph.setDescription( md.description );
		graph.setData( "time", md.time );
		graph.setData( "directed", md.directed );
		graph.setData( "type", md.type );

		graph.setData( "datalinks", md.datalinks );

		// attributes...
		if ( md.measures ) {
			for ( var j = 0; j < md.measures.length; j++ ) {
				var measureObj = md.measures[j];
				if ( measureObj.type == "double" ) measureObj.type = "number";
				if ( measureObj.title == "weight" ) measureObj.type = "number"; // weight is declared as string sometimes...
                try {
                    var attr = new GRAVIS3D.Model.Attribute({
                        isFor: measureObj["class"],
                        id: measureObj.property,
                        type: measureObj.type,
                        name: measureObj.title,
                        description: measureObj.description
                    });
                    graph.addAttribute(attr);
                } catch (error) {
                    // ignore measure if we don't know how to handle it
                    console.log("couldn't handle measure " + propKey);
                }
			}
		}
		if ( md.edgeproperties ) {
			var eps = md.edgeproperties;
			for ( var j = 0; j < eps.length; j++ ) {
				var prop = eps[j];
				var propKey = Object.keys( prop )[0];
				var propVal = prop[propKey];
				if ( propKey == "weight" ) propVal = "number"; // weight is declared as string sometimes...
				// check if the properties was already defined in measures..
				if ( graph.getEdgeAttributeById( propKey ) == null ) {
                    try {
                        var attr = new GRAVIS3D.Model.Attribute({
                            isFor: "edge",
                            id: propKey,
                            type: propVal,
                            name: propKey
                        });
                        graph.addAttribute(attr);
                    } catch (error) {
                        // ignore property if we don't know how to handle it
                        console.log("couldn't handle edge property " + propKey);
                    }
				}
			}
		}
		if ( md.nodeproperties ) {
			var nps = md.nodeproperties;
			for ( var j = 0; j < nps.length; j++ ) {
				var prop = nps[j];
				var propKey = Object.keys( prop )[0];
				var propVal = prop[propKey];
				if ( propKey == "weight" ) propVal = "number"; // weight is declared as string sometimes...
				// check if the properties was already defined in measures..
				if ( graph.getNodeAttributeById( propKey ) == null ) {
                    try {
                        var attr = new GRAVIS3D.Model.Attribute( {
                            isFor : "node",
                            id : propKey,
                            type : propVal,
                            name : propKey
                        } );
                        graph.addAttribute( attr );
                    } catch (error) {
                        // ignore property if we don't know how to handle it
                        console.log("couldn't handle node property " + propKey);
                    }
				}
			}
		}

		modifyIds( d.nodes, d.edges ); // check ids over time... 

		// retrieving data...
		// nodes...
		if ( d.nodes ) {
			for ( var j = 0; j < d.nodes.length; j++ ) {
				stats.nodeCount++;
				var n = d.nodes[j];
				var _n = new GRAVIS3D.Model.Node();
				if ( n.id ) _n.setId( n.id );
				if ( n.timeappearance ) _n.setData( "timeappearance", n.timeappearance );
				if ( n.type ) _n.setData( "type", n.type );
				if ( n.label ) {
					_n.setName( n.label );
					_n.setAttributeValue( "name", n.label );
				} else {
					_n.setName( n.id );
					_n.setAttributeValue( "name", n.id );
				}
				if ( n.weight ) {
					var w = parseFloat( n.weight );
					if ( !isNaN( w ) ) {
						_n.setWeight( w );
						_n.setData( "weight", w );
					}
				}
				var attributes = graph.getNodeAttributes();
				for ( id in attributes ) {
					// for all node attributes: get val
					if ( n[id] ) {
						var value = n[id];
						if ( attributes[id].getType() == "number" ) value = parseFloat( value );
						_n.setAttributeValue( id, value );
						attributes[id].addPossibleValue( value );
					}
				}
				graph.addNode( _n );
			}
		}

		// edges...
		if ( d.edges ) {
			for ( var j = 0; j < d.edges.length; j++ ) {
				stats.edgeCount++;
				var e = d.edges[j];
				var sourceNode = graph.getNodeById( e.source );
				var targetNode = graph.getNodeById( e.target );

				if ( sourceNode && targetNode ) {
					var _e = new GRAVIS3D.Model.Edge( {
						source : sourceNode,
						target : targetNode
					} );
					_e.setId( sourceNode.getId() + "-" + targetNode.getId() );
					if ( e.timeappearance ) _e.setData( "timeappearance", e.timeappearance );
					if ( e.type ) _e.setData( "type", e.type );
					if ( e.label ) {
						_e.setName( e.label );
						_e.setAttributeValue( "name", e.label );
					} else if ( e.id ) {
						_e.setName( e.id );
						_e.setAttributeValue( "name", e.id );
					}
					if ( e.weight ) {
						var w = parseFloat( e.weight );
						if ( !isNaN( w ) ) {
							_e.setWeight( w );
							_e.setData( "weight", w );
						}
					}
					var attributes = graph.getEdgeAttributes();
					for ( id in attributes ) {
						// for all edge attributes: get val
						if ( e[id] ) {
							var value = e[id];
							if ( attributes[id].getType() == "number" ) value = parseFloat( value );
							_e.setAttributeValue( id, value );
							attributes[id].addPossibleValue( value );
						}
					}
					graph.addEdge( _e );

				} else {
					throw new Error( "IO: edge " + e.id + " not valid (source or target missing)!" );
				}
			}
		}

		return graph;
	}

	function modifyIds( nodes, edges ) {
		if ( nodes ) {
			//1.check if label is usable
			var labels = true;
			for ( var i = 0; i < nodes.length; i++ ) {
				var n = nodes[i];
				// use label for id over time, if existing for all nodes...
				if ( n.label == undefined ) {
					labels = false;
				}
			}
			//2.make the labels unique if necessary (possible duplicates)
			function makeLabelsUnique() {
				// for all nodes
				for ( var i = 0; i < nodes.length; i++ ) {

					// look at each node n 
					var n = nodes[i];

					// if this node was changed / checked before.. skip
					if ( !( n.labelchecked == true ) ) {

						// clean label string from delimiters "(" and ")"
						n.label.split( "(" ).join( "" );
						n.label.split( ")" ).join( "" );

						// then look at all other nodes and find duplicates
						for ( var j = 0; j < nodes.length; j++ ) {
							var nX = nodes[j];
							var duplicates = 1;
							// rename all duplicates 
							// this will create labels like "label", "label(1)", "label(2)", "label(3)"
							if ( nX.label == n.label && j != i ) {
								nX.label = nX.label + "(" + duplicates++ + ")";
								nX.labelchecked = true; // remember change
							}
						}

					}
					// remember check
					n.labelchecked = true;

				}
			}
			//3.rename ids to label
			function idsFromLabel() {
				for ( var i = 0; i < nodes.length; i++ ) {
					var n = nodes[i];

					//rename edge source/target
					if ( edges ) {
						for ( var j = 0; j < edges.length; j++ ) {
							var e = edges[j];
							if ( e.source == n.id ) e.source = n.label;
							if ( e.target == n.id ) e.target = n.label;
						}
					}

					n.id = n.label;

				}
			}

			if ( labels ) {
				makeLabelsUnique();
				idsFromLabel();
			}

		}

	}

	// normalizing of cluster type and values
	function correctClusters( metadata, data ) {
		var clustersFound = false;
		if ( metadata.measures ) {
			for ( var i = 0; i < metadata.measures.length; i++ ) {
				if ( metadata.measures[i].property == "clusters" || metadata.measures[i].title == "clusters" ) {
					metadata.measures[i].type = "cluster";
					clustersFound = true;
				}
			}
		}
		if ( metadata.nodeproperties ) {
			for ( var i = 0; i < metadata.nodeproperties.length; i++ ) {
				if ( metadata.nodeproperties[i].clusters ) {
					metadata.nodeproperties[i].clusters = new Array( "cluster" );
					clustersFound = true;
				}
			}
		}
		if ( metadata.edgeproperties ) {
			for ( var i = 0; i < metadata.edgeproperties.length; i++ ) {
				if ( metadata.edgeproperties[i].clusters ) {
					metadata.edgeproperties[i].clusters = new Array( "cluster" );
					clustersFound = true;
				}
			}
		}
		if ( clustersFound ) {
			if ( data.edges ) {
				data.edges.forEach( function( edge ) {
					if ( typeof ( edge.clusters ) == "string" ) {
						edge.clusters = JSON.parse( edge.clusters );
						for ( var i = 0; i < edge.clusters.length; i++ ) {
							edge.clusters[i] = edge.clusters[i].toString();
						}
					}
				} );
			}
			if ( data.nodes ) {
				data.nodes.forEach( function( node ) {
					if ( typeof ( node.clusters ) == "string" ) {
						node.clusters = JSON.parse( node.clusters );
					}
					for ( var i = 0; i < node.clusters.length; i++ ) {
						node.clusters[i] = node.clusters[i].toString();
					}
				} );
			}
		}
	}


};
