function main(form, state, metaByInput) {
    if (metaByInput.in_1.nodeproperties.some(n => n.property === 'dc')) {
        
        var nodeproperty = {
            name: "Degree centrality", 
            property: "dc", 
            parsingtype: "double"
        };
        
        metaByInput.in_1.nodeproperties.push(nodeproperty);
    }
 
    return metaByInput.in_1;
}