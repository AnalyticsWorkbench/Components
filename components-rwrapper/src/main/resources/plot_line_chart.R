plot_line_chart <- function(data) {
	values <- c(data) 
	plot(values[[1]],type="o")
}

resultData <- list(dataUrl='line.data',plotUrl='line.png')

write.table(data, file="line.data")
png('line.png')
plot_line_chart(data)
dev.off()
