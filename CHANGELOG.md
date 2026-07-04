# Changelog

All notable changes to the OOSM backend are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/) and [Semantic Versioning](https://semver.org/).

## [1.4.0] - 2026-07-04

- Semver aligned with git history (OOSM rebrand milestone at HEAD)
- Single `VERSION` file synced with Maven POM and `version.properties`
- Docker build embeds version + git SHA for `/actuator/info`
- CI validates version sync; Railway deploy on `pfe-v2-final`

## [0.2.1] - 2026-07-04

- Versioning workflow baseline (superseded by 1.4.0)
