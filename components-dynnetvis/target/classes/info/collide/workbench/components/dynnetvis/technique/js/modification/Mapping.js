/**
 * @author henrik detjen
 */

/**
 * @namespace holds all classes used for mapping of visual representations 
 * @nameSpace
 */
GRAVIS3D.Mapping = {};

GRAVIS3D.Mapping.MappingManagerGUI = function( mappingManager ) {

	var manager = mappingManager;
	var self = this;
	var guiEl = $( "<div class='mappingManager' />" );

	//helper - form group
	function formGroupWrapper( label, wrappedInput, helpText ) {
		var uuid = GRAVIS3D.ID.get();
		$( wrappedInput ).attr( "id", uuid );
		if ( helpText == null ) helpText = "";
		var formGroupW = $( '<div class="form-horizontal"></div>' );
		var formGroup = $( '<div class="form-group"></div>' ).appendTo( formGroupW );
		var labelEl = $( '<label class="control-label col-sm-6" for="' + uuid + '">' + label + '</label>' ).appendTo(
				formGroup );
		var dgDiv = $( '<div class="col-sm-6"></div>' ).appendTo( formGroup );
		dgDiv.append( wrappedInput );
		GRAVIS3D.GUI.addTooltip( labelEl, helpText );
		//		if ( helpText != null ) $( '<span class="help-block">' + helpText + '</span>' ).appendTo( dgDiv );
		return formGroupW;
	}

	// begin..

	//head
	//	$('<h3>Mappings</h3><hr>').appendTo(guiEl);
	var navTabs = $( '<ul id="mappingTabs" class="nav nav-tabs" role="tablist">' ).appendTo( guiEl );

	//tabs
	function createTab( title, id, target ) {
		var tab1 = $( '<a href="#' + target + '" id="' + id + '" class="mappingTab" role="tab" data-toggle="tab">'
				+ title + '</a>' );
		$( '<li></li>' ).append( tab1 ).appendTo( navTabs );
		var clickTimer1 = null;
		tab1.on( "mouseover", function() {
			clickTimer1 = setTimeout( function() {
				tab1.trigger( "click" );
			}, GRAVIS3D.GUI.Defaults.tabs.clickTriggerDelay );
		} );
		tab1.on( "mouseout", function() {
			clearTimeout( clickTimer1 );
		} );
	}
	var id1 = GRAVIS3D.ID.get();
	var id2 = GRAVIS3D.ID.get();
	// the target id must match the content panel's id..
	createTab( "Mappings - Nodes", "mappings_nodeTab", id1 );
	createTab( "Mappings - Edges", "mappings_edgeTab", id2 );

	//content
	var content = $( '<div class="tab-content">' ).appendTo( guiEl );

	//panels
	function createPanel( id ) {
		var tabPane = $( '<div class="tab-pane fade" id="' + id + '"></div>' ).appendTo( content );
		var divWrapper = $( '<div class=""></div>' ).appendTo( tabPane );
		var panel = $( '<div class="panel panel-default"></div>' ).appendTo( divWrapper );
		var contentDiv = $( '<div class="panel-body"></div>' ).appendTo( panel );
		return contentDiv;
	}
	var nodePanel = createPanel( id1 );
	var edgePanel = createPanel( id2 );

	// table with all mappings
	function mappingList( mappings ) {

		// table
		var tableDiv = $( '<div class="table-responsive"></div>' );
		var mappingTable = $( '<table class="table table-hover table-condensed">' ).appendTo( tableDiv );

		// table - header
		var mappingTableHeadEl = $( '<thead></thead>' ).appendTo( mappingTable );
		var mappingTableHead = $( '<tr></tr>' ).appendTo( mappingTableHeadEl );
		$( '<th>Attribute</th>' ).appendTo( mappingTableHead );
		$( '<th>Visual Variable</th>' ).appendTo( mappingTableHead );
		$( '<th></th>' ).appendTo( mappingTableHead );
		$( '<th></th>' ).appendTo( mappingTableHead );

		// table - body
		var mappingTableBody = $( '<tbody></tbody>' ).appendTo( mappingTable );

		for ( var i = 0; i < mappings.length; i++ ) {
			mappingTableBody.append( createMappingEl( mappings[i] ) );
		}
		tableDiv.append( addBtn( mappings[0].isFor() ) );

		return tableDiv;

	}

	// a mapping el
	function createMappingEl( mapping ) {

		var mappingEl = $( '<tr></tr>' );

		var attrs = mapping.getGraphModel().getAttributes( mapping.isFor() );
		var attr = attrs[mapping.getAttributeId()];
		var attrType = attr.getType();
		var visvars = GRAVIS3D.Mapping.AttributeMappingDefaults[mapping.isFor()][attrType];
		var visVar = mapping.getVisualVariable();
		var vals = attr.getPossibleValues();
		var mappingParams = mapping.getMappingParams();

		// select - attribute id
		var td_attr = $( "<td></td>" ).appendTo( mappingEl );
		var select_attributes = $(
				'<select name="mapping_attributes" type="select" class="form-control mappingControl">' ).appendTo(
				td_attr );
		for ( id in attrs ) {
			
			var displayedName = id;
			if ( attrs[id].getName().length > 0 ) displayedName = attrs[id].getName();
			$( '<option value="' + id + '">' + displayedName + '</option>' ).appendTo( select_attributes );
		}
		select_attributes.val( mapping.getAttributeId() );
		select_attributes.change( function() {
			mapping.setAttributeId( select_attributes.val() );
			mapping.setVisualVariable( Object.keys( visvars )[0] );
			self.update();
		} );

		// select - visual variable
		var td_visvar = $( "<td></td>" ).appendTo( mappingEl );
		var select_visvar = $(
				'<select name="mapping_visualVariables" type="select" class="form-control mappingControl">' ).appendTo(
				td_visvar );
		for ( id in visvars ) {
			$( '<option value="' + id + '">' +firstLetterUp( id) + '</option>' ).appendTo( select_visvar );
		}
		select_visvar.val( mapping.getVisualVariable() );
		select_visvar.change( function() {
			mapping.setVisualVariable( select_visvar.val() );
			self.update();
		} );

		// button edit mapping
		var td_edit = $( "<td></td>" ).appendTo( mappingEl );
		var btn_edit = $(
				'<button type="button" name="remove" class="btn btn-primary btn-xs mappingControl"><span class="glyphicon glyphicon-edit"></span></button>' )
				.appendTo( td_edit );
		btn_edit.on( "click enter", function() {
			changeParamsDialog();
		} );

		// mapping params
		function changeParamsDialog( callback ) {

			var selectionDiv = $( "<div class='row' />" );
			var selection = $( "<div class='col-sm-12' />" ).appendTo( selectionDiv );
			var toSet = null;

			function input() {

				// create different inputs for vis vars
				var selectVars = [ "labelFont", "texture", "form", "showLabel" ];
				var colorVars = [ "color" ];
				var numberVars = [ "size", "opacity", "labelScale", "orientation", "brightness" ];

				var input;
				if ( colorVars.indexOf( visVar ) != -1 ) input = $( "<input type='color' class='form-control'>" );
				else if ( numberVars.indexOf( visVar ) != -1 ) input = $( "<input type='number' min='0' step='0.1' class='form-control'>" );
				else if ( selectVars.indexOf( visVar ) != -1 ) {
					input = $( "<select type='select' class='form-control'>" );
					var visVarVals = visvars[visVar];
					if ( visVar == "form" && attr.isFor() == "node" ) visVarVals = GRAVIS3D.VisualRepresentation.Defaults.node.possibleForms;
					if ( visVar == "texture" && attr.isFor() == "node" ) visVarVals = GRAVIS3D.VisualRepresentation.Defaults.node.possibleTextures;
					if ( visVar == "labelFont" && attr.isFor() == "node" ) visVarVals = GRAVIS3D.VisualRepresentation.Defaults.node.possibleLabelFonts;
					if ( visVar == "form" && attr.isFor() == "edge" ) visVarVals = GRAVIS3D.VisualRepresentation.Defaults.edge.possibleTextures;
					for ( var i = 0; i < visVarVals.length; i++ ) {
						input.append( "<option value='" + visVarVals[i] + "'>" + firstLetterUp(visVarVals[i]) + "</option>" )
					}
				} else input = $( "<input type='text' class='form-control'>" );
				return input;
			}

			//switch ( attrType ) {

				//TODO: validation with user-feedback
				//case "number":
            if (attrType == "number") {
                // select min
                var sel_Min = input();
                formGroupWrapper("Min", sel_Min).appendTo(selection);
                sel_Min.val(mappingParams.min);
                // select max
                var sel_Max = input();
                formGroupWrapper("Max", sel_Max).appendTo(selection);
                sel_Max.val(mappingParams.max);
                // change
                sel_Min.add(sel_Max).on("change click enter", function () {
                    toSet = {
                        min: sel_Min.val(),
                        max: sel_Max.val()
                    };
                });
                //	break;
                //case "string":
            } else if (attrType == "string") {
                var scale = d3.scale.ordinal();
                scale.domain(Object.keys(vals));
                scale.range(mappingParams);
                var _range = [];
                counter = 0;
                for (id in vals) {
                    var inp = input();
                    formGroupWrapper(id, inp).appendTo(selection);
                    inp.val(scale(id));
                    inp.prop("counter", counter++);
                    _range.push(scale(id));
                    inp.on("change click enter", function () {
                        _range[parseFloat($(this).prop("counter"))] = $(this).val();
                        toSet = _range;
                    });
                }
                //	break;
                //case "cluster":
            } else if (attrType == "cluster") {
                toSet = {};
                var _range = [];
                // select overlap
                var sel_O = input();
                formGroupWrapper("Overlap", sel_O).appendTo(selection);
                sel_O.val(mappingParams.overlap);
                // select none
                var sel_None = input();
                formGroupWrapper("None", sel_None).appendTo(selection);
                sel_None.val(mappingParams.none);
                // change
                sel_O.add(sel_None).on("change click enter", function () {
                    toSet.none = sel_None.val();
                    toSet.overlap = sel_O.val();
                    toSet.range = _range;
                });
                // rest like string
                var scale = d3.scale.ordinal();
                scale.domain(Object.keys(vals));
                scale.range(mappingParams.range);
                counter = 0;
                for (id in vals) {
                    if (id != "") {
                        var inp = input();
                        formGroupWrapper(id, inp).appendTo(selection);
                        inp.val(scale(id));
                        inp.prop("counter", counter++);
                        _range.push(scale(id));
                        inp.on("change click enter", function () {
                            _range[parseFloat($(this).prop("counter"))] = $(this).val();
                            toSet.none = sel_None.val();
                            toSet.overlap = sel_O.val();
                            toSet.range = _range;
                        });
                    }
                }
                //break;
                //default:
            } else {
                // unknown attrType
                //break;
			}

			GRAVIS3D.GUI.ConfirmModal( {
				title : "Change Mapping Parameters",
				$msg : selectionDiv,
				onOk : function() {
					if ( toSet != null ) mapping.setMappingParams( toSet );
					self.update();
				}
			} );

		}

		// button - remove mapping
		var td_rmv = $( "<td></td>" ).appendTo( mappingEl );
		var btn_rmv = $(
				'<button type="button" name="remove" class="btn btn-default btn-xs mappingControl"><span class="glyphicon glyphicon-remove"></span></button>' )
				.appendTo( td_rmv );
		btn_rmv.on( "click enter", function() {
			GRAVIS3D.GUI
			.ConfirmModal( {
				title : "Do you really want to remove this Mapping?",
				$msg : $( '<div class="alert alert-warning" role="alert"><span class="glyphicon glyphicon-warning-sign"></span> All defined parameters for this Mapping will be lost.</div>' ),
				onOk : function() {
					manager.removeMapping( mapping.getId() );
					manager.triggerChange();
					self.update();
				}
			} );
		} );


		return mappingEl;

	}


	// add btn
	function addBtn( context ) {
		var addMapping_btn = $( '<button type="button" class="btn btn-default mappingControl"><span class="glyphicon glyphicon-plus"></span> Add Mapping</button>' );
		addMapping_btn.on( "click enter", function() {
			var g = manager.getGraphModel();
			var attributes = g.getAttributesAsArray( context );
			var m = new GRAVIS3D.Mapping.AttributeMapping( {
				graph : g,
				isFor : context,
				attributeId : attributes[0].getId(),
				visualVariable : Object.keys( GRAVIS3D.Mapping.AttributeMappingDefaults[context][attributes[0]
						.getType()] )[0]
			} );
			manager.addMapping( m );
			self.update();
		} );
		return addMapping_btn;
	}

	// apply btn
	function applyBtn() {
		var applyBtn = $( '<button type="button" class="btn btn-primary"><span class="glyphicon glyphicon-ok"></span> Apply Changes</button>' );
		applyBtn.on( "click enter", function() {
			applyBtn.prop( "disabled", true );
			var btnTmp = applyBtn.html();
			applyBtn.html( "Loading..." );
			setTimeout( function() {
				manager.triggerChange();
				applyBtn.html( btnTmp );
			}, 10 );
		} );
		return applyBtn;
	}

	function noAttributes( context ) {
		return $( '<div class="alert alert-danger" role="alert"><span class="glyphicon glyphicon-ban-circle"></span> Not possible. No '
				+ context + '-attributes found for this graph.</div>' );
	}

	////////////////

	this.update = function() {

		// create interface 
		function build( context ) {

			var noMapping = '<div class="alert alert-info" role="alert"><span class="glyphicon glyphicon-info-sign"></span> No '
					+ context + '-mappings added so far.</div>';

			// get node / edgePanel
			var panel = null;
			if ( context == "edge" ) {
				panel = edgePanel;
			} else {
				panel = nodePanel;
			}
			panel.empty();

			// get attributes
			var attributes = mappingManager.getGraphModel().getAttributesAsArray( context );

			// if found > 1
			if ( attributes.length > 0 ) {

				// get node/edge mappings..
				var mappings = null;
				if ( context == "edge" ) {
					mappings = mappingManager.getEdgeMappingsAsArray();
				} else {
					mappings = mappingManager.getNodeMappingsAsArray();
				}

				if ( mappings.length < 1 ) {
					// no mappings?
					panel.append( noMapping );
					panel.append( addBtn( context ) );
				} else {
					// mappings found!
					panel.append( mappingList( mappings ) );
					panel.append( "<hr />" );
					panel.append( applyBtn() );
				}

			} else {
				// no attributes found
				panel.append( noAttributes( context ) );
			}

		}

		build( "node" );
		build( "edge" );

	};

	this.getDomElement = function() {
		setTimeout( function() {
			guiEl.find(".mappingTab").first().tab( 'show' );
		}, 1 );
		return guiEl;
	};

	this.update();

};


// TODO comments, validation
GRAVIS3D.Mapping.Manager = function( params ) {

	GRAVIS3D.InfoObject.call( this );

	if ( !params && !params.graph && !params.graph instanceof GRAVIS3D.Model.DynamicGraph ) throw new Error(
			"Mapping: MappingManager " + this.getId() + ": no proper graph passed... (params.graph: "
					+ JSON.strigify( params.graph ) + ")" );

	// model
	var graph = params.graph;
	var mappings = {};

	// notification callback...
	var onChange = null;
	this.triggerChange = function() {
		if ( onChange != null ) {
			onChange( mappings );
		}
	};

	// gui
	var gui = null;
	this.getGUI = function() {
		return gui;
	};
	this.refreshGUI = function(){
		$( gui.getDomElement() ).remove();
		gui = new GRAVIS3D.Mapping.MappingManagerGUI( this );
	};

	/////////////////////////

	/**
	 * @method getGaphModel
	 * @return {@link GRAVIS3D.Model.Graph}
	 */
	this.getGraphModel = function() {
		return graph;
	};

	/**
	 * @method getMappingById
	 * @param {@link GRAVIS3D.GraphMapping.Mapping#getId} mappingId
	 * @returns {@link GRAVIS3D.GraphMapping.Mapping}
	 */
	this.getMappingById = function( mappingId ) {
		return mappings[mappingId];
		this.getMappingById( mappingId );
	};
	this.getMappings = function() {
		return mappings;
	};
	this.getMappingsAsArray = function() {
		var array = [];
		for ( id in mappings ) {
			array.push( mappings[id] );
		}
		return array;
	};
	this.getNodeMappingsAsArray = function() {
		var array = [];
		for ( id in mappings ) {
			if ( mappings[id].isFor() == "node" ) array.push( mappings[id] );
		}
		return array;
	};
	this.getEdgeMappingsAsArray = function() {
		var array = [];
		for ( id in mappings ) {
			if ( mappings[id].isFor() == "edge" ) array.push( mappings[id] );
		}
		return array;
	};

	this.addMapping = function( mapping ) {
		mappings[mapping.getId()] = mapping;
	};

	this.removeMapping = function( mappingId ) {
		delete mappings[mappingId];
	};


	//////////////////////////

	if ( params.onChange ) onChange = params.onChange;
	if ( params.mappings ) mappings = params.mappings;

	if ( params.gui == false ) {
	} else gui = new GRAVIS3D.Mapping.MappingManagerGUI( this );

};

/**
 * Mapping-Parameter presets for context -> type -> variable<br>
 * context = node / edge <br>
 * type (of attribute) = string / number / cluster<br>
 * (visual) variable = context depending i.e. color / opacity / ...
 * 
 * @public
 * @constant
 */
GRAVIS3D.Mapping.AttributeMappingDefaults = {
	/**
	 * context = node
	 */
	node : {
		string : {
			color: ["#e41a1c","#4daf4a","#984ea3","#ff7f00","#ffff33","#a65628","#7f7f7f","#f781bf","#999999","#377eb8"],
			form : [ "sphere", "cube", "cylinder", "cone", "cone_down", "tetrahedron" ],
			labelFont : [ "sans-serif", "serif", "monospace", "cursive", "fantasy" ],
			texture : [ "wireframe", "default" ],
			orientation : [ 0, 0.5, 1 ],
			size : [ 1, 3 ],
			brightness : [ 0.4, 0.6 ],
			opacity : [ 0.1, 1 ],
			labelScale : [ 0.1, 1 ]
			//showLabel : [ true, false ]
		},
		number : {
			color : {
				min : "#4B088A",
				max : "#FF0000"
			},
			size : {
				min : 0.5,
				max : 3
			},
			brightness : {
				min : 0.2,
				max : 0.8
			},
			opacity : {
				min : 0.1,
				max : 1
			},
			orientation : {
				min : 0,
				max : 1
			},
			labelScale : {
				min : 0.5,
				max : 3
			}
		},
		cluster : {
			color : {
				range : d3.scale.category10().range(),
				overlap : "#FFFFFF",
				none : "#000000"
			},
			form : {
				range : [ "sphere", "cylinder", "cone", "cone_down", "tetrahedron" ],
				overlap : "cube",
				none : "torus"
			},
			labelFont : {
				range : [ "sans-serif", "serif", "cursive" ],
				overlap : "monospace",
				none : "fantasy"
			},
			texture : {
				range : [ "default" ],
				overlap : "fancy",
				none : "wireframe"
			},
			size : {
				range : [ 1 ],
				overlap : 2,
				none : 0.5
			},
			brightness : {
				range : [ 0.5 ],
				overlap : 1,
				none : 0
			},
			opacity : {
				range : [ 0.8 ],
				overlap : 1,
				none : 0.5
			},
			labelScale : {
				range : [ 1 ],
				overlap : 2,
				none : 0.5
			}
		}
	},
	/**
	 * context = edge
	 */
	edge : {
		string : {
			color : d3.scale.category10().range(),
			texture : [ "solid", "dashed", "dashed_narrow", "dashed_wide" ],
			size : [ 1, 3 ],
			brightness : [ 0.1, 1 ],
			opacity : [ 0.1, 1 ]
		},
		number : {
			color : {
				min : "#0000FF",
				max : "#FF0000"
			},
			size : {
				min : 0.5,
				max : 3
			},
			brightness : {
				min : 0,
				max : 1
			},
			opacity : {
				min : 0.1,
				max : 1
			}
		},
		cluster : {
			color : {
				range : d3.scale.category10().range(),
				overlap : "#FFFFFF",
				none : "#000000"
			},
			texture : {
				range : [ "dashed", "dashed_narrow" ],
				overlap : "solid",
				none : "dashed_wide"
			},
			size : {
				range : [ 1 ],
				overlap : 2,
				none : 0.5
			},
			brightness : {
				range : [ 0.5 ],
				overlap : 1,
				none : 0
			},
			opacity : {
				range : [ 0.8 ],
				overlap : 1,
				none : 0.5
			}
		}
	}
};

/**
 * @summary
 * Create a mapping for a {@link GRAVIS3D.VisualRepresentation.Graph} based on a {@link GRAVIS3D.Model.Attribute}. <br>
 * Every mapping has a <strong>target</strong> to perform on - the "isFor" ("node", "edge", "label").<br>
 * The Mapping on the target is depending on the Attribtue's <strong>type</strong> ("string", "number", "cluster"): <br>
 * - for "label"-target: the attribute values are set as new node labels.<br>
 * - for "node"- and "edge"-target: the type specific mapping is from each node/edge attribute value to its visual representation. <br>
 * &nbsp;therefore every value is scaled on the representations <strong>visual variable</strong>.
 * 
 * @since 1.0
 * 
 * @constructor AttributeMapping
 * @param { <br>
 * 		&emsp;graph: {@link GRAVIS3D.Model.Graph} <i>*required*</i> <br>
 * 		&emsp;isFor: {"node"|"edge"|"label"} <i>*required*</i> <br>
 * 		&emsp;attributeId: {String} <i>*required*</i><br>
 * 		&emsp;visualVariable: {String} <i>*required [exception: isFor="label"]*</i><br>
 * 		&emsp;mappingParams: ( <br>
 * 		&emsp;&emsp;[isFor="string"]: *[] | <br>
 * 		&emsp;&emsp;[isFor="number"]: {min: *, max; *} | <br>
 * 		&emsp;&emsp;[isFor="cluster"]: {range: *[], overlap: *, none: *} <br>		
 * 		&emsp;) <br>
 * 		&emsp;visualRepresentation: {@link GRAVIS3D.VisualRepresentation.Graph}<br>
 * } params
 * 
 * @example
 * <pre>
 * <code>
 * var graph = new GRAVIS3D.Model.Graph(someParams);
 * var visRep = new GRAVIS3D.VisualRepresentation.Graph({
 * 		graph: graph
 * });
 * 
 * //=> example 1: mapping with labels as target
 * var mapping1 = new GRAVIS3D.Mapping.AttributeMapping({
 * 		graph: graph,
 * 		isFor: "label",
 * 		attributeId: "someAttributeId"
 * });
 * visRep.addMapping(mapping1);
 * 
 * //=> example 2: mapping with nodes color as target
 * var mapping2 = new GRAVIS3D.Mapping.AttributeMapping({
 * 		graph: graph,
 * 		isFor: "nodes",
 * 		attributeId: "someAttributeId",
 * 		visualVariable: "color"
 * });
 * visRep.addMapping(mapping2);
 * 
 * //=> example 3: mapping with edges color as target and defined mapping params for a string-attribute
 * var mapping3 = new GRAVIS3D.Mapping.AttributeMapping({
 * 		graph: graph,
 * 		isFor: "edge",
 * 		attributeId: "someStringAttributeId",
 * 		visualVariable: "color",
 * 		mappingParams: ["#FFF", "#FF0044", "#565655"]
 * });
 * visRep.addMapping(mapping3); // this will overwrite mapping 2
 *
 * //=> example 4: mapping with edges size as target and defined mapping params for a number-attribute
 * var mapping4 = new GRAVIS3D.Mapping.AttributeMapping({
 * 		graph: graph,
 * 		isFor: "edge",
 * 		attributeId: "someNumberAttributeId",
 * 		visualVariable: "size",
 * 		mappingParams: {min: 0, max:4}
 * });
 * visRep.addMapping(mapping4);
 * 
 * //=> example 5: mapping with nodes color as target and defined mapping params for a cluster-attribute
 * var mapping5 = new GRAVIS3D.Mapping.AttributeMapping({
 * 		graph: graph,
 * 		isFor: "node",
 * 		attributeId: "someClusterAttributeId",
 * 		visualVariable: "color",
 * 		mappingParams: {range: ["#334455", "#556677", "#9900FF"], overlap: "#FFF", none: "#000"}
 * });
 * visRep.addMapping(mapping5); // this will overwrite mapping 3 (and therefore 2 as well)
 * </code>
 * </pre>
 */
GRAVIS3D.Mapping.AttributeMapping = function( params ) {

	GRAVIS3D.InfoObject.call( this );

	// GRAPH
	var _graph = null;
	/**
	 * returns the used graph model for filtering
	 * 
	 * @method getGraphModel
	 * @return {@link GRAVIS3D.Model.Graph |@link  GRAVIS3D.Model.DynamicGraph}
	 */
	this.getGraphModel = function() {
		return _graph;
	};
	/**
	 * Set the graph to use for filtering
	 * 
	 * @method setGraphModel
	 * @param {@link GRAVIS3D.Model.Graph |@link GRAVIS3D.Model.DynamicGraph} graph
	 */
	this.setGraphModel = function( graph ) {
		if ( graph instanceof GRAVIS3D.Model.Graph || graph instanceof GRAVIS3D.Model.DynamicGraph ) {
			_graph = graph;
		} else throw new Error( "Mapping: Attribute Mapping " + this.getId()
				+ " #setVisualRepresentation : param must be a Graph ... (param: " + JSON.stringify( graph ) + ")" );
	};

	// CONTEXT
	var isFor = "node"; // "node" | "edge" 
	/**
	 * returns the context 
	 * 
	 * @method isFor
	 * @return {"node" | "edge"} isFor
	 */
	this.isFor = function() {
		return isFor;
	};
	/**
	 * sets the context
	 * 
	 * @method setFor
	 * @param {"node"|"edge"} context
	 */
	this.setFor = function( context ) {
		if ( context.toLowerCase() == "node" || context.toLowerCase() == "edge" ) isFor = context.toLowerCase();
		else throw new Error( "Mapping: AttributeMapping " + this.getId()
				+ " # setFor: param must be  'node', 'label' or 'edge'... (param: " + JSON.stringify( context ) + ")" );
	};

	// ATTRIBUTE ID
	var attributeId = "";
	/**
	 * returns the id of the attribute, which will be mapped
	 * 
	 * @method getAttributeId
	 * @return {String} - {@link GRAVIS3D.Model.Attribute#getId}
	 */
	this.getAttributeId = function() {
		return attributeId;
	};
	/**
	 * sets the attribute (by id), which will be mapped
	 * 
	 * @method setAttributeId
	 * @param {String} id - {@link GRAVIS3D.Model.Attribute#getId}
	 */
	this.setAttributeId = function( id ) {
		if ( typeof id == "string" ) attributeId = id;
		else throw new Error( "Mapping: Attribute Mapping " + this.getId()
				+ " #setAttributeId: param must be a string... (param: " + JSON.stringify( id ) + ")" );
	};

	// VISUAL VARIABLE
	var visual_var = "";
	/**
	 * returns the used visual variable<br>
	 * have a look at {@link GRAVIS3D.Mapping.AttributeMappingDefaults} for possible vars
	 * 
	 * @method getVisualVariable
	 * @return {String} 
	 */
	this.getVisualVariable = function() {
		return visual_var;
	};
	/**
	 * sets the visual variable to use for this mapping<br>
	 * have a look at {@link GRAVIS3D.Mapping.AttributeMappingDefaults} for possible vars
	 * 
	 * @method setVisualVariable
	 * @param {String} variable
	 */
	this.setVisualVariable = function( variable ) {
		var attr_type = this.getGraphModel().getAttributeById( this.getAttributeId(), this.isFor() ).getType();
		var possibleVars = Object.keys( GRAVIS3D.Mapping.AttributeMappingDefaults[this.isFor()][attr_type] );
		if ( possibleVars.indexOf( variable ) != -1 ) {
			visual_var = variable;
			// set mapping params to default
			this.setMappingParams( GRAVIS3D.Mapping.AttributeMappingDefaults[this.isFor()][attr_type][variable] );
		} else throw new Error( "Mapping: Attribute Mapping " + this.getId()
				+ " #setVisualVariable:  variable not found... (param: " + JSON.stringify( variable ) + ") Possible: "
				+ possibleVars.toString() );
	};

	// MAPPING PARAMS
	var mappingParams = null;
	/**
	 * returns the mapping params
	 * 
	 * @method getMappingParams
	 * @return {*} - have a look at @this#setMappingParams description for syntax
	 */
	this.getMappingParams = function() {
		return mappingParams;
	};
	/**
	 * Set the mapping params and overwrite defaults
	 * 
	 * @method setMappingParams
	 * @param {*} mapping_params - 
	 * depending on attributes type... <br>
	 * string: pass an Array i.e. ["val1", "val2"] <br>
	 * number: pass an Object i.e. { min: 1, max: 2 } <br>
	 * cluster: pass an Object: i.e. { range: ["val1", "val2"], overlap: "val3", none: "val4" }
	 */
	this.setMappingParams = function( mapping_params ) {
		var type = this.getGraphModel().getAttributeById( this.getAttributeId(), this.isFor() ).getType();
		if ( type == "string" ) {
			if ( Object.prototype.toString.call( mapping_params ) === '[object Array]' ) mappingParams = mapping_params;
			else throw new Error( "Mapping: Attribute Mapping " + this.getId()
					+ " #setMappingParams: param must be an ARRAY, if you want to map a STRING-Attribute... (param: "
					+ JSON.stringify( mapping_params ) + ")" );
		} else if ( type == "number" ) {
			if ( mapping_params.min != undefined && mapping_params.max != undefined ) mappingParams = mapping_params;
			else throw new Error(
					"Mapping: Attribute Mapping "
							+ this.getId()
							+ " #setMappingParams: param must be an OBJECT with MIN and MAX i.e. {min: 0, max: 1}, if you want to map a NUMBER-Attribute... (param: "
							+ JSON.stringify( mapping_params ) + ")" );
		} else if ( type == "cluster" ) {
			if ( mapping_params.range && mapping_params.overlap && mapping_params.none ) mappingParams = mapping_params;
			else throw new Error(
					"Mapping: Attribute Mapping "
							+ this.getId()
							+ " #setMappingParams: param must be an OBJECT with RANGE (Array), OVERLAP and NONE i.e. "
							+ "{ range: ['#FFF', '#FF0077'], overlap: '#FF3344', none: '#AAFFCC' }, if you want to map a CLUSTER-Attribute... (param: "
							+ JSON.stringify( mapping_params ) + ")" );
		}
	};

	// USE
	/**
	 * applies the mapping to a visual representation
	 * 
	 * @method applyToVisualRepresentation
	 * @param {@link GRAVIS3D.VisualRepresentation.Graph} visRep
	 */
	this.applyToVisualRepresentation = function( visRep ) {

		// DO IT
//		var graph = this.getGraphModel();
		graph = visRep.getRepresentedGraphModel();

		//		var defaults = GRAVIS3D.Mapping.AttributeMappingDefaults;
		var type = graph.getAttributeById( this.getAttributeId(), this.isFor() ).getType();

		var attribute = this.getGraphModel().getAttributeById( this.getAttributeId(), this.isFor() );
		var possibleValues = attribute.getPossibleValues();

		var nodeReps = visRep.getNodeRepresentations();
		if ( this.isFor() == "edge" ) nodeReps = visRep.getEdgeRepresentations();

		// attribute type
		if ( type == "string" ) {
			var scale = d3.scale.ordinal();
			scale.domain( Object.keys( possibleValues ) );
			scale.range( this.getMappingParams() );
			for ( id in nodeReps ) {
				var nodeRep = nodeReps[id];
				var node = nodeRep.getRepresentedModel();
				var val = scale( node.getAttributeValue( this.getAttributeId() ) );
				nodeRep.set( this.getVisualVariable(), val );
			}
		}

		if ( type == "number" ) {
			var scale = d3.scale.linear();
			scale.domain( [ attribute.getRange().getMin(), attribute.getRange().getMax() ] );
			scale.range( [ this.getMappingParams().min, this.getMappingParams().max ] );
			for ( id in nodeReps ) {
				var nodeRep = nodeReps[id];
				var node = nodeRep.getRepresentedModel();
				var val = scale( node.getAttributeValue( this.getAttributeId() ) );
				nodeRep.set( this.getVisualVariable(), val );
			}
		}

		if ( type == "cluster" ) {
			var scale = d3.scale.ordinal();
			scale.domain( attribute.getPossibleValues() );
			var overlap = null;
			var none = null;
			scale.range( this.getMappingParams().range );
			overlap = this.getMappingParams().overlap;
			none = this.getMappingParams().none;
			for ( id in nodeReps ) {
				var nodeRep = nodeReps[id];
				var node = nodeRep.getRepresentedModel();
				var clusters = node.getAttributeValue( this.getAttributeId() );
				var val = null;
				if ( clusters.length == 1 ) {
					val = scale( clusters[0] );
				} else if ( clusters.length > 1 ) {
					val = overlap;
				} else {
					val = none;
				}
				nodeRep.set( this.getVisualVariable(), val );
			}
		}


	};
	/**
	 * "clean" a visual variable (the one used in this mapping) in a representation
	 * 
	 * @method removeFromVisualRepresentation 
	 * @param visRep {@link GRAVIS3D.VisualRepresentation.Graph}
	 */
	this.removeFromVisualRepresentation = function( visRep ) {
		var context = this.isFor();
		visRep.resetVisualVariable( context, this.getVisualVariable() );

	};

	/**
	 * @return copied @instance of @this
	 */
	this.copy = function() {
		return new GRAVIS3D.Mapping.AttributeMapping( {
			graph : this.getGraphModel(),
			isFor : this.isFor(),
			attributeId : this.getAttributeId(),
			visualVariable : this.getVisualVariable(),
			mappingParams : this.getMappingParams()
		} );
	};

	if ( !params || !params.graph || !params.isFor || !params.attributeId || !params.visualVariable ) throw new Error(
			"Mapping: AttributeMapping " + this.getId()
					+ ": REQUIRED PARAMS: graph, isFor, attributeId, visualVariable! " );

	this.setGraphModel( params.graph );
	this.setFor( params.isFor );
	this.setAttributeId( params.attributeId );
	this.setVisualVariable( params.visualVariable );

	if ( params.mappingParams ) this.setMappingParams( params.mappingParams );

	if ( params.id ) this.setId( params.id );
	if ( params.name ) this.setName( params.name );
	if ( params.description ) this.setDescription( params.description );
	if ( params.data ) this.setData( params.data );

	if ( params.visualRepresentation ) params.visualRepresentation.addMapping( this );

};
