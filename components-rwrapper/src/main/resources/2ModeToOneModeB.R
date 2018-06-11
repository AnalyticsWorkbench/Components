require(igraph)

projection <- function(graphs, labels) {
	
	convertedGraphs <- list()
	sapply(labels, function(l) {
				
				graph <- graphs[[l]]
				
				if (is.bipartite(graph)) {
					
					#Pajek .net ids are stored as id attribute whereas gml ids are stored as name attribute.
					#if (is.null(V(graph)$id)) {
						
					#	ids <- V(graph)$name	
					#} else {
						
					#	ids <- V(graph)$id
					#}
					
					#v1 <- bipartite.projection.size(graph)$vcount1
					#v2 <- bipartite.projection.size(graph)$vcount2
					#newIds <- ids[(v1+1):(v1+v2)]
					
					#adj <- get.adjacency(graph, sparse=TRUE)
					
					#adj2 <- adj[1:v1, (v1+1):(v1+v2)]
					
					#resAdj <- t(adj2) %*% adj2
					
					#res <- graph.adjacency(resAdj,mode="upper", weighted=TRUE, diag=FALSE)
					
					#V(res)$id <- newIds
					
					#convertedGraphs[[l]] <<- res

										g <- bipartite.projection(graph)[[2]]
										V(g)$type <- 0
                                        convertedGraphs[[l]] <<- g
					
				} else {
					convertedGraphs[[l]] <<- graph
				}
				
			})
	
	convertedGraphs
}


resultGraphs <- projection(graphs, labels)

filelist <- ""
isFirst <- TRUE
sapply(labels, function(l) {
			
			if (isFirst) {
				filelist <<- paste("onemode_", l,".gml",sep="")
				isFirst <<- FALSE
			} else {
				filelist <<- paste(filelist,",onemode_", l,".gml",sep="")
			}
			write.graph(resultGraphs[[l]], paste("onemode_", l,".gml",sep=""), format="gml")			
		})

resultData <- list(dataUrl=filelist,decoratedUrl='null')
