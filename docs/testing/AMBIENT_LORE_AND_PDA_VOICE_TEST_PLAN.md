# Ambient Lore and Expanded PDA Voice — Runtime Test Plan

Branch: `feature/lead-dev-expansion-2026-09-11`

## Static gate

Run:

```bat
VALIDATE_ADVANCEMENT_DIALOGUE_QOL.bat
```

Expected result: validator reports at least 64 PDA voice lines and exactly 32 optional physical lore records, with voice manifest parity and all ambient IDs present in the ambient loot table.

## Data load / build

Run:

```bat
gradlew.bat compileJava --no-daemon
gradlew.bat build --no-daemon
```

Verify no advancement, loot-table, item-model or registry data errors occur during Minecraft startup.

## Physical lore item

Use `/loot` or structure exploration to obtain several `recovered_lore_fragment` items.

For each sampled record verify:

- item name changes to the authored record title;
- tooltip shows classification and physical source;
- right-click displays the full recovered excerpt and PDA analysis;
- first authentication grants 15 XP and queues the matching PDA voice line;
- the item is not consumed;
- a second copy remains readable but does not grant another 15 XP;
- relogging and dimension travel preserve authentication state.

Also test a deliberately invalid/no-NBT fragment in Creative. It should report that the archive identifier is missing rather than crashing.

## Chest discovery

Generate fresh structures after installing this build. Old already-generated chests are not expected to retroactively gain new loot.

Verify at least one sample from each category:

- modern facility local chest;
- shared salvage chest in a legacy structure;
- Frontier structure using shared salvage;
- story/archive cache.

Modern local checks:

- HELIX only uses `helix_blank_chassis` / `helix_line_worker` for its local-lore roll;
- KESTREL uses `kestrel_recall_draft` / `kestrel_cleaning_loop`;
- ECHO-9 uses `echo9_operator_note` / `echo9_queue_fragment`;
- Bastion uses `bastion_kade_draft` / `bastion_armory_notice`;
- ICARUS uses `icarus_interlock_note` / `icarus_last_coffee`;
- Black Site uses `orpheus_redaction_key` / `orpheus_director_memo`.

The shared salvage roll should be uncommon rather than guaranteed. Story caches should surface ambient records much more frequently.

## PDA FIELD LOGS

Authenticate at least three records, then open the Data Pad.

Verify:

- new `FIELD LOGS` tab is visible;
- collection count matches authenticated SavedData;
- most recently recovered record is selected initially;
- PREV and NEXT page through recovered records only;
- title, classification, site, source, excerpt and PDA analysis are readable;
- long record text wraps rather than drawing outside the content panel;
- READ ALOUD narrates the selected record;
- STOP ends narration;
- switching tabs stops narration;
- optional records do not change the primary 16/16 Incident archive count.

Test at small GUI scale/window sizes and confirm the ninth sidebar tab does not overlap the Technical Manual button at normal supported resolutions.

## Facility-specific PDA discovery lines

In a fresh test world or with fresh player SavedData, enter representative facilities and verify the first canonical discovery now uses a site-specific line after any immediate hazard warning.

Test at least:

- DUSTWELL -> `site_dustwell`;
- ECHO-9 -> `site_echo9`;
- MORROW -> `site_morrow`;
- ICARUS -> `site_icarus`;
- ORPHEUS -> `site_orpheus`;
- LAGRANGE -> `site_lagrange`.

The notification queue should serialize a hazard warning and facility-history line rather than speaking them over one another.

## Optional-lore PDA voice lines

Authenticate records from at least four different chapters. Confirm the queued line is a short interpretation, not a verbatim reading of the full note.

With no prerecorded WAV installed, confirm OS TTS/Narrator/captions still provide a fallback.

With locally processed WAVs installed under `assets/matteroverdrive/pda_voice/`, confirm prerecorded audio is preferred.

## Advancement milestones

Authenticate 16 distinct optional records:

- `Field Archivist` should unlock exactly once.

Authenticate all 32:

- `Every Scrap Matters` should unlock exactly once;
- challenge XP reward should apply once;
- duplicate records must not change the recovered count or re-award either advancement.

## Save/multiplayer checks

With two players on one server:

- each player has independent optional-lore collection state;
- one player's authentication does not authenticate the other player's PDA;
- physical duplicate items can be traded normally;
- both players can authenticate the same lore ID independently;
- server restart preserves both collections.

## Regression checks

Confirm:

- all original 16 canonical dossiers still unlock exactly once;
- all five Incident reconstructions still use the original stable discovery mask;
- World Archivist and existing campaign advancements still load;
- GuideME still opens only as the technical manual;
- no Star Map appears;
- no world generation path force-loads chunks;
- structure generation/traversal remains unchanged by this pass.
