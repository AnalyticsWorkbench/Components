require(igraph)

filenames <- c()
graphStatistics <- function(graphs, labels) {
  
  isFirst <- TRUE
  sapply(labels, function(l) {
    
    g <- graphs[[l]]
    isDirected <- is.directed(g)
    
    diameter <- diameter(g, directed=isDirected)
    
    if (isDirected) {
      
      density <- length(E(g)) / length(V(g))^2 
    } else {
      
      density <- length(E(g)) / (length(V(g)) * (length(V(g)) - 1))
    }
    
    stats <- matrix(c(length(V(g)), length(E(g)), density, diameter), nrow=1)
    colnames(stats) <- c("num nodes", "num edges", "density", "diameter")
    
    filename <- paste("stats_", l, ".csv", sep="")
    write.csv(stats, filename)
    filenames <<- append(filenames, filename)
  })
}

graphStatistics(graphs, labels)

resultData <- list(dataUrl=filenames)