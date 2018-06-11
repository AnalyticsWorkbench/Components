/**
 * @author Henrik Detjen
 */


/**
 * view defaults
 */
GRAVIS3D.Views.Defaults.differenceGraphView = {
	controls : THREE.OrbitControls,
	timeSliceGapScale : 1,
	timeSlicePlane : {
		show : true,
		padding : 10,
		color : "#000033",
		opacity : 0.03,
		showBorder : true,
		borderColor : "#FFFFFF",
		borderOpacity : 0.5,
		borderSize : 1
	},
	timeSliceEdges : {

	}
};


/**
 * TODO
 */
/**
 * @param params
 */
GRAVIS3D.Views.DifferenceGraphView = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data
	var self = this;

	// OPTIONS //
	var edgesLayerGroupDepth = -0.6;
	var nodesLayerGroupDepth = -0.3;
	var layer1RelDepth = 0;
	var layer2RelDepth = 0.1;
	var layer3RelDepth = 0.2;
	var minSize = 0; // the minimal area of a node
	var maxSize = 25; // the maximal area of a node
	var maxRelChange = 3; //10x is the max in color scale for relative change
	var colorShrink = "#FF3300";
	var colorGrowth = "#31B404";
	var colorIntersected = "#000000";
	var labelScale = 0.7;
	var showLabels = {
		graph1 : true,
		graph2 : true,
		both : true
	};
	var edgeOpacity = 0.6;
	var showEdges = {
		graph1 : true,
		graph2 : true,
		both : true
	};
	var borderColor = "#000";
	var borderOpacity = 0.1;
	var borderSize = 1;

	var camFactor = 20; // ZOOM

	this.controls = {
		/**
		 * everything named in this array, causes the instance gui to hide controls
		 * i.e. hide: ["filtering", "mapping"] will make these unavailable for the user in this view
		 */
		hide : [ "filtering", "mapping", "search" ],
		/**
		 * if this is not null, the returned element will be shown as "custom"-controls
		 * i.e. return $(' &lt;div&rt;  awesome view controls in here... &lt;div&rt; ');
		 * @return {jQuery}
		 */
		own : function() {
			//helper - form group
			function formGroupWrapper( label, wrappedInput, helpText ) {
				var uuid = GRAVIS3D.ID.get();
				$( wrappedInput ).attr( "id", uuid );
				if ( helpText == null ) helpText = "";
				var formGroup = $( '<div class="form-group"></div>' );
				var labelEl = $( '<label class="control-label col-sm-3" for="' + uuid + '">' + label + '</label>' )
						.appendTo( formGroup );
				var dgDiv = $( '<div class="col-sm-9"></div>' ).appendTo( formGroup );
				dgDiv.append( wrappedInput );
				GRAVIS3D.GUI.addTooltip( labelEl, helpText );
				//		if ( helpText != null ) $( '<span class="help-block">' + helpText + '</span>' ).appendTo( dgDiv );
				return formGroup;
			}

			var tsViewDiv = $( "<div></div>" );
			tsViewDiv.append( "<h2>Difference Graph<h2>" );
			tsViewDiv.append( "<hr />" );
			var panelWrapperCtrlsParent = $( "<div class='panel panel-default ' />" ).appendTo( tsViewDiv );
			var panelWrapperCtrls = $( "<div class='panel-body' />" ).appendTo( panelWrapperCtrlsParent );

			// select - graphs
			var selectGraphDiv = $( "<div class='row' />" ).appendTo( panelWrapperCtrls );
			//			selectGraphDiv.append( "<h4>Compare Graphs<h4>" );
			var selectGraph1Div = $( "<div class='col-sm-6' />" ).appendTo( selectGraphDiv );
			$( '<label for="selG1">Graph1</label>' ).appendTo( selectGraph1Div );
			var select_graph1 = $( '<select id="selG1" name="graph" type="select" class="form-control">' ).appendTo(
					selectGraph1Div );
			var graphs = _visualRepresentations;
			for ( var i = 0; i < graphs.length; i++ ) {
				$(
						'<option value="' + i.toString() + '">' + i + ' - '
								+ _visualRepresentations[i].getRepresentedGraphModel().getName() + '</option>' )
						.appendTo( select_graph1 );
			}
			select_graph1.on( "change enter", function() {
				self.GRAPH1 = _visualRepresentations[parseInt( select_graph1.val() )];
			} );
			var selectGraph2Div = $( "<div class='col-sm-6' />" ).appendTo( selectGraphDiv );
			$( '<label for="selG2">Graph2</label>' ).appendTo( selectGraph2Div );
			var select_graph2 = $( '<select id="selG2" name="graph" type="select" class="form-control">' ).appendTo(
					selectGraph2Div );
			var graphs = _visualRepresentations;
			for ( var i = 0; i < graphs.length; i++ ) {
				$(
						'<option value="' + i.toString() + '">' + i + ' - '
								+ _visualRepresentations[i].getRepresentedGraphModel().getName() + '</option>' )
						.appendTo( select_graph2 );
			}
			if ( graphs.length > 0 ) select_graph2.val( 1 );
			select_graph2.on( "change enter", function() {
				self.GRAPH2 = _visualRepresentations[parseInt( select_graph2.val() )];
			} );
			selectGraphDiv.append( "<div class='clearfix' >" );

			// select - attribute
			var selectAttributeDiv = $( "<div style='margin-top: 15px;' class='row' />" ).appendTo( panelWrapperCtrls );
			//			selectAttributeDiv.append( "<h4>Examine Attribute<h4>" );
			var select_attribute = $( '<select name="attributes" type="select" class="form-control">' );
			var attributes = self.NODE_ATTRS;
			for ( var i = 0; i < attributes.length; i++ ) {
				$(
						'<option value="' + attributes[i].getId() + '%node">' + attributes[i].getName() + " (node)"
								+ '</option>' ).appendTo( select_attribute );
			}
			var attributes2 = self.EDGE_ATTRS;
			for ( var i = 0; i < attributes2.length; i++ ) {
				$(
						'<option value="' + attributes2[i].getId() + '%edge">' + attributes2[i].getName() + " (edge)"
								+ '</option>' ).appendTo( select_attribute );
			}
			select_attribute.change( function() {
				self.ATTRIBUTE_ID = select_attribute.val().split( "%" )[0];
				self.CONTEXT = select_attribute.val().split( "%" )[1];
			} );
			formGroupWrapper( "Attribute", select_attribute ).appendTo( selectAttributeDiv );
			selectAttributeDiv.append( "<div class='clearfix' ><br />" );

			// color
			var selectColorDiv = $( "<div class='row' />" ).appendTo( panelWrapperCtrls );
			//			selectColorDiv.append( "<h4>Color<h4>" );
			var selectColorMinDiv = $( "<div class='col-sm-4' />" ).appendTo( selectColorDiv );
			$( '<h5 for="selCMin">Graph 1</h5>' ).appendTo( selectColorMinDiv );
			var selectColorMin = $(
					'<input value="' + colorShrink + '" name="size" id="selCMin" type="color" class="form-control" >' )
					.appendTo( selectColorMinDiv );
			selectColorMin.on( "change enter keyup", function() {
				colorShrink = selectColorMin.val();
			} );
			var selectColormiddleDiv = $( "<div class='col-sm-4' />" ).appendTo( selectColorDiv );
			$( '<h5 for="selCmiddle">Both</h5>' ).appendTo( selectColormiddleDiv );
			var selectColormiddle = $(
					'<input value="' + colorIntersected
							+ '" name="size" id="selCmiddle" type="color" class="form-control" >' ).appendTo(
					selectColormiddleDiv );
			selectColormiddle.on( "change enter keyup", function() {
				colorIntersected = selectColormiddle.val();
			} );
			var selectColormaxDiv = $( "<div class='col-sm-4' />" ).appendTo( selectColorDiv );
			$( '<h5 for="selCmax">Graph 2</h5>' ).appendTo( selectColormaxDiv );
			var selectColormax = $(
					'<input value="' + colorGrowth + '" name="size" id="selCmax" type="color" class="form-control" >' )
					.appendTo( selectColormaxDiv );
			selectColormax.on( "change enter keyup", function() {
				colorGrowth = selectColormax.val();
			} );

			// labels?
			// graph1
			var other_options1 = $( "<div class='' />" ).appendTo( selectColorMinDiv );
			var cb_div1 = $( '<div class="checkbox" />' ).appendTo( other_options1 );
			var cb1 = $( '<label><input type="checkbox" checked="' + showLabels.graph1 + '"> Labels</label>' )
					.appendTo( cb_div1 );
			cb1.find( 'input' ).on( "change click enter", function() {
				showLabels.graph1 = cb1.find( 'input' ).is( ":checked" );
			} );
			// both
			var other_options2 = $( "<div class='' />" ).appendTo( selectColormiddleDiv );
			var cb_div2 = $( '<div class="checkbox" />' ).appendTo( other_options2 );
			var cb2 = $( '<label><input type="checkbox" checked="' + showLabels.both + '"> Labels</label>' ).appendTo(
					cb_div2 );
			cb2.find( 'input' ).on( "change click enter", function() {
				showLabels.both = cb2.find( 'input' ).is( ":checked" );
			} );
			// graph2
			var other_options3 = $( "<div class='' />" ).appendTo( selectColormaxDiv );
			var cb_div3 = $( '<div class="checkbox" />' ).appendTo( other_options3 );
			var cb3 = $( '<label><input type="checkbox" checked="' + showLabels.graph2 + '"> Labels</label>' )
					.appendTo( cb_div3 );
			cb3.find( 'input' ).on( "change click enter", function() {
				showLabels.graph2 = cb3.find( 'input' ).is( ":checked" );
			} );

			// edges?
			// graph1
			var other_options01 = $( "<div class='' />" ).appendTo( selectColorMinDiv );
			var cb_div01 = $( '<div class="checkbox" />' ).appendTo( other_options01 );
			var cb01 = $( '<label><input type="checkbox" checked="' + showEdges.graph1 + '"> Edges</label>' ).appendTo(
					cb_div01 );
			cb01.find( 'input' ).on( "change click enter", function() {
				showEdges.graph1 = cb01.find( 'input' ).is( ":checked" );
			} );
			// both
			var other_options02 = $( "<div class='' />" ).appendTo( selectColormiddleDiv );
			var cb_div02 = $( '<div class="checkbox" />' ).appendTo( other_options02 );
			var cb02 = $( '<label><input type="checkbox" checked="' + showEdges.both + '"> Edges</label>' ).appendTo(
					cb_div02 );
			cb02.find( 'input' ).on( "change click enter", function() {
				showEdges.both = cb02.find( 'input' ).is( ":checked" );
			} );
			// graph2
			var other_options03 = $( "<div class='' />" ).appendTo( selectColormaxDiv );
			var cb_div03 = $( '<div class="checkbox" />' ).appendTo( other_options03 );
			var cb03 = $( '<label><input type="checkbox" checked="' + showEdges.graph2 + '"> Edges</label>' ).appendTo(
					cb_div03 );
			cb03.find( 'input' ).on( "change click enter", function() {
				showEdges.graph2 = cb03.find( 'input' ).is( ":checked" );
			} );


			//			var selectRelChangeMaxIn = $( '<input value="' + maxRelChange
			//					+ '" name="size" type="number" class="form-control" min="0" max="20" step="1">' );
			//			selectRelChangeMaxIn.on( "change enter keyup", function() {
			//				maxRelChange = parseFloat( selectRelChangeMaxIn.val() );
			//			} );
			//			formGroupWrapper( "Relative Change Range (x-times)", selectRelChangeMaxIn ).appendTo( selectColorDiv );
			selectColorDiv.append( "<div class='clearfix' >" );

			//			// size
			//			var selectSizeDiv = $( "<div class='row' />" ).appendTo( panelWrapperCtrls );
			//			//			selectSizeDiv.append( "<h4>Size<h4>" );
			//			var selectSizeMinDiv = $( "<div class='col-sm-6' />" ).appendTo( selectSizeDiv );
			//			$( '<label for="selSMin">Min-Size</label>' ).appendTo( selectSizeMinDiv );
			//			var selectSizeMin = $(
			//					'<input value="'
			//							+ minSize
			//							+ '" name="size" id="selSMin" type="number" class="form-control" min="0.5" max="5" step="0.1">' )
			//					.appendTo( selectSizeMinDiv );
			//			selectSizeMin.on( "change enter keyup", function() {
			//				minSize = parseFloat( selectSizeMin.val() );
			//			} );
			//			var selectSizemaxDiv = $( "<div class='col-sm-6' />" ).appendTo( selectSizeDiv );
			//			$( '<label for="selSmax">Max-Size</label>' ).appendTo( selectSizemaxDiv );
			//			var selectSizemax = $(
			//					'<input value="'
			//							+ maxSize
			//							+ '" name="size" id="selSmax" type="number" class="form-control" min="0.5" max="5" step="0.1">' )
			//					.appendTo( selectSizemaxDiv );
			//			selectSizemax.on( "change enter keyup", function() {
			//				maxSize = parseFloat( selectSizemax.val() );
			//			} );
			//
			//			panelWrapperCtrls.append( "<div class='clearfix' ><hr/>" );


			panelWrapperCtrls.append( "<div class='clearfix' ><hr/>" );
			// apply changes
			var applyBtn = $(
					'<button type="button" id="dg_applyBtn" class="btn btn-primary btn"><span class="glyphicon glyphicon-ok"></span> Apply Changes</button>' )
					.appendTo( panelWrapperCtrls );
			applyBtn.on( "click enter", function() {
				applyBtn.attr( "disabled", true );
				var btnhtmltmp = applyBtn.html();
				applyBtn.html( "updating..." );
				window.setTimeout( function() {
					update( self.GRAPH2, self.GRAPH1 );
					onWindowResize();
					applyBtn.prop( "disabled", false );
					applyBtn.html( btnhtmltmp );
				}, 10 );
			} );

			//			tsViewDiv.append( "<hr />" );
			//			tsViewDiv.append( "<h4>Explanation</h4>" );
			//			tsViewDiv
			//					.append( "<p>Compares 2 time slices in selected attributes changes. Nodes and edges, which are contained in both slices will be displayed overlayed. For instance: A node, which has same values in both slices will be displayed in intersection color and and node with growing values will show a intersection with a outer ring in the growing color (its strength depends on relative change).</p>" );
			//			tsViewDiv
			//					.append( "<p><strong>Size</strong>: Absolute changes. The minimal value of the slices will be mapped to the minimal size and the maximal value to the maximal size</p>" );
			//			tsViewDiv
			//					.append( "<p><strong>Color</strong>: Relative changes.The minimal change (0) is displayed in the intersection color. Maximal changes will be mapped to the maximal color and minimal changes to the minimal color.</p>" );
			//			tsViewDiv.append( "<br>" );
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
	var g = null; // visrep render objs. will be appended to this
	var animFrame = null;
	var controls = null;

	/////////////////////

	var _visualRepresentations = null;
	var _visualRepresentation = null;

	this.ATTRIBUTE_ID = "";
	this.CONTEXT = "";
	this.GRAPH1 = null;
	this.GRAPH2 = null;
	this.DYN_GRAPH = null;
	this.NODE_ATTRS = null;
	this.EDGE_ATTRS = null;

	var layer1;
	var layer2;
	var layer3;


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
		} else throw new Error( "GRAVS3D.Views.DifferenceGraphView: No proper Visual Representation passed... "
				+ ( visualRepresentation ) );
		_visualRepresentation = visualRepresentation;

		// set graph 1 + 2
		if ( this.GRAPH1 == null ) this.GRAPH1 = _visualRepresentations[0];
		if ( this.GRAPH2 == null && _visualRepresentations[1] ) {
			this.GRAPH2 = _visualRepresentations[1];
		} else this.GRAPH2 = _visualRepresentations[0];
		this.DYN_GRAPH = _visualRepresentation.getRepresentedDynamicGraphModel();
		this.NODE_ATTRS = this.DYN_GRAPH.getAttributesAsArray( "node" );
		this.EDGE_ATTRS = this.DYN_GRAPH.getAttributesAsArray( "edge" );

		// init attributes 
		if ( this.NODE_ATTRS.length < 1 ) {
			if ( this.EDGE_ATTRS.length < 1 ) {
				GRAVIS3D.GUI
						.PromptModal( {
							title : "Error",
							msg : '<div class="alert alert-danger alert-dismissible" role="alert"><p>No attributes found.</p></div>'
						} );
				return;
			} else {
				this.ATTRIBUTE_ID = this.EDGE_ATTRS[0].getId();
				this.CONTEXT = "edge";
			}
		} else {
			this.ATTRIBUTE_ID = this.NODE_ATTRS[0].getId();
			this.CONTEXT = "node";
		}

		update();

	};


	function update() {
		// create scene
		scene = new THREE.Scene();
		initBorder();
		initCompareView( self.GRAPH1, self.GRAPH2, self.ATTRIBUTE_ID, self.CONTEXT );

		// init camera
		var savedCamPos = null;
		if ( camera != null ) {
			savedCamPos = camera.position;
		}

		var width = window.outerWidth;
		var height = window.outerHeight;
		camera = new THREE.OrthographicCamera( width / -camFactor, width / camFactor, height / camFactor, height
				/ -camFactor, 1, 100 );
		renderer.setSize( width, height );
		if ( savedCamPos == null ) camera.position.z = 1;//500;
		else {
			camera.position.x = savedCamPos.x;
			camera.position.y = savedCamPos.y;
			camera.position.z = savedCamPos.z;
		}
		if ( controls == null ) initControls();
	}

	function initControls() {
		// init controls
		controls = new THREE.OrthographicTrackballControls( camera, renderer.domElement );
		controls.noRotate = true;
		controls.panSpeed = 8;
		controls.zoomSpeed = 0.6;
	}

	// lay g2 behind g1
	function initCompareView( g1, g2, attributeId, context ) {
		// LAYER 1 -> UNDERLAYING MODEL
		var graphModel1 = g1.getRepresentedGraphModel();
		var edges1 = g1.getEdgeRepresentations();
		var nodes1 = g1.getNodeRepresentations();
		var attribute1 = graphModel1.getAttributeById( attributeId, context );
		var attrType = attribute1.getType();
		var attrMin1 = 1;
		var attrMax1 = 1;
		if ( attrType == "number" ) {
			attrMin1 = attribute1.getRange().getMin();
			attrMax1 = attribute1.getRange().getMax();
		}

		// LAYER 2 -> UNDERLAYING MODEL
		var graphModel2 = g2.getRepresentedGraphModel();
		var nodes2 = g2.getNodeRepresentations();
		var edges2 = g2.getEdgeRepresentations();
		var attribute2 = graphModel2.getAttributeById( attributeId, context );
		var attrMin2 = 1;
		var attrMax2 = 1;
		if ( attrType == "number" ) {
			attrMin2 = attribute2.getRange().getMin();
			attrMax2 = attribute2.getRange().getMax();
		}

		var normScale = d3.scale.linear();
		normScale.domain( [ 0, d3.max( [ attrMax1, attrMax2 ] ) ] );//d3.min( [ attrMin1, attrMin2 ] )
		normScale.range( [ minSize, maxSize ] );
		normScale.clamp( true );

		function valueAsArea( value ) {
			// normalize value first
			value = normScale( value );
			//			console.log(value+"->"+Math.sqrt( ( value / Math.PI ) ))
			if ( value == 0 ) return 0;
			return Math.sqrt( ( value / Math.PI ) );
		}

		var colorScaleGrowth = d3.scale.linear().domain( [ 0, 0 ] ).range( [ colorGrowth, colorGrowth ] ).clamp( true );
		var colorScaleShrink = d3.scale.linear().domain( [ 0, 0 ] ).range( [ colorShrink, colorShrink ] ).clamp( true );

		// init layers
		layer1 = new GRAVIS3D.VisualRepresentation.Graph();
		layer2 = new GRAVIS3D.VisualRepresentation.Graph();
		layer3 = new GRAVIS3D.VisualRepresentation.Graph();


		// graph1 SNAPSHOT LOOKED AT
		// set nodes..
		for ( id in nodes1 ) {

			// - LAYER 1 NODES -

			var nodeModel = nodes1[id].getRepresentedModel();
			var attrVal = nodeModel.getAttributeValues()[attributeId];
			var size = 1;
			if ( attrType == "number" && context == "node" ) {
				size = valueAsArea( attrVal );
			}

			//build node
			var layer1Node = new GRAVIS3D.VisualRepresentation.Node( {
				represents : nodeModel,
				color : colorShrink,
				size : size,
				position : new GRAVIS3D.Vector3D( nodes1[id].getPosition().getX(), nodes1[id].getPosition().getY(),
						nodesLayerGroupDepth + layer1RelDepth ),
				form : "circle"
			} );
			var label = attrVal;
			if ( attrType == "number" ) label = d3.round( attrVal, 4 );
			if ( context == "node" ) {
				layer1Node.setLabel( nodeModel.getName() + " (" + label + ")" );
			}

			layer1Node.setLabelScale( labelScale );
			layer1Node.setIfShowLabel( showLabels.graph1 );

			layer1.addNodeRepresentation( layer1Node );
			scene.add( layer1Node.renderObject );

		}

		// set edges
		for ( id in edges1 ) {

			// - LAYER 1 EDGES -

			var edgeModel = edges1[id].getRepresentedModel();
			var attrVal = edgeModel.getAttributeValues()[attributeId];
			var size = 1;
			if ( attrType == "number" && context == "edge" ) {
				size = valueAsArea( attrVal );
			}

			var op = edgeOpacity;
			if ( showEdges.graph1 == false ) op = 0;
			var layer1Edge = new GRAVIS3D.VisualRepresentation.Edge( {
				source : edges1[id].getSource(),
				target : edges1[id].getTarget(),
				represents : edgeModel,
				color : colorShrink,
				opacity : op,
				size : size,
				positionFrom : new GRAVIS3D.Vector3D( edges1[id].getPosition_From().getX(), edges1[id]
						.getPosition_From().getY(), edgesLayerGroupDepth + layer1RelDepth ),
				positionTo : new GRAVIS3D.Vector3D( edges1[id].getPosition_To().getX(), edges1[id].getPosition_To()
						.getY(), edgesLayerGroupDepth + layer1RelDepth )
			} );

			layer1.addEdgeRepresentation( layer1Edge );
			scene.add( layer1Edge.renderObject );
		}

		// graph2 SNAPSHOT LOOKED NOT AT
		// set nodes
		for ( id in nodes2 ) {

			// - LAYER 2 NODES - 
			var nodeModel = nodes2[id].getRepresentedModel();
			var attrVal2 = nodeModel.getAttributeValues()[attributeId];
			var size = 1;
			if ( attrType == "number" && context == "node" ) {
				size = valueAsArea( attrVal2 );
			}

			var layer2Node = new GRAVIS3D.VisualRepresentation.Node( {
				represents : nodeModel,
				color : colorGrowth,
				size : size,
				position : new GRAVIS3D.Vector3D( nodes2[id].getPosition().getX(), nodes2[id].getPosition().getY(),
						nodesLayerGroupDepth + layer2RelDepth ),
				form : "circle"
			} );
			var label = attrVal2;
			if ( attrType == "number" ) label = d3.round( attrVal2, 4 );
			if ( context == "node" ) {
				layer2Node.setLabel( nodeModel.getName() + " (" + label + ")" );
			}
			layer2Node.setLabelScale( labelScale );
			layer2Node.setIfShowLabel( showLabels.graph2 );

			layer2.addNodeRepresentation( layer2Node );
			scene.add( layer2Node.renderObject );

			// - LAYER 3 NODES -
			try {

				// found
				var node1 = layer1.getNodeRepresentationByNodeModelId( nodeModel.getId() );
				var attrVal1 = node1.getRepresentedModel().getAttributeValues()[attributeId];
				var node2 = layer2.getNodeRepresentationByNodeModelId( nodeModel.getId() );

				// from t1 -> t2 more?
				var growing = false;
				var shrinking = false;
				var same = false;
				if ( attrVal1 > attrVal2 ) {
					shrinking = true;
				} else if ( attrVal1 < attrVal2 ) {
					growing = true;
				} else if ( attrVal1 == attrVal2 ) {
					same = true;
				}

				// find out, if layer 1 or layer 2 have a bigger node size => set roles accordingly
				// (remove invisible one)
				var overLayingNode; // the smaller node is drawn with overlay-color on layer 3
				var overLayedNode;
				if ( node1.getSize() == d3.min( [ node1.getSize(), node2.getSize() ] ) ) {
					overLayingNode = node1;//=smaller/shrinking node!
					overLayedNode = node2;//=bigger/growing
				} else {
					overLayingNode = node2;
					overLayedNode = node1;
				}

				// build a new node on top layer..
				intersectionLayerNode = new GRAVIS3D.VisualRepresentation.Node( {
					represents : nodeModel,
					color : colorIntersected, // default : black
					size : overLayingNode.getSize(), // smaller node size -> black
					position : new GRAVIS3D.Vector3D( overLayedNode.getPosition().getX(), overLayedNode.getPosition()
							.getY(), nodesLayerGroupDepth + layer3RelDepth ),
					form : "circle",
					opacity : 0.999
				} );

				if ( same == true ) {
					var label = attrVal1;
					if ( attrType == "number" ) label = d3.round( attrVal1, 4 );
					if ( context == "node" ) {
						intersectionLayerNode.setLabel( nodeModel.getName() + " (" + label + ")" );
					}
					overLayedNode.setOpacity( 0 );
				}
				if ( growing == true ) {
					intersectionLayerNode.setLabel( "(" + d3.round( attrVal1, 4 ) + ")" );
					//					relChange = attrVal2 / attrVal1;
					//					overLayedNode.setColor( colorScaleGrowth( relChange ) );
				}
				if ( shrinking == true ) {
					intersectionLayerNode.setLabel( "(" + d3.round( attrVal2, 4 ) + ")" );
					//					relChange = attrVal1 / attrVal2;
					//					overLayedNode.setColor( colorScaleShrink( relChange ) );
				}
				intersectionLayerNode.setIfShowLabel( showLabels.both );
				intersectionLayerNode.setLabelScale( labelScale );

				// hide the node, we copied to layer 3 (smaller one)
				overLayingNode.setOpacity( 0 );

				layer3.addNodeRepresentation( intersectionLayerNode );
				scene.add( intersectionLayerNode.renderObject );
			} catch ( e ) {
				// not found
			}

		}


		// set edges
		for ( id in edges2 ) {

			// - LAYER 2 EDGES -

			var edgeModel = edges2[id].getRepresentedModel();

			var attrVal = edgeModel.getAttributeValues()[attributeId];
			var size = 1;
			if ( attrType == "number" && context == "edge" ) {
				size = valueAsArea( attrVal );
			}

			var op = edgeOpacity;
			if ( showEdges.graph2 == false ) op = 0;
			var layer2Edge = new GRAVIS3D.VisualRepresentation.Edge( {
				source : edges2[id].getSource(),
				target : edges2[id].getTarget(),
				represents : edgeModel,
				color : colorGrowth,
				opacity : op,
				size : size,
				positionFrom : new GRAVIS3D.Vector3D( edges2[id].getPosition_From().getX(), edges2[id]
						.getPosition_From().getY(), edgesLayerGroupDepth + layer2RelDepth ),
				positionTo : new GRAVIS3D.Vector3D( edges2[id].getPosition_To().getX(), edges2[id].getPosition_To()
						.getY(), edgesLayerGroupDepth + layer2RelDepth )
			} );

			layer2.addEdgeRepresentation( layer2Edge );
			scene.add( layer2Edge.renderObject );

			// - LAYER 3 EDGES -		

			try {
				// found
				var edge1 = layer1.getEdgeRepresentationByEdgeModelId( edgeModel.getId() );
				var edge2 = layer2.getEdgeRepresentationByEdgeModelId( edgeModel.getId() );

				// find out, if layer 1 or layer 2 have a bigger edge size => set roles accordingly
				var overLayingEdge; // the smaller edge is drawn with overlay-color on layer 3
				var overLayedEdge;
				if ( edge1.getSize() == d3.min( [ edge1.getSize(), edge2.getSize() ] ) ) {
					overLayingEdge = edge1;
					overLayedEdge = edge2;
				} else {
					overLayingEdge = edge2;
					overLayedEdge = edge1;
				}

				// build a new edge on top layer..
				var op = edgeOpacity;
				if ( showEdges.both == false ) op = 0;
				intersectionLayerEdge = new GRAVIS3D.VisualRepresentation.Edge( {
					source : edges2[id].getSource(),
					target : edges2[id].getTarget(),
					represents : edgeModel,
					color : colorIntersected, // default : black
					opacity : op,
					size : overLayingEdge.getSize(), // smaller edge size -> black
					positionFrom : new GRAVIS3D.Vector3D( overLayedEdge.getPosition_From().getX(), overLayedEdge
							.getPosition_From().getY(), edgesLayerGroupDepth + layer3RelDepth ),
					positionTo : new GRAVIS3D.Vector3D( overLayedEdge.getPosition_To().getX(), overLayedEdge
							.getPosition_To().getY(), edgesLayerGroupDepth + layer3RelDepth )
				} );

				// hide the edge, we copied to layer 3 (smaller one)
				overLayingEdge.setOpacity( 0 );
				if ( attrVal == edge2.getRepresentedModel().getAttributeValues()[attributeId] ) {
					overLayedEdge.setOpacity( 0 );
				}
				layer3.addEdgeRepresentation( intersectionLayerEdge );
				scene.add( intersectionLayerEdge.renderObject );
			} catch ( e ) {
				// not found
			}


		}

	}



	// BORDER
	function initBorder() {

		if ( GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.show == false ) return;

		var length = _visualRepresentations.length;

		// calculate max width / height for all layouts
		var l = _visualRepresentation.getLayout();
		var padding = GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.padding;
		var w = l.getWidth() + 2 * padding;
		var h = l.getHeight() + 2 * padding;
		// add layer plane to scene
		for ( var i = 0; i < 1; i++ ) {
			// create plane
			var planeGeom = new THREE.PlaneGeometry( w, h );
			var z = edgesLayerGroupDepth + layer3RelDepth + layer1RelDepth;
			// add border
			if ( GRAVIS3D.Views.Defaults.basicView.timeSlicePlane.showBorder != false ) {
				function line( posFrom, posTo ) {
					var lineGeom = new THREE.Geometry();
					lineGeom.vertices.push( posFrom, posTo );
					lineGeom.computeLineDistances();
					var line = new THREE.Line( lineGeom, new THREE.LineBasicMaterial( {
						color : borderColor,
						linewidth : borderSize
					} ) );
					line.material.transparent = true;
					line.material.opacity = borderOpacity;
					line.position.z = z;
					return line;
				}
				scene.add( line( planeGeom.vertices[0], planeGeom.vertices[1] ) );
				scene.add( line( planeGeom.vertices[1], planeGeom.vertices[3] ) );
				scene.add( line( planeGeom.vertices[2], planeGeom.vertices[0] ) );
				scene.add( line( planeGeom.vertices[3], planeGeom.vertices[2] ) );
			}
		}


	}

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
				}
			}
		}
	}

	////////////////////////

	var domElement = $( "<div class='view'></div>" );
	var canvasWrapper = $( '<div class="canvasWrapper"></div>' ).append( renderer.domElement );
	canvasWrapper.appendTo( domElement );
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

	// tell webgl renderer width and height with changing the browser window
	window.addEventListener( 'resize', onWindowResize );

	function onWindowResize() {

		if ( THREEx.FullScreen.activated() ) return;


		var height = window.innerHeight - $( "#tabs" ).height();
		var width = $( domElement ).width();

		renderer.setSize( width, height );

		// update the camera
		camera.left = -width / camFactor;
		camera.right = width / camFactor;
		camera.top = height / camFactor;
		camera.bottom = -height / camFactor;
		camera.updateProjectionMatrix();

		/**
		 * forces the controls to update its internal camera representation and sets size to domElement..
		 * have a look at /js/three/OrthographicTrackballControls.js
		 */
		controls.handleResize( camera );

	}
	this.handleResize = function() {
		onWindowResize();
	};


	this.fullscreen = function() {
		if ( THREEx.FullScreen.available() ) {
			var h = window.screen.height;
			var w = window.screen.width;
			renderer.setSize( w, h );
			camera.left = -w / camFactor;
			camera.right = w / camFactor;
			camera.top = h / camFactor;
			camera.bottom = -h / camFactor;
			camera.updateProjectionMatrix();
			controls.handleResize( camera );
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
				setTimout( onWindowResize(), 100 );
			}
		}
	}
	if ( document.addEventListener ) {
		document.addEventListener( 'webkitfullscreenchange', exitHandler, false );
		document.addEventListener( 'mozfullscreenchange', exitHandler, false );
		document.addEventListener( 'fullscreenchange', exitHandler, false );
		document.addEventListener( 'MSFullscreenChange', exitHandler, false );
	}

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
