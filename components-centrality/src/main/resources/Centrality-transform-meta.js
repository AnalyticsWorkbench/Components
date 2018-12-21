function main(form, state, metaByInput) {
    
    if (metaByInput.in_1.properties && metaByInput.in_1.properties.nodeproperties && 
            !metaByInput.in_1.properties.nodeproperties.some(n => n.property === 'dc')) {
        
        var nodeproperty = {
            name: "Degree centrality", 
            property: "dc", 
            parsingtype: "double"
        };
        
        metaByInput.in_1.properties.nodeproperties.push(nodeproperty);
    }
 
    return metaByInput.in_1;
}