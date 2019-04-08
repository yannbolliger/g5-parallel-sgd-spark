# g5-parallel-sgd-spark

Group 5 project for Systems for Data Science course @ EPFL, 2019.


### Run locally

- Get the data from ...
- Unpack them into `resources/rcv1/`


### TODOs

- Explain Docker/Kubernetes: Jonny
- Params for Docker

- Implement early stopping: Yann

- Implement final statistics for log: Jonny

- Tune learning rate for different batch fractions: Kyle

- Clone the hogwild-python: Kyle
  - apply changes

- Run hogwild-python: Kyle
  - Parameters:
    - sync, async
    - number of workers: 1, 5, 10, 20
    - subset size (batch fraction): 0.001, 0.1, 1

- Run SparkSGD (same params, sync): Jonny

- Start writing paper: Yann
