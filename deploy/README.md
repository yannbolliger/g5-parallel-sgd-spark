## Deployment

#### Advice for quicker deployment

`./spark/bin/docker-image-tool.sh` build and push commands,
generate the Docker image for 'Java/Scala', 'R' and 'python'.
Since we don't need the Docker images for 'R' and 'python', for faster
deployment you can:

- comment out lines 82-85 and lines 93-94 from `./spark/bin/docker-image-tool.sh`
