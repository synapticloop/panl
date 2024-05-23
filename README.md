# Synapticloop PANL

## Getting started

**NOTE**: This is based on Apache solr 9.6.0, however other versions should 
hopefully match

For explicit instructions, see the [Solr tutorial for the techproducts](https://solr.apache.org/guide/solr/latest/getting-started/tutorial-techproducts.html), 
**NOTE** that the instructions here use a different `collectionRequestHandler` name than `techproducts` - 


# Apache Solr

## 1. Download and install Apache Solr

[Download Apache Solr for your platform](https://solr.apache.org/downloads.html)

## 2. Initialise the ~~'techproducts'~~ 'example' collectionRequestHandler

_Windows_

```
bin\solr start -e cloud
```

_*NIX_

```
bin/solr start -e cloud
```

For each of the prompts:

1. press `enter` for the prompt `To begin, how many Solr nodes would you like to run in your local cluster? (specify 1-4 nodes) [2]:` 
2. press `enter` for the prompt `Please enter the port for node1 [8983]:`
3. press `enter` for the prompt `Please enter the port for node2 [7574]:`
4. type `example` and press `enter` for the prompt `Please provide a name for your new collectionRequestHandler: [gettingstarted]`
5. press `enter` for the prompt `How many shards would you like to split example into? [2]`
6. press `enter` for the prompt `How many replicas per shard would you like to create? [2]`
7. type `sample_techproducts_configs` and press `enter` for the prompt `Please choose a configuration for the example collectionRequestHandler, available options are: _default or sample_techproducts_configs [_default]`

## 3. Index the ~~'techproducts'~~ 'example' collectionRequestHandler

_Windows_

**NOTE:** The instructions in the Apache Solr tutorial are **INCORRECT** use the following line instead:

```
bin\solr post -c techproducts example\exampledocs\*
```
**NOT:** ~~`java -jar -Dc=techproducts -Dauto example\exampledocs\post.jar example\exampledocs\*`~~

_*NIX_

```
bin/solr post -c techproducts example/exampledocs/*
```

# Synapticloop Panl

## 1. Download the latest release

[Synapticloop Panl releases](https://github.com/synapticloop/panl/releases)

## 2. Extract the files

unzip or untgz the files

## 3. Run the example

```
java -jar synapticloop-panl.jar server
```

## 4. View the results

[http://localhost:8181/panl-results-viewer/](http://localhost:8181/panl-results-viewer/)

# Next Steps

Learn more about Synapticloop Panl 