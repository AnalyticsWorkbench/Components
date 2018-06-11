/**
 * Created with JetBrains WebStorm.
 * User: alfredo
 * Date: 2/25/13
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
// the network title
var title;
// the network description
var description;
// is the network directed
var directed;
// the network measures
var measures;
//the network data links
var datalinks;
// the node properties
var nodeproperties;
//the edge properties
var edgeproperties;
// the node set
var nodes;
// the edge set
var edges;

//time navegation variables
var timeappearances;
var currentTime;
var dataAppearanceIndex = 0;

//node defailt radius
var defaultRadius = 6;//the node colors
//the node colors
var nodeColors=d3.scale.category10();

//the exploration context
var exploration = false;

//the property for the search
var propertySearch = "label";

function loadData(dataFile){


    d3.json(dataFile, function(json) {

        this.title=json.metadata.title;
        this.description=json.metadata.description;
        this.type=json.metadata.type;
        this.directed = json.metadata.directed;
        if(json.metadata.measures!=null)
            this.measures = json.metadata.measures;
        this.datalinks = json.metadata.datalinks;
        if(json.metadata.nodeproperties!=null)
            this.nodeproperties =json.metadata.nodeproperties;
        if(json.metadata.edgeproperties!=null)
            this.edgeproperties =json.metadata.edgeproperties;

        // Modification of Data Table Visualizations.
        this.data = json.data;

        initialize();
        redraw();

    });
}


function initialize() {
    // Set time appearances and currenttime
    timeappearances = calculateTimeapperances();
    this.currentTime = timeappearances[dataAppearanceIndex];
    d3.select('#timeselector').attr("value", timeappearances[dataAppearanceIndex]) ;

    // Set description
    d3.select("#description").text("Network Description: "+ this.description );

    d3.select("#measures").selectAll("option").remove();

    d3.select('#measures').append("option")
        .attr("value","default")
        .text("None");

    if(measures!=null){
        measures.forEach(function(measure){
            d3.select('#measures').append("option")
                .attr("value",measure.property)
                .text(measure.title);
        })
    }
    
    initializeMeasureControlsSearch();
}


function start(){
    loadData(src[0].toString());
}


/**
 * Creates a select or comboBox into the div with id = 'customControls'
 * and fill the comboBox with all measures.
 */
function initializeMeasureControls(){
    
    try{
        
        var controlSlot = d3.select("#customControls");
        if(controlSlot != null && controlSlot[0][0] != null){
            
            d3.select("#customControls p").remove();
            d3.select("#customControls select").remove();
            
            controlSlot.append("p").text("Select measure:");// label
            var measuresY = controlSlot.append("select");       // select

            measuresY
                    .attr("id", "idSelectMeasure")
                    .on("change", function() {saveCurrentMeasure();draw();})
                    .selectAll("option")
                        .data(this.measures).enter()   // options
                            .append("option")
                                .attr("value", function(d) {return d.property;} )
                                .text( function(d) {return d.title;} );
            
            saveCurrentMeasure();
        }
    }
    catch(e){
        alert(e);
    }
}

/**
 * Saves the measure selected.
 */
function saveCurrentMeasure(){

    try{
        
        var measuresY = d3.select("#idSelectMeasure");
        
        var sOptionY = measuresY.node().options[measuresY.node().selectedIndex];
        
        currentMeasure = sOptionY.value;
        currentMeasureLabel = sOptionY.text;
    }
    catch(e){
        alert(e);
    }
}

/*
 Calculates timeapearances array using datalinks
 The results is a sorted array containing times
 */
function calculateTimeapperances() {

    taArray = new Array();

    src.forEach( function(n){
        taArray.push(n.toString().substring(0,n.toString().indexOf(".")));
    });
    return taArray;
}

/*
 Updates currentTime moving step in timeappearances array (sign of step indicates direction )
 Checks index array bounds and only updates in correct cases.
 Calls onUpdateCallback to execute actions if needed
 */
function updateCurrentTimeByStep(step, onUpdateCallback) {

    var currentTimeIndex = timeappearances.indexOf(currentTime);
    currentTimeIndex += step;

    if ( currentTimeIndex > -1 && currentTimeIndex <  timeappearances.length ) {
        currentTime =  timeappearances[currentTimeIndex];
        dataAppearanceIndex = currentTimeIndex;
        onUpdateCallback();
        loadData(src[currentTimeIndex].toString());
    }



}

// CurrentTime post update callback function
function onCurrentTimeUpdate() {
    d3.select('#timeselector').attr("value", currentTime) ;

}

/**
 * Funtions that returns the size of label in the X Axis.
 */
function getSizeLabelX(data){
    
    var sizeLabelX = 16;
    
    // length : 30 --> 16
    // length : 40 --> 15
    // length : 50 --> 14
    if(data.length > 30 && data.length < 160){        
        sizeLabelX = 16 - ((data.length - 30)/10);
    }
    else if(data.length >= 140){
        sizeLabelX = 5;
    }
    
    return sizeLabelX;
}


/* 
 * Method that returns de label of a Mesuare, the measure is defined by the
 * property 
 */
function getLabel(propertyName, mMeasuresY){
    
    var rLabel = "";
    
    var measuresY_path = mMeasuresY.filter(function(v) {
        return v.property === propertyName; 
    });

    if(measuresY_path != null && measuresY_path.length > 0){
        rLabel = (measuresY_path[0]).title;
    }
    
    return rLabel;
}


/* 
 * Method that selects a Input Select with the parameter index, if
 * this index not exist then we select the first element.  
 */
function comboBoxSelectOption (controlSelect, index){
    
    controlSelect.node().selectedIndex = index;
    
    if(controlSelect.node().selectedIndex == -1){
        controlSelect.node().selectedIndex = 0;
    }
}

/* 
 * Method that returns the atribute 'checked' of the parameter checkBox.  
 */
function checkBoxGetChecked (checkBox){
    
    return checkBox[0][0].checked;
}

/*
 * Method that searchs a value in a collection and returns this object.
 */
function searchValue(collection, field, value, fieldReturn){
    
    var rObj;
    
    var arrayFound = collection.filter(function(v) { 
        return v[field] === value;  
    });
    
    if(arrayFound != null && arrayFound.length > 0){
        rObj = arrayFound[0];
    }
    
    if(rObj != null){
        rObj = rObj[fieldReturn];
    }
    
    return rObj;
}

/*
 * Method that searchs a value in the object's fields and returns this object.
 */
function searchValueObj(pObj, index, fieldReturn){
    
    var rObj;
    
    rObj = pObj[index];
    
    
    if(rObj != null){
        rObj = rObj[fieldReturn];
    }
    
    return rObj;
}




function removeAllChild(oObjectD3){
    
    // Remove all.
    while (oObjectD3.node().hasChildNodes()) {
        oObjectD3.node().removeChild(oObjectD3.node().firstChild);
    }
}

/**
 * 
 * INIT - Search Data in Visualization
 * 
 */
function initializeMeasureControlsSearch(){
    
    try{
        
        var controlSlot = d3.select("#customControlsSearch");
        if(controlSlot != null && controlSlot.node() != null){
            
            // First, Remove all.
            removeAllChild(controlSlot);
            
            controlSlot.append("p").text("Search Value:");// label
            controlSlot.append("datalist")            
                .attr("id", "idDataList")
                .selectAll("option")
                .data(data.map(function (d){return d[propertySearch]})).enter()   // options
                    .append("option")
                        .attr("value", function(d) {return d;} )
            ;
                            
            controlSlot.append("input")
                .attr("id", "idTbSearch")
                .attr("type", "text")
                .attr("list", "idDataList")
//                .on("change", function() {alert('change');})
//                .on("select", "alert('select');")
            ;    
            
            controlSlot.append("input")
                .attr("id", "idBtSearch")
                .attr("type", "button")
                .attr("value", "Search")
                .on("click", function() {search();})
            ;
            
            controlSlot.append("input")
                .attr("id", "idBtClean")
                .attr("type", "button")
                .attr("value", "Clean")
                .on("click", function() {clean();})
            ;
            controlSlot.append("div")
                .attr("id", "idDivResultSearch")
                .style("height", "140px")
                .style("overflow", "auto")
                .style("border-style", "groove")
            ;
        }
    }
    catch(e){
        alert(e);
    }
}

function search(){
    
    // Search Data
    var result = searchData();
    
    // Delete old results
    var divResult = d3.select("#idDivResultSearch");
    if(divResult != null && divResult.node() != null){

        removeAllChild(divResult);
    }
    
    // Show new results
    try{
        showDataInVisualization(result);
    }
    catch(e){
    }

    
    if(result.data != null){
        
        var divResult = d3.select("#idDivResultSearch");
        if(divResult != null){
     
            var htmlContent = "Label: " + result.data["label"] + "<br>";
            for (i in this.measures){
                var measure = this.measures[i];                
                htmlContent+= measure["title"] + ": " + result.data[measure["property"]] + "<br>";
            } 
            divResult.append("p").html(htmlContent);
        }
    }
    
    if(result.coData != null){
        
        var divResult = d3.select("#idDivResultSearch");
        if(divResult != null){
     
            for (indexData in result.coData){
                var iData = result.coData[indexData];
                var htmlContent = "Label: " + iData["label"] + "<br>";
                for (indexMeasure in this.measures){
                    var measure = this.measures[indexMeasure];                
                    htmlContent+= measure["title"] + ": " + iData[measure["property"]] + "<br>";
                } 
                divResult.append("p").html(htmlContent);
            }
        }
    }
}

function searchData(){
    
    var rObject = new Object();
    rObject.index = -1;
    rObject.data = null;
    rObject.coData = null;
    
    var textSearch = d3.select("#idTbSearch").property("value").toString();
    
    if(textSearch!=null && textSearch!=''){
        for (i in this.data)
        {  
            var obj = this.data[i];
            if(obj[propertySearch] == textSearch){
                rObject.index = i;
                rObject.data = this.data[i];
            }
        }
        
        if(rObject.index == -1){
            rObject.coData = this.data.filter(function(e){
                
                var textSearch = d3.select("#idTbSearch").property("value").toString().toUpperCase();
                var textLabel = e.label.toUpperCase()
                if(textLabel.contains(textSearch)){
                    return true;
                }
                else{
                    return false
                }
            })
        }
    }
    
    return rObject;
}

function clean(){
    
    var divResult = d3.select("#idDivResultSearch");
    if(divResult != null && divResult.node() != null){

        removeAllChild(divResult);
    }
    
    // Remove hover -- nv-point nv-point-0
    var arrayTbSearch = d3.select("#idTbSearch");
    arrayTbSearch.each(function(){
        var tbSearch = this;
        tbSearch.value = "";
    });    
}
/**
 * 
 * END - Search Data in Visualization
 * 
 */

