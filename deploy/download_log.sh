#!/usr/bin/env bash

kubectl delete -f volume_pod.yaml
kubectl create -f volume_pod.yaml

sleep 2

cd ..
mkdir -p logs

kubectl cp volume-pod:data/logs/ logs
