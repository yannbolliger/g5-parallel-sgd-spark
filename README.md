# Group 5 Parallel SGD on Spark

This project implements stochastic gradient descent for support vector machines
(SVM) on Scala Spark. It was done for the course Systems for Data Science @
EPFL, 2019.

## Prerequisites

You need a running `bash`, `sbt` and `kubectl`.
Download those with your favorite installer like `apt-get` on Ubuntu or `brew`
on macOS.

The script will itself download and install Spark 2.4 in the folder `deploy`.

## Run locally

- Get the data by executing `download-data.sh` in the folder `deploy`.

```
./run.sh -w local
```

## Run
The `./run.sh` exposes three different parameters:

- `-n` the number of executor instances that will be allocated
- `-s` the subset size that is sampled by SGD (must set `-n` if you're running
on the cluster and want to set this)
- `-e` the number of epochs the algorithm is maximally run (must set `-s` if you
 want to set this)
- `-w` _where_ to run the program: `local` or `cluster`.

## References

This project is based on the excellent work of the `hogwild-python`
implementation by [`liabifano`](https://github.com/liabifano/hogwild-python)
that we forked [here](https://github.com/kyleger/hogwild-python).

### TODOs

- Run hogwild-python: Kyle
  - Parameters:
    - sync, async
    - number of workers: 1, 5, 10, 20
    - subset size (batch fraction): 10, 100, 1000, 10'000

- Run SparkSGD (same params, sync): Jonny

### Paper outline

- Intro

- Changes and critique to Hogwild (early stopping)

- Spark implementation
  - ...
  - deploy Kubernetes

- Results and Analysis

- Conclusion
