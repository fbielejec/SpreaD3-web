#!/bin/bash

NAME=nodrama/spread-server
TAG=$(git log -1 --pretty=%h)
IMG=$NAME:$TAG

# build jar
mvn clean package -DskipTests

echo "=============================="
echo "Buidling: " $IMG
echo "=============================="

# build and tag as latest
docker build -t $IMG -f docker-builds/spread-server/Dockerfile .
docker tag $IMG $NAME:latest

# dockerhub login
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

echo "Pushing: " $NAME

# push to dockerhub
docker push $NAME

echo "=============================="
echo "DONE"
echo "=============================="

exit $?
