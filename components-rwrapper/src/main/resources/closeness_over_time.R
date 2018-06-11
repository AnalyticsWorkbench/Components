require(igraph)
source("dynamics.R")

resultData <- list(dataUrl='dynamics.data',plotUrl='dynamics.png')

png('dynamics.png')
data <- centralityDynamics(graphs, labels, closeness)
#write.table(data, file="dynamics.data")
write.csv(data, file="dynamics.csv", row.names=TRUE)
dev.off()
