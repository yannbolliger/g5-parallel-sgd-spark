#!/usr/bin/env bash
set -eo pipefail

# build app
sbt clean
sbt package

# submit to spark
spark-submit \
    --class "com.github.yannbolliger.g5.parallel.sgd.spark.ParallelSGDApp" \
    --master local[*] \
    target/scala-2.11/*.jar $1

