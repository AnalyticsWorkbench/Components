plot_histogram <- function(data) {
	values <- c(data) 
	hist(values[[1]])
}

resultData <- list(dataUrl='hist.data',plotUrl='hist.png')

write.table(data, file="hist.data")
png('hist.png')
plot_histogram(data)
dev.off()
