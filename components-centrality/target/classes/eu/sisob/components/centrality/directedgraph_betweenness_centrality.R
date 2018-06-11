require(igraph)

decoratedGraphs <- ""
results <- ""

extract_betweenness <- function(graphs, labels) {
	
	isFirst <- TRUE
	sapply(labels, function(l) {
				
				cent <- betweenness(graphs[[l]], directed = TRUE, weights = rep(1, length(E(graphs[[l]]))))
				V(graphs[[l]])$dbc <- cent
				
				write.graph(graphs[[l]], paste(l,".gml", sep=""), "gml")
				write.csv(cent, paste(l,".csv", sep=""))
				
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

extract_betweenness(graphs, labels)
resultData <- list(dataUrl=decoratedGraphs, metadata="Directed Betweenness Centrality,dbc,double,node,none")