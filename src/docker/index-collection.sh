#!/bin/bash

cd /java-servers/solr/ && bin/solr start --force --user-managed
cd /java-servers/solr/ && bin/solr create -c mechanical-pencils -d mechanical-pencils
cd /java-servers/solr/ && bin/solr post -c mechanical-pencils /java-servers/panl/sample/data/mechanical-pencils.json
cd /java-servers/solr/ && bin/solr stop

