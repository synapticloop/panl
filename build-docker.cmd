@ECHO off

IF "%~1"=="" GOTO NO_ARG
IF "%~2"=="" GOTO NO_ARG

docker build --progress=plain --build-arg TAG=%1 --build-arg SOLR_SERVER_VERSION=%2 -f src/docker/Dockerfile -t synapticloop/solr-panl:%1 .

GOTO END

:NO_ARG
ECHO "Pass in the docker tag and solr server version for this build, exiting..."

:END