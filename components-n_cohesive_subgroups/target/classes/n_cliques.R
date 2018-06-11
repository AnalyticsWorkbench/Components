n_cliques <- function(graph, n) {
  
  n_graph <- connect.neighborhood(graph, n)
  
  cliques <- maximal.cliques(n_graph)
  
  cliques
}

n_clans <- function(graph, n) {
  
  candidates <- n_cliques(graph, n)
  
  
  n_clans <- list()
  i <- 1
  if (n>1) {
    
    sapply(candidates, function(cand) {
      
      
      sg <- induced.subgraph(graph, as.vector(cand))
      
      if (diameter(sg) <= n) {
        
        n_clans[[i]] <<- cand
        i <<- i + 1
      }
      
    })
  }
  
  n_clans
}


getCohesiveSubgroups <- function(graphs, n, func=n_clique) {
  
  lapply(graphs, function(g) {
    
    subgroups <- func(g, n)
    
    V(g)$clusters <- "["
    i <- 1
    sapply(subgroups, function(sg) {
      sapply(sg, function(m) {
        if (V(g)[m]$clusters == "[") {
          V(g)[m]$clusters <<- paste(V(g)[m]$clusters,i,sep="")
        } else {
          V(g)[m]$clusters <<- paste(V(g)[m]$clusters,i,sep=",")
        }
      })
      i <<- i + 1
    })
    V(g)$clusters <- paste(V(g)$clusters,"]",sep="")
    
    g
  })
}

if (method == "n-cliques") {
  
  type <- n_cliques
} else if (method == "n-clans") {
  
  type <- n_clans
}

resGraphs <- getCohesiveSubgroups(graphs, n, type)
filenames <- ""
isFirst <- TRUE


sapply(1:length(labels), function(i) {
  filename <- paste(method, "_", labels[i],".gml", sep="")
  if (isFirst) {
    
    isFirst <<- FALSE
    filenames <<- filename
  } else {
    
    filenames <<- paste(filenames, ",", filename, sep="")
  }
  
  write.graph(resGraphs[[i]], filename, "gml")
})

resultData <- list(dataUrl=filenames)