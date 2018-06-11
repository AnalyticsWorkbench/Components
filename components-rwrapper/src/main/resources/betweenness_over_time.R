require(igraph)
source("dynamics.R")

betweennessCentrality <- function(graph) {

	N <- length(V(graph))
	betw <- betweenness(graph, directed=is.directed(graph)) / ((N - 1)*(N - 2)/2)
	
	betw
}

resultData <- list(dataUrl='dynamics.data',plotUrl='dynamics.png')

png('dynamics.png')
data <- centralityDynamics(graphs, labels, betweennessCentrality)
#write.table(data, file="dynamics.data")
write.csv(data, file="dynamics.csv", row.names=TRUE)
dev.off()
