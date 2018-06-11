require(igraph)

decoratedGraphs <- ""
results <- ""

extract_degrees <- function(graphs, labels) {
	
	isFirst <- TRUE
	sapply(labels, function(l) {
				
				degrees <- graph.strength(graphs[[l]],  mode = c("out"))
				V(graphs[[l]])$ost <- degrees
				
				write.graph(graphs[[l]], paste(l,".gml", sep=""), "gml")
				write.csv(degrees, paste(l,".csv", sep=""))
				
				if (isFirst) {
					
					isFirst <<- FALSE
					decoratedGraphs <<- paste(l, ".gml", sep="")
					results <<- paste(l,".csv", sep="")
				} else {
					
					decoratedGraphs <<- paste(decoratedGraphs, ",", l, ".gml", sep="")
					results <<- paste(results, ",", l, ".csv", sep="")
				}
			})
}

extract_degrees(graphs, labels)
resultData <- list(dataUrl=decoratedGraphs, metadata="Outgoing Strength,ost,double,node,none")