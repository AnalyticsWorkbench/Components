require(igraph)

readPartitions <- function(graphs, labels, partitions) {
	
	coloredGraphs <- list()
	files <- ""
	for (i in 1:length(labels)) {
		
		graph <- graphs[[labels[i]]]
		part <- partitions[[labels[i]]]
		
		colors <- rainbow(max(part))
		for (j in 1:length(part)) {
			
			V(graph)[j - 1]$color <- colors[part[j]]
		}
		coloredGraphs[[labels[i]]] <- graph
		
		if (i == 1) {
			
			files <- paste(labels[i],".png",sep="")
		} else {
			
			files <- paste(files,",",labels[i],".png",sep="")
		}
		
		png(paste(labels[i],".png",sep=""));
		plot(graph, vertex.color=V(graph)$color, layout=layout.fruchterman.reingold, main=labels[i])
		dev.off()
	}	
	
	coloredGraphs
}


resultGraphs <- readPartitions(graphs, labels, partitions)

filelist <- ""
for (i in 1:length(labels)) {
	
	if (i == 1) {
		filelist <- paste(labels[i],"_part.net",sep="")		
	} else {
		filelist <- paste(filelist,",",labels[i],"_part.net",sep="")
	}
	write.graph(resultGraphs[[labels[i]]], paste(labels[i],"_part.graphml",sep=""), format="graphml")
}
resultData <- list(dataUrl=filelist,plotUrl='null')

