# Magkit-Query

<!-- TODO Build Status, is a great thing to have at the top of your repository, it shows that you take your CI/CD as first class citizens -->
<!-- [![Build Status](GitHub Actions) -->

## Scope

This module provides provides builders for SQL2 and XPATH query strings.

## Usage
### Search the JCR repository with SQL2 queries
The package `de.ibmix.magkit.query.sql2` contains a fluent builder API for building and executing SQL2 queries and statement strings.
You can execute queries using your handcrafted query string or use the builders to safely create them.

The class Sql2 serves a s a facade for all the different builders.

#### Execute handcrafted SQL2 query:
```
To read all page nodes from the WEBSITE repository:
NodesQueryBuilder builder = Sql2.Query.nodesFromWebsite().withStatement("SELECT * FROM [mgnl:page]");
// access the search result al List<Node>:
List<Node> result = builder.getResultNodes();
```
#### Use Statement builder to query for Nodes:
```
To read 10 page nodes from the "my-workspace" repository below the path /root starting from result node 5:
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
