require(igraph)

extractDegreeTable <- function(graphs, labels) {
	
	lst <- list()
	maxDegree <- 0
	
	sapply(labels <- function(l) {
	
		degrees <- degree(graphs[[l]])
		lst[[l]] <<- degrees
		
		if (max(degrees) > maxDegree) {
			
			maxDegree <<- max(degrees)
		}
	})
	
	values <- matrix(nrow=(maxDegree + 1),ncol=length(labels))
	
	sapply(1:length(lst), function(i) {
				
		tab <- table(lst[[i]])
		values[(as.numeric(names(tab)) + 1), i] <<- as.vector(tab)
	})

	values[which(is.na(values))] <- 0
	
	rownames(values) <- as.character(0:maxDegree)
	colnames(values) <- labels
	values
}

resultData <- list(dataUrl='degTable.data',decoratedUrl='null')
data <- extractDegreeTable(graphs, labels)
write.table(data, file="degTable.data")
