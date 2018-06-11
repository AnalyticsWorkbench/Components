/**
 * @author Henrik Detjen
 */

/////////////////////////////
// here are possible views //
/////////////////////////////
/**
 * TODO
 */
GRAVIS3D.Views.TimeSliceView = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data
	var self = this;
	this.setName( "Time Slice View" );

	//////////////////

	var maxNodeCount = 250;
	var showLabels = true;
	var showBorders = true;
	var showTimeEdges = true;
	var timeEdgesOpacity = 0.1;
	var overallnodecount = 0;

	// saves the view state of elements (in contrary to show x) - used in updateX()
	var shown = {
		timeEdges : showTimeEdges,
		borders : showBorders,
		labels : showLabels
	};

	this.controls = {
		/**
		 * everything named in this array, causes the instance gui to hide controls
		 * i.e. hide: ["filtering", "mapping"] will make these unavailable for the user in this view
		 */
		hide : [],
		/**
		 * if this is not null, the returned element will be shown as "custom"-controls
		 * i.e. return $(' &lt;div&rt;  awesome view controls in here... &lt;div&rt; ');
		 * @return {jQuery}
		 */
		own : function() {
			
			var tsViewDiv = $( "<div></div>" );
			tsViewDiv.append( "<h2>Time Slice View<h2>" );
			tsViewDiv.append( "<hr />" );
			var panelWrapperCtrlsParent = $( "<div class='panel panel-default ' />" ).appendTo( tsViewDiv );
			var panelWrapperCtrls = $( "<div class='panel-body' />" ).appendTo( panelWrapperCtrlsParent );

			// SHOW HIDE
			$("<strong>Visibility</strong>").appendTo(panelWrapperCtrls);
			
			// labels?
			var other_options1 = $( "<div class='' />" ).appendTo( panelWrapperCtrls );
			var cb_div1 = $( '<div class="checkbox" />' ).appendTo( other_options1 );
			var cb1 = $( '<label><input type="checkbox" checked="' + true + '"> Labels</label>' ).appendTo( cb_div1 );
			cb1.find( 'input' ).on( "change click enter", function() {
				showLabels = cb1.find( 'input' ).is( ":checked" );
			} );


			// border?
			var other_options3 = $( "<div class='' />" ).appendTo( panelWrapperCtrls );
			var cb_div3 = $( '<div class="checkbox" />' ).appendTo( other_options3 );
			var cb3 = $( '<label><input type="checkbox" checked="' + showBorders + '"> Background Planes</label>' )
					.appendTo( cb_div3 );
			cb3.find( 'input' ).on( "change click enter", function() {
				showBorders = cb3.find( 'input' ).is( ":checked" );
			} );

			// time edges?
			var other_options2 = $( "<div class='' />" ).appendTo( panelWrapperCtrls );
			var cb_div2 = $( '<div class="checkbox" />' ).appendTo( other_options2 );
			var cb2 = $( '<label><input type="checkbox" checked="' + showTimeEdges + '"> Time Edges</label>' )
			.appendTo( cb_div2 );
			cb2.find( 'input' ).on( "change click enter", function() {
				showTimeEdges = cb2.find( 'input' ).is( ":checked" );
			} );

			$( '<label class="control-label" for="inopsel">Time Edge - Opacity</label><br />' ).appendTo(panelWrapperCtrls );
			var input_op = $( "<input id='inopsel'type='number' min='0' max='1' step='0.1' class='form-control'>" ).appendTo(panelWrapperCtrls);
			input_op.val( timeEdgesOpacity );
			input_op.on("change enter", function(){
				var input_op_val = input_op.val();
				if(input_op_val >= 0 && input_op_val <= 1){
					timeEdgesOpacity = input_op_val;
				}
			});
			
			panelWrapperCtrls.append( "<hr />" );

			// apply changes
			var applyBtn = $(
					'<button type="button" id="dg_applyBtn" class="btn btn-primary btn"><span class="glyphicon glyphicon-ok"></span> Apply Changes</button>' )
					.appendTo( panelWrapperCtrls );
			applyBtn.on( "click enter", function() {
				applyBtn.attr( "disabled", true );
				var btnhtmltmp = applyBtn.html();
				applyBtn.html( "updating..." );
				window.setTimeout( function() {
					update();
					applyBtn.prop( "disabled", false );
					applyBtn.html( btnhtmltmp );
				}, 10 );
			} );

			return tsViewDiv;
		}
	};

	////////////////////

	// init three scene
	var scene = null;

	var z_max; // set the camera behind nearest point
	var camera = null;
	var renderer = new THREE.WebGLRenderer( {
		antialias : true,
		alpha : true
	} );
	//		renderer.setClearColor( 0x000000, 1);
	var g = null; // visrep render objs. will be appended to this
	var animFrame = null;
	var controls = null;

	/////////////////////

	var _visualRepresentations = null;
	var _visualRepresentation = null;

	this.getVisualRepresentation = function() {
		return _visualRepresentation;
	};
	this.init = function( visualRepresentation ) {
		// vis reps to show...
		_visualRepresentations = [];
		if ( visualRepresentation instanceof GRAVIS3D.VisualRepresentation.Graph ) {
			_visualRepresentations.push( visualRepresentation );//TODO: add dyngraph vis rep
		} else if ( visualRepresentation instanceof GRAVIS3D.VisualRepresentation.DynamicGraph ) {
			_visualRepresentations = visualRepresentation.getGraphRepresentationsArray();
		} else throw new Error( "arr.." );//TODO
		_visualRepresentation = visualRepresentation;

		// create scene
		scene = new THREE.Scene();
		// visrep wrapper
		//		g = new THREE.Object3D();
		// remember nearest point
		z_max = 0;

		stackRepresentations();
		initTimeEdges();
		initBorders();

		// add visReps to scene
		var visRepCount = _visualRepresentations.length;
		overallnodecount = 0;

		for ( var i = 0; i < visRepCount; i++ ) {

			var graphRep = _visualRepresentations[i];

			var nodes = graphRep.getNodeRepresentationsAsArray();
			var nodeCount = nodes.length;
			overallnodecount += nodeCount;
			// set nodes..
			for ( var j = 0; j < nodeCount; j++ ) {
				scene.add( nodes[j].renderObject );
			}

			// set edges
			var edges = graphRep.getEdgeRepresentations();
			for ( id in edges ) {
				scene.add( edges[id].renderObject );
			}

			// adjust camera distance
			if ( graphRep.getLayout().getMaxZ() > z_max ) {
			}
		}
		z_max = _visualRepresentation.getLayout().getMaxZ();

		// show labels in small network
		if ( overallnodecount > maxNodeCount ) {
			showLabels = false;
		}
		updateLabels();


		// add visreps
		//		scene.add( g );
		// set far clipping to 10x nearest point (zoomout = 10x)
		var farClipping = z_max * 10 + 500;
		// init camera
		camera = new THREE.PerspectiveCamera( 30, window.innerWidth / window.innerHeight, 0.1, farClipping );
		// set camera position
		camera.position.z = ( z_max * 2 ) + 50;
		camera.position.x = ( z_max * 2 ) + 50;
		camera.position.y = ( z_max * 1 ) + 50;
		// init controls
		controls = new GRAVIS3D.Views.Defaults.basicView.controls( camera, renderer.domElement );
		controls.noKeys = true;
	};

	// UPDATE
	function update() {
		updateTimeEdges();
		updateLabels();
		updateBorders();
	}

	// LABELS
	var labels_initialized = false;
	function updateLabels() {
		if ( shown.labels == showLabels && labels_initialized == true ) return;
		labels_initialized = true;
		
		shown.labels = showLabels;
		for ( var i = 0; i < _visualRepresentations.length; i++ ) {
			var graphRep = _visualRepresentations[i];
			var nodes = graphRep.getNodeRepresentations();
			// set label..
			for ( id in nodes ) {
				nodes[id].setIfShowLabel( showLabels );
			}
		}
	}

	// TIME EDGES
	var timeEdges = null;
	function updateTimeEdges() {
		
		for ( var obj, i = scene.children.length - 1; i >= 0; i-- ) {
			obj = scene.children[i];
			if ( obj.name == "timeEdge" ) {
				obj.vr.setOpacity(timeEdgesOpacity);
			}
		}
		
		if ( timeEdges == null || showTimeEdges == shown.timeEdges ) return;
		shown.timeEdges = showTimeEdges;
		
		if ( showTimeEdges == true ) {
			initTimeEdges();
		}
		if ( showTimeEdges == false ) {
			for ( var obj, i = scene.children.length - 1; i >= 0; i-- ) {
				obj = scene.children[i];
				if ( obj.name == "timeEdge" ) {
					scene.remove( obj );
				}
			}
		}
	}

	function initTimeEdges() {

		timeEdges = [];

		var length = _visualRepresentations.length;

		// only for multiple graph visualizations
		if ( length <= 1 ) return;

		// for all graph reps - add new Edge in between
		for ( var i = 0; i < length; i++ ) {

			var vr = _visualRepresentations[i];

			// there is a slice before this one
			if ( _visualRepresentations[i - 1] ) {

				//check for all previous nodes, if they exist in this slice too
				var nodes = vr.getNodeRepresentations();

				// set nodes..
				for ( id in nodes ) {

					// actual node model id 
					var modelId = nodes[id].getRepresentedModel().getId();

					// exists in any slice before?
					var prevNode = null;
					for ( var j = ( i - 1 ); j >= 0; j-- ) {
						try {
							// if so, take first node as source, and stop
							prevNode = _visualRepresentations[j].getNodeRepresentationByNodeModelId( modelId );
							break;
						} catch ( err ) {
						}
					}
					if ( prevNode != null ) {
						//on sucess: add a new edge to the vis rep
						var timeEdge = new GRAVIS3D.VisualRepresentation.Edge( {
							represents : "",
							id : modelId,
							source : prevNode,
							target : nodes[id],
							color : "#5555FF"
						} );
						timeEdge.renderObject.name = "timeEdge";
						timeEdge.setPosition_From( prevNode.getPosition() );
						timeEdge.setPosition_To( nodes[id].getPosition() );

						timeEdge.setOpacity( timeEdgesOpacity );

						timeEdges.push( timeEdge );
						scene.add( timeEdge.renderObject );
					}
				}

			}

		}

	}

	// STACKED GRAPHS
	function stackRepresentations() {

		var length = _visualRepresentations.length;

		// only for multiple graph visualzations
		if ( length <= 1 ) return;

		// modificators for the gap
		var constant = 0.5 * GRAVIS3D.Views.Defaults.basicView.timeSliceGapScale;
		var shrink = Math.pow( 0.92, _visualRepresentations.length );

		// add gap between visualizations
		for ( var i = 0; i < length; i++ ) {

			var vr = _visualRepresentations[( length - i - 1 )]; // place oldest slice most far away
			var layout = vr.getLayout();

			var newLayout = new GRAVIS3D.Layout.GraphLayout();
			var nodeReps = vr.getNodeRepresentations();

			// calculate a gap between visualizations
			var gap = ( layout.getWidth() + layout.getHeight() ) * constant * shrink + ( 1.5 + layout.getDepth() );
			// add 0.5 for a even count (to have the slices centered) -> odd: -1, 0, 1 / even: -0,5, 0,5
			var add = 0;
			if ( length % 2 == 0 ) add = 0.5;
			// calculate distance to the middle
			var distance = gap * ( i - ( length / 2 ) + add );
			// modify layout
			empty = true;
			for ( x in nodeReps ) {
				empty = false;
				var id = nodeReps[x].getRepresentedModel().getId();
				var v = new GRAVIS3D.Vector3D( layout.getPosition( id ).getX(), layout.getPosition( id ).getY(), layout
						.getPosition( id ).getZ()
						+ distance );
				newLayout.addPosition( id, v );
			}
			// case: no nodes defined: set layout to z-pos
			if ( empty ) {
				newLayout.setMinZ( distance );
				newLayout.setMaxZ( distance );
			}
			vr.setLayout( newLayout );

		}

	}

	// BORDERS

	function updateBorders() {
		if ( shown.borders == showBorders ) return;
		shown.borders = showBorders;
		if ( showBorders == false ) {
			for ( var obj, i = scene.children.length - 1; i >= 0; i-- ) {
				obj = scene.children[i];
				if ( obj.name == "timeSlicePlane" || obj.name == "timeSliceBorder" ) {
					scene.remove( obj );
				}
			}
		}
		if ( showBorders == true ) {
			initBorders();
		}
	}

	function initBorders() {

		if ( GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.show == false ) return;

		var length = _visualRepresentations.length;

		// calculate max width / height for all layouts
		var l = _visualRepresentation.getLayout();
		var padding = GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.padding;
		var w = l.getWidth() + 2 * padding;
		var h = l.getHeight() + 2 * padding;
		// add layer plane to scene
		for ( var i = 0; i < length; i++ ) {
			// create plane
			var planeGeom = new THREE.PlaneGeometry( w, h );
			var planeMat = new THREE.MeshBasicMaterial( {
				color : GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.color,
				side : THREE.DoubleSide,
				transparent : true,
				opacity : GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.opacity,
				depthWrite : false,
			//							depthTest : false
			} );
			// set z
			var plane = new THREE.Mesh( planeGeom, planeMat );
			var z = ( _visualRepresentations[i].getLayout().getMinZ() + _visualRepresentations[i].getLayout().getMaxZ() ) / 2;
			plane.position.z = z;
			plane.name = "timeSlicePlane";
			plane.vr = _visualRepresentations[i];
			scene.add( plane );
			// add border
			if ( GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.showBorder != false ) {
				function line( posFrom, posTo ) {
					var lineGeom = new THREE.Geometry();
					lineGeom.vertices.push( posFrom, posTo );
					lineGeom.computeLineDistances();
					var line = new THREE.Line( lineGeom, new THREE.LineBasicMaterial( {
						color : GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.borderColor,
						linewidth : GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.borderSize
					} ) );
					line.material.transparent = true;
					line.material.opacity = GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.borderOpacity;
					line.position.z = z;
					line.name = "timeSliceBorder";
					return line;
				}
				scene.add( line( planeGeom.vertices[0], planeGeom.vertices[1] ) );
				scene.add( line( planeGeom.vertices[1], planeGeom.vertices[3] ) );
				scene.add( line( planeGeom.vertices[2], planeGeom.vertices[0] ) );
				scene.add( line( planeGeom.vertices[3], planeGeom.vertices[2] ) );
			}
		}


	}

	// COLLISIONS
	var projector = new THREE.Projector();
	var INTERSECTED = {
		node : null,
		edge : null,
		slice : null
	};
	var INTERACTION = "";

	$( renderer.domElement ).on( "mousemove", function() {
		INTERACTION = "move";
		if ( controls.getState() == -1 ) collisionDetection();
	} );
	$( renderer.domElement ).on( "dblclick", function() {
		INTERACTION = "click";
		if ( controls.getState() == -1 ) collisionDetection();
	} );

	function collisionDetection() {
		// find intersections
		// create a Ray with origin at the mouse position and direction into the scene (camera direction)
		var vector = new THREE.Vector3( viewport.mouse.getX(), viewport.mouse.getY(), 1 );
		projector.unprojectVector( vector, camera );
		var ray = new THREE.Raycaster( camera.position, vector.sub( camera.position ).normalize() );

		// create an array containing all objects in the scene with which the ray intersects
		var intersects = ray.intersectObjects( scene.children, true );

		INTERSECTED.node = null;
		INTERSECTED.edge = null;
		INTERSECTED.slice = null;

		// if there is one (or more) intersections
		if ( intersects.length > 0 ) {
			document.body.style.cursor = "auto";
			for ( var i = 0; i < intersects.length; i++ ) {
				if ( intersects[i].object.name == "node" ) {
					document.body.style.cursor = "pointer";
					INTERSECTED.node = intersects[i];
					break;
				}
			}
			if ( INTERSECTED.node == null ) {
				for ( var i = 0; i < intersects.length; i++ ) {
					if ( intersects[i].object.name == "edge" ) {
						document.body.style.cursor = "pointer";
						INTERSECTED.edge = intersects[i];
						break;
					}
				}
			}
			if ( INTERSECTED.node == null && INTERSECTED.edge == null ) {
				for ( var i = 0; i < intersects.length; i++ ) {
					if ( intersects[i].object.name == "timeSlicePlane" ) {
						INTERSECTED.slice = intersects[i];
						break;
					}
				}
			}
		} else {
			document.body.style.cursor = "auto";
		}
		// handle the found collisions
		collisionHandling();
	}

	var HIGHLIGHTED = {
		node : {
			active : null,
			permanent : null
		},
		edge : {
			active : null,
			permanent : null
		},
		slice : {
			active : null,
			permanent : null
		}
	};

	function collisionHandling() {

		var permanent = false;
		if ( INTERACTION == "move" ) permanent = false;
		if ( INTERACTION == "click" ) permanent = true;

		// NODE
		if ( INTERSECTED.node != null ) {
			var modelId = INTERSECTED.node.object.parent.vr.getRepresentedModel().getId();
			if ( permanent == true ) {
				// check if a permanent highlight exists
				if ( HIGHLIGHTED.node.permanent != modelId ) {
					highlightNode( HIGHLIGHTED.node.permanent, false );
				}
				HIGHLIGHTED.node.permanent = modelId;
				nodeTooltip_simple( modelId, false );
				nodeTooltip_detailed( modelId, false );
				nodeTooltip_detailed( modelId, true );
			}
			// if active node is already highlighted -> do nothing
			if ( HIGHLIGHTED.node.active == modelId ) return;
			// remove focus from highlighted node
			highlightNode( HIGHLIGHTED.node.active, false );
			nodeTooltip_simple( HIGHLIGHTED.node.active, false );
			// set actual node to highlight 
			HIGHLIGHTED.node.active = modelId;
			highlightNode( HIGHLIGHTED.node.active, true );
			nodeTooltip_simple( HIGHLIGHTED.node.active, true );
		} else {
			if ( HIGHLIGHTED.node.active != null ) {
				if ( HIGHLIGHTED.node.permanent != HIGHLIGHTED.node.active ) {
					highlightNode( HIGHLIGHTED.node.active, false );
				}
				nodeTooltip_simple( HIGHLIGHTED.node.active, false );
				HIGHLIGHTED.node.active = null;
			}
			if ( permanent == true && HIGHLIGHTED.node.permanent != null ) {
				highlightNode( HIGHLIGHTED.node.permanent, false );
				nodeTooltip_detailed( HIGHLIGHTED.node.permanent, false );
				HIGHLIGHTED.node.permanent = null;
			}
		}
		// EDGE
		if ( INTERSECTED.edge != null ) {
			var modelId = INTERSECTED.edge.object.vr.getRepresentedModel().getId();
			if ( permanent == true ) {
				// check if a permanent highlight exists
				if ( HIGHLIGHTED.edge.permanent != modelId ) {
					highlightEdge( HIGHLIGHTED.edge.permanent, false );
				}
				HIGHLIGHTED.edge.permanent = modelId;
				edgeTooltip_simple( modelId, false );
				edgeTooltip_detailed( modelId, false );
				edgeTooltip_detailed( modelId, true );
			}
			// if active edge is already highlighted -> do nothing
			if ( HIGHLIGHTED.edge.active == modelId ) return;
			// remove focus from highlighted edge
			highlightEdge( HIGHLIGHTED.edge.active, false );
			edgeTooltip_simple( HIGHLIGHTED.edge.active, false );
			// set actual edge to highlight 
			HIGHLIGHTED.edge.active = modelId;
			highlightEdge( HIGHLIGHTED.edge.active, true );
			edgeTooltip_simple( HIGHLIGHTED.edge.active, true );
		} else {
			if ( HIGHLIGHTED.edge.active != null ) {
				if ( HIGHLIGHTED.edge.permanent != HIGHLIGHTED.edge.active ) {
					highlightEdge( HIGHLIGHTED.edge.active, false );
				}
				edgeTooltip_simple( HIGHLIGHTED.edge.active, false );
				HIGHLIGHTED.edge.active = null;
			}
			if ( permanent == true && HIGHLIGHTED.edge.permanent != null ) {
				highlightEdge( HIGHLIGHTED.edge.permanent, false );
				edgeTooltip_detailed( HIGHLIGHTED.edge.permanent, false );
				HIGHLIGHTED.edge.permanent = null;
			}
		}
		// SLICE
		if ( INTERSECTED.slice != null ) {
			var modelId = INTERSECTED.slice.object;
			if ( permanent == true ) {
				// check if a permanent highlight exists
				if ( HIGHLIGHTED.slice.permanent != modelId ) {
					highlightSlice( HIGHLIGHTED.slice.permanent, false );
				}
				HIGHLIGHTED.slice.permanent = modelId;
				sliceTooltip_simple( modelId, false );
				sliceTooltip_detailed( modelId, false );
				sliceTooltip_detailed( modelId, true );
			}
			// if active slice is already highlighted -> do nothing
			if ( HIGHLIGHTED.slice.active == modelId ) return;
			// remove focus from highlighted slice
			highlightSlice( HIGHLIGHTED.slice.active, false );
			sliceTooltip_simple( HIGHLIGHTED.slice.active, false );
			// set actual slice to highlight 
			HIGHLIGHTED.slice.active = modelId;
			highlightSlice( HIGHLIGHTED.slice.active, true );
			sliceTooltip_simple( HIGHLIGHTED.slice.active, true );
		} else {
			if ( HIGHLIGHTED.slice.active != null ) {
				if ( HIGHLIGHTED.slice.permanent != HIGHLIGHTED.slice.active ) {
					highlightSlice( HIGHLIGHTED.slice.active, false );
				}
				sliceTooltip_simple( HIGHLIGHTED.slice.active, false );
				HIGHLIGHTED.slice.active = null;
			}
			if ( permanent == true && HIGHLIGHTED.slice.permanent != null ) {
				highlightSlice( HIGHLIGHTED.slice.permanent, false );
				sliceTooltip_detailed( HIGHLIGHTED.slice.permanent, false );
				HIGHLIGHTED.slice.permanent = null;
			}
		}

	}

	function highlightNode( modelId, active ) {
		for ( var i = 0; i < _visualRepresentations.length; i++ ) {
			try {
				if ( active == true ) {
					_visualRepresentations[i].getNodeRepresentationByNodeModelId( modelId ).highlight();
				} else {
					_visualRepresentations[i].getNodeRepresentationByNodeModelId( modelId ).removeHighlight();
				}
			} catch ( err ) {
			}
		}
		for ( var i = 0; i < timeEdges.length; i++ ) {
			if ( timeEdges[i].getId() == modelId ) {
				if ( active == true ) {
					timeEdges[i].highlight();
				} else {
					timeEdges[i].removeHighlight();
				}
			}
		}
	}

	function highlightEdge( modelId, active ) {
		for ( var i = 0; i < _visualRepresentations.length; i++ ) {
			try {
				if ( active == true ) {
					_visualRepresentations[i].getEdgeRepresentationByEdgeModelId( modelId ).highlight();
				} else {
					_visualRepresentations[i].getEdgeRepresentationByEdgeModelId( modelId ).removeHighlight();
				}
			} catch ( err ) {
			}
		}
	}

	function highlightSlice( obj, active ) {
		if ( obj == null ) return;
		if ( active == true ) {
			obj.material.opacity = 0;
		} else {
			obj.material.opacity = GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.opacity;
		}
	}

	function nodeTooltip_detailed( modelId, active ) {
		if ( active ) {
			GRAVIS3D.GUI.detailedNodeTooltip( modelId, _visualRepresentation.getRepresentedDynamicGraphModel(),
					viewport.mouse.clientX + 15, viewport.mouse.clientY + 15 );
		} else {
			$( '.node_tooltip_detailed' ).remove();
		}
	}

	function nodeTooltip_simple( modelId, active ) {
		if ( active ) {
			GRAVIS3D.GUI.simpleNodeTooltip( modelId, viewport.mouse.clientX + 15, viewport.mouse.clientY + 15 );
		} else {
			$( '.node_tooltip_simple' ).remove();
		}
	}

	function edgeTooltip_detailed( modelId, active ) {
		if ( active ) {
		} else {
		}
	}

	function edgeTooltip_simple( modelId, active ) {
		if ( active ) {
			GRAVIS3D.GUI.simpleEdgeTooltip( modelId, viewport.mouse.clientX + 15, viewport.mouse.clientY + 15 );
		} else {
			$( '.edge_tooltip_simple' ).remove();
		}
	}

	function sliceTooltip_detailed( modelId, active ) {
		if ( active ) {
		} else {
		}
	}

	function sliceTooltip_simple( object, active ) {
		if ( active && camera.position.z > z_max * 1.8 ) {
			GRAVIS3D.GUI.simpleSliceTooltip( object.vr.getRepresentedGraphModel().getName(), viewport.mouse.clientX,
					viewport.mouse.clientY - 50 );
		} else {
			$( '.slice_tooltip_simple' ).remove();
		}
	}

	///////////////////////

	var isPaused = true;
	this.isPaused = function() {
		return isPaused;
	};
	this.render = function() {
		if ( isPaused == true ) {
			console.log( "GRAVIS3D.View: start rendering..." );
			threeRendering();
			if ( domElement.find( ".pauseIcon" ).length != 0 ) { // do not show play icon on init (first after a pause)
				domElement.append( ' <span class="glyphicon glyphicon-play playIcon"></span> ' );
			}
			domElement.find( ".pauseIcon" ).remove();
		}
		isPaused = false;
	};
	this.pauseRendering = function() {
		if ( isPaused != true ) {
			console.log( "GRAVIS3D.View: ...rendering paused." );
			cancelAnimationFrame( animFrame );
			domElement.append( ' <span class="glyphicon glyphicon-pause pauseIcon"></span> ' );
			domElement.find( ".playIcon" ).remove();
		}
		isPaused = true;
	};

	function threeRendering() {
		animFrame = requestAnimationFrame( threeRendering );
		renderer.render( scene, camera );
		controls.update();
		if ( _visualRepresentations ) {
			for ( var i = 0; i < _visualRepresentations.length; i++ ) {
				var graphRep = _visualRepresentations[i];
				var nodes = graphRep.getNodeRepresentations();
				for ( id in nodes ) {
					if ( nodes[id].ifShowLabel() ) nodes[id].renderObject.getObjectByName( "label", true ).lookAt(
							camera.position );
					if ( nodes[id].renderObject.getObjectByName( "highlight", true ) ) {
						nodes[id].renderObject.getObjectByName( "highlight", true ).material.uniforms.viewVector.value = camera.position;
					}
				}
			}
		}
	}

	////////////////////////

	// dom 
	var domElement = $( "<div class='view'></div>" ).append( renderer.domElement );
	if ( params.targetDomElement ) domElement.appendTo( params.targetDomElement );
	this.getDomElement = function() {
		return domElement;
	};

	// add a focus to the view element and start/stop rendering with focus-in/out
	var timer = null;
	$( domElement ).on( "mouseover", function() {
		self.render();
		if ( timer != null ) clearTimeout( timer );
	} );
	$( domElement ).on( "mouseout", function() {
		timer = window.setTimeout( function() {
			self.pauseRendering();
		}, GRAVIS3D.Views.Defaults.rendering.pauseAfter );
	} );

	// resize utils
	/**
	 * helper for getting the coordinates in browser window
	 */
	var viewport = {
		getTop : function() {
			return $( "#tabs" ).height();
		},
		getLeft : function() {
			return 0;
		},
		getWidth : function() {
			return $( domElement ).width();
		},
		getHeight : function() {
			return window.innerHeight - $( "#tabs" ).height();
		},
		mouse : {
			clientX : 0,
			clientY : 0,
			getX : function() {
				return ( ( viewport.mouse.clientX - viewport.getLeft() ) / viewport.getWidth() ) * 2 - 1;
			},
			getY : function() {
				return -( ( viewport.mouse.clientY - viewport.getTop() ) / viewport.getHeight() ) * 2 + 1;
			}
		}
	};
	renderer.domElement.addEventListener( 'mousemove', onDocumentMouseMove, false );
	function onDocumentMouseMove( e ) {
		// update the mouse variable
		viewport.mouse.clientX = e.clientX || e.pageX;
		viewport.mouse.clientY = e.clientY || e.pageY;
	}
	// tell webgl renderer width and height with changing the browser window
	window.addEventListener( 'resize', onWindowResize );
	function onWindowResize() {
		camera.aspect = viewport.getWidth() / viewport.getHeight();
		camera.updateProjectionMatrix();
		renderer.setSize( viewport.getWidth(), viewport.getHeight() );
	}
	this.handleResize = function() {
		window.setTimeout( onWindowResize, 10 );
	};

	// fullscreen mode
	this.fullscreen = function() {
		if ( THREEx.FullScreen.available() ) {
			$( renderer.domElement ).removeClass( "canvas_nofullscreen" );
			var w = window.screen.width;
			var h = window.screen.height;
			renderer.setSize( w, h );
			camera.aspect = w / h;
			camera.updateProjectionMatrix();
			THREEx.FullScreen.request( renderer.domElement );
		} else {
			new GRAVIS3D.GUI.PromptModal( {
				title : "Can not change to Fullscreen.",
				msg : "Fullscreen API isn't supported by your browser."
			} );
		}
	};
	function exitHandler() {
		if ( document.webkitIsFullScreen || document.mozFullScreen || document.msFullscreenElement !== null ) {
			if ( !THREEx.FullScreen.activated() ) {
				onWindowResize();
			}
		}
	}
	if ( document.addEventListener ) {
		document.addEventListener( 'webkitfullscreenchange', exitHandler, false );
		document.addEventListener( 'mozfullscreenchange', exitHandler, false );
		document.addEventListener( 'fullscreenchange', exitHandler, false );
		document.addEventListener( 'MSFullscreenChange', exitHandler, false );
	}
	$( renderer.domElement ).addClass( "canvas_nofullscreen" );

	///////////////////////

	if ( params.graph ) {
		if ( params.graph instanceof GRAVIS3D.Model.Graph ) {
			params.visualRepresentation = new GRAVIS3D.VisualRepresentation.Graph( {
				represents : params.graph
			} );
		} else if ( params.graph instanceof GRAVIS3D.Model.DynamicGraph ) {
			params.visualRepresentation = new GRAVIS3D.VisualRepresentation.DynamicGraph( {
				represents : params.graph
			} );
		}
	}

	if ( params.visualRepresentation ) {
		this.init( params.visualRepresentation );
	}

};
