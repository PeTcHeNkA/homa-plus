# NLaunch — Project summary

## Purpose and scope

Java class/resource-loader experiment that routes loading through native code; related to `GolandProjects/jnigga`.

## Implementation details

Critical contracts include bootstrap, class delegation, resource lookup, native initialization and classpath behavior; this is low-level JVM infrastructure rather than merely an end-user launcher.

## Current state and known limitations

Experimental runtime component.

## Recommended next steps

Document native builds per OS/architecture, supported JVM versions and tests for classes/resources/missing entries.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

