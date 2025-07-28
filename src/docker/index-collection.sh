#!/bin/bash

cd /java-servers/solr/ && bin/solr start -force --user-managed

cd /java-servers/solr/ && bin/solr create -c mechanical-pencils -d mechanical-pencils
cd /java-servers/solr/ && bin/solr post -c mechanical-pencils /java-servers/panl/sample/data/mechanical-pencils.json

cd /java-servers/solr/ && bin/solr create -c simple-date -d simple-date
cd /java-servers/solr/ && bin/solr post -c simple-date /java-servers/panl/sample/data/simple-date.json

cd /java-servers/solr/ && bin/solr create -c book-store -d book-store
cd /java-servers/solr/ && bin/solr post -c book-store /java-servers/panl/sample/data/book-store.json

cd /java-servers/solr/ && bin/solr stop

