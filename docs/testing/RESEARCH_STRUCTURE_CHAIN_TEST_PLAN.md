# Research and Structure-Chain Test Plan

## Purpose

Verify the player-facing loop:

`Explore -> discover a compact site -> recover a dossier -> analyze it -> advance research clearance -> unlock equipment`

The optional ordered investigation is:

`android_relay_outpost -> matter_observatory -> anomaly_research_site`

## Fresh-world setup

1. Start the dedicated Matter Overdrive Forge 1.20.1 profile in a fresh test world.
2. Keep a Data Pad, an empty inventory slot, and access to a powered Matter Analyzer.
3. Use the existing structure locate/debug workflow to reach compact sites without editing the damaged `ore test` save.
4. For non-destructive state inspection, run `/matteroverdrive research status`. Operator-only `/matteroverdrive research advance <stage>` advances at most one clearance stage per command for controlled test setup.

## Discovery and dossier flow

1. At each compact site, stand within the bounded discovery radius and confirm the required machine pairing is present.
2. Confirm the server announces a field discovery, grants one Data Pad, and grants a typed `Recovered Research Dossier`.
3. Open the Data Pad and verify the site appears in the discovery ledger and the next investigation step is shown.
4. Use the dossier on a powered Matter Analyzer. Confirm it queues for the credited player and consumes one dossier only after successful analysis.
5. After the analysis completes, verify XP, the archive-specific upgrade/item, and the shared research-clearance stage.

## Ordered-chain and duplicate-reward checks

1. Discover the relay outpost, then the observatory, then the anomaly research site; verify the chain advances 1/3, 2/3, and 3/3 in that order.
2. Confirm the final chain awards exactly one Parallel Processing Upgrade.
3. Revisit any already-discovered site and repeat the interaction; verify no second dossier, XP award, or chain reward is created.
4. Discover sites out of order and verify the ledger records the site but does not skip an intermediate chain stage.

## Persistence and multiplayer checks

1. Save/reload and restart the server between discoveries; verify the per-player archive and shared research stage persist.
2. With two players, have each discover and analyze separate dossiers; verify archive flags, XP, and rewards remain isolated.
3. Have player B attempt to finish a dossier queued by player A; verify the analyzer retains credit for player A and does not award B.
4. Fill the inventory before a reward; verify the item drops safely rather than being lost or duplicated.

## Evidence and maturity

- `scripts/validate_research_chain.py` checks the deterministic chain, server-side analyzer path, Data Pad state, persistence, and duplicate-reward guards.
- `tools/full-sanity-check.ps1` runs that validator before compiling and deploying the dedicated test JAR.
- Static/build maturity: **Functional**.
- Fresh-world traversal, interactive dossier analysis, save/reload, and two-player isolation: **Needs runtime verification**.
