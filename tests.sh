#!/usr/bin/env bash

N_EPOCH=1000

for N_WORKERS in 5 10 20
do
      for SUBSET_SIZE in 50 100 1000
      do
            POD_NAME="worker-$N_WORKERS-subset-$SUBSET_SIZE"
            echo "RUN $POD_NAME"
            ./run.sh -w cluster -n $N_WORKERS -s $SUBSET_SIZE -e 1000 -p $POD_NAME
            wait
      done
done

wait
echo "finish."
