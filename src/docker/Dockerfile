FROM alpine:3.22.1

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   The SOLR_VERSION argument is the Apache Solr server version that will be
#   integrated with. It has a default value which is set the the latest version
#   of Solr that was available when the code was last updated.  This value can
#   be over-ridden by passing in a command line orgument of the form:
#
#     --build-arg SOLR_VERSION=<version_number_here>
#
#   For example, to build the Panl server to connect to the Solr version 9.8.1
#   The command line argument would be
#
#     --build-arg SOLR_VERSION=9.8.1
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
ARG SOLR_VERSION=9.9.0

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   The TAG argument is the docker tag that will be used to build the docker
#   image and will be of the form:
#     synapticloop:${TAG}
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
ARG TAG

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Update and upgrade alpine linux, then Install necessary packages:
#          lsof - for the Solr server to be able to determine the port number
#                 that it was started on (so it can listen for startup)
#          bash - the preferred shell
#        procps - an updated ps command for the Solr server to determine the
#                 PID so that it can gracefully shtdown the server
#     openjdk21 - The Open JDK to run the Solr and Panl servers
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
RUN apk update && apk upgrade
RUN apk add lsof bash procps openjdk21


# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Make the directories for the downloaded files and Java server runtimes
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
RUN mkdir /java-servers/
RUN mkdir /archives/


# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Download the Solr server from the apache archives and extract it
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
ADD https://archive.apache.org/dist/solr/solr/$SOLR_VERSION/solr-$SOLR_VERSION-slim.tgz /archives/

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Extract the Solr server
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
RUN tar -xzvf /archives/solr-$SOLR_VERSION-slim.tgz -C /java-servers/

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   1. Set up the linking for the Solr installation
#   2. Copy the Solr configuration files for the standalone Solr server
#   3. Copy the override shell script so that Solr accepts all connections
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~

WORKDIR /java-servers/

RUN ln -s solr-$SOLR_VERSION-slim solr

RUN mkdir /java-servers/solr/mechanical-pencils/
RUN mkdir /java-servers/solr/server/solr/mechanical-pencils/

RUN mkdir /java-servers/solr/simple-date/
RUN mkdir /java-servers/solr/server/solr/simple-date/

RUN mkdir /java-servers/solr/book-store/
RUN mkdir /java-servers/solr/server/solr/book-store/

RUN cp -R /java-servers/panl/sample/solr/* /java-servers/solr/server/solr/

WORKDIR /

COPY src/docker/solr.in.sh /java-servers/solr/bin/
RUN chmod a+x /java-servers/solr/bin/solr.in.sh


# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Copy the Panl server from the local disk  (The Panl server is automatically
#   extracted by the `ADD` command below)
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
ADD build/distributions/${TAG}.tgz /java-servers/

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   1. Set up the linking for the Panl installation
#   2. Copy over the Panl configuration for thge standalone connection
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
WORKDIR /java-servers/

RUN ln -s ${TAG} panl

COPY src/docker/panl.properties /java-servers/panl/

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Copy over the indexer shell script and execute it - this will:
#     1. Start the Solr server in standalone mode
#     2. Create the mechanical-pencils collection
#     3. Populate the Solr index with the mechanical pencils dataset
#     4. Stop the Solr server
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
COPY src/docker/index-collection.sh /
RUN chmod a+x /index-collection.sh
RUN /index-collection.sh

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Copy the startup script over, make it executable.  This startup script will
#   Start the Panl server
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
COPY src/docker/startup.sh /
RUN chmod a+x /startup.sh

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Expose the Panl and Solr server ports
#     8181 for the Panl server
#     8983 for the Solr admin server
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
EXPOSE 8181/tcp
EXPOSE 8983/tcp


# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Run the startup script on container run.
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
CMD [ "/startup.sh" ]