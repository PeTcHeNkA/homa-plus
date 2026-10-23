# DmPathes — Project summary

## Purpose and scope

Java/Gradle collection containing a Bukkit damage-indicator plugin and a reusable YandeRePlugin command/configuration framework.

## Implementation details

DamageIndicator selects packet-manager implementations for server internals including 1.16_R3, 1.17_R1 and 1.18_R1. The base class manages commands, reloadable configs and nested subcommands.

## Current state and known limitations

Plugin with version-sensitive Bukkit/NMS integration; folder name is misleading.

## Recommended next steps

Document the supported server matrix and packet display flow; separate reusable framework from indicator feature if both are maintained.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

