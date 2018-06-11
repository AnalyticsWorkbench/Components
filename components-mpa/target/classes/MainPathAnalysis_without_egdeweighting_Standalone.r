library('igraph')

#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Fortunato_spc.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Fortunato_spc_post.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Strohman_spc.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Strohman_spc_post.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Teufel_spc.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Teufel_spc_post.gml', 'gml')
g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Hulth_spc.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Hulth_spc_post.gml', 'gml')
outputPath <- 'C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/'

threshold <- 1
#mpdetection <- 'local'
mpdetection <- 'global'

mpUrls <- ""
decUrls <- ""

require(igraph)

conventional <- FALSE
citList <- NULL
targets <- NULL

identifyLocalMainPath <- function(g, sources, threshold=1) {
  #vertices <- append(vertices, source)
  V(g)[sources]$mp <- "TRUE"
  
  for (s in sources) {
    citations <- citList[[s]]
    outEdgeIds <- get.edge.ids(g, as.vector(rbind(rep(s,length(citations)), citations)))
    
    if (!(s %in% targets)) {
      indexes <- which(E(g)[outEdgeIds]$weight >= (max(E(g)[outEdgeIds]$weight) * threshold))
      E(g)[outEdgeIds[indexes]]$mp <- "TRUE"
      choise <- citations[indexes]
      #edges <- append(edges, choise)
      
      g <- identifyLocalMainPath(g, choise, threshold)
    }
  }
  
  g
}

identifyGlobalMainPath <- function(g, threshold=1, sources) {
  results <- list("path" = list(), "sumOfWeights"=c())
  
  # create global hashmap to save results
  resultsHashMap <<- c()
  
  for (s in sources) {
    tempResults <- findMaximalPathToTarget(g, s, threshold)
    
    if (length(tempResults$path) > 0){
      if (length(results$path) > 0){
        results$sumOfWeights <- append(results$sumOfWeights, tempResults$sumOfWeights)
      } else {
        results$sumOfWeights <- tempResults$sumOfWeights
      }
      for(i in 1:length(tempResults$path)){
        results$path[[length(results$path)+1]] <- tempResults$path[[i]]  		
      }
    }
  }	
  
  if (length(results$sumOfWeights) > 0){	
    # find the highest weight of all paths
    index <- 1
    maxLength <- results$sumOfWeights[[1]]
    for (i in 2:length(results$path)){
      len <- results$sumOfWeights[[i]]
      if (len > maxLength){
        maxLength <- results$sumOfWeights[[i]]
        index <- i
      }
    }
    
    # find out which paths have a sumOfWeights within tolerance of maxLength
    indexesOfPathsWithinTolerance <- which(results$sumOfWeights >= (maxLength * threshold))
    
    # add edges of these paths to list
    edgesOnMainPath <- list()
    for(i in indexesOfPathsWithinTolerance){
      edgesOnMainPath <- append(edgesOnMainPath, results$path[[i]])
    }
    
    # mark these edges and their corresponding nodes as lying on mp
    for(e in edgesOnMainPath){
      E(g)[e]$mp <- "TRUE"
      # get endpoints of edge e, mark them as lying on main path
      V(g)[get.edges(g,E(g)[e])[1]]$mp <- "TRUE"
      V(g)[get.edges(g,E(g)[e])[2]]$mp <- "TRUE"			
    }
  }
  
  g
}

findMaximalPathToTarget <- function(g, s, threshold=1) {
  results <- list("path" = list(), "sumOfWeights"=c())
  
  if (is.element(s, targets)){
    # end of recursion
    return(results)	
  } else {	
    
    # check if this node has been traversed before
    # if it was traversed, the results have been stored in the hashmap
    if (as.character(s) %in% names(resultsHashMap)){
      return(resultsHashMap[[as.character(s)]])
    }
    
    # else
    # get ids of nodes s is connected to
    citations <- citList[[s]]
    outEdgeIds <- get.edge.ids(g, as.vector(rbind(rep(s,length(citations)), citations)))
    
    for (o in outEdgeIds){		
      weight <- E(g)[o]$weight
      
      # get endpoint of edge o
      newSource <- get.edges(g,E(g)[o])[2]
      
      # for every connected node calculate maximal path to targets
      tempResults <- findMaximalPathToTarget(g, newSource, threshold)			
      
      # incorporate results			
      if(length(tempResults$path) > 0){
        for (i in 1:length(tempResults$path)){					
          w <- weight + tempResults$sumOfWeights[[i]]
          
          if(length(results$path) > 0){
            results$sumOfWeights <- append(results$sumOfWeights, w)
            path <- append(tempResults$path[[i]], o)
            
            results$path[[length(results$path)+1]] <- path
          } else {
            results$sumOfWeights <- w		
            path <- append(tempResults$path[[i]], o)						
            results$path[[length(results$path)+1]] <- path
          }					
        }
      } else {
        w <- c(weight)
        if(length(results$path) > 0){
          results$sumOfWeights <- append(results$sumOfWeights, w)
          results$path[[length(results$path)+1]] <- list(o)
        } else {
          results$sumOfWeights <- w			
          results$path[[length(results$path)+1]] <- list(o)
        }
      }
    }			
  }
  
  # save results in case this node is inspected again
  resultsHashMap[[as.character(s)]] <<- results
  
  return(results)	
}

mpa <- function(g, th=1) {
  
  V(g)$mp <- "FALSE"
  E(g)$mp <- "FALSE"
  citList <<- get.adjlist(g, mode="out")
  targets <<- which(degree(g, mode="out") == 0)
  
  if(mpdetection == "local"){
    cat("local path identification phase \n")
    g <- identifyLocalMainPath(g, which(V(g)$startpoint == TRUE), th)
    cat("local path identification phase done \n")  
  }
  else if (mpdetection == "global"){
    cat("global path identification phase \n")
    sources <- which(degree(g, mode="in") == 0)
    g <- identifyGlobalMainPath(g, th, sources)
    cat("global path identification phase done \n")		
  } 
  else if (mpdetection == "globallocal"){
    cat("globallocal path identification phase \n")
    
    # first determine global main paths
    sources <- which(degree(g, mode="in") == 0)
    g <- identifyGlobalMainPath(g, th, sources)		
    # then use all vertices on main path as seeds for local main path detection
    sources2 <- which(V(g)$mp == TRUE)
    V(g)$mp <- "FALSE"
    E(g)$mp <- "FALSE"
    g <- identifyLocalMainPath(g, sources2, th)		
    
    cat("globallocal path identification phase done \n")		
  } else {
    cat("unknown mpdetection method required \n")			
  }
  
  g
}

isFirst <- TRUE
decUrls <- ""
resUrls <- ""
sapply(labels, function(l) {
  
  startTime <- proc.time()
  
  g <- mpa(g, th=threshold)       
  
  elapsedTime <- proc.time() - startTime
  print("runtime in seconds: ")
  print(elapsedTime)

  write.graph(g, paste(outputPath,l,"_mpdec.gml",sep=""), "gml")  

  sg <- subgraph.edges(g, which(E(g)$mp == "TRUE"))
  write.graph(sg, paste(outputPath,l,"_mp.gml",sep=""), "gml")
  
})

resultData <- list(dataUrl=resUrls, decoratedUrl=decUrls, metadata="edge weight,weight,double,edge,none")
