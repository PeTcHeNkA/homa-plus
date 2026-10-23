# TCord — Project summary

## Purpose and scope

Distributed large-text translation tool with a Java server and Python worker clients.

## Implementation details

README says run `TCord.jar` on Java 22+ with source/result JSON files; Python 3.9–3.11 clients install requirements and connect to `host:port`. Server distributes translation work among clients; examples live under `run/`.

## Current state and known limitations

Multi-process tool with a useful README; server/client versions must align.

## Recommended next steps

Document wire protocol, scheduling/retries, JSON schemas, trust boundaries and failed-job reporting.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

