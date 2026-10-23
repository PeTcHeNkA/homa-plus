# homa-plus

**homa-plus is a collection of 76 independent projects** imported from several development workspaces. It is an archive/catalog, not one application and not a unified build.

## How to navigate

- Projects are grouped under `GolandProjects/`, `IdeaProjects/`, `LAunch/` and `VisualStudioRepos/`.
- Each project has a `Summary.md` with its purpose, implementation details, current limitations and recommended maintenance work.
- The catalog below includes the actual summary text so you can understand the repository without opening 76 separate files.
- The descriptions distinguish details visible in code/README from unresolved intent. A project name alone is not treated as proof of functionality.
- No blanket build/runtime verification was performed across all projects. Build systems and supported versions vary independently.

## Complete project catalog

### `GolandProjects/`

#### [`jnigga`](GolandProjects/jnigga/Summary.md)

**Purpose:** Go/CGO/JNI experiment bridging Go and a JVM for Java class/resource loading and decoding packaged resources.

**Implementation:** Go utilities handle files and archive-backed resources; the project is related to Java-side NLaunch. In reviewed utils.go, WaitGroup.Done() runs before worker work finishes and a shared error variable is used concurrently.

**Status:** Experimental; concrete concurrency bug and possible data race.

**Next steps:** Defer Done until worker completion, return errors per worker, add race-enabled tests, and document JDK/CGO/OS requirements.

#### [`lc_key`](GolandProjects/lc_key/Summary.md)

**Purpose:** Small Go project with a comparatively large footprint of non-source data/artifacts; purpose is not reliably identifiable from the name alone.

**Implementation:** The tree contains few Go files and large data/binary files. Their role as inputs, fixtures or generated output is not established.

**Status:** Needs source-level identification.

**Next steps:** Document the entry point and data format, explain artifact provenance, and add a usage example and tests before cleanup.

#### [`NBitT`](GolandProjects/NBitT/Summary.md)

**Purpose:** Go prototype for typed binary values and reflection-based serialization/deserialization of structs using `nbt` tags.

**Implementation:** nbitt.go defines typed values for primitives, strings, maps, lists and arrays; main.go demonstrates nested TItem round-tripping. The module targets Go 1.18 and includes a binary sample.

**Status:** Useful prototype, not yet a proven stable format library; source contains unfinished areas.

**Next steps:** Add round-trip tests for every type, validate lengths and malformed input, fix list sizing, and document the binary format.

#### [`TableEditor`](GolandProjects/TableEditor/Summary.md)

**Purpose:** Go project organized around a package named `LApi`; the name suggests table editing, but formats and user workflow need confirmation.

**Implementation:** Several Go sources are present, but the tree alone does not prove whether this is a GUI, CLI or library or which table format it supports.

**Status:** Small-to-medium; description partly unresolved.

**Next steps:** Document input/output formats, row/column representation, public API, a minimal edit/save example and malformed-input behavior.

#### [`vwlaunch`](GolandProjects/vwlaunch/Summary.md)

**Purpose:** Go/Wails desktop launcher with a web frontend and Go backend for desktop integration.

**Implementation:** Frontend assets are combined with backend code for window/file/cache actions and update-related logic.

**Status:** Substantial desktop application; imported history was unavailable, so current files are the source of truth.

**Next steps:** Document Go/Node/Wails versions, dev mode, packaging per OS, update flow and runtime configuration.

### `IdeaProjects/`

#### [`authpatch`](IdeaProjects/authpatch/Summary.md)

**Purpose:** Java/Gradle project named for authentication patching; the target application and actual patch behavior are not established by directory metadata.

**Implementation:** There is a small Java source footprint and no obvious root README; the name alone does not establish what is patched.

**Status:** Purpose and supported target/version need confirmation.

**Next steps:** Identify target libraries and entry points, list modified behavior, specify versions and document reproducible apply/revert steps.

#### [`BikaAI`](IdeaProjects/BikaAI/Summary.md)

**Purpose:** IntelliJ-generated Java starter project rather than an implemented AI application in its current state.

**Implementation:** Inspected Main.java prints a greeting and numbers 1 through 5; no AI integration is visible in that entry point.

**Status:** Boilerplate/sample; current functionality does not match the name.

**Next steps:** Implement and document the intended AI use case or rename/archive the starter project and remove template comments.

#### [`CameraPlus`](IdeaProjects/CameraPlus/Summary.md)

**Purpose:** Java/Gradle project with many Java files; the name suggests a camera feature for a game or application, but host platform and behavior need confirmation.

**Implementation:** The tree is larger than a one-class stub and includes test-source code; inventory alone does not establish whether this is a plugin, mod or standalone app.

**Status:** Substantial but under-documented.

**Next steps:** Use descriptors, dependencies and public classes to document camera features, controls, supported versions and tests.

#### [`DisableServerCmd`](IdeaProjects/DisableServerCmd/Summary.md)

**Purpose:** Minimal Java/Gradle project whose name suggests disabling or restricting server commands.

**Implementation:** A single primary Java class appears to contain most behavior; exact commands, permissions and server implementation are not confirmed.

**Status:** Small plugin/prototype.

**Next steps:** Name the affected commands, explain the enforcement mechanism, document permissions/server versions and test allowed/denied cases.

#### [`DmPathes`](IdeaProjects/DmPathes/Summary.md)

**Purpose:** Java/Gradle collection containing a Bukkit damage-indicator plugin and a reusable YandeRePlugin command/configuration framework.

**Implementation:** DamageIndicator selects packet-manager implementations for server internals including 1.16_R3, 1.17_R1 and 1.18_R1. The base class manages commands, reloadable configs and nested subcommands.

**Status:** Plugin with version-sensitive Bukkit/NMS integration; folder name is misleading.

**Next steps:** Document the supported server matrix and packet display flow; separate reusable framework from indicator feature if both are maintained.

#### [`ELegantC`](IdeaProjects/ELegantC/Summary.md)

**Purpose:** Java Bukkit/Spigot chat/message plugin scaffold using Adventure MiniMessage, PlaceholderAPI and TSON configuration.

**Implementation:** ElegantMain requires PlaceholderAPI at startup and initializes an ElMessageBus. TODOs and commented-out command/event registration indicate unfinished paths.

**Status:** Partly implemented plugin scaffold with explicit dependencies.

**Next steps:** Document config and placeholder behavior, finish/remove TODO paths, define eval permissions and test startup failures.

#### [`fileSystemHelper`](IdeaProjects/fileSystemHelper/Summary.md)

**Purpose:** Java utility project focused by name on filesystem helper operations.

**Implementation:** Several Java sources exist, but the public API and supported operations must be determined from actual classes, not inferred from the name.

**Status:** Utility; API needs documentation.

**Next steps:** List operations, define path normalization/symlink behavior and exception semantics, and add temporary-directory tests.

#### [`FormateX`](IdeaProjects/FormateX/Summary.md)

**Purpose:** Java library documented as LSFPS-Format: a text formatter with typed variable providers and precompiled templates.

**Implementation:** README examples use MapVarProvider, FormatBuilder and Formatter. Variables can be registered manually or resolved from nested object properties; delimiters are configurable and multiple providers can be selected by input type.

**Status:** One of the better documented libraries; formats are intended to be built before repeated formatting.

**Next steps:** Add tests for null/missing/nested fields, escaping and provider selection; document formatter thread safety.

#### [`GProto`](IdeaProjects/GProto/Summary.md)

**Purpose:** Java tools for inspecting game tables/localization data and editing a character-skin binary database.

**Implementation:** Main uses GroupScanner/JsonTMap to apply translation JSON to an English `.bytes` pack, scan usages and export output. SkinMain can list skin records or copy one skin onto another. Defaults include machine-specific absolute paths.

**Status:** Practical data-conversion/reverse-engineering utility; CLI defaults are machine-specific.

**Next steps:** Replace hardcoded paths with arguments/config, validate inputs, back up outputs and document both translation and skin workflows.

#### [`GraalSpy`](IdeaProjects/GraalSpy/Summary.md)

**Purpose:** Experimental Java scripting integration using GraalVM Polyglot APIs for script-defined plugins and event handlers.

**Implementation:** A plugin abstraction uses Graal `Value` to connect script runtime values with host callbacks; this does not itself make a secure sandbox.

**Status:** Experimental; script host-access boundaries matter.

**Next steps:** List supported languages/runtime versions, constrain host capabilities, handle script exceptions and test unload/reload.

#### [`gravity-drive`](IdeaProjects/gravity-drive/Summary.md)

**Purpose:** Java/Gradle project likely centered on a game mechanic or extension; exact behavior is not confirmed by the name.

**Implementation:** Multiple source files exist, but target game, implementation mechanism and controls need confirmation from descriptors and entry points.

**Status:** Needs a source-level feature summary.

**Next steps:** Identify platform/entry point and document mechanics, config/commands, dependencies and supported versions.

#### [`GuiEngineV2`](IdeaProjects/GuiEngineV2/Summary.md)

**Purpose:** Small Java prototype for a GUI engine or UI abstraction.

**Implementation:** Only a few source files are present; no evidence establishes a stable widget API, renderer or production-ready toolkit.

**Status:** Prototype.

**Next steps:** Document intended rendering/event model, add a minimal example app and distinguish implemented features from plans.

#### [`hello_nlaunch`](IdeaProjects/hello_nlaunch/Summary.md)

**Purpose:** Minimal Java demonstration/test for the NLaunch native class-loader experiment.

**Implementation:** Tiny source footprint suggests a smoke test rather than a standalone application.

**Status:** Example/smoke test.

**Next steps:** Document native library/JVM prerequisites, exact launch command and expected class/resource-loading result.

#### [`HelloNL_SPIGOT`](IdeaProjects/HelloNL_SPIGOT/Summary.md)

**Purpose:** Java/Gradle integration experiment connecting NLaunch with Spigot, with `main` and `protected` modules.

**Implementation:** The multi-module layout suggests normal and specially packaged variants; the precise protection mechanism must be verified.

**Status:** Integration/demo project with multiple build variants.

**Next steps:** Explain module differences, JAR packaging, compatible Spigot/JDK versions and reproducible server startup.

#### [`Linguistic`](IdeaProjects/Linguistic/Summary.md)

**Purpose:** Java project with a large source tree and a language/linguistics-oriented name.

**Implementation:** The source count indicates a multi-class application/library, but exact algorithms, inputs and supported languages are not established by inventory alone.

**Status:** Substantial but under-documented.

**Next steps:** Document the processing pipeline, data models, supported languages/corpora, public API and representative tests.

#### [`LoliLo`](IdeaProjects/LoliLo/Summary.md)

**Purpose:** Small Java project with a README and a few source files.

**Implementation:** The README should be the first source of truth; the name alone does not establish whether this is a plugin, tool or library.

**Status:** Small project; purpose should be extracted from README/source.

**Next steps:** Summarize the README, list dependencies/runtime version and add a minimal build/run example.

#### [`LSN2`](IdeaProjects/LSN2/Summary.md)

**Purpose:** Small Java/Gradle project with a few source files and no confirmed public description.

**Implementation:** The short name does not reliably identify intended use; avoid speculative acronym expansion.

**Status:** Needs source-level identification.

**Next steps:** Record consumers, main classes, supported runtime versions and a reproducible test command.

#### [`mcforge_patch`](IdeaProjects/mcforge_patch/Summary.md)

**Purpose:** Java/Gradle repository for patches associated with Minecraft Forge.

**Implementation:** Compatibility depends on exact Minecraft/Forge versions and patched internal APIs.

**Status:** Version-sensitive patch project.

**Next steps:** List each patch and target class/method, add apply/rebase instructions and record Minecraft/Forge/JDK compatibility.

#### [`MEStatueGenerator`](IdeaProjects/MEStatueGenerator/Summary.md)

**Purpose:** Bukkit plugin for creating ModelEngine-backed statues/posed model entities in-game.

**Implementation:** The `cs` command spawns an ArmorStand, wraps it in Te4ModelledEntity, attaches an active model and adds it to a parent player. A timed path uses unsafe Thread.stop().

**Status:** Concrete game-server feature with legacy thread handling.

**Next steps:** Document command/model/state args and dependencies; replace Thread.stop() with cancellable, server-thread-safe scheduling.

#### [`Mimick0`](IdeaProjects/Mimick0/Summary.md)

**Purpose:** Tiny Java prototype with one main source class and little supporting structure.

**Implementation:** The footprint is too small to infer a stable API or finished feature.

**Status:** Prototype/likely unfinished.

**Next steps:** Describe actual mimicry/compatibility behavior, add a reproducible example, or archive if obsolete.

#### [`MM2patches`](IdeaProjects/MM2patches/Summary.md)

**Purpose:** Small Java patch project with one main source class; target and changed behavior are not confirmed by the name.

**Implementation:** Patch compatibility depends on exact upstream product/version.

**Status:** Target-specific patch prototype.

**Next steps:** Document target/version, exact changes, apply/revert steps and regression tests.

#### [`MPCKit`](IdeaProjects/MPCKit/Summary.md)

**Purpose:** Small Java project with a README and few source files; acronym is ambiguous without documentation.

**Implementation:** Do not expand MPC based only on folder name; use README/API.

**Status:** Small library/utility; scope needs confirmation.

**Next steps:** State purpose, show one usage example, document dependencies and test the primary operation.

#### [`MusicEngine2`](IdeaProjects/MusicEngine2/Summary.md)

**Purpose:** Bukkit plugin for controlling music/sound behavior through commands, events and WorldGuard regions.

**Implementation:** On load it registers a WorldGuard sound-id flag. On enable it loads config, optionally registers commands/events, loads music categories and registers a WorldGuard session handler/provider.

**Status:** Substantial plugin with WorldGuard/Bukkit integration.

**Next steps:** Document config, flags/region semantics, sound categories, permissions and compatible WorldGuard/Spigot versions.

#### [`NLaunch`](IdeaProjects/NLaunch/Summary.md)

**Purpose:** Java class/resource-loader experiment that routes loading through native code; related to `GolandProjects/jnigga`.

**Implementation:** Critical contracts include bootstrap, class delegation, resource lookup, native initialization and classpath behavior; this is low-level JVM infrastructure rather than merely an end-user launcher.

**Status:** Experimental runtime component.

**Next steps:** Document native builds per OS/architecture, supported JVM versions and tests for classes/resources/missing entries.

#### [`NoCmd`](IdeaProjects/NoCmd/Summary.md)

**Purpose:** Project with one primary Java class and a large non-source artifact footprint; behavior is not established by the name alone.

**Implementation:** Binaries may be dependencies, generated outputs or input files and should not be deleted without tracing their role.

**Status:** Minimal source, artifact-heavy.

**Next steps:** Identify main behavior, classify every binary and separate dependencies from generated build output.

#### [`PatchedModelEngine`](IdeaProjects/PatchedModelEngine/Summary.md)

**Purpose:** Small Java project intended for patches or compatibility changes for a model engine.

**Implementation:** Exact target version and modified methods must be read from code/build dependencies.

**Status:** Target-specific patch project.

**Next steps:** Record upstream artifact/version, changes, application steps and regression tests.

#### [`PathAuth`](IdeaProjects/PathAuth/Summary.md)

**Purpose:** Java project with several classes and an ambiguous name; actual security/authentication model is not established by inventory.

**Implementation:** It may concern path authorization, routing or another local convention; do not treat it as a security library without review.

**Status:** Security-sensitive purpose needs verification.

**Next steps:** Document trust boundaries and allow/deny rules; test normalization, traversal and symlink handling before reuse.

#### [`PathFilter`](IdeaProjects/PathFilter/Summary.md)

**Purpose:** Small Java project apparently concerned with filtering paths/resources.

**Implementation:** Path filters need explicit rules for normalization, separators, traversal and symlink resolution.

**Status:** Small utility; exact behavior needs verification.

**Next steps:** Specify matching syntax/case sensitivity and test absolute paths, `..`, separators, symlinks and empty input.

#### [`RandomChest`](IdeaProjects/RandomChest/Summary.md)

**Purpose:** Gradle/resource directory with no Java source files detected in the recursive tree.

**Implementation:** Despite the name suggesting randomized chest loot, no Java implementation is visible in the imported tree.

**Status:** Empty/incomplete as currently committed.

**Next steps:** Mark intentionally empty or restore missing sources; if generated, document the generator and expected output.

#### [`RandomLootBox`](IdeaProjects/RandomLootBox/Summary.md)

**Purpose:** Bukkit plugin implementing randomized inventory/reward behavior with configurable settings and messages.

**Implementation:** The main plugin initializes a ProtocolLib coder, settings/message configs, timer, random source, inventory helper and manager/event listener. Reloading recreates timer/config state and imports inventory state.

**Status:** Featureful server plugin dependent on ProtocolLib and Bukkit-compatible APIs.

**Next steps:** Document commands, reward rules, settings/messages, persistence and versions; test timer shutdown/reload and reward edge cases.

#### [`Redirect_player`](IdeaProjects/Redirect_player/Summary.md)

**Purpose:** Java project with many classes apparently implementing player redirection in a game-server context.

**Implementation:** Exact trigger and destination model are not confirmed by the directory name.

**Status:** Plugin-sized project without a precise feature description.

**Next steps:** Document source/destination, trigger rules, permissions/config, failure behavior and tested server/proxy versions.

#### [`RegionEX`](IdeaProjects/RegionEX/Summary.md)

**Purpose:** Java project with many classes focused on extending region functionality in a game-server environment.

**Implementation:** May overlap with `te4_regionEX`, but equivalence is not established.

**Status:** Substantial plugin; possible duplication.

**Next steps:** Document commands/API/storage and compare with te4_regionEX; define which implementation is maintained.

#### [`REPO`](IdeaProjects/REPO/Summary.md)

**Purpose:** Directory with only a couple of files and no obvious source tree or build configuration.

**Implementation:** There is not enough material to identify a runnable product or library.

**Status:** Likely placeholder/leftover.

**Next steps:** Identify purpose, then add a real source/build setup or archive/remove deliberately.

#### [`SolangEngine`](IdeaProjects/SolangEngine/Summary.md)

**Purpose:** Java script runtime built around `AppScriptEngine`, loading scripts from a configurable path and exposing modules such as `User`.

**Implementation:** Main defaults to `src/main/asol_example`, starts the engine and logs failures. This appears to be a custom scripting environment rather than a general-purpose game engine.

**Status:** Substantial engine prototype.

**Next steps:** Document script syntax, modules/security boundaries, execution lifecycle, standard library and error reporting; add parser/runtime tests.

#### [`spShip`](IdeaProjects/spShip/Summary.md)

**Purpose:** Java/Bukkit project with a reusable YandeRePlugin base for commands, configuration and recursive reloadable components; ship-specific gameplay needs deeper review.

**Implementation:** The base class manages configs, command/subcommand trees and Reloadable lifecycle. Exact ship features should not be guessed from the folder name.

**Status:** Plugin-sized codebase with custom command/config infrastructure.

**Next steps:** Document user-facing ship features separately from framework internals, required dependencies and lifecycle/reload tests.

#### [`T-Chat Reload`](IdeaProjects/T-Chat Reload/Summary.md)

**Purpose:** Java/Gradle project apparently implementing a server chat plugin.

**Implementation:** The tree contains a moderate number of Java files; exact message pipeline and server API need verification.

**Status:** Plugin-sized but under-documented.

**Next steps:** Document formatting, event priorities, commands, permissions, reload and interactions with other chat plugins.

#### [`TCord`](IdeaProjects/TCord/Summary.md)

**Purpose:** Distributed large-text translation tool with a Java server and Python worker clients.

**Implementation:** README says run `TCord.jar` on Java 22+ with source/result JSON files; Python 3.9–3.11 clients install requirements and connect to `host:port`. Server distributes translation work among clients; examples live under `run/`.

**Status:** Multi-process tool with a useful README; server/client versions must align.

**Next steps:** Document wire protocol, scheduling/retries, JSON schemas, trust boundaries and failed-job reporting.

#### [`te4_async_utls`](IdeaProjects/te4_async_utls/Summary.md)

**Purpose:** Java utility module focused on asynchronous operations in the TE4 ecosystem.

**Implementation:** Moderate Java source set; exact executor/task APIs should be listed from public types.

**Status:** Reusable utility candidate; thread-safety/cancellation need explicit documentation.

**Next steps:** Document each primitive, executor ownership, error propagation, cancellation and shutdown semantics; add concurrency tests.

#### [`te4_blocks`](IdeaProjects/te4_blocks/Summary.md)

**Purpose:** Java game-content module adding blocks/resources to the TE4 ecosystem.

**Implementation:** Contains registration logic and bundled assets; it is a module, not a standalone application.

**Status:** Content module coupled to matching game/mod version.

**Next steps:** List added blocks/resource IDs, registration points, dependencies, asset sources and supported version.

#### [`te4_dungeons`](IdeaProjects/te4_dungeons/Summary.md)

**Purpose:** Java content/generation module for dungeons or generated game locations in the TE4 ecosystem.

**Implementation:** Integration points are world-generation hooks, registration and dependencies on related TE4 modules.

**Status:** Game-content module; not independently runnable.

**Next steps:** Document generation triggers/config/content and add deterministic seed-based smoke tests.

#### [`te4_forgeserver`](IdeaProjects/te4_forgeserver/Summary.md)

**Purpose:** Large Java/Forge project with hundreds of Java sources and an existing README.

**Implementation:** Belongs to Minecraft/Forge modding; exact supported versions must come from build metadata.

**Status:** Major project; reproducibility depends on explicit version/build instructions.

**Next steps:** Document Minecraft/Forge/JDK versions, Gradle tasks, setup, client/server launch configs and external dependencies.

#### [`te4_inventory`](IdeaProjects/te4_inventory/Summary.md)

**Purpose:** Java module for inventory functionality within the TE4/game ecosystem.

**Implementation:** Should be treated as a reusable module/plugin component; responsibility overlap with te4_mc_invlib needs clarification.

**Status:** Library/module with unclear responsibility boundary.

**Next steps:** List public abstractions, serialization/storage behavior, game API versions and dependency relationship to te4_mc_invlib.

#### [`te4_mc_invlib`](IdeaProjects/te4_mc_invlib/Summary.md)

**Purpose:** Java library for Minecraft inventory-related types or operations.

**Implementation:** Substantial Java source tree appears intended for use by other modules.

**Status:** Library candidate; API compatibility must be stated.

**Next steps:** Provide import/use example, supported API versions and tests for slot bounds, empty stacks and serialization.

#### [`te4_plugin_system`](IdeaProjects/te4_plugin_system/Summary.md)

**Purpose:** Java plugin runtime loading JARs through a custom class loader, reading `plugin.tson`, ordering by priority and calling lifecycle hooks.

**Implementation:** This is a plugin framework, not a security sandbox: loading arbitrary JARs executes code in-process.

**Status:** Experimental runtime framework; trust boundaries matter.

**Next steps:** Document descriptor schema, lifecycle, dependencies/classloader rules and tests for missing dependencies, duplicate IDs and startup failure.

#### [`te4_regionEX`](IdeaProjects/te4_regionEX/Summary.md)

**Purpose:** Java region-extension module in the TE4 ecosystem.

**Implementation:** Its relationship to `IdeaProjects/RegionEX` is unresolved; compare entry points, APIs and history before treating them as equivalent.

**Status:** Possible fork/module/duplicate.

**Next steps:** Explain differences and compatibility, and tell users which implementation to depend on.

#### [`te4_spaceworlds`](IdeaProjects/te4_spaceworlds/Summary.md)

**Purpose:** Large Java content module apparently adding space-themed worlds/dimensions and related resources.

**Implementation:** Content-heavy module likely loaded with a specific TE4/Forge base and related modules.

**Status:** Large game-content project; exact world inventory needs verification.

**Next steps:** Document dimensions/biomes/structures, generation rules, asset provenance, dependencies and supported versions.

#### [`te4_spships`](IdeaProjects/te4_spships/Summary.md)

**Purpose:** Large Java content module apparently providing ship-related content or mechanics for the TE4/space-world ecosystem.

**Implementation:** Contains many Java classes and assets; dependency relationship with te4_spaceworlds should be explicit.

**Status:** Large content-heavy module.

**Next steps:** List ship classes/behaviors, required modules/assets and supported versions; verify loading order in an integration run.

#### [`Te4hno-s Patvhes`](IdeaProjects/Te4hno-s Patvhes/Summary.md)

**Purpose:** Small Java/Gradle patch project whose name suggests changes for TE4hno components.

**Implementation:** Target components and compatible upstream versions are not clear from the directory name.

**Status:** Patch collection needing a change list.

**Next steps:** Enumerate each patch, affected class/method/version and reproducible apply/revert procedure.

#### [`te4mobs`](IdeaProjects/te4mobs/Summary.md)

**Purpose:** Java content module apparently adding mobs/entities to the TE4 ecosystem.

**Implementation:** Likely entity registration/AI/resources; exact list should be generated from the classes rather than guessed.

**Status:** Content module coupled to target game runtime.

**Next steps:** Publish mob list, behaviors/drops/spawn rules, assets and dependencies; test entity registration.

#### [`Te4Motd`](IdeaProjects/Te4Motd/Summary.md)

**Purpose:** Java server plugin likely providing a message-of-the-day feature.

**Implementation:** Exact formatting, placeholders, permissions and config should be taken from its descriptor and source.

**Status:** Small server plugin.

**Next steps:** Document config keys, placeholders, colors, permissions and malformed-config fallback; test supported versions.

#### [`te4net`](IdeaProjects/te4net/Summary.md)

**Purpose:** Java server infrastructure launched by `Main`, which parses args and creates `HServer`; includes HTTP routing/API, user/auth data, email and Minecraft-related services.

**Implementation:** Reviewed auth/data paths include SQL string concatenation and plaintext password comparisons. Root build setup was not obvious in the inventory.

**Status:** Substantial backend with security and deployment documentation gaps.

**Next steps:** Parameterize SQL, use salted password hashes, document endpoints/sessions/secrets, establish reproducible build and add auth/API integration tests.

#### [`TestCache`](IdeaProjects/TestCache/Summary.md)

**Purpose:** Java test/experiment classes apparently focused on cache behavior.

**Implementation:** Not enough evidence to treat it as a production cache library; the specific strategy/property should be named.

**Status:** Test/experiment until confirmed.

**Next steps:** Document cache model and invalidation/expiration expectation; add deterministic tests rather than timing-only checks.

#### [`TestFSX`](IdeaProjects/TestFSX/Summary.md)

**Purpose:** Small project with few source files and Gradle scaffolding, apparently experimenting with filesystem behavior.

**Implementation:** No confirmed standalone API or usage guide has been established.

**Status:** Experiment.

**Next steps:** Document tested filesystem behavior, expected result, OS assumptions and cleanup; add reproducible command.

#### [`TestJ22`](IdeaProjects/TestJ22/Summary.md)

**Purpose:** Minimal Java test project with one Java class and no obvious build setup in the inventory.

**Implementation:** The name suggests a Java 22 test, but the precise runtime feature is unconfirmed.

**Status:** Test stub.

**Next steps:** State JDK version and behavior under test, add compile/run command and expected output.

#### [`TestPFVM`](IdeaProjects/TestPFVM/Summary.md)

**Purpose:** Small Java experiment with several classes and no obvious root build setup.

**Implementation:** The acronym and tested behavior are unclear from directory metadata.

**Status:** Experiment/needs identification.

**Next steps:** Expand the test name, document inputs/expected output and add a supported build command.

#### [`TestProject2`](IdeaProjects/TestProject2/Summary.md)

**Purpose:** Gradle project with one Java class; imported Git history was unavailable.

**Implementation:** The single-class footprint resembles a test scaffold rather than a product.

**Status:** Minimal test/stub.

**Next steps:** Explain what it tests and how to run it, or archive if obsolete; avoid unneeded generated files.

#### [`TextAlignerPlaceholder`](IdeaProjects/TextAlignerPlaceholder/Summary.md)

**Purpose:** Explicit placeholder for a text-aligner project with bilingual README files and only a few Java classes.

**Implementation:** The name and small source set indicate that full text-alignment functionality should not be assumed to exist.

**Status:** Placeholder, not finished implementation.

**Next steps:** List planned versus implemented alignment modes and examples, or clearly label as an empty scaffold.

#### [`ThisBeautyWorld`](IdeaProjects/ThisBeautyWorld/Summary.md)

**Purpose:** Procedural world/map-generation experiment with a matrix-based world model and JMonkeyEngine 3D inspection application.

**Implementation:** Code references GlobalMapCGen, IslandCGen and RiverMixCGen; a CLI generates a matrix and writes a textual render, while a JME app tests terrain rendering/collisions and a custom TSON asset loader. Asset-heavy tree.

**Status:** Large prototype; generation and visual test app should be documented separately.

**Next steps:** Document map dimensions/seeds/resolution, generation stages, assets, and how to run CLI versus viewer; add deterministic tests.

#### [`TimidBlocks`](IdeaProjects/TimidBlocks/Summary.md)

**Purpose:** Client/server game feature where spoken words cause a block to disappear; includes Fabric client mod, server plugin variants and audio library.

**Implementation:** README specifies Fabric 1.21.8 with loader 0.16.1+1.21.8 for client and tested server support 1.16.5–1.21.8. `config.tson` sets model-dir, speech buffers, audio rate and phrase-to-material mappings; microphone needs 22050 Hz/16-bit and server uses Vosk models.

**Status:** Multi-module project with external audio/model requirements and several variants (`AudioLib`, `TimidBlocks-SP`, `ALLE2`).

**Next steps:** Document canonical modules, Vosk model setup/licensing, client/server audio synchronization and build/run per version.

#### [`TJoinControl`](IdeaProjects/TJoinControl/Summary.md)

**Purpose:** Small Java/Gradle server plugin apparently controlling player join behavior.

**Implementation:** Current files have no imported commit history and intended behavior is not clear from the name; it could change messages, gates or redirection.

**Status:** Small plugin needing confirmation.

**Next steps:** Document join-event behavior, permissions/config and server versions; test first join, reconnect and rejected joins.

#### [`tson_lang`](IdeaProjects/tson_lang/Summary.md)

**Purpose:** IntelliJ Platform plugin adding TSON syntax support through a custom lexer/editor integration.

**Implementation:** TsonLexer.reset() ignores `start`, `end` and `initialState`, which can break incremental lexing/highlighting and non-zero offsets.

**Status:** IDE plugin with a concrete lexer correctness issue.

**Next steps:** Honor all reset parameters, test partial buffers/incremental edits and document IntelliJ Platform compatibility.

#### [`TsonConfigurations`](IdeaProjects/TsonConfigurations/Summary.md)

**Purpose:** Java parser/model library for TSON, STSON and JSON-like input; supports primitives, maps, lists, class references, object construction and serialization.

**Implementation:** TsonObj is the base abstraction; TsonMap/TsonList represent collections; TsonField can construct classes; TJsonParser handles JSON. ClassManager controls which classes may be created. STSON can execute calls/expressions, so it is more than passive config.

**Status:** Core library with an IntelliJ syntax plugin and consumer projects; reflective/executable features have security implications.

**Next steps:** Document grammar/escaping/serialization, add parser round-trip/error tests and use restrictive class allowlists for reflective construction/expression execution.

#### [`TsonCord`](IdeaProjects/TsonCord/Summary.md)

**Purpose:** Java Discord bot using JDA and a TSON-based plugin/config builder.

**Implementation:** Main constructs JDATsonBuilder with `dsplugins`, delegating behavior to the builder/framework. Real Discord credentials must be injected outside source control.

**Status:** Bot with custom config integration.

**Next steps:** Document intents/permissions, plugin discovery, config schema and deployment; keep tokens out of repository and test invalid config.

#### [`TsonMessages`](IdeaProjects/TsonMessages/Summary.md)

**Purpose:** Small Java project associated with messaging in the TSON ecosystem; exact public API and consumers are not established by inventory.

**Implementation:** Do not assume it is a localization system, transport or template engine based on its name alone.

**Status:** Needs source-level description.

**Next steps:** Document message representation, formatting/parsing, fallback rules, consumers and a minimal example.

#### [`TsonSpy`](IdeaProjects/TsonSpy/Summary.md)

**Purpose:** Bukkit/Spigot base plugin that loads TSON config and installs a Java Scripting API engine, preferring configured engine and falling back to Groovy JSR-223.

**Implementation:** TsonPlugin wires metrics and TSON event/listener utilities; config errors fall back to an empty map, so diagnostics and script trust boundaries matter.

**Status:** Experimental scriptable server plugin; scripts may have powerful in-process access.

**Next steps:** Document script API/events/engine versions and permissions; restrict script sources, improve errors and test engine fallback.

### `LAunch/`

#### [`LAunch`](LAunch/LAunch/Summary.md)

**Purpose:** Android/Java launcher application organized into `app` and `core` Gradle modules.

**Implementation:** The Android UI/lifecycle belongs in app and shared logic in core; supported games/services and auth/update flows must be verified from code.

**Status:** Large Android app; not built or run as part of this inventory.

**Next steps:** Document target services, auth/download/update flow, JDK/Gradle/Android SDK versions and emulator/device smoke test.

### `VisualStudioRepos/`

#### [`CXFreezePlus`](VisualStudioRepos/CXFreezePlus/Summary.md)

**Purpose:** C# project with a large tree; exact relationship to cx_Freeze/Python packaging cannot be established from directory name alone.

**Implementation:** No obvious root README in the imported tree; it could be a wrapper, fork or separate utility.

**Status:** Purpose requires code-level identification.

**Next steps:** Identify entry point and target .NET/Python versions, explain upstream relationship and document build/package steps.

#### [`FTemplate`](VisualStudioRepos/FTemplate/Summary.md)

**Purpose:** C# project with dozens of source files; name suggests a template system but this remains unverified.

**Implementation:** No obvious root README; document from namespaces, public classes, sample inputs and output behavior rather than guessing from name.

**Status:** Substantial enough to need an API/architecture summary.

**Next steps:** Identify core abstraction and syntax, intended consumers, .NET target, example and tests.

#### [`GF2_LaunchHX`](VisualStudioRepos/GF2_LaunchHX/Summary.md)

**Purpose:** Very large C# application associated with a GF2 launcher; thousands of files may include generated/vendor/content files.

**Implementation:** Supported game version, login/update/download flow and authored-vs-generated assets are not established by folder name; file count is not authored-code size.

**Status:** Major application with documentation/repository-hygiene needs.

**Next steps:** Write component map, document auth/patch/update, SDKs and build/package steps; classify binaries/generated assets.

#### [`GF2_RU_Launch`](VisualStudioRepos/GF2_RU_Launch/Summary.md)

**Purpose:** Small C# project apparently associated with a Russian-language GF2 launcher.

**Implementation:** Relationship to GF2_LaunchHX is unclear: localization, older variant or independent app.

**Status:** Small app with likely overlap to resolve.

**Next steps:** Document differences, game versions and config compatibility; identify maintained variant versus legacy copy.

#### [`LUpdater`](VisualStudioRepos/LUpdater/Summary.md)

**Purpose:** Small C# project with README, apparently an updater for a launcher.

**Implementation:** An updater's critical behavior is version checking, downloading, validation and safe replacement; exact protocol must be taken from source/README.

**Status:** Small utility; update integrity is important.

**Next steps:** Document manifest format, hash/signature checks, atomic replacement/rollback, runtime and tests for interrupted/corrupt downloads.

## Git history

Available histories from the original repositories are connected to `main` through a single multi-parent integration commit. This preserves independent histories in the Git commit graph without a separate integration commit for every source branch. Project trees were relocated beneath collection paths during import, so rewritten commit IDs differ from original IDs.

Some projects had Git metadata but no reachable commits:

- `GolandProjects/vwlaunch`
- `IdeaProjects/te4_async_utls`
- `IdeaProjects/TestProject2`
- `IdeaProjects/TJoinControl`

Only their available current files could be preserved; there were no existing commits to import. Original version tags are retained under `history/<project-path>/<tag>` to avoid collisions. These are tags, not additional working branches.

## Repository limitations

This collection combines unrelated technologies and project generations. Some directories retain binaries, generated resources, machine-specific paths, incomplete prototypes or missing build metadata. Do not assume that a project is actively maintained, safe to execute or reproducible from source until its own build files and dependencies have been reviewed.
