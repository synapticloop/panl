# Synapticloop PANL

<img src="src/docs/synapticloop-panl-headline.png" alt="The Synapticloop Panl headline" />


> **_Rapidly get up and running with a fully featured, SEO friendly, keyword
> searchable, faceted search engine with an in-built, search page to test it all
> out._**

> **_And, yes, there is full documentation available, over 600 pages, 
> covering all aspects of the configuration of the Panl server so that you 
> can get the most out of your Solr and Panl experience._**

# IMPORTANT:

Apache®, Solr® the names of Apache projects, and the multicolor feather logo are registered trademarks or trademarks of the Apache Software Foundation in the United States and/or other countries.  

The mention and references of any Apache projects, sub-projects, or resources in no way constitutes an endorsement for the Synapticloop Panl project.


# Development Information 

| Latest<br />Release                                                          | Latest Book Release                                                | Development Branch (version) | 
|------------------------------------------------------------------------------|--------------------------------------------------------------------|------------------------------|
| ![GitHub Release](https://img.shields.io/github/v/release/synapticloop/panl) | Synapticloop - Getting Started With Panl version 2.1.0 (release 1) | `bitter-shadow`              |


**Major branch release status:**

| BRANCH          | STATUS   | VERSION | TYPE                                                 | LATEST SOLR<br />VERSION TESTED |
|-----------------|----------|---------|------------------------------------------------------|---------------------------------|
| `bitter-shadow` | [![CircleCI](https://dl.circleci.com/status-badge/img/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/bitter-shadow.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/bitter-shadow)           | `2.2.0` | Development branch - integration with Solr version 9 | `9.9.0`                         |
| `MAIN`          | [![CircleCI](https://dl.circleci.com/status-badge/img/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/main.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/main)           | `2.1.0` | Trunk - integration with Solr version 9              | `9.9.0`                         |
| `SOLR PANL 9`   | [![CircleCI](https://dl.circleci.com/status-badge/img/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/solr-panl-9.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/solr-panl-9)           | `2.1.0` | Panl integration with Solr version 9                 | `9.9.0`                         |
| `SOLR PANL 8`   | [![CircleCI](https://dl.circleci.com/status-badge/img/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/solr-panl-8.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/solr-panl-8)           | `2.1.0` | Panl integration with Solr version 8                 | `8.11.4`                        |
| `SOLR PANL 7`   | [![CircleCI](https://dl.circleci.com/status-badge/img/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/solr-panl-7.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/circleci/3Y1eqqe4mcvtSZuzJnQ3tJ/DyFKUm7c7AoLa1wHmRjAnf/tree/solr-panl-7)           | `2.1.0` | Panl integration with Solr version 7                 | `7.7.3`                         |

> We _always_ recommend that you use the most recently available release which
> contains bug fixes and new functionality and is _generally_ backwards
> compatible.
> 
> We do not keep previous individual Panl branches available for Solr version 8 
> and version 7 - i.e. there is no branch available for SOLR PANL 8 - which 
> is not the 2.0.0 branch.

**Branches/Release:**

This is for reference

| BRANCH              | TYPE     | VERSION | GITHUB                                                                                      |
|---------------------|----------|---------|---------------------------------------------------------------------------------------------|
| `bitter-shadow`     | `BRANCH` | `2.2.0` | [GitHub > `bitter-shadow`](https://github.com/synapticloop/panl/tree/bitter-shadow)         |
|                     |          |         |                                                                                             |
| `MAIN`              | `BRANCH` | `2.1.0` | [GitHub > MAIN](https://github.com/synapticloop/panl)                                       |
|                     |          |         |                                                                                             |
| `hidden-summer`     | `BRANCH` | `2.1.0` | [GitHub > `hidden-summer`](https://github.com/synapticloop/panl/tree/hidden-summer)         |
| `billowing-feather` | `BRANCH` | `2.0.0` | [GitHub > `billowing-feather`](https://github.com/synapticloop/panl/tree/billowing-feather) |

<strong>BREAKING CHANGE TO LPSE URL path part for versions below</strong>

| BRANCH                                                 | TYPE     | VERSION | GITHUB                                                                                                        |
|--------------------------------------------------------|----------|---------|---------------------------------------------------------------------------------------------------------------|
| `needy-phantom`                                        | `BRANCH` | `1.2.0` | [GitHub > `needy-phanton`](https://github.com/synapticloop/panl/tree/needy-phanton)              |
| `grizzled-pebble`                                      | `TAG`    | `1.1.1` | [GitHub > Tag 1.1.1](https://github.com/synapticloop/panl/tree/1.1.1)                                         |
| `broad-firefly`                                        | `TAG`    | `1.1.0` | [GitHub > Tag 1.1.0](https://github.com/synapticloop/panl/tree/1.1.0)                                         |
| `bright-wildflower`                                    | `TAG`    | `1.0.0` | [GitHub > Tag 1.1.0](https://github.com/synapticloop/panl/tree/1.0.0)                                         |


The Synapticloop Panl project uses `major.minor.micro` versioning, the meaning
of which:

- `major` - the major version will increment when there is a **BREAKING CHANGE
  to the Panl LPSE URL**. Upon increment of the major version, both the minor
  and micro version number will be reset to 0 (zero).
- `minor` - the minor version will increment when there is additional
  functionality added to the release. Upon increment of the minor version, the
  micro number will be reset to 0 (zero).
- `micro` - the micro version will increment for bug fixes only.

The book version matches the version of the Synapticloop Project version. Any
changes to the book without any changes to the codebase will be updated on the
`main` branch and the `ghpages` based website will be updated.

Any out-of-band book updates will not be reflected in the panl code release 
updates.


# Why?

Because...

`/Caran d'Ache/true/Black/bDW/`

looks A LOT nicer than

`q=*:*&facet.mincount=1&rows=10&facet.field=lead_size_indicator&facet.field=grip_material&facet.field=colours&facet.field=nib_shape&facet.field=diameter&facet.field=cap_shape&facet.field=brand&facet.field=mechanism_type&facet.field=length&facet.field=hardness_indicator&facet.field=grip_type&facet.field=cap_material&facet.field=lead_grade_indicator&facet.field=tubing_material&facet.field=in_built_sharpener&facet.field=disassemble&facet.field=category&facet.field=body_shape&facet.field=clip_material&facet.field=mechanism_material&facet.field=lead_length&facet.field=body_material&facet.field=in_built_eraser&facet.field=grip_shape&facet.field=relative_weight&facet.field=name&facet.field=nib_material&facet.field=weight&facet.field=variants&facet=true&fq=brand:"Caran+d'Ache"&fq=disassemble:"true"&fq=colours:"Black"&q.op=AND`

# Why Synapticloop Panl?

Panl was designed to convert rather long and unfriendly (both in human-readable
and SEO terms) to shorter, nicer, and friendlier URL paths throughout the entire
search journey.

Working with a Solr schema, the Panl configuration files translate unwieldy URL
parameters into concise and precise URL paths.

- **Have SEO friendlier URL paths with much shorter URLs than traditional query
  parameters (ll the way through the search journey)** - This was the primary
  driver and the base functionality.

- **Abstract away the complexities of the Solr query string** - Being able to
  have a simple interface through the URL which could generate complex queries.
  Not having to fully understand how Solr works in the back-end abstracts away
  the complexity of a front-end integrator and reduces the need to have the
  back-end and front-end understand each-other.

- **Be quick to start up and easy to configure** - During development of a
  solution, being able to iterate over a solution, or change the way that Panl
  is configured is a must have. Additionally, being able to upgrade the Panl
  server and have the configuration files be automatically picked up and work
  without any changes is a plus.

- **Protect Solr from errant queries** - Hiding the Solr implementation details
  from the end user and parsing, decoding, and validating the URL before passing
  the query through to Solr. Additionally, Solr has a tendency to return an
  internal server error when the query string is not as it expected, and this
  should not disturb the return of the results.

- **Be able to present the same Solr collection in multiple different ways** - A
  single Solr collection should be able to serve up different fields and facets
  from the result documents without any back-end logic.

- **Have a configuration file drive the generation of the UI as much as 
  possible** - Rather than hard-coding facets and then determining how to  
  display them, being able to have a returned JSON response which can be 
  interrogated to determine how the facets should be displayed.

## Additional Panl Niceties

1. **MULTIPLE ways to 'SLICE and DICE'** - From one Solr collection, the Panl
   server can present the results and facets in multiple different ways,
   providing individual use cases for specific needs.

1. **PREFIXES and SUFFIXES** - Panl can also add prefixes and suffixes to the
   URI path to increase readability, for example, with configuration. For the
   example LPSE URI path of

   `/Caran d'Ache/true/Black/bDW/`

   could also have the brand Solr field prefixed with ‘`Manufactured By `’ and
   suffixed by ‘` Company`’ to produce the URI path

   `/Manufactured By The Caran d'Ache Company/true/Black/bDW/`

1. **BOOLEAN value translations**, for any Solr field that is defined as a
   `solr.BoolField`, then an additional translation can be performed. ‘True’ and
   ‘false’ values can be replaced with arbitrary text, which will be
   transparently converted between Panl and Solr. For the LPSE URI path of

   `/Caran d'Ache/true/Black/bDW/`

   the true value (which is defined as whether the mechanical pencil can be
   disassembled could be changed to ‘`Able to be disassembled`’ for true values,
   and ‘`Cannot be disassembled`’ for false values.

   The above URI path would then become

   `/Caran d'Ache/Able to be disassembled/Black/bDW/`

1. **BOOLEAN checkboxes** - Whilst this may seem obvious to have a checkbox for
   a true/false value, the checkboxes work in a subtly different way.  By
   selecting the checkbox, the only one of facet values will be selected
   when deselected, the BOOLEAN facet is in a don't care start - the facet value
   can be either of the values.

1. **CONDENSED multiple field values** - Rather than having a forward slash URL
   path separator for multiple values of the same facet (used in OR Facets and
   Multi-valued REGULAR facets), Panl can be configured to condense these values
   into a single path part, saving URL characters, and reducing URL length, and
   making the URL far more human-readable. For example, selecting pencils
   manufactured by `Faber-Castell` **OR** `Koh-i-Noor` could have the URI path
   of

   `/Manufactured by Koh-i-Noor/Manufactured by Faber-Castell/bb/`,

   with condensed multiple field values - this could be configured (with a 
   value separator configured to be `, or `) to become

   `/Manufactured by Koh-i-Noor, or Faber-Castell Co./b/`

   Saving 15 characters in the URL, the more multivalued fields values that 
   are selected, the more URL space is saved (In the example, with 3 values 
   selected, the saving becomes 30 characters).

1. **SEARCH ALL OR SPECIFIC SOLR FIELDS** - Any Solr field that is analysed can
   be selected to be searched on, for example, in the Book Store Walkthrough,
   the user can select to search within the title, the author, the description,
   or all of them. **Also configure the query time boost.**

1. **MORE LIKE THIS** - Return 'More Like This' results from the Solr server 
   with your specific query, with the ability to configure the Solr query 
   operands on the fly.

1. **FIELD VALUE validation** - By default, Solr will error (or give an 
   erroneous result) when an invalid value is passed through - for example, 
   if Solr is expecting a numeric value and it could not parse the passed in 
   value, it will throw an exception.  Panl protects against this by 
   attempting to parse the value as best it can, and silently dropping the 
   parameter if it cannot be sensibly parsed. This is done for numeric types 
   (integer, long, float, and double) and boolean values.

1. **HIERARCHICAL facets** - Only show facets if a parent facet is currently
   selected, allowing you to narrow down the facet results and lead users
   through the search journey.

1. **UNLESS facets** - Continue to show a facet unless another specified facet is
   selected.  This can be thought of as the inverse of a hierarchical facet  and 
   is useful when a facet no longer becomes relevant as the user goes through 
   the search journey.

1. **SORTED facets** - Each facet can be individually configured to order 
   the facet results by either the facet count in descending order (which is 
   the default), or the facet value (e.g. alphabetic/numeric based on the 
   value of the facet - in either ascending or descending).

1. **MORE facets** - Solr (and Panl) configures a limit for the maximum 
   number of facet values that are returned, this functionality enables you 
   to dynamically load additional facet values if they are available but 
   weren't returned with the results by default.

1. **RESULTS SORTING options** - Sort by any of the Solr fields, either
   ascending, or descending and with multiple sub-sorting available - e.g.
   sorting by a brand name, than the model number.  Additionally, Panl generates
   URLs for the inverse of the sorting without impacting any sub-sorting.

1. **INTEGRATED TYPEAHEAD/LOOKAHEAD** - Retrieve results suggestions as you type
   in the query search box.

1. **PAGINATION** - All the data to easily generate pagination URL paths giving
   you options and control over your own implementation.  The returned 
   information includes:
   1. the number of pages of results,
   1. the number of results per page,
   1. the total number of results,
   1. the current page number, and
   1. whether the returned results are an exact number.

1. **STATIC SITE GENERATION** - With the exception of a query parameter, all
   available links for every conceivable URI path can be statically generated
   ahead of time, all with canonical URLs. Additionally, for search results
   which do not change frequently, the Panl JSON response Object can be cached
   for faster lookups.

1. **STATELESS** - No state is stored in the Panl server, all state is from the
   URL path part that is passed through. No sessions, no memory, nothing to
   backup or replicate across servers, easy to update and quick to start and 
   restart.

1. **CACHE-ABLE** - Unless the underlying Solr search document index changes,
   each Solr request is able to be cached.

1. **100% TEXT CONFIGURATION** - All configuration for Panl is based on text 
   files (Java `.properties`) files so they can be stored in a source code 
   management system.  Additionally, upgrades to the Panl server are easy - 
   just drop in the new Panl release package, use your existing 
   configuration, and it will just work.  And with quick restart times, the 
   configuration changes will be seen in an instant.



# Instructions

## Download the Panl Server Release

1. [https://github.com/synapticloop/panl/releases](https://github.com/synapticloop/panl/releases)
2. Read the [5-Step Quick Start section](https://github.com/synapticloop/panl/tree/main?tab=readme-ov-file#quick-start---the-5-steps)
3. Done.

## Upgrading the Panl Server Release

> **IMPORTANT !!!** 
> 
> Version 2.\*.\* is a breaking change with version 1.\*.\*,
> 
> Both the LPSE URL and the JSON response have changes.


Panl is designed to be a drop in replacement for your current version. Although
backwards compatibility is always the highest priority, do keep an eye out 
in the release notes for any breaking features.

Your existing configuration files _should_ just work with the downloaded release
package.

### Some important notes on upgrading

With the release of Panl version 2.1.0 the addition of the 'More Like This' 
functionality will require additional configuration in the `solrconfig.xml` 
file in order for this to work.  This configuration is _not_ included by 
default in the Apache Solr distributions.  Unless you are starting from a 
fresh install, you _will need to reconfigure your Solr server with a new 
handler_.

## Read the Documentation

- Online book (
  HTML): [https://synapticloop.github.io/panl/](https://synapticloop.github.io/panl/)
- Offline book (
  PDF): [Getting Started With Synapticloop Panl.pdf](https://github.com/synapticloop/panl/blob/main/src/dist/book/Getting%20Started%20With%20Synapticloop%20Panl.pdf)
  _(over 600 pages of documentation, written with you, the integrator, in 
  mind)_

Both of the book links above refer to Solr Panl integration 9 with instructions
for setting up and running earlier versions of Solr.


# Getting up to Speed... Fast!

The Solr Panl release package was designed to get you up and running as quickly
as possible.

With the in-built tool, point it at your existing Solr `managed-schema.xml`
file, run the Panl server and view the results. From there you can tweak the
configuration, generate new configurations and see your results in an instant.

## The Panl Results Viewer Web App

<img src="src/docs/panl-features.png" alt="The Panl Features" />

_**Image**: The features and functionality of the Panl Simple Results Viewer Web
App_

_The image is a screenshot of the in-built Panl Results Viewer Web App available
in the release package, and whilst not intended as a production search page, can
be used to fine-tune the configuration, or just to have a quick overview of the
results._

1. **A list of available Collections and FieldSet URI Paths (CaFUPs)** that Panl
   is configured to serve. CaFUPs enable different Solr fields to be returned in
   the documents with the same search parameters.

1. **A textual representation of the CaFUPs** that the Panl Results Viewer web
   app is using.

1. **The canonical URI path** (which is returned with the Panl results JSON
   object) - this is important as multiple Panl LPSE URI paths will return
   exactly the same results - this is the unique URI path for this result set
   and necessary for de-duplicating the search engine results. This also
   includes a link to the Panl Results Explainer web app.

1. **The search query box**, by default, Panl responds to the same parameter
   name as The Solr server - i.e. 'q'. This can be configured to be a different
   value should you choose.

   **Specific Search Fields (not shown)** If multiple fields are configured 
   to be searchable, display the fields that are available to search within, and
   allow the user to select within the field.

   <img src="src/docs/specific-search-fields.png" >

   **Integrated Lookahead (not shown)** If multiple fields are configured
   to be searchable, display the fields that are available to search within, and
   allow the user to select within the field.

   <img src="src/docs/lookahead.png" >

1. **Active filters** - either queries or any of the selected facets that have
   been used to refine the search results.

1. **Active BOOLEAN filters** - if the selected facet is a BOOLEAN facet (i.e.
   either true/false) then a link (<img src="src/main/resources/webapp/static/invert.png" alt="invert" />) 
   can be included to invert this selection (i.e. change the value from true if 
   currently false and vice versa).

1. **Active sorting** - sorting options that are currently ordering the
   results - the <img src="src/main/resources/webapp/static/remove.png" alt="remove" /> link 
   is the URI path that will remove this query, facet,
   or sorting option from the results. If it is an active sorting filter, the
   <img src="src/main/resources/webapp/static/invert.png" alt="invert" /> 
   `Change to DESC` or <img src="src/main/resources/webapp/static/invert.png" alt="invert" /> 
   `Change to ASC` links will invert the sorting order without affecting any 
   further sub-ordering.

1. **BOOLEAN Checkboxes** - any facets that have been defined as BOOLEAN 
   checkboxes, which allows the integrator to highlight one of the values 
   (either true or false).

1. **RANGE filters** - for facets that are defined as ranges - allowing
   end-users to select a range of values - the values are inclusive (i.e.
   include the minimum and maximum values).

   **DATE Range filters (not shown)** - Enabling searching on a range of dates 
   (but not a specific date) in the form of:
   next/previous <any_integer> hours/days/months/years.
    - For example:
      - Last 30 days
      - Previous 24 hours

1. **Available filters** - additional facets that can further refine and limit
   the Solr search results.  These facets can be sorted by the count 
   descending (which is the default) and also by the index (or value) either 
   ascending or descending. This may also display a link to load more facets if 
   the returned number of facets is not the complete set.

1. **Number of results found**, and whether this is an exact match.

1. **Query operand** - whether the query is OR, or AND, this affects the search
   query, not the faceting - i.e. the Solr server q.op parameter.

1. **Page information**, the number of pages, how many results are shown per
   page, and how many results are shown on this page.

1. **Sorting options** - Whether to sort by relevance (the default) or by other
   configured sorting options with ascending and descending options available.
   Any Solr field can be configured to be used as a sorting option. And
   multi-sort orders are available, allowing sorting on more than one field.

1. **Pagination options** - the Panl server returns all information needed to
   build a pagination system, number of results, number of results shown per
   page and the current page number.

1. **Number of results per page**. Able to dynamically set the number of results
   to return for the query. **Note:** In the above image, the values 3, 5, 10
   are just examples - this can be set to any positive integer number.

1. **Timing information** about how long the Panl server took to build and
   return the results (including how much time the Solr server took to find and
   return the results).

1. **The results** - the fields that are returned with the documents and are
   shown in the results sections which are configured by the CaFUPs. Multiple
   field sets can be configured for the collection.

## The Panl Results Explainer Web App

<img src="src/docs/panl-results-explainer.png" alt="The In-Built Panl Results Explainer" />

_**Image**: The features and functionality of the Panl results explainer_

_The image is a screenshot of the in-built Panl Results Explainer Web App
available in the release package, and whilst not intended as a production search
page, can be used to look into, troubleshoot, and fine-tune the configuration._

1. **A list of available Collections and FieldSet URI Paths (CaFUPs)** that Panl
   is configured to serve. CaFUPs enable different Solr fields to be returned in
   the documents with the same search parameters.
1. **A textual representation of the CaFUPs** that the Panl Results Viewer web
   app is using.
1. **The canonical URI path entry field** allows you to enter any canonical URI
   path and have the parsing and tokenising explained to you, including whether
   the parsed token was valid, the LPSE code found and the original value that
   Panl attempted to decode.
1. **The request token explainer** - for any canonical URI entered, this will
   list the parsing and decoding steps, with the following details
    1. Whether the token is valid (if it is invalid, it will be ignored and not
       passed through to the Solr search server),
    1. The type of token that was found,
    1. The LPSE code,
    1. The parsed value,
    1. The original value, and
    1. Where pertinent, additional information pertaining to the specific code.
1. **Configuration parameters** - parameters that are not fields or facets with
   information about the value, a description, and the property that set the
   value.
1. **Field configuration explainer** - for each of the fields or facets that are
   configured in the LPSE order an explanation of their configuration including:
    1. The type of Java field type,
    1. The LPSE code,
    1. The Solr field name,
    1. The Solr field type, the Panl field name, and
    1. Additional configuration items which may include Prefixes, Suffixes,
       Ranges, Facet type, or Minimum/maximum values
    1. **Any configuration warning messages** that were found whilst parsing the
       properties files.

## The Panl Single Page Search Web App

<img src="src/docs/panl-single-page-search.png" alt="The Panl Example Single Search Page interface" />

_**Image**: The In-Built Panl Single Page Search Web Application_

Panl also ships with a URL that will provide a separate JSON response, allowing
you to build a single page search interface, giving your users all the options
at a glance.

1. A list of available Collections URI Paths for each available single page
   search interface.
1. The generated Panl LPSE path from the selections.
1. All the facets and the facet values that can be selected.
1. The generated Panl LPSE path from the selections.
1. A search button that will take you the in-built Panl Results Viewer web app
   so that you can view the results instantly.




# Quick Start - The 5 Steps

At the end of this chapter, you will have a web page up and running with the
mechanical-pencils collection indexed and ready to sort and facet on the URL:
http://localhost:8181/panl-results-viewer/


<img src="src/docs/panl-results-viewer.png" alt="The Panl In-Built Simple Results Viewer" />

_**Image**: The In-Built Panl Results Viewer Web Application_

## 0. Download Solr and Panl

Download the latest release of Synapticloop Panl

[https://github.com/synapticloop/panl/releases](https://github.com/synapticloop/panl/releases)

Download the latest version of Apache Solr - this book is using the `9.9.0-slim`
version

[https://solr.apache.org/downloads.html](https://solr.apache.org/downloads.html)

**A Note On Running The Commands**

*These are the commands for either Microsoft Windows or *NIX operating systems
(Linux/Apple Macintosh). Should there be any errors - see the ‘Getting Started’
section for a more in-depth explanation and approach.**

---

> **WARNING:** The Solr Release version before `9.8.0` has different command 
> line options for creating a new example cloud.  For these versions the 
> command line option should be `-noprompt` rather than `--no-prompt`.
> 
> Additionally, when creating the collection, the options should be changed 
> from `--shards` to `-s`.
>
> All other commands remain the same

---

```
**IMPORTANT**: You will need to replace the

SOLR_INSTALL_DIRECTORY

  and

PANL_INSTALL_DIRECTORY

references in the commands for your particular setup.
```

# Windows Commands

```
**IMPORTANT**: Each of the commands - either Windows or *NIX must be run on a
 single line - watch out for continuations.
```

## 1. Create an example cloud instance

This requires no interaction, will use the default setup, two replicas, and two
shards under the 'example' cloud node.

Command(s)

```shell
cd SOLR_INSTALL_DIRECTORY

bin\solr start -e cloud --no-prompt
```

## 2. Create the mechanical pencils collection

This will set up the mechanical pencil collection and schema so that the data
can be indexed.
Command(s)

```shell
cd SOLR_INSTALL_DIRECTORY

bin\solr create -c mechanical-pencils -d PANL_INSTALL_DIRECTORY\sample\solr\mechanical-pencils\ --shards 2 -rf 2
```

## 3. Index the mechanical pencils data

This will index all mechanical pencil data into the Solr instance.
Command(s)

```shell
cd SOLR_INSTALL_DIRECTORY

bin\solr post -c mechanical-pencils PANL_INSTALL_DIRECTORY\sample\data\mechanical-pencils.json
```

## 4. Start the Panl server

This will start the server and be ready to accept requests.
Command(s)

```shell
cd PANL_INSTALL_DIRECTORY

bin\panl.bat -properties PANL_INSTALL_DIRECTORY\sample\panl\mechanical-properties\panl.properties
```

## 5. Start searching and faceting

Open [http://localhost:8181/panl-results-viewer/](http://localhost:8181/panl-results-viewer/)
in your favourite browser.

Choose a collection/fieldset and search, facet, sort, paginate and view the
results

# *NIX Commands

```
**IMPORTANT**: Each of the commands - either Windows or *NIX must be run on
 a single line - watch out for continuations.
```

## 1. Create an example cloud instance

No prompting, default setup, two replicas, and two shards under the 'example'
cloud node.
Command(s)

```shell
cd SOLR_INSTALL_DIRECTORY

bin/solr start -e cloud -noprompt
```

## 2. Create the mechanical pencils collection

Set up the schema so that the data can be indexed.
Command(s)

```shell
cd SOLR_INSTALL_DIRECTORY

bin/solr create -c mechanical-pencils -d PANL_INSTALL_DIRECTORY/sample/solr/mechanical-pencils/ --shards 2 -rf 2
```

## 3. Index the mechanical pencils data

Index all of the data into the Solr instance
Command(s)

```shell
cd SOLR_INSTALL_DIRECTORY

bin/solr post -c mechanical-pencils PANL_INSTALL_DIRECTORY/sample/data/mechanical-pencils.json
```

## 4. Start the Panl server

Ready to go.
Command(s)

```shell
cd PANL_INSTALL_DIRECTORY

bin/panl -properties PANL_INSTALL_DIRECTORY/sample/panl/mechanical-properties/panl.properties
```

View the in-built Panl Results Viewer web application

## 5. Start searching and faceting

Open [http://localhost:8181/panl-results-viewer/](http://localhost:8181/panl-results-viewer/)
in your favourite browser.

Choose a collection/fieldset and search, facet, sort, paginate and view the
results.

# Build/Development Related Tasks

See the `DEV-PROCESS.md` file in this repository for building and testing
the code.

## Building the Code

This will build, test, and assemble the distributable

### _Windows_

```shell
gradlew.bat build
```

### _*NIX_

```shell
./gradlew build
```

## Assembling the Distributable

This will assemble the distribution

### _Windows_

```shell
gradlew.bat assemble
```

### _*NIX_

```shell
./gradlew assemble
```

The distributions (both a `.zip` and a `.tar` file) will be created in the build
distributions directory.

I.e.

- `./build/distributions` (*NIX), or
- `.\build\distributions` (Windows)

with the release files named `solr-panl-9-x.x.x` where `x.x.x` is the version
number.

# Quick Info

## Starting up the example cloud

> **WARNING:** The Solr Release version `9.7.0` has changed the options for
> starting a new example cloud.
> The command line option has changed from `-cloud` to `--cloud`

> All other commands remain the same - For versions greater than `9.7.0` they
> have re-added the `-cloud` option

If you have stopped the example Solr server, starting it up:

### _Windows_

```shell
cd SOLR_INSTALL_DIRECTORY
bin\solr start -cloud -p 8983 -s "example\cloud\node1\solr"
bin\solr start -cloud -p 7574 -s "example\cloud\node2\solr" -z localhost:9983
```

### _*NIX_

```shell
cd SOLR_INSTALL_DIRECTORY
bin/solr start -cloud -p 8983 -s "example/cloud/node1/solr"
bin/solr start -cloud -p 7574 -s "example/cloud/node2/solr" -z localhost:9983
```

## Docker Building

There is an in-built task to build a docker container in gradle

### _ANY OS - WINDOWS/*NIX_

```shell
gradlew docker
```

---

And to run the container

### _ANY OS - WINDOWS/*NIX_

```shell
docker run -p 8181:8181 -p 8983:8983 synapticloop:solr-panl-9-2.1.0
```

_**NOTE:** You do not need to pass through the `-p 8983:8983` command line 
argument if you do not need to view the underlying Solr server._ 

This will expose the ports for both the Panl server (port 8181) and the Solr 
server (port 8983) so that it can be viewed:

 - SOLR: [http://localhost:8983/solr/](http://localhost:8983/solr/)
 - PANL: [http://localhost:8181/panl-results-viewer/](http://localhost:8181/panl-results-viewer/)


# Version History

## 2.2.0 Internal Harnesses (codename `bitter-shadow`) **UNDER DEVELOPMENT**

## 2.1.0 Internal Niceties and Wanted Functionality (codename `hidden-summer`)

- **New Features**
  - Added in `is_multivalue` JSON key to the active facets
  - Added in `panl.extra.<lpse_code>` to add a JSON object keyed on `extra` to
    the returned active and available facets.
  - Added in `panl.server.extra` to add a JSON object keyed on `extra` to 
    the server with every response.
  - Added in `panl.collection.extra` to add a JSON object keyed on `extra` to
    every returned response for the collection (this will overwrite any 
    duplicate keys in the server response above).
  - Added `panl.remove.solr.json.keys` which will removed duplicated information
    and un-needed information in the returned Solr response.
  - Added `panl.lpse.facetorder` to the Panl response object so that the 
    ordering may be different from the LPSE URL order.
  - Added 'More Like This' Solr functionality, including handler and 
    additional properties:
    - `panl.mlt.enable` (default is '`false`')
    - `panl.mlt.handler` (default is '`/mlt`')
    - `panl.mlt.fields` (no default)
  - Added property `solr.numrows.morelikethis` to the collection (default is 5)
  - Ensured that duplicate collections are not registered and that Panl 
    collections are not 
  - Added `indexdesc` as a sorting option so that the facets can be sorted 
    by index (ascending is the default) and now descending.
  - Updated generator to:
    - Include the uniquekey property for the correct Solr field
    - Updated the commenting for Solr fields that are analysed with a 
      warning that it probably shouldn't be a facet.
    - Instead of using LPSE codes for the `panl.lpse.order`, 
      `panl.lpse.facetorder`, `panl.lpse.ignore`, the Solr field name can be 
      used which makes it easier to understand the ordering and ignore codes.
  - Added in a docker build for testing


- **Bug Fixes**
  - Fixed generator where it would leave an empty (and ignored) property of
    `panl.lpse.fields` in the properties file 
  - Fixed bug where BOOLEAN facets were allowed to have multiple values - which 
    it shouldn't.
  - Fixed passing through the LPSE code for the passthrough parameter if there 
    wasn't a passthrough value sent through.
  - Fixed bug with 'extra' JSON object not correctly overriding parent object
  - Fixed connection reset when attempting to get the SolrJ client - now 
    returns a 503 status message
  - Removed unregistered fields that weren't defined in the field list


- **Code Changes**
    - Fixed output formatting for explanation of tokens
    - Refactored constants into single place


- **Documentation Update**
  - Added in documentation for new features
  - Larger documentation update for:
    - 'More Like This' functionality
    - Panl Cookbook
  - Added in new keys and descriptions
  - Spelling and grammar updates
  - Added in more detail for the `TODO` tags

!! The included PDF contains over **600** pages of documentation for every 
part of the Panl server. !!

[View the code for this release](https://github.com/synapticloop/panl/tree/2.1.0)

[Download the release packages](https://github.com/synapticloop/panl/releases/tag/2.1.0)

[See all releases](https://github.com/synapticloop/panl/releases/)


| Release<br />Number   | Short note           | Codename            | Release Date         | 
|-----------------------|----------------------|---------------------|----------------------|
| 2.1.0                 | niceties/want-it-ies | `hidden-summer`     | `TBA`                |
| 2.0.0                 | fluffy stuff         | `billowing-feather` | `February  28, 2025` |
| -- breaking change -- | -------------------- | ------------------- | `------------------` |
| 1.2.0                 | more like this       | `needy-phanton`     | `October   30, 2024` |
| 1.1.1                 | the fly spray        | `grizzled-pebble`   | `September 24, 2024` |
| 1.1.0                 | the better update    | `broad-firefly`     | `September 19, 2024` |
| 1.0.0                 | the initial release  | `bright-wildflower` | `September 04, 2024` |

For full release notes for previous versions, see the [releases page](https://github.com/synapticloop/panl/releases/).


# End Plate 

```
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#                                        __                                   #
#                          .-----.-----.|  |.----.                            #
#                          |__ --|  _  ||  ||   _|                            #
#                          |_.-----.-----.--.--|  |                           #
#                            |  _  |  _  |     |  |                           #
#                            |   __|___._|__|__|__|                           #
#                            |__|     ... .-..                                #
#                                                                             #
#                                ~ ~ ~ * ~ ~ ~                                #
#                                                                             #
#                                                                             #
#                                  SOLR/PANL                                  #
#                                                                             #
#                                  ---------                                  #
#                                                                             #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

                         "Parting is such sweet sorrow"

                                                               Romeo And Juliet 
                                                                         Act 2, 
                                                                       Scene 2, 
                                                                       176–185
```
