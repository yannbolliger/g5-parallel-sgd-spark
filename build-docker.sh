#!/bin/bash
set -e

# SETTINGS
namespace="cs449g5"

# EXECUTION

# Download and unzip Apache Spark
if ! ls spark* 1> /dev/null 2>&1; then
      echo "Download Apache Spark"
      curl -O "http://mirror.easyname.ch/apache/spark/spark-2.4.1/spark-2.4.1-bin-hadoop2.7.tgz"
      tar -xvzf *.tgz
      mv spark-2.4.1-bin-hadoop2.7 spark
      rm *.tgz
fi

# Build app
#echo "Compile package..."
#sbt package

# Generate dockerfile image
cp ./Dockerfile spark/
cp -r target/ spark/target/
cd spark
#docker build -t $namespace-spark:latest -f ./Dockerfile .

 ./bin/docker-image-tool.sh -f ./Dockerfile -t latest -r jonathanbesomi build
./bin/docker-image-tool.sh -r jonathanbesomi push


rm -r target/*
rm Dockerfile
