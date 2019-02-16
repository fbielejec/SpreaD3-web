NAME=nodrama/spread-server
TAG=$(git log -1 --pretty=%h)
IMG=$NAME:$TAG

# build jar
mvn clean package -DskipTests

# build and tag as latest
docker build -t $IMG -f docker-builds/Dockerfile .
docker tag $IMG $NAME:latest

# dockerhub login
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

# push to dockerhub
docker push $NAME
