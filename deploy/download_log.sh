#!/usr/bin/sh

kind: Pod
apiVersion: v1
metadata:
  name: demo-pod
spec:
  volumes:
    - name: demo-storage
      persistentVolumeClaim:
       claimName: cs449-scratch
  containers:
    - name: demo-container
      image: alpine
      volumeMounts:
        - mountPath: /data
          name: demo-storage
      stdin: true
      tty: true
      command: ["/bin/sh"]

kubectl create -f demo-pod
kubectl attach -i -t demo-pod
kubectl cp /data/*.json /logs.json
