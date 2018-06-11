require(igraph)
require(plyr)

combineNodeAttributes <- function(nodeData) {
  
  idOrLabel <- ifelse(byLabel, "label", "id")
    
  ddply(nodeData, idOrLabel, function(subdata) {
    
    as.data.frame(lapply(subdata, function(column) {
      
      if (is.numeric(column)) {
        
        sum(column, na.rm = TRUE)
      } else {
        
        paste(unique(column), collapse = ",")
      }
    }))
  })  
}

mergeGraphs <- function(g1, g2) {
  
  if (byLabel) {
    
    if (is.null(V(g1)$label) || is.null(V(g2)$label)) {
      
      stop("Not every graph has node labels")
    }
    
    V(g1)$name <- V(g1)$label
    V(g2)$name <- V(g2)$label
  } else {
    
    V(g1)$name <- V(g1)$id
    V(g2)$name <- V(g2)$id
  }
  
  # This is necessary since node and edge attributes might not match completely
  edgeDf1 <- as_data_frame(g1, what="edges")
  edgeDf1$gId <- 1
  edgeDf2 <- as_data_frame(g2, what="edges")
  edgeDf2$gId <- 2
  
  nodeDf1 <- as_data_frame(g1, what="vertices")
  nodeDf1$gId <- 1
  nodeDf2 <- as_data_frame(g2, what="vertices")
  nodeDf2$gId <- 2
  
  edges <- merge(edgeDf1, edgeDf2, all=TRUE)
  nodes <- combineNodeAttributes(
    merge(nodeDf1, nodeDf2, all=TRUE)
  )
  # node identifier must be the first column in node data frame.
  if (byLabel) {
    
    nodes <- nodes[,c("name", setdiff(names(nodes), "name"))]
  } else {
    
    nodes <- nodes[,c("id", setdiff(names(nodes), "id"))]
  }
  res <- graph_from_data_frame(edges, directed=(is.directed(g1) || is.directed(g2)), vertices = nodes)
  
  if (!doUnion) {
    
    res <- delete_edges(res, !is.multiple(res))
  } 
  
  res <- simplify(res, remove.loops = FALSE, edge.attr.comb = function(attribute) {  
    
    if (is.numeric(attribute)) {
      
      sum(attribute)
    } else {
      
      paste(unique(attribute), collapse = ",")
    }
  })
  
  V(res)$id <- 1:vcount(res)
  res <- delete_edge_attr(res, "gId")
  delete_vertex_attr(res, "gId")
}

# set occurrences attributes
graphs <- lapply(graphs, function(g) {
  
  E(g)$occurrences <- 1
  V(g)$occurrences <- 1
  g
})
res <- Reduce(mergeGraphs, graphs)

filename <- "merged_graph.gml"

write.graph(res, filename, "gml")
resultData <- list(dataUrl=filename, metadata="Occurences,oc,double,node,number_of_occurences,Occurences,oc,double,edge,none")