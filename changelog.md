# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]

## [v0.3.0]
### Fixed
* Fix XML serializer to produce the correct SRX format
 
### Features
* CLI accepts a locale option
* CLI accepts a target directory as command line argument

### Changes
* Update README.md to warn the locale argument
* Refactor SRX class to be a pure data class
* LanguageCodes to be able to change a locale
* Improve MapRules ctor
* JAXB to generated srx definition
* Bump Gradle Wrapper@9.3.1
* Bump gradle-develocity@4.3.2

## [v0.2.0]
* Update README.md to explain usage
* Configure Gradle to build with JDK 21
* Bump Gradle wrapper@9.3.0
* Bump jackson@2.21.0
* Bump gradle-develocity@4.3.1
* Bump GitHub Actions checkout@v6 setup-java@v5

## v0.1.0
* First internal release

[Unreleased]: https://github.com/omegat-org/segmentation-migrator/compare/v0.3.0...HEAD
[v0.3.0]: https://github.com/omegat-org/segmentation-migrator/compare/v0.2.0...v0.3.0
[v0.2.0]: https://github.com/omegat-org/segmentation-migrator/compare/v0.1.0...v0.2.0
