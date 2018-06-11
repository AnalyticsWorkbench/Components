require(igraph)

resUrls <- ""
decUrls <- ""
filename <- "forest_fire.gml"

g<-sample_forestfire(n,fw.prob=fwburn,bw.factor=bwburn,directed=dir)

write.graph(g, filename, "gml")

resultData <- list(dataUrl=filename, decoratedUrl=filename)