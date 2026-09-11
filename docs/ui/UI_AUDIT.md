# Matter Overdrive UI/UX Audit

Status: active repository-wide audit (started 2026-09-10)

## Inventory baseline

Static inventory found 32 client screen classes, 24 menu classes, and 41 client files containing GUI/HUD/overlay rendering or interaction code. `scripts/validate_ui_inventory.py` now verifies that every discovered screen and menu name appears in this audit document and is included in `tools/full-sanity-check.ps1`. The working tree contains unrelated user changes and imported Destiny assets; these are preserved.

### Android / progression

| Screen | Purpose | Initial static risk | Severity | Status |
|---|---|---|---|---|
| `AndroidClassLoadoutScreen` | class/build selection | compact layout and inspection-panel scaling | HIGH | responsive compact layout repaired; runtime screenshot still required |
| `AndroidLoadoutScreen` | active loadout | compact panel/input geometry | HIGH | responsive compact layout repaired; runtime screenshot still required |
| `AndroidSkillTreeScreen` | perk progression | dense node geometry, scroll/hit-test risk | HIGH/OVERHAUL candidate | responsive node spacing and clamped inspection/progress geometry repaired; runtime review pending |
| `AndroidStationScreen` | chassis/modules | many manual bounds and slot coordinates | HIGH | dynamic header/instruction text fitted; runtime screenshot still required |
| `AndroidSpawnerScreen` | android spawning | fixed-size layout and action feedback | MEDIUM | dynamic control/status text fitted; runtime screenshot still required |
| `DroneManagementScreen` | drone controls | interaction/state feedback review required | HIGH | responsive controls, bounded rows, server command routing, and empty-state feedback statically verified; runtime interaction review still required |

### Machines / infrastructure

The machine-screen pixel-boundary pass is complete for the listed screens: `ChargingStationScreen`, `DroneFabricatorScreen`, `DecomposerScreen`, `EnergyPipeScreen`, `InscriberScreen`, `MatterAnalyzerScreen`, `MatterRecyclerScreen`, `MicrowaveScreen`, `NetworkRouterScreen`, `NetworkSwitchScreen`, `PatternMonitorScreen`, `PatternStorageScreen`, `PylonScreen`, `ReplicatorScreen`, `SolarPanelScreen`, `SpacetimeAcceleratorScreen`, `TransporterScreen`, `TritaniumCrateScreen`, and `ContractMarketScreen`. Runtime screenshots remain the evidence gap for visual confirmation.

The weapon-machine surface is `WeaponStationScreen` with `WeaponStationMenu` and is included in the same geometry/state review group.

### Menu inventory

The container/menu layer paired with the screens is: `AndroidSpawnerMenu`, `AndroidStationMenu`, `ChargingStationMenu`, `DroneFabricatorMenu`, `ContractMarketMenu`, `DecomposerMenu`, `EnergyPipeMenu`, `FusionReactorMenu`, `GravitationalStabilizerMenu`, `InscriberMenu`, `MatterAnalyzerMenu`, `MatterRecyclerMenu`, `MicrowaveMenu`, `NetworkRouterMenu`, `NetworkSwitchMenu`, `PatternMonitorMenu`, `PatternStorageMenu`, `PylonMenu`, `ReplicatorMenu`, `SolarPanelMenu`, `SpacetimeAcceleratorMenu`, `TransporterMenu`, `TritaniumCrateMenu`, and `WeaponStationMenu`.

Initial finding: machine screens use a shared `MachineScreenStyle`, but many still duplicate fixed `leftPos + offset` geometry between button creation and rendering. This is a medium/high maintainability and hitbox-drift risk; the highest-priority machine pass will compare those pairs and consolidate only where justified.

### Reactor / anomaly

`FusionReactorScreen`, `GravitationalStabilizerScreen`, and `ReactorAssemblyGuideScreen` require dashboard hierarchy, state/error feedback, and responsive-bound review.

### Guide / documentation / miscellaneous

`DocumentationScreen`, `DataPadScreen`, and `NpcDialogueScreen` require wrapping, navigation, density, and tooltip-bound review. Guide assets and GuideME integration require resource-path and stale-content checks.

### HUD / overlays / rendering

`AndroidHudOverlay`, `QuestTrackerOverlay`, `WeaponHudOverlay`, `FusionReactorGuideOverlay`, `DeveloperVisualDebug`, `NativeDestinyWeaponRenderer`, and `WeaponItemRenderer` require screen-edge, GUI-scale, render-order, and interaction-overlap review.

## Initial classifications

- HIGH: Android skill-tree/build surfaces until node bounds, scrolling, and selection feedback are verified.
- HIGH: Android and drone HUD surfaces if they overlap the crosshair/hotbar or persist beyond useful moment-to-moment state.
- HIGH: any machine control whose rendered control bounds differ from its widget bounds.
- MEDIUM: duplicated machine layout constants and inconsistent status presentation.
- MEDIUM: guide and data-pad density/navigation until all page wrapping and bounds are checked.

## Completed repairs

### Android skill tree — responsive geometry pass

The skill tree now uses shared local geometry methods for node origin, spacing, and row placement in both widget creation and rendering. Bounds are clamped for narrow windows, capstone spacing remains derived from the same step value, the inspection panel cannot start off the left edge, and the XP bar uses a non-negative drawable width. Static compilation passed; runtime checks at multiple GUI scales remain required.

### Network router — diagnostic text bounds

The router’s variable-length routing and graph diagnostics are now clipped to the diagnostic panel width before drawing. This prevents long endpoint/guard/stall summaries from bleeding into adjacent UI or beyond the frame. Static compilation passed; runtime review should confirm the shortened text remains understandable at the normal GUI scale.

## Working protocol

For each UI group, record purpose, defects, severity, design decision, implementation status, static validation, and runtime checks. Preserve server authority and persistence while changing client presentation. Build and run the project sanity checks after coherent implementation groups.

## Static geometry validation

`scripts/validate_ui_geometry.py` now checks literal `leftPos + x, topPos + y, width, height` widget bounds against each container screen’s declared frame. It is included in `tools/full-sanity-check.ps1`. Dynamic expressions and standalone `Screen` layouts remain explicitly marked for separate review rather than being treated as proven by this conservative check.

Latest static result: 31 literal bounds checked successfully. Latest full Gradle build passed, and the resulting JAR was deployed to the configured test `mods` directory; interactive screenshot evidence remains required for the Fabricator.

### HUD / overlay — narrow-window safety

`AndroidHudOverlay` and `WeaponHudOverlay` now clamp their panel widths and screen anchors for small GUI dimensions. Variable Android ability/loadout text, weapon names, and weapon footer instructions are fitted with ellipses so they remain inside their panels. Full Gradle build passed after this change; the deployed JAR hash is `94054B054056480D356683C2C1FECDDC63FE8FEF18A03E1A1F4A5E39EEF16EF3` and the Minecraft test instance was restarted. Runtime checks should still cover simultaneous overlays and GUI-scale changes.

### Documentation index — responsive columns

The documentation index previously used fixed 265-pixel buttons at fixed offsets, which could place controls outside the screen on narrow windows. Index buttons now derive their two-column positions and widths from the available panel width, keeping the rendered index and clickable regions inside the same panel.

### NPC dialogue — responsive footer controls

Dialogue Back/Continue controls previously used fixed right-edge coordinates and 92-pixel widths. They now derive their widths, gap, and right anchor from the available screen width, preventing the footer controls from leaving the viewport on narrow windows.

### Fusion reactor — diagnostic text bounds

Dynamic anomaly, feed, suppression, and hazard lines now fit within the reactor diagnostic panel before drawing. This preserves the dashboard’s primary status hierarchy and prevents long numeric telemetry strings from bleeding into the frame edge.

Latest validation for this reactor pass: full Gradle build passed, the previous Minecraft test process was confirmed absent before launch, the new JAR was deployed with SHA-256 `ABCA55811755F0B4DAC128A3785FCDE24A7BC48CF30708DA94F633BC42DCCAD2`, and a fresh test session was started.

### UI resource references — modern constructors

Documentation and shared machine GUI texture references now use the non-deprecated namespace/path factory. This removes avoidable client build warnings while preserving the existing resource IDs.

The same cleanup was extended to `MatterAnalyzerScreen` and `ReactorAssemblyGuideScreen`. The resulting build still reports four unrelated Forge render-layer deprecations in `ClientModEvents`; those are outside this UI resource-reference repair and remain tracked for a separate compatibility pass.

### Gravitational stabilizer — right-panel text bounds

Stabilizer status, beam telemetry, upgrade guidance, and the dynamic left-side status are now fitted to their available panel widths. This prevents long redstone, beam, anomaly, and upgrade strings from crossing the panel boundary while retaining actionable state feedback.

Latest resource pass: the Matter Overdrive test client was closed by verified project-process identity before building, the build completed, the JAR was deployed, and a fresh test client was launched. Deployed SHA-256: `3832AFD812815149797DB82E20570FC8C717B07DE5CF0C4B1073BAC218E3DEB7`.

The lingering visible `RUN_M2_CLIENT.bat` command shell was also identified by its exact command line and closed without touching unrelated Codex/plugin command processes.

## Runtime evidence follow-up

The earlier launcher-side `latest.log` contained stale `star_map` and Vex sound warnings from an older artifact/profile. A clean build and fresh dev-session log were correlated to the current artifact; those Matter Overdrive resource and JEI-plugin errors no longer occur. The remaining external JEI `bakedsubstring` error is documented separately below.

### Data Pad — pixel-width text fitting

Contract titles, objectives, progress summaries, and the page footer now use font-width fitting instead of character-count trimming. This keeps variable contract text inside the responsive Data Pad panel at reduced GUI widths while preserving the existing two-click abandon safety flow.

Latest Data Pad validation: the identified Matter Overdrive test client was closed before build, full Gradle build passed, the JAR was deployed, and a fresh test client was launched with SHA-256 `43B925B8F13266983517F0FA8239F03E17A5A250F49F74E5C9E020F570BF2E00`.

## Local LLM audit

Qwen 7B/Aider was invoked for the required bounded repository-local inventory with an explicit read-only request. The initial no-file attempt returned no findings, so a second bounded run supplied the five Android screen files directly. That run identified fixed-coordinate scaling risks; independent inspection confirmed the compact-layout issue and the resulting repair. A larger machine-screen delegation exceeded the local model context limit and was not treated as evidence.
### Android loadout — responsive panel contract

Below 640 GUI pixels, `AndroidLoadoutScreen` now suppresses the secondary inspection panel and expands the primary build area to the available width. This avoids the previous guaranteed overlap caused by reserving a 286-pixel panel alongside a 430-pixel minimum main area. The normal wide layout and inspection experience remain unchanged; narrow-layout runtime screenshots are still required.

The bounded local Qwen 7B audit identified fixed-coordinate risks across the Android screens. Independent inspection confirmed the same failure mode in `AndroidClassLoadoutScreen`; its compact layout now suppresses the diagnostic panel entirely and clamps the close control so minimum-width geometry cannot force controls off-screen.

Machine-screen follow-up: `WeaponStationScreen` now pixel-fits weapon names, heat status, and readiness messaging to the 180-pixel status panel, preventing long localized or Destiny weapon labels from bleeding into the panel edge.

Shared helper: machine screens can now use `MachineScreenStyle.fit(Font, text, width)` for consistent pixel-based clipping and ellipsis behavior.

Guide navigation validation: added `scripts/validate_guide_links.py` and wired it into `tools/full-sanity-check.ps1`; it validates local link targets in all 16 bundled GuideME Markdown pages and rejects path escape or missing-file references.

GuideME compatibility now uses the modern namespaced `ResourceLocation` factory for the manual and start page identifiers, removing a client-side deprecation in the optional guide bridge.

Documentation navigation now derives the Previous/Index/Next row from available width, preventing the Previous button from leaving the viewport at narrow GUI scales.

Client renderer audit: native Destiny visual-library resources and the developer model inspector now use the modern namespaced resource factory consistently, avoiding legacy client resource construction during model/texture lookup.

Contract market follow-up: its instructional message now uses shared pixel fitting to stay inside the 120-pixel board column instead of crossing into the live status panel.

Tooltip audit: the legacy hint subscriber now defers to `MatterValueTooltipEvents` whenever the UI-safe registry resolves a value, eliminating duplicate matter lines while retaining the SHIFT hint for unresolved items.

HUD audit: `QuestTrackerOverlay` now filters `RenderGuiOverlayEvent.Post` to the vanilla hotbar pass, preventing duplicate tracker redraws caused by receiving every overlay phase.

Debug overlay audit: developer model item IDs and render contexts are now pixel-fitted to the capped diagnostic panel, preventing long resource identifiers from escaping the debug HUD.

Android Station audit: offline/status text and the selected core ability label now use pixel fitting within their fixed header regions, preventing progression and ability text from bleeding into the frame.

Android Station follow-up: the body-system instruction and hardware-removal guidance now use pixel fitting within the 272-pixel screen, keeping the progression instructions inside the frame at small GUI scales.

Android Spawner and Recycler follow-up: dynamic squad labels, mode/color values, queue/status values, and recycler yield/demand text now use pixel-width fitting; the Spawner control row no longer allows its two dynamic labels to collide.

Charging Station follow-up: battery, transfer, buffer, range, Android-count, and charge-rate values now use pixel fitting inside the station’s narrow status columns.

Network Router follow-up: filter, throughput, routing-core, and endpoint diagnostics now fit the 88/112-pixel panels; long route labels no longer bleed into adjacent UI.

Fresh dev-session runtime evidence (10 September 2026 23:09): Matter Overdrive initialized with 14 native Destiny weapons, 18 block entities, 15 menus, and all audited machine, reactor, network, Android, documentation, and structure systems enabled. No Matter Overdrive UI/resource exceptions were present in the session tail. Remaining warnings were external or non-blocking (Forge language-loader metadata, tiny texture mip limits, vanilla goat-horn events, and Realms authorization).

Reactor guide overlay audit: enabled-controller positions are now cleared whenever the client level changes, preventing stale static overlay state from carrying into a new world/session.

Client/server audit: the reactor guide overlay subscriber is explicitly restricted to `Dist.CLIENT`, matching its client-only render event and preventing dedicated-server classloading of rendering code.

Client-boundary validation: added `scripts/validate_client_boundaries.py` to the full sanity workflow; it rejects GUI/render/input event subscribers that omit `Dist.CLIENT`.

Structure evidence review: `scripts/audit_structures.py` independently reports all 12 registered native structures and 5 compact generated systems with zero errors or warnings; the full sanity workflow also confirms 18 player-scale layouts and traversal/topology guards.

Artifact validation: added `scripts/validate_clean_artifact.py` to inspect the built JAR and reject retired Star Map guide/loot resources or stale `star_map` references in the compiled JEI plugin.

Build hygiene: remaining shared sound, network, and restoration resource IDs now use the modern namespaced factory, reducing deprecation noise during client/UI verification.

Runtime review of the clean dev session found no Matter Overdrive resource, GuideME, or JEI-plugin errors. The only remaining startup error is external JEI environment configuration: JEI 15.49.0 cannot load `net.mezzdev.bakedsubstring.BakedSubstringIndex` because that dependency is absent from the test runtime. It is not caused by Matter Overdrive UI code and remains an environment follow-up.

### Reactor assembly guide — header fitting

Localized page titles and the page indicator are now fitted to the fixed guide header widths before drawing. The wrapped guide body remains unchanged, and the existing page controls retain their behavior.

### Developer visual diagnostics — bounded panel text

The opt-in GUI geometry diagnostic now fits region names, mouse coordinates, overlap summaries, and button geometry to its capped panel width. This keeps long Matter Overdrive registry IDs readable without escaping the overlay at narrow window sizes; the model-transform diagnostic remains client-local and ephemeral.

### Android skill tree — responsive progression text

The skill-tree header, power/XP readouts, branch focus labels, inspected perk title/state, mastery totals, and prerequisite counts now fit the responsive panel widths. This prevents long progression values or localized perk names from crossing into the node graph or inspection frame at small GUI scales.

### Documentation guide — hierarchy and narrow-panel fitting

Guide parsing now checks `##` subsections before generic headings, preserving the intended visual hierarchy and spacing. Index labels, document titles, subtitle, and page indicator use pixel-width fitting against their actual panel widths, keeping navigation readable at narrow GUI scales.

### Reactor assembly guide — responsive viewport geometry

The standalone reactor guide now derives its frame, navigation buttons, illustration size, and wrapped body width from the available viewport. This prevents the fixed 360×248 layout from clipping buttons or placing the guide outside the screen at small window sizes while retaining the existing four-page assembly flow.

### Drone management — narrow command-console geometry

The drone command console now selects a two-column layout for narrow viewports, derives button widths from available space, and bounds the REFRESH/CLOSE footer buttons symmetrically. This prevents all-drone mode commands and footer controls from extending past the screen while retaining server-authoritative command/status packets.

### Documentation screen — negative-width guard

The guide and index render paths now clamp their panel width before calculating borders, columns, and centered content. This prevents invalid negative draw regions at pathological ultra-narrow GUI sizes while preserving normal and compact layouts.

### Weapon station — pixel-bounded dynamic stats

Weapon module names, effective modifiers, capacity, zoom, and barrel diagnostics now use pixel-width fitting within the 164-pixel status panel. This prevents localized or custom module labels from crossing the station frame while keeping the effective weapon snapshot and module behavior unchanged.

### Android loadout — inspection-label fitting

Dynamic specialization, ultimate, passive, and inspection headings now use pixel-width fitting within the 230-pixel Android loadout inspection panel. Long subclass or localized ability names therefore remain inside the panel without changing selection packets or progression state.

### Android class matrix — inspection-label fitting

Class, subclass, ability, ultimate, aspect, fragment, passive, and cooldown labels in the class-matrix inspection panel now use the panel’s responsive pixel width. This keeps the Android build/progression explanation readable without altering server-authoritative selection or progression behavior.

The follow-up pass also bounds H/N ability labels, drone-perk headings, and all inspection descriptions to the same responsive width instead of the previous fixed 230-pixel assumption.

### Compact machine feedback — accelerator and microwave

Spacetime accelerator and microwave task/configuration panels now pixel-fit interval, target, cycle, duration, demand, and redstone-mode values to their diagnostics columns. This standardizes dynamic machine feedback without changing menu actions or server-side processing.

The recycler task panel received the same treatment for energy, progress, cycle, duration, yield, and demand values, completing this compact task-feedback consistency pass.

### Data Pad — standalone field-console repurpose

The Data Pad no longer exposes a GuideME launch button or depends on the optional GuideME bridge. It is now explicitly a standalone field/research console for progression status, contract management, and persistent block-scan history; dedicated documentation items remain responsible for opening guide screens.

Its Previous/Next navigation and active-contract panel anchors are also derived from available width, preventing controls from colliding or leaving the viewport at narrow GUI sizes.

The standalone console now also exposes an explicit `OPEN GUIDE` button. It invokes the existing reflective GuideME bridge when the optional manual is installed, and remains inactive with a clear fallback message when GuideME is unavailable.

### Runtime evidence — current clean client session

The current project test log confirms Matter Overdrive registry initialization, all 14 native Destiny weapon visuals loading, and a successful native Destiny weapon render event. No Matter Overdrive UI/resource exception is present. JEI reports a separate missing `net.mezzdev.bakedsubstring.BakedSubstringIndex` dependency; this is an external test-environment issue and is not attributed to Matter Overdrive UI code.

The two remaining Matter Overdrive texture mip warnings were inspected directly: `vex_mythoclast_dark.png` is an intentional 4×4 pixel icon and `pattern_monitor_holo_back.png` is an intentional 17×17 pixel-art backplate. Both load successfully; the warnings reflect source dimensions versus mip configuration, not missing or invalid UI resources.

### Transporter — task and destination telemetry fitting

Transporter task progress, elapsed cycle, energy cost, cooldown, saved-location count, selection, and distance values now use shared pixel-width fitting within the right diagnostic panel. This closes the remaining dynamic-text overflow gap in the compact transport workflow without changing server-side transport behavior.

### Replicator — pattern and task telemetry fitting

Replicator energy, matter, pattern cost/progress, local task progress, and FE demand values now use shared pixel-width fitting in the left and right diagnostic panels. This prevents large synchronized values from crossing the machine frame while preserving the existing network task and replication behavior.

### Fusion Reactor — dashboard telemetry fitting

Fusion Reactor buffer/matter totals, generated output, output-bus demand, ring-bus flow, and IO/stabilizer counts now use pixel-width fitting within their dashboard columns. This prevents high-value synchronized telemetry from overlapping the reactor frame while retaining the existing reactor state and hazard diagnostics.

### Matter Analyzer — scan telemetry fitting

Matter Analyzer scan progress, pattern knowledge, and redstone mode now use shared pixel-width fitting within the task and configuration panels. This keeps synchronized scan state inside the analyzer frame without altering scan progression or server-side data.

### Solar Panel — generation telemetry fitting

Solar Panel peak generation and sky-access status now use shared pixel-width fitting within the generation panel. This keeps environmental generation feedback inside the panel at narrow GUI scales while preserving the existing daylight and FE-output logic.

### Cross-screen dynamic telemetry sweep

The broader expression scan found and repaired remaining live-value overflow paths in Inscriber progress, Charging Station redstone mode, Drone Management overflow count, and Weapon Station frame/FE/module/sight diagnostics. All now use panel-aware pixel fitting; behavior, input routing, and server authority are unchanged.

### Runtime follow-up — compact machine tab collision

Runtime screenshots exposed that four 34-pixel tabs cannot contain the Charging Station labels at the compact test resolution. The Charging Station now uses a two-row tab grid and the Pattern Monitor marks its active tab consistently. The Android inspection column now derives its usable width from the rendered panel and clamps it to a safe minimum, preventing the one-character-per-line wrapping seen in the supplied loadout screenshot.

The same cramped tab geometry was then removed from Decomposer and Microwave. Their four-page navigation now uses two rows with 68-pixel buttons, keeping all labels and hitboxes inside the 145-pixel diagnostic panel.

### Machine navigation — three/four-page label audit

The follow-up source scan found additional undersized tab buttons in Gravitational Stabilizer, Inscriber, Matter Recycler, Solar Panel, Pattern Storage, and Transporter. Their navigation now uses the same two-column, 68-pixel tab grid, providing readable labels and non-overlapping hitboxes at the compact test resolution.

### Android class matrix — inspection width regression

Runtime evidence showed the unified Android class matrix wrapping inspection descriptions one character per line. Its width was incorrectly calculated from the main content's right edge, which lies left of the inspection column and therefore collapsed to one pixel. The inspection width is now derived from the panel's actual right edge with a 180-pixel safety floor, and the geometry validator explicitly rejects the faulty formula.
