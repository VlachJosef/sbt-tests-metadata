sbt-test-metadata
=================

Plugin to support running Scala tests from Emacs.

## Installation

Easiest way to enable `sbt-tests-metadata` plugin is adding following line into global `~/.sbt/1.0/plugins/plugins.sbt` file:

```
addSbtPlugin("io.github.vlachjosef" % "sbt-tests-metadata" % "0.1.0")
```

## Usage

This plugin defines one new setting called `testsMetadata`. This setting holds information about defined tests in sbt project. By default this setting is empty. To populate this setting you can run `testsMetadataRefresh` command.

Although `testsMetadata` can be queried from sbt and `testsMetadataRefresh` command can be run manually, their intended purpose is to be used transparently from [sbt-test-runner.el](https://github.com/VlachJosef/sbt-test-runner.el).
