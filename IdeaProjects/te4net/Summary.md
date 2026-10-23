# te4net — Project summary

## Purpose and scope

Java server infrastructure launched by `Main`, which parses args and creates `HServer`; includes HTTP routing/API, user/auth data, email and Minecraft-related services.

## Implementation details

Reviewed auth/data paths include SQL string concatenation and plaintext password comparisons. Root build setup was not obvious in the inventory.

## Current state and known limitations

Substantial backend with security and deployment documentation gaps.

## Recommended next steps

Parameterize SQL, use salted password hashes, document endpoints/sessions/secrets, establish reproducible build and add auth/API integration tests.

## Build, run and validation

Use this project's own build files and any existing README as the source of truth. Supported toolchain versions and exact commands have not been verified as part of this repository-wide inventory; do not assume compatibility based on neighboring projects. A complete hand-off should record reproducible build/run commands, required external services/configuration and one smoke test with expected output.

