require(igraph)

resUrls <- ""
decUrls <- ""
filename <- "preferential_attachment.gml"

g<-sample_pa(n, power = pa_power, m = edges_added)

write.graph(g, filename, "gml")

resultData <- list(dataUrl=filename)

