require(igraph)

deleteIsolates <- function(graphs, labels) {
	
	resultGraphs <- list()
	for (i in 1:length(labels)) {
		
		graph <- graphs[[labels[i]]]
		
		deg <- degree(graph)
		
		x <- which(deg > 0)
		
		ids <- c()
		
		if (length(x) > 0) {
			
			sg <- induced.subgraph(graph, x)
		} else {
			
			sg <- graph.empty()
		}
		resultGraphs[[labels[i]]] <- sg
		
	}
	
	resultGraphs
}


resultGraphs <- deleteIsolates(graphs, labels)

filelist <- ""
sapply(labels, function(l) {
			
			if (i == 1) {
				filelist <<- paste("mod_", l,".net",sep="")		
			} else {
				filelist <<- paste(filelist,",mod_", l,".net",sep="")
			}
			write.graph(resultGraphs[[l]], paste("mod_", l,".net",sep=""), format="pajek")			
		})
resultData <- list(dataUrl=filelist,decoratedUrl='null')
