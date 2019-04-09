#!/usr/bin/env bash

# parse arguments

while getopts ":n:s:w:e:" opt; do
  case $opt in
    # number of workers
    n) N_WORKERS="$OPTARG";;

    # subset size for stochastic sampling
    s) SUBSET_SIZE="$OPTARG";;

    # run locally or in cluster
    w) WHERE="$OPTARG";;

    # number of epoch
    e) N_EPOCH="$OPTARG";;

    \?) echo "Invalid option provided -$OPTARG" >&2
    ;;
  esac
done

if [[ $WHERE = "cluster" ]];
then
    cd deploy && ./deploy.sh $N_WORKERS $SUBSET_SIZE $N_EPOCH
    exit 0
else
    ./run-local.sh $SUBSET_SIZE
    exit 0
fi;
