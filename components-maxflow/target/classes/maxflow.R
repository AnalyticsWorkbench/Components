# Calculates the maximum flow for flow graphs
# 
# Author: steinert
###############################################################################

maxflow <- function(g) {
	
	# indentify the source node (ONLY ONE!)
	# the source node must have a 'source' attribute flag set to TRUE
    cat("identify the source node \n")
	source <- V(g)[which(V(g)$source == TRUE)[1]]$id
	
	# indentify the sink node (ONLY ONE!)
	# the sink node must have a 'sink' attribute flag set to TRUE
    cat("identify the sink node\n")
	target <- V(g)[which(V(g)$sink == TRUE)[1]]$id
	
	# 'capacity' edge attribute labeled 'capacity' of the graph
	# calculate the maximal flow
	flow <- graph.maxflow(g, source, target)$flow
	
	# attach this flow to a new edge attribute 'maxflow'
	E(g)$maxflow <- flow
	
	# return graph
    g
}


isFirst <- TRUE
decUrls <- ""
resUrls <- ""

# for all graphs compute the max flow
sapply(labels, function(l) {

    graph <- graphs[[l]]
    
    #compute max flow
    g <- maxflow(graph)
    
    #write results
    write.graph(g, paste(l,"_maxflow.gml",sep=""), "gml")

    if (isFirst) {
        isFirst <<- FALSE
        resUrls <<- paste(l,"_maxflow.gml",sep="")
        
    } else {
        resUrls <<- paste(resUrls,",",l,"_maxflow.gml",sep="")
    }
})

resultData <- list(dataUrl=resUrls, decoratedUrl=decUrls)