# SolangEngine — Project summary

## Purpose and scope

Java script runtime built around `AppScriptEngine`, loading scripts from a configurable path and exposing modules such as `User`.

## Implementation details

Main defaults to `src/main/asol_example`, starts the engine and logs failures. This appears to be a custom scripting environment rather than a general-purpose game engine.

## Current state and known limitations

Substantial engine prototype.

## Recommended next steps

Document script syntax, modules/security boundaries, execution lifecycle, standard library and error reporting; add parser/runtime tests.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

