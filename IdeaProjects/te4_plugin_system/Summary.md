# te4_plugin_system — Project summary

## Purpose and scope

Java plugin runtime loading JARs through a custom class loader, reading `plugin.tson`, ordering by priority and calling lifecycle hooks.

## Implementation details

This is a plugin framework, not a security sandbox: loading arbitrary JARs executes code in-process.

## Current state and known limitations

Experimental runtime framework; trust boundaries matter.

## Recommended next steps

Document descriptor schema, lifecycle, dependencies/classloader rules and tests for missing dependencies, duplicate IDs and startup failure.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

