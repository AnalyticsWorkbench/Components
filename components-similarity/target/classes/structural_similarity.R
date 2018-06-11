require(blockmodeling)
require(igraph)

result <- sapply(labels, function(l) {
  filename <- paste("structural_similarity", "_", l,".csv", sep="")
  structuralSimililarity <- 1 - as.matrix(sedist(get.adjacency(graphs[[l]], sparse=FALSE), method="euclidean"))
  
  write.csv(structuralSimililarity, filename, row.names = FALSE)
  filename
  
})

resultData <- list(dataUrl=paste(result, collapse = ","))
