require(igraph)

decoratedGraphs <- ""
results <- ""

extract_evcent <- function(graphs, labels) {
  
  isFirst <- TRUE
  sapply(labels, function(l) {
    
    cent <- evcent(graphs[[l]], directed=TRUE, weights = rep(1, length(E(graphs[[l]]))))
    V(graphs[[l]])$ec <- cent$vector
    
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

extract_evcent(graphs, labels)
resultData <- list(dataUrl=decoratedGraphs, metadata="Eigenvector Centrality,ec,double,node,none")