#!/usr/bin/env bash
set -eo pipefail

# build app
sbt package

# submit to spark
spark-submit \
    --class "com.github.yannbolliger.g5.parallel.sgd.spark.ParallelSGDApp" \
    --master local[4] \
    target/scala-2.11/g5-parallel-sgd-spark*.jar

