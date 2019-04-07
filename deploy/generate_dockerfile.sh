#!/bin/sh


(cat Dockerfile_head ; echo "COPY /$1 /opt/code.jar" ; cat Dockerfile_tail) > spark/Dockerfile
