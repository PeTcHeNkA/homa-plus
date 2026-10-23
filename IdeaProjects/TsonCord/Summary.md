# TsonCord — Project summary

## Purpose and scope

Java Discord bot using JDA and a TSON-based plugin/config builder.

## Implementation details

Main constructs JDATsonBuilder with `dsplugins`, delegating behavior to the builder/framework. Real Discord credentials must be injected outside source control.

## Current state and known limitations

Bot with custom config integration.

## Recommended next steps

Document intents/permissions, plugin discovery, config schema and deployment; keep tokens out of repository and test invalid config.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

