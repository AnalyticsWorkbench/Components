require(blockmodeling)
require(igraph)

network <- graphs[[1]]

sim_table <- tables[[1]]

del_list <- c("id", "label")

id <- "id"
label <- "label"


# delete all non-values
sim_table <- subset(sim_table, select = -id)
sim_table <- subset(sim_table, select = -label)

d <- as.dist(1 - sim_table)
h <- hclust(d, method = "ward.D")
part <- cutree(h, clustCount)

nodesConnected <- length(which(igraph::degree(network) > 0))
dens <- length(E(network)) / (nodesConnected * (nodesConnected - 1))

# calculate the blockmodel
result <- crit.fun(get.adjacency(network, sparse=FALSE), part, approach="bin", blocks=blockRelations, blockWeights=c(null=1,reg=dens/(1-dens)), norm=TRUE)

## create output for cluster relations
nodeCount <- dim(result$IM)[1]
g <- graph.full(n = nodeCount, directed = TRUE, loops = TRUE)

edgeAttributes <- c()

for (i in 1:nodeCount) {
  for (j in 1:nodeCount) {
    edgeAttributes <- c(edgeAttributes, result$IM[i,j])
  }
}
# add the edgeAttributes to the corresponding edge
E(g)$relation <- edgeAttributes

# add cluster affiliation to the nodes
vertexCount <- vcount(network)
for (n in 1:vertexCount) {
  network <- set.vertex.attribute(graph = network, name = "cluster", index = n, value=result$clu[n])
}

# output for the block relations
blockRel <- "block_relations.gml"

# output for the cluster affiliation
clusterAffil <- "cluster_affiliation.gml"

write.graph(g, blockRel, "gml")
write.graph(network, clusterAffil, "gml")

resultData <- list(dataUrl=blockRel, decoratedUrl=clusterAffil)