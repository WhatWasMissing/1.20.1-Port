# Advancements / Branching Dialogue / QoL Test Plan

## Static/build gate

Run:

```bat
VALIDATE_ADVANCEMENT_DIALOGUE_QOL.bat
gradlew.bat compileJava --no-daemon
gradlew.bat build --no-daemon
```

Also retain the existing PDA, structure and lore validators.

## Dialogue UI

Test all nine contemporary roles at normal and small GUI scale.

Expected:
- portrait/hologram remains readable;
- dialogue text wraps without clipping;
- long text paginates;
- choices appear only on the final text page;
- up to three choices fit above the control row;
- READ ALOUD remains usable;
- synthetic contacts retain orange presentation;
- humans retain cyan field communication presentation;
- CLOSE always remains available.

## Server validation

For one human and one synthetic NPC:
1. Open conversation normally.
2. Pick a choice.
3. Confirm response corresponds to that branch.
4. Choose `Ask something else` and confirm root choices return.
5. Close and immediately re-open; confirm return-contact wording.
6. Relog and repeat; memory must persist.
7. Travel to Nether/End and return; memory must remain unchanged.

Packet abuse/static inspection expectations:
- client sends only dialogue ID, node ID and choice ID;
- no client-provided response/reward/trust value exists;
- mismatched/expired sessions do nothing;
- sessions are removed on logout.

## Contact progression

Accumulate Field Team Trust through researcher/salvager/recovery/ICARUS/JANUS/Archivist branches.
- At Field Team Trust >= 8, **Field Liaison** should award once.

Accumulate Synthetic Trust through MORROW/Chorus/HEPHAESTUS branches.
- At Synthetic Trust >= 8, **Synthetic Liaison** should award once.

Accumulate Archive Insight across evidence-heavy questions.
- At Archive Insight >= 18, **Incident Analyst** should award once.

Open the PDA after dialogue choices.
- Action/status line should show current FIELD / SYNTHETIC / ARCHIVE INSIGHT values.
- Values must come from the server packet, not stale client-local state.

## New item-driven advancements

Verify each can complete using survival-obtainable systems:
- Pattern Architect: Pattern Storage + Pattern Monitor + Replicator.
- Network Specialist: Network Pipe + Router + Switch + Pylon + Network Flash Drive.
- Applied Energy Weapons: Weapon Station + at least one supported energy weapon.
- Industrialist: Transporter + Charging Station + Space-Time Accelerator + Inscriber + Matter Recycler.
- Containment Engineer: Reactor Controller + Coil + IO + Gravitational Stabilizer + Space-Time Equalizer.
- Full-Spectrum Engineer: representative Matter + network + drone + reactor + containment + weapon infrastructure.

Check advancement screen layout for overlaps or unreasonable horizontal sprawl.

## PDA voice-bank hierarchy

With no prerecorded bank installed:
- short PDA callouts must still use local OS TTS where available;
- Narrator fallback remains available;
- captions always render.

Then place a valid WAV at:
`config/matteroverdrive/pda_voice/icarus_warning.wav`

Trigger ICARUS warning.
Expected: prerecorded file plays instead of System.Speech.

Replace it with an invalid/corrupt WAV.
Expected: no crash; local OS TTS/Narrator fallback still works.

Repeat with a bundled test resource if packaging the final voice bank into the JAR.

## Regression

- Existing legacy scientist one-way dialogue must still open.
- GuideME must still open only through Technical Manual routing.
- PDA Data Bank/Incident READ ALOUD must remain independent of queued short callouts.
- Existing 16 structure discoveries and lore reconstruction must remain unchanged.
- No Star Map UI/items/progression should reappear.
- No structure/worldgen code should gain chunk tickets or force loads.
