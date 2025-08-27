# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.1] - 2025-08-27

### Fixed

- missing fetch all commit in release
- add missing jacoco plugin to calculate coverage
- sonar issues

### Changed

- update dependency versions

## [0.1.0] - 2025-08-26

### Added

- migrate gitlab ci to github action
- renaming Directory Manager to Identity Manager
- integrate entity mapper
- exclude disabled routes based on configuration
- provide context for all provider plugin
- return only attributes from entity in controller
- manage uncatch error from delete action
- add service to manage jinja templating
- manage route for dynamic entities and plugins
- setup base of plugin management
- load plugin from jar inside a system folder
- setup watcher for configuration and all configuration services
- setup internationalization controller and service


[0.1.1]: https://github.com/linagora/linid-im-api/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/linagora/linid-im-api/releases/tag/v0.1.0
