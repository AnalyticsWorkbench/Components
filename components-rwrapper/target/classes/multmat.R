source("mult_rel_bm.R")

multiplicationMatrix <- function (Blockmatrix1, Blockmatrix2) {
  
  AKlassen_berechnen(Blockmatrix1, Blockmatrix2)$Klassen_Matrix
}

if (length(tables) < 2) {
  
  warning("Less than 2 image matrices.")
  
  if (length(tables) < 1) {
    
    stop("No input matrices.")
  } else {
    
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
    
    mat1 <- as.matrix(tables[[1]][,dataColumns])
    
    dataColumns <- which(colnames(tables[[2]]) != "label")
    dataColumns <- intersect(dataColumns, which(colnames(tables[[2]]) != "id"))
    dataColumns <- intersect(dataColumns, which(colnames(tables[[2]]) != "timeappearance"))
    dataColumns <- intersect(dataColumns, which(colnames(tables[[2]]) != "type"))
    
    help <- c()
    sapply(dataColumns, function(c) {
      
      if (is.numeric(tables[[2]][,c])) {
        
        help <<- append(help,c)
      }
    })
    mat2 <- as.matrix(tables[[2]][,dataColumns])
    
    print(dim(mat1))
    print(dim(mat2))
  }
  
} else {
  
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
  
  mat1 <- as.matrix(tables[[1]][,dataColumns])
  mat2 <- as.matrix(tables[[1]][,dataColumns])
}


filename <- "multiplication_matrix.csv"
write.csv(multiplicationMatrix(mat1, mat2), filename, row.names=FALSE)

resultData <- list(dataUrl=filename)