# Author: steinert
###############################################################################


# if(CommunityDetectionAlgorithm == "Edge Betweennes"){
#   cdFunction <- edge.betweenness.community
# }
# if(CommunityDetectionAlgorithm == "Edge Betweennes"){
#   cdFunction <- edge.betweenness.community
# }
# if(CommunityDetectionAlgorithm == "Edge Betweennes"){
#   cdFunction <- edge.betweenness.community
# }

modularityclustering <- function(g) {
  
  if(nchar(weightName)>0){	
    #		print(get.edge.attribute(g, weightName))
    fc <- fastgreedy.community(g, weights=get.edge.attribute(g, weightName))	
  }
  else {
    fc <- fastgreedy.community(g)		
  }
  
  com<-membership(fc)
  V(g)$clusters <- paste("[",com,"]", sep="")
  
  # return graph
  g
}


isFirst <- TRUE
decUrls <- ""
resUrls <- ""

# for all graphs compute the max flow
sapply(labels, function(l) {

    graph <- graphs[[l]]
    
     # replace ',' with '_'
    if(grepl(",", l)){
	    l = gsub(",", "_", l)
    }

    # make graph undirected
    graphUndirected <- as.undirected(graph, mode="each")

    #perform modularity clustering
    g <- modularityclustering(graphUndirected)
    
    #write results
    write.graph(g, paste(l,"_modularityClustering.gml",sep=""), "gml")

    if (isFirst) {
        isFirst <<- FALSE
        resUrls <<- paste(l,"_modularityClustering.gml",sep="")
        
    } else {
        resUrls <<- paste(resUrls,",",l,"_modularityClustering.gml",sep="")
    }
})

resultData <- list(dataUrl=resUrls, decoratedUrl=decUrls)