require(igraph)

simplifyGraphs <- function(graphs, labels) {
	
	convertedGraphs <- list()
	
	sapply(labels, function(l) {
				
		graph <- graphs[[l]]
				
		convertedGraphs[[l]] <<- simplify(graph, remove.multiple=TRUE, remove.loops=TRUE)
	})
	
	convertedGraphs
}


resultGraphs <- simplifyGraphs(graphs, labels)

filelist <- ""

sapply(labels, function(l) {

	isFirst <- TRUE
	
	if (isFirst) {
		
		filelist <<- paste("simplified_", l, ".gml", sep="")
		isFirst <- FALSE
		
	} else {
		
		filelist <<- paste(filelist,", simplified_", l, ".gml", sep="")
	}
	write.graph(resultGraphs[[l]], paste("simplified_", l, ".gml", sep=""), format="gml")
})

resultData <- list(dataUrl=filelist,decoratedUrl='null')







