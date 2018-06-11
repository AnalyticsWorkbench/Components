var table;
var tbody;
var colors=['#E96500','#A5CC7A',"#FFF1A2"];

function Init(){
    table = document.getElementById("resulttable");
    tbody = table.getElementsByTagName("tbody")[0];
    
    console.log(rawData);
   //var logs =JSON.parse(rawData[0].filedata).logs;
   if(rawData.items !== undefined){
    var logs =rawData.items;

    for(var i=0;i<logs.length;i++){

        addTableRow(logs[i]);
        if(i % 10 === 0 && i!== 0){
            addFooter();
        }
    }

    colorRows(3);
}else{
    console.log("null");
            addTableRow(rawData);

}
   
}

function addTableRow(json){
    var jsonData = json;
    
    var row = document.createElement('tr');

    var td1 = document.createElement('td');
    td1.innerHTML = new Date(jsonData.published).toLocaleString();
    row.appendChild(td1);

    td1 = document.createElement('td');
    td1.innerHTML = jsonData.verb;
    row.appendChild(td1);

    td1 = document.createElement('td');
    if(jsonData.actor != undefined){
    	td1.innerHTML = "<ul><li>"+jsonData.actor.objectType+"</li><li>"+jsonData.actor.id+"</li></ul>";
    }
    row.appendChild(td1);

    td1 = document.createElement('td');
    if(jsonData.object != undefined){
	if (jsonData.object.content != undefined) {
    		td1.innerHTML = "<ul><li>"+jsonData.object.objectType+"</li><li>"+jsonData.object.content+"</li></ul>";
	} else if (jsonData.object.title != undefined) {
		td1.innerHTML = "<ul><li>"+jsonData.object.objectType+"</li><li>"+jsonData.object.title+"</li></ul>";
	} else if (jsonData.object.name != undefined) {
		td1.innerHTML = "<ul><li>"+jsonData.object.objectType+"</li><li>"+jsonData.object.name+"</li></ul>";
	} else {
		td1.innerHTML = "<ul><li>"+jsonData.object.objectType+"</li><li>"+jsonData.object.id+"</li></ul>";
	}
    }
    row.appendChild(td1);
 
    td1 = document.createElement('td');
    if(jsonData.target != undefined){
	if (jsonData.target.displayName != undefined) {
		td1.innerHTML = "<ul><li>"+jsonData.target.objectType+"</li><li>"+jsonData.target.displayName+"</li></ul>";
	} else if (jsonData.target.title != undefined) {	
		td1.innerHTML = "<ul><li>"+jsonData.target.objectType+"</li><li>"+jsonData.target.title+"</li></ul>";
	} else if (jsonData.target.name != undefined) {
		td1.innerHTML = "<ul><li>"+jsonData.target.objectType+"</li><li>"+jsonData.target.name+"</li></ul>";
	} else {
		td1.innerHTML = "<ul><li>"+jsonData.target.objectType+"</li><li>"+jsonData.target.id+"</li></ul>";
	}
    } 
    row.appendChild(td1);
    
    td1 = document.createElement('td');
    if(jsonData.generator != undefined){
	if (jsonData.generator.displayName != undefined) {
    		td1.innerHTML = "<ul><li>"+jsonData.generator.objectType+"</li><li>"+jsonData.generator.displayName+"</li><li>"+jsonData.generator.url+"</li></ul>";
	} else if (jsonData.generator.title != undefined) {
		td1.innerHTML = "<ul><li>"+jsonData.generator.objectType+"</li><li>"+jsonData.generator.title+"</li><li>"+jsonData.generator.url+"</li></ul>";
	} else if (jsonData.generator.name != undefined) {
		td1.innerHTML = "<ul><li>"+jsonData.generator.objectType+"</li><li>"+jsonData.generator.name+"</li><li>"+jsonData.generator.url+"</li></ul>";
	} else if (jsonData.generator.displayName != undefined) {
		td1.innerHTML = "<ul><li>"+jsonData.generator.objectType+"</li><li>"+jsonData.generator.id+"</li><li>"+jsonData.generator.url+"</li></ul>";
	} else {
	}
    }
    row.appendChild(td1);
    
    td1 = document.createElement('td');
    if(jsonData.provider != undefined){
    	td1.innerHTML = jsonData.provider.objectType;
    }
    row.appendChild(td1);
    //colorRow(jsonData,row);

    tbody.appendChild(row);

}

function highLightVerb(contentText,position){
  
    var trs =  tbody.getElementsByTagName("tr");

    for(var i=0;i<trs.length;i++){
        var verb = trs[i].childNodes[position];
        console.log(verb);
        if(verb.innerHTML === contentText){
            trs[i].style.backgroundColor ="#E96500";
        }
    }
}

function addFooter(){
    var footer = document.createElement('tr');
    footer.innerHTML ="<td>Published</td><td>Verb</td><td>Actor</td><td>Object</td><td>Target</td><td>Generator</td><td>Provider</td>";
    
    footer.className="midHeader"

    tbody.appendChild(footer);
    }

function colorRow(jsonData, row){
   if(jsonData.verb === 'add'){
        row.style.backgroundColor ="#E96500";
    }else if(jsonData.verb === 'delete'){
        row.style.backgroundColor ="#A5CC7A";
    }else if(jsonData.verb === 'update'){
        row.style.backgroundColor ="#FFF1A2";
    }  
}

function colorRows(column){
    var trs =  tbody.getElementsByTagName("tr");
    for(var i=0;i<trs.length;i++){
        var verb = trs[i].childNodes[column];
        
        if(column ===1){
             var verbs = getVerbs();
             for(var y=0;y<verbs.length;y++){
                if(verb.innerHTML === verbs[y]){
                 trs[i].style.backgroundColor = colors[y];
                 break;
                } 
             }    
        }
    }
}

function getVerbs(){
    var verbs =[];
    for(var i=0;i<rawData.length;i++){
        var verb =JSON.parse(rawData[i].filedata).verb;
        if(indexOf.call(verbs,verb) < 0){
            verbs.push(verb);
        }
    }
    return verbs;
}
function getObjects(){
    var objects =[];
    for(var i=0;i<rawData.length;i++){
        var object =JSON.parse(rawData[i].filedata).object.objectType;
        if(indexOf.call(objects,object) < 0){
            objects.push(object);
        }
    }
    return objects;
}
var indexOf = function(needle) {
    if(typeof Array.prototype.indexOf === 'function') {
        indexOf = Array.prototype.indexOf;
    } else {
        indexOf = function(needle) {
            var i = -1, index = -1;

            for(i = 0; i < this.length; i++) {
                if(this[i] === needle) {
                    index = i;
                    break;
                }
            }

            return index;
        };
    }

    return indexOf.call(this, needle);
};
