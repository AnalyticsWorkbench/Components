/**
 * Created by Batman on 01.08.2015.
 */
var clusterEvoChart = function () {
    'use strict';

    // chart variables
    var width = 1024 - 60,
        height = 600 - 60,
        margin = {top: 5, right: 5, bottom: 5, left: 5},
        lineWidth = 2,
        maxClusterWidth = 30,
        minClusterWidth = 10,
        maxNodeArea = 150,
        minNodeArea = 80,
        actorAreaScale = d3.scale.linear(),
        eventAreaScale = d3.scale.linear(),
        edgeWidthScale = d3.scale.linear(),
        minEdgeWidth = 2,
        maxEdgeWidth = 7,
        clicks = 0,
        CLICKDELAY = 400,
        DEFAULT_CLUSTER_OPACITY = 0.5,
        DEFAULT_NODE_EDGE_OPACITY = 1,
        pipeColor = d3.hsl(0, 0, 0),
        actorType = '1',
        eventType = '0',
        edgeWidthByMembers = false,
        timer, clusterData, edgeData, nodeData,
        resetMemberSelect;

    // value accessor functions
    var xValue = function (d) {
        return d.timestep;
    }, yValue = function (d) {
        return d.y;
    };

    // actorMembersLength accessor function
    var actorMembersLength = function (d) {
        if (d.type == actorType) {
            return d.members.length | 0;
        }
    };

    // edgeWidth accessor function
    var edgeWidth = function (d) {
        return d.memberCount;
    };

    // eventMembersLength accessor function
    var eventMembersLength = function (d) {
        if (d.type == eventType) {
            return d.members.length | 0;
        }
    };

    // reset Highlighting accessor function
    var resetHighlighting = function () {
        d3.selectAll('.group, .edge')
            .transition()
            .style('opacity', DEFAULT_NODE_EDGE_OPACITY);
        d3.selectAll('.cluster')
            .transition()
            .style('opacity', DEFAULT_CLUSTER_OPACITY);
    };

    /**
     * Highlights nodes containing the given member.
     * @param member the member to highlight
     * @param resetOnly if set to true, only a reset of the highlighting is done
     */
    var highlightByMember = function (member, resetOnly) {
        resetHighlighting();
        if (resetOnly) {
            return;
        }
        hideAll();
        nodeData.forEach(function (d) {
            if (d.members.indexOf(member) != -1) {
                highlightSingleNode(d);
            }
        });
        highlightEdgesBetweenHighlightedNodes(member);
    };

    /**
     * Highlits edges between nodes, which contain the given member
     * @param member
     */
    function highlightEdgesBetweenHighlightedNodes(member) {
        edgeData.forEach(function (d) {
            var sourceNode = getNodeById(nodeData, d.source);
            if (typeof sourceNode !== 'undefined' && sourceNode.members.indexOf(member) != -1) {
                var targetNode = getNodeById(nodeData, d.target);
                if (typeof targetNode !== 'undefined' && targetNode.members.indexOf(member) != -1) {
                    highlightEdge(d.source, d.target);
                }
            }
        })
    }

    var updateEdgeWidthByMembers = function (widthByMembers) {
        edgeWidthByMembers = widthByMembers;
        d3.selectAll('.edge')
            .attr('stroke-width', function (d) {
                if (edgeWidthByMembers) {
                    return edgeWidthScale(edgeWidth(d));
                }
                return lineWidth;
            })
            .attr('marker-end', 'url(#arrow_head)');
    };

    // onClick accessor function
    var onClick = function (d) {
        var $popup = $('#popup-' + d.id);
        if (!$popup.length) {
            var title = d.label;
            if (d.successors.length > 0 || d.predecessors.length > 0) {
                title += ' (' + xValue(d) + ')'
            }
            var $newPopup = $('<div>').attr('id', 'popup-' + d.id).attr('title', title),
                $memberHeader = $('<p>').text('Members(' + d.members.length + '):'),
                $list = $('<ul>');
            d.members.forEach(function (member) {
                $list.append($('<li>').text(member));
            });
            var members = $('<p>').html($list);
            $newPopup.append($memberHeader, members);
            $newPopup.dialog({
                autoOpen: false,
                height: 200,
                width: 300
            });
            $newPopup.dialog('open');
        } else {
            $popup.dialog('open');
        }
    };

    // dblClick accessor function
    var onDblClick = function (d) {
        try {
            resetMemberSelect();
        } catch (error) {
            console.log('resetMemberSelect function is not defined');
        }
        hideAll();
        // select group element
        highlightPredecessorNodes(d);
        highlightNodeAndCluster(d);
        highlightSuccessorNodes(d);
        // select cluster element
        var cluster = getClusterById(d.clusterId);
        d3.select('#cluster-' + cluster.id)
            .transition()
            .style('opacity', 0.5);
    };

    function hideAll() {
        var allElements = d3.selectAll('.group, .cluster, .edge');
        allElements.transition()
            .style('opacity', 0.2);
    }

    /**
     * Highlights all successors of the given group
     * @param group
     */
    function highlightSuccessorNodes(group) {
        group.successors.forEach(function (d) {
            highlightNodeAndCluster(d);
            highlightEdge(group.id, d.id);
            highlightSuccessorNodes(d);
        });
        if (group.tempSuccessors) {
            group.tempSuccessors.forEach(function (d) {
                highlightNodeAndCluster(d);
                highlightEdge(group.id, d.id);
                highlightSuccessorNodes(d);
            });
        }
    }

    /**
     * Highlights all predecessors of the given group
     * @param group
     */
    function highlightPredecessorNodes(group) {
        group.predecessors.forEach(function (d) {
            highlightNodeAndCluster(d);
            highlightEdge(d.id, group.id);
            highlightPredecessorNodes(d);
        });
    }

    /**
     * Highlights the Edge with the given sourceId and targetId
     * @param sourceId
     * @param targetId
     */
    function highlightEdge(sourceId, targetId) {
        d3.select('#edge_' + sourceId + '_' + targetId)
            .transition()
            .style('opacity', DEFAULT_NODE_EDGE_OPACITY);
    }

    /**
     * Highlights the whole cluster of the given node
     * @param node
     */
    function highlightNodeAndCluster(node) {
        var cluster = getClusterById(node.clusterId);
        var groupSelection = d3.select('#type' + cluster.actorGroup.type + '-group' + cluster.actorGroup.id);
        groupSelection[0].push(d3.select('#type' + cluster.eventGroup.type + '-group' + cluster.eventGroup.id).node());
        groupSelection
            .transition()
            .style('opacity', DEFAULT_NODE_EDGE_OPACITY);
        d3.select('#cluster-' + cluster.id)
            .transition()
            .style('opacity', DEFAULT_CLUSTER_OPACITY);
    }

    /**
     * Highlights the given node
     * @param node
     */
    function highlightSingleNode(node) {
        d3.select('#type' + node.type + '-group' + node.id)
            .transition()
            .style('opacity', DEFAULT_NODE_EDGE_OPACITY);
    }

    function getClusterById(clusterId) {
        for (var i = 0; i < clusterData.length; i++) {
            var cl = clusterData[i];
            if (cl.id == clusterId) {
                return cl;
            }
        }
    }

    /**
     *
     * @param array array to search in
     * @param nodeId the id to search for
     * @returns {object} returns the node with the given id in the given array
     */
    function getNodeById(array, nodeId) {
        for (var i = 0; i < array.length; i++) {
            var n = array[i];
            if (n.id == nodeId) {
                return n;
            }
        }
        console.log('node not found: ' + nodeId);
    }

    function chart(selection) {
        selection.each(function (data) {
                nodeData = data.nodes;
                edgeData = data.edges;
                clusterData = data.clusters;

                var actorMinMax = d3.extent(nodeData, actorMembersLength);
                actorAreaScale.domain(actorMinMax).range([minNodeArea, maxNodeArea]);
                var eventMinMax = d3.extent(nodeData, eventMembersLength);
                eventAreaScale.domain(eventMinMax).range([minNodeArea, maxNodeArea]);

                var edgeWidthMinMax = d3.extent(edgeData, edgeWidth);
                edgeWidthScale.domain(edgeWidthMinMax).range([minEdgeWidth, maxEdgeWidth]);

                // bind data to the svg element
                var div = d3.select(this),
                    svg = div.selectAll('svg').data([nodeData]);

                // append svg and initialize it
                svg.enter().append('svg')
                    .call(chart.svgInit);

                // add markers to defs
                var defs = svg.append('defs'),
                    arrow_head = defs.append('marker')
                        .attr('id', 'arrow_head')
                        .attr('refX', '6')
                        .attr('refY', '3')
                        .attr('markerUnits', 'strokeWidth')
                        .attr('markerWidth', '9')
                        .attr('markerHeight', '9')
                        .attr('orient', 'auto');
                arrow_head.append('path')
                    .attr('d', 'M 0,0 l 6,3 l -6,3 z');

                // base group element
                var g = svg.select('g.chart-content');

                // min/max values for the axes
                var firstLastTimeStep = d3.extent(nodeData, xValue);
                var minMaxYPos = d3.extent(nodeData, yValue);

                // scales
                var xScale = d3.scale.linear()
                        .domain([firstLastTimeStep[0], firstLastTimeStep[1]])
                        .range([0, width - margin.left - margin.right]),
                    yScale = d3.scale.linear()
                        .domain([minMaxYPos[0], minMaxYPos[1]])
                        .range([height - margin.top - margin.bottom, 0]);

                var xAxis = d3.svg.axis()
                    .scale(xScale)
                    .tickFormat(d3.format('d'))
                    .ticks(firstLastTimeStep[1] - firstLastTimeStep[0] + 1)
                    .orient('bottom');
                svg.append('g')
                    .attr('class', 'x-axis')
                    .attr('transform', 'translate(' + margin.left + ',' + height + ')')
                    .call(xAxis);

                // draw clusters
                var clusters = g.selectAll('ellipse').data(clusterData);
                clusters.enter().append('ellipse')
                    .attr('class', 'cluster')
                    .attr('id', function (d) {
                        return 'cluster-' + d.id;
                    })
                    .attr('cx', function (d) {
                        return xScale(xValue(d.actorGroup));
                    })
                    .attr('cy', function (d) {
                        var yActor = yScale(yValue(d.actorGroup)),
                            yResource = yScale(yValue(d.eventGroup));
                        return yResource + Math.abs(yActor - yResource) / 2;
                    })
                    .attr('rx', Math.min(Math.max((Math.abs(xScale('0') - xScale('1')) * 0.3), minClusterWidth), maxClusterWidth))
                    .attr('ry', function (d) {
                        var yActor = yScale(yValue(d.actorGroup)),
                            yResource = yScale(yValue(d.eventGroup));
                        return Math.abs(yActor - yResource) * 0.7;
                    })
                    .style('fill', 'grey')
                    .style('opacity', 0.5);

                // draw edges
                var edges = g.selectAll('line').data(edgeData);
                edges.enter().append('line')
                    .attr('class', 'edge')
                    .attr('id', function (d) {
                        return 'edge_' + d.source + '_' + d.target;
                    })
                    .attr('x1', function (d) {
                        return xScale(xValue(getNodeById(nodeData, d.source)));
                    })
                    .attr('x2', function (d) {
                        return xScale(xValue(getNodeById(nodeData, d.target)));
                    })
                    .attr('y1', function (d) {
                        return yScale(yValue(getNodeById(nodeData, d.source)));
                    })
                    .attr('y2', function (d) {
                        return yScale(yValue(getNodeById(nodeData, d.target)));
                    })
                    .attr('title', function (d) {
                        return d.label;
                    })
                    .attr('stroke', '#000')
                    .attr('stroke-opacity', 0.5)
                    .attr('stroke-width', function (d) {
                        if (edgeWidthByMembers) {
                            return edgeWidthScale(edgeWidth(d));
                        }
                        return lineWidth;
                    })
                    .attr('marker-end', 'url(#arrow_head)')
                    .on('mouseover', function (d) {
                        d3.select(this).transition()
                            .attr('stroke-width', edgeWidthByMembers ? edgeWidthScale(edgeWidth(d)) * 1.5 : lineWidth * 1.5);
                    })
                    .on('mouseout', function (d) {
                        d3.select(this).transition()
                            .attr('stroke-width', edgeWidthByMembers ? edgeWidthScale(edgeWidth(d)) : lineWidth);
                    });
                $('.edge').tooltip({track: true});

                // draw groups
                var symbol = function buildSymbol(d) {
                    var gGroup = d3.select(this).append('g'),
                        lengthUnit;
                    switch (d.type) {
                        case actorType:
                            var radius = getRadius(d);
                            lengthUnit = (2 * radius) / 5;
                            gGroup.append('circle')
                                .attr('cx', 0)
                                .attr('cy', 0)
                                .attr('r', radius)
                                .attr('fill', function () {
                                    return d.color;
                                });

                            // right pipe
                            if (d.successors == undefined || d.successors.length < 1) {
                                gGroup.append('line')
                                    .attr('x1', radius + 3)
                                    .attr('y1', -radius)
                                    .attr('x2', radius + 3)
                                    .attr('y2', radius)
                                    .attr('stroke-width', lengthUnit / 2)
                                    .attr('stroke', pipeColor);
                            }

                            // left pipe
                            if (d.predecessors == undefined || d.predecessors.length < 1) {
                                gGroup.append('line')
                                    .attr('x1', -radius - 3)
                                    .attr('y1', -radius)
                                    .attr('x2', -radius - 3)
                                    .attr('y2', radius)
                                    .attr('stroke-width', lengthUnit / 2)
                                    .attr('stroke', pipeColor);
                            }
                            break;
                        case eventType:
                            var rectLength = getSideLength(d);
                            lengthUnit = rectLength / 5;
                            gGroup.append('rect')
                                .attr('x', -rectLength / 2)
                                .attr('y', -rectLength / 2)
                                .attr('width', rectLength)
                                .attr('height', rectLength)
                                .attr('fill', function () {
                                    return d.color;
                                });

                            //right pipe
                            if (d.successors == undefined || d.successors.length < 1) {
                                gGroup.append('line')
                                    .attr('x1', (rectLength / 2) + 3)
                                    .attr('y1', -rectLength / 2)
                                    .attr('x2', (rectLength / 2) + 3)
                                    .attr('y2', rectLength / 2)
                                    .attr('stroke-width', lengthUnit / 2)
                                    .attr('stroke', pipeColor);
                            }

                            // left pipe
                            if (d.predecessors == undefined || d.predecessors.length < 1) {
                                gGroup.append('line')
                                    .attr('x1', (-rectLength / 2) - 3)
                                    .attr('y1', -rectLength / 2)
                                    .attr('x2', (-rectLength / 2) - 3)
                                    .attr('y2', rectLength / 2)
                                    .attr('stroke-width', lengthUnit / 2)
                                    .attr('stroke', pipeColor);
                            }
                            break;
                    }
                    return gGroup.node();
                };

                var groups = g.selectAll('.group').data(nodeData);
                groups.enter().append(symbol)
                    .attr('class', 'group')
                    .attr('transform', function (d) {
                        return 'translate(' + xScale(xValue(d)) + ',' + yScale(yValue(d)) + ')';
                    })
                    .attr('id', function (d) {
                        return 'type' + d.type + '-group' + d.id;
                    })
                    .attr('title', function (d) {
                        var title = d.label;
                        if (d.successors.length > 0 || d.predecessors.length > 0) {
                            title += ' (' + xValue(d) + ')'
                        }
                        return title;
                    })
                    .on('click', function (d) {
                        // workaround for distinguishing single and double click
                        clicks++;
                        d3.event.stopPropagation();
                        if (clicks === 1) {
                            timer = setTimeout(function () {
                                clicks = 0;
                                onClick(d);
                            }, CLICKDELAY);
                        } else {
                            clearTimeout(timer);
                            clicks = 0;
                            onDblClick(d);
                        }
                    });
                $('.group').tooltip({track: true});
            }
        );
    }

    function getRadius(node) {
        if (node.type == actorType) {
            var area = actorAreaScale(actorMembersLength(node));
            return Math.sqrt(area / Math.PI);
        }
        return 0;
    }

    function getSideLength(node) {
        if (node.type == eventType) {
            var area = eventAreaScale(eventMembersLength(node));
            return Math.sqrt(area);
        }
        return 0;
    }

    // init svg element
    chart.svgInit = function (svg) {
        svg.attr('width', width)
            .attr('height', height)
            .style('padding', '30px');

        var g = svg.append('g')
            .attr('class', 'chart-content')
            .attr('transform', 'translate(' + [margin.left, margin.top] + ')')
            .on('click', function () {
                resetHighlighting();
                resetMemberSelect();
            });

        g.append('rect')
            .attr('width', width - margin.left - margin.right)
            .attr('height', height - margin.top - margin.bottom)
            .attr('fill', 'white');
    };

    // Accessor Methods

    // resetMemberSelect Accessor
    chart.resetMemberSelect = function (accessorFunction) {
        resetMemberSelect = accessorFunction;

        return chart;
    };

    // actorType Accessor
    chart.actorType = function (value) {
        if (!arguments.length) {
            return actorType;
        }
        actorType = value;
        return chart;
    };

    // eventType Accessor
    chart.eventType = function (value) {
        if (!arguments.length) {
            return eventType;
        }
        eventType = value;
        return chart;
    };

    // edgeWidthByMembers Accessor
    chart.edgeWidthByMembers = function (value) {
        if (!arguments.length) {
            return edgeWidthByMembers;
        }
        edgeWidthByMembers = value;
        return chart;
    };

    // reset highlighting Accessor
    chart.resetHighlighting = resetHighlighting;

    // highlightByMember Accessor
    chart.highlightByMember = highlightByMember;

    // updateEdgeWidthByMembers Accessor
    chart.updateEdgeWidthByMembers = updateEdgeWidthByMembers;


    return chart;
};