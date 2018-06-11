/**
  * @author Henrik Detjen
 */

////////////////////////////////////////////////////////
////////// The Visual Representation of a Node /////////
////////////////////////////////////////////////////////
/**
 * The default values used by the {@link GRAVIS3D.VisualRepresentation.Edge} constructor
 */
GRAVIS3D.VisualRepresentation.Defaults.edge = {
	color : "#888888",//"#7676ff",
	brightness : 1,
	opacity : 1,
	size : 0.5,
	texture : "default",
	positionFrom : new GRAVIS3D.Vector3D( 0, 0, 0 ),
	positionTo : new GRAVIS3D.Vector3D( 0, 0, 0 ),
	directed : false,
	highlightColor : "#FF55AA",
	/**
	 * list of possible (and valid) texture ids - used by setForm() and GUI
	 */
	possibleTextures : [ "default", "dashed", "dashed_narrow", "dashed_wide", "solid", "random" ]
};

/**
 * @summary
 * An abstract Visual Representation for a Node
 * 
 * @since 1.0
 * 
 * @constructor Edge
 * @param { <br>
 * 		&emsp;source: {@link GRAVIS3D.VisualRepresentation.Node}, <i>*required*</i> <br>
 * 		&emsp;target: {@link GRAVIS3D.VisualRepresentation.Node}, <i>*required*</i> <br>
 * 		&emsp;represents: {@link GRAVIS3D.Model.Edge} <br>
 * 		&emsp;color: {String}, <br>
 * 		&emsp;brightness: {0-1}, <br>
 * 		&emsp;opacity: {0-1}, <br>
 * 		&emsp;size: {0-n}, <br>
 * 		&emsp;texture: {String}, <br>
 * 		&emsp;positionFrom: {@link GRAVIS3D.Vector3D}, <br>
 * 		&emsp;positionTo: {@link GRAVIS3D.Vector3D}, <br>
 * 		&emsp;position: {from:{@link GRAVIS3D.Vector3D}, to: {@link GRAVIS3D.Vector3D}} <br>
 * } params
 * 
 * @example
 * TODO
 * 
 */
GRAVIS3D.VisualRepresentation.Edge = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// SOURCE
	var _source = null; // a vis rep node
	/**
	 * returns the source of this link representation
	 * 
	 * @method getSource
	 * @return {@link GRAVIS3D.VisualRepresentation.Node}
	 */
	this.getSource = function() {
		return _source;
	};
	/**
	 * sets the source of this link representation
	 * 
	 * @method setSource
	 * @param {@link GRAVIS3D.VisualRepresentation.Node} source
	 * @return
	 */
	this.setSource = function( source ) {
		if ( source instanceof GRAVIS3D.VisualRepresentation.Node ) {
			_source = source;
			this.setPosition_From( source.getPosition() );
		} else throw new Error( "Visual Representation - Edge " + this.getId()
				+ " #setSource: param must be a Visual Representation - Node... (param:" + JSON.stringify( source )
				+ ")" );
	};

	// TARGET
	var _target = null; // a vis rep node
	/**
	 * returns the target of this link representation
	 * 
	 * @method getTarget
	 * @return {@link GRAVIS3D.VisualRepresentation.Node}
	 */
	this.getTarget = function() {
		return _target;
	};
	/**
	 * sets the target of this link representation
	 * 
	 * @method setTarget
	 * @param target {@link GRAVIS3D.VisualRepresentation.Node} 
	 */
	this.setTarget = function( target ) {
		if ( target instanceof GRAVIS3D.VisualRepresentation.Node ) {
			_target = target;
			this.setPosition_To( target.getPosition() );
		} else throw new Error( "Visual Representation: Edge " + this.getId()
				+ " #setTarget: param must be a Visual Representation - Node... (param:" + JSON.stringify( target )
				+ ")" );
	};


	// MODEL LINK
	var represents = null;
	/**
	 * gets the corresponding edge model (for quick access to meta data in the views)
	 *
	 *@method getRepresentedModel
	 *@return {@link GRAVIS3D.Model.Edge}
	 */
	this.getRepresentedModel = function() {
		return represents;
	};
	/**
	 * set the corresponding edge model (for quick access to meta data in the views)
	 *
	 *@method setRepresentedModel
	 *@param {@link GRAVIS3D.Model.GRAVIS3D.Model.Edge}
	 */
	this.setRepresentedModel = function( edgeModel ) {
		if ( edgeModel instanceof GRAVIS3D.Model.Edge ) {
			represents = edgeModel;
			this.setIfDirected( edgeModel.ifDirected() );
		} else throw new Error( "Visual Representation: Edge " + this.getId()
				+ " #getRepresentedNodeModel: param must be a Model - Edge... (param:" + JSON.stringify( edgeModel )
				+ ")" );
	};

	// DIRECTED
	var directed = GRAVIS3D.VisualRepresentation.Defaults.edge.directed;
	var directedNeedsUpdate = false;
	/**
	 * returns if the edge representation has a direction
	 * 
	 * @method ifDirected
	 * @return {Boolean}
	 */
	this.ifDirected = function() {
		return directed;
	};
	/**
	 * Sets the direction
	 * 
	 * @method setIfDirected
	 * @param {Boolean} boolean
	 */
	this.setIfDirected = function( boolean ) {
		if ( boolean == this.ifDirected() ) return;
		if ( typeof boolean == "boolean" ) directed = boolean;
		else throw new Error( "Visual Representation: Edge " + this.getId()
				+ " #setIfDirected: param must be a bool... (param:" + JSON.stringify( boolean ) + ")" );
	};

	// COLOR
	var color = GRAVIS3D.VisualRepresentation.Defaults.edge.color;
	var colorNeedsUpdate = false;
	/**
	 * sets the color of this edge
	 * 
	 * @method setColor
	 * @param {String} hex - a hexcode i.e. "#FF00FF", pattern: /^#([0-9a-f]{3}|[0-9a-f]{6})$/i
	 */
	this.setColor = function( hex ) {
		if ( hex == this.getColor() ) return;
		var valid = new RegExp( /^#([0-9a-f]{3}|[0-9a-f]{6})$/i ).test( hex );
		if ( valid ) {
			color = hex;
			colorNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Edge " + this.getId()
					+ " #setColor: no proper hex color set, please match i.e. '#000' or '#000000'... (param:"
					+ JSON.stringify( hex ) + ")" );
		}
	};
	/**
	 * returns the actual color
	 * 
	 * @method getColor
	 * @return {String} color - as hex-color: i.e. "#FF00FF", pattern: /^#([0-9a-f]{3}|[0-9a-f]{6})$/i
	 */
	this.getColor = function() {
		return color;
	};

	// BRIGHTNESS
	var brightness = GRAVIS3D.VisualRepresentation.Defaults.edge.brightness;
	var brightnessNeedsUpdate = false;
	/**
	 * sets the brightness the less bright, the blacker 0 = black / 1 = normal color
	 * 
	 * @method setBrightness
	 * @param {Number} number 0-1
	 */
	this.setBrightness = function( number ) {
		if ( number == this.getBrightness() ) return;
		if ( number < 0 ) number = 0;
		if ( number > 1 ) number = 1;
		if ( number >= 0 && number <= 1 ) {
			brightness = number;
			brightnessNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Edge " + this.getId()
					+ " #setBrightness: value must be between 0 and 1... (param:" + JSON.stringify( number ) + ")" );
		}
	};
	/**
	 * returns the brightness 0 = black / 1 = normal color
	 * 
	 * @method getBrightness
	 * @return {Number} brightness 0-1
	 */
	this.getBrightness = function() {
		return brightness;
	};

	// OPACITY
	var opacity = GRAVIS3D.VisualRepresentation.Defaults.edge.opacity;
	var opacityNeedsUpdate = false;
	/**
	 * sets the opacity 1 = fully opaque / 0 = fully transparent
	 * 
	 * @method setOpacity
	 * @param {Number} number 0-1
	 */
	this.setOpacity = function( number ) {
		if ( number == this.getOpacity() ) return;
		if ( number < 0 ) number = 0;
		if ( number > 1 ) number = 1;
		if ( number >= 0 && number <= 1 ) {
			opacity = number;
			opacityNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Edge " + this.getId()
					+ " #setOpacity: value must be between 0 and 1... (param:" + JSON.stringify( number ) + ")" );
		}
	};
	/**
	 * returns the actual opacity 1 = fully opaque / 0 = fully transparent
	 * 
	 * @method getOpacity
	 * @return {Number} opacity 0-1
	 */
	this.getOpacity = function() {
		return opacity;
	};

	// SIZE
	var size = GRAVIS3D.VisualRepresentation.Defaults.edge.size;
	var sizeNeedsUpdate = false;
	/**
	 * sets the size 1 = 100%
	 * 
	 * @method setSize
	 * @param {Number} number
	 */
	this.setSize = function( number ) {
		if ( number == this.getSize()) return;
		if ( number < 0 ) number = 0;
		if ( number >= 0 ) {
			size = number;
			sizeNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Edge " + this.getId()
					+ " #setSize: value must be a number >= 0... 1 = 100% 0.5 = 50%, ... (param:"
					+ JSON.stringify( number ) + ")" );
		}
	};
	/**
	 * returns the actual size
	 * 
	 * @method getSize
	 * @return {Number} size 0-n
	 */
	this.getSize = function() {
		return size;
	};

	// TEXTURE
	var texture = GRAVIS3D.VisualRepresentation.Defaults.edge.texture;
	var textureNeedsUpdate = false;
	/**
	 * sets the used texture by id it must be included in {@link GRAVIS3D.VisualRepresentation.possibleNodeTextures}
	 * 
	 * @method setTexture
	 * @param {String} textureId
	 */
	this.setTexture = function( textureId ) {
		if (textureId == this.getTexture()) return;
		if ( GRAVIS3D.VisualRepresentation.Defaults.edge.possibleTextures.indexOf( textureId.toLowerCase() ) != -1 ) {
			texture = textureId.toLowerCase();
			textureNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Edge " + this.getId() + " #setTexture: No such texture - "
					+ JSON.stringify( textureId ) + ". Possible Textures are: "
					+ GRAVIS3D.VisualRepresentation.Defaults.edge.possibleTextures );
		}
	};
	/**
	 * returns the actual texture id
	 * 
	 * @method getTexture
	 * @return {String} texture
	 */
	this.getTexture = function() {
		return texture;
	};

	// POSITION FROM
	var positionFrom = GRAVIS3D.VisualRepresentation.Defaults.edge.positionFrom;
	var positionFromNeedsUpdate = false;
	/**
	 * sets the start position
	 * 
	 * @method setPosition
	 * @param {@link GRAVIS3D.Vector3D} vector3d
	 */
	this.setPosition_From = function( vector3d ) {
		if ( vector3d instanceof GRAVIS3D.Vector3D ) {
			positionFrom = vector3d;
			positionFromNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Edge " + this.getId()
					+ " #setPositionFrom: No proper position (GRAVIS3D.Vector3D) passed... (param:"
					+ JSON.stringify( vector3d ) + ")" );
		}
	};
	/**
	 * returns the start position
	 * 
	 * @method getPosition
	 * @return {@link GRAVIS3D.Vector3D} position
	 */
	this.getPosition_From = function() {
		return positionFrom;
	};

	// POSITION TO
	var positionTo = GRAVIS3D.VisualRepresentation.Defaults.edge.positionTo;
	var positionToNeedsUpdate = false;
	/**
	 * sets the end position
	 * 
	 * @method setPosition
	 * @param {@link GRAVIS3D.Vector3D} vector3d
	 */
	this.setPosition_To = function( vector3d ) {
		if ( vector3d instanceof GRAVIS3D.Vector3D ) {
			positionTo = vector3d;
			positionToNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Edge " + this.getId()
					+ " #setPositionTo: No proper position (GRAVIS3D.Vector3D) passed... (param:"
					+ JSON.stringify( vector3d ) + ")" );
		}
	};
	/**
	 * returns the end position
	 * 
	 * @method getPosition
	 * @return {@link GRAVIS3D.Vector3D} position
	 */
	this.getPosition_To = function() {
		return positionTo;
	};

	// set / get both positions
	/**
	 * sets the start and end position
	 * 
	 * @method setPosition
	 * @param {@link GRAVIS3D.Vector3D} posFrom_vector3d
	 * @param {@link GRAVIS3D.Vector3D} posTo_vector3d
	 */
	this.setPosition = function( posFrom_vector3d, posTo_vector3d ) {
		if ( posFrom_vector3d instanceof GRAVIS3D.Vector3D && posTo_vector3d instanceof GRAVIS3D.Vector3D ) {
			positionFrom = posFrom_vector3d;
			positionTo = posTo_vector3d;
			positionFromNeedsUpdate = true;
			positionToNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Edge " + this.getId()
					+ " #setPosition: No proper position passed... (param:" + JSON.stringify( posFrom_vector3d ) + "/"
					+ JSON.stringify( posTo_vector3d ) + ")" );
		}
	};
	/**
	 * returns the start and end position
	 * 
	 * @method getPosition
	 * @return { { from:
	 * @link GRAVIS3D.Vector3D, to:
	 * @link GRAVIS3D.Vector3D } position
	 */
	this.getPosition = function() {
		return {
			from : this.getPosition_From(),
			to : this.getPosition_To()
		};
	};

	var highlighted = false;
	var highlightNeedsUpdate = false;
	/**
	 * TODO
	 */
	this.highlight = function() {
		if ( highlighted == true ) return;
		highlighted = true;
		highlightNeedsUpdate = true;
		this.updateImplObject();
	};
	/**
	 * TODO
	 */
	this.removeHighlight = function() {
		if ( highlighted == false ) return;
		highlighted = false;
		highlightNeedsUpdate = true;
		this.updateImplObject();
	};

	/**
	 * calls the setter for a given (visual) variable
	 * 
	 * @method set
	 * @param {String} variable (color, size, positionfrom, positionto, texture, opacity, brightness, directe, source, target)
	 * @param {*} value - see doc of variable's specific setter 
	 */
	this.set = function( variable, value ) {
		switch ( variable.toLowerCase() ) {
			case "color":
				this.setColor( value );
				break;
			case "size":
				this.setSize( value );
				break;
			case "position_from":
			case "positionfrom":
				this.setPosition_From( value );
				break;
			case "position_to":
			case "positionto":
				this.setPosition_To( value );
				break;
			case "texture":
				this.setTexture( value );
				break;
			case "opacity":
				this.setOpacity( value );
				break;
			case "brightness":
				this.setBrightness( value );
				break;
			case "directed":
				this.setIfDirected( value );
				break;
			case "source":
				this.setSource( value );
				break;
			case "target":
				this.setTarget( value );
				break;
			case "model":
				this.setRepresentedModel( value );
			default:
				throw new Error(
						"Visual Representation: Edge #set: no such variable ("
								+ JSON.stringify( variable )
								+ ") found. Possible: color, size, positionfrom, positionto, texture, opacity, brightness, directed, source, target." );
				break;
		}
	};

	/////////////////////////////
	// THREE.JS IMPLEMENTATION //
	/////////////////////////////

	var geometry = new THREE.Geometry();
	var posFrom = this.getPosition_From();
	var posTo = this.getPosition_To();
	geometry.vertices.push( new THREE.Vector3( posFrom.getX(), posFrom.getY(), posFrom.getZ() ), new THREE.Vector3(
			posTo.getX(), posTo.getY(), posTo.getZ() ) );
	geometry.computeLineDistances();
	var EdgeImpl = new THREE.Line( geometry, GRAVIS3D.VisualRepresentation.EdgeTextureBuilder( this.getTexture(), this
			.getSize() ) );
	this.renderObject = EdgeImpl;
	this.renderObject.vr = this;
	this.renderObject.name = "edge";
	textureNeedsUpdate = true;
	var highlightTMP = null;

	this.updateImplObject = function() {

		if ( highlightNeedsUpdate == true ) {
			if ( highlighted == true ) {
				highlightTMP = {
					color : this.getColor(),
					size : this.getSize(),
					opacity : this.getOpacity()
				};
				color = GRAVIS3D.VisualRepresentation.Defaults.edge.highlightColor;
				colorNeedsUpdate = true;
				size = this.getSize() * 4;
				sizeNeedsUpdate = true;
				opacity = 1;
				opacityNeedsUpdate = true;
			} else {
				color = highlightTMP.color;
				colorNeedsUpdate = true;
				size = highlightTMP.size;
				sizeNeedsUpdate = true;
				opacity = highlightTMP.opacity;
				opacityNeedsUpdate = true;
			}
			highlightNeedsUpdate = false;
		}

		if ( positionFromNeedsUpdate == true || positionToNeedsUpdate == true ) {
			var geom = new THREE.Geometry();
			var posFrom = this.getPosition_From();
			var posTo = this.getPosition_To();
			geom.vertices.push( new THREE.Vector3( posFrom.getX(), posFrom.getY(), posFrom.getZ() ), new THREE.Vector3(
					posTo.getX(), posTo.getY(), posTo.getZ() ) );
			geom.computeLineDistances();
			EdgeImpl.geometry = geom;
			positionNeedsUpdate = false;
			directedNeedsUpdate = true; //adjust arrow with new positions
		}

		if ( directedNeedsUpdate == true ) {
			// TODO
			// hol die positions
			// mache einen pfeil ans target
			// richte diesen gen source aus
			directedNeedsUpdate = false;
		}

		if ( textureNeedsUpdate == true || sizeNeedsUpdate == true ) {
			EdgeImpl.material = GRAVIS3D.VisualRepresentation.EdgeTextureBuilder( this.getTexture(), this.getSize() );
			textureNeedsUpdate = false;
			colorNeedsUpdate = true; //refresh on new material
			opacityNeedsUpdate = true; //refresh on new material
			if ( EdgeImpl.material && EdgeImpl.material.linewidth ) {
				EdgeImpl.material.linewidth = this.getSize();
			}
		}

		if ( opacityNeedsUpdate == true ) {
			if ( EdgeImpl.material && EdgeImpl.material.opacity ) {
				EdgeImpl.material.visible = true;
				if ( this.getOpacity() < 1 ) {
					EdgeImpl.material.transparent = true;
					EdgeImpl.material.opacity = this.getOpacity();
					if ( this.getOpacity() == 0 );// EdgeImpl.material.visible = false;
				} else {
					EdgeImpl.material.transparent = false;
					EdgeImpl.material.opacity = 1;
				}
			}
			opacityNeedsUpdate = false;
		}

		if ( colorNeedsUpdate == true || brightnessNeedsUpdate == true ) {
			if ( EdgeImpl.material && EdgeImpl.material.color ) {
				var c1 = new THREE.Color( "#000" );
				var c2 = new THREE.Color( this.getColor() );
				c2.lerp( c1, ( 1 - this.getBrightness() ) );
				EdgeImpl.material.color = c2;
			}
			colorNeedsUpdate = false;
			brightnessNeedsUpdate = false;
		}


		if ( this.filtered == true ) {
			EdgeImpl.material.depthTest = false;
			EdgeImpl.material.depthWrite = false;
		} else {
			EdgeImpl.material.depthTest = true;
			EdgeImpl.material.depthWrite = true;
		}


	};

	///////////////////////////
	// END OF IMPLEMENTATION //
	///////////////////////////

	if ( !params || !params.source || !params.target ) throw new Error( "Visual Representation - Edge " + this.id
			+ ": REQUIRED PARAMS: source, target." );
	this.setSource( params.source );
	this.setTarget( params.target );

	if ( params.represents ) this.setRepresentedModel( params.represents );
	if ( params.id ) this.setId( params.id );
	if ( params.name ) this.setName( params.name );
	if ( params.description ) this.setDescription( params.description );
	if ( params.data ) this.setData( params.data );
	if ( params.color ) this.setColor( params.color );
	if ( params.brightness >= 0 ) this.setBrightness( params.brightness );
	if ( params.opacity >= 0 ) this.setOpacity( params.opacity );
	if ( params.size >= 0 ) this.setSize( params.size );
	if ( params.texture ) this.setTexture( params.texture );
	if ( params.positionFrom ) this.setPosition_From( params.positionFrom );
	if ( params.positionTo ) this.setPosition_To( params.positionTo );
	if ( params.position ) this.setPosition( params.position.from, params.position.to );
	if ( params.orientation >= 0 ) this.setOrientation( params.orientation );

	this.updateImplObject();

};
/**
 * THREE.JS Implementation of Textures for an Edge
 * 
 * @method EdgeTextures
 * @param {String} textureId - must be included in {@link GRAVIS3D.VisualRepresentation.Defaults.edge.possibleTextures}
 * @return {@link THREE.Material}
 */
GRAVIS3D.VisualRepresentation.EdgeTextureBuilder = function( textureId, size ) {
	switch ( textureId ) {
		case "dashed":
			return new THREE.LineDashedMaterial( {
				color : 0xffaa00,
				dashSize : 0.3 * size * 1.618,
				gapSize : 0.3 * size
			} );
		case "dashed_narrow":
			return new THREE.LineDashedMaterial( {
				color : 0xffaa00,
				dashSize : 0.3 * size * 1.618 * 0.5,
				gapSize : 0.3 * size * 0.5
			} );
		case "dashed_wide":
			return new THREE.LineDashedMaterial( {
				color : 0xffaa00,
				dashSize : 0.3 * size * 1.618 * 2,
				gapSize : 0.3 * size * 2
			} );
		case "random":
			var textures = GRAVIS3D.VisualRepresentation.Defaults.edge.possibleTextures;
			var rndTex = textures[parseInt( Math.random() * ( textures.length - 1 ) )];
			return GRAVIS3D.VisualRepresentation.EdgeTextureBuilder( rndTex, size );
		case "solid":
		case "default":
		default:
			return new THREE.LineBasicMaterial( {
				color : 0x0000ff
			} );
	}
};
