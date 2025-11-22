if [ -z "$1" ] || [ -z "$2" ]
  then
    echo "Pass in the docker tag and solr server version for this build, exiting..."
    exit
fi

docker build --progress=plain --build-arg TAG=%1 --build-arg SOLR_SERVER_VERSION=%2 -f src/docker/Dockerfile -t synapticloop/solr-panl:%1 .
