#!/bin/bash

cd /java-servers/solr/ && bin/solr start -force
cd /java-servers/panl/ && bin/panl server -properties panl.properties
