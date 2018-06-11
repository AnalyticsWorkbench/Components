/**
 * Created by Batman on 24.07.2015.
 */
function edbc() {
    'use strict';
    var edges, nodes, initNodes, initEdges, clusters, chart, currentActorMinGroupSize, currentEventMinGroupSize,
        currentRemoveSingleNodes, currentEdgeWidthByMembers,
        allMembers = [],
        actorGroupSize = {
            min: 0,
            max: 0
        },
        eventGroupSize = {
            min: 0,
            max: 0
        },
        minActorGroupSize = 0,
        minEventGroupSize = 0,
        actorType = '1',
        eventType = '0',
        occ = {},
        init = true,
        StepSpace = function () {
            this.slots = [];

            /**
             *
             * @param slot
             * @returns {Object} the (cluster-)object at the given slot in the stepspace
             */
            this.getObjectAtSlot = function (slot) {
                for (var i = 0; i < this.slots.length; i++) {
                    var positionObject = this.slots[i];
                    if (positionObject && positionObject.slot == slot) {
                        return positionObject;
                    }
                }
                return undefined;
            };

            /**
             * adds a the cluster at the given slot
             * @param slot
             * @param cluster
             */
            this.addCluster = function (slot, cluster) {
                this.slots.push({slot: slot, cluster: cluster});
            };

            /**
             *
             * @returns {Number} the number of cluster in this stepspace
             */
            this.size = function () {
                return this.slots.length;
            };

            /**
             *
             * @param index
             * @returns {Object} the slot at the given index
             */
            this.get = function (index) {
                return this.slots[index];
            };

            /**
             * swaps the objects at the two given slots
             * @param s1
             * @param s2
             */
            this.swapSlots = function (s1, s2) {
                var index1 = findIndexOfSlot(s1, this.slots),
                    index2 = findIndexOfSlot(s2, this.slots);
                if (typeof index1 !== 'undefined' && typeof index2 !== 'undefined') {
                    this.slots[index1].slot = s2;
                    this.slots[index2].slot = s1;
                } else if (typeof index1 === 'undefined' && typeof index2 !== 'undefined') {
                    this.slots[index2].slot = s1;
                } else if (typeof index1 !== 'undefined' && typeof index2 === 'undefined') {
                    this.slots[index1].slot = s2;
                }
            };

            /**
             *
             * @returns {Array} an array with two numbers. First number is the lowest used slot, the second number is the highest used slot
             */
            this.getSlotRange = function () {
                this.slots.sort(function (s1, s2) {
                    return s1.slot - s2.slot;
                });
                if (this.slots.indexOf(undefined) != -1) {
                    return [this.slots[0].slot, this.slots[this.slots.indexOf(undefined) - 1].slot];
                }
                return [this.slots[0].slot, this.slots[this.slots.length - 1].slot];
            };

            /**
             *
             * @param s
             * @param slotArray
             * @returns {Number|undefined} the index of the given slot in the slotArray
             */
            function findIndexOfSlot(s, slotArray) {
                for (var i = 0; i < slotArray.length; i++) {
                    var obj = slotArray[i];
                    if (obj && obj.slot == s) {
                        return i;
                    }
                }
                return undefined;
            }

        },
        /**
         * a line equation like y=mx+n between the given points (x1,y1) and (x2,y2)
         * @param x1 x-coordinate of the first point
         * @param y1 y-coordinate of the first point
         * @param x2 x-coordinate of the second point
         * @param y2 y-coordinate of the second point
         * @param id the id of the corresponding edge
         * @param sourceId the id of node at the first point
         * @param targetId the id of node at the second point
         * @constructor
         */
        LineEquation = function (x1, y1, x2, y2, id, sourceId, targetId) {
            this.m = (y2 - y1) / (x2 - x1);
            this.n = (-1 * this.m * x1) + y1;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.id = id;
            this.source = sourceId;
            this.target = targetId;

            this.calculateY = function (x) {
                return (this.m * x) + this.n;
            };

            /**
             *
             * @param x
             * @returns {boolean} true if x lies between x1 and x2, otherwise false
             */
            this.isValidXValue = function (x) {
                if (x1 < x2) {
                    return x1 <= x && x2 >= x;
                } else {
                    return x2 <= x && x1 >= x;
                }
            };

            /**
             *
             * @param y
             * @returns {boolean} true if y lies between y1 and y2, otherwise false
             */
            this.isValidYValue = function (y) {
                if (y1 < y2) {
                    return y1 <= y && y2 >= y;
                } else {
                    return y2 <= y && y1 >= y;
                }
            }
        },
        resetMemberSelect = function () {
            var $sel = $('#memberSelect');
            $sel[0].selectedIndex = 0;
            $sel.selectmenu('refresh');
            $sel.val()
        };


    function start() {
        initOptions();
        d3.json('data.json', function (error, raw) {
            if (error) {
                console.error('Error while parsing the file.');
                $('#chart').text('Error while parsing the file - No data found');
                return error;
            }
            // set the title
            var title = raw.metadata.title;
            if (typeof title !== 'undefined' && title !== 'undefined') {
                var $title = $('<div>').css({'width': '100%', 'margin': '0 auto', 'text-align': 'center'});
                $title.text(title);
                $('#header').append($title);
            }
            initEdges = raw.data.edges;
            initNodes = raw.data.nodes;
            if (typeof initNodes === 'undefined' || initNodes.length === 0) {
                $('#chart').text('No data found');
                return;
            }
            initNodes.sort(function (n1, n2) {
                return n1.id - n2.id;
            });
            initNodes.sort(function (n1, n2) {
                return n1.timestep - n2.timestep;
            });
            nodes = initNodes;
            edges = initEdges;
            setupNodeTypes();
            initializeNodes();
            detectCluster();
            rankNodes();
            var space = calculatePositions();
            var optimize = optimizeIntersections(space);
            while (optimize) {
                optimize = optimizeIntersections(space);
            }
            calculateEdgeWidths();
            initGraph();
            updateOptions();
            init = false;
        });
    }

    /**
     * detect the two used node types and store them in actorType and eventType
     */
    function setupNodeTypes() {
        for (var i = 0; i < initEdges.length; i++) {
            var e = initEdges[i],
                targetNode = getNodeById(e.target, true),
                sourceNode = getNodeById(e.source, true);
            if (sourceNode.type != targetNode.type) {
                actorType = sourceNode.type;
                eventType = targetNode.type;
                return;
            }
        }
    }

    /**
     * updates the ui elements in the options panel
     */
    function updateOptions() {
        $('#minActorGroupSizeSlider').slider({
            min: actorGroupSize.min,
            max: actorGroupSize.max
        });
        currentActorMinGroupSize = actorGroupSize.min;
        $('#minActorGroupSize').val(actorGroupSize.min);
        $('#minEventGroupSizeSlider').slider({
            min: eventGroupSize.min,
            max: eventGroupSize.max
        });
        currentEventMinGroupSize = eventGroupSize.min;
        $('#minEventGroupSize').val(eventGroupSize.min);

        $('#resetHighlightButton').click(function () {
            resetMemberSelect();
            chart.resetHighlighting();
        });

        var memberSelect = $('#memberSelect');
        if (init) {
            memberSelect.append($('<option>').attr('value', '-1').text('No filter'));
            allMembers.forEach(function (d) {
                memberSelect.append($('<option>').attr('value', d).text(d));
            });
        }
        memberSelect.selectmenu('refresh');
        chart.resetMemberSelect(resetMemberSelect);
    }

    /**
     * calculates the width of each edge, based on the members which have the source node and target node in common
     */
    function calculateEdgeWidths() {
        edges.forEach(function (edge) {
            var source = getNodeById(edge.source),
                target = getNodeById(edge.target),
                counter = 0;
            source.members.forEach(function (member) {
                if (target.members.indexOf(member) !== -1) {
                    counter++;
                }
            });
            edge.memberCount = counter;
            edge.label = edge.memberCount + '/' + source.members.length + ' members';
        })
    }

    /**
     * initialize the ui in the options panel
     */
    function initOptions() {
        var $minActorGroupSizeSlider = $('#minActorGroupSizeSlider');
        $minActorGroupSizeSlider.slider({
            min: 0,
            max: 0,
            slide: function (event, ui) {
                $('#minActorGroupSize').val(ui.value);
            }
        });
        $('#minActorGroupSize').val($minActorGroupSizeSlider.slider('value'));

        var $minEventGroupSizeSlider = $('#minEventGroupSizeSlider');
        $minEventGroupSizeSlider.slider({
            min: 0,
            max: 0,
            slide: function (event, ui) {
                $('#minEventGroupSize').val(ui.value);
            }
        });
        $('#minEventGroupSize').val($minEventGroupSizeSlider.slider('value'));

        $('#updateButton').button()
            .click(function () {
                minActorGroupSize = parseInt($('#minActorGroupSize').val());
                minEventGroupSize = parseInt($('#minEventGroupSize').val());
                var removeSingleNodes = $('#removeSingleNodesCheckbox').prop('checked'),
                    edgeWidthByMembers = $('#edgeWidthCheckbox').prop('checked');
                if (minActorGroupSize != currentActorMinGroupSize || minEventGroupSize != currentEventMinGroupSize
                    || removeSingleNodes != currentRemoveSingleNodes) {
                    currentActorMinGroupSize = minActorGroupSize;
                    currentEventMinGroupSize = minEventGroupSize;
                    currentRemoveSingleNodes = removeSingleNodes;
                    currentEdgeWidthByMembers = edgeWidthByMembers;
                    cleanCanvas();
                    occ = {};
                    filterNodesBySize(minActorGroupSize, minEventGroupSize, removeSingleNodes);
                    if (nodes.length === 0) {
                        $('#chart').text('No data found');
                        return;
                    } else {
                        $('#chart').text('');
                    }
                    detectCluster();
                    rankNodes();
                    var space = calculatePositions();
                    var optimize = optimizeIntersections(space);
                    while (optimize) {
                        optimize = optimizeIntersections(space);
                    }
                    calculateEdgeWidths();
                    initGraph(edgeWidthByMembers);
                    chart.resetMemberSelect(resetMemberSelect);
                } else if (currentEdgeWidthByMembers != edgeWidthByMembers) {
                    currentEdgeWidthByMembers = edgeWidthByMembers;
                    chart.updateEdgeWidthByMembers(currentEdgeWidthByMembers);
                }
                var selectedMember = $('#memberSelect option:selected').val();
                chart.highlightByMember(selectedMember, selectedMember === '-1');
            });
        $('#resetHighlightButton').button();

        $('#optionsButton')
            .button({
                icons: {primary: 'ui-icon-gear'}
            });

        $('#memberSelect').selectmenu()
            .selectmenu({
                width: "100%"
            })
            .selectmenu("menuWidget")
            .addClass("overflow");
        $('#removeSingleNodesCheckbox').prop('checked', false);
        currentRemoveSingleNodes = false;

        $('#edgeWidthCheckbox').prop('checked', false);
        currentEdgeWidthByMembers = false;

        $('#header').clingify();
        $.slidebars();

    }

    function cleanCanvas() {
        $('#chart').empty();
    }

    /**
     * Filters out all clusters, which two groups do not have a minimum size of actorSize AND eventSize. Updates the edges.
     * After this call nodes only contains nodes with actor groups with at least actorSize members and
     * event groups containing at least eventSize members.
     * @param actorSize the minimal size of actor groups
     * @param eventSize the minimal size of event groups
     * @param removeSingleNodes if true, removes cluster with no predecessor and no successor
     */
    function filterNodesBySize(actorSize, eventSize, removeSingleNodes) {
        var newNodes = [];
        initNodes.forEach(function (d) {
            // reset position;
            d.y = undefined;
            d.tempSuccessors = undefined;
            var corNode = getCorrespondingNode(d);
            switch (d.type) {
                case actorType:
                    if ((!removeSingleNodes
                            // filter single nodes
                        || d.predecessors.length > 0 || d.successors.length > 0 || corNode.predecessors.length > 0 || corNode.successors.length > 0)
                            // filter by member size
                        && (d.members.length >= actorSize && corNode.members.length >= eventSize)) {
                        newNodes.push(d);
                    }
                    break;
                case eventType:
                    if ((!removeSingleNodes
                            // filter single nodes
                        || d.predecessors.length > 0 || d.successors.length > 0 || corNode.predecessors.length > 0 || corNode.successors.length > 0)
                            // filter by member size
                        && (d.members.length >= eventSize && corNode.members.length >= actorSize)) {
                        newNodes.push(d);
                    }
            }
        });
        nodes = newNodes;
        nodes.sort(function (n1, n2) {
            return n1.id - n2.id;
        });
        nodes.sort(function (n1, n2) {
            return n1.timestep - n2.timestep;
        });
        updateEdges();
    }

    /**
     *
     * @param node
     * @returns {Object} returns the corresponding event node if node is an actor node,
     * returns the corresponding actor node if node is an event node
     */
    function getCorrespondingNode(node) {
        for (var i = 0; i < initEdges.length; i++) {
            var edge = initEdges[i];
            if (edge.source == node.id) {
                var targetNode = getNodeById(edge.target, true);
                if (targetNode.type != node.type) {
                    return targetNode;
                }
            } else if (edge.target == node.id) {
                var sourceNode = getNodeById(edge.source, true);
                if (sourceNode.type != node.type) {
                    return sourceNode;
                }
            }
        }
    }

    /**
     * This function updates the edgeset based on the actual nodeset
     */
    function updateEdges() {
        var newEdges = [];
        initEdges.forEach(function (d) {
            var sourceNode = getNodeById(d.source),
                targetNode = getNodeById(d.target);
            if (sourceNode && targetNode) {
                // source and target are in new nodeset
                newEdges.push(d);
            } else if (sourceNode) {
                // only source is in new nodeset
                var nextSuccessor = getNextSuccessorsInNodeset(sourceNode);
                if (sourceNode.tempSuccessors) {
                    sourceNode.tempSuccessors.push(nextSuccessor);
                } else {
                    sourceNode.tempSuccessors = [nextSuccessor];
                }
                createEdges(sourceNode, nextSuccessor, newEdges);
            }
        });
        edges = newEdges;
    }

    /**
     * This function creates edges and addes them to the given array
     * @param sourceNode the source node
     * @param targetNodes array of target nodes
     * @param edgeArrayToAdd array to add the created edges to
     */
    function createEdges(sourceNode, targetNodes, edgeArrayToAdd) {
        targetNodes.forEach(function (target) {
            edgeArrayToAdd.push({
                "id": "edge_" + sourceNode.id + "_" + target.id,
                "source": sourceNode.id,
                "target": target.id
            })
        })
    }

    /**
     * This function returns an array of successors which are in the actual nodeset
     * @param node
     * @returns {Array} returns an array of nodes
     */
    function getNextSuccessorsInNodeset(node) {
        var result = [];
        for (var i = 0; i < node.successors; i++) {
            var sucNode = node.successors[i];
            if (getNodeById(sucNode.id)) {
                result.push(sucNode);
            } else {
                result.concat(getNextSuccessorsInNodeset(sucNode));
            }
        }
        return result;
    }

    function initGraph(edgeWidthByMember) {
        chart = clusterEvoChart();
        chart.actorType(actorType)
            .eventType(eventType)
            .edgeWidthByMembers(edgeWidthByMember);
        d3.select('#chart')
            .data([{nodes: nodes, edges: getFilteredEdges(), clusters: clusters}])
            .call(chart);
    }

    /**
     *
     * @returns {Array} the returned array contains only edges, where the sourceNode and targetNode are in the actual nodeset
     */
    function getFilteredEdges() {
        var filtered = [];
        edges.forEach(function (d) {
            var source = getNodeById(d.source),
                target = getNodeById(d.target);
            if (source.type == target.type) {
                filtered.push(d);
            }
        });
        return filtered;
    }

//---------- initializing nodes
    function initializeNodes() {
        var startNodes = [];
        nodes.forEach(function (node) {
            node.successors = [];
            node.predecessors = [];
            for (var i = 0; i < edges.length; i++) {
                var edge = edges[i];
                if (edge.target == node.id) {
                    var predecessorNode = getNodeById(edge.source);
                    if (predecessorNode.type == node.type) {
                        node.predecessors.push(predecessorNode);
                    }
                } else if (edge.source == node.id) {
                    var successorNode = getNodeById(edge.target);
                    if (successorNode.type == node.type) {
                        node.successors.push(successorNode);
                    }
                }
            }
            node.degree = node.successors.length + node.predecessors.length;
            if (node.successors.length > 0) {
                startNodes.push(node);
            }
            node.timestep = parseInt(node.timestep);

            // convert members-string to valid array
            if (typeof node.members === 'string') {
                node.members = node.members.substring(1, node.members.length - 1).split(',');
                node.members = $.map(node.members, $.trim);
                node.members.sort(caseInsensitiveCompare);
                addMembers(node.members);
            }
            // determine min and max group size
            if (init) {
                switch (node.type) {
                    case actorType:
                        if (actorGroupSize.min == 0 || actorGroupSize.min > node.members.length) {
                            actorGroupSize.min = node.members.length;
                        } else if (actorGroupSize.max == 0 || actorGroupSize.max < node.members.length) {
                            actorGroupSize.max = node.members.length;
                        }
                        break;
                    case eventType:
                        if (eventGroupSize.min == 0 || eventGroupSize.min > node.members.length) {
                            eventGroupSize.min = node.members.length;
                        } else if (eventGroupSize.max == 0 || eventGroupSize.max < node.members.length) {
                            eventGroupSize.max = node.members.length;
                        }
                }
            }
        });
        allMembers.sort(caseInsensitiveCompare);
    }

    /**
     * compares a and b and ignores the case
     * @param a
     * @param b
     * @returns {boolean} see String.prototype.localeCompare
     */
    function caseInsensitiveCompare(a, b) {
        return a.toLowerCase().localeCompare(b.toLowerCase());
    }

    /**
     * Adds the given members to allMembers. If a member of members is already in allMembers, it is not added
     * @param members
     */
    function addMembers(members) {
        members.forEach(function (d) {
            if (allMembers.indexOf(d) == -1) {
                allMembers.push(d);
            }
        })
    }

//---------- end of initializing/coloring

    function detectCluster() {
        clusters = [];
        nodes.forEach(function (n) {
                if (n.type == actorType) {
                    var rGroup = getResourceToActor(n.id);
                    rGroup.clusterId = clusters.length;
                    n.clusterId = clusters.length;
                    clusters.push({
                        id: clusters.length,
                        actorGroup: n,
                        eventGroup: rGroup
                    });
                }
            }
        );
    }

    /**
     * calculate the positions of all nodes/clusters, depending on predecessors and successors
     * @returns {Array} returns the space after all nodes are positioned
     */
    function calculatePositions() {
        var timeSteps = getTimeSteps(),
            maxStepSize = getMaximumStepSize(timeSteps) / 2,
            space = [];
        timeSteps.forEach(function (timeStepNodes, index) {
            var stepSpace = typeof space[index] !== 'undefined' ? space[index] : new StepSpace(),
                flag = 'mid';
            maxStepSize = maxStepSize > stepSpace.size() ? maxStepSize : stepSpace.size();
            timeStepNodes.sort(rankCompare);
            timeStepNodes.forEach(function (node) {
                var pointer,
                    cluster = getClusterById(node.clusterId),
                    successorsHavePos = false;
                for (var j = 0; j < node.successors.length; j++) {
                    if (typeof node.successors[j].y !== 'undefined') {
                        successorsHavePos = true;
                        break;
                    }
                }
                var spaceReserved = false;
                var mostDistantActorSucc = getMostDistantSuccessor(cluster.actorGroup),
                    mostDistantEventSucc = getMostDistantSuccessor(cluster.eventGroup),
                    nodeToCheck, succ;
                if (typeof node.y === 'undefined') {
                    if (successorsHavePos) {
                        pointer = findMinimumDistancePosition(stepSpace, cluster.actorGroup.successors, cluster.eventGroup.successors);
                    } else if (node.predecessors.length < 1) {
                        switch (flag) {
                            case 'top':
                                pointer = maxStepSize - 1;
                                while (stepSpace.getObjectAtSlot(pointer) != undefined) {
                                    pointer--;
                                }
                                flag = 'bot';
                                break;
                            case 'mid':
                                pointer = Math.floor(maxStepSize / 2) - 1;
                                var i = 1,
                                    dec = false;
                                while (stepSpace.getObjectAtSlot(pointer) != undefined) {
                                    if (dec) {
                                        pointer -= i;
                                    } else {
                                        pointer += i;
                                    }
                                    dec = !dec;
                                    i++;
                                }
                                flag = 'top';
                                break;
                            case 'bot':
                                pointer = 0;
                                while (stepSpace.getObjectAtSlot(pointer) != undefined) {
                                    pointer++;
                                }
                                flag = 'mid';
                                break;
                        }

                    } else {
                        pointer = findMinimumDistancePosition(stepSpace, cluster.actorGroup.predecessors, cluster.eventGroup.predecessors);
                    }

                    cluster.actorGroup.y = (pointer * 2) + 1;
                    cluster.eventGroup.y = (pointer * 2) + 2;
                    stepSpace.addCluster(pointer, cluster);
                    if (typeof mostDistantActorSucc !== 'undefined' && typeof mostDistantEventSucc !== 'undefined') {
                        if (mostDistantActorSucc.timestep < mostDistantEventSucc.timestep) {
                            if (typeof mostDistantEventSucc.y === 'undefined') {
                                nodeToCheck = cluster.eventGroup;
                                succ = mostDistantEventSucc;
                            } else if (typeof mostDistantActorSucc.y === 'undefined') {
                                nodeToCheck = cluster.actorGroup;
                                succ = mostDistantActorSucc;
                            }
                        } else {
                            if (typeof mostDistantActorSucc.y === 'undefined') {
                                nodeToCheck = cluster.actorGroup;
                                succ = mostDistantActorSucc;
                            } else if (typeof mostDistantEventSucc.y === 'undefined') {
                                nodeToCheck = cluster.eventGroup;
                                succ = mostDistantEventSucc;
                            }
                        }
                    } else if (typeof mostDistantActorSucc !== 'undefined') {
                        if (typeof mostDistantActorSucc.y === 'undefined') {
                            nodeToCheck = cluster.actorGroup;
                            succ = mostDistantActorSucc;
                        }
                    } else if (typeof mostDistantEventSucc !== 'undefined') {
                        if (typeof mostDistantEventSucc.y === 'undefined') {
                            nodeToCheck = cluster.eventGroup;
                            succ = mostDistantEventSucc;
                        }
                    }
                    if (typeof nodeToCheck !== 'undefined') {
                        spaceReserved = checkNextOccurrences(space, nodeToCheck, pointer, index, succ);
                        if (!spaceReserved) {
                            if (nodeToCheck.id == cluster.actorGroup.id && occ[cluster.eventGroup.label].length > 1) {
                                checkNextOccurrences(space, cluster.eventGroup, pointer, index);
                            } else if (nodeToCheck.id == cluster.eventGroup.id && occ[cluster.actorGroup.label].length > 1) {
                                checkNextOccurrences(space, cluster.actorGroup, pointer, index);
                            }
                        }
                    }
                } else {
                    if (node.successors.length > 0 && !successorsHavePos) {
                        switch (node.type) {
                            case actorType:
                                pointer = (node.y - 1) / 2;
                                break;
                            case eventType:
                                pointer = (node.y - 2) / 2;
                                break;
                        }
                        if (typeof mostDistantActorSucc !== 'undefined' && typeof mostDistantEventSucc !== 'undefined') {
                            if (mostDistantActorSucc.timestep < mostDistantEventSucc.timestep) {
                                if (typeof mostDistantEventSucc.y === 'undefined') {
                                    nodeToCheck = cluster.eventGroup;
                                    succ = mostDistantEventSucc;
                                } else if (typeof mostDistantActorSucc.y === 'undefined') {
                                    nodeToCheck = cluster.actorGroup;
                                    succ = mostDistantActorSucc;
                                }
                            } else {
                                if (typeof mostDistantActorSucc.y === 'undefined') {
                                    nodeToCheck = cluster.actorGroup;
                                    succ = mostDistantActorSucc;
                                } else if (typeof mostDistantEventSucc.y === 'undefined') {
                                    nodeToCheck = cluster.eventGroup;
                                    succ = mostDistantEventSucc;
                                }
                            }
                        } else if (typeof mostDistantActorSucc !== 'undefined') {
                            if (typeof mostDistantActorSucc.y === 'undefined') {
                                nodeToCheck = cluster.actorGroup;
                                succ = mostDistantActorSucc;
                            }
                        } else if (typeof mostDistantEventSucc !== 'undefined') {
                            if (typeof mostDistantEventSucc.y === 'undefined') {
                                nodeToCheck = cluster.eventGroup;
                                succ = mostDistantEventSucc;
                            }
                        }
                        if (typeof nodeToCheck !== 'undefined' && typeof succ !== 'undefined') {
                            spaceReserved = checkNextOccurrences(space, nodeToCheck, pointer, index, succ);
                            if (!spaceReserved) {
                                if (nodeToCheck.id == cluster.actorGroup.id && occ[cluster.eventGroup.label].length > 1) {
                                    checkNextOccurrences(space, cluster.eventGroup, pointer, index, succ);
                                } else if (nodeToCheck.id == cluster.eventGroup.id && occ[cluster.actorGroup.label].length > 1) {
                                    checkNextOccurrences(space, cluster.actorGroup, pointer, index, succ);
                                }
                            }
                        }
                    }
                }
            });
            space[index] = stepSpace;
        });
        return space;
    }

    /**
     * reduce the intersections based on the edge equations of all (visible) edges
     * @param space
     * @returns {boolean} returns true if at least one cluster changed its position
     */
    function optimizeIntersections(space) {
        var edgeEquations = getEdgeEquations(edges),
            updatedEdgeEquations = edgeEquations,
            somethingChanged = false;
        edgeEquations.sort(function (e1, e2) {
            return e1.x1 - e2.x1;
        });
        for (var i = 0; i < edgeEquations.length; i++) {
            // get updated edge
            var edge = getEdgeEquationById(edgeEquations[i].id);
            var interSectionCount = countIntersections(edge, updatedEdgeEquations);
            if (interSectionCount > 0) {
                var sourceNode = getNodeById(edge.source),
                    sourceCluster = getClusterById(sourceNode.clusterId),
                    sourceEdges = addEdgesToArray(getEdgesOfNode(sourceCluster.actorGroup, edges), getEdgesOfNode(sourceCluster.eventGroup, edges)),
                    targetNode = getNodeById(edge.target),
                    targetCluster = getClusterById(targetNode.clusterId),
                    targetEdges = addEdgesToArray(getEdgesOfNode(targetCluster.actorGroup, edges), getEdgesOfNode(targetCluster.eventGroup, edges));
                if (sourceEdges.length === targetEdges.length) {
                    // swap source
                    if (swapCluster(space[sourceNode.timestep], getEdgeEquations(sourceEdges), updatedEdgeEquations, sourceCluster, space)) {
                        updatedEdgeEquations = getEdgeEquations(edges);
                        somethingChanged = true;
                    }
                    // swap target
                    if (swapCluster(space[targetNode.timestep], getEdgeEquations(targetEdges), updatedEdgeEquations, targetCluster, space)) {
                        updatedEdgeEquations = getEdgeEquations(edges);
                        somethingChanged = true;
                    }
                } else if (sourceEdges.length < targetEdges.length) {
                    // swap source
                    if (swapCluster(space[sourceNode.timestep], getEdgeEquations(sourceEdges), updatedEdgeEquations, sourceCluster, space)) {
                        updatedEdgeEquations = getEdgeEquations(edges);
                        somethingChanged = true;
                    }
                    // swap target
                    if (swapCluster(space[targetNode.timestep], getEdgeEquations(targetEdges), updatedEdgeEquations, targetCluster, space)) {
                        updatedEdgeEquations = getEdgeEquations(edges);
                        somethingChanged = true;
                    }
                } else {
                    // swap target
                    if (swapCluster(space[targetNode.timestep], getEdgeEquations(targetEdges), updatedEdgeEquations, targetCluster, space)) {
                        updatedEdgeEquations = getEdgeEquations(edges);
                        somethingChanged = true;
                    }
                    // swap source
                    if (swapCluster(space[sourceNode.timestep], getEdgeEquations(sourceEdges), updatedEdgeEquations, sourceCluster, space)) {
                        updatedEdgeEquations = getEdgeEquations(edges);
                        somethingChanged = true;
                    }
                }
            }
        }
        return somethingChanged;
    }

    /**
     *
     * @param edgeId
     * @returns {LineEquation} returns a LineEquation corresponding to the edge with the id edgeId
     */
    function getEdgeEquationById(edgeId) {
        var edge;
        for (var i = 0; i < edges.length; i++) {
            edge = edges[i];
            if (edge.id == edgeId) {
                break;
            }
        }
        var sourceNode = getNodeById(edge.source),
            targetNode = getNodeById(edge.target);
        return new LineEquation(sourceNode.timestep, sourceNode.y, targetNode.timestep, targetNode.y, edge.id, edge.source, edge.target);
    }

    /**
     * Adds edges to the given array. If an edge of edges is already in array, it is not added.
     * @param array
     * @param edges
     * @returns {Array} the array with the edges added
     */
    function addEdgesToArray(array, edges) {
        edges.forEach(function (edge) {
            if (!edgeIsInArray(array, edge)) {
                array.push(edge);
            }
        });
        return array;
    }

    /**
     *
     * @param array
     * @param edge
     * @returns {boolean} true if there is an edge in array with the id of the given edge
     */
    function edgeIsInArray(array, edge) {
        for (var i = 0; i < array.length; i++) {
            var e = array[i];
            if (e.id == edge.id) {
                return true;
            }
        }
        return false;
    }

    function getOverallSlotRange(space) {
        var overallRange = [100, -100];
        space.forEach(function (d) {
            var range = d.getSlotRange();
            overallRange[0] = Math.min(overallRange[0], range[0]);
            overallRange[1] = Math.max(overallRange[1], range[1]);
        });
        return overallRange;
    }

    /**
     *
     * @param stepSpace the Space of the cluster
     * @param nodeEdgeEquations all EdgeEquations belonging to the cluster
     * @param allEdgeEquations all EdgeEquations
     * @param cluster
     */
    function swapCluster(stepSpace, nodeEdgeEquations, allEdgeEquations, cluster, space) {
        var range = getOverallSlotRange(space),
            intersectionCountToCompare = 0,
            swapped = false;
        nodeEdgeEquations.forEach(function (e) {
            intersectionCountToCompare += countIntersections(e, allEdgeEquations);
        });
        var internalIntersections = 0;
        nodeEdgeEquations.forEach(function (e) {
            internalIntersections += countIntersections(e, nodeEdgeEquations);
        });
        intersectionCountToCompare -= internalIntersections / 2;
        var offSet = 2;
        if (range[0] == range[1]) {
            offSet = 1
        }
        for (var j = range[0] - 1; j < range[1] + offSet; j++) {
            var slot = stepSpace.getObjectAtSlot(j),
                cache = {c1: {}, c2: {}},
                counter = 0,
                newInternalIntersections = 0,
                newEquations;
            if (slot === undefined) {
                // save original positions
                cache.c1.actor = cluster.actorGroup.y;
                cache.c1.resource = cluster.eventGroup.y;
                cluster.actorGroup.y = (j * 2) + 1;
                cluster.eventGroup.y = (j * 2) + 2;

                newEquations = getEdgeEquations(addEdgesToArray(getEdgesOfNode(cluster.actorGroup, edges), getEdgesOfNode(cluster.eventGroup, edges)));
                newEquations.forEach(function (e) {
                    counter += countIntersections(e, getEdgeEquations(edges));
                });
                newEquations.forEach(function (e) {
                    newInternalIntersections += countIntersections(e, newEquations);
                });
                counter -= newInternalIntersections / 2;
                if (counter < intersectionCountToCompare) {
                    stepSpace.swapSlots((cache.c1.actor - 1) / 2, j);
                    clearReservedSlotsForCluster(cluster, space);
                    intersectionCountToCompare = counter;
                    swapped = true;
                } else {
                    cluster.actorGroup.y = cache.c1.actor;
                    cluster.eventGroup.y = cache.c1.resource;
                }
            } else if (slot.cluster.id != 'reserved') {
                cache.c1.actor = cluster.actorGroup.y;
                cache.c1.resource = cluster.eventGroup.y;
                cache.c2.actor = slot.cluster.actorGroup.y;
                cache.c2.resource = slot.cluster.eventGroup.y;

                var swapTargetEquations = getEdgeEquations(addEdgesToArray(getEdgesOfNode(slot.cluster.actorGroup, edges), getEdgesOfNode(slot.cluster.eventGroup, edges))),
                    swapTargetCounter = 0,
                    internalTargetIntersections = 0;
                swapTargetEquations.forEach(function (e) {
                    swapTargetCounter += countIntersections(e, getEdgeEquations(edges));
                });
                swapTargetEquations.forEach(function (e) {
                    internalTargetIntersections += countIntersections(e, swapTargetEquations);
                });
                swapTargetCounter -= internalTargetIntersections / 2;
                cache.c2.intersectionCounter = swapTargetCounter;

                cluster.actorGroup.y = cache.c2.actor;
                cluster.eventGroup.y = cache.c2.resource;
                slot.cluster.actorGroup.y = cache.c1.actor;
                slot.cluster.eventGroup.y = cache.c1.resource;
                newEquations = getEdgeEquations(addEdgesToArray(getEdgesOfNode(cluster.actorGroup, edges), getEdgesOfNode(cluster.eventGroup, edges)));
                newEquations.forEach(function (e) {
                    counter += countIntersections(e, getEdgeEquations(edges));
                });
                newEquations.forEach(function (e) {
                    newInternalIntersections += countIntersections(e, newEquations);
                });
                counter -= newInternalIntersections / 2;
                var newSwapTargetEquations = getEdgeEquations(addEdgesToArray(getEdgesOfNode(slot.cluster.actorGroup, edges), getEdgesOfNode(slot.cluster.eventGroup, edges))),
                    newSwapTargetCounter = 0,
                    newInternalTargetIntersections = 0;
                newSwapTargetEquations.forEach(function (e) {
                    newSwapTargetCounter += countIntersections(e, getEdgeEquations(edges));
                });
                newSwapTargetEquations.forEach(function (e) {
                    newInternalTargetIntersections += countIntersections(e, newSwapTargetEquations);
                });
                newSwapTargetCounter -= newInternalTargetIntersections / 2;

                if (counter + newSwapTargetCounter < intersectionCountToCompare + cache.c2.intersectionCounter) {
                    stepSpace.swapSlots((cache.c1.actor - 1) / 2, j);
                    clearReservedSlotsForCluster(cluster, space);
                    clearReservedSlotsForCluster(slot.cluster, space);

                    intersectionCountToCompare = counter;
                    swapped = true;
                } else {
                    cluster.actorGroup.y = cache.c1.actor;
                    cluster.eventGroup.y = cache.c1.resource;
                    slot.cluster.actorGroup.y = cache.c2.actor;
                    slot.cluster.eventGroup.y = cache.c2.resource;
                }
            }
        }
        return swapped;
    }

    /**
     * clears all slots, which are reserved by nodes of the given cluster
     * @param cluster
     * @param space
     */
    function clearReservedSlotsForCluster(cluster, space) {
        clearReservedSlots(space, cluster.actorGroup);
        cluster.actorGroup.predecessors.forEach(function (d) {
            clearReservedSlots(space, d);
        });
        cluster.actorGroup.successors.forEach(function (d) {
            clearReservedSlots(space, d);
        });
        clearReservedSlots(space, cluster.eventGroup);
        cluster.eventGroup.predecessors.forEach(function (d) {
            clearReservedSlots(space, d);
        });
        cluster.eventGroup.successors.forEach(function (d) {
            clearReservedSlots(space, d);
        });
    }

    /**
     * Clears all slots, which are reserved by the given node
     * @param space
     * @param node
     */
    function clearReservedSlots(space, node) {
        space.forEach(function (s) {
            var newSlots = [];
            for (var i = 0; i < s.size(); i++) {
                var slot = s.slots[i];
                if (!(typeof slot !== 'undefined' && slot.cluster.id === 'reserved' && slot.cluster.by.id == node.id)) {
                    newSlots.push(slot);
                }
            }
            s.slots = newSlots;
        })
    }

    /**
     *
     * @param node
     * @param edgeArray
     * @returns {Array} returns an array of all edges incoming to or outgoing from the given node, which are in edgeArray
     */
    function getEdgesOfNode(node, edgeArray) {
        var edgesOfNode = [];
        edgeArray.forEach(function (edge) {
            if (node.id == edge.target || node.id == edge.source) {
                edgesOfNode.push(edge);
            }
        });
        return edgesOfNode;
    }

    /**
     *
     * @param edgeArray
     * @returns {Array} returns an array containing all edgeEquations of the edges in edgeArray
     */
    function getEdgeEquations(edgeArray) {
        var edgeEquations = [];
        edgeArray.forEach(function (d) {
            var sourceNode = getNodeById(d.source),
                targetNode = getNodeById(d.target);
            edgeEquations.push(new LineEquation(sourceNode.timestep, sourceNode.y, targetNode.timestep, targetNode.y, d.id, d.source, d.target));
        });
        return edgeEquations;
    }

    /**
     * Counts the intersections of the given edge and the edges in edgeEquations
     * @param edge {LineEquation} The edge to check
     * @param edgeEquations {LineEquation[]} An array of EdgeEquations
     * @returns {number} returns the number of intersections between edge and all edges in edgeEquations
     */
    function countIntersections(edge, edgeEquations) {
        var intersectionCount = 0;
        for (var i = 0; i < edgeEquations.length; i++) {
            var e2 = edgeEquations[i];
            if (edge.id != e2.id && edge.source != e2.source && edge.target != e2.source
                && edge.source != e2.target && edge.target != e2.target && doIntersect(edge, e2)) {
                if (edge.m == e2.m) {
                    intersectionCount += 10;
                } else {
                    intersectionCount++;
                }
            }
        }
        return intersectionCount;
    }

    /**
     * Calculates if the two given edge equations do intersect
     * @param e1 EdgeEquation
     * @param e2 EdgeEquation
     * @returns {boolean} true if e1 and e2 do intersect, false otherwise
     */
    function doIntersect(e1, e2) {
        if ((e1.m != Infinity && e1.m != -Infinity && e2.m != Infinity && e2.m != -Infinity) && (e1.m == e2.m)) {
            // m1 and m2 unequal to (-) infinity and m1 equal to m2
            return e1.n == e2.n && (e1.isValidXValue(e2.x1) || e1.isValidXValue(e2.x2) || e2.isValidXValue(e1.x1) || e2.isValidXValue(e1.x2))
        }
        if (e1.m != Infinity && e1.m != -Infinity && e2.m != Infinity && e2.m != -Infinity) {
            var intersecX = (e2.n - e1.n) / (e1.m - e2.m);
            return e1.isValidXValue(intersecX) && e2.isValidXValue(intersecX);
        } else if ((e1.m == Infinity || e1.m == -Infinity) && (e2.m == Infinity && e2.m == -Infinity)) {
            // both (-)Infinity
            return e1.isValidYValue(e2.y1) || e2.isValidYValue(e1.y1);
        } else if ((e1.m == Infinity || e1.m == -Infinity) && e2.m != Infinity && e2.m != -Infinity) {
            // e1 (-)Infinity
            return e2.isValidXValue(e1.x1) && e1.isValidYValue(e2.calculateY(e1.x1));
        } else {
            // e2 (-)Infinity
            return e1.isValidXValue(e2.x1) && e2.isValidYValue(e1.calculateY(e2.x1));
        }
    }

    /**
     * Searches the most distant successor and reserves the slots between node and the successor
     * @param space
     * @param node
     * @param pointer
     * @param index
     * @param mostDistantSucc
     * @returns {boolean} true if space was reserved, false if no space was reserved
     */
    function checkNextOccurrences(space, node, pointer, index, mostDistantSucc) {
        var mostDistantSuccessor = mostDistantSucc || getMostDistantSuccessor(node);
        if (typeof mostDistantSuccessor !== 'undefined' && mostDistantSuccessor.timestep != index + 1) {
            reserveSpace(space, index, mostDistantSuccessor.timestep, pointer, node);
            return true;
        }
        return false;
    }

    /**
     * Determines the most distant successor of node
     * @param node
     * @returns {object} returns the node of the most distant successor or undefined;
     */
    function getMostDistantSuccessor(node) {
        var result;
        for (var i = 0; i < node.successors.length; i++) {
            var suc = node.successors[i];

            if (typeof suc !== 'undefined' && isInScopeSet(suc)) {
                result = result != undefined ? suc.timestep > result.timestep ? suc : result : suc;
            }
        }
        return result;
    }

    /**
     * Checks if node is in the actual nodeset nodes
     * @param node
     * @returns {boolean} true if node is in nodes, false otherwise
     */
    function isInScopeSet(node) {
        for (var i = 0; i < nodes.length; i++) {
            var n = nodes[i];
            if (n.id == node.id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the position with a minimum distance, which neither contains a node nor is reserved
     * @param stepSpace
     * @param actorPredecessors
     * @param resourcePredecessors
     * @returns {number} the position wih the minimum distance to actorPredecessors and resourcePredecessors in the given stepspace
     */
    function findMinimumDistancePosition(stepSpace, actorPredecessors, resourcePredecessors) {
        var minPos = undefined,
            clusterPositions = [],
            checkPosition = 0,
            dec = true;
        actorPredecessors.forEach(function (d) {
            if (typeof d.y !== 'undefined') {
                clusterPositions.push((d.y - 1) / 2);
                checkPosition += (d.y - 1) / 2;
            }
        });
        resourcePredecessors.forEach(function (d) {
            if (typeof d.y !== 'undefined') {
                clusterPositions.push((d.y - 2) / 2);
                checkPosition += (d.y - 2) / 2;
            }
        });
        checkPosition = Math.floor(checkPosition / (actorPredecessors.length + resourcePredecessors.length));
        for (var i = 0; i < (stepSpace.size() + 2); i++) {
            if (dec) {
                checkPosition -= i;
            } else {
                checkPosition += i;
            }
            var slot = stepSpace.getObjectAtSlot(checkPosition);

            if (typeof slot === 'undefined') {
                var dist = 0;
                clusterPositions.forEach(function (d) {
                    dist += Math.abs(checkPosition - d);
                });
                if (typeof minPos === 'undefined') {
                    minPos = {pos: checkPosition, dist: dist};
                } else {
                    minPos = minPos.dist < dist ? minPos : {pos: checkPosition, dist: dist};
                }
            }
            dec = !dec;
        }
        return minPos.pos;
    }

    /**
     * Reserves all slots at pointer between timestep start and timestep end
     * @param space
     * @param start
     * @param end
     * @param pointer
     * @param node
     */
    function reserveSpace(space, start, end, pointer, node) {
        var endTimeSpace = typeof space[end] !== 'undefined' ? space[end] : new StepSpace(),
            endNode = undefined;
        var diff = end - start;
        for (var i = 0; i < diff - 1; i++) {
            var stepSpace = space[start + 1 + i] != undefined ? space[start + 1 + i] : new StepSpace();
            stepSpace.addCluster(pointer, {id: 'reserved', by: node});
            space[start + 1 + i] = stepSpace;
        }

        for (var i = 0; i < node.successors.length; i++) {
            var suc = node.successors[i];
            if (isInScopeSet(suc)) {
                if (typeof endNode === 'undefined') {
                    endNode = suc;
                } else {
                    endNode = suc.timestep > endNode.timestep ? suc : endNode;
                }
            }
        }
        var endCluster = getClusterById(endNode.clusterId);
        endCluster.actorGroup.y = (pointer * 2) + 1;
        endCluster.eventGroup.y = (pointer * 2) + 2;
        endTimeSpace.addCluster(pointer, endCluster);
        space[end] = endTimeSpace;
    }

    /**
     * Searches the cluster with the given id
     * @param clusterId
     * @returns {object|undefined} returns the cluster-object if found, undefined otherwise
     */
    function getClusterById(clusterId) {
        for (var i = 0; i < clusters.length; i++) {
            var c = clusters[i];
            if (c.id == clusterId) {
                return c;
            }
        }
    }

    /**
     * Compares the rank-field of n1 and n2
     * @param n1
     * @param n2
     * @returns {number} returns a negative number if the rank of n1 is higher than the rank of n2,
     * returns a positive number if the rank of n2 is higher than he rank of n1, returns 0 if the two ranks are equal
     */
    function rankCompare(n1, n2) {
        return n2.rank - n1.rank;
    }

    function getMaximumStepSize(steps) {
        var max = 0;
        steps.forEach(function (s) {
            max = max > s.length ? max : s.length;
        });
        return max;
    }

    /**
     *
     * @returns {Array} returns an array of arrays of nodes. The index of the arrays represents the timestep. The array at index i contains all nodes in timestep i.
     */
    function getTimeSteps() {
        var steps = [];
        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            steps[node.timestep] = typeof steps[node.timestep] === 'undefined' ? [node] : steps[node.timestep].concat(node);
        }
        return steps;
    }

    /**
     * Calculates the rank of each node and stores it in the "rank"-field of each node.
     */
    function rankNodes() {
        nodes.forEach(function (n) {
            occ[n.label] = occ[n.label] == undefined ? [n.timestep] : occ[n.label].concat(n.timestep);
            n.predecessors.forEach(function (p) {
                if (typeof occ[p.label] !== 'undefined' && occ[p.label].indexOf(n.timestep) == -1) {
                    occ[p.label] = occ[p.label].concat(n.timestep);
                }
            })
        });
        nodes.forEach(function (n) {
            n.rank = occ[n.label].length + n.predecessors.length + n.successors.length;
        });
    }

    /**
     *
     * @param actorId
     * @returns {object} the corresponding resource node to the actor node with the given id
     */
    function getResourceToActor(actorId) {
        for (var i = 0; i < edges.length; i++) {
            var edge = edges[i];
            if (edge.source == actorId) {
                var targetNode = getNodeById(edge.target);
                if (targetNode.type == eventType) {
                    return targetNode;
                }
            }
        }
    }

    /**
     *
     * @param nodeId
     * @param useInitNodes if true, searches in initNodes, else in nodes
     * @returns {object} returns the node with the given id
     */
    function getNodeById(nodeId, useInitNodes) {
        var nodePool;
        if (useInitNodes) {
            nodePool = initNodes;
        } else {
            nodePool = nodes;
        }
        for (var i = 0; i < nodePool.length; i++) {
            var n = nodePool[i];
            if (n.id == nodeId) {
                return n;
            }
        }
    }

    return start();
}