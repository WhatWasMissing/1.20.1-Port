# Android three-class active ability pass

Branch: `work/0.3-titan-artifact-drone-specialisation`

## Class structure

The Android system now has three top-level combat frames:

- **Strider** — agile hunter-style frame built around mobility, cloak, target marking and precision positioning.
- **Juggernaut** — titan-style frontline frame built around force fields, kinetic pressure, powered melee and durability.
- **Architect** — warlock-style synthetic technomancer built around FE recursion, nanites, control and drone command.

Existing specialisations are retained underneath the classes so old saved loadouts remain meaningful:

- Strider -> Utility
- Juggernaut -> Assault / Chassis
- Architect -> Drone Commander

## Active class abilities

Class abilities unlock at Android level 2 and use server-authoritative FE + cooldowns.

### Strider

**Reflex Shift** — 5,000 FE, 10s cooldown. Rapid forward evasive burst, short cloak, Speed II and brief damage resistance.

**Predator Sweep** — 7,000 FE, 18s cooldown. Marks and weakens hostiles in a large radius and grants a short hunter cloak + acceleration window.

### Juggernaut

**Bastion Frame** — 6,500 FE, 16s cooldown. Grants Resistance II + Absorption II and pushes nearby enemies away from the operator.

**Seismic Charge** — 9,000 FE, 20s cooldown. Close-range kinetic detonation dealing 10 damage and strong launch/knockback to nearby hostiles.

### Architect

**Restoration Well** — 6,000 FE, 18s cooldown. Grants regeneration + absorption and repairs/reinforces nearby linked drones.

**Nanite Surge** — 8,000 FE, 20s cooldown. Applies poison, weakness and target marking to nearby hostiles; heals and buffs linked drones and recycles FE per affected target.

## Class combat intrinsics

- **Strider:** passive powered movement speed. Ordinary attacks gain bonus damage, increased further against marked/glowing targets and while sprinting; marked-target attacks recycle a little FE.
- **Juggernaut:** passive powered damage resistance. Ordinary player attacks can spend 220 FE for +3 damage and additional knockback.
- **Architect:** passive ambient FE recycling and drone regeneration. Ordinary player attacks recycle FE, weaken the target and periodically repair nearby linked drones.

## Controls

- `B` — activate currently selected legacy Android ability.
- `V` — cycle legacy Android ability.
- `H` — activate class ability.
- `N` — activate tech ability.
- `G` — activate Ultimate.
- `L` — open three-class Android loadout screen.
- `K` — open permanent Ascension tree.

All keys remain rebindable through Minecraft Controls.

## Manual testing

1. Convert to Android and reach level 2.
2. Open the class loadout with L and switch between Strider, Juggernaut and Architect.
3. Verify H executes the correct class ability for each frame and consumes the documented FE.
4. Verify H cannot be spammed during its cooldown.
5. Verify N executes the correct tech ability and consumes FE.
6. Verify N cannot be spammed during its cooldown.
7. Strider: confirm Reflex Shift changes velocity, briefly cloaks and accelerates the player.
8. Strider: confirm Predator Sweep marks nearby hostile mobs and increases damage against them.
9. Juggernaut: confirm Bastion grants protection and repels nearby enemies.
10. Juggernaut: confirm Seismic Charge damages and launches nearby hostile mobs.
11. Architect: link at least one drone, activate Restoration Well and verify player/drone recovery.
12. Architect: activate Nanite Surge near mobs + linked drones and verify hostile debuffs, drone buffs and FE return.
13. Verify ordinary combat differs by class according to the intrinsic descriptions above.
14. Verify G still activates the selected specialisation Ultimate after switching class.
15. Relog and verify selected class/specialisation, aspects, fragments, artifact and Drone Commander state persist.
