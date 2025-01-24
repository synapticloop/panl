# Instructions for Development Process

## 1. Name the new version 

Find a suitable name for the new version and branch - an example site is the [Release name generator](https://codesandbox.io/p/sandbox/release-name-generator-5ow5w?file=%2Fsrc%2Findex.js) 
which somehow (_sort-of_) relates to the release.  The next release may very well be called `hidden-summer` as there may 
be some boost terms added to queries.

## 2. Create a branch 

Create a branch from the `main` branch with the above name i.e. `hidden-summer`.

## 3. Work, work, work

Do the work, run the tests, but don't forget the following:

### a. Update the version

See the file `src/main/resources/gradle.properties` and update the following properties:

 - `panl.version` - increment the major, minor, micro version - generally this will be a `minor` increment
 - `panl.solr.version` - this can be left at `9` as this is the latest Solr release, this will need to be updated if Solr releases a version `10`

### b. Update the 

## Merge back the changes

### 1. Merge back to the `main` branch

Merge all changes to the main version, there should be no conflicts....  but resolve them.

1. checkout `main`
2. merge

## Update the documentation

