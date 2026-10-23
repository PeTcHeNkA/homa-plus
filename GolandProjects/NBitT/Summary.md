# NBitT — Project summary

## Purpose and scope

Go prototype for typed binary values and reflection-based serialization/deserialization of structs using `nbt` tags.

## Implementation details

nbitt.go defines typed values for primitives, strings, maps, lists and arrays; main.go demonstrates nested TItem round-tripping. The module targets Go 1.18 and includes a binary sample.

## Current state and known limitations

Useful prototype, not yet a proven stable format library; source contains unfinished areas.

## Recommended next steps

Add round-trip tests for every type, validate lengths and malformed input, fix list sizing, and document the binary format.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

