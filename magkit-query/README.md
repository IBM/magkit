# Magkit-Query

<!-- TODO Build Status, is a great thing to have at the top of your repository, it shows that you take your CI/CD as first class citizens -->
<!-- [![Build Status](GitHub Actions) -->

## Overview
Magkit-Query provides fluent, type-safe builders for composing and executing JCR SQL2 and XPath queries in Magnolia.

### Main Features
- Unified facade `Sql2` grouping sub-facades: `Query`, `Statement`, `Condition` for clear separation of concerns.
- SELECT statement shortcuts for common Magnolia node types: pages, components, areas, content, contentNode.
- Rich condition DSL: path (descendant), name, template, identifier, full-text contains, null checks, date/time (created, lastModified, lastActivated, deleted), numeric (long/double) comparisons, between ranges, AND / OR grouping.
- Convenience query helpers: `nodesFromWebsite()`, `nodesFrom(workspace)`, `nodesByIdentifiers(..)`, `nodesByTemplates(..)`, row queries, limit and offset support.
- XPath builder (`XpathBuilder`) with ISO9075 path encoding, node type element selectors, property and ordering constraints, composable `append(...)` operations.
- Safe string composition reducing manual SQL2 / XPath error risk; focused on read-only queries (no repository mutations).
- Returns results as `List<Node>` via fluent terminal methods (`getResultNodes()`), or row results when using row builders.
- Stateless builders intended for per-thread usage; easy extension for custom conditions.

### Quick Examples
Build and execute a SQL2 page query:
```java
List<Node> pages = Sql2.Query.nodesFromWebsite()
    .withStatement(
        Sql2.Statement.selectPages()
            .whereAll(
                Sql2.Condition.Path.isDescendant("/root"),
                Sql2.Condition.String.templateEquals("myModule:page")
            )
    )
    .withLimit(25)
    .getResultNodes();
```
XPath query construction:
```java
String xpath = XpathBuilder.xPathBuilder()
    .path("/root/site")
    .type("*", "mgnl:page")
    .property("@mgnl:template='myModule:page'")
    .orderBy("@jcr:created descending")
    .build();
```

## Usage
Add the Maven dependency to your project.

```xml
<dependency>
  <groupId>de.ibmix.magkit</groupId>
  <artifactId>magkit-query</artifactId>
  <version>1.1.0</version><!-- or the latest released version, e.g. 1.0.2 -->
</dependency>
```

### Search the JCR repository with SQL2 queries
The package `de.ibmix.magkit.query.sql2` contains a fluent builder API for building and executing SQL2 queries and statement strings.
You can execute queries using your handcrafted query string or use the builders to safely create them.

The class Sql2 serves as a facade for all the different builders.

#### Execute handcrafted SQL2 query:
```Java
// To read all page nodes from the WEBSITE repository:

NodesQueryBuilder builder = Sql2.Query.nodesFromWebsite().withStatement("SELECT * FROM [mgnl:page]");
// access the search result al List<Node>:
List<Node> result = builder.getResultNodes();
```
#### Use Statement builder to query for Nodes:
```Java
// To read 10 page nodes from the "my-workspace" repository below the path /root starting from result node 5:

List<Node> result = Sql2.Query.nodesFrom("my-workspace")
            .withStatement(
                Sql2.Statement.selectPages()
                    .whereAll(Sql2.Condition.Path.isDescendant("/root"))
            ).withLimit(10).withOffset(5)
            .getResultNodes();
```

For more examples please inspect the tests.

### Search the JCR repository with XPATH queries

<!-- A notes section is useful for anything that isn't covered in the Usage or Scope. Like what we have below. -->
## Notes

<!-- Questions can be useful but optional, this gives you a place to say, "This is how to contact this project maintainers or create PRs -->
If you have any questions or issues you can create a new [issue here][issues].

Pull requests are very welcome! Make sure your patches are well tested.
Ideally create a topic branch for every separate change you make. For
example:

1. Fork the repo
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## License

All source files must include a Copyright and License header. The SPDX license header is 
preferred because it can be easily scanned.

If you would like to see the detailed LICENSE click [here](../LICENSE).

```text
#
# Copyright IBM Corp. 2020-
# SPDX-License-Identifier: Apache2.0
#
```
## Authors

Optionally, you may include a list of authors, though this is redundant with the built-in
GitHub list of contributors.

- Author: New OpenSource IBMer <new-opensource-ibmer@ibm.com>

[issues]: https://github.com/IBM/magkit/issues/new
