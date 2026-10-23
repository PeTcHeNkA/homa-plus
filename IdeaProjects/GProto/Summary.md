# GProto — Project summary

## Purpose and scope

Java tools for inspecting game tables/localization data and editing a character-skin binary database.

## Implementation details

Main uses GroupScanner/JsonTMap to apply translation JSON to an English `.bytes` pack, scan usages and export output. SkinMain can list skin records or copy one skin onto another. Defaults include machine-specific absolute paths.

## Current state and known limitations

Practical data-conversion/reverse-engineering utility; CLI defaults are machine-specific.

## Recommended next steps

Replace hardcoded paths with arguments/config, validate inputs, back up outputs and document both translation and skin workflows.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

