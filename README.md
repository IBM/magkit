# Magkit

<!-- TODO Build Status, is a great thing to have at the top of your repository, it shows that you take your CI/CD as first class citizens -->
<!-- [![Build Status](GitHub Actions) -->

## Scope

The purpose of this project is to provide commonly used helper classes for magnolia projects. It contains 5 modules:

* **magkit-core:** A maven module providing utility classes for working with jcr nodes and their properties and values, cache, links... 
* **magkit-notfound:** A magnolia module that provides improved 404 handling
* **magkit-query:** A maven module that provides builders for SQL2 and XPATH query strings
* **magkit-setup:** A maven module that provides util classes to write magnolia configurations and version handler tasks.
* **magkit-ui:** A magnolia module that provides some additional dialog fields and a folder template

## Usage
This project needs 
* Java 11 or later
* Magnlia 6.2.19 or later

It contains some legacy code for Magnolia 5.x used in old projects but this is deprecated and will be deleted soon. 

For the purpose of the modules and usage examples please see their module readme.md files.

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

If you would like to see the detailed LICENSE click [here](LICENSE).

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
