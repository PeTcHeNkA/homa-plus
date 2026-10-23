# TimidBlocks — Project summary

## Purpose and scope

Client/server game feature where spoken words cause a block to disappear; includes Fabric client mod, server plugin variants and audio library.

## Implementation details

README specifies Fabric 1.21.8 with loader 0.16.1+1.21.8 for client and tested server support 1.16.5–1.21.8. `config.tson` sets model-dir, speech buffers, audio rate and phrase-to-material mappings; microphone needs 22050 Hz/16-bit and server uses Vosk models.

## Current state and known limitations

Multi-module project with external audio/model requirements and several variants (`AudioLib`, `TimidBlocks-SP`, `ALLE2`).

## Recommended next steps

Document canonical modules, Vosk model setup/licensing, client/server audio synchronization and build/run per version.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

