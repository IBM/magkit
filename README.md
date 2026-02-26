# Magkit

[![build-module](https://github.com/IBM/magkit/actions/workflows/build.yaml/badge.svg)](https://github.com/IBM/magkit/actions/workflows/build.yaml)

## Scope

The purpose of this project is to provide commonly used helper classes for Magnolia projects. It contains five modules:

* **magkit-core:** A maven module providing utility classes for working with jcr nodes and their properties and values, cache, links... 
* **magkit-notfound:** A magnolia module that provides improved 404 handling
* **magkit-query:** A maven module that provides builders for SQL2 and XPATH query strings
* **magkit-setup:** A maven module that provides util classes to write magnolia configurations and version handler tasks.
* **magkit-ui:** A magnolia module that provides some additional dialog fields and a folder template


### Versions, technology stack and Maven dependency

| Version | Java | Magnolia | Magkit Test | 
|---------|------|----------|-------------|
| 1.0.0   | 11   | 6.2.19   | 1.0.8       | 
| 1.0.1   | 11   | 6.2.45   | 1.0.10      | 
| 1.1.0   | 17   | 6.3.17   | 1.1.0       | 
| 1.2.0   | 17   | 6.4.2    | 1.2.0       | 

To use the magkit-core module in your Maven project, add the following dependency to your `pom.xml`:

```xml
    <dependency>
        <artifactId>magkit-core</artifactId>
        <groupId>de.ibmix.magkit</groupId>
        <version>${module.version}</version>
    </dependency>
```

## Usage

It contains some legacy code for Magnolia 5.x used in old projects, but this is deprecated and will be deleted soon. 

For the modules and usage examples, please see their module readme.md files.

## Notes

If you have any questions or issues, you can create a new [issue here][issues].

Pull requests are very welcome! Make sure your patches are well tested.
Ideally create a topic branch for every change you make. For
example:

1. Fork the repo
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create a new pull request

## License

All source files must include a Copyright and License header. The SPDX license header is 
preferred because it can be easily scanned.

If you would like to see the detailed LICENSE, click [here](LICENSE).

```text
#
# Copyright IBM Corp. 2020-
# SPDX-License-Identifier: Apache2.0
#
```

[issues]: https://github.com/IBM/magkit/issues/new
