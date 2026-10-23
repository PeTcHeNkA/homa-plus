# TsonConfigurations — Project summary

## Purpose and scope

Java parser/model library for TSON, STSON and JSON-like input; supports primitives, maps, lists, class references, object construction and serialization.

## Implementation details

TsonObj is the base abstraction; TsonMap/TsonList represent collections; TsonField can construct classes; TJsonParser handles JSON. ClassManager controls which classes may be created. STSON can execute calls/expressions, so it is more than passive config.

## Current state and known limitations

Core library with an IntelliJ syntax plugin and consumer projects; reflective/executable features have security implications.

## Recommended next steps

Document grammar/escaping/serialization, add parser round-trip/error tests and use restrictive class allowlists for reflective construction/expression execution.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

