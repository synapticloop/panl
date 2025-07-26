FROM alpine:3.22.1

RUN apk update && apk upgrade
RUN apk add lsof
RUN apk add bash
RUN apk add procps
RUN apk add openjdk21

# Get the Solr server
ADD https://www.apache.org/dyn/closer.lua/solr/solr/9.8.1/solr-9.8.1-slim.tgz?action=download /archives/solr-9.8.1-slim.tgz

# Download the Panl server
ADD https://github.com/synapticloop/panl/releases/download/2.0.0/solr-panl-9-2.0.0.tar /archives/solr-panl-9-2.0.0.tar

# Now extract
RUN mkdir /java-servers/

RUN tar -xzvf /archives/solr-9.8.1-slim.tgz -C /java-servers/
RUN tar -xvf /archives/solr-panl-9-2.0.0.tar -C /java-servers/

WORKDIR /java-servers/
RUN ln -s solr-9.8.1-slim solr
RUN ln -s solr-panl-9-2.0.0 panl

# Setup the solr instance for standalone mode
RUN mkdir /java-servers/solr/mechanical-pencils/
RUN mkdir /java-servers/solr/server/solr/mechanical-pencils/

# Copy over the configuration files
RUN cp -R /java-servers/panl/sample/solr/mechanical-pencils/* /java-servers/solr/server/solr/mechanical-pencils/

WORKDIR /

COPY src/docker/panl.properties /java-servers/panl/sample/panl/mechanical-pencils/panl.properties
COPY src/docker/index-collection.sh /
RUN chmod a+x /index-collection.sh
RUN /index-collection.sh

COPY src/docker/startup.sh /
RUN chmod a+x /startup.sh

EXPOSE 8181/tcp

CMD [ "/startup.sh" ]