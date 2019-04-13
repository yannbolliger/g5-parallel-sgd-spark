#!/usr/bin/env bash
set -eo pipefail

N_WORKERS=4
SUBSET_SIZE=1000
WHERE="local"
N_EPOCH=1000
POD_NAME="pod"

# parse arguments
while getopts ":n:s:w:e:p:" opt; do
  case $opt in
    # number of workers
    n) N_WORKERS="$OPTARG";;

    # subset size for stochastic sampling
    s) SUBSET_SIZE="$OPTARG";;

    # run locally or in cluster
    w) WHERE="$OPTARG";;

    # number of epoch
    e) N_EPOCH="$OPTARG";;

    # Pod name
    p) POD_NAME="$OPTARG";;

    \?) echo "Invalid option provided -$OPTARG" >&2
    ;;
  esac
done

if [[ $WHERE = "cluster" ]];
then
      echo "deploy on cluster ..."
      cd deploy && ./run-deploy.sh $N_WORKERS $SUBSET_SIZE $N_EPOCH $POD_NAME
      exit 0
else
      echo "run locally ..."

      if ! ls ./resources/rcv1 1> /dev/null 2>&1;
      then
            echo "download dataset ..."
            ./download_log.sh
      else
            echo "dataset already present, skip download."
      fi

      # build app
      sbt clean
      sbt package

      # submit to spark
      spark-submit \
          --class "com.github.yannbolliger.g5.parallel.sgd.spark.ParallelSGDApp" \
          --master local[*] \
          target/scala-2.11/*.jar \
          $SUBSET_SIZE $N_EPOCH
    exit 0
fi;
