require(igraph)
require(Matrix)

bipartiteModularity <- function(g, s) {
  
  nodesType0 <- which(V(g)$type == 0)
  nodesType1 <- which(V(g)$type == 1)
  adj <- get.adjacency(g)
  
  deg <- rowSums(adj)
  
  sum(sapply(nodesType0, function(i) {
    
    sapply(nodesType1, function(j) {
      
      if (s[i] == s[j]) {
        
        adj[i,j] - ((deg[i] * deg[j]) / length(E(g)))
        
      } else {
        
        0
      } 
    })  
  })) / length(E(g))  
}

bipartiteModularityGain <- function(g, sOld, sNew, changedNode) {
  
  if (is.null(E(g)$weight)) {
    
    adj <- get.adjacency(g)
    # new
    l <- length(E(g))
  } else {
    
    adj <- get.adjacency(g, attr="weight")
    #new: considering edge weight
    l <- sum(E(g)$weight)
  }
  #l <- length(E(g))
  nodesInCold <- which(sOld == sOld[changedNode])
  nodesInColdType0 <- intersect(nodesInCold, which(V(g)$type == 0))
  nodesInColdType1 <- intersect(nodesInCold, which(V(g)$type == 1))
  
  nodesInCnew <- setdiff(which(sNew == sNew[changedNode]), changedNode)
  nodesInCnewType0 <- intersect(nodesInCnew, which(V(g)$type == 0))
  nodesInCnewType1 <- intersect(nodesInCnew, which(V(g)$type == 1))
  #nodesInC <- which(sNew == sNew[changedNode])
  deg <- rowSums(adj)#degree(g, weighted=TRUE)
  #l <- length(E(g))
  
  ki_in <- sum(adj[changedNode, nodesInCold])
  ki <- deg[changedNode]
  
  if (V(g)[changedNode]$type == 0) {
    
    sumKother <- sum(deg[nodesInColdType1])
  } else {
    
    sumKother <- sum(deg[nodesInColdType0])
  }
 
  if ((length(nodesInColdType0) == 0) || (length(nodesInColdType1) == 0)) {
    
    q0 <- 0
    #c_in <- 0
    #c_tot <- 0
    
  } else {
    c_in <- sum(adj[nodesInColdType0, nodesInColdType1])
    c_tot <- sum(sapply(nodesInColdType0, function(i) { #sum(deg[nodesInCold])
        
      sapply(nodesInColdType1, function(j) {
        
        deg[i] * deg[j]
      })
    }))
    
    
    q0 <- (((c_in - ki_in) / l) - ((c_tot - (ki * sumKother)) / l^2)) - ((c_in / l) - (c_tot / l^2))
  }
  
  #q0
  
  ki_in <- sum(adj[changedNode, nodesInCnew])
  ki <- deg[changedNode]
  
  if (V(g)[changedNode]$type == 0) {
    
    sumKother <- sum(deg[nodesInCnewType1])
  } else {
    
    sumKother <- sum(deg[nodesInCnewType0])
  }
  
  if ((length(nodesInCnewType0) == 0) || (length(nodesInCnewType1) == 0)) {
    
    #q1 <- 0
    c_in <- 0
    c_tot <- 0
  } else {
    
    c_in <- sum(adj[nodesInCnewType0, nodesInCnewType1])
    c_tot <- sum(sapply(nodesInCnewType0, function(i) { #sum(deg[nodesInCold])
      
      sapply(nodesInCnewType1, function(j) {
        
        deg[i] * deg[j]
      })
    }))
  }

  q1 <- (((c_in + ki_in) / l) - ((c_tot + (ki * sumKother)) / l^2)) - ((c_in / l) - (c_tot / l^2))
  #q1
  
  q0 + q1
 
}

optimize <- function(g) {
  
  #print("optimize")
  
  vType1 <- which(V(g)$type == 0)
  vType2 <- which(V(g)$type == 1)
  
  
  clusters <- c()
  update <- TRUE
  numUpdates <- 0
  while(update) {
    update <- FALSE

    sapply(vType1, function(i) {
      
      clusters <<- setdiff(V(g)[neighbors(g, i)]$cluster, V(g)[i]$cluster)
      bestFit <- 0
      bestCluster <- -1
      if (length(clusters) > 0) {
        
        sapply(clusters, function(c) {
            
      
          s1 <- V(g)$cluster
          s2 <- s1
          s2[i] <- c
          gain <- bipartiteModularityGain(g, s1, s2, i)
            
          if (gain > bestFit) {
                    
            bestFit <<- gain
            bestCluster <<- c
      
          }  
        })
      }
      
      if (bestCluster > 0) {
      #  cat("new cluster of ", i, "is ", bestCluster, "\n")
        V(g)[i]$cluster <<- bestCluster
        update <<- TRUE
        numUpdates <<- numUpdates + 1
      }
    })
    
    sapply(vType2, function(i) {
      
      clusters <<- setdiff(V(g)[neighbors(g, i)]$cluster, V(g)[i]$cluster)
      bestFit <- 0
      bestCluster <- -1
      
      if (length(clusters) > 0) {
      
        sapply(clusters, function(c) {
      
          s1 <- V(g)$cluster
          s2 <- s1
          s2[i] <- c
          gain <- bipartiteModularityGain(g, s1, s2, i)
          #print("check ", i , "to", c)  
          if (gain > bestFit) {
      
            bestFit <<- gain
            bestCluster <<- c
          }
        })
      }
      
      if (bestCluster > 0) {
      #  cat("new cluster of ", i, "is ", bestCluster, "\n")
        V(g)[i]$cluster <<- bestCluster
        update <<- TRUE
        numUpdates <<- numUpdates + 1
      }
    })
    
  }
  
  g
}

bipartiteLouvaine <- function(g, preclustering=FALSE) {
  
  if (!preclustering) {
    
    V(g)$cluster <- 1:length(V(g))
  }
  #print("first optimization")
  initGraph <- optimize(g)

  clusterNodeMapping <- list()
  scdLevelGraph <- graph.empty()
  i <- 1
  clustNames <- unique(V(initGraph)$cluster)
  #print("modularity: ")
  #print(bipartiteModularity(initGraph, V(initGraph)$cluster))
  scdLevelGraph <- add.vertices(scdLevelGraph, length(clustNames))
  sapply(1:length(clustNames), function(i) {
    
    nodesInCluster1 <- which(V(initGraph)$cluster == clustNames[i])
    clusterNodeMapping[[i]] <<- nodesInCluster1
    
    sapply(1:length(clustNames), function(j) {
  
      nodesInCluster2 <- which(V(initGraph)$cluster == clustNames[j])
      
      nb <- unlist(neighborhood(initGraph, 1, nodesInCluster2))
      #nb <- nb[which(!(nb %in% nodesInCluster2)] #setdiff(nb, nodesInCluster2)  WRONG!!!
      #linkWeight <- length(intersect(nb, nodesInCluster1))
      
      linkWeight <- length(which(V(initGraph)[nb]$cluster == clustNames[i]))
      
      if (i == j) {
        # because of the included 0 neighborhood.
        linkWeight <- (linkWeight - length(nodesInCluster2)) / 2
      } 
      
      if (linkWeight > 0) {
        
        scdLevelGraph <<- scdLevelGraph + edge(c(i, j), weight=linkWeight)  
      }
    })
  })
  
  scdLevelGraph <- as.undirected(scdLevelGraph)
  
  #plot(scdLevelGraph, edge.label=E(scdLevelGraph)$weight)
  mlRes <- multilevel.community(scdLevelGraph)

  sapply(unique(mlRes$membership), function(mem) {
    
    help <- which(mlRes$membership == mem)
    V(g)[unlist(clusterNodeMapping[help])]$cluster <<- mem
  })
  #print("modularity: ")
  #print(bipartiteModularity(g, V(g)$cluster))
  # workaround for the case that the initial clustering is already a local optimum.
  if (bipartiteModularity(initGraph, V(initGraph)$cluster) > bipartiteModularity(g, V(g)$cluster)) {
    
    V(g)$cluster <- V(initGraph)$cluster
  } else {
    
    #print("second optimization")
    #print("modularity: ")
    g <- optimize(g)  
    #print(bipartiteModularity(g, V(g)$cluster))
  }
  
  g
}

filenames <- sapply(labels, function(lab) {

    g <- graphs[[lab]]

    res <- bipartiteLouvaine(g)

    filename <- paste("cl", lab, ".gml", sep="")
    write.graph(res, filename)

    filename
})
filelist <- paste(filenames, collapse=",")
resultData <- list(dataUrl=filelist)