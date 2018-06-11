/**
 * @author Henrik Detjen
 */

/**
 * Holds all default settings
 * 
 * @public
 * @static
 * @constant
 */
GRAVIS3D.Layout.Defaults = {};



/**
 * TODO comments
 */
GRAVIS3D.Layout.GraphLayout = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	var positions = {}; // { nodeModelId: Vector3D }
	this.addPosition = function( nodeModelId, vector3d ) {
		if ( vector3d instanceof GRAVIS3D.Vector3D ) {
			positions[nodeModelId] = vector3d;
			updateMinMax( vector3d );
		} else {
			throw new Error( "Layout: No proper Node or Vector3D added." );
		}
	};
	this.getPosition = function( nodeId ) {
		if ( positions[nodeId] ) {
			return positions[nodeId];
		} else {
			return null;
		}
	};
	this.getPositions = function() {
		return positions;
	};
	this.removePosition = function( nodeId ) {
		if ( positions[nodeId] ) delete positions[nodeId];
	};

	var xmin = null;
	var xmax = null;
	var ymin = null;
	var ymax = null;
	var zmin = null;
	var zmax = null;

	this.getMinX = function() {
		if ( xmin == null ) return 0;
		return xmin;
	};
	this.setMinX = function( number ) {
		xmin = number;
	};
	this.getMaxX = function() {
		if ( xmax == null ) return 0;
		return xmax;
	};
	this.setMaxX = function( number ) {
		xmax = number;
	};
	this.getMinY = function() {
		if ( ymin == null ) return 0;
		return ymin;
	};
	this.setMinY = function( number ) {
		ymin = number;
	};
	this.getMaxY = function() {
		if ( ymax == null ) return 0;
		return ymax;
	};
	this.setMaxY = function( number ) {
		ymax = number;
	};
	this.getMinZ = function() {
		if ( zmin == null ) return 0;
		return zmin;
	};
	this.setMinZ = function( number ) {
		zmin = number;
	};
	this.getMaxZ = function() {
		if ( zmax == null ) return 0;
		return zmax;
	};
	this.setMaxZ = function( number ) {
		zmax = number;
	};

	this.getWidth = function() {
		return this.getMaxX() - this.getMinX();
	};
	this.getHeight = function() {
		return this.getMaxY() - this.getMinY();
	};
	this.getDepth = function() {
		return this.getMaxZ() - this.getMinZ();
	};

	this.getBoundingBox = function() {
		var start = new GRAVIS3D.Vector3D( this.getMinX(), this.getMinY(), this.getMinZ() );
		return new GRAVIS3D.Box( start, this.getWidth(), this.getHeight(), this.getDepth() );
	};

	this.getAvgX = function() {
		var avg = 0;
		var posCount = Object.keys( positions ).length;
		for ( id in positions ) {
			var p = positions[id].getX();
			avg += ( p / posCount );
		}
		return avg;
	};
	this.getAvgY = function() {
		var avg = 0;
		var posCount = Object.keys( positions ).length;
		for ( id in positions ) {
			var p = positions[id].getY();
			avg += ( p / posCount );
		}
		return avg * avg;
	};
	this.getAvgZ = function() {
		var avg = 0;
		var posCount = Object.keys( positions ).length;
		for ( id in positions ) {
			var p = positions[id].getZ();
			avg += ( p / posCount );
		}
		return avg;
	};

	function updateMinMax( vector3d ) {
		var x = vector3d.getX();
		var y = vector3d.getY();
		var z = vector3d.getZ();
		if ( xmin == null ) xmin = x;
		if ( xmax == null ) xmax = x;
		if ( ymin == null ) ymin = y;
		if ( ymax == null ) ymax = y;
		if ( zmin == null ) zmin = z;
		if ( zmax == null ) zmax = z;
		if ( x < xmin ) xmin = x;
		if ( x > xmax ) xmax = x;
		if ( y < ymin ) ymin = y;
		if ( y > ymax ) ymax = y;
		if ( z < zmin ) zmin = z;
		if ( z > zmax ) zmax = z;
	}
	function fSgn( number ) {
		if ( number >= 0 ) return 1;
		if ( number < 0 ) return -1;
	}

	/**
	 * Set all positions for node/edge representations
	 * 
	 * @method applyToVisualRepresentation
	 * @param {@link GRAVIS3D.VisualRepresentation.Graph} visRep
	 */
	this.applyToVisualRepresentation = function( visRep ) {
		if ( visRep instanceof GRAVIS3D.VisualRepresentation.Graph ) {
			for ( nodeModelId in positions ) {
				try {
					visRep.getNodeRepresentationByNodeModelId( nodeModelId ).setPosition( positions[nodeModelId] );
				} catch ( err ) {
					//					console.log( err.message );
				}
			}
			var edgeRepresentations = visRep.getEdgeRepresentations();
			for ( id in edgeRepresentations ) {
				edgeRepresentations[id].setPosition_From( edgeRepresentations[id].getSource().getPosition() );
				edgeRepresentations[id].setPosition_To( edgeRepresentations[id].getTarget().getPosition() );
			}
		} else {
			throw new Error(
					"Layout: "
							+ this.getId()
							+ ": #applyToVisualRepresentation: param seems to be wrong.. must be Visual Representation - Graph... "
							+ JSON.stringify( visRep ) );
		}
	};

	if ( params && params.id ) this.setId( params.id );
	if ( params && params.name ) this.setName( params.name );
	if ( params && params.description ) this.setDescription( params.description );

};
