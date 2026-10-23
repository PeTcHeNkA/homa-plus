# homa-plus

A consolidated collection of independent projects preserved from several development workspaces.

## Project collections

- `GolandProjects/` — projects grouped from a GoLand workspace.
- `IdeaProjects/` — projects grouped from an IntelliJ IDEA workspace.
- `LAunch/` — projects retained under their original collection name.
- `VisualStudioRepos/` — projects grouped from a Visual Studio workspace.

Projects retain their own directory structure. Languages, build systems, dependencies, and runtime requirements may differ. Consult project-level documentation and build files where available.

## Git history

Available histories from the original repositories are connected to `main` through a single multi-parent integration commit. This keeps independent histories in the Git commit graph without adding a separate integration commit for every source branch.

Project trees were relocated beneath their collection paths during import, so rewritten commit IDs differ from their original IDs.

Some projects had Git metadata but no reachable commits:

- `GolandProjects/vwlaunch`
- `IdeaProjects/te4_async_utls`
- `IdeaProjects/TestProject2`
- `IdeaProjects/TJoinControl`

Only their available current files could be preserved; there were no existing commits to import.

Original version tags are retained under the `history/<project-path>/<tag>` namespace to avoid naming collisions between projects. They are tags, not additional working branches.

The default branch is `main`.

## Notes

This is a collection of independent projects, not a single unified build. Existing dependencies, binaries, generated assets, or build outputs may be present. Review each project's documentation and dependencies before building or running it.
