require(igraph)

resUrls <- ""
decUrls <- ""
filename <- "erdos_renyi.gml"

g<-erdos.renyi.game(n,edge_prob, type=c("gnp", "gnm"), directed=dir, loops=loops_bool)

write.graph(g, filename, "gml")

resultData <- list(dataUrl=filename)

