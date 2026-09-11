# The Overdrive Incident — Structure Story Campaign

This pass turns structure exploration into one connected narrative rather than sixteen independent abandoned locations.

## Narrative spine

The campaign is called **The Overdrive Incident**.

The six legacy structures introduce apparently unrelated warning signs:

1. **Crashed Ship / Halcyon-7** — Pilot Mara Venn diverts after Quantum Relay ECHO-9 broadcasts impossible coordinates.
2. **Cargo Ship / Atlas Freight 12** — synthetic chassis and Tritanium are rerouted to HELIX Manufacturing under classified authority.
3. **Underwater Base / NEREID** — Dr. Saira Holt records a gravity tide before the first public anomaly event.
4. **Mad Scientist House / Voss Notes** — Dr. Elias Voss steals quarantine telemetry to experiment with synthetic cognition and exotic Matter.
5. **Android Safehouse / MORROW** — Unit A-17 shelters defecting Androids after the GLASS KNIFE purge order.
6. **Excavation Site / DUSTWELL** — SAMPLE M-0, a pre-collapse Matter lattice, is recovered and shipped to MNEMOSYNE.

The six modern facilities reconstruct the institutional chain of failure:

7. **HELIX Manufacturing Plant** — Directive-0 security frames are rushed into production.
8. **KESTREL Matter Refinery** — M-0 resonance contaminates derived Matter batches.
9. **ECHO-9 Quantum Relay** — a distress packet arrives before it is transmitted.
10. **Bastion Android Command Bunker** — Operation GLASS KNIFE orders self-directed synthetics detained.
11. **ICARUS Fusion Complex** — a runaway anomaly event follows an ORPHEUS remote override.
12. **ORPHEUS Black Site** — Director Cassian Rook intentionally combines anomaly resonance, Matter replication and synthetic cognition under the OVERDRIVE protocol.

The four Frontier locations reveal the deeper truth:

13. **MNEMOSYNE Deep Matter Vault** — M-0 resonance propagated through every derived Matter batch.
14. **HEPHAESTUS Drone Foundry** — autonomous worker drones reject an existentially unsafe command and begin protecting humans and synthetics.
15. **JANUS Quarantine Site** — M-0 resonance synchronizes with Android neural lattices rather than destroying them.
16. **LAGRANGE Orbital Recovery Array** — an orbital object broadcasts: **DO NOT COMPLETE THE LOOP.**

Together, the records establish that the catastrophe was not a random industrial accident. ORPHEUS deliberately completed the chain that other facilities were independently warning about.

## Runtime implementation

`StructureLoreEvents` checks native `StructureManager` membership when a player enters a new chunk. Each structure class has exactly one one-time record per player.

Recovered dossiers use the existing `facility_research` item and contain:

- `LoreArc=OVERDRIVE_INCIDENT`
- structure ID
- record title
- author/unit
- incident summary
- cross-reference
- current `LoreProgress`

The dossier receives a custom hover name and readable lore lines. Duplicate visits to the same structure class do not duplicate the record.

`StructureLoreSavedData` stores a 16-bit per-player discovery mask. Completion of all sixteen records awards:

- 500 XP from the structure-lore system,
- a named **Closed Loop Artifact** tagged `LoreArc=OVERDRIVE_INCIDENT_COMPLETE`,
- the **Reconstruct the Incident** challenge advancement, which separately grants its advancement XP reward.

## Rare archive layouts

Modern and Frontier layout `2` now adds a `RareArchiveMezzaninePiece` inside the main facility volume.

The mezzanine is deliberately optional:

- it is raised above the ground-level critical route,
- a three-wide stair provides bidirectional access,
- the centre lane below remains untouched,
- it contains a lectern, archive shelving and seeded story cache,
- it may contain a finite facility-security spawner,
- it uses normal chunk clipping and never force-loads neighbouring chunks.

This gives the deterministic rare layout a meaningful exploration difference without risking another disconnected-room topology problem.

## Design rule

Lore is additive to the existing campaign rather than a replacement for research progression, Frontier discovery, threat clearance or advancements. Structure exploration should now reward three different things:

1. **material progression** through guarded salvage/research caches,
2. **campaign progression** through research and advancements,
3. **narrative understanding** through the Overdrive Incident archive.
