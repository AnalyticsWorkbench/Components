require(igraph)

extractCores <- function(graphs, labels, k) {
	
  graphCores <- list()
  
  sapply(labels, function(l) {
    
    graph <- graphs[[l]]
    
    crn <- graph.coreness(graph)
    
    coreNodes <- which(crn >= k)
    
    ids <- c()
    
    if (length(coreNodes) > 0) {
      
      sg <- induced.subgraph(graph, coreNodes)
    } else {
      
      sg <- graph.empty()
    }
    graphCores[[l]] <<- sg
  })
  
  graphCores
}


resultGraphs <- extractCores(graphs, labels, k)

filelist <- ""
isFirst <- TRUE
sapply(labels, function(l) {

    if (isFirst) {

        filelist <<- paste(l,"_core.gml",sep="")
        isFirst <<- FALSE
    } else {

        filelist <<- paste(filelist,",",l,"_core.gml",sep="")
    }
    
    write.graph(resultGraphs[[l]], paste(l,"_core.gml",sep=""), "gml")
})

resultData <- list(dataUrl=filelist)