require(igraph)

decoratedGraphs <- ""
results <- ""

extract_closeness <- function(graphs, labels) {
	
	isFirst <- TRUE
	sapply(labels, function(l) {
				
				cent <- closeness(graphs[[l]], mode = c("out", "in", "all", "total"), weights = rep(1, length(E(graphs[[l]]))))
				V(graphs[[l]])$dcc <- cent
				
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

extract_closeness(graphs, labels)
resultData <- list(dataUrl=decoratedGraphs, metadata="Directed Closeness Centrality,dcc,double,node,none")