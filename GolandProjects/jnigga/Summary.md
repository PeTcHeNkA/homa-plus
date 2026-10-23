# jnigga — Project summary

## Purpose and scope

Go/CGO/JNI experiment bridging Go and a JVM for Java class/resource loading and decoding packaged resources.

## Implementation details

Go utilities handle files and archive-backed resources; the project is related to Java-side NLaunch. In reviewed utils.go, WaitGroup.Done() runs before worker work finishes and a shared error variable is used concurrently.

## Current state and known limitations

Experimental; concrete concurrency bug and possible data race.

## Recommended next steps

Defer Done until worker completion, return errors per worker, add race-enabled tests, and document JDK/CGO/OS requirements.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

