# GraalSpy — Project summary

## Purpose and scope

Experimental Java scripting integration using GraalVM Polyglot APIs for script-defined plugins and event handlers.

## Implementation details

A plugin abstraction uses Graal `Value` to connect script runtime values with host callbacks; this does not itself make a secure sandbox.

## Current state and known limitations

Experimental; script host-access boundaries matter.

## Recommended next steps

List supported languages/runtime versions, constrain host capabilities, handle script exceptions and test unload/reload.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

