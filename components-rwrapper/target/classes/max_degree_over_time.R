require(igraph)

dcot.getCentralityOverTime <- function(graphs, labels) {
	values <- c()
	nlabels <- c()
	for (i in 1:length(labels)) {
		values[i] <- max(degree(graphs[[labels[i]]]))	
	}
	barplot(values, main="Degree Centrality over time", xlab="timeslides", ylab="max degrees", names.arg=labels);
	values
}

resultData <- list(dataUrl='degOverTime.data',plotUrl='degOverTime.png')

png('degOverTime.png')
data <- dcot.getCentralityOverTime(graphs, labels)
write.table(data, file="degOverTime.data")
dev.off()

