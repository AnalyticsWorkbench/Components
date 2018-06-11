require(igraph)
getTokens <- function(str) {
	
	s <- strsplit(str, " ")
	tokens <- unlist(s)
	tokens
}

readPajek <- function(src) {
	isDirected <- FALSE
	con <- file(src)
	open(con)
	numVerticesA = 0
	numVerticesB = 0
	#read vertices
	numVertices <- 0
	line <- readLines(con, n=1)
	#cat(line)
	tokens <- getTokens(line[1])
	rows <- strtoi(tokens[2])
	numVertices <- strtoi(tokens[2])
	
	if (!is.na(tokens[3])) {
		
		numVerticesA <- strtoi(tokens[3])
		numVerticesB <- numVertices - numVerticesA
	} 
	
	nLines <- readLines(con, n=numVertices)		
	names <- rep(NA,numVertices)
	for (i in 1:length(nLines)) {
		nameTokens <- getTokens(nLines[i])
		
		#ignore timestamps
		if (substr(nameTokens[length(nameTokens)],1,1) == "[") {
			
			name <- paste(nameTokens[2:(length(nameTokens) - 1)], collapse=" ")
		} else {
			
			name <- paste(nameTokens[2:length(nameTokens)], collapse=" ")
		}
		
		if (regexpr("\".*\"",name) > 0) {
			
			name <- substr(name, 2, nchar(name) - 1)
		}
		names[i] <- name
	}
	#read edges
	line <- readLines(con,n=1)
	if ((regexpr("Arcs",line) > 0) || (regexpr("arcs",line) > 0)) {
		
		isDirected <- TRUE
	}
	#cat(line)
	eLines <- readLines(con,n=-1)
	if (length(eLines != 0)) {
		edges <- rep(NA,length(eLines))
	} else {
		edges <- c()
	}
	
	i <- 1
	j <- 1
	while(i <= length(eLines)) {			
		
		tokens <- getTokens(eLines[i])
		edges[j] <- strtoi(tokens[1])
		edges[j+1] <- strtoi(tokens[2])
		i <- i + 1
		j <- j + 2
	}
	if (length(edges) != 0) {
		
		edges <- edges[!is.na(edges)]
	}
	close(con)
	cat(numVertices)
	net <- graph(edges, n=numVertices, directed=isDirected)
	V(net)$id <- names
	cat(numVerticesB > 0)
	
	if (numVerticesB > 0) {
		V(net)$type <- FALSE
		V(net)[which(V(net) > numVerticesA)]$type <- TRUE
	}
	
	net
}