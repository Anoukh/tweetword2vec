library(dplyr)
library(stringr)
library(igraph)

source("http://michael.hahsler.net/SMU/ScientificCompR/code/map.R")


configureLabels <- function(g, c=FALSE){
  dg <- degree(g, mode="out")
  logdg <- log(dg+1)/3; 
  V(g)$label.cex <- ifelse(logdg >0.9, 0.5, 0.01)
  V(g)$size=map(dg,c(2,20))
  if(c){
    V(g)$color <- V(g)$c
    V(g)$frame.color <- V(g)$c    
  }
  return(g)
}


createRetweetGraph <- function(csvFile, filterLimit=0){
  rawTweets<-read.csv(csvFile)
  
  rtToRaw  <- str_match(rawTweets$text, "RT @([A-z0-9]*):.*");
  rawTweets$from <- rtToRaw[,2];
  retweets <-filter(rawTweets, !is.na(from))
  retweets <- retweets %>% group_by(from_user, from) %>% summarise(count = length(text)) %>% arrange(count)
  retweets <- filter(retweets, count > filterLimit)
  
  return(retweets)
}

createRetweetGraphFromRawTweets <- function(rawTweets, filterLimit=0){
  rtToRaw  <- str_match(rawTweets$text, "RT @([A-z0-9]*):.*");
  rawTweets$from <- rtToRaw[,2];
  retweets <-filter(rawTweets, !is.na(from))
  retweets <- retweets %>% group_by(from_user, from) %>% summarise(count = length(text)) %>% arrange(count)
  retweets <- filter(retweets, count > filterLimit)
  
  return(retweets)
}

createTweetsDataFromRawTweets <- function(csvFile, filterLimit=0){
  rawTweets<-read.csv(csvFile)
  rtToRaw  <- str_match(rawTweets$text, "RT @([A-z0-9]*):.*");
  rawTweets$from <- rtToRaw[,2];
  retweets <-filter(rawTweets, !is.na(from))
  retweets <- retweets %>% group_by(from_user, from) %>% summarise(count = length(text), followers=max(user_followers_count)) %>% arrange(count)
  retweets <- filter(retweets, count > filterLimit)
  
  return(retweets)
}



drawLogLogPlot <- function(valueList, label="Count"){
  yvals <- sort(valueList, decreasing=TRUE); 
  xvals <- seq(1,length(yvals))
  plot(xvals, yvals, log="xy", ylab=label) 
}


createWeightedGraph <- function(retweets, filterLimit=0){
  retweetsAg <- retweets %>% group_by(from, from_user) %>% summarise(tot = sum(count)) 
  verticesName <- union(retweetsAg$from, retweetsAg$from_user)
  
  #thrid feild if given will automatically taken as weights
  edges <- data.frame(from=retweetsAg$from, to=retweetsAg$from_user, weight=retweetsAg$tot)
  g <- graph.data.frame(edges, directed=TRUE)
  return (g);
}


removeVL <- function(g, verticesToRemoveAsNamedList){
  g <- delete.vertices(g, names(verticesToRemoveAsNamedList))
  return (g);
}


#ctL2df <- function(l1, l2){
#
#}