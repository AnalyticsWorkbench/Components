library('igraph')

#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Fortunato/CiteSeer_Depth3_Fortunato_CN _DoubleEdgesDeleted_YearsCorrected_OnlyOldNodes_Analyzed_GST_CoOcc_CoCit.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Strohman/CiteSeer_Depth2_Strohman_OnlyOldNodes_Analyzed_GST_CoOcc_CoCit.gml', 'gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Teufel/CiteSeer_Depth2_Teufel_OnlyOldNodes_GST_CoOcc_CoCit.gml', 'gml')
g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/Hulth/Hulth_Analyzed_GST_CoOcc_CoCit.gml', 'gml')

outputPath <- 'C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/'

threshold <- 1
nameofweight <- 'topicSimilarity'
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

isFirst <- TRUE
decUrls <- ""
resUrls <- ""
sapply(labels, function(l) {
  
  startTime <- proc.time()

  citList <<- get.adjlist(g, mode="out")
  targets <<- which(degree(g, mode="out") == 0)

  cat("begin edge weighting phase \n")
  if(edgeWeightingSchema == "spc"){  
    g <- spc(g, threshold=threshold)
  }
  else if(edgeWeightingSchema == "spcpost"){
    g <- spcpost(g, threshold=threshold)
  }
  cat("edge weighting phase done \n")

  elapsedTime <- proc.time() - startTime
  print("runtime in seconds: ")
  print(elapsedTime)
  
  write.graph(g, paste(outputPath,l,"_spc.gml",sep=""), "gml")
})
