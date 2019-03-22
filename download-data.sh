#!/usr/bin/env bash
set -eo pipefail

sbt package
spark-submit \
    --class "Paral" \
    --master local[4] \
    target/scala-2.11/g5-parallel-sgd-spark*.jar

