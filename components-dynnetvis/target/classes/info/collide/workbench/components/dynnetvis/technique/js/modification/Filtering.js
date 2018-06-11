/**
 * @author henrik detjen
 */

/**
 * @namespace holds all classes used for filtering 
 * @nameSpace
 */
GRAVIS3D.Filtering = {};

/**
 * @summary 
 * A user interface element for create / delete / interact with filters of filter manager
 * 
 * @since 1.0
 * @method GUI
 * @param {@link GRAVIS3D.Filtering.Manager} filterManager
 */
GRAVIS3D.Filtering.GUI = function( filterManager ) {

	var self = this;
	var manager = filterManager;
	var guiEl = $( "<div class='filterManager'></div>" );

	//head
	//tabs
	var toggleFilterCat_Tabs = $( '<ul id="filterTabs" class="nav filterTabs nav-tabs" role="tablist">' ).appendTo(
			guiEl );

	function createTab( title, id, target ) {
		var tab1 = $( '<a href="#' + target + '" id="' + id + '" class="filterTab" role="tab" data-toggle="tab">'
				+ title + '</a>' );
		$( '<li></li>' ).append( tab1 ).appendTo( toggleFilterCat_Tabs );
		var clickTimer1 = null;
		tab1.on( "mouseover", function() {
			clickTimer1 = setTimeout( function() {
				tab1.trigger( "click" );
			}, GRAVIS3D.GUI.Defaults.tabs.clickTriggerDelay );
		} );
		tab1.on( "mouseout", function() {
			clearTimeout( clickTimer1 );
		} );
		tab1.trigger( "click" )
	}
	// the target id must match the content panel's id..
	var ids = [];
	for ( var i = 0; i <= 3; i++ ) {
		ids.push( GRAVIS3D.ID.get() );
	}
	createTab( "View - Nodes", "viewFiltersTabNodes", ids[0] );
	createTab( "View - Edges", "viewFiltersTabEdges", ids[1] );
	createTab( "Data - Nodes", "dataFiltersTabNodes", ids[2] );
	createTab( "Data - Edges", "dataFiltersTabEdges", ids[3] );


	//content
	var toggleFilterCat_TabPanes = $( '<div class="tab-content">' ).appendTo( guiEl );//root

	//panels
	function createPanel( id, title ) {
		var tabPane = $( '<div class="tab-pane fade" id="' + id + '"></div>' ).appendTo( toggleFilterCat_TabPanes );
		var divWrapper = $( '<div class=""></div>' ).appendTo( tabPane );
		var panel = $( '<div class="panel panel-default"></div>' ).appendTo( divWrapper );
		//			$( '<div class="panel-heading"><h4>'+title+'</h4></div>' ).appendTo( panel );
		var contentDiv = $( '<div class="panel-body"></div>' ).appendTo( panel );
		return contentDiv;
	}
	// panel ids must match tab's taget ids
	var viewFilter_NodesDiv = createPanel( ids[0] );
	var viewFilter_EdgesDiv = createPanel( ids[1] );
	var dataFilter_NodesDiv = createPanel( ids[2] );
	var dataFilter_EdgesDiv = createPanel( ids[3] );
	function getPanel( type, context ) {
		var panels = {
			view : {
				node : viewFilter_NodesDiv,
				edge : viewFilter_EdgesDiv
			},
			data : {
				node : dataFilter_NodesDiv,
				edge : dataFilter_EdgesDiv
			}
		};
		return panels[type][context];
	}

	// helper - form group
	function formGroupWrapper( label, wrappedInput, helpText, _size ) {
		var uuid = GRAVIS3D.ID.get();
		$( wrappedInput ).attr( "id", uuid );
		if ( helpText == null ) helpText = "";
		var size = 6;
		if ( _size ) size = _size;
		var formGroup = $( '<div class=" col-sm-' + size + '"></div>' );
		var labelEl = $( '<label class="control-label " for="' + uuid + '">' + label + '</label>' )
				.appendTo( formGroup );
		var dgDiv = $( '<div class=""></div>' ).appendTo( formGroup );
		dgDiv.append( wrappedInput );
		GRAVIS3D.GUI.addTooltip( labelEl, helpText );
		//		if ( helpText != null ) $( '<span class="help-block">' + helpText + '</span>' ).appendTo( dgDiv );
		return formGroup;
	}

	// helper - form wrapper - to allow bootstrapvalidator on single fields
	function formWrapper( wrappedInput ) {
		var form = $( '<div class="" />' );
		var grp = $( ' <div class="form-group col-sm-12" />' ).appendTo( form );
		grp.append( wrappedInput );
		return form;
	}

	// filter element
	function filter( type, context ) {

		// get filter to set values later on
		var filter = filterManager.getFilter( type, context );

		// form
		var filterEl = $( '<form onSubmit="return false;" class=""></form>' );

		var top = $( '<div class="row">' ).appendTo( filterEl );
		// input - command string
		var cmdEl = $( '<input name="cmd" type="text" class="form-control" placeholder="i.e. 2&(1|3)" value="'
				+ filter.getCommandString() + '" >' );
		top.append( formGroupWrapper( "Command", cmdEl,
				"Connect the Rules by connecting their ids with operators &, | and brackets.", 7 ) );
		// --> onChange is handled in validator

		// select - visual variable (only for view filters)
		var visvarEl;
		if ( type == "view" ) {
			visvarEl = $( '<select name="visualVariable" type="select" class="form-control">' );
			var visVars = Object.keys( GRAVIS3D.Filtering.Defaults.possibleVisualVariables[context] );
			for ( var i = 0; i < visVars.length; i++ ) {
				$( '<option value="' + visVars[i] + '">' + firstLetterUp( visVars[i] ) + '</option>' ).appendTo(
						visvarEl );
			}
			visvarEl.val( filter.getVisualVariable() );
			top.append( formGroupWrapper( "Visual Variable", visvarEl,
					"Select on which visual channel/parameter of the view the filter should be applied.", 5 ) );
			visvarEl.change( function() {
				filter.setVisualVariable( visvarEl.val() );
				self.update();
			} );
		}

		filterEl.append( "<div class='clearfix' /><br />" )


		// list - rules
		var rulesPanel = $( '<div class="panel panel-default" />' );
		rulesPanel.append( ruleList( filter.getRules() ) );
		filterEl.append( rulesPanel );

		function ruleList( rules ) {

			var div = $( '<div class="panel-body" role=""></div>' );

			// get attribtues..
			var attrs;
			if ( context == "node" ) {
				attrs = filter.getGraphModel().getNodeAttributes();
			} else {
				attrs = filter.getGraphModel().getEdgeAttributes();
			}

			var morethanonerule = false;
			for ( id in rules ) {
				morethanonerule = true;
			}

			// >0 rules: create list items and add table to gui
			if ( morethanonerule == true ) {

				// table
				var tableDiv = $( '<div class="table-responsive ruleTable"></div>' );
				var ruleTable = $( '<table class="table table-hover table-condensed">' ).appendTo( tableDiv );
				//				ruleTable.append( "<caption><h4>Rules</h4></caption>" )
				// table - header
				var ruleTableHeadEl = $( '<thead></thead>' ).appendTo( ruleTable );
				var ruleTableHead = $( '<tr></tr>' ).appendTo( ruleTableHeadEl );
				var th_id = $( '<th width="55">Id</th>' ).appendTo( ruleTableHead );
				//						GRAVIS3D.GUI.addTooltip( th_id, "Letters, numbers and underscore." );
				var th_attr = $( '<th>Attribute</th>' ).appendTo( ruleTableHead );
				//			GRAVIS3D.GUI.addTooltip( th_attr, "These attribute's values of a node/edge will be affected." );
				var th_op = $( '<th width="70"></th>' ).appendTo( ruleTableHead );
				//			GRAVIS3D.GUI.addTooltip( th_op, "This op	erator will be used to check an node's/edge's value against the rule's value." );
				var th_val = $( '<th width="">Value</th>' ).appendTo( ruleTableHead );
				//			GRAVIS3D.GUI.addTooltip( th_val, "Number-attributes will be normalized to 0 - 1. Multiple values for operators =, != can be defined as an JSON-array: [value1,value2,value3]" );
				var th_rmv = $( '<th></th>' ).appendTo( ruleTableHead );
				//			GRAVIS3D.GUI.addTooltip( th_rmv, "" );

				// rule element
				function rule( rule ) {
					var ruleEl = $( '<tr></tr>' );
					
					var attr = attrs[rule.getAttributeId()];
					var attrType = attr.getType();
					//					console.log( rule.getId() + "-" + rule.getOperator() + "-" + rule.getValue() )
					
					// input - id
					var td_id = $( "<td></td>" ).appendTo( ruleEl );
					var input_id = $( '<input name="rule_id" type="text" class="form-control" placeholder="id" value="'
							+ rule.getId() + '" >' );
					var form_id = formWrapper( input_id ).appendTo( td_id );
					td_id.on( "change enter", function() {
						// validate input
						var inputOk = filterEl.data( 'bootstrapValidator' ).isValidContainer( form_id );
						if ( input_id.val() != rule.getId() && inputOk ) {
							filter.removeRule( rule.getId() );
							rule.setId( input_id.val() );
							filter.addRule( rule );
							self.update();
						}
					} );
					
					// select - attribute id
					var td_attr = $( "<td></td>" ).appendTo( ruleEl );
					var select_attributes = $( '<select name="rule_attributes" type="select" class="form-control">' )
					.appendTo( td_attr );
					for ( id in attrs ) {
						$( '<option value="' + id + '">' + attrs[id].getName() + '</option>' ).appendTo(
								select_attributes );
					}
					select_attributes.val( rule.getAttributeId() );
					select_attributes
					.change( function() {
						rule.setAttributeId( select_attributes.val() );
						rule
						.setOperator( GRAVIS3D.Filtering.Defaults.possibleRuleOperators.basic[attrs[select_attributes
						                                                                            .val()].getType()] );
						if ( attrs[select_attributes.val()].getType() == "number" ) {
							rule.setValue( 1 );
						} else {
							rule.setValue( "" );
						}
						self.update();
					} );
					
					// select - operator / condition
					var td_op = $( "<td></td>" ).appendTo( ruleEl );
					var select_op = $( '<select name="rule_operators" type="select" class="form-control">' ).appendTo(
							td_op );
					var operators = GRAVIS3D.Filtering.Defaults.possibleRuleOperators[attrType];
					for ( var i = 0; i < operators.length; i++ ) {
						$( '<option value="' + operators[i] + '">' + operators[i] + '</option>' ).appendTo( select_op );
					}
					select_op.val( rule.getOperator() );
					select_op.change( function() {
						rule.setOperator( select_op.val() );
						self.update();
					} );
					
					// input - value
					var td_val = $( "<td></td>" ).appendTo( ruleEl );
					var input_val = null;
					var sug_ID = GRAVIS3D.ID.get();
					var suggestions = $( '<datalist id="' + sug_ID + '"></datalist>' ).appendTo( ruleEl );
					var vals = attr.getPossibleValues();
					for ( id in vals ) {
						suggestions.append( '<option value="' + id + '">' + id + '</option>' );
					}
					if ( attrType == "number" ) {
						input_val = $( '<input name="rule_val" type="number" list="suggestions_' + attr.getId()
								+ '" class="excluded form-control" min="0" step="0.1" placeholder="" value="'
								+ rule.getValue() + '" >' );
					} else {
						input_val = $( '<input name="rule_val" type="text" class="excluded form-control" placeholder="val1, val2,..." value="'
								+ rule.getValue() + '" list="' + sug_ID + '">' );
					}
					var form_val = formWrapper( input_val ).appendTo( td_val );
					input_val.on( "change enter", function() {
						//						var inputOk = filterEl.data( 'bootstrapValidator' ).isValidContainer( form_val );
						if ( input_val.val() != rule.getValue() ) {
							rule.setValue( input_val.val() );
							self.update();
						}
					} );
					
					// button - remove rule
					var td_rmv = $( "<td></td>" ).appendTo( ruleEl );
					var btn_rmv = $(
					'<button type="button" name="remove" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-remove"></span></button>' )
					.appendTo( td_rmv );
					btn_rmv
					.on(
							"click enter",
							function() {
								GRAVIS3D.GUI
								.ConfirmModal( {
									title : "Do you really want to remove this rule?",
									$msg : $( '<div class="alert alert-warning" role="alert"><span class="glyphicon glyphicon-warning-sign"></span> All defined settings for this rule will be lost. The Command will be reset, if it contains this rule.</div>' ),
									onOk : function() {
										filter.removeRule( rule.getId() );
										self.update();
									}
								} );
							} );
					
					return ruleEl;
				}
				
				// table - body
				var ruleTableBody = $( '<tbody></tbody>' ).appendTo( ruleTable );
				for ( id in rules ) {
					// listitems
					var r = rules[id];
					ruleTableBody.append( rule( r ) );
				}


				div.append( tableDiv );

			} else {
				// 0 rules: show info
				div
						.append( '<div class="alert alert-info" role="alert"><span class="glyphicon glyphicon-info-sign"></span> No rules added so far.</div>' );
			}

			// btn - new rule
			var btn_div = $( '<div class="pull-left"></div>' ).appendTo( div );
			var btn_addRule = $(
					'<button type="button" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span> New Rule </button>' )
					.appendTo( btn_div );
			btn_addRule.on( "click enter", function() {
				var existingIds = Object.keys( rules );
				var number = 1;
				var id = "1";
				while ( existingIds.indexOf( id ) > -1 ) {
					id = "" + number;
					number++;
				}
				var attr = attrs[Object.keys( attrs )[0]];
				var val = "";
				if ( attr.getType() == "number" ) val = 1;
				var r = new GRAVIS3D.Filtering.Rule( {
					id : id,
					attributeId : attr.getId(),
					operator : GRAVIS3D.Filtering.Defaults.possibleRuleOperators.basic[attr.getType()],
					value : val
				} );
				filter.addRule( r );
				if ( filter.getCommandString() == "" ) {
					filter.setCommandString( id );
				} else {
					filter.setCommandString( filter.getCommandString() + "&" + id );
				}
				self.update();

			} );

			div.append( "<div class='clearfix' />" );

			return div;
		}

		filterEl.append( "<hr />" );

		// buttons..
		var btnGrp = $( '<div />' ).appendTo( filterEl );
		var leftBtnDiv = $( '<div class="pull-left"></div>' ).appendTo( btnGrp );
		var rightBtnDiv = $( '<div class="pull-right"></div>' ).appendTo( btnGrp );
		btnGrp.append( '<div class="clearfix"></div>' );

		// apply changes
		var applyBtn = $(
				'<button type="button" class="btn btn-primary"><span class="glyphicon glyphicon-ok"></span> Apply Changes</button>' )
				.appendTo( leftBtnDiv );
		applyBtn.on( "click enter", function() {

			filterEl.data( 'bootstrapValidator' ).validate();

			// only for valid input: send changes to app controler
			if ( filterEl.data( 'bootstrapValidator' ).isValid() ) {
				applyBtnOff();


				var btnTmp = applyBtn.html();
				applyBtn.html( "Loading..." );
				setTimeout( function() {
					filterEl.data( 'bootstrapValidator' ).resetForm();

					if ( type == "view" ) {
						manager.triggerViewFiltersChange();
					}
					if ( type == "data" ) {
						manager.triggerDataFiltersChange();
					}
					applyBtn.html( btnTmp );
				}, 10 );

			}

		} );
		function applyBtnOn() {
			applyBtn.attr( "disabled", false );
		}
		function applyBtnOff() {
			applyBtn.attr( "disabled", true );
		}

		// remove
		var rmvBtn = $(
				'<button type="button" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> Remove Filter</button>' )
				.appendTo( rightBtnDiv );
		rmvBtn
				.on(
						"click enter",
						function() {
							GRAVIS3D.GUI
									.ConfirmModal( {
										title : "Do you really want to remove this Filter?",
										$msg : $( '<div class="alert alert-warning" role="alert"><span class="glyphicon glyphicon-warning-sign"></span> All defined settings and rules will be lost.</div>' ),
										onOk : function() {
											manager.removeFilter( type, context );
											if ( type == "view" ) {
												manager.triggerViewFiltersChange();
											}
											if ( type == "data" ) {
												manager.triggerDataFiltersChange();
											}
											self.update();
										}
									} );
						} );

		//validate data in form with bootstrap validator
		filterEl.bootstrapValidator( {
			excluded : [ ".excluded" ],
			message : 'This value is not valid',
			fields : {
				//						rule_val : {
				//							onError : function() {
				//								applyBtnOff();
				//							},
				//							onSuccess : function() {
				//								applyBtnOn();
				//							},
				//							validators : {
				//								callback : {
				//									callback : function( value, validator, $field ) {
				//										var type = $field.prop( "type" );
				//										if ( type == "number" ) {
				//											if ( value < 0  ) { return {
				//												valid : false,
				//												message : "value must be >= 0."
				//											}; }
				//										}
				//										return {
				//											valid : true
				//										};
				//									}
				//								}
				//							}
				//						},
				rule_id : {
					onError : function() {
						applyBtnOff();
					},
					onSuccess : function() {
						applyBtnOn();
					},
					validators : {
						notEmpty : {
							message : 'The id is required and cannot be empty.'
						},
						regexp : {
							regexp : /^[a-zA-Z0-9_]+$/,
							message : 'The id can only consist of alphabetical, number and underscore.'
						}
					}
				},
				cmd : {
					onError : function() {
						applyBtnOff();
					},
					onSuccess : function() {
						applyBtnOn();
					},
					validators : {
						callback : {
							callback : function( value, validator, $field ) {
								var error = false;
								try {
									filter.setCommandString( value );
								} catch ( err ) {
									error = err.message;
								}
								if ( error == false || value == "" ) { return {
									valid : true
								}; }
								return {
									valid : false,
									message : $( '<div class="alert alert-danger" role="alert"><strong>Ups! </strong>'
											+ error + '</div>' )
								};
							}
						}
					}
				}
			}
		} );

		function validate() {
			filterEl.data( 'bootstrapValidator' ).validate();
			if ( filterEl.data( 'bootstrapValidator' ).isValid() ) applyBtn.attr( "disabled", false );
			else applyBtn.attr( "disabled", true );
		}

		return filterEl;
	}

	// no filter info + add filter
	function noFilter( type, context ) {
		var noFilterDiv = $( '<div />' );
		noFilterDiv
				.append( '<div class="alert alert-info" role="alert"><span class="glyphicon glyphicon-info-sign"></span> No '
						+ context + '-filter for ' + type + ' added so far.</div>' );
		var btnNew = $( '<button type="button" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span> add Filter</button>' );
		btnNew.on( "click enter", function() {
			var newFilter = new GRAVIS3D.Filtering.Filter( {
				isFor : context,
				graph : filterManager.getGraphModel()
			} );
			manager.setFilter( type, context, newFilter );
			self.update();
		} );
		noFilterDiv.append( btnNew );
		return noFilterDiv;
	}

	// no attributes warning
	function noAttributes( context ) {
		return $( '<div class="alert alert-danger" role="alert"><span class="glyphicon glyphicon-ban-circle"></span> Not possible. No '
				+ context + '-attributes found for this graph.</div>' );
	}



	///////////////////

	this.update = function() {

		//create interface
		function build( type, context ) {

			var panel = getPanel( type, context );
			panel.empty();

			// get attributes
			var attributes = manager.getGraphModel().getAttributesAsArray( context );

			// if found > 1
			if ( attributes.length > 0 ) {

				// get filter..
				var _filter = manager.getFilter( type, context );
				if ( _filter == null ) {
					panel.append( noFilter( type, context ) );
				} else {
					panel.append( filter( type, context ) );
				}

			} else {
				// no attributes found
				panel.append( noAttributes( context ) );
			}

		}

		build( "view", "node" );
		build( "view", "edge" );
		build( "data", "node" );
		build( "data", "edge" );

	};

	this.getDomElement = function() {
		setTimeout( function() {
			$( guiEl ).find( '.filterTab' ).first().tab( 'show' );
		}, 1 );
		return guiEl;
	};

	this.update();

};

// XXX rewrite docs
/**
 * @summary
 * a list to store/handle filters
 * 
 * @since 1.0
 * @method Manager
 * 
 * @param {<br>
 * &emsp;	graph: {} *required* <br>
 * &emsp;	gui: {boolean}, (prevent building HTML-interface) <br>
 * &emsp;	onViewFiltersChange: {Function},	<br>
 * &emsp;	onDataFiltersChange: {Function},	<br>
 * &emsp;	dataFilter_nodes: {@link GRAVIS3D.Filtering.Filter}, 	<br>
 * &emsp;	dataFilter_edges: {@link GRAVIS3D.Filtering.Filter}, 	<br>
 * &emsp;	viewFilter_nodes: {@link GRAVIS3D.Filtering.Filter}, 	<br>
 * &emsp;	viewFilter_edges: {@link GRAVIS3D.Filtering.Filter}, 	<br>
 * } params
 * 
 * @example
 * <pre>
 * <code>
 * 
 * // 1. you need to create or read in a filter
 * var filter = new GRAVIS3D.Filtering.Filter({
 * 	isFor: "node",
 * 	graph: someGraph,
 * 	rules: [someRule, anotherRule]
 * 	command: someRule.getId() + "&" + anotherRule.getId()
 * });
 * 
 * // 2. create this to manage a someGraphs' filters ...and get an awesome userinterface for those filters.
 * var filterManager = new GRAVIS3D.Filtering.Manager({
 * 	graph: someGraph
 * 	viewFilter_nodes: filter,
 * 	onViewFiltersChange: function(filters){
 * 		console.log(filters.viewFilter_nodes);
 * 		// your logic... i.e. re-render  
 * 	}
 * });
 * 
 * // 3. append the user-interface to a dom-element
 * var gui = filterManager.getGUI().getDomElement();
 * $('#destination').append( gui );
 * 
 * <code>
 * </pre>
 */
GRAVIS3D.Filtering.Manager = function( params ) {

	GRAVIS3D.InfoObject.call( this );

	if ( !params && !params.graph && !params.graph instanceof GRAVIS3D.Model.DynamicGraph ) throw new Error(
			"Filtering: FilterManager " + this.getId() + ": no proper graph passed... (params.graph: "
					+ JSON.strigify( params.graph ) + ")" );

	// model
	var graph = params.graph;
	var filters = {
		data : {
			node : null,
			edge : null
		},
		view : {
			node : null,
			edge : null
		}
	};

	// notification Callbacks
	var onChange = null;
	var onDataFiltersChange = null;
	var onViewFiltersChange = null;
	this.triggerChange = function() {
		if ( onChange != null ) {
			onChange( filters );
		}
	};
	this.triggerViewFiltersChange = function() {
		if ( onViewFiltersChange != null ) onViewFiltersChange( filters.view );
	};
	this.triggerDataFiltersChange = function() {
		if ( onDataFiltersChange != null ) onDataFiltersChange( filters.data );
	};

	// gui
	var gui = null;
	this.getGUI = function() {
		return gui;
	};
	this.refreshGUI = function() {
		$( gui.getDomElement() ).remove();
		gui = new GRAVIS3D.Filtering.GUI( this );
	};

	///////////////////////

	/**
	 * @method getGaphModel;
	 * @return {@link GRAVIS3D.Model.Graph}
	 */
	this.getGraphModel = function() {
		return graph;
	};
	// TODO: validation
	/**
	 * returns a filter for view / data in node / edge context
	 * @method getFilter
	 * @param {"view"|"data"} type
	 * @param {"node"|"edge"} context
	 * @return {@link GRAVIS3D.Filtering.Filter}
	 */
	this.getFilter = function( type, context ) {
		return filters[type][context];
	};
	/**
	 * sets a filter for view / data in node / edge context
	 * @method getFilter
	 * @param {"view"|"data"} type
	 * @param {"node"|"edge"} context
	 * @param {@link GRAVIS3D.Filtering.Filter} filter
	 */
	this.setFilter = function( type, context, filter ) {
		filters[type][context] = filter;
	};

	this.removeFilter = function( type, context ) {
		filters[type][context] = null;
	};

	////////////////////

	if ( params && params.filters ) filters = params.filters;

	if ( params.onChange ) onChange = params.onChange;
	if ( params.onDataFiltersChange ) onDataFiltersChange = params.onDataFiltersChange;
	if ( params.onViewFiltersChange ) onViewFiltersChange = params.onViewFiltersChange;

	if ( params.gui == false ) {
	} else gui = new GRAVIS3D.Filtering.GUI( this );

};


/**
 * Holds all default settings for filtering
 * 
 * @public
 * @constant
 */
GRAVIS3D.Filtering.Defaults = {
	/**
	 * possible operators to connect rules
	 */
	possibleExpressionOperators : [ "&", "|" ],
	/**
	 * possible operators for rules inner conditions
	 */
	possibleRuleOperators : {
		all : [ ">", "=>", "<", "<=", "=", "!=" ],
		basic : {
			string : "=",
			number : "<=",
			cluster : "="
		},
		number : [ ">", "=>", "<", "<=", "=", "!=" ],
		string : [ "=", "!=" ],
		cluster : [ "=", "!=" ]
	},
	/**
	 * this will be used for a visual representation filtering
	 */
	basicVisualVariable : {
		node : "opacity",
		edge : "opacity"
	},
	possibleVisualVariables : {
		node : {
			opacity : {
				filtered : 0.1,
				notFiltered : 1
			},
			brightness : {
				filtered : 0.1,
				notFiltered : 1
			},
			size : {
				filtered : 0.5,
				notFiltered : 2
			},
		//			showLabel : {
		//				filtered : false,
		//				notFiltered : true
		//			}
		},
		edge : {
			opacity : {
				filtered : 0.1,
				notFiltered : 1
			},
			brightness : {
				filtered : 0.1,
				notFiltered : 1
			},
			size : {
				filtered : 0.5,
				notFiltered : 2
			},
			texture : {
				filtered : "dashed",
				notFiltered : "solid"
			}
		}
	}
};

/**
 * @summary
 * A Filter is used to modify the returned result of  <br>
 * {@link GRAVIS3D.Model.Graph#getNodes}/{@link GRAVIS3D.Model.Graph#getEdges} <br>
 * (Data-Filter / Model-Filter / Pre-Filter) <br>
 *  <br>
 *  or <br>
 *  <br>
 * to modify the apperance of a {@link GRAVIS3D.VisualRepresentation.Graph} <br>
 * (View-Filter / Representation-Filter / Post-Filter)
 *  
 * @since 1.0
 * 
 * @constructor Filter
 * @param { <br>
 * 		&emsp;	isFor: {"node"|"edge"}, <i>*required*</i><br>
 * 		&emsp;	graph: {@link GRAVIS3D.Model.Graph}, <i>*required*</i><br>
 * 		&emsp;	command: {String},<br>
 * 		&emsp;	rules: {@link GRAVIS3D.Filtering.Rule[]}<br>
 * 		&emsp;	visualRepresentation: {@link GRAVIS3D.VisualRepresentation.Graph}<br>
 * 		&emsp;	visualVariable: {String}<br>		
 * 		&emsp;	filterGraph: {Boolean}<br>
 * } params
 * 
 * @example
 * TODO
 */
GRAVIS3D.Filtering.Filter = function( params ) {

	GRAVIS3D.InfoObject.call( this );
	var self = this;

	// GRAPH MODEL
	var _graph = null;
	/**
	 * returns the used graph model for filtering
	 * 
	 * @method getGraphModel
	 * @return {@link GRAVIS3D.Model.Graph}
	 */
	this.getGraphModel = function() {
		return _graph;
	};
	/**
	 * Set the graph to use for filtering
	 * 
	 * @method setGraphModel
	 * @param {@link GRAVIS3D.Model.Graph} graph
	 */
	this.setGraphModel = function( graph ) {
		if ( graph instanceof GRAVIS3D.Model.Graph || graph instanceof GRAVIS3D.Model.DynamicGraph ) {
			_graph = graph;
		} else throw new Error( "Filtering: Filter " + self.getId()
				+ " #setGraphModel : param must be a GraphModel/Dynamic Graph Model ... (param: "
				+ JSON.stringify( graph ) + ")" );
	};

	// CONTEXT
	var isFor = "node"; // "node" | "edge"
	/**
	 * returns the context 
	 * 
	 * @method isFor
	 * @return {"node"|"edge"} isFor
	 */
	this.isFor = function() {
		return isFor;
	};
	/**
	 * sets the context
	 * 
	 * @method setFor
	 * @param {"node"|"edge"} nodeOrEdge
	 */
	this.setFor = function( nodeOrEdge ) {
		if ( nodeOrEdge.toLowerCase() == "node" || nodeOrEdge.toLowerCase() == "edge" ) {
			isFor = nodeOrEdge.toLowerCase();
		} else throw new Error( "Filtering: Filter " + this.getId()
				+ " # setFor: param must be  'node' or 'edge'... (param: " + JSON.stringify( nodeOrEdge ) + ")" );
	};

	// COMMAND
	var commandString = "";
	/**
	 * returns the actual used commandExpression in its string format
	 * 
	 * @method getCommandString
	 * @returns {String}
	 */
	this.getCommandString = function() {
		return commandString;
	};
	/**
	 * used to connect the rules<br>
	 * <strong>IMPORTANT: call this method AFTER you have set the filter's rules</strong><br>
	 * (reason: this method auto updates the result of the filter and will throw an error, if the referenced rules within this string are not there)
	 * 
	 * @method setCommandString
	 * @param {String} string - see example
	 * @example 
	 * " rule1.getId() & rule2.getId() "... <br>
	 * let rule1's id be 1 and rule2's 2 and rule3's 3: <br>
	 * "1&2"<br>
	 * "1&2|3"<br>
	 * "1|2&3"<br>
	 * "(1|2)&3"<br>
	 * ...
	 */
	this.setCommandString = function( string ) {
		var parsedObj = null;
		// check if its a string
		if ( typeof string != "string" ) { throw new Error( "Filtering: Filter " + this.getId()
				+ " #setCommandString: param is not a string (param: " + JSON.stringify( string ) + ")" ); }
		string = string.split( " " ).join( "" ); //remove Spaces..
		// empty string -> reset everything
		if ( string == "" ) {
			commandString = string;
			command = {};
			result = [];
			return;
		}
		// check if the string is a valid expression (syntax check), if so: set expression
		try {
			parsedObj = GRAVIS3D.Filtering.Parser.parse( string );
		} catch ( err ) {
			throw new Error( "Syntax Error parsing " + JSON.stringify( string ) + " -> " + err.message );
		}
		// check if the expression can be used with the specified rules (semantic check), if so: set result
		var self = this;
		try {
			function test( cmdExpression ) {
				if ( typeof cmdExpression == "string" ) {
					// single filter
					self.getRule( cmdExpression );
				} else {
					if ( cmdExpression["and"] ) {
						test( cmdExpression["and"][0] );
						test( cmdExpression["and"][1] );
					}
					if ( cmdExpression["or"] ) {
						test( cmdExpression["or"][0] );
						test( cmdExpression["or"][1] );
					}
				}
			}
			test( parsedObj );
			//test( GRAVIS3D.Filtering.Parser.parse( string ) );
		} catch ( err ) {
			throw new Error( "Semantic Error in " + JSON.stringify( string ) + " -> " + err.message );
		}
		commandString = string;
		command = parsedObj;
	};

	var command = {};
	/**
	 * the interpretable object build from a command string
	 * 
	 * @method getCommandExpression
	 * @return command
	 */
	this.getCommandExpression = function() {
		return command;
	};


	// RULES
	var rules = {}; // {ruleId: Rule}
	/**
	 * returns a rule for a given id
	 * 
	 * @method getRule
	 * @param {String} ruleId - {@link GRAVIS3D.Filtering.Rule#getId}
	 * @return {@link GRAVIS3D.Filtering.Rule}
	 */
	this.getRule = function( ruleId ) {
		if ( rules[ruleId] ) return rules[ruleId];
		else throw new Error( JSON.stringify( ruleId ) + " not found... Possible Rules: "
				+ Object.keys( rules ).toString() );
	};
	/**
	 * returns all rules
	 * 
	 * @method getRules
	 * @return rules - {ruleId: Rule}
	 */
	this.getRules = function() {
		return rules;
	};
	/**
	 * returns all rules
	 * 
	 * @method getRulesAsArray
	 * @return {@link GRAVIS3D.Filtering.Rule[]}  
	 */
	this.getRulesAsArray = function() {
		var array = [];
		for ( id in rules ) {
			array.push( rules[id] );
		}
		return array;
	};
	/**
	 * adds a rule
	 * 
	 * @method addRule
	 * @param {@link GRAVIS3D.Filtering.Rule} rule
	 */
	this.addRule = function( rule ) {
		if ( rule instanceof GRAVIS3D.Filtering.Rule ) {
			rules[rule.getId()] = rule;
		} else throw new Error( "Filtering: Filter " + this.getId() + " #addRule : param must be a Rule ... (param: "
				+ JSON.stringify( rule ) + ")" );
	};
	/**
	 * adds an array of rules
	 * 
	 * @method addRules
	 * @param {@link GRAVIS3D.Filtering.Rule[]} rules
	 */
	this.addRules = function( rules ) {
		for ( var i = 0; i < rules.length; i++ ) {
			this.addRule( rules[i] );
		}
	};
	/**
	 * sets all rules
	 * 
	 * @method setRules
	 * @param {@link GRAVIS3D.Filtering.Rule[]} rules
	 */
	this.setRules = function( rules ) {
		rules = {};
		this.addRules( rules );
	};
	/**
	 * removes a certain rule
	 * 
	 * @method removeRule
	 * @param {String} ruleId - {@link GRAVIS3D.Filtering.Rule#getId}
	 */
	this.removeRule = function( ruleId ) {
		if ( rules[ruleId] ) {
			if ( commandString.indexOf( ruleId ) != -1 ) {
				this.setCommandString( "" );
			}
			delete rules[ruleId];
		} else throw new Error( "Filtering: Filter " + this.getId()
				+ " #removeRule : param must be a valid ruleId ... (param: " + JSON.stringify( ruleId )
				+ "). Possible are: " + Object.keys( rules ).toString() );
	};

	//... RESULT
	var result = [];
	/**
	 * this is ment to be used with setters.. if you initialize @this with all params use @this#getResult
	 * 
	 * @method calculateResult
	 * @return {String[]} result - this ids of the filtered nodes / edges
	 */
	this.calculateResult = function() {

		if ( this.getRulesAsArray().length == 0 || this.getCommandString() == "" ) {
			result = [];
			return;
		}

		var graph = this.getGraphModel();

		var set;
		if ( this.isFor() == "edge" ) set = graph.getEdges( true );
		else set = graph.getNodes( true );
		var cmd = this.getCommandExpression();

		function exe( cmdExpression ) {
			if ( typeof cmdExpression == "string" ) {
				// single filter
				return useRule( cmdExpression );
			} else {
				// combined
				if ( cmdExpression["and"] ) { return difference( exe( cmdExpression["and"][0] ),
						exe( cmdExpression["and"][1] ) ); }
				if ( cmdExpression["or"] ) { return union( exe( cmdExpression["or"][0] ),
						exe( cmdExpression["or"][1] ) ); }
			}
		}
		var filteredIds = [];
		filteredIds = exe( cmd );

		function useRule( ruleId ) {
			var _result = [];

			var rule = self.getRule( ruleId );
			var attrId = rule.getAttributeId();
			var attr = graph.getAttributeById( attrId, self.isFor() );
			var attrType = attr.getType();
			var attrRange = attr.getRange();

			var value = rule.getValue();
			var operator = rule.getOperator();

			if ( attrType == "string" | attrType == "cluster" ) {
				//build array for cluster / string type and remove white spaces after comma
				value = value.split( "," ); // make array 
				for ( var i = 0; i < value.length; i++ ) {
					value[i] = value[i].trim(); // remove spaces
				}
			}
			if ( attrType == "number" ) {
				value = parseFloat( value );
			}

			for ( id in set ) {
				var n = set[id];
				var nodesAttrVal = n.getAttributeValue( attrId );
				//normalize numbers
				//				if ( attrType == "number" ) {
				//					nodesAttrVal = parseFloat( nodesAttrVal );
				//					if ( attrRange.getMax() != 0 ) {
				//						nodesAttrVal = nodesAttrVal / parseFloat( attrRange.getMax() );
				//						console.log(nodesAttrVal+" / " + attrRange.getMax())
				//					}
				//				}

				// compare attribute value (nodesAttrVal) to threshould (value)
				switch ( operator ) {
					case ">":
						if ( nodesAttrVal > value ) {
							_result.push( n.getId() );
						}
						break;
					case "<": {
						if ( nodesAttrVal < value ) {
							_result.push( n.getId() );
						}
						break;
					}
					case ">=":
						if ( nodesAttrVal >= value ) {
							_result.push( n.getId() );
						}
						break;
					case "<=": {
						if ( nodesAttrVal <= value ) {
							_result.push( n.getId() );
						}
						break;
					}
					case "!=": {
						// handle numbers a single value / string, cluster as possible array 
						if ( attrType == "string" | attrType == "cluster" ) {
							for ( var i = 0; i < value.length; i++ ) {
								if ( value[i] != nodesAttrVal ) {
									_result.push( n.getId() );
								}
							}
						}
						if ( attrType == "number" ) {
							if ( value != nodesAttrVal ) {
								_result.push( n.getId() );
							}
						}
						break;
					}
					default:
					case "=": {
						// handle numbers a single value / string, cluster as possible array 
						if ( attrType == "string" | attrType == "cluster" ) {
							for ( var i = 0; i < value.length; i++ ) {
								if ( value[i] == nodesAttrVal ) {
									_result.push( n.getId() );
								}
							}
						}
						if ( attrType == "number" ) {
							if ( value == nodesAttrVal ) {
								_result.push( n.getId() );
							}
						}
						break;
					}
				}
			}

			return _result;
		}

		function union( nodes1, nodes2 ) {
			var _result = nodes1.slice(0);
			for ( var i = 0; i < nodes2.length; i++ ) {
				var node = nodes2[i];
				if ( _result.indexOf( node ) == -1 ) _result.push( node );
			}
			return _result;
		}

		function difference( nodes1, nodes2 ) {
			var _result = [];
			for ( var i = 0; i < nodes2.length; i++ ) {
				var node = nodes2[i];
				if ( nodes1.indexOf( node ) != -1 ) _result.push( node );
			}
			return _result;
		}

		if ( filteredIds == undefined ) result = [];
		result = filteredIds;
	};
	/**
	 * if there were all params set or passed to @this, it returns the computed result
	 * 
	 * @method getResult
	 * @param graph *OPTIONAL*
	 * @return {String[]} result - the ids of the filtered nodes / edges
	 */
	this.getResult = function( graph ) {
		if ( graph ) this.setGraphModel( graph );
		this.calculateResult();
		return result;
	};

	// USEAGE WITH VISUAL REPRESENTATION
	var _visualVariable = null;
	/**
	 * gets the variable on which the filter will be applied to a visual representation
	 * 
	 * @method getVisualVariable
	 * @return {String} - have a look at GRAVIS3D.Filtering.Defaults.possibleVisualVariables
	 */
	this.getVisualVariable = function() {
		if ( _visualVariable == null ) visualVariable = GRAVIS3D.Filtering.Defaults.basicVisualVariable[this.isFor()];
		return _visualVariable;
	};
	/**
	 * gets the variable on which the filter will be applied to a visual representation
	 * 
	 * @method setVisualVariable
	 * @param {String} visualVariable - have a look at GRAVIS3D.Filter.Defaults.possibleVisualVariables
	 */
	this.setVisualVariable = function( visualVariable ) {
		if ( Object.keys( GRAVIS3D.Filtering.Defaults.possibleVisualVariables[this.isFor()] ).indexOf( visualVariable ) != -1 ) _visualVariable = visualVariable;
		else throw new Error( "Filtering: Filter " + this.getId()
				+ " #setVisualVariable : param must be a valid variable ... (param: " + JSON.stringify( visualVariable )
				+ "). Possible are: "
				+ Object.keys( GRAVIS3D.Filtering.Defaults.possibleVisualVariables[this.isFor()] ).toString() );
	};
	/**
	 * applies all filter rules to a visual representation (with a specific variable)
	 * 
	 * @method applyToVisualRepresentation
	 * @param visRep {@link GRAVIS3D.VisualRepresentation.Graph}
	 */
	this.applyToVisualRepresentation = function( visRep ) {

		var idsToFilter = this.getResult( visRep.getRepresentedGraphModel() );//getfilteredIds( visRep.isFor(), filter );
		var visVar;
		if ( this.getVisualVariable() != null ) visVar = this.getVisualVariable();
		else visVar = GRAVIS3D.Filtering.Defaults.basicVisualVariable[this.isFor()];
		var valFiltered = GRAVIS3D.Filtering.Defaults.possibleVisualVariables[this.isFor()][visVar].filtered;
		var valNotFiltered = GRAVIS3D.Filtering.Defaults.possibleVisualVariables[this.isFor()][visVar].notFiltered;

		if ( this.isFor() == "node" ) {
			var nodes = visRep.getNodeRepresentations();
			for ( id in nodes ) {
				var node = nodes[id];
				var modelId = node.getRepresentedModel().getId();
				if ( idsToFilter.indexOf( modelId ) != -1 ) {
					node.set( visVar, valFiltered );
					node.filtered = true;
				} else {
					node.set( visVar, valNotFiltered );
					node.filtered = false;
				}
				// filter all edges, which are connected to filtered nodes.. (if there is an similar channel for edges)
				if ( GRAVIS3D.Filtering.Defaults.possibleVisualVariables["edge"][visVar] != undefined ) {
					var edges = visRep.getEdgeRepresentations();
					for ( id in edges ) {
						var edge = edges[id];
						var sourceId = edge.getSource().getRepresentedModel().getId();
						var targetId = edge.getTarget().getRepresentedModel().getId();
						if ( idsToFilter.indexOf( sourceId ) != -1 || idsToFilter.indexOf( targetId ) != -1 ) {
							edge.filtered = true;
							edge.set( visVar, valFiltered );
						} else {
							if ( edge.filtered != true ) {
								edge.filtered = false;
								edge.set( visVar, valNotFiltered );
							}
						}
					}
				}
			}
		} else {
			var edges = visRep.getEdgeRepresentations();
			for ( id in edges ) {
				var edge = edges[id];
				var modelId = edge.getRepresentedModel().getId();
				if ( idsToFilter.indexOf( modelId ) != -1 ) {
					edge.filtered = true;
					edge.set( visVar, valFiltered );
				} else {
					if ( edge.filtered != true ) {
						edge.filtered = false;
						edge.set( visVar, valNotFiltered );
					}
				}
			}
		}



	};
	/**
	 * Removes @this from a visual representation
	 * 
	 * @method removeFromVisualRepresentation
	 * @param {@link GRAVIS3D.VisualRepresentation.Graph}visualRepresentation
	 */
	this.removeFromVisualRepresentation = function( visualRepresentation ) {
		var context = this.isFor();
		var edges = visualRepresentation.getEdgeRepresentations();
		for ( id in edges ) {
			edges[id].filtered = false;
		}
		var nodes = visualRepresentation.getNodeRepresentations();
		for ( id in nodes ) {
			nodes[id].filtered = false;
		}
		if ( context == "node" ) {
			visualRepresentation.resetVisualVariable( "node", this.getVisualVariable() );
			try {
				visualRepresentation.resetVisualVariable( "edge", this.getVisualVariable() );
			} catch ( err ) {
			}
		}
		if ( context == "edge" ) {
			visualRepresentation.resetVisualVariable( "edge", this.getVisualVariable() );
		}
	};

	// USEAGE WITH MODEL
	this.applyToModel = function( graph ) {
		if ( graph instanceof GRAVIS3D.Model.Graph ) graph.setFilter( this );
		else throw new Error( "Filtering: Filter " + this.getId()
				+ " #applyToModel : param must be a Model - Graph... (param: " + JSON.stringify( graph ) + ")." );
	};

	// COPY
	/**
	 * @return {@link GRAVIS3D.Filtering.Filter} @instance
	 */
	this.copy = function() {
		var rulesOrg = this.getRules();
		var rulesCopy = [];
		for ( id in rulesOrg ) {
			rulesCopy.push( rulesOrg[id].copy() );
		}
		return new GRAVIS3D.Filtering.Filter( {
			graph : self.getGraphModel(),
			isFor : self.isFor(),
			rules : rulesCopy,
			command : self.getCommandString(),
			visualVariable : self.getVisualVariable()
		} );
	};


	if ( !params || !params.isFor || !params.graph ) throw new Error( "Filtering: Filter " + this.getId()
			+ ": REQUIRED PARAMS: graph, isFor!" );

	this.setGraphModel( params.graph );
	this.setFor( params.isFor );

	if ( params.rules ) this.addRules( params.rules );
	if ( params.command ) this.setCommandString( params.command );

	if ( params.id ) this.setId( params.id );
	if ( params.name ) this.setName( params.name );
	if ( params.description ) this.setDescription( params.description );
	if ( params.data ) this.setData( params.data );

	if ( params.visualVariable ) {
		this.setVisualVariable( params.visualVariable );
	} else {
		this.setVisualVariable( GRAVIS3D.Filtering.Defaults.basicVisualVariable[this.isFor()] );
	}
	if ( params.visualRepresentation ) params.visualRepresentation.setFilter( this );

	if ( params.filterGraph == true ) this.getGraphModel().setFilter( this );
};

/**
 * @summary
 * A Rule for a Filter. Is used to identify nodes/edges, which fulfill the rules condition.<br>
 * Have a look at the example.
 * 
 * @since 1.0
 * 
 * @constructor Rule
 * @param { <br>
 * 		&emsp;id: {String} <i>*required*</i><br>
 * 		&emsp;attributeId: {String}, <i>*required*</i><br>
 * 		&emsp;operator: {">","<","!","="}, <i>*required*</i><br>
 * 		&emsp;value: {String | Number}, <i>*required*</i><br>
 * } params
 * 
 * @example
 *<code>
 * var rule1 = new GRAVIS3D.Filtering.Rule({<br>
 * 		&emsp;id: "rule_1",<br>
 * 		&emsp;attributeId: "foo",<br>
 * 		&emsp;operator: "=",<br>
 * 		&emsp;value: "bar"<br>
 * });<br>
 * var rule2 = new GRAVIS3D.Filtering.Rule({<br>
 * 		&emsp;id: "rule_2",<br>
 * 		&emsp;attributeId: "awsm",<br>
 * 		&emsp;operator: "<",<br>
 * 		&emsp;value: 0.8<br>
 * });<br>
 * someFilter.addRule( rule1 );<br>
 * someFilter.addRule( rule2 );<br>
 * </code><br>
 * rule1 filters all nodes/edges with attribute "foo" set to "bar"<br>
 * rule2 filters all nodes/edges with attribute "awsm" smaller than 0.8 (normalized between min and max)
 */
GRAVIS3D.Filtering.Rule = function( params ) {

	GRAVIS3D.InfoObject.call( this );

	// ATTRIBUTE ID
	var _attributeId = "";
	/**
	 * get the used attribute
	 * 
	 * @method getAttributeId
	 * @return {String}
	 */
	this.getAttributeId = function() {
		return _attributeId;
	};
	/**
	 * set the used attribute
	 * SHUOLD only consist of numbers letters and _
	 * (/^[a-zA-Z0-9_]+$/)
	 * 
	 * @method setAttributeId
	 * @param {String} attributeId - {@link GRAVIS3D.Model.Attribute#getId}
	 */
	this.setAttributeId = function( attributeId ) {
		var patt = new RegExp( /^[a-zA-Z0-9_]+$/ );
		if ( typeof attributeId == "string" && patt.test( attributeId ) ) _attributeId = attributeId;
		else throw new Error( "Filtering: Rule " + this.getId()
				+ " #setAttributeId : param must be a string ... (param: " + JSON.stringify( attributeId ) + ")" );
	};

	// OPERATOR
	var _operator = "=";
	/**
	 * returns the operator 
	 * 
	 * @method getOperator
	 * @return {">" | "<" | "=" | "!"} 
	 */
	this.getOperator = function() {
		return _operator;
	};
	/**
	 * sets the operator
	 * 
	 * @method setOperator
	 * @param {">" | "<" | "=" | "!"} operator
	 */
	this.setOperator = function( operator ) {
		if ( GRAVIS3D.Filtering.Defaults.possibleRuleOperators.all.indexOf( operator ) != -1 ) _operator = operator;
		else throw new Error( "Filtering: Rule " + this.getId() + " #setOperator : param must be a string ("
				+ GRAVIS3D.Filtering.Defaults.possibleRuleOperators.all.toString() + ") ... (param: "
				+ JSON.stringify( operator ) + ")" );
	};

	// VALUE
	var _value = "";
	/**
	 * get the constraining value
	 * 
	 * @method getValue
	 * @return {String} - Numbers are parsed automatically into strings and back later on..
	 */
	this.getValue = function() {
		return _value;
	};
	/**
	 * set the constraining value
	 * 
	 * @method setValue
	 * @param {String|Number} value - depending on attributes type
	 */
	this.setValue = function( value ) {
		if ( typeof value != "string" ) value = JSON.stringify( value );
		_value = value;
	};

	/**
	 * @return {GRAVIS3D.Filtering.Rule} @instance 
	 */
	this.copy = function() {
		return new GRAVIS3D.Filtering.Rule( {
			id : this.getId(),
			attributeId : this.getAttributeId(),
			operator : this.getOperator(),
			value : this.getValue()
		} );
	};

	if ( !params || !params.attributeId || !params.operator || !params.id ) throw new Error(
			"Filtering - REQUIRED PARAMS: id, attributeId, operator, value" );

	this.setId( params.id );
	this.setAttributeId( params.attributeId );
	this.setOperator( params.operator );
	this.setValue( params.value );
	if ( params.name ) this.setName( params.name );
	if ( params.description ) this.setDescription( params.description );
	if ( params.data ) this.setData( params.data );

};


GRAVIS3D.Filtering.Parser = ( function() {

	/**
	 * Generated by PEG.js 0.8.0.
	 * http://pegjs.majda.cz/
	 */
	function peg$subclass( child, parent ) {

		function ctor() {
			this.constructor = child;
		}
		ctor.prototype = parent.prototype;
		child.prototype = new ctor();
	}

	function SyntaxError( message, expected, found, offset, line, column ) {
		this.message = message;
		this.expected = expected;
		this.found = found;
		this.offset = offset;
		this.line = line;
		this.column = column;

		this.name = "SyntaxError";
	}

	peg$subclass( SyntaxError, Error );

	function parse( input ) {

		var options = arguments.length > 1 ? arguments[1] : {},

		peg$FAILED = {},

		peg$startRuleFunctions = {
			or : peg$parseor
		}, peg$startRuleFunction = peg$parseor,

		peg$c0 = peg$FAILED, peg$c1 = function( left, right ) {
			return {
				or : [ left, right ]
			}
		}, peg$c2 = function( left, right ) {
			return {
				and : [ left, right ]
			}
		}, peg$c3 = "(", peg$c4 = {
			type : "literal",
			value : "(",
			description : "\"(\""
		}, peg$c5 = ")", peg$c6 = {
			type : "literal",
			value : ")",
			description : "\")\""
		}, peg$c7 = function( or ) {
			return or;
		}, peg$c8 = [], peg$c9 = /^[0-9a-zA-Z\xF6\xE4\xFC_#]/, peg$c10 = {
			type : "class",
			value : "[0-9a-zA-Z\\xF6\\xE4\\xFC_#]",
			description : "[0-9a-zA-Z\\xF6\\xE4\\xFC_#]"
		}, peg$c11 = function( string ) {
			return string.join( "" );
		}, peg$c12 = /^["|", "||"]/, peg$c13 = {
			type : "class",
			value : "[\"|\", \"||\"]",
			description : "[\"|\", \"||\"]"
		}, peg$c14 = /^["&","&&"]/, peg$c15 = {
			type : "class",
			value : "[\"&\",\"&&\"]",
			description : "[\"&\",\"&&\"]"
		}, peg$c16 = function( and ) {
			return and;
		}, peg$c17 = " ", peg$c18 = {
			type : "literal",
			value : " ",
			description : "\" \""
		},

		peg$currPos = 0, peg$reportedPos = 0, peg$cachedPos = 0, peg$cachedPosDetails = {
			line : 1,
			column : 1,
			seenCR : false
		}, peg$maxFailPos = 0, peg$maxFailExpected = [], peg$silentFails = 0,

		peg$result;

		if ( "startRule" in options ) {
			if ( !( options.startRule in peg$startRuleFunctions ) ) { throw new Error(
					"Can't start parsing from rule \"" + options.startRule + "\"." ); }

			peg$startRuleFunction = peg$startRuleFunctions[options.startRule];
		}

		function text() {
			return input.substring( peg$reportedPos, peg$currPos );
		}

		function offset() {
			return peg$reportedPos;
		}

		function line() {
			return peg$computePosDetails( peg$reportedPos ).line;
		}

		function column() {
			return peg$computePosDetails( peg$reportedPos ).column;
		}

		function expected( description ) {
			throw peg$buildException( null, [ {
				type : "other",
				description : description
			} ], peg$reportedPos );
		}

		function error( message ) {
			throw peg$buildException( message, null, peg$reportedPos );
		}

		function peg$computePosDetails( pos ) {

			function advance( details, startPos, endPos ) {
				var p, ch;

				for ( p = startPos; p < endPos; p++ ) {
					ch = input.charAt( p );
					if ( ch === "\n" ) {
						if ( !details.seenCR ) {
							details.line++;
						}
						details.column = 1;
						details.seenCR = false;
					} else if ( ch === "\r" || ch === "\u2028" || ch === "\u2029" ) {
						details.line++;
						details.column = 1;
						details.seenCR = true;
					} else {
						details.column++;
						details.seenCR = false;
					}
				}
			}

			if ( peg$cachedPos !== pos ) {
				if ( peg$cachedPos > pos ) {
					peg$cachedPos = 0;
					peg$cachedPosDetails = {
						line : 1,
						column : 1,
						seenCR : false
					};
				}
				advance( peg$cachedPosDetails, peg$cachedPos, pos );
				peg$cachedPos = pos;
			}

			return peg$cachedPosDetails;
		}

		function peg$fail( expected ) {
			if ( peg$currPos < peg$maxFailPos ) { return; }

			if ( peg$currPos > peg$maxFailPos ) {
				peg$maxFailPos = peg$currPos;
				peg$maxFailExpected = [];
			}

			peg$maxFailExpected.push( expected );
		}

		function peg$buildException( message, expected, pos ) {

			function cleanupExpected( expected ) {
				var i = 1;

				expected.sort( function( a, b ) {
					if ( a.description < b.description ) {
						return -1;
					} else if ( a.description > b.description ) {
						return 1;
					} else {
						return 0;
					}
				} );

				while ( i < expected.length ) {
					if ( expected[i - 1] === expected[i] ) {
						expected.splice( i, 1 );
					} else {
						i++;
					}
				}
			}

			function buildMessage( expected, found ) {

				function stringEscape( s ) {

					function hex( ch ) {
						return ch.charCodeAt( 0 ).toString( 16 ).toUpperCase();
					}

					return s.replace( /\\/g, '\\\\' ).replace( /"/g, '\\"' ).replace( /\x08/g, '\\b' ).replace( /\t/g,
							'\\t' ).replace( /\n/g, '\\n' ).replace( /\f/g, '\\f' ).replace( /\r/g, '\\r' ).replace(
							/[\x00-\x07\x0B\x0E\x0F]/g, function( ch ) {
								return '\\x0' + hex( ch );
							} ).replace( /[\x10-\x1F\x80-\xFF]/g, function( ch ) {
						return '\\x' + hex( ch );
					} ).replace( /[\u0180-\u0FFF]/g, function( ch ) {
						return '\\u0' + hex( ch );
					} ).replace( /[\u1080-\uFFFF]/g, function( ch ) {
						return '\\u' + hex( ch );
					} );
				}

				var expectedDescs = new Array( expected.length ), expectedDesc, foundDesc, i;

				for ( i = 0; i < expected.length; i++ ) {
					expectedDescs[i] = expected[i].description;
				}

				expectedDesc = expected.length > 1 ? expectedDescs.slice( 0, -1 ).join( ", " ) + " or "
						+ expectedDescs[expected.length - 1] : expectedDescs[0];

				foundDesc = found ? "\"" + stringEscape( found ) + "\"" : "end of input";

				return "Expected " + expectedDesc + " but " + foundDesc + " found.";
			}

			var posDetails = peg$computePosDetails( pos ), found = pos < input.length ? input.charAt( pos ) : null;

			if ( expected !== null ) {
				cleanupExpected( expected );
			}

			return new SyntaxError( message !== null ? message : buildMessage( expected, found ), expected, found, pos,
					posDetails.line, posDetails.column );
		}

		function peg$parseor() {
			var s0, s1, s2, s3;

			s0 = peg$currPos;
			s1 = peg$parseand();
			if ( s1 !== peg$FAILED ) {
				s2 = peg$parseor_operator();
				if ( s2 !== peg$FAILED ) {
					s3 = peg$parseor();
					if ( s3 !== peg$FAILED ) {
						peg$reportedPos = s0;
						s1 = peg$c1( s1, s3 );
						s0 = s1;
					} else {
						peg$currPos = s0;
						s0 = peg$c0;
					}
				} else {
					peg$currPos = s0;
					s0 = peg$c0;
				}
			} else {
				peg$currPos = s0;
				s0 = peg$c0;
			}
			if ( s0 === peg$FAILED ) {
				s0 = peg$parseand();
			}

			return s0;
		}

		function peg$parseand() {
			var s0, s1, s2, s3;

			s0 = peg$currPos;
			s1 = peg$parseprimary();
			if ( s1 !== peg$FAILED ) {
				s2 = peg$parseand_operator();
				if ( s2 !== peg$FAILED ) {
					s3 = peg$parseand();
					if ( s3 !== peg$FAILED ) {
						peg$reportedPos = s0;
						s1 = peg$c2( s1, s3 );
						s0 = s1;
					} else {
						peg$currPos = s0;
						s0 = peg$c0;
					}
				} else {
					peg$currPos = s0;
					s0 = peg$c0;
				}
			} else {
				peg$currPos = s0;
				s0 = peg$c0;
			}
			if ( s0 === peg$FAILED ) {
				s0 = peg$parseprimary();
			}

			return s0;
		}

		function peg$parseprimary() {
			var s0, s1, s2, s3;

			s0 = peg$parseruleId();
			if ( s0 === peg$FAILED ) {
				s0 = peg$currPos;
				if ( input.charCodeAt( peg$currPos ) === 40 ) {
					s1 = peg$c3;
					peg$currPos++;
				} else {
					s1 = peg$FAILED;
					if ( peg$silentFails === 0 ) {
						peg$fail( peg$c4 );
					}
				}
				if ( s1 !== peg$FAILED ) {
					s2 = peg$parseor();
					if ( s2 !== peg$FAILED ) {
						if ( input.charCodeAt( peg$currPos ) === 41 ) {
							s3 = peg$c5;
							peg$currPos++;
						} else {
							s3 = peg$FAILED;
							if ( peg$silentFails === 0 ) {
								peg$fail( peg$c6 );
							}
						}
						if ( s3 !== peg$FAILED ) {
							peg$reportedPos = s0;
							s1 = peg$c7( s2 );
							s0 = s1;
						} else {
							peg$currPos = s0;
							s0 = peg$c0;
						}
					} else {
						peg$currPos = s0;
						s0 = peg$c0;
					}
				} else {
					peg$currPos = s0;
					s0 = peg$c0;
				}
			}

			return s0;
		}

		function peg$parseruleId() {
			var s0, s1, s2, s3;

			s0 = peg$currPos;
			s1 = peg$parse_();
			if ( s1 !== peg$FAILED ) {
				s2 = [];
				if ( peg$c9.test( input.charAt( peg$currPos ) ) ) {
					s3 = input.charAt( peg$currPos );
					peg$currPos++;
				} else {
					s3 = peg$FAILED;
					if ( peg$silentFails === 0 ) {
						peg$fail( peg$c10 );
					}
				}
				if ( s3 !== peg$FAILED ) {
					while ( s3 !== peg$FAILED ) {
						s2.push( s3 );
						if ( peg$c9.test( input.charAt( peg$currPos ) ) ) {
							s3 = input.charAt( peg$currPos );
							peg$currPos++;
						} else {
							s3 = peg$FAILED;
							if ( peg$silentFails === 0 ) {
								peg$fail( peg$c10 );
							}
						}
					}
				} else {
					s2 = peg$c0;
				}
				if ( s2 !== peg$FAILED ) {
					s3 = peg$parse_();
					if ( s3 !== peg$FAILED ) {
						peg$reportedPos = s0;
						s1 = peg$c11( s2 );
						s0 = s1;
					} else {
						peg$currPos = s0;
						s0 = peg$c0;
					}
				} else {
					peg$currPos = s0;
					s0 = peg$c0;
				}
			} else {
				peg$currPos = s0;
				s0 = peg$c0;
			}

			return s0;
		}

		function peg$parseor_operator() {
			var s0, s1, s2, s3;

			s0 = peg$currPos;
			s1 = peg$parse_();
			if ( s1 !== peg$FAILED ) {
				if ( peg$c12.test( input.charAt( peg$currPos ) ) ) {
					s2 = input.charAt( peg$currPos );
					peg$currPos++;
				} else {
					s2 = peg$FAILED;
					if ( peg$silentFails === 0 ) {
						peg$fail( peg$c13 );
					}
				}
				if ( s2 !== peg$FAILED ) {
					s3 = peg$parse_();
					if ( s3 !== peg$FAILED ) {
						peg$reportedPos = s0;
						s1 = peg$c7( s2 );
						s0 = s1;
					} else {
						peg$currPos = s0;
						s0 = peg$c0;
					}
				} else {
					peg$currPos = s0;
					s0 = peg$c0;
				}
			} else {
				peg$currPos = s0;
				s0 = peg$c0;
			}

			return s0;
		}

		function peg$parseand_operator() {
			var s0, s1, s2, s3;

			s0 = peg$currPos;
			s1 = peg$parse_();
			if ( s1 !== peg$FAILED ) {
				if ( peg$c14.test( input.charAt( peg$currPos ) ) ) {
					s2 = input.charAt( peg$currPos );
					peg$currPos++;
				} else {
					s2 = peg$FAILED;
					if ( peg$silentFails === 0 ) {
						peg$fail( peg$c15 );
					}
				}
				if ( s2 !== peg$FAILED ) {
					s3 = peg$parse_();
					if ( s3 !== peg$FAILED ) {
						peg$reportedPos = s0;
						s1 = peg$c16( s2 );
						s0 = s1;
					} else {
						peg$currPos = s0;
						s0 = peg$c0;
					}
				} else {
					peg$currPos = s0;
					s0 = peg$c0;
				}
			} else {
				peg$currPos = s0;
				s0 = peg$c0;
			}

			return s0;
		}

		function peg$parse_() {
			var s0, s1;

			s0 = [];
			if ( input.charCodeAt( peg$currPos ) === 32 ) {
				s1 = peg$c17;
				peg$currPos++;
			} else {
				s1 = peg$FAILED;
				if ( peg$silentFails === 0 ) {
					peg$fail( peg$c18 );
				}
			}
			while ( s1 !== peg$FAILED ) {
				s0.push( s1 );
				if ( input.charCodeAt( peg$currPos ) === 32 ) {
					s1 = peg$c17;
					peg$currPos++;
				} else {
					s1 = peg$FAILED;
					if ( peg$silentFails === 0 ) {
						peg$fail( peg$c18 );
					}
				}
			}

			return s0;
		}

		peg$result = peg$startRuleFunction();

		if ( peg$result !== peg$FAILED && peg$currPos === input.length ) {
			return peg$result;
		} else {
			if ( peg$result !== peg$FAILED && peg$currPos < input.length ) {
				peg$fail( {
					type : "end",
					description : "end of input"
				} );
			}

			throw peg$buildException( null, peg$maxFailExpected, peg$maxFailPos );
		}
	}

	return {
		SyntaxError : SyntaxError,
		parse : parse
	};
} )();
