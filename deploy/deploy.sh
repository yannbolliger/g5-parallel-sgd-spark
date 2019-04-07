#!/bin/bash
set -e

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
rm target/scala-*/jar-*

cd deploy
# Compute Dockerfile with filename (to speed-up deployment process)
./generate_dockerfile.sh $filename

cd spark
# build and push dockerfile
echo "build docker image ..."
./bin/docker-image-tool.sh -f ./Dockerfile -t $tag -r jonathanbesomi build
echo "push docker image ..."
./bin/docker-image-tool.sh -r jonathanbesomi -t $tag push

# remove Dockerfile and jar file
rm *.jar
rm Dockerfile

# name of the spark pod
pod_name="spark-parallelsgdapp"
# Our namespace
namespace="cs449g5"

# Submit to Kubernetes
# if exist, delete old 'pods'
kubectl delete pods $pod_name

./bin/spark-submit \
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
  --conf spark.kubernetes.container.image=jonathanbesomi/spark:$tag \
  --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
  local:///opt/code.jar
