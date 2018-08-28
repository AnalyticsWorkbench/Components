# Author: steinert
###############################################################################
library('igraph')

g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/graph_nodeSubset.gml','gml')
#g <- read.graph('C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/1_mpdec.gml','gml')
#outputPath <- 'C:/Users/steinert/eigene Forschung/Weighted_Citation_Networks/SVN/Versuche/Prediction_Evaluation/'

k <- 10
nameRecommendationFlag <- 'isInNodeSubset'
#nameRecommendationFlag <- 'mp'

decUrls <- ""
resUrls <- ""

###############################################################################
sapply(labels, function(l) {
  graph <- g
   
	V(graph)$recommendation <- get.vertex.attribute(graph, nameRecommendationFlag)	
	# Recommendations marked by Find Node Subset 
	recommendations = V(graph)[which(V(graph)$recommendation=='true')]
	# recommendations marked by Main Path
	recommendations = append(recommendations, V(graph)[which(V(graph)$recommendation=='TRUE')])

	# assign 'label' attribute
	for (v in V(graph)){
		V(graph)[v]$label <- get.vertex.attribute(graph, "label", index=v)
	}
	
#	print(V(graph)[recommendations]$label)
	
	# disable any existing weights, otherwise this is used by the betweenness centraliyt measure
	E(graph)$weight <- 1
	
	pageRankScores <- page.rank(graph, vids=recommendations, directed=TRUE)$vector
	
	labelpageRankScoresArray <- list()
	for(i in 1:length(recommendations)){
		labelpageRankScoresArray[[i]] <- c(label=as.numeric(V(graph)[recommendations[i]]$label), score=as.numeric(pageRankScores[i]))
	}

	# sort by second entry, i.e. betweenness centrality score, decreasing
	sortedLabelpageRankScoresArray <- labelpageRankScoresArray[order(sapply(labelpageRankScoresArray, '[[', 2), decreasing=TRUE)]
	
	if(k>length(recommendations)){
		k <- length(recommendations)
	}
	
	# copy all prints to file (output)
	filename <- paste(outputPath,"topKRecommendations.txt",sep="")
#	sink(filename, append=FALSE, split=FALSE)
	
	V(graph)$seed <- get.vertex.attribute(graph, "seed")
	seed <- V(graph)[which(V(graph)$seed=='true')]
	seedLabel <- get.vertex.attribute(graph, "label", index=seed)
	
	seedWasExcluded = FALSE
	print("Top k recommendations:")
	for(i in 1:k){
		# exclude seed
		if(sortedLabelpageRankScoresArray[[i]][1] == seedLabel){
			print("Seed excluded")	
			seedWasExcluded = TRUE
		}
		else {
			print(paste("Node Label", sortedLabelpageRankScoresArray[[i]][1],"with score",sortedLabelpageRankScoresArray[[i]][2], sep=" "))
		}
	}
	if(seedWasExcluded){
		if((k+1) <= length(recommendations)){
			print(paste("Node Label", sortedLabelpageRankScoresArray[[(k+1)]][1],"with score",sortedLabelpageRankScoresArray[[(k+1)]][2], sep=" "))
		}
	}
#	sink()

#    # write decorated graph
#   	write.graph(graph, "graph.gml", "gml")
#    decUrls <<- "graph.gml"
    
#    # write result
#	resUrls <<- filename
#  print(filename)
	
})

resultData <- list(dataUrl=resUrls, decoratedUrl=decUrls)