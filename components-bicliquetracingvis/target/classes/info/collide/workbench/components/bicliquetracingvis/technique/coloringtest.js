/**
 * Created by Batman on 12.05.2015.
 */

var metadata,
    nodes,
    edges,
    slices;

function initialize(file) {
    d3.json(file, function (e, d) {
            initializeData(d);
            initializeUI();
        }
    );
}

function initializeData(raw) {
    metadata = raw.metadata;
    nodes = raw.data.nodes;
    edges = raw.data.edges;

    initStartNodes();

    var timeSteps = getTimesteps(nodes);
    slices = new Array(timeSteps.length);
    for (var i = 0; i < slices.length; i++) {
        slices[i] = [];
    }

    nodes.forEach(function (d, i) {
        slices[timeSteps.indexOf(d.timestep)].push(d);
    });

    nodes.sort(function (d1, d2) {
        return d1.timestep - d2.timestep;
    });

    colorNodes();
}

function colorNodes() {
    nodes.forEach(function (actualNode) {
        var successorNodes = actualNode.successors,
            predecessorNodes = actualNode.predecessors;

        //calculate new color for the actual node if it is a result of merged nodes
        if (predecessorNodes.length > 1) {
            calculateMergedColor(predecessorNodes, actualNode);
        }

        //calculate color for all successorNodes
        successorNodes.forEach(function (sucNode, i) {
            if (sucNode.type === actualNode.type) {
                var difference = clj_fuzzy.metrics.jaccard(actualNode.members, sucNode.members);
                console.log(difference);
                var secondColor = d3.hsl(actualNode.color.h + calculateColorHueOffset(successorNodes.length, i), actualNode.color.s, actualNode.color.l);
                var interpolator = d3.interpolateHsl(actualNode.color, secondColor);
                console.log(sucNode);
                console.log(actualNode);
                sucNode.color = d3.hsl(interpolator(difference));
            }
        });
    })
}

function calculateMergedColor(predecessorNodes, actualNode) {
    var hValues = [],
        sValues = 0,
        lValues = 0,
        hX = 0.0,
        hY = 0.0;
    predecessorNodes.forEach(function (preNode) {
        hValues.push(preNode.color.h);
        sValues += preNode.color.s;
        lValues += preNode.color.l;
    });
    hValues.forEach(function (h) {
        hX += Math.cos(h);
        hY += Math.sin(h);
    });
    var meanAngle = Math.atan2(hY / hValues.length, hX / hValues.length),
        meanS = sValues / hValues.length,
        meanL = lValues / hValues.length;

    actualNode.color = d3.hsl(meanAngle, meanS, meanL);
}

function calculateColorHueOffset(numberOfFollowers, index) {
    return (90 + ((index + 1) * (180 / (numberOfFollowers + 1)))) | 0;
}

function getNodeById(id) {
    for (var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        if (node.id == id) {
            return node;
        }
    }
}

function initStartNodes() {
    var initNodes = [];
    nodes.forEach(function (node) {
        var isStartNode = true;
        node.successors = [];
        node.predecessors = [];
        for (var i = 0; i < edges.length; i++) {
            var edge = edges[i];
            if (edge.target == node.id) {
                var predecessorNode = getNodeById(edge.source);
                if (predecessorNode.type == node.type) {
                    node.predecessors.push(predecessorNode);
                    isStartNode = false;
                }
            } else if (edge.source == node.id) {
                var successorNode = getNodeById(edge.target);
                if (successorNode.type == node.type) {
                    node.successors.push(successorNode);
                }
            }
        }
        if (isStartNode) {
            initNodes.push(node);
        }
    });
    calculateStartNodeColors(initNodes);
}

function calculateStartNodeColors(startNodes) {
    var offset = ((360 / (startNodes.length))) % 360 | 0;
    startNodes.forEach(function (d, i) {
        d.color = d3.hsl((i) * offset, 1, 0.5);
    });
}

function getTimesteps(data) {
    var timesteps = [];
    data.forEach(function (d) {
        if (timesteps.indexOf(d.timestep) == -1) {
            timesteps.push(d.timestep);
        }
    });
    return timesteps;
}

function initializeUI() {
    $("#header").text(metadata.description);

    var content = $("#content");

    slices.forEach(function (slice) {
        var line = $("<div>").attr("class", "slice").text("timestep: " + slice[0].timestep).appendTo(content);
        slice.forEach(function (d) {
            var p = $("<p>").text(d.label).attr("id", d.id).appendTo(line);
            var div = $("<div>").attr("class", "color-box").css("background-color", d.color === undefined ? "black" : d.color.toString()).appendTo(line);
        });
    });
}
