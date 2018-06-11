require(igraph)
source("dynamics.R")

degreeCentrality <- function(graph) {

	N <- length(V(graph))
	d <- degree(graph)
	
	centralities = c()
	
	if (is.directed(graph)) {
		
		for (i in 1:length(d)) {
			
			centralities[i] <- d[i] / (2 * (N - 1))
		}	
	} else {
		for (i in 1:length(d)) {
	
			centralities[i] <- d[i] / (N - 1)
		}
	}
	
	centralities
}

resultData <- list(dataUrl='dynamics.csv',plotUrl='dynamics.png')

png('dynamics.png',width=900,height=700)
#data <- centralityDynamics(graphs, labels, degreeCentrality)
data <- centralityDynamics(graphs, labels, degree)
#write.table(data, file="dynamics.data")
row.names
write.csv(data, file="dynamics.csv", row.names=TRUE)
dev.off()
