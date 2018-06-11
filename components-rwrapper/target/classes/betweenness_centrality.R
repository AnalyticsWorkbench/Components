require(igraph)

decoratedGraphs <- ""
results <- ""

extract_betweenness <- function(graphs, labels) {

        isFirst <- TRUE
        sapply(labels, function(l) {

            cent <- betweenness(graphs[[l]])
            V(graphs[[l]])$bc <- cent

            write.graph(graphs[[l]], paste(l,".gml", sep=""), "gml")
            if (!is.null(V(graphs[[l]])$label)) {

                write.csv(cent, file=paste(l,".csv", sep=""), row.names=V(graphs[[l]])$label)
            } else {
            
                write.csv(cent, file=paste(l,".csv", sep=""))
            }

            if (isFirst) {

                isFirst <<- FALSE
                decoratedGraphs <<- paste(l, ".gml", sep="")
                results <<- paste(l,".csv", sep="")
            } else {

                decoratedGraphs <<- paste(decoratedGraphs, ",", l, ".gml", sep="")
                results <<- paste(results, ",", l, ".csv", sep="")
            }
        })
}

extract_betweenness(graphs, labels)
resultData <- list(dataUrl=decoratedGraphs, decoratedUrl="null", metadata="Betweenness Centrality,bc,double,node,none")