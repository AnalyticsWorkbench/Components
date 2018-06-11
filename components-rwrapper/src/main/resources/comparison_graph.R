require(igraph)

graphComparison <- function(graphs, labels, byLabel=FALSE) {
	
	if (length(labels) > 1) {
		
		graph1 <- simplify(graphs[[labels[1]]], remove.loops = FALSE)
		graph2 <- simplify(graphs[[labels[2]]], remove.loops = FALSE)
		
    if (byLabel) {
      
      names <- union(V(graph1)$label, V(graph2)$label)
      V(graph1)$name <- V(graph1)$label
      V(graph2)$name <- V(graph2)$label
      
    } else {
      
      names <- union(V(graph1)$id, V(graph2)$id)
    }
    
    isFirst <- TRUE
    
		outlist1 <- get.adjlist(graph1, mode="out")
		outlist2 <- get.adjlist(graph2, mode="out")
    
    mat <- matrix(0, nrow=length(names), ncol=length(names))
    
    rownames(mat) <- names
    colnames(mat) <- names
    
    sapply(names, function(n) {
      
      if (!is.null(outlist1[[n]])) {
          
        sapply(outlist1[[n]], function(nb) {
          
          mat[n,V(graph1)[nb]$label] <<- 1  
        })
      }
      if (!is.null(outlist2[[n]])) {
        
        sapply(outlist2[[n]], function(nb) {
          
          mat[n,V(graph2)[nb]$label] <<- mat[n,V(graph2)[nb]$label] + 2  
        })
      }
    })
    
    if (is.directed(graph1) || is.directed(graph2)) {
      
      resGraph <- graph.adjacency(mat, mode="directed", weighted="type", add.colnames="label")  
    } else {
      
      resGraph <- graph.adjacency(mat, mode="undirected", weighted="type", add.colnames="label")
    }
		
	} else {
		
		resGraph <- graphs[[labels[1]]]
		V(resGraph)$type <- 1
	}
	
	resGraph
}

resultGraph <- graphComparison(graphs, labels, !is.null(V(graphs[[1]])$label))

filelist <- ""
for (i in 1:length(labels)) {
	
	if (i == 1) {
		filelist <- paste(labels[i],".gml",sep="")		
	} else {
		filelist <- paste(filelist,labels[i],".gml",sep="")
	}
	write.graph(resultGraph, "comparisonGraph.gml", format="gml")
}
resultData <- list(dataUrl="comparisonGraph.gml", decoratedUrl="comparisonGraph.gml")