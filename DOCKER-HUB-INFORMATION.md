```
                     '-/syhhhys+-'
                  ':smMNmhyyyhmMMd:                                      ''..'
                 -hMNy/...-/+/./mMN-                          '..'     '/ysyhm-
                :mMh-..-smMMh-..dMM/'''''''    ''''''''''.sssyhssd/'' -ho:yosM:    '' ''''''
               .dMd.../NMNNMy/+hmmNyydyyyyys:+ssyyhyyyyyhhs-:Ns-+Ndyyyh::hosNdyy//syysyyyyyy+'''
               -MMo...NMN:/dNNMo.so.sd.-o..md+:/./d-.o..d+.:yo./d+:-:m-.+ody::+ymo-+:do.//.oNydo
               -MMs..:MMm' '-do.oo.oh-:d+.os.:h::h:-o/.:/.-ds.:y.:mhh:.ymm+.o+:/-.s/::.++-.+shd-
               'yMN:..dMM+  ys./o.:o.:m+.:+..y:.s-.ys./y..s/.-s..dNy:./my:.:d/d/.oy/+./h:-ym+:'
                .dMd-.-dMN+.N/.../s::mm::/+::/:-..:++yNo::+:-:o::+oys:/oyo-:odN/-/so..+/sdy-
                 -mMh..-mMNyho+./Nyssoossoosshm:.+oNs::sssosss+osso:+sso/osso-/syNy./:Nd+-'
         ''-://++/yMM/../Mmohy-+No'         .d/.yoyN'                           +h./m:Mo
      '-+hmNNmmmmmNMM+...h/ho:ym/'          +d.+y+No                           'm:-h+hm.
     .sNNho/:-----:/s-...yyyhho.          ./dNsyhd+'                            sysydy-
    'hMm:..:oyhdddy:.....yNMMmyo/:--..-:+ymMmMMMds+////oy/'                     '-:-.'
    :MM+..oNMMMMmy/.....:.-:oydmNNNNNNNNmds/-:oydmmNNNmmMNh/'
    -NMs...:///-.....-omNmy+:-.---:///:--.........---/yNMNh:'
    'oNMh+-.......:+yNMNyymNNNdys+/:-...........-:+sdNMms-'
     ':ymMNNmmmmmNNMNdo-' '.:oydmNMNNNmmddddddmNNNMNds:.
        '-/+ssssso/-.'         ''.-:++ssyyyyyyss+/-.'

```

> **_Rapidly get up and running with a fully featured, SEO friendly, keyword
> searchable, faceted search engine with an in-built, search page to test it all
> out._**

# Synapticloop Panl Server

These images contain examples of the Panl server integrating with specific versions 
of the Solr server.

Docker hub tags that start with
 - `solr-panl/9` integrate with Solr version 9
 - `solr-panl/8` integrate with Solr version 8
 - `solr-panl/7` integrate with Solr version 7

The number after the Solr implementation number are the Panl server version.  It is always
recommended that you use the latest Panl version.  All Panl major versions are backwards 
compatible with the configuration and the LPSE URL will not change.

Source code and releases can be found here:

 - [GitHub Source](https://github.com/synapticloop/panl)
 - [GitHub Releases](https://github.com/synapticloop/panl/releases)

> **_And, yes, there is full documentation available, over 600 pages,
> covering all aspects of the configuration of the Panl server so that you
> can get the most out of your Solr and Panl experience._**

 - [Latest Release Documentation (PDF)](https://github.com/synapticloop/panl/tree/main/src/dist/book)
 - [Latest Release Documentation (HTML)](https://synapticloop.github.io/panl/book/)

# IMPORTANT:

Apache®, Solr® the names of Apache projects, and the multicolor feather logo are
registered trademarks or trademarks of the Apache Software Foundation in the
United States and/or other countries.

The mention and references of any Apache projects, sub-projects, or resources in
no way constitutes an endorsement for Synapticloop or the Panl project.

# Why?

Because...

`/Caran d'Ache/true/Black/bDW/`

looks **A LOT** nicer than

`q=*:*&facet.mincount=1&rows=10&facet.field=lead_size_indicator&facet.field=grip_material&facet.field=colours&facet.field=nib_shape&facet.field=diameter&facet.field=cap_shape&facet.field=brand&facet.field=mechanism_type&facet.field=length&facet.field=hardness_indicator&facet.field=grip_type&facet.field=cap_material&facet.field=lead_grade_indicator&facet.field=tubing_material&facet.field=in_built_sharpener&facet.field=disassemble&facet.field=category&facet.field=body_shape&facet.field=clip_material&facet.field=mechanism_material&facet.field=lead_length&facet.field=body_material&facet.field=in_built_eraser&facet.field=grip_shape&facet.field=relative_weight&facet.field=name&facet.field=nib_material&facet.field=weight&facet.field=variants&facet=true&fq=brand:"Caran+d'Ache"&fq=disassemble:"true"&fq=colours:"Black"&q.op=AND`

# Why Synapticloop Panl?

Panl was designed to convert rather long and unfriendly (both in human-readable
and SEO terms) to shorter, nicer, and friendlier URL paths throughout the entire
search journey.

Working with a Solr schema, the Panl configuration files translate unwieldy URL
parameters into concise and precise URL paths.

- **Have SEO friendlier URL paths with much shorter URLs than traditional query
  parameters (all the way through the search journey)** - This was the primary
  driver and the base functionality.

- **Abstract away the complexities of the Solr query string** - Being able to
  have a simple interface through the URL which could generate complex queries.
  Not having to fully understand how Solr works in the back-end abstracts away
  the complexity of a front-end integrator and reduces the need to have the
  back-end and front-end understand each-other.

- **Be quick to start up and easy to configure** - During development of a
  solution, being able to iterate over a solution, or change the way that Panl
  is configured is a must-have. Additionally, being able to upgrade the Panl
  server and have the configuration files be automatically picked up and work
  without any changes is a plus. (on the aging Windows development
  build machine, it takes less than a second to start..)

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
   ‘False’ values can be replaced with arbitrary text, which will be
   transparently converted between Panl and Solr. For the LPSE URI path of

   `/Caran d'Ache/true/Black/bDW/`

   the true value (which is defined as whether the mechanical pencil can be
   disassembled could be changed to ‘`Able to be disassembled`’ for `true`
   values, and ‘`Cannot be disassembled`’ for `false` values.

   The above URI paths would then become

   `/Caran d'Ache/Able to be disassembled/Black/bDW/`

   and

   `/Caran d'Ache/Cannot be disassembled/Black/bDW/`

1. **BOOLEAN checkboxes** - Whilst this may seem obvious to have a checkbox for
   a true/false value, the checkboxes work in a subtly different way.  By
   selecting the checkbox, the only one of facet values will be selected when
   deselected, the BOOLEAN facet is in a don't care start - the facet value can
   be either of the values.

1. **OR Facets** - Where an item has only one facet value attached to it (for
   example a pencil will only be manufactured by one company), OR facets allow
   you to expand the selection to choose one or more of the single-valued
   facets.

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
   be selected to be searched on, for example, in the Bookstore Walkthrough,
   the user can select to search within the title, the author, the description,
   none (i.e. the default search) or all of them.  Additionally, you may choose
   to configure the Solr query time boost to put more weight on one (or more)
   of the fields.

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

1. **UNLESS facets** - Continue to show a facet unless another specified facet
   is selected.  This can be thought of as the inverse of a hierarchical facet
   and is useful when a facet no longer becomes relevant as the user goes
   through the search journey.

1. **SORTED facets** - Each facet can be individually configured to order
   the facet results by either the facet count in descending order (which is
   the default), or the facet value (e.g. alphabetic/numeric based on the
   value of the facet - in _either_ ascending or descending).

1. **MORE facets** - Solr (and Panl) configures a limit for the maximum
   number of facet values that are returned with any query, this functionality
   enables you  to dynamically load additional facet values if they are
   available but weren't returned with the results by default.

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
   each Solr request can be cached.

1. **100% TEXT CONFIGURATION** - All configuration for Panl is based on text
   files (Java `.properties`) files so they can be stored in a source code
   management system.  Additionally, upgrades to the Panl server are easy -
   just drop in the new Panl release package, use your existing
   configuration, and it will just work.  And with quick restart times, the
   configuration changes will be seen in an instant.

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
