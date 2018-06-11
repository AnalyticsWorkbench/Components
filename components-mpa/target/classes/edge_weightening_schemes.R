# TODO: Add comment
# 
# Author: hecking
###############################################################################

require(igraph)

conventional <- FALSE

forward <- function(g, topOrder, inlist, outlist) {
	cat("forward \n")
	isSource <- TRUE
	
	outEdgesIds <- c()
	inEdgesEis <- c()
	
	sapply(topOrder, function(v) {
	
		if (!isSource) {
			outEdgesIds <<- get.edge.ids(g, as.vector(rbind(rep(v,length(outlist[[v]])),outlist[[v]])))
			inEdgesIds <<- get.edge.ids(g, as.vector(rbind(inlist[[v]], rep(v,length(inlist[[v]])))))			
			E(g)[outEdgesIds]$weightF <<- sum(E(g)[inEdgesIds]$weightF)
			V(g)[v]$weightF <<- sum(V(g)[inlist[[v]]]$weightF)						
		} else {
			isSource <<- FALSE
			outEdgesIds <<- get.edge.ids(g, as.vector(rbind(rep(v,length(outlist[[v]])),outlist[[v]])))
			E(g)[outEdgesIds]$weightF <<- 1
			V(g)[v]$weightF <<- 1
		}
	})
	
	g
}

forwardPre <- function(g, topOrder, inlist, outlist) {
	cat("forward pre\n")
	isSource <- TRUE
	
	outEdgesIds <- c()
	inEdgesEis <- c()
	
	sapply(topOrder, function(v) {
	
		if (!isSource) {
			outEdgesIds <<- get.edge.ids(g, as.vector(rbind(rep(v,length(outlist[[v]])),outlist[[v]])))
			inEdgesIds <<- get.edge.ids(g, as.vector(rbind(inlist[[v]], rep(v,length(inlist[[v]])))))
			
			tempEdge <- 0
			for (e in inEdgesIds){
				tempEdge <- tempEdge + E(g)[e]$weightF * get.edge.attribute(g, nameofweight, index=e)
			}
			tempNode <- 0
			for (e in inlist[[v]]){
				tempNode <- tempNode + V(g)[e]$weightF * get.edge.attribute(g, nameofweight, index=e)
			}
			E(g)[outEdgesIds]$weightF <<- tempEdge
			V(g)[v]$weightF <<- tempNode
		} else {
			isSource <<- FALSE
			outEdgesIds <<- get.edge.ids(g, as.vector(rbind(rep(v,length(outlist[[v]])),outlist[[v]])))
			E(g)[outEdgesIds]$weightF <<- 1
			V(g)[v]$weightF <<- 1
		}
	})

	g
}

backward <- function(g, revTopOrder, inlist, outlist) {
	cat("backward \n")
	isSink <- TRUE
	
	outEdgesIds <- c()
	inEdgesEis <- c()
	
	sapply(revTopOrder, function(v) {
				
		if (!isSink) {
					
			outEdgesIds <<- get.edge.ids(g, as.vector(rbind(rep(v,length(outlist[[v]])),outlist[[v]])))
			inEdgesIds <<- get.edge.ids(g, as.vector(rbind(inlist[[v]], rep(v,length(inlist[[v]])))))
			
			E(g)[inEdgesIds]$weightB <<- sum(E(g)[outEdgesIds]$weightB)
			V(g)[v]$weightB <<- sum(V(g)[outlist[[v]]]$weightB)
			
			E(g)[outEdgesIds]$weight <<- E(g)[outEdgesIds]$weightF * E(g)[outEdgesIds]$weightB

			V(g)[v]$weight <<- V(g)[v]$weightF * V(g)[v]$weightB					
								
		} else {
				
			isSink <<- FALSE
			inEdgesIds <- get.edge.ids(g, as.vector(rbind(inlist[[v]], rep(v,length(inlist[[v]])))))
			E(g)[inEdgesIds]$weightB <<- 1
			V(g)[v]$weightB <<- 1
			V(g)[v]$weight <<- V(g)[v]$weightF
			
		}		
	})
	
	V(g)[revTopOrder[length(revTopOrder)]]$weight <- 1
	g
}

backwardPre <- function(g, revTopOrder, inlist, outlist) {
	cat("backward pre\n")
	isSink <- TRUE
	
	outEdgesIds <- c()
	inEdgesEis <- c()
	
	sapply(revTopOrder, function(v) {
				
		if (!isSink) {
					
			outEdgesIds <<- get.edge.ids(g, as.vector(rbind(rep(v,length(outlist[[v]])),outlist[[v]])))
			inEdgesIds <<- get.edge.ids(g, as.vector(rbind(inlist[[v]], rep(v,length(inlist[[v]])))))

			tempEdge <- 0
			for (e in outEdgesIds){
				tempEdge <- tempEdge + E(g)[e]$weightB * get.edge.attribute(g, nameofweight, index=e)
			}
			tempNode <- 0
			for (e in outlist[[v]]){
				tempNode <- tempNode + V(g)[e]$weightB * get.edge.attribute(g, nameofweight, index=e)
			}
			E(g)[inEdgesIds]$weightB <<- tempEdge
			V(g)[v]$weightB <<- tempNode
			
			E(g)[outEdgesIds]$weight <<- E(g)[outEdgesIds]$weightF * E(g)[outEdgesIds]$weightB
			V(g)[v]$weight <<- V(g)[v]$weightF * V(g)[v]$weightB					
					
		} else {				
			isSink <<- FALSE
			inEdgesIds <- get.edge.ids(g, as.vector(rbind(inlist[[v]], rep(v,length(inlist[[v]])))))
			E(g)[inEdgesIds]$weightB <<- 1
			V(g)[v]$weightB <<- 1
			V(g)[v]$weight <<- V(g)[v]$weightF
		}		
	})
	
	V(g)[revTopOrder[length(revTopOrder)]]$weight <- 1
	g
}

determineStartAndEndpoints <- function(graph, sources, sinks, threshold=1) {
	
	if (conventional) {
		
		### conventional approach
		
		outlist <- get.adjlist(graph, mode="out")
		maxWeight <- 0
		startPoints <- c()
		
		maxEW <- max(weights)

		sapply(sources, function(s) {
					
			neighbours <- outlist[[s]]		
			
			if (length(neighbours) > 0) {
				
				edgeIds <- get.edge.ids(graph, as.vector(rbind(rep(s,length(neighbours)), neighbours)))	

				weights <- E(graph)[edgeIds]$weight
				
				maxEW <- max(weights)
				
				if (maxEW > maxWeight) {
					
					maxWeight <<- maxEW
					startPoints <<- c(s)
				} else if (maxEW >= maxWeight) {
					
					startPoints <<- append(startPoints, s)
				}

				edgeIds <- get.edge.ids(graph, as.vector(rbind(rep(s,length(neighbours)), neighbours)))	
				
				
				if (maxEW >= (maxWeight * threshold)) {
					
					startPoints <<- append(startPoints, s)
				} 
			}
		})
		
		V(graph)[startPoints]$startpoint <- TRUE
		
	} else { 
		### new approach
		# startpoints
		edgeIds <- get.edge.ids(graph, as.vector(rbind(rep((length(V(graph)) - 1),length(sources)), sources)))
		sourceWeights <- E(graph)[edgeIds]$weight
		V(graph)[sources[which(sourceWeights >= max((sourceWeights * threshold)))]]$startpoint <- TRUE
	}
	
	V(graph)[sinks]$endpoint <- TRUE
	
	graph
}

edgeBetweenness <- function(graph, th=1) {
	
#	oldSize <- length(V(graph))
#	oldVertices <- V(graph)
	sources <- which(degree(graph, mode="in") == 0)
	sinks <- which(degree(graph, mode="out") == 0)
#	
#	graph <- add.vertices(graph, 2)
#	graph <- add.edges(graph, as.vector(rbind(rep((oldSize + 1),oldSize), oldVertices)))
#	graph <- add.edges(graph, as.vector(rbind(sinks, rep((oldSize + 2),length(sinks)))))
	
	E(graph)$weight <- edge.betweenness(graph)
	
	graph <- determineStartAndEndpoints(graph, sources, sinks, threshold=th)
	graph
}

spc <- function(graph, topOrder=NULL, threshold=1) {

	standalone <- FALSE
	
	
	if (is.null(topOrder)) {
		standalone <- TRUE
		oldSize <- length(V(graph))
		
		sources <- which(degree(graph, mode="in") == 0)
		sinks <- which(degree(graph, mode="out") == 0)
		
		graph <- add.vertices(graph, 2)
		graph <- add.edges(graph, as.vector(rbind(rep((oldSize + 1),length(sources)),sources)))
		graph <- add.edges(graph, as.vector(rbind(sinks, rep((oldSize + 2),length(sinks)))))
		topOrder <- topological.sort(graph)
	}
	inlist <- get.adjlist(graph, mode="in")
	outlist <- get.adjlist(graph, mode="out")
	
	graph <- forward(graph, topOrder, inlist, outlist)
	graph <- backward(graph,rev(topOrder), inlist, outlist)
	
	if (standalone) {
		
		graph <- determineStartAndEndpoints(graph, sources, sinks, threshold=threshold)
		graph <- delete.vertices(graph, V(graph)[(length(V(graph)) - 1):length(V(graph))])

		#write.graph(graph, "spc_weighted.gml", "gml")
	}
	graph
}

existingWeights <- function(graph, topOrder=NULL, threshold=1) {
	cat("weight for main path calculation:", nameofweight, "\n")
	for (e in E(graph)){
		E(graph)[e]$weight <- get.edge.attribute(graph, nameofweight, index=e)
	}
	
	sources <- which(degree(graph, mode="in") == 0)	
	V(graph)[sources]$startpoint <- TRUE
	
	cat("sources:", V(graph)[sources], "\n")
	
	graph
}

spcpre <- function(graph, topOrder=NULL, threshold=1) {

	standalone <- FALSE	
	
	if (is.null(topOrder)) {
		standalone <- TRUE
		oldSize <- length(V(graph))
		
		sources <- which(degree(graph, mode="in") == 0)
		sinks <- which(degree(graph, mode="out") == 0)
		graph <- add.vertices(graph, 2)
		l <- list()
		l[[nameofweight]] <- 1
		graph <- add.edges(graph, as.vector(rbind(rep((oldSize + 1),length(sources)),sources)), attr=l)
		graph <- add.edges(graph, as.vector(rbind(sinks, rep((oldSize + 2),length(sinks)))), attr=l)
		
		topOrder <- topological.sort(graph)
	}
	
	inlist <- get.adjlist(graph, mode="in")
	outlist <- get.adjlist(graph, mode="out")

	graph <- forwardPre(graph, topOrder, inlist, outlist)
	graph <- backwardPre(graph,rev(topOrder), inlist, outlist)
	
	if (standalone) {
		
		graph <- determineStartAndEndpoints(graph, sources, sinks, threshold=threshold)
		graph <- delete.vertices(graph, V(graph)[(length(V(graph)) - 1):length(V(graph))])

		#write.graph(graph, "spc_weighted.gml", "gml")
	}
	graph
}

spcpost <- function(graph, topOrder=NULL, threshold=1) {

	oldWeights <- get.edge.attribute(g, nameofweight, index=E(g))
	
	graph <- spc(graph, topOrder, threshold)
	
	# multiply spc with old weight
	E(graph)$weight <- E(graph)$weight * oldWeights
	
	graph
}

spcnormpost <- function(graph, topOrder=NULL, threshold=1) {

	oldWeights <- get.edge.attribute(g, nameofweight, index=E(g))
	
	graph <- spc(graph, topOrder, threshold)

	# normalize spc weights
	maxSPC = max(E(graph)$weight)	
	E(graph)$weight <- E(graph)$weight / maxSPC
	
	# multiply norm. spc with old weight
	E(graph)$weight <- E(graph)$weight * oldWeights
	
	graph
}

splc <- function(graph, threshold=1) {
	
	# Rl
	oldSize <- length(V(graph))
	oldVertices <- V(graph)
	sources <- which(degree(graph, mode="in") == 0)
	sinks <- which(degree(graph, mode="out") == 0)
	
	graph <- add.vertices(graph, 2)
	graph <- add.edges(graph, as.vector(rbind(rep((oldSize + 1),oldSize), oldVertices)))
	graph <- add.edges(graph, as.vector(rbind(sinks, rep((oldSize + 2),length(sinks)))))
	topOrder <- topological.sort(graph)
	
	# spc
	graph <- spc(graph, topOrder)
	
	# indicate start- and endpoints for the main path construction
	
	graph <- determineStartAndEndpoints(graph, sources, sinks, threshold=threshold)
	graph <- delete.vertices(graph, V(graph)[(length(V(graph)) - 1):length(V(graph))])
	graph
}

spnp <- function(graph, threshold=1) {
	
	# Rp
	sources <- which(degree(graph, mode="in") == 0)
	sinks <- which(degree(graph, mode="out") == 0)
	
	oldSize <- length(V(graph))
	oldVertices <- V(graph)
	graph <- add.vertices(graph, 2)
	graph <- add.edges(graph, as.vector(rbind(rep((oldSize + 1),oldSize), oldVertices)))
	graph <- add.edges(graph, as.vector(rbind(oldVertices, rep((oldSize + 2),oldSize))))
	topOrder <- topological.sort(graph)
	
	# spc
	graph <- spc(graph, topOrder)
	
	graph <- determineStartAndEndpoints(graph, sources, sinks, threshold=threshold)
	graph <- delete.vertices(graph, V(graph)[(length(V(graph)) - 1):length(V(graph))])
	graph
}

npcc <- function(graph, threshold=1) {
	
	# 1. Floyd Warshall
	dist <- shortest.paths(graph)
	
	# 2. row * colum sums
	edgeWeights <- colSums(dist) * rowSums(dist)
	
	graph <- determineStartAndEndpoints(graph, sources, sinks, threshold=threshold)
	edgeWeights
}
