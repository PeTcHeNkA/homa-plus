# MEStatueGenerator — Project summary

## Purpose and scope

Bukkit plugin for creating ModelEngine-backed statues/posed model entities in-game.

## Implementation details

The `cs` command spawns an ArmorStand, wraps it in Te4ModelledEntity, attaches an active model and adds it to a parent player. A timed path uses unsafe Thread.stop().

## Current state and known limitations

Concrete game-server feature with legacy thread handling.

## Recommended next steps

Document command/model/state args and dependencies; replace Thread.stop() with cancellable, server-thread-safe scheduling.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

