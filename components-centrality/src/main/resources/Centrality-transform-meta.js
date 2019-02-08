    // function main(form, state, metaByInput) {
    //     return metaByInput.in_1;
    // }
    function main(form, state, metaByInput) {
        if (metaByInput.in_1.nodeproperties.some(n => n.property === 'dc')) {

            // if (metaByInput.in_1.properties && metaByInput.in_1.properties.nodeproperties &&
            //     !metaByInput.in_1.properties.nodeproperties.some(n => n.property === 'dc')) {

          debugger;
            var nodeproperty = {
                name: "Degree centrality",
                property: "dc",
                parsingtype: "double"
            };

            metaByInput.in_1.nodeproperties.push(nodeproperty);
        }
        // metaByInput.in_1.properties.nodeproperties.push(nodeproperty);
        return metaByInput.in_1;
    }