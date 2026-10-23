# spShip — Project summary

## Purpose and scope

Java/Bukkit project with a reusable YandeRePlugin base for commands, configuration and recursive reloadable components; ship-specific gameplay needs deeper review.

## Implementation details

The base class manages configs, command/subcommand trees and Reloadable lifecycle. Exact ship features should not be guessed from the folder name.

## Current state and known limitations

Plugin-sized codebase with custom command/config infrastructure.

## Recommended next steps

Document user-facing ship features separately from framework internals, required dependencies and lifecycle/reload tests.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

