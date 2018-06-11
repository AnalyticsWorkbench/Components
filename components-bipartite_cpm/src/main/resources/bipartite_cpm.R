computeBicliques <- function(graph, k, l) {
  
  graph <- connect.neighborhood(graph, 2)
  
  cliques <- maximal.cliques(graph)
  
  bicliques <- list()
  j <- 1
  sapply(1:length(cliques), function(i) {
    type0Members <- which(V(graph)[cliques[[i]]]$type == 0)
    type1Members <- which(V(graph)[cliques[[i]]]$type == 1)                    
    if ((length(type0Members) >= k) && (length(type1Members) >= l)) {
      
      bicliques[[j]] <<- list(m1=cliques[[i]][type0Members], m2=cliques[[i]][type1Members])
      j <<- j + 1
    }
  })
  
  bicliques
}

filelist <- ""

cpm <- function(graph, k, l) {
  
  graph <- simplify(graph)
  
  clq <- computeBicliques(graph, k, l)
  clq <- clq[!sapply(clq, is.null)]
  edges <- c()
  for (i in seq(along=clq)) {

    for (j in i:length(clq)) {
      #if ( length(unique(c(clq[[i]],
      #                   clq[[j]]))) == k+1 ) {
      
      if((length(intersect(clq[[i]]$m1, clq[[j]]$m1)) >= k-1) &&
           (length(intersect(clq[[i]]$m2, clq[[j]]$m2)) >= l-1)) {

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
sapply(labels, function(lab) {
  
  write.graph(cpm(graphs[[lab]], k, l), paste("c_", lab, ".gml", sep=""), "gml")
  if (isFirst) {
    
    filelist <<- paste("c_", lab, ".gml", sep="")
    isFirst <<- FALSE
    
  } else {
    
    filelist <<- paste(filelist,",c_", lab, ".gml", sep="")
  }
})

resultData <- list(dataUrl=filelist)