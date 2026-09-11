# PDA + GuideME Integration Test Plan

## Static gate

Run:

```bat
VALIDATE_PDA_GUIDEME_INTEGRATION.bat
```

Expected result: `PDA / GuideME integration validation: PASS`.

Then run:

```bat
gradlew.bat compileJava --no-daemon
gradlew.bat build --no-daemon
```

## Matrix A — GuideME not installed

1. Launch without GuideME.
2. Right-click the Matter Overdrive Data Pad.
3. Confirm the **Matter Overdrive PDA** opens rather than a documentation-only screen.
4. Confirm Overview, Data Bank, Facilities, Research, Operations, Contracts and Scan Log are accessible.
5. Press **TECH MANUAL • BUILT-IN**.
6. Confirm the bundled Matter Overdrive System Guide opens.
7. Use the standalone System Guide item and confirm it also opens the bundled System Guide.
8. Confirm research, Field Operations, contracts and scans remain usable.

## Matrix B — GuideME installed

1. Install the supported GuideME build.
2. Launch and open the Data Pad.
3. **Blocking requirement:** the Matter Overdrive PDA must still open first. GuideME must not replace it.
4. Confirm all PDA sections remain available and show the same gameplay state as Matrix A.
5. Press **TECH MANUAL • GUIDEME**.
6. Confirm the registered Matter Overdrive GuideME manual opens at its start page.
7. Use the standalone System Guide item and confirm it also opens GuideME.
8. Close/reopen the PDA and verify no research/lore/operations state was lost.

## Lore archive tests

Use a fresh test player where practical.

1. Open the PDA before discovering a Matter Overdrive structure.
   - Reconstruction should show `0/16`.
   - Data Bank entries should be locked.
   - Facilities should not reveal undiscovered facility names.
2. Enter any one of the 16 structure classes.
3. Confirm the recovered physical dossier is granted only once.
4. Confirm the recovery message reports both canonical Archive Entry and reconstruction progress.
5. Reopen the PDA.
   - Reconstruction should show `1/16`.
   - The matching canonical Data Bank entry should now show title, facility, author, summary and cross-reference.
6. Discover a structure out of canonical order, for example ORPHEUS/Black Site before an earlier record.
   - It must display as its fixed archive entry, not as “entry 2”.
   - Reconstruction should increment normally.
7. Revisit another instance of the same structure type.
   - No duplicate unique dossier.
   - Archive count unchanged.
8. Recover all 16 records and verify the Closed Loop Artifact/completion reward remains functional.

## Cross-dimension persistence

1. Recover at least one Overdrive Incident record in the Overworld.
2. Open the PDA and note the count.
3. Travel to the Nether and open the PDA again.
4. Travel to the End and repeat if available.
5. The lore mask/count must remain identical in every dimension.

## Existing Data Pad functions

Verify the PDA redesign did not remove existing gameplay functions:

- Research programme status is visible.
- Research roadmap/progression remains visible.
- Field Operations status/completion remains visible.
- Active carried contracts are listed.
- Contract abandon remains a two-click confirmation.
- Scanning a block records it.
- Scan history is visible after reopening the PDA.
- Legacy quest scanner behaviour still works for its supported crops.

## GuideME failure fallback

With GuideME installed, deliberately test a state where the MO guide cannot open if practical (for example a temporary development resource error).

The Technical Manual action must fall back to the bundled `DocumentationScreen` rather than closing the PDA with no result or crashing.

## Multiplayer isolation

With two players:

1. Player A discovers one or more lore sites.
2. Player B does not.
3. Both open their PDAs.
4. Each player must see their own archive reconstruction mask/count.
5. Research/contract/scan state must also remain player-specific as before.

## UI/readability

At common GUI scales:

- Sidebar remains usable.
- Technical Manual button does not overlap navigation.
- Data Bank summaries wrap rather than clip.
- Locked records do not reveal undiscovered facility names.
- Contract controls remain reachable.
- PREV/NEXT appears only for Data Bank/Facilities.
- No text extends outside the PDA panel.

Any failure where GuideME installation prevents access to the PDA, lore, research, operations, contracts or scan history is a release-blocking regression.
