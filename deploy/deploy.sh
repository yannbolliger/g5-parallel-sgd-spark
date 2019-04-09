#!/usr/bin/env bash
#set -e

N_WORKERS=$1
SUBSET_SIZE=$2
N_EPOCH=$3

# name of the spark pod
POD_NAME="spark-parallelsgdapp"

# Our namespace
NAMESPACE="cs449g5"

# Docker hub repo
REPO=jonathanbesomi

# Download and unzip Apache Spark
if ! ls spark* 1> /dev/null 2>&1; then
      echo "download Apache Spark ..."
      curl -O "http://mirror.easyname.ch/apache/spark/spark-2.4.1/spark-2.4.1-bin-hadoop2.7.tgz"
      tar -xvzf *.tgz
      mv spark-2.4.1-bin-hadoop2.7 spark
      rm *.tgz
fi

echo "compile jar file ..."
cd ..
sbt package

# get filename of compiled jar file
target_folder=target/scala-*/jar-*
filename=$(basename $target_folder)
tag="${filename%.*}"

# Move jar to spark folder and remove from 'target' folder
cp target/scala-*/$filename deploy/spark/$filename
sbt clean

cd deploy
# Compute Dockerfile with filename (to speed-up deployment process)
./generate_dockerfile.sh $filename

cd spark
# build and push dockerfile
echo "build docker image ..."
./bin/docker-image-tool.sh -f ./Dockerfile -t $tag -r $REPO build
echo "push docker image ..."
./bin/docker-image-tool.sh -r $REPO -t $tag push

# remove Dockerfile and jar file
rm *.jar
rm Dockerfile


# Submit to Kubernetes
# if exist, delete old 'pods'
kubectl delete pods $POD_NAME

./bin/spark-submit \
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
