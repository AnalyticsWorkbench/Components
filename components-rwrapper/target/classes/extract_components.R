require(igraph)
getComponents <- function(g) {
  
  cl <- clusters(g)$membership
  compIds <- unique(cl)
  
  lapply(compIds, function(id) {
    
    induced.subgraph(g, which(cl == id))
  })
}

isFirst <- TRUE
filenames <- ""
sapply(labels, function(l) {
  
  g <- graphs[[l]]
  
  components <- getComponents(g)
  
  sapply(1:length(components), function(i) {
    
    filename <- paste(l, "_", i, ".gml", sep="")
    write.graph(components[[i]], filename, "gml")
    if (isFirst) {
      
      isFirst <<- FALSE
      filenames <<- filename
    } else {
      
      filenames <<- paste(filenames, ",", filename, sep="")
    }
  })
})

resultData <- list(dataUrl=filenames)