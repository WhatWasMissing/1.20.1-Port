# Legacy Processing Machine Redstone Configuration

Authoritative references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0.

The legacy `MOTileEntityMachine` base exposed one shared configuration primitive across processing machines: a three-state redstone mode.

- `LOW` — active when the machine is **not** receiving a redstone signal.
- `HIGH` — active while the machine **is** receiving a redstone signal.
- `DISABLED` — ignore redstone and operate whenever normal machine requirements are met.

The 1.20 port defaults existing and new processing machines to `DISABLED` so worlds created before this parity pass retain their prior always-available behavior until the player chooses a mode.

## Restored first-stage machines

- Decomposer
- Matter Recycler
- Microwave

For these machines, redstone controls the active processing cycle only. Battery-to-machine charging remains available while paused. The Decomposer also continues exposing/storing/outputting already-created matter while its decomposition cycle is paused. An in-progress valid cycle is preserved when redstone blocks work and resumes when the mode becomes active again; invalid recipes/inputs still reset progress according to the existing backend.

Each operator screen now has a real `CONFIG` page and server-side `CYCLE RS` action. The synchronized menu state displays the current mode instead of presenting a decorative button.

## Why this is staged

Machines with richer bespoke behavior (Replicator network tasks, Inscriber recipes, Charging Station charge targets, Space-Time Accelerator area effects, Matter Analyzer scan workflow, Transporter dispatch, Solar generation, reactor/stabilizer controls) are audited separately before receiving the generic gate. The legacy base class does not justify blindly freezing unrelated modern networking/storage/charging side effects. Reactor and Stabilizer already have source-backed dedicated redstone controls and must not receive a duplicate generic mode.
