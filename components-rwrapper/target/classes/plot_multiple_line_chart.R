plotLineChart <- function(table) {
	
	#one color per line
	colors <- rainbow(length(table[1,]))
	labels <- colnames(table)
	types <- 1:length(labels)
	
	yLimit <- 0
	for (i in 1:ncol(table)) {
		
		if (max(table[,i]) > yLimit) {
			
			yLimit <- max(table[,i])
		}
	}
	cat(yLimit)
	plot(table[,1], type="o", col=colors[1], ylim=c(0, yLimit), lty=1)
		
	box()
	
	if (ncol(table) > 1) {
		for (i in 2:ncol(table)) {
			
			lines(table[,i], type="o", lty=types[i], col=colors[i])
		}
	}
	legend(1, yLimit, labels, cex=0.8, col=colors, lty=types);
}

resultData <- list(dataUrl='line.data',plotUrl='line.png')

write.table(data, file="line.data")
png('line.png')
plotLineChart(data)
dev.off()
