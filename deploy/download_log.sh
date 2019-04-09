#!/usr/bin/env bash

kubectl create -f volume_pod.yaml

sleep 2

cd ..
mkdir -p logs

kubectl cp volume-pod:data/logs/ logs

kubectl delete -f volume_pod.yaml
