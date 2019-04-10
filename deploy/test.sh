#!/usr/bin/env bash

POD_NAME="pod"

cmd=$(kubectl get pods | grep $POD_NAME |grep "Completed")
if [ -z "$cmd" ];
then
      echo "$POD_NAME is not Completed, start or restart it ..."

else
      echo "$POD_NAME is Completed, terminated it ..."
      kubectl delete pods $POD_NAME
fi
