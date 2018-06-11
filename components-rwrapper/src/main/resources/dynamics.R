require(igraph)

centralityDynamics <- function(graphs, labels, getCentrality) {
	
	labels <- sort(labels)
	require(lattice)
	names <- c()
	values <- NULL

	i <- 1
	#for each timeslice
	sapply(labels, function(l) {
	
		graph <- graphs[[l]]
		#graph may be empty
		if (length(V(graph)) > 0) {
					
			#extract actor names.
			newNames <- which(!(unique(V(graph)$id) %in% names))
			names <<- append(names, V(graph)[newNames]$id)
			values <<- rbind(values,matrix(nrow=length(newNames), ncol=length(labels)))
				
			#compute centrality
			centrality <- getCentrality(graph)
		
			for (j in 1:length(centrality)) {
						
				rowIndex = which(names == V(graph)[j]$id)
						
				values[rowIndex,i] <<- centrality[j]
			}
		}
		i <<- i + 1
	})
	
	values[which(is.na(values))] <- 0
	colnames(values) <- labels
	if (nrow(values) > 0) {
		
		rownames(values) <- names
		
		colors <- rev(heat.colors(50))
		plot(levelplot(values,col.regions=colors,xlab="actors",ylab="centrality",aspect="fill",scales=list(x=list(rot=90))))
	} else {
		
		plot(1,type="n",axes=FALSE,xlab="",ylab="",main="All graphs are empty")	
	}
	
	values
}
