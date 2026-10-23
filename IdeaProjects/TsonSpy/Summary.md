# TsonSpy — Project summary

## Purpose and scope

Bukkit/Spigot base plugin that loads TSON config and installs a Java Scripting API engine, preferring configured engine and falling back to Groovy JSR-223.

## Implementation details

TsonPlugin wires metrics and TSON event/listener utilities; config errors fall back to an empty map, so diagnostics and script trust boundaries matter.

## Current state and known limitations

Experimental scriptable server plugin; scripts may have powerful in-process access.

## Recommended next steps

Document script API/events/engine versions and permissions; restrict script sources, improve errors and test engine fallback.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

