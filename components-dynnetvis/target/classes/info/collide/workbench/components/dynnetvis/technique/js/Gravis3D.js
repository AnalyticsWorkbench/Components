/**
 * @author Henrik Detjen
 * @copyright Henrik Detjen, 2014 
 * @license MIT-License
 * @version 1.0
 * 
 * @namespace Framework Container
 * @nameSpace
 */
var GRAVIS3D = {};
/**
 * containter for all input / output related things
 * @nameSpace IO
 * @namespace IO
 */
GRAVIS3D.IO = {};
/**
 * containter for all layout related things
 * @nameSpace Model
 * @namespace Model
 */
GRAVIS3D.Model = {};
/**
 * containter for all layout related things
 * @nameSpace Layout
 * @namespace Layout
 */
GRAVIS3D.Layout = {};

/**
 * @nameSpace VisualRepresentation
 * @namespace VisualRepresentation
 */
GRAVIS3D.VisualRepresentation = {};
/**
 * @nameSpace Views
 * @namespace Views
 * The different view modules, the visualization "visual representation" of a graph are defined here
 * 
 * if you write new views please make sure you implement following methods (even if you do not need them):
 * 
 * yourView.render() 
 * yourView.pauseRendering() 
 * yourView.getVisualRepresentation() 
 * yourView.setVisualRepresentation()
 * 
 */
GRAVIS3D.Views = {};


//////////////////////////////
// Helper Classes & Methods //
//////////////////////////////

/**
 * Helper for inheritance between "classes"
 * 
 * @param Super - the Superclass
 * @param Sub - the Subclass
 */
function inheritPseudoClass( Super, Sub ) {
	Sub.prototype = Object.create( Super.prototype );
	Sub.prototype.constructor = Sub;
}
/**
 * Helper for deep copy of a object
 * @param {Object} obj
 */
function deepCopy( obj ) {
	var copy = jQuery.extend( true, {}, obj );
	copy.__proto__ = obj.__proto__;
	return copy;
}

/**
 * A simple Id Generator
 * 
 * Produces unique ids through a running number plus random chars. This is sufficient in this context and it is much
 * faster than a UUID. If needed, the algorithm can be changed to have a broader range of ids.
 */
GRAVIS3D.ID = {

	runningNumber : 0,
	maxNumber : 100000000,
	possibleChars : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",

	rndChars : function() {
		var text = "";
		for ( var i = 0; i < 4; i++ )
			text += this.possibleChars.charAt( Math.floor( Math.random() * this.possibleChars.length ) );
		return text;
	},

	get : function() {
		if ( this.runningNumber == this.maxNumber ) {
			this.runningNumber = 0;
		}
		return "id-" + this.rndChars() + "-" + this.rndChars() + "-" + this.runningNumber++;
	}

};

/**
 * A simple Basic Object for inheritance of default informations
 */
GRAVIS3D.InfoObject = function() {

	var _id = GRAVIS3D.ID.get();
	this.getId = function() {
		return _id;
	};
	this.setId = function( id ) {
		if ( typeof id != "string" ) id = JSON.stringify( id );
		_id = id;
	};
	var _name = "";
	this.getName = function() {
		return _name;
	};
	this.setName = function( name ) {
		if ( typeof name == "string" ) _name = name;
	};
	var _description = "";
	this.getDescription = function() {
		return _description;
	};
	this.setDescription = function( description ) {
		if ( typeof description == "string" ) _description = description;
	};
	var data = {}; // { someKey: aValue }
	this.getData = function( key ) {
		return data[key];
	};
	this.setData = function( key, value ) {
		data[key] = value;
	};
};

/**
 * A basic containers for positional informations in 3D space
 */
GRAVIS3D.Vector3D = function( x, y, z ) {

	var _x = 0;
	this.setX = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_x = number;
		} else {
			throw new Error( "Vector3D: no number passed... (" + JSON.stringify(number) + ")" );
		}
	};
	this.getX = function() {
		return _x;
	};
	if ( x != null ) this.setX( x );

	var _y = 0;
	this.setY = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_y = number;
		} else {
			throw new Error( "Vector3D: no number passed... (" + JSON.stringify(number) + ")" );
		}
	};
	this.getY = function() {
		return _y;
	};
	if ( y != null ) this.setY( y );

	var _z = 0;
	this.setZ = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_z = number;
		} else {
			throw new Error( "Vector3D: no number passed... (" + JSON.stringify(number) + ")" );
		}
	};
	this.getZ = function() {
		return _z;
	};
	if ( z != null ) this.setZ( z );

};
/**
 * A basic containers for positional informations in 3D space
 */
GRAVIS3D.Vector2D = function( x, y ) {

	var _x = 0;
	this.setX = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_x = number;
		} else {
			throw new Error( "Vector2D: no number passed... (" + JSON.stringify(number) + ")" );
		}
	};
	this.getX = function() {
		return _x;
	};
	if ( x != null ) this.setX( x );

	var _y = 0;
	this.setY = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_y = number;
		} else {
			throw new Error( "Vector2D: no number passed... (" + JSON.stringify(number) + ")" );
		}
	};
	this.getY = function() {
		return _y;
	};
	if ( y != null ) this.setY( y );

};
GRAVIS3D.Rectangle = function( startPoint_vector2d, width, height ) {

	// START
	var start = new GRAVIS3D.Vector2D( 0, 0 );
	this.getStartPoint = function() {
		return start;
	};
	this.setStartPoint = function( vector2d ) {
		if ( vector2d instanceof GRAVIS3D.Vector2D ) {
			start = vector2d;
		} else {
			throw new Error( "Rectangle: no vector passed." );
		}
	};

	// WIDTH
	var _width = 0;
	this.getWidth = function() {
		return _width;
	};
	this.setWidth = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_width = number;
		} else {
			throw new Error( "Rectangele: width must be a number." );
		}
	};

	// HEIGHT
	var _height = 0;
	this.getHeight = function() {
		return _height;
	};
	this.setHeight = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_height = number;
		} else {
			throw new Error( "Rectangele: height must be a number." );
		}
	};

	// INTERSECTION
	this.intersects = function( rectangle ) {
		if ( rectangle instanceof GRAVIS3D.Rectangle ) {
			var r_min = rectangle.getStartPoint().getX();
			var r_max = r_min + rectangle.getWidth();
			var this_min = this.getStartPoint().getX();
			var this_max = this_min + this.getWidth();
			//x-axis intersection?
			if ( ( this_min >= r_min && this_min <= r_max ) || ( this_max >= r_min && this_max <= r_max ) ) {
				var r_min2 = rectangle.getStartPoint().getY();
				var r_max2 = r_min2 + rectangle.getHeight();
				var this_min2 = this.getStartPoint().getY();
				var this_max2 = this_min2 + this.getHeight();
				//y-axis intersection?
				if ( ( this_min2 >= r_min2 && this_min2 <= r_max2 ) || ( this_max2 >= r_min2 && this_max2 <= r_max2 ) ) { return true; }
			}
			return false;
		} else {
			throw new Error( "Rectangele: intersection param must be a rectangle." );
		}
	};

	this.setStartPoint( startPoint_vector2d );
	this.setHeight( height );
	this.setWidth( width );

};
/**
 * Box
 */
GRAVIS3D.Box = function( startPoint_vector3d, width, height, depth ) {

	// START
	var start = new GRAVIS3D.Vector2D( 0, 0, 0 );
	this.getStartPoint = function() {
		return start;
	};
	this.setStartPoint = function( vector3d ) {
		if ( vector3d instanceof GRAVIS3D.Vector3D ) {
			start = vector3d;
		} else {
			throw new Error( "Box: no vector passed." );
		}
	};

	// WIDTH
	var _width = 0;
	this.getWidth = function() {
		return _width;
	};
	this.setWidth = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_width = number;
		} else {
			throw new Error( "Box: width must be a number." );
		}
	};

	// HEIGHT
	var _height = 0;
	this.getHeight = function() {
		return _height;
	};
	this.setHeight = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_height = number;
		} else {
			throw new Error( "Box: height must be a number." );
		}
	};

	// DEPTH
	var _depth = 0;
	this.setDepth = function() {
		return _depth;
	};
	this.setDepth = function( number ) {
		if ( typeof ( number ) == "number" && !isNaN( number ) ) {
			_depth = number;
		} else {
			throw new Error( "Box: depth must be a number." );
		}
	};

	// INTERSECTION
	this.intersects = function( box ) {
		if ( box instanceof GRAVIS3D.box ) {
			var r_min = box.getStartPoint().getX();
			var r_max = r_min + box.getWidth();
			var this_min = this.getStartPoint().getX();
			var this_max = this_min + this.getWidth();
			//x-axis intersection?
			if ( ( this_min >= r_min && this_min <= r_max ) || ( this_max >= r_min && this_max <= r_max ) ) {
				var r_min2 = box.getStartPoint().getY();
				var r_max2 = r_min2 + box.getHeight();
				var this_min2 = this.getStartPoint().getY();
				var this_max2 = this_min2 + this.getHeight();
				//y-axis intersection?
				if ( ( this_min2 >= r_min2 && this_min2 <= r_max2 ) || ( this_max2 >= r_min2 && this_max2 <= r_max2 ) ) {
					var r_min3 = box.getStartPoint().getZ();
					var r_max3 = r_min3 + box.getDepth();
					var this_min3 = this.getStartPoint().getZ();
					var this_max3 = this_min3 + this.getDepth();
					//y-axis intersection?
					if ( ( this_min3 >= r_min3 && this_min3 <= r_max3 )
							|| ( this_max3 >= r_min3 && this_max3 <= r_max3 ) ) { return true; }
				}
			}
			return false;
		} else {
			throw new Error( "Box: intersection param must be a box." );
		}
	};

	this.setStartPoint( startPoint_vector3d );
	this.setHeight( height );
	this.setWidth( width );
	this.setDepth( depth );

};

/**
 * A Range
 */
GRAVIS3D.Range = function( min, max ) {

	// MIN
	var _min = 0;
	this.getMin = function() {
		return _min;
	};
	this.setMin = function( number ) {
		if ( typeof number == "number" ) {
			_min = number;
		} else {
			throw new Error( "Range: argument must be a number." );
		}
	};

	// MAX
	var _max = 0;
	this.getMax = function() {
		return _max;
	};
	this.setMax = function( number ) {
		if ( typeof number == "number" ) {
			_max = number;
		} else {
			throw new Error( "Range: argument must be a number." );
		}
	};

	if ( min ) this.setMin( min );
	if ( max ) this.setMax( max );

};
