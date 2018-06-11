require(blockmodeling)
require(igraph)

result <- sapply(labels, function(l) {
  filename <- paste("regular_similarity", "_", l,".csv", sep="")
  regularSimililarity <- REGE.for(get.adjacency(graphs[[l]], sparse=FALSE))$E
  
  write.csv(regularSimililarity, filename, row.names = FALSE)
  filename
  
})

resultData <- list(dataUrl=paste(result, collapse = ","))
