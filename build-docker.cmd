@ECHO off

IF "%~1"=="" GOTO NO_ARG

docker build --build-arg TAG=%1 -f src/docker/Dockerfile -t synapticloop:%1 .

GOTO END

:NO_ARG
ECHO "Pass in the docker tag for this build, exiting..."

:END