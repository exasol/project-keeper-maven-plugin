# Project keeper maven plugin 0.6.0, released 2021-03-19

Code name: Validation of dependency update section in changes file.

## Summary

This release adds validation for dependency changes in the changes file. In addition, it updates the project configuration for releasing with release-droid 0.4.0.

## Features

* #67: Added broken links checker workflow
* #25: Added validation for dependency update section in changelog
* #85: Added support for new release-droid version
* #93: Added validation for reproducible-build-maven-plugin

## Bugfixes

* #70: Fixed changes template
* #76: Dependencies with property versions break dependency section validation
* #74: Fixed a bug that the project was only valid after running fix twice
* #78: Fixed that plugins with no versions were ignored in changelog
* #83: Fixed wrong format for updated dependencies
* #87: Fixed failing dependency section validation after release

## Refactoring

* #91: Update version of links' checker workflow

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:0.2.2` to `0.4.0`
* Added `org.apache.maven:maven-core:3.6.3`
* Added `org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r`

### Test Dependency Updates

* Removed `org.apache.maven:maven-core:3.6.3`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:0.5.0` to `0.6.0`
* Added `io.github.zlika:reproducible-build-maven-plugin:0.13`
