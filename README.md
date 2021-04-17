sbt-test-metadata
=================

Plugin to support running Scala tests from Emacs.

## Motivation

Ability to provide discovery of defined Scala tests with only [sbt](https://www.scala-sbt.org/) as a dependency. No other tool is necessary.

## Installation

Easiest way to enable `sbt-tests-metadata` plugin is by adding following line into global `~/.sbt/1.0/plugins/plugins.sbt` file:

```
addSbtPlugin("io.github.vlachjosef" % "sbt-tests-metadata" % "0.1.0")
```

## Usage

This plugin defines:

 - **sbt setting** called `testsMetadata`. It provides information about defined tests in sbt projects. By default this setting is empty.

 - **sbt task** called `testsMetadataRefresh`. Running this task will populate `testsMetadata` setting with discovered tests metadata.

Although `testsMetadata` can be queried from sbt and `testsMetadataRefresh` command can be run manually, their intended purpose is to be used transparently from [sbt-test-runner.el](https://github.com/VlachJosef/sbt-test-runner.el).
