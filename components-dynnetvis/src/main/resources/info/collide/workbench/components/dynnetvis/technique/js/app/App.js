GRAVIS3D.App = {};
GRAVIS3D.App.Defaults = {
	GUI : {
		targetDomElement : $( '#wrapper' )
	},
	Instance : {
		layouter : GRAVIS3D.Layout.GraphLayoutBuilder.ForceDirected2D,
		superLayouter : GRAVIS3D.Layout.DynamicGraphLayoutBuilder.Supergraph,
		view : GRAVIS3D.Views.TimeSliceView
	},
	InstanceManager : {},
	WaitScreen : {
		title : "loading",
		msg : "",
		targetDomElement : $( "body" )
	}
};

GRAVIS3D.App.Instance = function( params ) {

	GRAVIS3D.InfoObject.call( this );
	var self = this;

	if ( !params || !params.graph ) throw new Error( "App: Instance " + this.getId()
			+ ": REQUIRED PARAMS are: graph, instanceManager" );

	// graph
	this.graph = null;
	if ( params.graph instanceof GRAVIS3D.Model.DynamicGraph ) {
		this.graph = params.graph;
	} else if ( params.graph instanceof GRAVIS3D.Model.Graph ) {
		this.graph = new GRAVIS3D.Model.DynamicGraph( {
			graphs : [ params.graph ]
		} );
	} else throw new Error( "App: Instance " + this.getId()
			+ ": #setGraph: param must be a Dynamic Graph Model... (param: " + JSON.stringify( params.graph ) + ")" );
	this.getGraph = function() {
		return this.graph;
	};

	// visual representation
	var visRep = null;
	if ( params.visRep ) visRep = params.visRep;
	this.getVisualRepresentation = function() {
		return visRep;
	};

	// layouter
	this.layouter = GRAVIS3D.App.Defaults.Instance.layouter;
	if ( params.layouter ) layouter = params.layouter;

	// superlayouter
	this.superLayouter = GRAVIS3D.App.Defaults.Instance.superLayouter;
	if ( params.superLayouter ) superLayouter = params.superLayouter;

	this.layouts = {}; //graphId -> layout
	if ( params.layouts ) this.layouts = params.layouts;

	// view
	this.viewBuilder = GRAVIS3D.App.Defaults.Instance.view;
	if ( params.viewBuilder ) this.viewBuilder = params.viewBuilder;
	this.view = null;
	this.getView = function() {
		return this.view;
	};
	if ( params.view ) this.view = params.view;

	// filters
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
	if ( params.filters ) filters = params.filters;
	this.filterManager = new GRAVIS3D.Filtering.Manager( {
		graph : self.getGraph(),
		onDataFiltersChange : function( dataFilters ) {
			LOADSCREEN.msg( "Removing Old Data Filters", function() {
				// remove old filters..
				if ( filters.data.node != null ) self.graph.removeFilter( "node" );
				if ( filters.data.edge != null ) self.graph.removeFilter( "edge" );
				// apply new ones
				if ( dataFilters.node != null ) filters.data.node = dataFilters.node.copy();
				else filters.data.node = null;
				if ( dataFilters.edge != null ) filters.data.edge = dataFilters.edge.copy();
				else filters.data.edge = null;
				self.execute( "data_filters" );
			} );
		},
		onViewFiltersChange : function( viewFilters ) {
			LOADSCREEN.msg( "Clearing Visual Representation", function() {
				// remove old filters..
				if ( filters.view.node != null ) visRep.removeFilter( "node" );
				if ( filters.view.edge != null ) visRep.removeFilter( "edge" );
				// apply new ones
				if ( viewFilters.node != null ) filters.view.node = viewFilters.node.copy();
				else filters.view.node = null;
				if ( viewFilters.edge != null ) filters.view.edge = viewFilters.edge.copy();
				else filters.view.edge = null;
				self.execute( "view_filters" );
			} );
		}
	} );
	this.getFilterManager = function() {
		return this.filterManager;
	};

	// mappings
	var _mappings = {};
	if ( params.mapping ) _mappings = params.mappings;
	this.mappingManager = new GRAVIS3D.Mapping.Manager( {
		graph : self.getGraph(),
		onChange : function( mappings ) {
			LOADSCREEN.msg( "Clearing Visual Representation", function() {
				// remove old mappings..
				visRep.removeMappings( Object.keys( _mappings ) );
				//			for ( id in _mappings ) {
				//			}
				// set new mappings	
				_mappings = {};
				for ( id in mappings ) {
					var cp = mappings[id].copy();
					_mappings[cp.getId()] = cp;
				}

				self.execute( "mappings" );
			} );
		}
	} );
	this.getMappingManager = function() {
		return this.mappingManager;
	};

	/**
	 * MAIN FLOW - from model to view...
	 */
	this.execute = function( startingPoint ) {
		
		if ( startingPoint == null ) {
			startingPoint = "model";
			//			if ( this.layouts != null ) startingPoint = "layout";
		}

		var t_0;
		switch ( startingPoint.toLowerCase() ) {
			case "model":
				
				// XXX loader here (files as params) 
				// handle async file loading somehow (redefine complete gui building logic - is synchronous on object creation...)
				
//				console.log( "########################################" );
//				console.log( "GRAVIS3D.App.Instance: #build - model..." );
//				LOADSCREEN.msg( "Building Model", function() {
//					t_0 = new Date();
//					setTimeout( function() {
//						self.execute( "data_filters" );
//					}, 1 );
//					console.log( "===> " + ( new Date() - t_0 ) + " ms <===" );
//				} );
//				break;
				
			case "data_filters":
				console.log( "########################################" );
				console.log( "GRAVIS3D.App.Instance: #build - data filters..." );
				LOADSCREEN.msg( "Applying Data Filters", function() {
					t_0 = new Date();
					var gs = self.graph.getGraphs();
					for ( id in gs ) {
						if ( filters.data.node != null ) {
							gs[id].setNodeFilter( filters.data.node );
						}
						if ( filters.data.edge != null ) {
							gs[id].setEdgeFilter( filters.data.edge );
						}
					}
					self.GUI.update( "data_filters" );
					setTimeout( function() {
						self.execute( "layout" );
					}, 10 );
					console.log( "===> " + ( new Date() - t_0 ) + " ms <===" );
				} );
				break;
			case "layout":
				console.log( "########################################" );
				console.log( "GRAVIS3D.App.Instance: #build - layouts..." );
				LOADSCREEN.msg( "Calculating Layout", function() {
					t_0 = new Date();

					self.layouts = self.superLayouter( {
						subLayouter : self.layouter,
						graph : self.graph
					} );


					self.GUI.update( "layout" );
					setTimeout( function() {
						self.execute( "visual_representation" );
					}, 1 );
					console.log( "===> " + ( new Date() - t_0 ) + " ms <===" );
				} );
				break;
			case "visual_representation":
				console.log( "########################################" );
				console.log( "GRAVIS3D.App.Instance: #build - visual representations..." );
				LOADSCREEN.msg( "Creating Visual Representations", function() {
					t_0 = new Date();
					if (self.view != null )	self.view.pauseRendering();
					
					visRep = new GRAVIS3D.VisualRepresentation.DynamicGraph( {
						represents : self.graph
					} );

					var graphReps = visRep.getGraphRepresentations();
					for ( graphId in self.layouts ) {
						for ( id in graphReps ) {
							if ( graphReps[id].getRepresentedGraphModel().getId() == graphId ) {
								graphReps[id].setLayout( self.layouts[graphId] );
							}
						}
					}

					self.GUI.update( "visual_representation" );
					setTimeout( function() {
						self.execute( "view" );
					}, 1 );
					console.log( "===> " + ( new Date() - t_0 ) + " ms <===" );
				} );
				break;
			case "view":
				console.log( "########################################" );
				console.log( "GRAVIS3D.App.Instance: #build - view..." );
				LOADSCREEN.msg( "Initializing View", function() {
					t_0 = new Date();

					if (self.view != null )	self.view.pauseRendering();
					self.view = new self.viewBuilder( {
						visualRepresentation : visRep
					} );
					self.view.render();

					self.GUI.update( "view" );
					setTimeout( function() {
						self.execute( "mappings" );
					}, 1 );
					console.log( "===> " + ( new Date() - t_0 ) + " ms <===" );
				} );
				break;
			case "mappings":
				// TODO : clear mappings here in future, to avoid initalizing a new view with old mappings (same for filters)
				console.log( "########################################" );
				console.log( "GRAVIS3D.App.Instance: #build - mappings..." );
				LOADSCREEN.msg( "Applying Attribute Mappings", function() {
					t_0 = new Date();

					for ( id in _mappings ) {
						visRep.addMapping( _mappings[id] );
					}
					self.GUI.update( "mappings" );
					setTimeout( function() {
						self.execute( "view_filters" );
					}, 1 );
					console.log( "===> " + ( new Date() - t_0 ) + " ms <===" );
				} );
				break;
			case "view_filters":
				console.log( "########################################" );
				console.log( "GRAVIS3D.App.Instance: #build - view filters..." );
				LOADSCREEN.msg( "Applying View Filters", function() {
					t_0 = new Date();

					if ( filters.view.node != null ) {
						visRep.setNodeFilter( filters.view.node );
					}
					if ( filters.view.edge != null ) {
						visRep.setEdgeFilter( filters.view.edge );
					}
					self.GUI.update( "view_filters" );

					console.log( "===> " + ( new Date() - t_0 ) + " ms <===" );

					LOADSCREEN.exit();
				} );
				break;
		}

	};

	this.copy = function() {
		var copy = new GRAVIS3D.App.Instance( {
			graph : deepCopy( self.graph ),
			viewBuilder : self.viewBuilder,
			layouter : self.layouter,
			superLayouter : self.superLayouter,
			layouts : self.layouts,
			mappings : _mappings,
//			filters : filters
		} );
		return copy;
	};

	///////////////////////////

	// gui
	//	this.targetDomElement;
	this.GUI = new GRAVIS3D.GUI.Instance( this );
	this.getGUI = function() {
		return this.GUI;
	};
	//	this.execute();
};


GRAVIS3D.GUI.Instance = function( instance ) {

	GRAVIS3D.InfoObject.call( this );
	var self = this;

	var viewElPanelWidth = 8;
	var controlElPanelWidth = 4;

	var domElement = $( '<div class="app" />' );
	this.getDomElement = function() {
		return domElement;
	};

	// view panel...
	var viewEl = $( '<div class="viewEl">' );
	viewEl.addClass( "col-sm-" + viewElPanelWidth );
	var fullScreenBtn = $(
			'<button type="button" class="fullscreen_btn btn btn-link btn-md"><span class="glyphicon glyphicon-fullscreen"></span></button>' )
			.appendTo( viewEl );
	fullScreenBtn.on( "click enter", function() {
		self.state.normal();
		instance.getView().fullscreen();
	} );

	// control panel...
	var controlPanelEl = $( "<div id='controls' class='controlsPanel'></div>" );
	controlPanelEl.addClass( "col-sm-" + controlElPanelWidth );
	var controlPanelElContent = $( '<div></div>' ).appendTo( controlPanelEl );
	var closeControlsBtn = $(
			'<button type="button" class="closeControls_btn btn btn-danger btn-md"><span class="glyphicon glyphicon-remove"></span></button>' )
			.appendTo( controlPanelEl );
	closeControlsBtn.on( "click enter", function() {
		self.state.normal();
	} );

	// control menu
	var controlMenuEl = $( "<div class='controlsMenu'></div>" );
	//TODO use dropdown btn for menu ... code below
	//		$('<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">');
	//	controlMenuEl<div class="dropdown">
	//	  <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown">
	//	    Dropdown
	//	    <span class="caret"></span>
	//	  </button>
	//	  <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
	//	    <li role="presentation"><a role="menuitem" tabindex="-1" href="#">Action</a></li>
	//	    <li role="presentation"><a role="menuitem" tabindex="-1" href="#">Another action</a></li>
	//	    <li role="presentation"><a role="menuitem" tabindex="-1" href="#">Something else here</a></li>
	//	    <li role="presentation" class="divider"></li>
	//	    <li role="presentation"><a role="menuitem" tabindex="-1" href="#">Separated link</a></li>
	//	  </ul>
	//	</div>

	//////////////////////////

	// state machine
	this.state = {
		normal : function() {
			element_to_scroll_to = document.getElementById( 'tabs' );
			element_to_scroll_to.scrollIntoView();

			viewEl.show();
			controlMenuEl.show();
			controlPanelEl.hide();

			// view resize
			viewEl.addClass( 'col-sm-12' );
			viewEl.removeClass( 'col-sm-' + viewElPanelWidth );

			viewEl.find( '.view canvas' ).removeClass( 'innerBorder' );
			if ( instance.getView() != null ) instance.getView().handleResize();


		},
		inControls : function( name ) {
			element_to_scroll_to = document.getElementById( 'controls' );
			element_to_scroll_to.scrollIntoView();
			setTimeout( function() {
				element_to_scroll_to.scrollIntoView();
			}, 50 );

			viewEl.show();
			controlMenuEl.show();
			controlPanelEl.show();

			// make split screen - therefore: resize view 
			viewEl.removeClass( 'col-sm-12' );
			viewEl.addClass( 'col-sm-' + viewElPanelWidth );
			controlPanelElContent.children().hide();
			var el = controlPanelElContent.find( ".controls_" + name );
			el.show();
			viewEl.find( '.view canvas' ).addClass( 'innerBorder' );
			//			viewEl.removeClass('col-sm-6');
			if ( instance.getView() != null ) instance.getView().handleResize();

		},
		loading : function() {
			viewEl.hide();
			controlMenuEl.hide();
			controlPanelEl.hide();
			// zeige ladeschirm
		}
	};

	// esc press -> state normal
	$( document ).keyup( function( e ) {
		if ( e.keyCode == 27 ) {
			self.state.normal();
		}
	} );

	////////////////////////////

	/**
	 * react on changed app values
	 */
	this.update = function( startingPoint ) {
		if ( startingPoint == null ) startingPoint = "view";
		switch ( startingPoint.toLowerCase() ) {
			case "model":
				break;

			break;
		case "visual_representation":
			break;
		case "layout":
			break;
		case "view":
			// update view el
			viewEl.find( '.view' ).remove();
			viewEl.append( instance.getView().getDomElement() );
			instance.filterManager.refreshGUI();
			instance.mappingManager.refreshGUI();
			updateControls( instance.getView().controls );
			instance.getView().handleResize();//scroll pane not hidden in chrome - do it again...
			instance.getView().handleResize();
			this.state.normal();
			break;
		case "mappings":
			break;
		case "view_filters":
			break;
		default:
			break;
	}

}	;

	// rebuild controls with new view..
	function updateControls( viewoptions ) {

		controlPanelElContent.empty();
		controlMenuEl.empty();

		var toHide = viewoptions.hide;
		var toAppend = viewoptions.own;

		// controls... 
		var controlElements = {
			info : {
				btn : function() {
					return $( '<button type="button" class="btn btn-primary btn-lg"><span class="glyphicon glyphicon-info-sign"></span></button>' );
				},
				content : function() {
					var wrapper = $( "<div></div>" );
					wrapper.append( "<h2>Metadata</h2><hr />" )
					var content = $( '<div class="jumbotron"><h3>Files:</h3></div>' ).appendTo( wrapper );
					content.append( "<hr />" )
					var snaps = instance.getGraph().getOrderedSnapshots();
					for ( var i = 0; i < snaps.length; i++ ) {
						var p = $( '<p><strong>' + snaps[i].getData( "file" ) + '</strong></p>' );
						p.append( "<div>Title: " + snaps[i].getGraph().getName() + "</div>" );
						p.append( "<div>Order: " + snaps[i].getTime().getOrder() + "</div>" );
						p.append( "<div>Time: " + snaps[i].getGraph().getData( "time" ) + "</div>" );
						p.append( "<div>Type: " + snaps[i].getGraph().getData( "type" ) + "</div>" );
						p.append( "<div>Directed: " + snaps[i].getGraph().getData( "directed" ) + "</div>" );
						p.append( "<div>Description: " + snaps[i].getGraph().getDescription() + "</div>" );
						content.append( p );
					}
					return wrapper;
				}
			},
			search : {
				btn : function() {
					return $( '<button type="button" class="btn btn-primary btn-lg"><span class="glyphicon glyphicon-search"></span></button>' );
				},
				content : function() {
					var content = $( '<div class=""><h2>Search</h2></div>' );

					content.append( "<hr />" );
					var panelWrapperParent = $( "<div class='panel panel-default ' />" ).appendTo( content );
					var panelWrapper = $( "<div class='panel-body' />" ).appendTo( panelWrapperParent );

					// select - view
					var searchDiv = $( "<div class='col-sm-12' />" ).appendTo( panelWrapper );
					searchDiv.append( "<h4><label for='search'>Lookup Node<label></h4>" );
					var datalistId = GRAVIS3D.ID.get();
					var search = $(
							'<input placeholder="type in a node\'s id"list="' + datalistId
									+ '" id="search" name="search" type="search" class="form-control">' ).appendTo(
							searchDiv );
					var list = $( '<datalist id="' + datalistId + '" />' ).appendTo( searchDiv );
					var nodes = instance.graph.getNodes();
					var visReps = instance.view.getVisualRepresentation().getGraphRepresentations();
					for ( id in nodes ) {
						list.append( '<option id="' + id + '">' + id + '</option>' );
					}
					var okBtn = $( '<br><button type="button" class="btn app_apply_btn btn-primary">Highlight</button>' )
							.appendTo( searchDiv );
					var clearBtn = $(
							'<button type="button" class="btn pull-right app_apply_btn btn-primary">Unhighlight All</button>' )
							.appendTo( searchDiv );
					var found = {};
					search.on( "change click enter", function() {
						searchNode();
					} );
					okBtn.on( "change click enter", function() {
						searchNode();
					} );
					function searchNode() {
						var foundIds = Object.keys( found );
						if ( foundIds.indexOf( search.val() ) == -1 ) {
							for ( id in visReps ) {
								try {
									var node = visReps[id].getNodeRepresentationByNodeModelId( search.val() );
									found[search.val()] = {};
									found[search.val()].size = node.getSize();
									node.setSize( node.getSize() * 2 );
									node.highlight();
								} catch ( err ) {
								}
							}
						}
					}
					clearBtn.on( "click enter", function() {
						for ( id in found ) {
							for ( v in visReps ) {
								try {
									var node = visReps[v].getNodeRepresentationByNodeModelId( id );
									node.setSize( found[id].size );
									node.removeHighlight();
								} catch ( err ) {
								}
							}
						}
						search.val( "" );
						found = {};
					} );

					searchDiv.append( "<hr />" );

					searchDiv.append( "<h4><label for='search'>Lookup Edge<label></h4>" );
					var datalistId2 = GRAVIS3D.ID.get();
					var search2 = $(
							'<input placeholder="type in a edge\'s id"list="' + datalistId2
									+ '" id="search" name="search" type="search" class="form-control">' ).appendTo(
							searchDiv );
					var list = $( '<datalist id="' + datalistId2 + '" />' ).appendTo( searchDiv );
					var edges = instance.graph.getEdges();
					for ( id in edges ) {
						list.append( '<option id="' + id + '">' + id + '</option>' );
					}
					var okBtn = $( '<br><button type="button" class="btn app_apply_btn btn-primary">Highlight</button>' )
							.appendTo( searchDiv );
					var clearBtn = $(
							'<button type="button" class="btn pull-right app_apply_btn btn-primary">Unhighlight All</button>' )
							.appendTo( searchDiv );
					var found3 = {};
					search2.on( "change click enter", function() {
						searchEdge();
					} );
					okBtn.on( "change click enter", function() {
						searchEdge();
					} );
					function searchEdge() {
						var foundIds = Object.keys( found3 );
						if ( foundIds.indexOf( search2.val() ) == -1 ) {
							for ( id in visReps ) {
								try {
									var edge = visReps[id].getEdgeRepresentationByEdgeModelId( search2.val() );
									found3[search2.val()] = {};
									edge.highlight();
								} catch ( err ) {
								}
							}
						}
					}
					clearBtn.on( "click enter", function() {
						for ( id in found3 ) {
							for ( v in visReps ) {
								try {
									var edge = visReps[v].getEdgeRepresentationByEdgeModelId( id );
									edge.removeHighlight();
								} catch ( err ) {
								}
							}
						}
						search2.val( "" );
						found3 = {};
					} );
					return content;
				}
			},
			global : {
				btn : function() {
					return $( '<button type="button" class="btn btn-primary btn-lg"><span class="glyphicon glyphicon glyphicon-globe"></span></button>' );
				},
				content : function() {
					var content = $( '<div class=""><h2>Global Controls</h2></div>' );

					content.append( "<hr />" );
					var panelWrapperParent = $( "<div class='panel panel-default ' />" ).appendTo( content );
					var panelWrapper = $( "<div class='panel-body' />" ).appendTo( panelWrapperParent );

					// select - view
					var selectViewDiv = $( "<div class='col-sm-12' />" ).appendTo( panelWrapper );
					selectViewDiv.append( "<h4>Change View<h4>" );
					//					$( '<label class="col-sm-3" for="selV">Selecet a view</label>' ).appendTo( selectViewDiv );
					var select_view = $( '<select id="selV" name="view" type="select" class="form-control">' )
							.appendTo( selectViewDiv );
					var possibleViews = Object.keys( GRAVIS3D.Views );
					for ( var i = 0; i < possibleViews.length; i++ ) {
						if ( possibleViews[i] != "Defaults" ) {
							$( '<option value="' + i.toString() + '">' + possibleViews[i] + '</option>' ).appendTo(
									select_view );
						}
					}
					var changeVBtn = $( '<br><button type="button" class="btn app_apply_btn btn-primary">Ok</button>' )
							.appendTo( selectViewDiv );
					changeVBtn.on( "click enter", function() {
						// disable all apply btns
						$( '.app_apply_btn' ).prop( "disabled", true );
						var btnTmp = changeVBtn.html();
						changeVBtn.html( "Loading..." );
						setTimeout( function() {
							instance.viewBuilder = GRAVIS3D.Views[possibleViews[parseInt( select_view.val() )]];
							instance.execute( "visual_representation");
							self.state.normal();
							changeVBtn.html( btnTmp );
							$( '.app_apply_btn' ).prop( "disabled", false );
						}, 10 );
					} );

					panelWrapper.append( "<div class='clearfix' /><hr/>" );

					// select - layout
					var selectLayoutDiv = $( "<div class='col-sm-12' />" ).appendTo( panelWrapper );
					selectLayoutDiv.append( "<h4>Change Layout<h4>" );
					//					$( '<label class="col-sm-3" for="selL">Graph Layout</label>' ).appendTo( selectLayoutDiv );

					var select_layout = $( '<select id="selL" name="layout" type="select" class="form-control">' )
							.appendTo( selectLayoutDiv );
					var possibleLayouts = Object.keys( GRAVIS3D.Layout.GraphLayoutBuilder );
					for ( var i = 0; i < possibleLayouts.length; i++ ) {
						$( '<option value="' + i.toString() + '">' + possibleLayouts[i] + '</option>' ).appendTo(
								select_layout );
					}
					//					$( '<label class="col-sm-3" for="selL2">Dynamic Graph Layout</label>' ).appendTo( selectLayoutDiv );
					//					var select_layout2 = $( '<select id="selL2" name="graph" type="select" class="form-control">' )
					//					.appendTo(
					//							selectLayoutDiv );
					//					var possibleLayouts2 = Object.keys( GRAVIS3D.Layout.DynamicGraphLayoutBuilder );
					//					for ( var i = 0; i < possibleLayouts.length; i++ ) {
					//						$( '<option value="' + i.toString() + '">' + possibleLayouts2[i] + '</option>' ).appendTo(
					//							
					//								select_layout2 );
					//					}
					var changeLBtn = $( '<br><button type="button" class="btn app_apply_btn btn-primary">Ok</button>' )
							.appendTo( selectLayoutDiv );
					changeLBtn
							.on(
									"click enter",
									function() {
										$( '.app_apply_btn' ).prop( "disabled", true );
										var btnTmp = changeLBtn.html();
										changeLBtn.html( "Loading..." );
										setTimeout(
												function() {
													instance.layouter = GRAVIS3D.Layout.GraphLayoutBuilder[possibleLayouts[parseInt( select_layout
															.val() )]];
													//						instance.superLayouter  = GRAVIS3D.Layout.DynamicGraphLayoutBuilder[possibleLayouts2[parseInt(select_layout2.val())]];
													instance.execute( "layout" );
													self.state.normal();
													changeLBtn.html( btnTmp );
													$( '.app_apply_btn' ).prop( "disabled", false );
												}, 10 );
									} );

					panelWrapper.append( "<div class='clearfix' />" );

					// - labels
					//					panelWrapper.append( "<div>TODO label controls</div>" );

					return content;
				}
			},
			filtering : {
				btn : function() {
					return $( '<button type="button" class="btn btn-primary btn-lg"><span class="glyphicon glyphicon-filter"></span></button>' );
				},
				content : function() {
					var content = $( "<div />" );
					// make sure the specified content exists if not (due error) return null 
					try {
						content.append( '<div class=""><h2>Filtering</h2></div>' );
						content.append( "<hr />" );
						content.append( instance.getFilterManager().getGUI().getDomElement() );
					} catch ( err ) {
						return null;
					}
					return content;
				}
			},
			mapping : {
				btn : function() {
					return $( '<button type="button" class="btn btn-primary btn-lg"><span class="glyphicon glyphicon-eye-open"></span></button>' );
				},
				content : function() {
					var content = $( "<div />" );
					// make sure the specified content exists if not (due error) return null 
					try {
						content.append( '<div class=""><h2>Mapping</h2></div>' );
						content.append( "<hr />" );
						content.append( instance.getMappingManager().getGUI().getDomElement() );
					} catch ( err ) {
						return null;
					}
					return content;
				}
			},
			own : {
				btn : function() {
					return $( '<button type="button" class="app_apply_btn btn btn-info btn-lg"><span class="glyphicon glyphicon-th-list"></span></button>' );
				},
				content : function() {
					return toAppend();
				}
			}
		};

		// build all - except the ones to hide..
		for ( name in controlElements ) {
			if ( toHide.indexOf( name ) == -1 ) {
				addControlElement( name );
			}
		}

		// add the dom elements if not null
		function addControlElement( name ) {
			// check if the element is defined
			if ( controlElements[name] == undefined ) throw new Error( "App: GUI: the specified control element ("
					+ JSON.stringify( name ) + ") does not exist." );

			// append to gui 
			if ( controlElements[name].content() != null ) {
				// MENU
				var btn = controlElements[name].btn();
				btn.on( "click enter", function() {
					self.state.inControls( name );
				} );
				//				var t = null;
				//				btn.on( "mouseover", function() {
				//					t = setTimeout( function() {
				//						btn.trigger( "click" );
				//					}, GRAVIS3D.GUI.Defaults.tabs.clickTriggerDelay );
				//				} );
				//				btn.on( "mouseout", function() {
				//					clearInterval( t );
				//				} );
				// PANEL
				var content = controlElements[name].content();
				content.addClass( "controls_" + name );

				controlMenuEl.append( btn );

				controlPanelElContent.append( content );
			}

		}

		controlMenuEl.append( '<div class="clearfix"/>' );

	}

	//////////////////////////////

	// build on init...
	var row1 = $( '<div class="" />' ).appendTo( domElement );
	row1.append( controlMenuEl.hide() );

	var row2 = $( '<div class="" />' ).appendTo( domElement );
	row2.append( viewEl.hide() );
	row2.append( controlPanelEl.hide() );

	//	console.log(instance.targetDomElement)
	//	instance.targetDomElement.append( domElement );
	//
	//	// set state
	this.state.normal();
	//	this.update();
};


GRAVIS3D.App.GUI = function( instanceManager ) {

	////////////////

	var _instanceManager = instanceManager; // is set with passing this to a instanceManager

	var domElement = GRAVIS3D.App.Defaults.GUI.targetDomElement;
	var tabs = $(
			'<ul id="tabs" class="nav nav-tabs" role="tablist"></ul><div id="tabcontent" class="tab-content"></div>' )
			.appendTo( domElement );
	var addInstance = function( instance ) {
		var id = instance.getId();
		var tabname = id;
		if ( instance.getName() != "" ) {
			tabname = instance.getView().getName();
		}

		var tab = $( '<li id="tab_' + id + '"></li>' );
		tab.on( "click", function() {
			var x = this.id.split( "_" )[1];
			_instanceManager.setInstanceActive( x );
		} );
		tab.on( 'shown.bs.tab', function( e ) {
			$( tabPane ).find( "canvas" ).height( ( $( window ).height() - $( tabs ).height() ) );
		} );
		var tabInner = $( '<a href="#tabPane_' + id + '" role="tab" data-toggle="tab">' + tabname + '&emsp;</a>' );

		var button_duplicate = $( '<button id="btnDuplicate_'
				+ id
				+ '" type="button" name="duplicate" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-export"></span></button>' );
		button_duplicate.on( "click", function() {
			var x = this.id.split( "_" )[1];
			var self = $( this );
			self.prop( 'disabled', true );
			setTimeout( function() {
				_instanceManager.duplicateInstanceById( x );
				self.prop( 'disabled', false );
			}, 200 );
		} );

		var button_remove = $( '<button id="btnRemove_'
				+ id
				+ '" type="button" name="remove" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-remove"></span></button>' );
		button_remove.on( "click", function() {
			var x = this.id.split( "_" )[1];
			_instanceManager.removeInstanceById( x );
		} );
		tabInner.append( button_duplicate );
		tabInner.append( button_remove );
		tab.append( tabInner );

		var tabPane = $( "<div class='tab-pane' id='tabPane_" + id + "'></div>" );
		tabPane.append( instance.getGUI().getDomElement() );
		$( "#tabs" ).append( tab );
		$( "#tabcontent" ).append( tabPane );
		$( '#tabs a:last' ).tab( 'show' );

		//		function checkTabMenuHeight(){
		//			var h = $(tabs).height() + "px";
		//			$("#tabcontent").css({top: h});
		//		}
		//		$(window).on("resize", checkTabMenuHeight);
		//		checkTabMenuHeight();
	};

	var removeInstance = function( instanceId ) {
		var active = $( "#tab_" + instanceId ).hasClass( "active" );

		$( "#tab_" + instanceId ).remove();
		$( "#tabPane_" + instanceId ).remove();

		if ( active ) $( '#tabs a:last' ).tab( 'show' );
	};

	////////////////

	this.addInstance = function( instance ) {
		addInstance( instance );
	};
	this.removeInstanceById = function( instanceId ) {
		removeInstance( instanceId );
	};

	this.enableButton = function( buttonName, instanceId ) {
		$( "#btn" + buttonName + "_" + instanceId ).prop( "disabled", false );
	};
	this.disableButton = function( buttonName, instanceId ) {
		$( "#btn" + buttonName + "_" + instanceId ).prop( "disabled", true );
	};

};

GRAVIS3D.App.InstanceManager = function() {

	var gui = new GRAVIS3D.App.GUI( this );

	////////////////

	var instances = {};

	function rmv( instanceId ) {
		delete instances[instanceId];
		gui.removeInstanceById( instanceId );
	}
	function add( instance, isCopied ) {
		instances[instance.getId()] = instance;
		gui.addInstance( instance );
		if (isCopied == true) instance.execute("visual_representation");
		else instance.execute()
		//		instance.getView().getDomElement().height( "100%");
		//		instance.getView().render();
	}
	function duplicate( instanceId ) {
		var instance = instances[instanceId];
		instance.getView().pauseRendering();
		var copy = instance.copy();
		//		copy.setId( GRAVIS3D.ID.get() );
		add( copy, true );

	}
	function activate( instanceId ) {
		for ( id in instances ) {
			if ( id == instanceId ) {
				instances[id].getView().render();
			} else instances[id].getView().pauseRendering();
		}
	}

	////////////////

	this.addInstance = function( instance ) {
		add( instance );
	};
	this.removeInstanceById = function( instanceId ) {
		rmv( instanceId );
	};
	this.duplicateInstanceById = function( instanceId ) {
		duplicate( instanceId );
	};
	this.setInstanceActive = function( instanceId ) {
		activate( instanceId );
	};

};

GRAVIS3D.App.LoadScreen = function( params, callback ) {

	GRAVIS3D.InfoObject.call( this );

	var target = null;
	if ( params && params.targetDomElement ) target = params.targetDomElement;
	else target = GRAVIS3D.App.Defaults.WaitScreen.targetDomElement;

	var domElement = function() {
		var div = $( '<div class="loadScreen panel panel-body " style="display:none" ></div>' );
		var content = div.append( '<div class="loadScreen_content"></div>' );
		//		content.append( '<div class="planeAnimation loadScreen_g3d">GRAVIS3D</div>' );
		content.append( '<div class="planeAnimation loadScreen_g3d">GRAVIS3D</div>' );
		//		content.append( '<span class="loadScreen_icon glyphicon glyphicon-plane plane"></span>' );
		content.append( '<span class="loadScreen_icon glyphicon glyphicon-refresh"></span>' );
		//		content.append( '<hr><hr>' );
		content.append( '<div class="loadScreen_title"></div>' );
		//		content.append( '<hr><hr>' );
		//		content.append( '<div class="loadScreen_message"></div>' );
		var dots = $( '<div class="loadScreen_dots">' );
		dots.append( '<span class="loadScreen_dot dot1 dot_anim1">.</span>' );
		dots.append( '<span class="loadScreen_dot dot2 dot_anim2">.</span>' );
		dots.append( '<span class="loadScreen_dot dot3 dot_anim3">.</span>' );
		content.append( dots );
		div.append( content );
		return div;
	};

	target.append( domElement() );

	function startAnimation() {
		//		target.find( '.dot' ).addClass( "dot_anim" );
		//		target.find( '.plane' ).addClass( "planeAnimation" );
	}
	function stopAnimation() {
		//		target.find( '.dot' ).removeClass( "dot_anim" );
		//		target.find( '.plane' ).removeClass( "planeAnimation" );
	}

	this.show = function() {
		startAnimation();
		target.find( '.loadScreen' ).show();
	};
	this.exit = function() {
		stopAnimation();
		target.find( '.loadScreen' ).hide();
	};
	this.destroy = function() {
		target.find( '.loadScreen' ).remove();
	};
	this.msg = function( string, msg_callback ) {
		target.find( ".loadScreen_title" ).text( string );
		this.show();
		setTimeout( function() {
			if ( msg_callback != null ) msg_callback();
		}, 1 );
	};
	this.title = function( string ) {
		target.find( ".loadScreen_message" ).text( string );
		this.show();
	};

	var msg = null;
	if ( params && params.msg ) msg = params.msg;
	else msg = GRAVIS3D.App.Defaults.WaitScreen.msg;
	this.msg( msg );

	var title = null;
	if ( params && params.title ) title = params.title;
	else title = GRAVIS3D.App.Defaults.WaitScreen.title;
	this.title( title );

	/////////////////

	this.show();
	setTimeout( function() {
		if ( callback != null ) callback();
	}, 10 );
};

var LOADSCREEN;
var CONTROLLER;

var start = function() {

	LOADSCREEN = new GRAVIS3D.App.LoadScreen();
	CONTROLLER = new GRAVIS3D.App.InstanceManager();
	LOADSCREEN.msg( "Reading Data", function() {
		console.log( "########################################" );
		console.log( "GRAVIS3D.App.Instance: #build - model..." );
		var t_0 = new Date();
		GRAVIS3D.IO.Importers.SISOB( INPUT, function( graph ) {
			var i = new GRAVIS3D.App.Instance( {
				instanceManager : CONTROLLER,
				graph : graph,
			} );
			console.log( "===> " + ( new Date() - t_0 ) + " ms <===" );
			setTimeout( function() {
				CONTROLLER.addInstance( i );
			}, 1 );
		} );
	} );


}();
