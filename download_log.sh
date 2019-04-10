#!/usr/bin/env bash

DROP=""

while getopts ":d" opt; do
  case $opt in
    d) DROP="1";;

    \?) echo "Invalid option provided -$OPTARG" >&2
    ;;
  esac
done

kubectl create -f deploy/volume_pod.yaml
sleep 5
mkdir -p logs # mkdir only if does not exist already

if [ ! -z $DROP ];
then
      echo "opening volume-pod ..."
      kubectl exec volume-pod -i -t /bin/sh
else
      echo "copy files ..."
      kubectl cp volume-pod:data/logs/ logs
fi;

kubectl delete pods volume-pod
