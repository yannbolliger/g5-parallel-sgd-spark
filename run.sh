#!/usr/bin/env bash

# parse arguments

while getopts ":n:s:w:" opt; do
  case $opt in
    # number of workers
    n) N_WORKERS="$OPTARG";;

    # subset size for stochastic sampling
    s) SUBSET_SIZE="$OPTARG";;

    # run locally or in cluster
    w) WHERE="$OPTARG";;

    \?) echo "Invalid option provided -$OPTARG" >&2
    ;;
  esac
done

if [[ $WHERE = "cluster" ]];
then
    cd deploy && ./deploy.sh $NUMBER_WORKERS $SUBSET_SIZE
    exit 0
else
    ./run-local.sh $SUBSET_SIZE
    exit 0
fi;


