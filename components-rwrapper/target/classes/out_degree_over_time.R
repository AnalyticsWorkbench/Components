require(igraph)
source("dynamics.R")

degreeCentrality <- function(graph) {
	
	N <- length(V(graph))
	deg <- degree(graph, mode="out") / (N - 1)
	
	deg
}

resultData <- list(dataUrl='dynamics.data',plotUrl='dynamics.png')

png('dynamics.png',width=900,height=700)
data <- centralityDynamics(graphs, labels, degreeCentrality)
#write.table(data, file="dynamics.data")
write.csv(data, file="dynamics.csv", row.names=TRUE)
dev.off()



