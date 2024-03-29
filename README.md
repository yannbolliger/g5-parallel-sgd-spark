# Group 5 Parallel SGD on Spark

This project implements stochastic gradient descent for support vector machines
(SVM) on Scala Spark. It was done for the course Systems for Data Science @
EPFL, 2019.

## Prerequisites

You need a running `bash`, `sbt` and `kubectl`.
Download those with your favorite installer like `apt-get` on Ubuntu or `brew`
on macOS.

The script will itself download and install Spark 2.4 in the folder `deploy`.


## Download the data locally

To run the code locally, the data set needs to be downloaded. You can use this
script to download the data.

```
./download-data.sh
```

In case you want to run the code on a Kubernetes cluster,
it's not necessary to download the data, since the complete data set already
needs to be present in a PersistentVolume.



## Run

The `./run.sh` exposes 5 different parameters:

- `-w` _where_ to run the program: `local` or in the `cluster`. [default local]
- `-n` the number of executor instances that will be allocated [default 5]
- `-s` the subset size that is sampled by SGD [default 1000]
- `-e` the number of epochs the algorithm is maximally run [default 1000]
- `-p` the name of the executed pod on Kubernetes [default pod]

For example:

```
./run.sh -w cluster -n 5 -s 50 -e 1000 -p mypod
```
runs the code on the cluster, with 5 workers, subset size 50, 1000 epochs and
'mypod' as pod name.


## References

This project is based on the excellent work of the `hogwild-python`
implementation by [`liabifano`](https://github.com/liabifano/hogwild-python)
that we forked [here](https://github.com/kyleger/hogwild-python).
