# Reactor Operating Profile and Containment Test Plan

1. Assemble a valid Fusion Reactor with an anomaly, powered stabilizer, matter, and an output receiver.
2. Run `/matteroverdrive reactor status` within 32 blocks and confirm it matches the controller GUI.
3. Open the controller and record the initial profile, heat, stability, output, and matter drain.
3. Cycle `PROFILE` through Balanced, Overdrive, and Conservation. Confirm the server message and menu values update for the same controller.
4. Run each profile for at least 30 seconds. Confirm Overdrive produces more output and heat, Conservation produces less output and heat, and matter drain follows the profile.
5. Remove stabilizer power while operating. Confirm stability falls and the UI reports the changing containment state.
6. Scram the reactor or remove its output demand. Confirm heat cools and stability recovers while the reactor is not generating.
7. In a controlled creative test, allow heat to reach the cap. Confirm `THERMAL RUNAWAY — SCRAMMED`, zero generation, and bounded values rather than a runaway tick loop.
8. Stand within 24 blocks while heat crosses 80% and reaches runaway. Confirm one rate-limited warning and one automatic-SCRAM alarm appear in the operator action bar, with flame particles and a distinct alarm tone at the controller.
9. Save/reload and restart the server. Confirm profile, heat, stability, and scram state persist.
10. With two players viewing the same controller, confirm both menus receive identical profile/heat/stability values and neither can alter the other's player state.
11. Connect the controller to a Facility Network Controller and Holographic Status Panel. Confirm the shared telemetry reports the highest reactor heat and lowest containment stability across loaded reactors, adds the matching heat/stability alarms at 80%/25% thresholds, and renders the compact `R <heat>/<stability>` summary in the panel overview.
12. Deploy a Reactor Maintenance drone within four blocks of the controller. While heat is at least 650 or stability is below 1000, confirm each service pulse consumes 300 drone energy, reduces heat by 40, restores stability by 30, and clears a thermal-runaway fault only after heat falls below 500. Confirm a healthy reactor is not drained by idle maintenance polls.
13. As an operator, run `/matteroverdrive reactor scram` within 32 blocks and confirm generation stops immediately with `Manual SCRAM`; after heat cools below 500 and stability remains above zero, run `/matteroverdrive reactor reset` and confirm the controller returns to `Ready`. Confirm reset is denied while containment is unsafe.

Static/build evidence is provided by `scripts/validate_reactor_profiles.py` and `tools/full-sanity-check.ps1`. Interactive stress, recovery, persistence, and multiplayer checks remain **Needs runtime verification**.

The validator also enforces that heat and stability are clamped to `0..1000`, that thermal runaway and containment failure both scram the reactor, and that the expanded 37-entry menu synchronization contract remains aligned.

Network telemetry and panel field wiring are covered statically by `scripts/validate_network_transport_consistency.py`; placement, threshold crossing, reload, and multiplayer fan-out remain **Needs runtime verification**.
