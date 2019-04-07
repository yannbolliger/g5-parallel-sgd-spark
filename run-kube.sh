#!/usr/bin/env bash
set -eo pipefail

# SETTINGS

# name of the spark pod
pod_name="spark-parallelsgdapp"

# Our namespace
namespace="cs449g5"

# Build app
#sbt package

# Generate dockerfile image
#cd spark-*
#docker build -t $namespace-spark:latest -f ../Dockerfile .

# Submit to Kubernetes
# if exist, delete old 'pods'
kubectl delete pods $pod_name

spark-submit \
  --master k8s://https://10.90.36.16:6443 \
  --deploy-mode cluster \
  --name pyspark-wc \
  --class "com.github.yannbolliger.g5.parallel.sgd.spark.ParallelSGDApp" \
  --conf spark.executor.instances=5 \
  --conf spark.kubernetes.namespace=$namespace \
  --conf spark.kubernetes.driver.pod.name=$pod_name \
  --conf spark.kubernetes.driver.volumes.persistentVolumeClaim.myvolume.options.claimName=$namespace-scratch\
  --conf spark.kubernetes.executor.volumes.persistentVolumeClaim.myvolume.options.claimName=$namespace-scratch\
  --conf spark.kubernetes.driver.volumes.persistentVolumeClaim.myvolume.mount.path=/data \
  --conf spark.kubernetes.executor.volumes.persistentVolumeClaim.myvolume.mount.path=/data \
  --conf spark.kubernetes.container.image=jonathanbesomi/spark:latest \
  --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
  local:///opt/spark/g5-parallel-sgd-spark_2.11-0.1.jar
