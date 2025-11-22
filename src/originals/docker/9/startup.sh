#!/bin/bash

cd /java-servers/solr/ && bin/solr start -force --user-managed
cd /java-servers/panl/ && bin/panl server -properties panl.properties
