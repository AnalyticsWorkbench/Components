GRAVIS3D.GUI = {};
GRAVIS3D.GUI.Defaults = {
	tabs : {
		clickTriggerDelay : 250
	}
};
/**
 * @summary
 * Create a simple modal with ok/canel button and a spcific message / title
 * 
 * @since 1.0
 * 
 * @constructor ConfirmModal
 * @param {<br>
 * &emsp; 	title: {String},<br>
 * &emsp;	msg: {String},<br>
 * &emsp;	onOk: {Function},<br>
 * &emsp;	onCancel: {Function},<br>
 * $emsp;	$msg: {jquery},<br>
 * $emsp;	$title: {jquery},<br>
 * } params
 */
GRAVIS3D.GUI.ConfirmModal = function( params ) {

	var modal = $( '<div class="modal fade"></div>' );
	var dialog = $( '<div class="modal-dialog"></div>' ).appendTo( modal );
	var content = $( '<div class="modal-content"></div>' ).appendTo( dialog );
	var header = $(
			'<div class="modal-header"> <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button></div>' )
			.appendTo( content );
	var title = "";
	if ( params && params.title ) title = params.title;
	var titleDiv = ( '<h4 class="modal-title">' + title + '</h4>' );
	if ( params && params.$title ) titleDiv = params.$title;
	header.append( titleDiv );
	var body = $( '<div class="modal-body"><div>' ).appendTo( content );
	var msg = "";
	if ( params && params.msg ) msg = params.msg;
	var msgDiv = $( '<p>' + msg + '</p>' );
	if ( params && params.$msg ) msgDiv = params.$msg;
	body.append( msgDiv );
	var footer = $( '<div class="modal-footer"></div>' ).appendTo( content );
	var btn_ok = $( '<button type="button" class="btn btn-primary" data-dismiss="modal">Ok</button>' )
			.appendTo( footer );
	btn_ok.click( function() {
		if ( params && params.onOk ) params.onOk();
	} );
	var btn_cancel = $( '<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>' )
			.appendTo( footer );
	btn_cancel.click( function() {
		if ( params && params.onCancel ) params.onCancel();
	} );
	modal.modal();
};

/**
 * @summary
 * Create a simple modal with ok/canel button and a spcific message / title
 * 
 * @since 1.0
 * 
 * @constructor ConfirmModal
 * @param {<br>
 * &emsp; 	title: {String},<br>
 * &emsp;	msg: {String},<br>
 * &emsp;	onOk: {Function},<br>
 * $emsp;	$msg: {jquery},<br>
 * $emsp;	$title: {jquery},<br>
 * } params
 */
GRAVIS3D.GUI.PromptModal = function( params ) {

	var modal = $( '<div class="modal fade"></div>' );
	var dialog = $( '<div class="modal-dialog"></div>' ).appendTo( modal );
	var content = $( '<div class="modal-content"></div>' ).appendTo( dialog );
	var header = $(
			'<div class="modal-header"> <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button></div>' )
			.appendTo( content );
	var title = "";
	if ( params && params.title ) title = params.title;
	var titleDiv = ( '<h4 class="modal-title">' + title + '</h4>' );
	if ( params && params.$title ) titleDiv = params.$title;
	header.append( titleDiv );
	var body = $( '<div class="modal-body"><div>' ).appendTo( content );
	var msg = "";
	if ( params && params.msg ) msg = params.msg;
	var msgDiv = $( '<p>' + msg + '</p>' );
	if ( params && params.$msg ) msgDiv = params.$msg;
	body.append( msgDiv );
	var footer = $( '<div class="modal-footer"></div>' ).appendTo( content );
	var btn_ok = $( '<button type="button" class="btn btn-primary" data-dismiss="modal">Ok</button>' )
			.appendTo( footer );
	btn_ok.on( "click enter", function() {
		if ( params && params.onOk ) params.onOk();
	} );
	modal.modal();
};

/**
 * @summary add a bootstrap tooltip markup to target element
 * @since 1.0
 * @method addTooltip
 * @param {jquery} $el
 */
GRAVIS3D.GUI.addTooltip = function( $el, text ) {
	$el.tooltip( {
		delay : {
			"show" : 500,
			"hide" : 100
		},
		placement : 'right',
		title : text
	} );
};

GRAVIS3D.GUI.detailedNodeTooltip = function( modelId, dynGraph, x, y ) {

	var el_id = GRAVIS3D.ID.get();
	var div = $(
			'<div id="' + el_id
					+ '" class="node_tooltip_detailed col-sm-6 alert alert alert-dismissible" role="alert" />' )
			.appendTo( 'body' );
	div
			.append( '<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>' );


	var graphs = dynGraph.getOrderedGraphs();
	var slices = [];
	for ( var j = 0; j < graphs.length; j++ ) {
		if ( graphs[j].getNodeById( modelId ) ) {
			slices.push( j );
		}
	}

	var content = $( '<div class="ts_view_tooltip_content" />' ).appendTo( div );
	content.append( "<strong>" + modelId + "</strong><div>Appears in " + slices.length + " of " + graphs.length
			+ " slices (" + slices + ").</div><hr /><div>Attributes:</div>" );

	var node_attrs = dynGraph.getAttributes( "node" );
	var colors = d3.scale.category10();
	colors.domain( Object.keys( node_attrs ) );
	var nodeAttrs_data = [];
	var counter = 0;
	var drawLineChart = false;
	for ( id in node_attrs ) {
		nodeAttrs_data[counter] = {
			v : [], // org values
			f : [], // formatted (rounded, trimmed  values only number type)
			n : []
		// normalized values (only number type)
		};
		for ( var j = 0; j < graphs.length; j++ ) {
			if ( graphs[j].getNodeById( modelId ) ) {
				var val = graphs[j].getNodeById( modelId ).getAttributeValue( id );
				if ( val == undefined && val != 0 ) val = "";
				// add org value
				nodeAttrs_data[counter].v[j] = val;
				if ( node_attrs[id].getType() == "number" ) {
					// format numbers and add to f
					nodeAttrs_data[counter].f[j] = d3.round( val, 3 );
					// normalize number and add to n
					var sliceMax = graphs[j].getAttributes( "node" )[id].getRange().getMax();
					if ( sliceMax != 0 ) {
						nodeAttrs_data[counter].n[j] = val / sliceMax;
					} else {
						nodeAttrs_data[counter].n[j] = val;
					}
					// draw values  
					drawLineChart = true;
				} else {
					// remove spaces at begin/end of string, add to f
					nodeAttrs_data[counter].f[j] = val.trim();
				}
			} else {
				nodeAttrs_data[counter].v[j] = "";
				nodeAttrs_data[counter].f[j] = "";
				nodeAttrs_data[counter].n[j] = "";
			}
		}
		if ( id == "id" ) {
			nodeAttrs_data[counter].f = [];
			nodeAttrs_data[counter].f[0] = modelId;
		}
		content.append( "<div><span style='color:" + colors( id ) + ";'><strong>" + id + "</strong></span>:&nbsp;"
				+ nodeAttrs_data[counter].f.toString().split( "," ).join( "; " ) + "</div> " );
		counter++;
	}

	content.append( "<hr /><div>Compared to Slice's Max:</div>" );
	function createAttrValChart( attrs ) {

		// define dimensions of graph
		var m = [ 20, 20, 20, 20 ]; // margins
		var w = ( graphs.length * 30 ) - m[1] - m[3]; // width
		var h = 100 - m[0] - m[2]; // height

		// X scale will fit all values from data[] within pixels 0-w
		var x = d3.scale.linear().domain( [ 0, ( graphs.length - 1 ) ] ).range( [ 0, w ] );
		// Y scale will fit values from 0-10 within pixels h-0 (Note the inverted domain for the y-scale: bigger is up!)
		var y = d3.scale.linear().domain( [ 0, 1 ] ).range( [ h, 0 ] );

		var line = d3.svg.line()
		// assign the X function to plot our line as we wish
		.x( function( d, i ) {
			//					console.log( 'Plotting X value for data point: ' + d + ' using index: ' + i + ' to be at: ' + x( i )
			//							+ ' using our xScale.' );
			// return the X coordinate where we want to plot this datapoint
			return x( i );
		} ).y( function( d ) { // verbose logging to show what's actually being done
			//			console.log( 'Plotting Y value for data point: ' + d + ' to be at: ' + y( d ) + " using our yScale." );
			// return the Y coordinate where we want to plot this datapoint
			return y( d );
		} );

		// Add an SVG element with the desired dimensions and margin.
		var graph = d3.select( ( "#" + el_id ) ).append( "svg:svg" ).attr( "width", w + m[1] + m[3] ).attr( "height",
				h + m[0] + m[2] ).append( "svg:g" ).attr( "transform", "translate(" + m[3] + "," + m[0] + ")" );

		// create yAxis
		var xAxis = d3.svg.axis().scale( x ).tickSize( -h ).tickSubdivide( true );
		// Add the x-axis.
		graph.append( "svg:g" ).attr( "class", "x axis" ).attr( "transform", "translate(0," + h + ")" ).call( xAxis );


		// create left yAxis
		var yAxisLeft = d3.svg.axis().scale( y ).ticks( 4 ).orient( "left" );
		// Add the y-axis to the left
		graph.append( "svg:g" ).attr( "class", "y axis" ).attr( "transform", "translate(-25,0)" ).call( yAxisLeft );

		// Add the line by appending an svg:path element with the data line we created above
		// do this AFTER the axes above so that the line is above the tick-lines
		var counter = 0;
		for ( id in attrs ) {
			if ( attrs[id].getType() == "number" ) {
				var data = nodeAttrs_data[counter].n;
				graph.append( "svg:path" ).attr( "d", line( data ) ).style( "stroke", colors( id ) );
			}
			counter++;
		}

	}
	//check for number attribute and draw table if one is found
	if ( drawLineChart == true ) createAttrValChart( node_attrs );
	// set inital position to mouse (if passed)
	if ( x && y ) {
		if ( ( x + $( "#" + el_id ).width() + 60 ) > window.innerWidth ) {
			x = ( window.innerWidth - $( "#" + el_id ).width() - 60 );
		}
		if ( y + $( "#" + el_id ).height() + 60 > window.innerHeight ) {
			y = window.innerHeight - $( "#" + el_id ).height() - 60;
			if ( y < 0 ) y = 0;
		}
		$( "#" + el_id ).css( {
			top : y,
			left : x
		} );
	}
	// make it draggable!
	$( "#" + el_id ).drags();
};

GRAVIS3D.GUI.simpleNodeTooltip = function( modelId, x, y ) {

	var el_id = GRAVIS3D.ID.get();
	var div = $( '<div id="' + el_id + '" class="node_tooltip_simple alert " role="alert" />' ).appendTo( 'body' );
	var content = $( '<div class="ts_view_tooltip_content" />' ).appendTo( div );
	content.append( "<strong>" + modelId + "</strong></div>" );

	div.on( "mouseover", function() {
		div.remove();
	} );

	// set inital position to mouse (if passed)
	if ( x && y ) {
		if ( ( x + $( "#" + el_id ).width() + 60 ) > window.innerWidth ) {
			x = ( window.innerWidth - $( "#" + el_id ).width() - 60 );
		}
		if ( y + $( "#" + el_id ).height() + 60 > window.innerHeight ) {
			y = window.innerHeight - $( "#" + el_id ).height() - 60;
			if ( y < 0 ) y = 0;
		}
		$( "#" + el_id ).css( {
			top : y,
			left : x
		} );
	}
};

GRAVIS3D.GUI.simpleEdgeTooltip = function( modelId, x, y ) {

	var el_id = GRAVIS3D.ID.get();
	var div = $( '<div id="' + el_id + '" class="edge_tooltip_simple alert " role="alert" />' ).appendTo( 'body' );
	var content = $( '<div class="ts_view_tooltip_content" />' ).appendTo( div );
	content.append( "<strong>" + modelId + "</strong></div>" );

	div.on( "mouseover", function() {
		div.remove();
	} );

	// set inital position to mouse (if passed)
	if ( x && y ) {
		if ( ( x + $( "#" + el_id ).width() + 60 ) > window.innerWidth ) {
			x = ( window.innerWidth - $( "#" + el_id ).width() - 60 );
		}
		if ( y + $( "#" + el_id ).height() + 60 > window.innerHeight ) {
			y = window.innerHeight - $( "#" + el_id ).height() - 60;
			if ( y < 0 ) y = 0;
		}
		$( "#" + el_id ).css( {
			top : y,
			left : x
		} );
	}
};

GRAVIS3D.GUI.simpleSliceTooltip = function( modelId, x, y ) {

	var el_id = GRAVIS3D.ID.get();
	var div = $( '<div id="' + el_id + '" class="slice_tooltip_simple " role="alert" />' ).appendTo( 'body' );
	var content = $( '<div class="ts_view_tooltip_content" />' ).appendTo( div );
	content.append( "<strong>" + modelId + "</strong></div>" );

	div.on( "mouseover", function() {
		div.remove();
	} );

	// set inital position to mouse (if passed)
	if ( x && y ) {
		x = x - div.width() / 2;
		if ( ( x + $( "#" + el_id ).width() + 60 ) > window.innerWidth ) {
			x = ( window.innerWidth - $( "#" + el_id ).width() - 60 );
		}
		if ( y + $( "#" + el_id ).height() + 60 > window.innerHeight ) {
			y = window.innerHeight - $( "#" + el_id ).height() - 60;
			if ( y < 0 ) y = 0;
		}
		$( "#" + el_id ).css( {
			top : y,
			left : x
		} );
	}
};


function firstLetterUp( string ) {
	var result;
	try {
		result = string.charAt( 0 ).toUpperCase() + string.substr( 1, string.length );
	} catch ( err ) {
		return string;
	}
	return result;
}
