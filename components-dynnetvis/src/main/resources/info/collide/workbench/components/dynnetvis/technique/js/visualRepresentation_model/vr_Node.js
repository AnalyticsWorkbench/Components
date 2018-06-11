/**
 * @author Henrik Detjen
 */

/////////////////////////////////////////
// The Visual Representation of a Node //
/////////////////////////////////////////
/**
 * The default values used by {@link GRAVIS3D.VisualRepresentation.Node}
 */
GRAVIS3D.VisualRepresentation.Defaults.node = {
	showLabel : false,
	colorLabel : true,
	label : " ",
	labelScale : 1,
	labelColor : "#000",//not used as visual variable so far..
	labelFont : "Helvetica",
	color : "#380B61",
	brightness : 1,
	opacity : 1,
	size : 1,
	highlightColor : "#FF55AA",
	texture : "default",
	form : "default",
	orientation : 0,
	position : new GRAVIS3D.Vector3D( 0, 0, 0 ),
	/**
	 *  list of possible (and valid) form ids - used by setForm() and GUI
	 */
	possibleForms : [ "default", "cube", "cone", "cone_down", "cylinder", "tetrahedron", "octahedron", "icosahedron",
			"torus", "person", "bag", "plane", "triangle", "ring", "circle", "sphere", "random" ],
	/**
	 * list of possible (and valid) texture ids - used by setForm() and GUI
	 */
	possibleTextures : [ "default", "wireframe", "fancy", "random" ],
	/**
	 * The possible Fonts for a label used by {@link GRAVIS3D.VisualRepresentation.Node#setLabel} and the GUI
	 */
	possibleLabelFonts : [ "sans-serif", "serif", "monospace", "cursive", "fantasy", "Courier New", "Courier",
			"Lucida Console", "Monaco", "Helvetica", "Arial", "Lucida Sans Unicode", "Impact", "Arial Black", "Gadget",
			"Comic Sans MS", "Charcoal", "Tahoma", "Geneva", "Trebuchet MS", "Verdana" ]
};

/**
 * @summary
 * An abstract Visual Representation for a Node
 * 
 * @since 1.0
 * 
 * @constructor Node
 * @param {<br>
 * 		&emsp;represents: {@link GRAVIS3D.Model.Node}, <br>
 * 		&emsp;color: {String},<br>
 * 		&emsp;brightness: {0-1}, <br>
 * 		&emsp;opacity: {0-1}, <br>	
 * 		&emsp;size: {0-n},<br>
 * 		&emsp;texture: {String}, <br>
 *		&emsp;form: {String}, <br>
 *		&emsp;orientation: {0-1}, <br>
 *		&emsp;position: {@link GRAVIS3D.Vector3D} <br>
 * 		&emsp;showLabel: {Boolean}, <br>
 * 		&emsp;label: {String}, <br>
 * 		&emsp;labelScale: {0-n}, <br>
 * 		&emsp;labelFont: {String}, <br>
 * 		&emsp;colorLabel: {Boolean}, <br>
 * } params
 * 
 * @example
 * TODO
 */
GRAVIS3D.VisualRepresentation.Node = function( params ) {

	GRAVIS3D.InfoObject.call( this ); // id, name, description, data

	// MODEL NODE
	var represents = null;
	/**
	 * gets the corresponding edge model (for quick access to meta data in the views)
	 *
	 *@method getRepresentedModel
	 *@return {@link GRAVIS3D.Model.Node}
	 */
	this.getRepresentedModel = function() {
		return represents;
	};
	/**
	 * set the corresponding node model (for quick access to meta data in the views)
	 *
	 *@method setRepresentedModel
	 *@param {@link GRAVIS3D.Model.Node}
	 */
	this.setRepresentedModel = function( nodeModel ) {
		if ( nodeModel instanceof GRAVIS3D.Model.Node ) {
			represents = nodeModel;
			this.resetLabel();
		} else throw new Error( "Visual Representation - Node " + this.id
				+ ": getRepresentedNodeModel(): param must be a Model - Node... (param:" + JSON.stringify( nodeModel )
				+ ")" );
	};


	// LABEL
	var label = GRAVIS3D.VisualRepresentation.Defaults.node.label;
	var labelNeedsUpdate = false;
	/**
	 * sets the node label
	 * 
	 * @method setLabel
	 * @param {String} string
	 */
	this.setLabel = function( string ) {
		if ( string == this.getLabel() ) return;
		if ( typeof string != "string" && string != undefined ) string = JSON.stringify( string );
		//		if ( string.length >= 30 ){
		//			string = string.substring(0,26) + "...";
		//		}
		label = string;
		if ( this.ifShowLabel() ) {
			labelNeedsUpdate = true;
			this.updateImplObject();
		}
	};
	/**
	 * resets the label
	 */
	this.resetLabel = function() {
		if ( represents == null ) this.setLabel( GRAVIS3D.VisualRepresentation.Defaults.node.label );
		else {
			this.setLabel( represents.getName() );
		}
	};
	/**
	 * returns the node label
	 * 
	 * @method getLabel
	 * @return {String} label
	 */
	this.getLabel = function() {
		return label;
	};

	var labelScale = GRAVIS3D.VisualRepresentation.Defaults.node.labelScale; // to scale the labels independent 
	/**
	 * set the scale of the label (to scale independent from size) 1 = 100%
	 * 
	 * @method setLabelScale
	 * @param {Number} number
	 */
	this.setLabelScale = function( number ) {
		if ( number == this.getLabelScale() ) return;
		if ( number < 0 ) number = 0;
		if ( number >= 0 ) {
			labelScale = number;
			labelNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Node " + this.getId()
					+ " - Label Scale: value must be a number greater than 0... (param:" + JSON.stringify( number )
					+ ")" );
		}
	};
	/**
	 * returns the scale 1 = 100%
	 * 
	 * @method getLabelScale
	 * @return {Number} labelScale
	 */
	this.getLabelScale = function() {
		return labelScale;
	};

	var showLabel = GRAVIS3D.VisualRepresentation.Defaults.node.showLabel; // if the label should be hidden
	/**
	 * set if the label of the node is shown or not
	 * 
	 * @method setIfShowLabel
	 * @param {Boolean} boolean
	 */
	this.setIfShowLabel = function( boolean ) {
		if ( boolean == this.ifShowLabel() ) return;
		if ( typeof boolean == "string" ) {
			if ( boolean == "true" ) boolean = true;
			if ( boolean == "false" ) boolean = false;
		}
		if ( typeof boolean == "boolean" ) {
			showLabel = boolean;
			labelNeedsUpdate = true;
			this.updateImplObject();
		}
	};
	/**
	 * if the label is shown or not
	 * 
	 * @method ifShowLabel
	 * @return {Boolean} showLabel
	 */
	this.ifShowLabel = function() {
		return showLabel;
	};

	var labelFont = GRAVIS3D.VisualRepresentation.Defaults.node.labelFont; // the font used by the label 
	/**
	 * sets the labels font must be included in {@link GRAVIS3D.VisualRepresentation.Defaults.node.possibleLabelFonts}
	 * 
	 * @method setLabelFont
	 * @param {String} font
	 */
	this.setLabelFont = function( font ) {
		if ( font == this.getLabelFont() ) return;
		if ( GRAVIS3D.VisualRepresentation.Defaults.node.possibleLabelFonts.indexOf( font ) != -1 ) {
			labelFont = font;
			labelNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Node " + this.getId()
					+ " - Label Font: Font not found - have a look at possible fonts... (param:"
					+ JSON.stringify( font ) + ")" );
		}
	};
	/**
	 * returns the font actually used by the node's label
	 * 
	 * @method getLabelFont
	 * @return {String} labelFont
	 */
	this.getLabelFont = function() {
		return labelFont;
	};

	var colorLabel = GRAVIS3D.VisualRepresentation.Defaults.node.colorLabel; // if the labels color should be the node's one
	/**
	 * sets if the label should have the same color as the node
	 * 
	 * @method setIfColorLabel
	 * @param {Boolean} boolean
	 */
	this.setIfColorLabel = function( boolean ) {
		if ( boolean == this.ifColorLabel() ) return;
		if ( typeof boolean == "boolean" ) {
			colorLabel = boolean;
			labelNeedsUpdate = true;
			this.updateImplObject();
		}
	};
	/**
	 * if the label has the same color as the node
	 * 
	 * @method ifColorLabel
	 * @return {Boolean} colorLabel
	 */
	this.ifColorLabel = function() {
		return colorLabel;
	};

	// COLOR
	var color = GRAVIS3D.VisualRepresentation.Defaults.node.color;
	var colorNeedsUpdate = false;
	/**
	 * sets the color of this node
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
			throw new Error( "Visual Representation: Node " + this.getId()
					+ " - Color: no proper hex color set, please match i.e. '#000' or '#000000'.. (param:"
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
	var brightness = GRAVIS3D.VisualRepresentation.Defaults.node.brightness;
	var brightnessNeedsUpdate = false;
	/**
	 * sets the brightness
	 * the less bright, the blacker
	 * 0 = black / 1 = normal color
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
			throw new Error( "Visual Representation: Node " + this.getId()
					+ " - Brightness: value must be between 0 and 1... (param:" + JSON.stringify( number ) + ")" );
		}
	};
	/**
	 * returns the brightness
	 * 0 = black / 1 = normal color
	 * 
	 * @method getBrightness
	 * @return {Number} brightness 0-1
	 */
	this.getBrightness = function() {
		return brightness;
	};

	// OPACITY
	var opacity = GRAVIS3D.VisualRepresentation.Defaults.node.opacity;
	var opacityNeedsUpdate = false;
	/**
	 * sets the opacity
	 * 1 = fully opaque / 0 = fully transparent
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
			throw new Error( "Visual Representation: Node " + this.getId()
					+ " - Opacity: value must be between 0 and 1... (param:" + JSON.stringify( number ) + ")" );
		}
	};
	/**
	 * returns the actual opacity
	 * 1 = fully opaque / 0 = fully transparent
	 * 
	 * @method getOpacity
	 * @return {Number} opacity 0-1
	 */
	this.getOpacity = function() {
		return opacity;
	};

	// SIZE
	var size = GRAVIS3D.VisualRepresentation.Defaults.node.size;
	var sizeNeedsUpdate = false;
	/**
	 * sets the size
	 * 1 = 100%
	 * 
	 * @method setSize
	 * @param {Number} number
	 */
	this.setSize = function( number ) {
		if ( number == this.getSize() ) return;
		if ( number < 0 ) number = 0;
		if ( number >= 0 ) {
			size = number;
			sizeNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Node " + this.getId()
					+ " - Size: value must be a number. 1 = 100% 0.5 = 50%, .... (param:" + JSON.stringify( number )
					+ ")" );
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
	var texture = GRAVIS3D.VisualRepresentation.Defaults.node.texture;
	var textureNeedsUpdate = false;
	/**
	 * sets the used texture by id
	 * it must be included in {@link GRAVIS3D.VisualRepresentation.Defaults.node.possibleTextures}
	 * 
	 * @method setTexture
	 * @param {String} textureId
	 */
	this.setTexture = function( textureId ) {
		if ( textureId == this.getTexture() ) return;
		if ( GRAVIS3D.VisualRepresentation.Defaults.node.possibleTextures.indexOf( textureId.toLowerCase() ) != -1 ) {
			texture = textureId.toLowerCase();
			textureNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Node " + this.getId() + " - Texture: No such texture - "
					+ JSON.stringify( textureId ) + ". PossibleTextures are: "
					+ GRAVIS3D.VisualRepresentation.Defaults.node.possibleTextures.toString() );
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

	// FORM
	var form = GRAVIS3D.VisualRepresentation.Defaults.node.form;
	var formNeedsUpdate = false;
	/**
	 * sets the used form by id
	 * it must be included in {@link GRAVIS3D.VisualRepresentation.Defaults.node.possibleForms}
	 * 
	 * @method setForm
	 * @param {String} formId
	 */
	this.setForm = function( formId ) {
		if ( formId == this.getForm() ) return;
		if ( GRAVIS3D.VisualRepresentation.Defaults.node.possibleForms.indexOf( formId ) != -1 ) {
			form = formId;
			formNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Node " + this.getId() + " - Form: No such form - "
					+ JSON.stringify( formId ) + ". Possible Forms are: "
					+ GRAVIS3D.VisualRepresentation.Defaults.node.possibleForms.toString() );
		}
	};
	/**
	 * returns the actual form id 
	 * 
	 * @method getForm
	 * @return {String} form
	 */
	this.getForm = function() {
		return form;
	};

	// POSITION
	var position = GRAVIS3D.VisualRepresentation.Defaults.node.position;
	var positionNeedsUpdate = false;
	/**
	 * sets the position
	 * 
	 * @method setPosition
	 * @param {@link GRAVIS3D.Vector3D} vector3d
	 */
	this.setPosition = function( vector3d ) {
		if ( vector3d instanceof GRAVIS3D.Vector3D ) {
			position = vector3d;
			positionNeedsUpdate = true;
			this.updateImplObject();
		} else {
			if ( vector3d.x && vector3d.y && vector3d.z ) {
				this.setPosition( new GRAVIS3D.Vector3D( vector3d.x, vector3d.y, vector3d.z ) );
			} else {
				throw new Error( "Visual Representation: Node " + this.getId()
						+ " - Position: No proper position (GRAVIS3D.Vector3D) passed... (param:"
						+ JSON.stringify( vector3d ) + ")" );
			}
		}
	};
	/**
	 * returns the position
	 * 
	 * @method getPosition
	 * @return {@link GRAVIS3D.Vector3D} position
	 */
	this.getPosition = function() {
		return position;
	};

	// ORIENTATION
	var orientation = GRAVIS3D.VisualRepresentation.Defaults.node.orientation;
	var orientationNeedsUpdate = false;
	/**
	 * 0 = No Rotation to 1 = 180°Rotation
	 * 
	 * @method setOrientation
	 * @param {Number} number
	 */
	this.setOrientation = function( number ) {
		if ( number == this.getOrientation() ) return;
		if ( number < 0 ) number = 0;
		if ( number > 1 ) number = 1;
		if ( number >= 0 && number <= 1 ) {
			orientation = number;
			orientationNeedsUpdate = true;
			this.updateImplObject();
		} else {
			throw new Error( "Visual Representation: Node " + this.getId()
					+ " #setOrientation: value must be between 0 and 1... (param:" + JSON.stringify( number ) + ")" );
		}
	};
	/**
	 * 0 = No Rotation to 1 = 180°Rotation
	 * 
	 * @method getOrientation
	 * @return {Number} orientation
	 */
	this.getOrientation = function() {
		return orientation;
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
		highlightNeedsUpdate = true;
		highlighted = false;
		this.updateImplObject();
	};

	/**
	 * calls the setter for a given (visual) variable
	 * 
	 * @param {String} variable (color, size, position, orientation, texture, form, opacity, brightness, label, labelscale, labelfont, showlabel, colorlabel)
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
			case "position":
				this.setPosition( value );
				break;
			case "orientation":
				this.setOrientation( value );
				break;
			case "texture":
				this.setTexture( value );
				break;
			case "form":
				this.setForm( value );
				break;
			case "opacity":
				this.setOpacity( value );
				break;
			case "brightness":
				this.setBrightness( value );
				break;
			case "label":
				this.setLabel( value );
				break;
			case "labelscale":
				this.setLabelScale( value );
				break;
			case "labelfont":
				this.setLabelFont( value );
				break;
			case "showlabel":
				this.ifShowLabel( value );
				break;
			case "colorlabel":
				this.ifColorLabel( value );
				break;
			case "model":
				this.setRepresentedModel( value );
				break;
			default:
				throw new Error(
						"Visual Representation: Node #set: no such variable ("
								+ JSON.stringify( variable )
								+ ") found. Possible: color, size, position, orientation, texture, form, opacity, brightness, label, labelscale, labelfont, showlabel, colorlabel." );
				break;
		}
	};

	/////////////////////////////
	// THREE.JS IMPLEMENTATION //
	/////////////////////////////

	this.renderObject = new THREE.Object3D(); // render interface for view
	this.renderObject.name = "nodeObject";
	this.renderObject.vr = this;
	var nodeImpl = new THREE.Mesh( GRAVIS3D.VisualRepresentation.NodeFormBuilder( this.getForm(), this.getSize() ),
			GRAVIS3D.VisualRepresentation.NodeTextureBuilder( this.getTexture() ) );
	nodeImpl.name = "node";
	var labelImpl = new textSprite( this.getLabel(), {
		size : this.getSize()
	} );
	labelImpl.name = "label";
	this.renderObject.add( nodeImpl );
	this.renderObject.add( labelImpl );

	/**
	 * this method is called to apply changes from the abstract object to the implemented object
	 * 
	 * @method updateImplObject
	 */
	this.updateImplObject = function() {

		if ( size == 0 ) {
			size = 0.00001;
		}

		if ( formNeedsUpdate == true ) {

			this.renderObject.remove( nodeImpl );
			nodeImpl = new THREE.Mesh( GRAVIS3D.VisualRepresentation.NodeFormBuilder( this.getForm(), this.getSize() ),
					GRAVIS3D.VisualRepresentation.NodeTextureBuilder( this.getTexture() ) );
			nodeImpl.name = "node";
			this.renderObject.add( nodeImpl );
			formNeedsUpdate = false;
			sizeNeedsUpdate = false;
			textureNeedsUpdate = false;
			positionNeedsUpdate = true;
			colorNeedsUpdate = true;
			brightnessNeedsUpdate = true;
			opacityNeedsUpdate = true;
			if ( this.ifShowLabel() == true ) labelNeedsUpdate = true;
			if ( highlighted == true ) highlightNeedsUpdate = true;
		}

		if ( sizeNeedsUpdate == true ) {
			var geomToSet = GRAVIS3D.VisualRepresentation.NodeFormBuilder( this.getForm(), this.getSize() );
			nodeImpl.geometry.dynamic = true;
			nodeImpl.geometry.vertices = geomToSet.vertices;
			nodeImpl.geometry.verticesNeedUpdate = true;
			nodeImpl.geometry.computeBoundingBox();
			sizeNeedsUpdate = false;
			if ( this.ifShowLabel() == true ) labelNeedsUpdate = true; // set size of the label, too
			if ( highlighted == true ) highlightNeedsUpdate = true;
		}

		if ( textureNeedsUpdate == true ) {
			nodeImpl.material = GRAVIS3D.VisualRepresentation.NodeTextureBuilder( this.getTexture() );
			textureNeedsUpdate = false;
			colorNeedsUpdate = true; //refresh on new material
			opactiyNeedsUpdate = true; //refresh on new material
		}

		if ( colorNeedsUpdate == true || brightnessNeedsUpdate == true ) {
			if ( nodeImpl.material && nodeImpl.material.color ) {
				if ( this.getBrightness() < 0.5 ) {
					var c1 = new THREE.Color( "#000" );
					var c2 = new THREE.Color( this.getColor() );
					var s = d3.scale.linear().domain( [ 0, 0.5 ] ).range( [ 0, 1 ] );
					c2.lerp( c1, s( this.getBrightness() ) );
					nodeImpl.material.color = c2;
				} else {
					var c1 = new THREE.Color( "#FFF" );
					var c2 = new THREE.Color( this.getColor() );
					var s = d3.scale.linear().domain( [ 0, 5, 1 ] ).range( [ 0, 1 ] );
					c2.lerp( c1, s( this.getBrightness() ) );
					nodeImpl.material.color = c2;
				}
			}
			colorNeedsUpdate = false;
			brightnessNeedsUpdate = false;
			if ( this.ifShowLabel() == true && this.ifColorLabel() == true ) labelNeedsUpdate = true; // set color of label, too

		}

		if ( opacityNeedsUpdate == true ) {
			if ( nodeImpl.material && nodeImpl.material.opacity ) {
				if ( this.getOpacity() < 1 ) {
					nodeImpl.material.transparent = true;
					nodeImpl.material.opacity = this.getOpacity();
				} else {
					nodeImpl.material.transparent = false;
					nodeImpl.material.opacity = 1;
				}
			}
			opacityNeedsUpdate = false;
			if ( this.ifShowLabel() == true ) labelNeedsUpdate = true; // set opacity of the label, too
		}

		if ( positionNeedsUpdate == true ) {
			var pos = this.getPosition();
			nodeImpl.position.x = pos.getX();
			nodeImpl.position.y = pos.getY();
			nodeImpl.position.z = pos.getZ();
			positionNeedsUpdate = false;
			if ( this.ifShowLabel() == true ) labelNeedsUpdate = true; // set pos of label, too
		}

		if ( orientationNeedsUpdate == true ) {
			nodeImpl.rotation.z = Math.PI * this.getOrientation();
			orientationNeedsUpdate = false;
		}

		if ( labelNeedsUpdate == true ) {
			if ( this.ifShowLabel() == true ) {
				var c = GRAVIS3D.VisualRepresentation.Defaults.node.labelColor;
				if ( this.ifColorLabel() == true ) {
					var c1 = new THREE.Color( "#000" );
					var c2 = new THREE.Color( this.getColor() );
					c2.lerp( c1, ( 1 - this.getBrightness() ) );
					c = "#" + c2.getHexString();
				}

				var __size = this.getSize();
				if ( __size < 1 ) __size = 1; // in case size is 0 
				this.renderObject.remove( labelImpl );
				labelImpl = new textSprite( this.getLabel(), {
					size : __size * this.getLabelScale(),
					font : this.getLabelFont() + ", sans-serif", // add sans-serif as fallback
					color : c
				} );
				labelImpl.name = "label";
				this.renderObject.add( labelImpl );
				//				var toSet = textSprite( this.getLabel(), {
				//					size : __size * this.getLabelScale(),
				//					font : this.getLabelFont() + ", sans-serif", // add sans-serif as fallback
				//					color : c
				//				} );
				//				labelImpl.geometry.dynamic = true;
				//				labelImpl.geometry.vertices = toSet.geometry.vertices;
				//				labelImpl.geometry.verticesNeedUpdate = true;
				//				labelImpl.material = toSet.material;
				//				labelImpl.geometry.computeBoundingBox();

				// place label above node form
				var pos = this.getPosition();
				var formHeight = this.getSize() / 2; // relative to Position
				var labelHeight = ( this.getSize() / 2 ) * this.getLabelScale(); // relative to Position
				var up = formHeight + labelHeight;
				labelImpl.position.x = pos.getX();
				labelImpl.position.y = pos.getY() + up;
				labelImpl.position.z = pos.getZ() + 0.001;
				if ( this.getOpacity() < 1 ) {
					labelImpl.material.transparent = true;
					labelImpl.material.opacity = this.getOpacity();
				} else {
					labelImpl.material.transparent = false;
					labelImpl.material.opacity = 1;
				}
				labelImpl.visible = true;
			} else {
				labelImpl.material.transparent = true;
				labelImpl.material.opacity = 0;
				labelImpl.visible = false;
			}
			labelNeedsUpdate = false;
		}

		if ( highlightNeedsUpdate == true ) {
			var highlight = this.renderObject.getObjectByName( "highlight", true );
			if ( highlight != null ) {
				this.renderObject.remove( highlight );
			}
			if ( highlighted == true ) {
				var geom = GRAVIS3D.VisualRepresentation.NodeFormBuilder( this.getForm(), this.getSize() );
				//			var modifier = new THREE.SubdivisionModifier( 2 );
				//			modifier.modify( geom ); 
				var mat = new THREE.ShaderMaterial( {
					uniforms : {
						"c" : {
							type : "f",
							value : 1.0
						},
						"p" : {
							type : "f",
							value : 1.4
						},
						glowColor : {
							type : "c",
							value : new THREE.Color( GRAVIS3D.VisualRepresentation.Defaults.node.highlightColor )
						},
						viewVector : {
							type : "v3",
							value : new THREE.Vector3( 0, 0, 5 )
						}
					},
					vertexShader : document.getElementById( 'vertexShader' ).textContent,
					fragmentShader : document.getElementById( 'fragmentShader' ).textContent,
					side : THREE.FrontSide,
					blending : THREE.AdditiveBlending,
					transparent : true
				} );
				highlight = new THREE.Mesh( geom, mat );
				var pos = this.getPosition();
				highlight.position.x = pos.getX();
				highlight.position.y = pos.getY();
				highlight.position.z = pos.getZ();
				highlight.scale.multiplyScalar( 1.05 );
				highlight.name = "highlight";
				this.renderObject.add( highlight );
			}
			highlightNeedsUpdate = false;
		}

	};

	/**
	 * creates a label by rendering an image in canvas2D and placing this on a 3D plane
	 */
	function textSprite( text, params ) {

		if ( params.size == undefined ) params.size = 1;

		var _font = "Helvetica";
		var _fontSize = 100 * params.size; //not the real size this is used for the texture
		var _color = "#FFF";
		if ( params.font ) _font = params.font;
		if ( params.color ) _color = params.color;
		if ( params.fontSize ) _fontSize = params.fontSize;
		_font = "bold " + _fontSize + "px " + _font;

		var canvas = document.createElement( 'canvas' );
		var context = canvas.getContext( '2d' );
		context.font = _font;

		// get size data (height depends only on font size)
		var metrics = context.measureText( text );
		var textWidth = metrics.width;
		if ( textWidth > 4000 ) textWidth = 4000;

		canvas.width = textWidth + 0;
		canvas.height = _fontSize + 10;

		context.font = _font;
		context.fillStyle = _color;
		context.fillText( text, 0, ( _fontSize - _fontSize * 0.2 ) );

		// canvas contents will be used for a texture
		var texture = new THREE.Texture( canvas );
		texture.needsUpdate = true;

		var canvasRatio = canvas.width / canvas.height;

		// put texture on plane object with a texture on material
		var mesh = new THREE.Mesh( new THREE.PlaneGeometry( params.size * canvasRatio, params.size ),
				new THREE.MeshBasicMaterial( {
					map : texture,
					side : THREE.FrontSide
				} ) );

		var labelScale = 1;
		if ( params.labelScale ) labelScale = params.labelScale;
		mesh.scale.normalize().multiplyScalar( 0.5 * labelScale );
		mesh.geometry.computeBoundingBox();

		// CHROME BUG - does not release textures (if not set to null)
		canvas = null;

		return mesh;

	}

	///////////////////////////
	// END OF IMPLEMENTATION //
	///////////////////////////

	if ( params && params.represents ) this.setRepresentedModel( params.represents );
	if ( params && params.id ) this.setId( params.id );
	if ( params && params.name ) this.setName( params.name );
	if ( params && params.description ) this.setDescription( params.description );
	if ( params && params.data ) this.setData( params.data );
	if ( params && params.label ) this.setLabel( params.label );
	if ( params && params.showLabel ) this.setShowLabel( params.showLabel );
	if ( params && params.colorLabel ) this.setColorLabel( params.showLabel );
	if ( params && params.color ) this.setColor( params.color );
	if ( params && params.brightness >= 0 ) this.setBrightness( params.brightness );
	if ( params && params.opacity >= 0 ) this.setOpacity( params.opacity );
	if ( params && params.size >= 0 ) this.setSize( params.size );
	if ( params && params.texture ) this.setTexture( params.texture );
	if ( params && params.form ) this.setForm( params.form );
	if ( params && params.position ) this.setPosition( params.position );
	if ( params && params.orientation >= 0 ) this.setOrientation( params.orientation );

	this.updateImplObject();

};

/**
 * THREE.JS Implementation of Textures for a Node
 * 
 * @method NodeTextures
 * @param {String} textureId - must be included in {@link GRAVIS3D.VisualRepresentation.Defaults.node.possibleTextures}
 * @return instance of THREE.Material
 */
GRAVIS3D.VisualRepresentation.NodeTextureBuilder = function( textureId ) {
	switch ( textureId ) {
		case "fancy":
			return new THREE.MeshNormalMaterial( {
				overdraw : 0.5,
				side : THREE.FrontSide
			} );
		case "wireframe":
			return new THREE.MeshBasicMaterial( {
				color : GRAVIS3D.VisualRepresentation.Defaults.node.color,
				wireframe : true,
				side : THREE.FrontSide
			} );
		case "random":
			var textures = GRAVIS3D.VisualRepresentation.Defaults.node.possibleTextures;
			var rndTex = textures[parseInt( Math.random() * ( textures.length - 1 ) )];
			return GRAVIS3D.VisualRepresentation.NodeTextureBuilder( rndTex );
		case "default":
		default:
			return new THREE.MeshBasicMaterial( {
				color : GRAVIS3D.VisualRepresentation.Defaults.node.color,
				side : THREE.FrontSide
			} );
	}
};

/**
 * THREE.JS Implementation of Forms for a Node
 * 
 * @method NodeForms
 * @param {String} formId - must be included in {@link GRAVIS3D.VisualRepresentation.Defaults.node.possibleForms}
 * @param {Number} size - 1 = 100%
 * @return instance of THREE.Geometry
 */
GRAVIS3D.VisualRepresentation.NodeFormBuilder = function( formId, size ) {
	var quality = 16 * GRAVIS3D.VisualRepresentation.Defaults.rendering.quality; //segments to use..
	switch ( formId ) {
		case "sphere":
			return new THREE.SphereGeometry( ( size / 2 ), quality, quality );
		case "cube":
			return new THREE.BoxGeometry( size, size, size );
		case "cylinder":
			return new THREE.CylinderGeometry( size / 2, size / 2, size, quality * 2 );
		case "cone":
			return new THREE.CylinderGeometry( 0, size / 2, size, quality );
		case "cone_down":
			return new THREE.CylinderGeometry( size / 2, 0, size, quality );
		case "tetrahedron":
			return new THREE.TetrahedronGeometry( size / 2, 0 );
		case "octahedron":
			return new THREE.OctahedronGeometry( size / 2, 0 );
		case "icosahedron":
			return new THREE.IcosahedronGeometry( size / 2, 0 );
		case "torus":
			var tube = size / 8;
			var radius = size / 2 - ( tube / 2 );
			return new THREE.TorusGeometry( radius, tube, quality, quality );
		case "person":
			var bodySize = size * 0.618 * 1.2;
			var headSize = size * 0.382 * 1.2;//golden cut relation
			var headRadius = headSize / 2;
			var body = new THREE.CylinderGeometry( 0, headRadius * 1.618, bodySize + headRadius, quality );
			var head = new THREE.SphereGeometry( headRadius, quality, quality );
			var t = new THREE.Matrix4();
			t.makeTranslation( 0, ( ( size / 2 ) - headRadius ), 0 );
			body.merge( head, t );
			return body;
		case "bag":
			var bagHeight = size * 1.2 * 0.618;
			var bag = new THREE.BoxGeometry( size, bagHeight, size * 0.25 );
			var tube = size / 24;
			var radius = size / 8 - ( tube / 2 );
			var grasp = new THREE.TorusGeometry( radius, tube, quality, quality );
			var t = new THREE.Matrix4();
			t.makeTranslation( 0, bagHeight / 2, 0 );
			bag.merge( grasp, t );
			return bag;
		case "plane":
			return new THREE.PlaneGeometry( size, size );
		case "triangle":
			var t = new THREE.Shape();
			t.moveTo( 0, -( size / 2 ) );
			t.lineTo( -( size / 2 ), -( size / 2 ) );
			t.lineTo( 0, ( size / 2 ) );
			t.lineTo( ( size / 2 ), -( size / 2 ) );
			t.lineTo( 0, -( size / 2 ) );
			return new THREE.ShapeGeometry( t );
		case "circle":
			return new THREE.CircleGeometry( size / 2, quality * 3 );
		case "ring":
			return new THREE.RingGeometry( size / 4, size / 2, quality * 2 );
		case "random":
			var forms = GRAVIS3D.VisualRepresentation.Defaults.node.possibleForms;
			var rndForm = forms[parseInt( Math.random() * ( forms.length - 1 ) )];
			return GRAVIS3D.VisualRepresentation.NodeFormBuilder( rndForm, size );
		case "default":
		default:
			return new THREE.SphereGeometry( ( size / 2 ), quality, quality );
	}
};
