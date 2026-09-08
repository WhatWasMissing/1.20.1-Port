# Matter Overdrive 1.20.1 - Current Project Direction

## NON-NEGOTIABLE: STAR MAP IS RETIRED

The Star Map system is permanently retired from this port.

Do **not** add, restore, repair, expand, rebalance, polish, port, or otherwise develop Star Map functionality in future passes. Do not list it as a roadmap item, parity gap, regression target, or candidate feature. Its item/block, GUI/backend, packets, strategic simulation, GuideMe content and gameplay hooks are being removed from the active mod. Other systems must not gain new Star Map dependencies.

Older handoffs that discuss Star Map development are superseded by this file.

## Active development direction

Continue work on the rest of Matter Overdrive: Android progression and Drone Commander, quests/NPCs, matter/network infrastructure, machines and configuration, structures/worldgen, fusion reactor/anomalies, weapons, GuideMe, transporter/security, dimensional pylon, visual parity, stability and new systems that fit the mod's technological scope.

## Newly introduced systems

### Android Chassis Hardware

Androids now have a persistent physical hardware layer with five slots: Core, Frame, Muscles, Optics and Shell. Modules are swappable by right-clicking the module while converted; replacing a module returns the old one to the player. Current modules include capacitor/overclock cores, lightweight/reinforced frames, agility/siege myomers, hunter/precision optics and stealth/reactive shells.

This system should be expanded rather than replaced. Good follow-up work includes an Android Station chassis-management GUI, crafting progression, better unique module visuals, exact HUD reporting of installed hardware and more interactions with subclasses/Drone Commander.

### Facility Network Controller

A Facility Network Controller block now performs live topology/telemetry scans over the Matter Network. It reports connected clients, FE/matter endpoints and aggregate storage. Sneak-use reports a device inventory. This is the foundation for a larger facility-management UI; future work can add machine status, alarms, remote enable/disable, throughput history and route diagnostics.

### Anomaly Containment Unit

The Anomaly Containment Unit captures a Gravitational Anomaly, removes it from the world, stores its accumulated mass in item NBT and redeploys the same mass elsewhere. This exists specifically so players do not need to build permanent bases around naturally generated anomalies. Keep this behavior available and preserve anomaly mass across relocation.

## Testing priorities for this pass

1. Build locally; GitHub Actions should not be required.
2. Convert to Android and install/swap every chassis slot type; verify the replaced module is returned.
3. Confirm chassis state survives death/relog and its combat/mobility effects apply only to Androids.
4. Place Facility Network Controller against a Network Pipe/Router/Switch topology and verify normal-use summary plus sneak-use device inventory.
5. Capture an anomaly with non-default mass, move away, redeploy it into empty space and verify the same mass is restored.
6. Verify no Star Map item/system is reintroduced while implementing any of the above.
