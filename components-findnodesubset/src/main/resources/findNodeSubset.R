# Author: steinert
###############################################################################

decUrls <- ""
resUrls <- ""
overallMaxSum <- -1
sumOfEdgeWeights <- 0
averageEdgeWeight <- 0
maxComponent <- NULL
fileOut <- NULL

thetaStepWidth <<- 0.05

###############################################################################
findNodeSubset <- function(g, threshold) {
		
	# retrieve list of components; 'weak' means we ignore edge direction
	components <- decompose.graph(g, mode="weak", max.comps = NA, min.vertices = 1) 

	if(threshold == 0){
		# the only component should be the whole graph
		# hence the sum of edge weights is maximal because all weights >= 0
		# the maximal sum is needed to multiply it with the avg. edge weight in order to find the intersection
		for(component in components){	
			# calculate sum of edge weights
			sum = 0
			if (length(E(component)) > 0){
				sum = Reduce("+", E(component)$weight)
			}
			if (sum > overallMaxSum){
				overallMaxSum <<- sum
			}
		}
	}
	
	if(componentSelector == "NumberOfNodes"){
		maxSize = 0;
		for(component in components){
			if (vcount(component) > maxSize){
				maxSize <- vcount(component)
				maxComponent <<- component
			}
			else if (vcount(component) == maxSize){
				# always prefer component in which seed is
				if ("true" %in% V(component)$seed){ 
					maxSize <<- vcount(component)
					maxComponent <<- component				
				}
			}
		}
	}
	else if (componentSelector == "SumOfEdgeWeights"){
		maxSum = -1;
		for(component in components){		
			sum = 0;
			# calculate sum of edge weights
			if (length(E(component)) > 0){
				sum = Reduce("+", E(component)$weight)
			}
			if (sum > maxSum){
				maxSum <- sum
				maxComponent <<- component
			}
			else if (sum == maxSum){
				# always prefer component in which seed is
				if ("true" %in% V(component)$seed){ 
					# then the seed is not in the other component
					maxSum <<- sum
					maxComponent <<- component				
				}	
			}		
		}
	}
	
	# calculate sum of edge weights and average edge weight for the largest component
	sumOfEdgeWeights <<- 0
	averageEdgeWeight <<- 0
	if (length(E(maxComponent)) > 0){
		sumOfEdgeWeights <<- Reduce("+", as.double(E(maxComponent)$weight))
		averageEdgeWeight <<- sumOfEdgeWeights / length(E(maxComponent))
	}

	cat(threshold, ";", sumOfEdgeWeights, ";", averageEdgeWeight, ";", length(V(maxComponent)), "\n")
    	
	# return graph
    g
}

###############################################################################
filterEdges <- function(g, threshold) {
	edgesToDelete <- which(E(g)$weight < threshold)	
	j <- length(edgesToDelete) + 1
	for(i in 1:length(E(g))){
		if(isTRUE(all.equal(get.edge.attribute(g, "weight", index=i), threshold))){
			edgesToDelete[j] <- i
			j <- j + 1
		}
	}
	g <- delete.edges(g, edgesToDelete)			
	
	# return graph
    g
}

###############################################################################
sapply(labels, function(l) {
    graph <- graphs[[l]]
    
    # replace ',' with '_'
    if(grepl(",", l)){
	    l = gsub(",", "_", l)
    }

	# assign edge weights
	for (e in E(graph)){
		E(graph)[e]$weight <- get.edge.attribute(graph, edgeWeightName, index=e)
	}

	# assign 'seed' attribute
	for (v in V(graph)){
		V(graph)[v]$seed <- get.vertex.attribute(graph, "seed", index=v)
	}

	# assign 'label' attribute
	for (v in V(graph)){
		V(graph)[v]$label <- get.vertex.attribute(graph, "label", index=v)
	}
	
	# make sure that no edges exist that have a weight of infinity
	if(length(E(graph))>0){
		edgesToDelete <- which(is.infinite(E(graph)$weight))
		if(length(edgesToDelete) > 0){
			graph <- delete.edges(graph, edgesToDelete)
		}
	}

	# normalize edge weights
	maxEdgeWeight = max(E(graph)$weight)
	for (e in E(graph)){
		E(graph)[e]$weight <- E(graph)[e]$weight / maxEdgeWeight
	}
	
	# redirects all outputs to a file (and to the screen)
	sink("../output.csv", append=FALSE, split=TRUE)
			
	cat("Theta; Sum of Edge Weight; Average Edge Weight; No. of Nodes in largest component\n")

	largestComponents <- list()	
	differenceAvgSumEdgeWeights <- c()
	differenceNumberRecommendations <- c()

    threshold = 0.0000
    index = 1
    graphTemp <- graph
    while(threshold <= 1.0001) {
		graphTemp <- filterEdges(graphTemp, threshold)
        findNodeSubset(graphTemp, threshold)     
        
        largestComponents[[index]] <- maxComponent
        
        if(length(E(maxComponent)) > 0){
        	# the average edge weight is multiplied with the maximum of the sum of edge weights to ensure
        	# the likelihood of an intersection by equalizing the yranges
	        differenceAvgSumEdgeWeights <- c(differenceAvgSumEdgeWeights, abs(sumOfEdgeWeights-(averageEdgeWeight*overallMaxSum)))
	    }
	    else {
	   		# no edges exist, therefore both measures are zero
	   		# since we seek the minimal distance and want to avoid this case
	   		# we set the difference to the maximum
	        differenceAvgSumEdgeWeights <- c(differenceAvgSumEdgeWeights, overallMaxSum)	    
	    }

        differenceNumberRecommendations <- c(differenceNumberRecommendations, abs(numberRec-length(V(maxComponent))))		
                    
       	threshold <- threshold + thetaStepWidth
       	index <- index + 1
    }
    
    index <- 1
    
    # find intersection of the average and the sum of edge weights
    if (thetaSelector == "Intersection"){
    	index <- which.min(differenceAvgSumEdgeWeights)
		cat("\nintersection is at theta:", (index-1)*thetaStepWidth, "\n")
	}
    else if (thetaSelector == "FixedNumber"){
    	index <- which.min(differenceNumberRecommendations)
		cat("\nFixedNumber is at theta:", (index-1)*thetaStepWidth, "\n")
	}
	# find the theta when the seed node is last in the biggest component
	# there has to be more than minSize nodes inside the largest component, otherwise
	# the theta=1 because if no edges exist, the component with the seed is preferred among equally sized components
	else if (thetaSelector == "SeedIsLost"){
		# at theta=0 the whole graph is considered, hence the seed is in the only component
		seedIsInLargestComponent <- TRUE
		i <- 1
		for(component in largestComponents){
			if(!(("true" %in% V(component)$seed) && (length(V(component))>minSize))&& seedIsInLargestComponent){
				seedIsInLargestComponent <- FALSE
				cat("seed lost after theta ")
				cat((i-2)*thetaStepWidth)
				cat("\n")
			}

			# only consider it if more than minSize nodes are in component
			else if ("true" %in% V(component)$seed && length(V(component))>minSize){
				seedIsInLargestComponent <- TRUE
				cat("seed present at theta ")
				cat((i-1)*thetaStepWidth)
				cat("\n")
				index <- i
			}
			i <- i+1
		}
	}	    

	cat("number of nodes in largest component:", length(V(largestComponents[[index]])), "\n")
	    
    graphname <- paste(l, "_nodeSubset" ,sep="")

#	print("node labels in largest component:")
	# mark largest component
	V(graph)$isInNodeSubset <- "FALSE"
	for(n in V(largestComponents[[index]])$label){
		tempNode <- V(graph)[which(V(graph)$label==n)]
		V(graph)[tempNode]$isInNodeSubset <- 'TRUE'
#		print(V(graph)[tempNode]$label)
	}

    # write decorated graph
   	write.graph(graph, paste(graphname, ".gml", sep=""), "gml")
    decUrls <<- paste(graphname, ".gml", sep="")
    
    # write result
    # sometimes the filename is too long which causes an error "cannot write edgelist..."
#    filename <- paste(graphname, "_biggestComponent.gml", sep="")   
	filename <- "biggestComponent.gml"
    write.graph(largestComponents[[index]], filename, "gml")
	resUrls <<- filename
	
	sink()
})

resultData <- list(dataUrl=resUrls, decoratedUrl=decUrls)