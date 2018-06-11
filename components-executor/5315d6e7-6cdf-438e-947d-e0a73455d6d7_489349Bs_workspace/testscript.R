require(igraph)

g <- barabasi.game(100, 1.4)

V(g)$dc <- degree(g)
V(g)$bc <- betweenness(g)


#----
# Centrality vector to empirical distribution
degreeDist <- table(V(g)$dc)
betDist <- table(V(g)$bc)

# normalize degrees


# Plot two distributions in one chart with two lines
plot(degreeDist, type="l", lty=1)
lines(betDist, lty=2)

# Spearman correlation between centrality vectors
cor(V(g)$dc, V(g)$bc, method = "spearman")

# Kolmogorov Smirnov distribution test
kstestResult <- ks.test(V(g)$dc, V(g)$bc)
print(kstestResult)