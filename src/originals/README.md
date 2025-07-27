# Docker files

`./docker/<solr_version_number>`

These directories consist of the original Dockerfiles schemas and associated 
shell scripts for the numbered version as they can be over-written with a 
merge from the main branch (or side branch).

On `git merge` of the main branch into this one:

1. commit the merged files,
2. Copy the schema/version directories from this directory with the
   appropriate Solr version number to the `src/docker/` directory
3. Diff the changes (if any - they really shouldn't change) **_UNLESS_**
   they have been over-written.

# Solr Schemas

`./solr/<solr_version_number>`

These directories consist of the original Solr schemas and configuration 
files for the numbered version as they can be over-written with a merge from 
the main branch (or side branch). 

On `git merge` of the main branch into this one:

1. commit the merged files,
2. Copy the schema/version directories from this directory with the 
   appropriate Solr version number to the `src/dist/sample/solr/` directory
3. Diff the changes (if any - they really shouldn't change) **_UNLESS_** 
   they have been over-written.

The Solr configuration doesn't change much at all and these folders should 
be the accepted ones unless you have updated the Solr schema for the book.