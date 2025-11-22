FROM alpine:3.22.1

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
#   Download the Solr server and the Panl server from the local disk
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
ADD https://dlcdn.apache.org/solr/solr/9.9.0/solr-9.9.0-slim.tgz /archives/
ADD build/distributions/${TAG}.tgz /java-servers/


# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Extract the Solr server (The Panl server is automatically extracted by
#   the `ADD` command above)
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
RUN tar -xzvf /archives/solr-9.9.0-slim.tgz -C /java-servers/

# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   1. Set up the linking for the Solr and Panl installation
#   2. Copy the Solr configuration files for the standalone Solr server
#   3. Copy the override shell script so that Solr accepts all connections
#   4. Copy over the Panl configuration for thge standalone connection
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
WORKDIR /java-servers/

RUN ln -s solr-9.9.0-slim solr
RUN ln -s ${TAG} panl

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
#   Expose the Solr and Panl server ports
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
EXPOSE 8181/tcp
EXPOSE 8983/tcp


# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
#   Run the startup script on container run.
# ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
CMD [ "/startup.sh" ]