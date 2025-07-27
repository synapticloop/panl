#!/bin/bash

cd /java-servers/solr/ && bin/solr start -force --user-managed
cd /java-servers/panl/ && bin/panl server -properties /java-servers/panl/sample/panl/mechanical-pencils/panl.properties
