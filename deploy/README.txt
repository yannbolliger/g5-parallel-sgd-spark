# DEPLOYMENT

Advice for quick deployment:

      ./bin/docker-image-tool.sh build and push commands, generate the docker image for 'Java/Scala', 'R' and 'python'.
      Since we don't need the docker images 'R' and 'python', for a fast deyploment is advised to:

      - add comment to line 82-85 and line 93-94
