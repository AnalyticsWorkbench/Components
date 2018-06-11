require(igraph)




getTokens <- function(str,separate) {

	
s <- strsplit(str, separate)
	
        tokens <- unlist(s)
	
        tokens

}



readPajek <- function(src,Tstamp=FALSE,slice=1) {
	
       isDirected <- FALSE

       List_of_graphs_Vertices <- list()
       List_of_graphs_Edges <- list()
       graph_no_old <- 0
       graph_no <- 0
       con <- file(src)
	
       open(con)
	
       numVerticesA = 0
	
       numVerticesB = 0
	
       #read vertices
	
       numVertices <- 0
	
       line <- readLines(con, n=1)
	
       #cat(line)
	
       tokens <- getTokens(line[1]," ")
	## for example it will return "*Vertices" "1325" 
       rows <- strtoi(tokens[2])
	
       numVertices <- strtoi(tokens[2])

       if (!is.na(tokens[3])) {

	   numVerticesA <- strtoi(tokens[3])

	   numVerticesB <- numVertices - numVerticesA

	} 

	nLines <- readLines(con, n=numVertices)  ##read all lines in file 
	names <- rep(NA,numVertices)
 ## create like a matrix for storing vertices	
       for (i in 1:length(nLines)) {

        		
        nameTokens <- getTokens(nLines[i]," ")

        
        nameTokens  <- nameTokens [which(nameTokens !="")] 
        		

        #ignore timestamps
	
	if (substr(nameTokens[length(nameTokens)],1,1) == "[") {

            name <- paste(nameTokens[2:(length(nameTokens) - 1)], collapse=" ")


          if( (Tstamp) )
            {
             time_string=nameTokens[length(nameTokens)]

             
             time_s=substr(time_string,2,nchar(time_string)-1)
             time_s=getTokens(time_s,",")
             for (n in 1:length(time_s)) 
             {
                if(mod(strtoi(time_s[n]),slice) != 0)
                   graph_no <- floor(strtoi(time_s[n])/slice) + 1
                 else
                   graph_no <- floor(strtoi(time_s[n])/slice) 
                    
                   if(graph_no != graph_no_old )
                {
                    if(length(List_of_graphs_Vertices)< graph_no)
                       length(List_of_graphs_Vertices) <-  graph_no       
                    List_of_graphs_Vertices[[graph_no]] <- c(List_of_graphs_Vertices[[graph_no]],strtoi(nameTokens[1]))
                    graph_no_old <- graph_no
                    
                }

                
             }
             graph_no <- 0
             graph_no_old <- 0
           }            
	} 
        else {

            
	    name <- paste(nameTokens[2:length(nameTokens)], collapse=" ")

		}
		
	
	if (regexpr("\".*\"",name) > 0) {

	    name <- substr(name, 2, nchar(name) - 1)
	
	}

        		
        names[i] <- name

        	
}




#read edges

graph_no_old <- 0
graph_no <- 0
List_of_graphs_Edges <- list()	
line <- readLines(con,n=1)
	
if ((regexpr("Arcs",line) > 0) || (regexpr("arcs",line) > 0)) {
	
      isDirected <- TRUE
	}

      #cat(line)

      eLines <- readLines(con,n=-1)
	
      if (length(eLines != 0)) {

	  edges <- rep(NA,length(eLines))

	} 
        else
        {

	   edges <- c()

	}


	i <- 1

	j <- 1

	while(i <= length(eLines)) {
             
	     tokens <- getTokens(eLines[i]," ")

             tokens  <- tokens [which(tokens !="")] 
             
           if((!is.na(tokens[1])) && (!is.na(tokens[2])) )
           {  
	     edges[j] <- strtoi(tokens[1])

	     edges[j+1] <- strtoi(tokens[2])
            if( (Tstamp) && (!is.na(tokens[4]))  )
            {
             time_string=tokens[4]

             time_s=substr(time_string,2,nchar(time_string)-1)
             time_s=getTokens(time_s,",")
             for (n in 1:length(time_s)) 
             {
                if(mod(strtoi(time_s[n]),slice) != 0)
                   graph_no <- floor(strtoi(time_s[n])/slice) + 1
                 else
                   graph_no <- floor(strtoi(time_s[n])/slice) 
                    
                   if(graph_no != graph_no_old )
                {
                    if(length(List_of_graphs_Edges)< graph_no)
                       length(List_of_graphs_Edges) <-  graph_no       
                    List_of_graphs_Edges[[graph_no]] <- c(List_of_graphs_Edges[[graph_no]],strtoi(tokens[1]))
                    List_of_graphs_Edges[[graph_no]] <- c(List_of_graphs_Edges[[graph_no]],strtoi(tokens[2]))
                    graph_no_old <- graph_no
                }

                
             }
             graph_no <- 0
             graph_no_old <- 0
           }
           }
             i <- i + 1
	
             j <- j + 2

             
        }	
             
	
	if (length(edges) != 0) {

		
edges <- edges[!is.na(edges)]

	}
	
close(con)

if(!Tstamp)
{
   cat(numVertices)

   net <- graph(edges, n=numVertices, directed=FALSE)

   V(net)$id <- names

   cat(numVerticesB > 0)
	

   if (numVerticesB > 0) {
	
     V(net)$type <- FALSE
	
     V(net)[which(V(net) > numVerticesA)]$type <- TRUE
	
   }
   return(net)


}
else
{
   List_of_graphs <- list()
   length( List_of_graphs )= length(List_of_graphs_Edges)
   for(i in 1:length(List_of_graphs_Edges))
  {
    if(! is.null(List_of_graphs_Edges[[i]]) )
    {
     nets=graph(List_of_graphs_Edges[[i]] , directed=FALSE)
     V(nets)$id <- names
     nets <- delete.vertices(nets, V(nets) [degree(nets)==0])
     n1 <- length(List_of_graphs_Vertices[[i]])
     n2 <- length(V(nets))
     if (n1 > n2)
       for(j in 1:n1)
        {
        if(!(names[ List_of_graphs_Vertices[[i]][j]
] %in% V(nets)$id))
           {
             nets <- add.vertices(nets,1)
             V(nets)$id[length(V(nets))] <- names[List_of_graphs_Vertices[[i]][j]
]
           }
          
        }
     List_of_graphs[[i]] <- nets
    }
  }
  return(List_of_graphs)


}


}

mod<-function(x,m)
{
   t1<-floor(x/m)
   return(x-t1*m)
 }
