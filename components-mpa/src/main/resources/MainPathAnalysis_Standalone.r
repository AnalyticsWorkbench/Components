library('igraph')
g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Hulth/tss=unweightedSum/Hulth_Analyzed_GST_CoOcc_CoCit.gml','gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Fortunato/Fortunato_Analyzed_GST_CoOcc_CoCit_tss=3xCocit+LingSim.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Teufel/Teufel_Analyzed_GST_CoOcc_CoCit_tss=3xCoCit+LingSim.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Hulth/Hulth_Analyzed_GST_COOcc_CoCit_tss=3xcoCit+lingSim.gml','gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Hulth/tss=unweightedSum/Hulth_Analyzed_GST_CoOcc_CoCit.gml','gml')
#g <- read.graph('/home/steinert/CiteSeer_Depth2_Strohman_OnlyOldNodes_Analyzed_GST_CoOcc_CoCit.gml', 'gml')
outputPath <- 'C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Hulth/'
#outputPath <- '/home/steinert/'
threshold <- 0.8
nameofweight <- 'graphSimilarity'
#nameofweight <- 'linguisticSimilarity'
#nameofweight <- 'topicSimilarity'
mpdetection <- 'local'
#mpdetection <- 'global'
#edgeWeightingSchema <- 'existingWeights'
#edgeWeightingSchema <- 'spc'
edgeWeightingSchema <- 'spcpost'

mpUrls <- ""
decUrls <- ""

#=========================================================
#############    Edge Weighting Mechanisms   #############
#=========================================================
require(igraph)

conventional <- FALSE

forward <- function(g, topOrder, inlist, outlist) {
  cat("forward \n")
  isSource <- TRUE
  
  outEdgesIds <- c()
  inEdgesEis <- c()
  
  sapply(topOrder, function(v) {
    
    if (!isSource) {
      outEdgesIds <<- get.edge.ids(g, as.vector(rbind(rep(v,length(outlist[[v]])),outlist[[v]])))
      inEdgesIds <<- get.edge.ids(g, as.vector(rbind(inlist[[v]], rep(v,length(inlist[[v]])))))    	
      E(g)[outEdgesIds]$weightF <<- sum(E(g)[inEdgesIds]$weightF)
      V(g)[v]$weightF <<- sum(V(g)[inlist[[v]]]$weightF)						
    } else {
      isSource <<- FALSE
      outEdgesIds <<- get.edge.ids(g, as.vector(rbind(rep(v,length(outlist[[v]])),outlist[[v]])))
      E(g)[outEdgesIds]$weightF <<- 1
      V(g)[v]$weightF <<- 1
    }
  })
  
  g
}

backward <- function(g, revTopOrder, inlist, outlist) {
  cat("backward \n")
  isSink <- TRUE
  
  outEdgesIds <- c()
  inEdgesEis <- c()
  
  sapply(revTopOrder, function(v) {
    
    if (!isSink) {
      
      outEdgesIds <<- get.edge.ids(g, as.vector(rbind(rep(v,length(outlist[[v]])),outlist[[v]])))
      inEdgesIds <<- get.edge.ids(g, as.vector(rbind(inlist[[v]], rep(v,length(inlist[[v]])))))
      
      E(g)[inEdgesIds]$weightB <<- sum(E(g)[outEdgesIds]$weightB)
      V(g)[v]$weightB <<- sum(V(g)[outlist[[v]]]$weightB)
      
      E(g)[outEdgesIds]$weight <<- E(g)[outEdgesIds]$weightF * E(g)[outEdgesIds]$weightB
      
      V(g)[v]$weight <<- V(g)[v]$weightF * V(g)[v]$weightB  				
      
    } else {
      
      isSink <<- FALSE
      inEdgesIds <- get.edge.ids(g, as.vector(rbind(inlist[[v]], rep(v,length(inlist[[v]])))))
      E(g)[inEdgesIds]$weightB <<- 1
      V(g)[v]$weightB <<- 1
      V(g)[v]$weight <<- V(g)[v]$weightF
      
    }		
  })
  
  V(g)[revTopOrder[length(revTopOrder)]]$weight <- 1
  g
}


determineStartAndEndpoints <- function(graph, sources, sinks, threshold=1) {
  
  if (conventional) {
    
    ### conventional approach
    
    outlist <- get.adjlist(graph, mode="out")
    maxWeight <- 0
    startPoints <- c()
    
    maxEW <- max(weights)
    
    sapply(sources, function(s) {
      
      neighbours <- outlist[[s]]		
      
      if (length(neighbours) > 0) {
        
        edgeIds <- get.edge.ids(graph, as.vector(rbind(rep(s,length(neighbours)), neighbours)))	
        
        weights <- E(graph)[edgeIds]$weight
        
        maxEW <- max(weights)
        
        if (maxEW > maxWeight) {
          
          maxWeight <<- maxEW
          startPoints <<- c(s)
        } else if (maxEW >= maxWeight) {
          
          startPoints <<- append(startPoints, s)
        }
        
        edgeIds <- get.edge.ids(graph, as.vector(rbind(rep(s,length(neighbours)), neighbours)))	
        
        
        if (maxEW >= (maxWeight * threshold)) {
          
          startPoints <<- append(startPoints, s)
        } 
      }
    })
    
    V(graph)[startPoints]$startpoint <- TRUE
    
  } else { 
    ### new approach
    # startpoints
    edgeIds <- get.edge.ids(graph, as.vector(rbind(rep((length(V(graph)) - 1),length(sources)), sources)))
    sourceWeights <- E(graph)[edgeIds]$weight
    V(graph)[sources[which(sourceWeights >= max((sourceWeights * threshold)))]]$startpoint <- TRUE
  }
  
  V(graph)[sinks]$endpoint <- TRUE
  
  graph
}

spc <- function(graph, topOrder=NULL, threshold=1) {
  
  standalone <- FALSE
  
  
  if (is.null(topOrder)) {
    standalone <- TRUE
    oldSize <- length(V(graph))
    
    sources <- which(degree(graph, mode="in") == 0)
    sinks <- which(degree(graph, mode="out") == 0)
    
    graph <- add.vertices(graph, 2)
    graph <- add.edges(graph, as.vector(rbind(rep((oldSize + 1),length(sources)),sources)))
    graph <- add.edges(graph, as.vector(rbind(sinks, rep((oldSize + 2),length(sinks)))))
    topOrder <- topological.sort(graph)
  }
  inlist <- get.adjlist(graph, mode="in")
  outlist <- get.adjlist(graph, mode="out")
  
  graph <- forward(graph, topOrder, inlist, outlist)
  graph <- backward(graph,rev(topOrder), inlist, outlist)
  
  if (standalone) {
    
    graph <- determineStartAndEndpoints(graph, sources, sinks, threshold=threshold)
    graph <- delete.vertices(graph, V(graph)[(length(V(graph)) - 1):length(V(graph))])
    
    #write.graph(graph, "spc_weighted.gml", "gml")
  }
  graph
}

spcpost <- function(graph, topOrder=NULL, threshold=1) {
  
  oldWeights <- get.edge.attribute(g, nameofweight, index=E(g))
  
  graph <- spc(graph, topOrder, threshold)
  
  # multiply spc with old weight
  E(graph)$weight <- E(graph)$weight * oldWeights
  
  graph
}

existingWeights <- function(graph, topOrder=NULL, threshold=1) {
  cat("weight for main path calculation:", nameofweight, "\n")
  for (e in E(graph)){
    E(graph)[e]$weight <- get.edge.attribute(graph, nameofweight, index=e)
  }
  
  sources <- which(degree(graph, mode="in") == 0)	
  V(graph)[sources]$startpoint <- TRUE
    
  graph
}

#=========================================================
############   Main Path Detection Variants   ############
#=========================================================
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

mpa <- function(g, ewScheme, th=1) {
  
  V(g)$mp <- "FALSE"
  V(g)$startpoint <- FALSE
  V(g)$endpoint <- FALSE
  E(g)$mp <- "FALSE"
  citList <<- get.adjlist(g, mode="out")
  targets <<- which(degree(g, mode="out") == 0)
  
  cat("begin edge weighting phase \n")
  g <- ewScheme(g, threshold=th)
  cat("edge weighting phase done \n")
  
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
sapply(c("1"), function(l) {
  
  # redirects all outputs to a file (and to the screen)
  f  = "C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/mp_output.csv"
  if(file.exists(f)){
    file.remove(f)
    file.create(f)
  }  
  sink(f, append=FALSE, split=TRUE)  
  
  if(edgeWeightingSchema == "spc"){
    g <- mpa(g, spc, th=threshold)       
  }
  else if(edgeWeightingSchema == "spcpost"){
    g <- mpa(g, spcpost, th=threshold)       
  }
  else if(edgeWeightingSchema == "existingWeights"){
    g <- mpa(g, existingWeights, th=threshold)       
  }
    
  write.graph(g, paste(outputPath,l,"_mpdec.gml",sep=""), "gml")
  
  sg <- subgraph.edges(g, which(E(g)$mp == "true"))
  write.graph(sg, paste(utPath,l,"_mp.gml",sep=""), "gml")
  
  print(paste("no. of nodes on main path:", length(V(sg))))
  print("labels of nodes on main path:")
  for (v in V(sg)){
    print(V(sg)[v]$label)
  }
  
})

resultData <- list(dataUrl=resUrls, decoratedUrl=decUrls, metadata="edge weight,weight,double,edge,none")
