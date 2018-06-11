require(igraph)

extractCores <- function(graphs, labels) {
	
	graphCores <- list()
	for (i in 1:length(labels)) {
		
		graph <- graphs[[labels[i]]]
		
		crn <- graph.coreness(graph)
		
		x <- which(crn > 5)
		
		ids <- c()
		for (j in 1:length(x)) {
			
			ids[j] <- x[j] - 1
		}
		sg <- subgraph(graph, ids)
		graphCores[[labels[i]]] <- sg
		
	}
	
	graphCores
}


resultGraphs <- extractCores(graphs, labels)

filelist <- ""
for (i in 1:length(labels)) {
	cat("a")
	if (i == 1) {
		filelist <- paste(labels[i],"_core.net",sep="")		
	} else {
		
		filelist <- paste(filelist,",",labels[i],"_core.net",sep="")	
		
	}
	
		write.graph(resultGraphs[[labels[i]]], paste(labels[i],"_core.net",sep=""), format="pajek")
}
resultData <- list(dataUrl=filelist,plotUrl='null')



