require(igraph)

filelist <- ""

cpm <- function(graph, k) {
  
  graph <- simplify(graph)
  #clq <- cliques(graph, min=k, max=k)
  clq <- maximal.cliques(graph)
  clq <- clq[lapply(clq, length) >= k]
  edges <- c()
  for (i in seq(along=clq)) {
    for (j in i:length(clq)) {
      #if ( length(unique(c(clq[[i]], 
       #                   clq[[j]]))) == k+1 ) {
      if(length(intersect(clq[[i]], clq[[j]])) >= k-1) {
        edges <- c(edges, c(i,j))
      }
    }
  }
  
  if(!is.null(edges))
  {

    clq.graph <- simplify(graph(edges))
    V(clq.graph)$name <- 
      seq(length=vcount(clq.graph))
    comps <- decompose.graph(clq.graph)
   
    cliques <- lapply(comps, function(x) {
     
      unique(unlist(clq[ V(x)$name ]))
    
    })

    V(graph)$clusters <- "["
    i <- 1
    sapply(cliques, function(c) {
      sapply(c, function(m) {
        if (V(graph)[m]$clusters == "[") {
        	V(graph)[m]$clusters <<- paste(V(graph)[m]$clusters,i,sep="")
    	} else {
    		V(graph)[m]$clusters <<- paste(V(graph)[m]$clusters,i,sep=",")
    	}
      })
      i <<- i + 1
    })
    V(graph)$clusters <- paste(V(graph)$clusters,"]",sep="")
    
  }
  
  graph
}

isFirst <- TRUE
sapply(labels, function(l) {
  
  write.graph(cpm(graphs[[l]], k), paste("c_", l, ".gml", sep=""), "gml")
  if (isFirst) {
    
    filelist <<- paste("c_", l, ".gml", sep="")
    isFirst <<- FALSE
    
  } else {
    
    filelist <<- paste(filelist,",c_", l, ".gml", sep="")
  }
})
  
resultData <- list(dataUrl=filelist)



