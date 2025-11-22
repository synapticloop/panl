#!/bin/bash

cd /java-servers/solr/ && bin/solr start -force

cd /java-servers/solr/ && bin/solr create -force -c mechanical-pencils -d mechanical-pencils
cd /java-servers/solr/ && java -Dc=mechanical-pencils -Dtype=application/json -jar example/exampledocs/post.jar /java-servers/panl/sample/data/mechanical-pencils.json

cd /java-servers/solr/ && bin/solr create -force -c simple-date -d simple-date
cd /java-servers/solr/ && java -Dc=simple-date -Dtype=application/json -jar example/exampledocs/post.jar /java-servers/panl/sample/data/simple-date.json

cd /java-servers/solr/ && bin/solr create -force -c book-store -d book-store
cd /java-servers/solr/ && java -Dc=book-store -Dtype=application/json -jar example/exampledocs/post.jar /java-servers/panl/sample/data/book-store.json

cd /java-servers/solr/ && bin/solr stop
