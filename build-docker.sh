if [ -z "$1" ]
  then
    echo "Pass in the docker tag for this build, exiting..."
    exit
fi

docker build --build-arg TAG=$1 -f src/docker/Dockerfile -t $1 .