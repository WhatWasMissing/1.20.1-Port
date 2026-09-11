# Overdrive Incident Lore Expansion — Runtime Test Plan

Branch target: `feature/lead-dev-expansion-2026-09-11`

This pass expands the sixteen structure dossiers into a chronological campaign archive and adds five evidence-based incident reconstructions. It deliberately keeps the existing sixteen discovery bits stable so old worlds retain their discoveries.

## Static gate

Run:

```bat
VALIDATE_STRUCTURE_LORE.bat
```

Expected result: `RESULT: PASS`.

The validator now checks:

- all sixteen structure IDs still exist;
- discovery bits remain exactly 0 through 15;
- chronological archive indexes are exactly 1 through 16 with no duplicates;
- all five reconstructions exist;
- `THE CLOSED LOOP` requires all sixteen records;
- expanded dossier fields are written;
- the PDA exposes source, excerpt, forensic and reconstruction views;
- lore SavedData is global through the Overworld data store;
- rare archive pieces remain chunk-safe;
- completion reward and advancement wiring remain present.

## Build gate

```bat
gradlew.bat compileJava --no-daemon
gradlew.bat build --no-daemon
```

Do not weaken the lore system to bypass a compile failure. Fix API/signature issues directly.

## Existing-world compatibility

Use a world that already discovered at least one structure before this expansion.

- [ ] Existing discoveries are still present in the PDA.
- [ ] No previously discovered structure has to be rediscovered.
- [ ] Archive ordering may change because chronology is now independent from discovery-bit order; this is expected.
- [ ] Discovery count remains correct.
- [ ] Moving to the Nether or End and opening the PDA does not show an empty archive.
- [ ] Returning to the Overworld preserves the same mask.
- [ ] Save/reload preserves the same archive and reconstruction state.

## PDA Data Bank

Open the Data Pad and select `DATA BANK`.

For an unrecovered entry:

- [ ] the chronological archive slot is visible;
- [ ] title/body remain hidden;
- [ ] the UI says the record has not been recovered;
- [ ] navigating through all three file pages never leaks the unrecovered story text.

For a recovered entry:

### File 1 — source file

- [ ] archive index is correct;
- [ ] timestamp is visible;
- [ ] record title and facility are visible;
- [ ] classification is visible;
- [ ] source/author is visible;
- [ ] site function is visible;
- [ ] primary record summary is readable.

### File 2 — recovered excerpt

- [ ] transcript/excerpt is readable;
- [ ] PDA forensic analysis is readable;
- [ ] no text renders outside the main panel at the normal GUI scale.

### File 3 — incident implication

- [ ] implication is readable;
- [ ] cross-reference is readable;
- [ ] authentication/reconstruction state appears.

Navigate backward and forward across an entry boundary:

- [ ] `NEXT` from file 3 advances to file 1 of the next archive entry;
- [ ] `PREV` from file 1 returns to file 3 of the previous entry;
- [ ] first/last navigation buttons correctly disable at the limits.

## Chronology verification

Confirm the PDA archive order is:

1. DUSTWELL Excavation
2. MNEMOSYNE Deep Matter Vault
3. KESTREL Matter Refinery
4. Atlas Freight 12
5. HELIX Manufacturing
6. NEREID Underwater Base
7. ECHO-9 Quantum Relay
8. Halcyon-7 Crashed Ship
9. Voss Research Residence
10. JANUS Quarantine Site
11. MORROW Android Safehouse
12. Bastion Command Bunker
13. HEPHAESTUS Drone Foundry
14. ICARUS Fusion Complex
15. ORPHEUS Black Site
16. LAGRANGE Recovery Array

The order the player physically finds structures must not change this list.

## Physical dossier test

Enter a structure class that has not yet been discovered by the test player.

- [ ] exactly one dossier is awarded for the first discovery;
- [ ] repeating entry does not duplicate the one-time discovery dossier;
- [ ] dossier name matches the record title;
- [ ] tooltip includes archive entry, timestamp/classification, source, primary summary, field analysis and cross-reference;
- [ ] NBT includes `LoreTimestamp`;
- [ ] NBT includes `LoreClassification`;
- [ ] NBT includes `LoreSitePurpose`;
- [ ] NBT includes `LoreExcerpt`;
- [ ] NBT includes `LoreAnalysis`;
- [ ] NBT includes `LoreImplication`;
- [ ] `ArchiveIndex` matches chronology, not bit/discovery order.

## Incident reconstructions

Open the PDA and select `INCIDENT`.

There should be five reconstruction pages:

1. THE MATTER CHAIN
2. THE IMPOSSIBLE SIGNAL
3. THE SYNTHETIC SCHISM
4. PROJECT OVERDRIVE
5. THE CLOSED LOOP

For a locked reconstruction:

- [ ] it shows current evidence count;
- [ ] it lists required source-chain slots;
- [ ] recovered required records are visibly checked;
- [ ] unrecovered required records remain anonymous/locked;
- [ ] the full conclusion is not displayed.

When the final required record for a reconstruction is discovered:

- [ ] chat announces `ARCHIVE RECONSTRUCTION COMPLETE`;
- [ ] subtitle is shown;
- [ ] 75 XP is granted once;
- [ ] re-entering structures does not grant the reconstruction XP again;
- [ ] opening the INCIDENT page now displays the authenticated lead, evidence, analysis and conclusion.

## Reconstruction source masks

Verify the following evidence relationships by normal play or controlled test worlds:

### THE MATTER CHAIN

Requires DUSTWELL, MNEMOSYNE, KESTREL, Atlas and HELIX.

### THE IMPOSSIBLE SIGNAL

Requires NEREID, ECHO-9, Halcyon-7 and LAGRANGE.

### THE SYNTHETIC SCHISM

Requires HELIX, Voss, JANUS, MORROW, Bastion and HEPHAESTUS.

### PROJECT OVERDRIVE

Requires KESTREL, JANUS, ICARUS and ORPHEUS.

### THE CLOSED LOOP

Requires every one of the sixteen primary records.

## Full archive completion

After all sixteen records are recovered:

- [ ] archive header reads `16/16`;
- [ ] all five reconstructions are authenticated;
- [ ] THE CLOSED LOOP text is visible;
- [ ] player receives 500 completion XP once;
- [ ] player receives the `Closed Loop Artifact` once;
- [ ] artifact lore includes the causal-chain shorthand;
- [ ] artifact lore includes `No external origin identified.`;
- [ ] artifact lore includes `DO NOT COMPLETE THE LOOP`;
- [ ] the campaign advancement still triggers as intended;
- [ ] relogging or revisiting structures does not duplicate the completion reward.

## Narrative consistency smoke test

While reading the entire archive, verify:

- [ ] M-0 is first discovered at DUSTWELL;
- [ ] MNEMOSYNE identifies future-era tool marks;
- [ ] KESTREL distributes resonance rather than containing it;
- [ ] HELIX anomalies occur before intentional JANUS convergence experiments;
- [ ] NEREID and ECHO-9 establish pre-event acausal evidence;
- [ ] GLASS KNIFE is a purge/refusal conflict, not rewritten as an unprovoked Android attack;
- [ ] Kade, MORROW and HEPHAESTUS preserve mixed human/synthetic moral complexity;
- [ ] Rook had access to the major warnings before zero hour;
- [ ] ICARUS is a deliberate convergence experiment entering runaway feedback;
- [ ] LAGRANGE links post-ICARUS debris to pre-ICARUS M-0;
- [ ] the final archive presents the Closed Loop as the best-supported reconstruction, not omniscient certainty about an external origin;
- [ ] `DO NOT COMPLETE THE LOOP` remains of uncertain authorship.

## Regression checks

- [ ] Data Pad still opens the PDA when GuideME is installed.
- [ ] Technical Manual button still opens GuideME when available.
- [ ] Built-in technical manual fallback still works without GuideME.
- [ ] Research tab still shows research data.
- [ ] Operations tab still shows Field Operations.
- [ ] Contracts tab still lists and can abandon contracts with confirmation.
- [ ] Scan Log still shows recorded block scans.
- [ ] no retired Star Map functionality has returned.
