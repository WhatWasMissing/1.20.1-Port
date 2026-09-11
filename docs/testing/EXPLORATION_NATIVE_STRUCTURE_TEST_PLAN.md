# Exploration-Native Structure Runtime Test Plan

Branch: `feature/lead-dev-expansion-2026-09-11`

## Static gate
Run `VALIDATE_STRUCTURE_TOPOLOGY.bat`. Any FAIL blocks release/testing acceptance.

## New-world requirement
Use a fresh world after pulling this pass. Existing generated chunks may still contain old serialized structure pieces by design.

## Per-structure playthrough
For each of the 16 structure families:
1. `/locate structure matteroverdrive:<id>`.
2. Approach from ordinary surrounding terrain rather than teleporting into the structure.
3. Enter without mining, placing blocks, flight or creative movement.
4. Follow the visually strongest route first.
5. Reach the guarded focal cache.
6. Confirm security Androids spawn only in usable side space and do not seal the route.
7. Inspect optional salvage/story rooms.
8. Confirm no functioning Matter Overdrive production/network/reactor machines are present as free loot/infrastructure.
9. Return to terrain without breaking or placing blocks.
10. Repeat across at least three starts where practical.

## Legacy six
### Crashed Ship
- torn approach/ramp works both directions;
- central wreck lane remains readable;
- engineering focal cache reachable;
- breach/debris never seals the return path.

### Cargo Ship
- ship is grounded and boardable;
- loading approach leads into the longitudinal spine;
- cargo side rewards do not obstruct the main route;
- guarded engineering cache is reachable and escapable.

### Underwater Base
- south airlock is usable underwater;
- hub and radial tubes remain dry/usable as intended;
- no water/terrain seam seals a pod connection.

### Mad Scientist Lab
- residence-to-basement staircase is continuous after generation;
- lab shell does not erase the traversal repair overlay;
- focal research cache has usable combat space.

### Android Safehouse
- west/east/rear corridors genuinely meet their rooms;
- no off-by-one sealed wing remains.

### Excavation Site
- continuous descent reaches relic floor;
- no jump/placement is needed on the L-shaped descent.

## Modern six
### Synthetic Manufacturing Plant
- gate -> checkpoint -> production -> dispatch route reads clearly;
- assembly/fabrication wings are optional branches;
- focal dispatch archive is guarded but not doorway-blocked.

### Matter Refinery
- entry/security/core route works;
- `ModernTraversalRepairPiece` bridges core to lowered excavation wing;
- processing focal cache is reachable.

### Quantum Relay
- entrance/control/core path is clear;
- masts are visually supported and recognizable;
- side signal/power rooms reconnect cleanly.

### Android Command Bunker
- surface entrance reaches bunker level via continuous descent;
- drone/barracks wings do not become mandatory maze branches;
- armory is guarded focal reward.

### Fusion Research Complex
- entrance/control leads naturally to circular reactor chamber;
- chamber has enough combat floor;
- no inert reactor scenery blocks cardinal movement.

### Black Site
- surface entrance reaches security/core;
- core-to-deep stair begins from a continuous floor connection;
- deep vault is reachable and returnable;
- strongest guard package has enough combat space.

## Frontier four
### Deep Matter Vault
- surface archive is visible/reachable;
- long descent is bidirectional;
- guarded vault core reachable;
- side archive/refinery ruins remain optional.

### Autonomous Drone Foundry
- entry/security/control/fabrication/hangar progression is readable;
- drone-rack scenery does not block lanes;
- hangar supports combat and return route.

### Anomaly Quarantine Site
- surface approach and descent work;
- decon/observation/containment functions are visually distinct;
- containment cache is guarded without sealing the room.

### Orbital Recovery Array
- approach/control/core route is clear;
- antenna reads as exterior landmark;
- storage/signal side rooms reconnect.

## Old-world compatibility
Load a copy of an existing test world containing pre-pass structures and confirm registered legacy/technology/frontier piece serializers still load without missing-piece errors. Old structures may retain old layouts; the sanitizer is expected to prevent old generated working machines from remaining free infrastructure after relevant chunks load.

## Failure rule
Any unreachable entrance, sealed required room, one-way drop without exit, guard blocking the only route, free functional machine in a new structure, or inability to return to terrain is a blocking generation bug.
