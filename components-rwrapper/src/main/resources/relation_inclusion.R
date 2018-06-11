source("mult_rel_bm.R")

relationInclusion <- function(multMat) {
  
  roleChains <- Rollen_berechnen_Ketten_ausgeben(multMat)
  
  res <- sapply(roleChains, function(chain) {
    
    paste(chain, collapse="-")
  })
  
  res <- matrix(res, ncol=1)
  
  colnames(res) <- c("role_chains")
  
  res
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

mat <- as.matrix(tables[[1]][,dataColumns])

result <- relationInclusion(mat)

filename_chains="rel_incl.csv"
filename_matrix=paste(labels[1],".csv",sep="")

write.csv(result, filename_chains, row.names=FALSE)

resultData <- list(dataUrl=paste(filename_chains, ",", filename_matrix, sep=""))