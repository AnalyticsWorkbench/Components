aggregateTables <- function(tables, labels) {
  
  singleTables <- list()
  names <- c()
  
  # identify id and data columns
  if (!is.null(tables[[1]]$label)) {
    
    idColumn <- which(colnames(tables[[1]]) == "label")
  } else if (!is.null(tables[[1]]$id)) {
    
    idColumn <- which(colnames(tables[[1]]) == "id")
  } else {
    
    idColumn <- 0
  }
             
  dataColumns <- which(colnames(tables[[1]]) != "label")
  dataColumns <- intersect(dataColumns, which(colnames(tables[[1]]) != "id"))
  dataColumns <- intersect(dataColumns, which(colnames(tables[[1]]) != "timeappearance"))
  dataColumns <- intersect(dataColumns, which(colnames(tables[[1]]) != "type"))
  
  help <- c()
  sapply(dataColumns, function(c) {
    
    if (is.numeric(tables[[1]][,c])) {
      
      help <<- append(help,c)
    }
  })
  
  dataColumns <- intersect(dataColumns, help)
  
  # identify same entities by their labels or ids.
  if ((length(labels) > 1) && idColumn != 0) {
    
    i <- 1
    sapply(dataColumns, function(dc) {
      
      values <- matrix()
      
      sapply(labels, function(l) {
        
        names <<- union(names, tables[[l]][,idColumn])#rownames(tables[[l]]))
      })
      
      column <- 1
      isFirst <- TRUE
      sapply(labels, function(l) {
        
        if (isFirst) {
          
          isFirst <<- FALSE
          values <<- as.matrix(rep(0, length(names)))
        } else {
          
          values <<- cbind(values, rep(0, length(names)))
        }
        
        row <- 1
        
        sapply(names, function(name) {
          
          if (name %in% tables[[l]][,idColumn]) {#rownames(tables[[l]])) {
            
            values[row,column] <<- tables[[l]][which(tables[[l]][,idColumn] == name),dc]#rownames(tables[[l]]) == name),1]
          }
          
          row <<- row + 1
        })
        
        column <<- column + 1
      })
      
      colnames(values) <- paste(colnames(tables[[1]])[dc], "_", labels, sep="")
      
      #rownames(values) <- names
      cbind(names, values)
      colnames(values) <- append("id", paste(colnames(tables[[1]])[dc], "_", labels, sep=""))
      singleTables[[i]] <<- values
      
      i <<- i + 1
    })
  } else {
    
    singleTables[[1]] <- tables[[1]]
  }
  
  singleTables
}

res <- aggregateTables(tables, labels)

isFirst <- TRUE
filenames <- ""
sapply(1:length(res), function(i) {
  
  if (isFirst) {
    
    isFirst <<- FALSE
    filenames <<- paste("table_",i,".csv", sep="")
  } else {
    
    filenames <<- paste(filenames,",table_",i, ".csv", sep="")
  }
  write.csv(res[[i]], file=paste("table_",i,".csv", sep=""), row.names=FALSE)
})

resultData <- list(dataUrl=filenames)