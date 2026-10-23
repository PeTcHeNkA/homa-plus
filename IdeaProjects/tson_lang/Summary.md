# tson_lang — Project summary

## Purpose and scope

IntelliJ Platform plugin adding TSON syntax support through a custom lexer/editor integration.

## Implementation details

TsonLexer.reset() ignores `start`, `end` and `initialState`, which can break incremental lexing/highlighting and non-zero offsets.

## Current state and known limitations

IDE plugin with a concrete lexer correctness issue.

## Recommended next steps

Honor all reset parameters, test partial buffers/incremental edits and document IntelliJ Platform compatibility.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

