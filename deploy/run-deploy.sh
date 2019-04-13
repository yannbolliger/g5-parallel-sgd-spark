#!/usr/bin/env bash
#set -e

# Set args
N_WORKERS=$1
SUBSET_SIZE=$2
N_EPOCH=$3
POD_NAME=$4

# Our namespace
NAMESPACE="cs449g5"

# Docker hub repo
REPO=jonathanbesomi

mkdir -p spark

echo "Compile jar file ..."
cd ..
sbt package

# get filename of compiled jar file
target_folder=target/scala-*/jar-*
filename=$(basename $target_folder)
tag="${filename%.*}"

# Move compiled jar to spark folder
cp target/scala-*/$filename deploy/spark/$filename
sbt clean

cd deploy
# Compute Dockerfile with filename (to speed-up deployment process)
./generate_dockerfile.sh $filename

cd spark
# build and push dockerfile
echo "Build docker image ..."
docker-image-tool.sh -f ./Dockerfile -t $tag -r $REPO build
echo "Push docker image ..."
docker-image-tool.sh -r $REPO -t $tag push

# remove Dockerfile and jar file from 'spark folder'
rm *.jar
rm Dockerfile

echo "Start ..."
spark-submit \
  --master k8s://https://10.90.36.16:6443 \
  --deploy-mode cluster \
  --class "com.github.yannbolliger.g5.parallel.sgd.spark.ParallelSGDApp" \
  --conf spark.executor.instances=$N_WORKERS \
  --conf spark.kubernetes.namespace=$NAMESPACE \
  --conf spark.kubernetes.driver.pod.name=$POD_NAME \
  --conf spark.executor.memory=4g \
  --conf spark.driver.memory=4g \
  --conf spark.kubernetes.driver.volumes.persistentVolumeClaim.myvolume.options.claimName=$NAMESPACE-scratch\
  --conf spark.kubernetes.executor.volumes.persistentVolumeClaim.myvolume.options.claimName=$NAMESPACE-scratch\
  --conf spark.kubernetes.driver.volumes.persistentVolumeClaim.myvolume.mount.path=/data \
  --conf spark.kubernetes.executor.volumes.persistentVolumeClaim.myvolume.mount.path=/data \
  --conf spark.kubernetes.container.image=$REPO/spark:$tag \
  --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
  local:///opt/code.jar $SUBSET_SIZE $N_EPOCH


# Submit to Kubernetes
cmd=$(kubectl get pods | grep $POD_NAME | grep "Completed")
if [ "$cmd" ];
then
      echo "Execution completed!"
else
      echo "FAILED N_WORKERS=$1, SUBSET_SIZE=$2, N_EPOCH=$3, POD_NAME=$4"> status.txt
fi


kubectl delete pods $POD_NAME
